#Signature file v4.1
#Version 1.54

CLSS public abstract interface java.awt.datatransfer.Transferable
meth public abstract boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public abstract java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public abstract java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

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

CLSS public abstract interface !annotation java.lang.annotation.Inherited
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

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface !annotation javax.xml.bind.annotation.XmlAccessorType
 anno 0 java.lang.annotation.Inherited()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PACKAGE, TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault javax.xml.bind.annotation.XmlAccessType value()

CLSS public abstract interface !annotation javax.xml.bind.annotation.XmlEnum
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<?> value()

CLSS public abstract interface !annotation javax.xml.bind.annotation.XmlRegistry
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.xml.bind.annotation.XmlRootElement
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String namespace()

CLSS public abstract interface !annotation javax.xml.bind.annotation.XmlSchema
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PACKAGE])
fld public final static java.lang.String NO_LOCATION = "##generate"
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String location()
meth public abstract !hasdefault java.lang.String namespace()
meth public abstract !hasdefault javax.xml.bind.annotation.XmlNsForm attributeFormDefault()
meth public abstract !hasdefault javax.xml.bind.annotation.XmlNsForm elementFormDefault()
meth public abstract !hasdefault javax.xml.bind.annotation.XmlNs[] xmlns()

CLSS public abstract interface !annotation javax.xml.bind.annotation.XmlSeeAlso
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class[] value()

CLSS public abstract interface !annotation javax.xml.bind.annotation.XmlType
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
innr public final static DEFAULT
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class factoryClass()
meth public abstract !hasdefault java.lang.String factoryMethod()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String namespace()
meth public abstract !hasdefault java.lang.String[] propOrder()

CLSS public org.netbeans.modules.websvc.saas.model.CustomSaas
cons public init(org.netbeans.modules.websvc.saas.model.SaasGroup,org.netbeans.modules.websvc.saas.model.jaxb.SaasServices)
meth protected org.netbeans.modules.websvc.saas.model.CustomSaasMethod createSaasMethod(org.netbeans.modules.websvc.saas.model.jaxb.Method)
supr org.netbeans.modules.websvc.saas.model.Saas

CLSS public org.netbeans.modules.websvc.saas.model.CustomSaasMethod
cons public init(org.netbeans.modules.websvc.saas.model.Saas,org.netbeans.modules.websvc.saas.model.jaxb.Method)
meth public java.lang.String getHref()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Method$Input getInput()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Method$Output getOutput()
supr org.netbeans.modules.websvc.saas.model.SaasMethod

CLSS public org.netbeans.modules.websvc.saas.model.Saas
cons public init(org.netbeans.modules.websvc.saas.model.SaasGroup,java.lang.String,java.lang.String,java.lang.String)
cons public init(org.netbeans.modules.websvc.saas.model.SaasGroup,org.netbeans.modules.websvc.saas.model.jaxb.SaasServices)
fld protected final org.netbeans.modules.websvc.saas.model.jaxb.SaasServices delegate
fld protected org.openide.filesystems.FileObject saasFile
fld protected org.openide.filesystems.FileObject saasFolder
fld public final static java.lang.String ARTIFACT_TYPE_LIBRARY = "library"
fld public final static java.lang.String ASMX_EXT = "asmx"
fld public final static java.lang.String NS_SAAS = "http://xml.netbeans.org/websvc/saas/services/1.0"
fld public final static java.lang.String NS_WADL = "http://research.sun.com/wadl/2006/10"
fld public final static java.lang.String NS_WADL_09 = "http://wadl.dev.java.net/2009/02"
fld public final static java.lang.String NS_WSDL = "http://schemas.xmlsoap.org/wsdl/"
fld public final static java.lang.String PROP_LOCAL_SERVICE_FILE = "local.service.file"
fld public final static java.lang.String PROP_PARENT_GROUP = "parentGroup"
fld public final static java.lang.String PROP_STATE = "saasState"
fld public final static java.lang.String SAAS_PROPERTIES = "saas.properties"
fld public final static java.lang.String WADL_EXT = "wadl"
fld public final static java.lang.String WSDL_EXT = "wsdl"
fld public final static java.lang.String XML_EXT = "xml"
fld public final static java.lang.String[] SUPPORTED_EXTENSIONS
fld public final static java.lang.String[] SUPPORTED_TARGETS
innr public final static !enum State
intf java.lang.Comparable<org.netbeans.modules.websvc.saas.model.Saas>
meth protected java.lang.String getProperty(java.lang.String)
meth protected org.netbeans.modules.websvc.saas.model.SaasMethod createSaasMethod(org.netbeans.modules.websvc.saas.model.jaxb.Method)
meth protected void computePathFromRoot()
meth protected void refresh()
meth protected void setParentGroup(org.netbeans.modules.websvc.saas.model.SaasGroup)
meth protected void setProperty(java.lang.String,java.lang.String)
meth protected void setState(org.netbeans.modules.websvc.saas.model.Saas$State)
meth protected void setUserDefined(boolean)
meth public boolean isUserDefined()
meth public int compareTo(org.netbeans.modules.websvc.saas.model.Saas)
meth public java.lang.String getApiDoc()
meth public java.lang.String getDescription()
meth public java.lang.String getDisplayName()
meth public java.lang.String getPackageName()
meth public java.lang.String getUrl()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.SaasMethod> getMethods()
meth public java.util.List<org.openide.filesystems.FileObject> getLibraryJars()
meth public org.netbeans.modules.websvc.saas.model.Saas$State getState()
meth public org.netbeans.modules.websvc.saas.model.SaasGroup getParentGroup()
meth public org.netbeans.modules.websvc.saas.model.SaasGroup getTopLevelGroup()
meth public org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata getSaasMetadata()
meth public org.netbeans.modules.websvc.saas.model.jaxb.SaasServices getDelegate()
meth public org.netbeans.modules.websvc.saas.model.jaxb.SaasServices$Header getHeader()
meth public org.openide.filesystems.FileObject getSaasFile() throws java.io.IOException
meth public org.openide.filesystems.FileObject getSaasFolder()
meth public void save()
meth public void toStateReady(boolean)
meth public void upgrade()
supr java.lang.Object
hfds JAVA_TARGETS,PHP_TARGETS,libraryJars,parentGroup,propFile,props,saasMethods,state,topGroup,userDefined

CLSS public final static !enum org.netbeans.modules.websvc.saas.model.Saas$State
 outer org.netbeans.modules.websvc.saas.model.Saas
fld public final static org.netbeans.modules.websvc.saas.model.Saas$State INITIALIZING
fld public final static org.netbeans.modules.websvc.saas.model.Saas$State READY
fld public final static org.netbeans.modules.websvc.saas.model.Saas$State REMOVED
fld public final static org.netbeans.modules.websvc.saas.model.Saas$State RETRIEVED
fld public final static org.netbeans.modules.websvc.saas.model.Saas$State UNINITIALIZED
meth public static org.netbeans.modules.websvc.saas.model.Saas$State valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.saas.model.Saas$State[] values()
supr java.lang.Enum<org.netbeans.modules.websvc.saas.model.Saas$State>

CLSS public org.netbeans.modules.websvc.saas.model.SaasCatalog
cons public init()
intf org.netbeans.modules.xml.catalog.spi.CatalogDescriptor
intf org.netbeans.modules.xml.catalog.spi.CatalogReader
intf org.xml.sax.EntityResolver
meth public java.awt.Image getIcon(int)
meth public java.lang.String getDisplayName()
meth public java.lang.String getShortDescription()
meth public java.lang.String getSystemID(java.lang.String)
meth public java.lang.String resolvePublic(java.lang.String)
meth public java.lang.String resolveURI(java.lang.String)
meth public java.util.Iterator getPublicIDs()
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void addCatalogListener(org.netbeans.modules.xml.catalog.spi.CatalogListener)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void refresh()
meth public void removeCatalogListener(org.netbeans.modules.xml.catalog.spi.CatalogListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds IMAGE_PATH,RESOURCES_DIR,SAAS_SERVICES_1_0,SAAS_SERVICES_1_0_ID,SCHEMA,URI_SAAS_SERVICES_1_0

CLSS public org.netbeans.modules.websvc.saas.model.SaasGroup
cons public init(org.netbeans.modules.websvc.saas.model.SaasGroup,org.netbeans.modules.websvc.saas.model.jaxb.Group)
fld public final static java.lang.String PROP_GROUP_NAME = "groupName"
intf java.lang.Comparable<org.netbeans.modules.websvc.saas.model.SaasGroup>
meth protected boolean removeChildGroup(org.netbeans.modules.websvc.saas.model.SaasGroup)
meth protected org.netbeans.modules.websvc.saas.model.SaasGroup createGroup(java.lang.String)
meth protected void addChildGroup(org.netbeans.modules.websvc.saas.model.SaasGroup)
meth protected void setIcon16Path(java.lang.String)
meth protected void setIcon32Path(java.lang.String)
meth public boolean canRemove()
meth public boolean isUserDefined()
meth public boolean removeService(org.netbeans.modules.websvc.saas.model.Saas)
meth public boolean serviceExists(java.lang.String)
meth public int compareTo(org.netbeans.modules.websvc.saas.model.SaasGroup)
meth public java.lang.String getIcon16Path()
meth public java.lang.String getIcon32Path()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.Saas> getServices()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.SaasGroup> getChildrenGroups()
meth public org.netbeans.modules.websvc.saas.model.Saas getChildService(java.lang.String)
meth public org.netbeans.modules.websvc.saas.model.SaasGroup getChildGroup(java.lang.String)
meth public org.netbeans.modules.websvc.saas.model.SaasGroup getParent()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Group getDelegate()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Group getPathFromRoot()
meth public void addService(org.netbeans.modules.websvc.saas.model.Saas)
meth public void setName(java.lang.String)
supr java.lang.Object
hfds children,delegate,icon16Path,icon32Path,parent,services,userDefined

CLSS public org.netbeans.modules.websvc.saas.model.SaasMethod
cons public init(org.netbeans.modules.websvc.saas.model.Saas,org.netbeans.modules.websvc.saas.model.jaxb.Method)
intf java.lang.Comparable<org.netbeans.modules.websvc.saas.model.SaasMethod>
meth protected java.lang.String getHref()
meth protected org.netbeans.modules.websvc.saas.model.jaxb.Method$Input getInput()
meth protected org.netbeans.modules.websvc.saas.model.jaxb.Method$Output getOutput()
meth public int compareTo(org.netbeans.modules.websvc.saas.model.SaasMethod)
meth public java.lang.String getDisplayName()
meth public java.lang.String getDocumentation()
meth public java.lang.String getName()
meth public org.netbeans.modules.websvc.saas.model.Saas getSaas()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Method getMethod()
supr java.lang.Object
hfds method,saas

CLSS public org.netbeans.modules.websvc.saas.model.SaasServicesModel
fld public final static java.lang.String PROFILE_PROPERTIES_FILE = "profile.properties"
fld public final static java.lang.String PROP_GROUPS = "groups"
fld public final static java.lang.String PROP_SERVICES = "services"
fld public final static java.lang.String ROOT_GROUP = "Root"
fld public final static java.lang.String SERVICE_GROUP_XML = "service-groups.xml"
fld public final static java.lang.String WEBSVC_HOME
innr public final static !enum State
meth protected void fireChange(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object)
meth public org.netbeans.modules.websvc.saas.model.Saas createSaasService(org.netbeans.modules.websvc.saas.model.SaasGroup,java.lang.String,java.lang.String)
meth public org.netbeans.modules.websvc.saas.model.Saas getTopService(java.lang.String)
meth public org.netbeans.modules.websvc.saas.model.SaasGroup createGroup(org.netbeans.modules.websvc.saas.model.SaasGroup,java.lang.String)
meth public org.netbeans.modules.websvc.saas.model.SaasGroup createTopGroup(java.lang.String)
meth public org.netbeans.modules.websvc.saas.model.SaasGroup getInitialRootGroup()
meth public org.netbeans.modules.websvc.saas.model.SaasGroup getRootGroup()
meth public org.netbeans.modules.websvc.saas.model.SaasGroup getTopGroup(java.lang.String)
meth public org.netbeans.modules.websvc.saas.model.SaasServicesModel$State getState()
meth public org.netbeans.modules.websvc.saas.model.WsdlSaas createWsdlService(org.netbeans.modules.websvc.saas.model.SaasGroup,java.lang.String,java.lang.String,java.lang.String)
meth public static org.netbeans.modules.websvc.saas.model.SaasServicesModel getInstance()
meth public static org.openide.filesystems.FileObject getWebServiceHome()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void initRootGroup()
meth public void refreshService(org.netbeans.modules.websvc.saas.model.Saas)
meth public void removeGroup(org.netbeans.modules.websvc.saas.model.SaasGroup)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeService(org.netbeans.modules.websvc.saas.model.Saas)
meth public void renameGroup(org.netbeans.modules.websvc.saas.model.SaasGroup,java.lang.String)
meth public void saveRootGroup()
supr java.lang.Object
hfds instance,pps,rootGroup,state

CLSS public final static !enum org.netbeans.modules.websvc.saas.model.SaasServicesModel$State
 outer org.netbeans.modules.websvc.saas.model.SaasServicesModel
fld public final static org.netbeans.modules.websvc.saas.model.SaasServicesModel$State INITIALIZING
fld public final static org.netbeans.modules.websvc.saas.model.SaasServicesModel$State READY
fld public final static org.netbeans.modules.websvc.saas.model.SaasServicesModel$State UNINITIALIZED
meth public static org.netbeans.modules.websvc.saas.model.SaasServicesModel$State valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.saas.model.SaasServicesModel$State[] values()
supr java.lang.Enum<org.netbeans.modules.websvc.saas.model.SaasServicesModel$State>

CLSS public org.netbeans.modules.websvc.saas.model.WadlSaas
cons public init(org.netbeans.modules.websvc.saas.model.SaasGroup,java.lang.String,java.lang.String,java.lang.String)
cons public init(org.netbeans.modules.websvc.saas.model.SaasGroup,org.netbeans.modules.websvc.saas.model.jaxb.SaasServices)
meth protected org.netbeans.modules.websvc.saas.model.WadlSaasMethod createSaasMethod(org.netbeans.modules.websvc.saas.model.jaxb.Method)
meth public java.lang.String getBaseURL()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.WadlSaasResource> getResources()
meth public java.util.List<org.openide.filesystems.FileObject> getJaxbSourceJars()
meth public java.util.List<org.openide.filesystems.FileObject> getLibraryJars()
meth public java.util.List<org.openide.filesystems.FileObject> getLocalSchemaFiles() throws java.io.IOException
meth public org.netbeans.modules.websvc.saas.model.oauth.Metadata getOauthMetadata() throws java.io.IOException,javax.xml.bind.JAXBException
meth public org.netbeans.modules.websvc.saas.model.wadl.Application getWadlModel() throws java.io.IOException
meth public org.openide.filesystems.FileObject getLocalWadlFile()
meth public void refresh()
meth public void setJaxbSourceJars(java.util.List<org.openide.filesystems.FileObject>)
meth public void setLibraryJars(java.util.List<org.openide.filesystems.FileObject>)
meth public void toStateReady(boolean)
supr org.netbeans.modules.websvc.saas.model.Saas
hfds jaxbJars,jaxbSourceJars,resources,schemaFiles,wadlFile,wadlModel

CLSS public org.netbeans.modules.websvc.saas.model.WadlSaasMethod
cons public init(org.netbeans.modules.websvc.saas.model.WadlSaas,org.netbeans.modules.websvc.saas.model.jaxb.Method)
cons public init(org.netbeans.modules.websvc.saas.model.WadlSaasResource,org.netbeans.modules.websvc.saas.model.wadl.Method)
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public org.netbeans.modules.websvc.saas.model.WadlSaas getSaas()
meth public org.netbeans.modules.websvc.saas.model.WadlSaasResource getParentResource()
meth public org.netbeans.modules.websvc.saas.model.wadl.Method getWadlMethod()
meth public org.netbeans.modules.websvc.saas.model.wadl.Resource[] getResourcePath()
supr org.netbeans.modules.websvc.saas.model.SaasMethod
hfds DELETE,GET,POST,PUT,displayName,name,parent,path,wadlMethod

CLSS public org.netbeans.modules.websvc.saas.model.WadlSaasResource
cons public init(org.netbeans.modules.websvc.saas.model.WadlSaas,org.netbeans.modules.websvc.saas.model.WadlSaasResource,org.netbeans.modules.websvc.saas.model.wadl.Resource)
intf java.lang.Comparable<org.netbeans.modules.websvc.saas.model.WadlSaasResource>
meth public int compareTo(org.netbeans.modules.websvc.saas.model.WadlSaasResource)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.WadlSaasMethod> getMethods()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.WadlSaasResource> getChildResources()
meth public org.netbeans.modules.websvc.saas.model.WadlSaas getSaas()
meth public org.netbeans.modules.websvc.saas.model.WadlSaasResource getParent()
meth public org.netbeans.modules.websvc.saas.model.wadl.Resource getResource()
supr java.lang.Object
hfds childResources,methods,parent,resource,saas

CLSS public org.netbeans.modules.websvc.saas.model.WsdlSaas
cons public init(org.netbeans.modules.websvc.saas.model.SaasGroup,java.lang.String,java.lang.String,java.lang.String)
cons public init(org.netbeans.modules.websvc.saas.model.SaasGroup,org.netbeans.modules.websvc.saas.model.jaxb.SaasServices)
intf java.beans.PropertyChangeListener
meth protected org.netbeans.modules.websvc.saas.model.WsdlSaasMethod createSaasMethod(org.netbeans.modules.websvc.saas.model.jaxb.Method)
meth protected void refresh()
meth protected void setWsdlData(org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData)
meth public java.lang.String getDefaultServiceName()
meth public java.lang.String getPackageName()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.WsdlSaasPort> getPorts()
meth public org.netbeans.modules.websvc.jaxwsmodelapi.WSService getWsdlModel()
meth public org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData getWsdlData()
meth public org.openide.filesystems.FileObject getLocalWsdlFile()
meth public org.openide.filesystems.FileObject getSaasFolder()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void toStateReady(boolean)
supr org.netbeans.modules.websvc.saas.model.Saas
hfds ports,wsData

CLSS public org.netbeans.modules.websvc.saas.model.WsdlSaasMethod
cons public init(org.netbeans.modules.websvc.saas.model.WsdlSaas,org.netbeans.modules.websvc.saas.model.jaxb.Method)
cons public init(org.netbeans.modules.websvc.saas.model.WsdlSaasPort,org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation)
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation getWsdlOperation()
meth public org.netbeans.modules.websvc.jaxwsmodelapi.WSPort getWsdlPort()
meth public org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaMethod getJavaMethod()
meth public org.netbeans.modules.websvc.saas.model.WsdlSaas getSaas()
supr org.netbeans.modules.websvc.saas.model.SaasMethod
hfds operation,parent,port

CLSS public org.netbeans.modules.websvc.saas.model.WsdlSaasPort
cons public init(org.netbeans.modules.websvc.saas.model.WsdlSaas,org.netbeans.modules.websvc.jaxwsmodelapi.WSPort)
intf java.lang.Comparable<org.netbeans.modules.websvc.saas.model.WsdlSaasPort>
meth public int compareTo(org.netbeans.modules.websvc.saas.model.WsdlSaasPort)
meth public java.lang.String getName()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.WsdlSaasMethod> getWsdlMethods()
meth public org.netbeans.modules.websvc.jaxwsmodelapi.WSPort getWsdlPort()
meth public org.netbeans.modules.websvc.saas.model.WsdlSaas getParentSaas()
supr java.lang.Object
hfds methods,parentSaas,port

CLSS public org.netbeans.modules.websvc.saas.model.jaxb.Artifact
cons public init()
fld protected java.lang.String id
fld protected java.lang.String requires
fld protected java.lang.String type
fld protected java.lang.String url
meth public java.lang.String getId()
meth public java.lang.String getRequires()
meth public java.lang.String getType()
meth public java.lang.String getUrl()
meth public void setId(java.lang.String)
meth public void setRequires(java.lang.String)
meth public void setType(java.lang.String)
meth public void setUrl(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.jaxb.Artifacts
cons public init()
fld protected java.lang.String profile
fld protected java.lang.String targets
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.Artifact> artifact
meth public java.lang.String getProfile()
meth public java.lang.String getTargets()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.Artifact> getArtifact()
meth public void setProfile(java.lang.String)
meth public void setTargets(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.jaxb.Authenticator
cons public init()
fld protected java.lang.String xref
fld protected org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator useGenerator
fld protected org.netbeans.modules.websvc.saas.model.jaxb.UseTemplates useTemplates
meth public java.lang.String getXref()
meth public org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator getUseGenerator()
meth public org.netbeans.modules.websvc.saas.model.jaxb.UseTemplates getUseTemplates()
meth public void setUseGenerator(org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator)
meth public void setUseTemplates(org.netbeans.modules.websvc.saas.model.jaxb.UseTemplates)
meth public void setXref(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.jaxb.FieldDescriptor
cons public init()
fld protected java.lang.String initValue
fld protected java.lang.String modifiers
fld protected java.lang.String name
fld protected java.lang.String type
meth public java.lang.String getInitValue()
meth public java.lang.String getModifiers()
meth public java.lang.String getName()
meth public java.lang.String getType()
meth public void setInitValue(java.lang.String)
meth public void setModifiers(java.lang.String)
meth public void setName(java.lang.String)
meth public void setType(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.jaxb.Group
cons public init()
fld protected java.lang.String name
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.Group> group
meth public java.lang.String getName()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.Group> getGroup()
meth public void setName(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.jaxb.Method
cons public init()
fld protected java.lang.String documentation
fld protected java.lang.String href
fld protected java.lang.String id
fld protected java.lang.String name
fld protected java.lang.String operationName
fld protected java.lang.String portName
fld protected java.lang.String serviceName
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Method$Input input
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Method$Output output
innr public static Input
innr public static Output
meth public java.lang.String getDocumentation()
meth public java.lang.String getHref()
meth public java.lang.String getId()
meth public java.lang.String getName()
meth public java.lang.String getOperationName()
meth public java.lang.String getPortName()
meth public java.lang.String getServiceName()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Method$Input getInput()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Method$Output getOutput()
meth public void setDocumentation(java.lang.String)
meth public void setHref(java.lang.String)
meth public void setId(java.lang.String)
meth public void setInput(org.netbeans.modules.websvc.saas.model.jaxb.Method$Input)
meth public void setName(java.lang.String)
meth public void setOperationName(java.lang.String)
meth public void setOutput(org.netbeans.modules.websvc.saas.model.jaxb.Method$Output)
meth public void setPortName(java.lang.String)
meth public void setServiceName(java.lang.String)
supr java.lang.Object

CLSS public static org.netbeans.modules.websvc.saas.model.jaxb.Method$Input
 outer org.netbeans.modules.websvc.saas.model.jaxb.Method
cons public init()
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Params params
meth public org.netbeans.modules.websvc.saas.model.jaxb.Params getParams()
meth public void setParams(org.netbeans.modules.websvc.saas.model.jaxb.Params)
supr java.lang.Object

CLSS public static org.netbeans.modules.websvc.saas.model.jaxb.Method$Output
 outer org.netbeans.modules.websvc.saas.model.jaxb.Method
cons public init()
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Method$Output$Media media
innr public static Media
meth public org.netbeans.modules.websvc.saas.model.jaxb.Method$Output$Media getMedia()
meth public void setMedia(org.netbeans.modules.websvc.saas.model.jaxb.Method$Output$Media)
supr java.lang.Object

CLSS public static org.netbeans.modules.websvc.saas.model.jaxb.Method$Output$Media
 outer org.netbeans.modules.websvc.saas.model.jaxb.Method$Output
cons public init()
fld protected java.lang.String type
meth public java.lang.String getType()
meth public void setType(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.jaxb.MethodDescriptor
cons public init()
fld protected java.lang.String _throws
fld protected java.lang.String body
fld protected java.lang.String bodyRef
fld protected java.lang.String id
fld protected java.lang.String modifiers
fld protected java.lang.String name
fld protected java.lang.String paramNames
fld protected java.lang.String paramTypes
fld protected java.lang.String returnType
meth public java.lang.String getBody()
meth public java.lang.String getBodyRef()
meth public java.lang.String getId()
meth public java.lang.String getModifiers()
meth public java.lang.String getName()
meth public java.lang.String getParamNames()
meth public java.lang.String getParamTypes()
meth public java.lang.String getReturnType()
meth public java.lang.String getThrows()
meth public void setBody(java.lang.String)
meth public void setBodyRef(java.lang.String)
meth public void setId(java.lang.String)
meth public void setModifiers(java.lang.String)
meth public void setName(java.lang.String)
meth public void setParamNames(java.lang.String)
meth public void setParamTypes(java.lang.String)
meth public void setReturnType(java.lang.String)
meth public void setThrows(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.jaxb.Methods
cons public init()
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.Method> method
meth public java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.Method> getMethod()
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.jaxb.ObjectFactory
cons public init()
meth public javax.xml.bind.JAXBElement<org.netbeans.modules.websvc.saas.model.jaxb.Group> createGroup(org.netbeans.modules.websvc.saas.model.jaxb.Group)
meth public javax.xml.bind.JAXBElement<org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata> createSaasMetadata(org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata)
meth public javax.xml.bind.JAXBElement<org.netbeans.modules.websvc.saas.model.jaxb.SaasServices> createSaasServices(org.netbeans.modules.websvc.saas.model.jaxb.SaasServices)
meth public org.netbeans.modules.websvc.saas.model.jaxb.Artifact createArtifact()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Artifacts createArtifacts()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Authenticator createAuthenticator()
meth public org.netbeans.modules.websvc.saas.model.jaxb.FieldDescriptor createFieldDescriptor()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Group createGroup()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Method createMethod()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Method$Input createMethodInput()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Method$Output createMethodOutput()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Method$Output$Media createMethodOutputMedia()
meth public org.netbeans.modules.websvc.saas.model.jaxb.MethodDescriptor createMethodDescriptor()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Methods createMethods()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Params createParams()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Params$Param createParamsParam()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Params$Param$Set createParamsParamSet()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Prompt createPrompt()
meth public org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata createSaasMetadata()
meth public org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication createSaasMetadataAuthentication()
meth public org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$ApiKey createSaasMetadataAuthenticationApiKey()
meth public org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$Authenticator createSaasMetadataAuthenticationAuthenticator()
meth public org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$HttpBasic createSaasMetadataAuthenticationHttpBasic()
meth public org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$SessionKey createSaasMetadataAuthenticationSessionKey()
meth public org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$SignedUrl createSaasMetadataAuthenticationSignedUrl()
meth public org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$CodeGen createSaasMetadataCodeGen()
meth public org.netbeans.modules.websvc.saas.model.jaxb.SaasServices createSaasServices()
meth public org.netbeans.modules.websvc.saas.model.jaxb.SaasServices$Header createSaasServicesHeader()
meth public org.netbeans.modules.websvc.saas.model.jaxb.ServletDescriptor createServletDescriptor()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Sign createSign()
meth public org.netbeans.modules.websvc.saas.model.jaxb.TemplateType createTemplateType()
meth public org.netbeans.modules.websvc.saas.model.jaxb.TemplateType$Template createTemplateTypeTemplate()
meth public org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator createUseGenerator()
meth public org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator$Login createUseGeneratorLogin()
meth public org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator$Logout createUseGeneratorLogout()
meth public org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator$Token createUseGeneratorToken()
meth public org.netbeans.modules.websvc.saas.model.jaxb.UseTemplates createUseTemplates()
supr java.lang.Object
hfds _Group_QNAME,_SaasMetadata_QNAME,_SaasServices_QNAME

CLSS public org.netbeans.modules.websvc.saas.model.jaxb.Params
cons public init()
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.Params$Param> param
innr public static Param
meth public java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.Params$Param> getParam()
supr java.lang.Object

CLSS public static org.netbeans.modules.websvc.saas.model.jaxb.Params$Param
 outer org.netbeans.modules.websvc.saas.model.jaxb.Params
cons public init()
fld protected java.lang.Boolean required
fld protected java.lang.String _default
fld protected java.lang.String fixed
fld protected java.lang.String id
fld protected java.lang.String name
fld protected java.lang.String type
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Params$Param$Set set
innr public static Set
meth public java.lang.Boolean isRequired()
meth public java.lang.String getDefault()
meth public java.lang.String getFixed()
meth public java.lang.String getId()
meth public java.lang.String getName()
meth public java.lang.String getType()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Params$Param$Set getSet()
meth public void setDefault(java.lang.String)
meth public void setFixed(java.lang.String)
meth public void setId(java.lang.String)
meth public void setName(java.lang.String)
meth public void setRequired(java.lang.Boolean)
meth public void setSet(org.netbeans.modules.websvc.saas.model.jaxb.Params$Param$Set)
meth public void setType(java.lang.String)
supr java.lang.Object

CLSS public static org.netbeans.modules.websvc.saas.model.jaxb.Params$Param$Set
 outer org.netbeans.modules.websvc.saas.model.jaxb.Params$Param
cons public init()
fld protected java.util.List<java.lang.Object> value
meth public java.util.List<java.lang.Object> getValue()
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.jaxb.Prompt
cons public init()
fld protected java.lang.String url
meth public java.lang.String getUrl()
meth public void setUrl(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata
cons public init()
fld protected java.lang.String localizingBundle
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Group group
fld protected org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication authentication
fld protected org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$CodeGen codeGen
innr public static Authentication
innr public static CodeGen
meth public java.lang.String getLocalizingBundle()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Group getGroup()
meth public org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication getAuthentication()
meth public org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$CodeGen getCodeGen()
meth public void setAuthentication(org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication)
meth public void setCodeGen(org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$CodeGen)
meth public void setGroup(org.netbeans.modules.websvc.saas.model.jaxb.Group)
meth public void setLocalizingBundle(java.lang.String)
supr java.lang.Object

CLSS public static org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication
 outer org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata
cons public init()
fld protected java.lang.Object custom
fld protected java.lang.String profile
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$Authenticator> authenticator
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$SessionKey> sessionKey
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$SignedUrl> signedUrl
fld protected org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$ApiKey apiKey
fld protected org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$HttpBasic httpBasic
innr public static ApiKey
innr public static Authenticator
innr public static HttpBasic
innr public static SessionKey
innr public static SignedUrl
meth public java.lang.Object getCustom()
meth public java.lang.String getProfile()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$Authenticator> getAuthenticator()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$SessionKey> getSessionKey()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$SignedUrl> getSignedUrl()
meth public org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$ApiKey getApiKey()
meth public org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$HttpBasic getHttpBasic()
meth public void setApiKey(org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$ApiKey)
meth public void setCustom(java.lang.Object)
meth public void setHttpBasic(org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$HttpBasic)
meth public void setProfile(java.lang.String)
supr java.lang.Object

CLSS public static org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$ApiKey
 outer org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication
cons public init()
fld protected java.lang.String id
meth public java.lang.String getId()
meth public void setId(java.lang.String)
supr java.lang.Object

CLSS public static org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$Authenticator
 outer org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication
cons public init()
fld protected java.lang.String name
meth public java.lang.String getName()
meth public void setName(java.lang.String)
supr org.netbeans.modules.websvc.saas.model.jaxb.Authenticator

CLSS public static org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$HttpBasic
 outer org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication
cons public init()
fld protected java.lang.String password
fld protected java.lang.String username
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Authenticator authenticator
meth public java.lang.String getPassword()
meth public java.lang.String getUsername()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Authenticator getAuthenticator()
meth public void setAuthenticator(org.netbeans.modules.websvc.saas.model.jaxb.Authenticator)
meth public void setPassword(java.lang.String)
meth public void setUsername(java.lang.String)
supr java.lang.Object

CLSS public static org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$SessionKey
 outer org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication
cons public init()
fld protected java.lang.String apiId
fld protected java.lang.String id
fld protected java.lang.String sessionId
fld protected java.lang.String sigId
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Authenticator authenticator
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Sign sign
meth public java.lang.String getApiId()
meth public java.lang.String getId()
meth public java.lang.String getSessionId()
meth public java.lang.String getSigId()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Authenticator getAuthenticator()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Sign getSign()
meth public void setApiId(java.lang.String)
meth public void setAuthenticator(org.netbeans.modules.websvc.saas.model.jaxb.Authenticator)
meth public void setId(java.lang.String)
meth public void setSessionId(java.lang.String)
meth public void setSigId(java.lang.String)
meth public void setSign(org.netbeans.modules.websvc.saas.model.jaxb.Sign)
supr java.lang.Object

CLSS public static org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication$SignedUrl
 outer org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$Authentication
cons public init()
fld protected java.lang.String id
fld protected java.lang.String sigId
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Authenticator authenticator
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Sign sign
meth public java.lang.String getId()
meth public java.lang.String getSigId()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Authenticator getAuthenticator()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Sign getSign()
meth public void setAuthenticator(org.netbeans.modules.websvc.saas.model.jaxb.Authenticator)
meth public void setId(java.lang.String)
meth public void setSigId(java.lang.String)
meth public void setSign(org.netbeans.modules.websvc.saas.model.jaxb.Sign)
supr java.lang.Object

CLSS public static org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata$CodeGen
 outer org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata
cons public init()
fld protected java.lang.String packageName
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.Artifacts> artifacts
meth public java.lang.String getPackageName()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.Artifacts> getArtifacts()
meth public void setPackageName(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.jaxb.SaasServices
cons public init()
fld protected java.lang.String apiDoc
fld protected java.lang.String description
fld protected java.lang.String displayName
fld protected java.lang.String type
fld protected java.lang.String url
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Methods methods
fld protected org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata saasMetadata
fld protected org.netbeans.modules.websvc.saas.model.jaxb.SaasServices$Header header
innr public static Header
meth public java.lang.String getApiDoc()
meth public java.lang.String getDescription()
meth public java.lang.String getDisplayName()
meth public java.lang.String getType()
meth public java.lang.String getUrl()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Methods getMethods()
meth public org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata getSaasMetadata()
meth public org.netbeans.modules.websvc.saas.model.jaxb.SaasServices$Header getHeader()
meth public void setApiDoc(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setDisplayName(java.lang.String)
meth public void setHeader(org.netbeans.modules.websvc.saas.model.jaxb.SaasServices$Header)
meth public void setMethods(org.netbeans.modules.websvc.saas.model.jaxb.Methods)
meth public void setSaasMetadata(org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata)
meth public void setType(java.lang.String)
meth public void setUrl(java.lang.String)
supr java.lang.Object

CLSS public static org.netbeans.modules.websvc.saas.model.jaxb.SaasServices$Header
 outer org.netbeans.modules.websvc.saas.model.jaxb.SaasServices
cons public init()
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Params params
meth public org.netbeans.modules.websvc.saas.model.jaxb.Params getParams()
meth public void setParams(org.netbeans.modules.websvc.saas.model.jaxb.Params)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.jaxb.ServletDescriptor
cons public init()
fld protected java.lang.String className
fld protected java.lang.String modifiers
fld protected java.lang.String servletMapping
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.FieldDescriptor> fieldDescriptor
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.MethodDescriptor> methodDescriptor
meth public java.lang.String getClassName()
meth public java.lang.String getModifiers()
meth public java.lang.String getServletMapping()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.FieldDescriptor> getFieldDescriptor()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.MethodDescriptor> getMethodDescriptor()
meth public void setClassName(java.lang.String)
meth public void setModifiers(java.lang.String)
meth public void setServletMapping(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.jaxb.Sign
cons public init()
fld protected java.lang.String id
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Params params
meth public java.lang.String getId()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Params getParams()
meth public void setId(java.lang.String)
meth public void setParams(org.netbeans.modules.websvc.saas.model.jaxb.Params)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.jaxb.TemplateType
cons public init()
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.FieldDescriptor> fieldDescriptor
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.MethodDescriptor> methodDescriptor
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.ServletDescriptor> servletDescriptor
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.TemplateType$Template> template
innr public static Template
meth public java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.FieldDescriptor> getFieldDescriptor()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.MethodDescriptor> getMethodDescriptor()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.ServletDescriptor> getServletDescriptor()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.jaxb.TemplateType$Template> getTemplate()
supr java.lang.Object

CLSS public static org.netbeans.modules.websvc.saas.model.jaxb.TemplateType$Template
 outer org.netbeans.modules.websvc.saas.model.jaxb.TemplateType
cons public init()
fld protected java.lang.String href
fld protected java.lang.String type
meth public java.lang.String getHref()
meth public java.lang.String getType()
meth public void setHref(java.lang.String)
meth public void setType(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator
cons public init()
fld protected org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator$Login login
fld protected org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator$Logout logout
fld protected org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator$Token token
innr public static Login
innr public static Logout
innr public static Token
meth public org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator$Login getLogin()
meth public org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator$Logout getLogout()
meth public org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator$Token getToken()
meth public void setLogin(org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator$Login)
meth public void setLogout(org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator$Logout)
meth public void setToken(org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator$Token)
supr java.lang.Object

CLSS public static org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator$Login
 outer org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator
cons public init()
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Method method
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Sign sign
meth public org.netbeans.modules.websvc.saas.model.jaxb.Method getMethod()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Sign getSign()
meth public void setMethod(org.netbeans.modules.websvc.saas.model.jaxb.Method)
meth public void setSign(org.netbeans.modules.websvc.saas.model.jaxb.Sign)
supr java.lang.Object

CLSS public static org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator$Logout
 outer org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator
cons public init()
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Method method
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Sign sign
meth public org.netbeans.modules.websvc.saas.model.jaxb.Method getMethod()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Sign getSign()
meth public void setMethod(org.netbeans.modules.websvc.saas.model.jaxb.Method)
meth public void setSign(org.netbeans.modules.websvc.saas.model.jaxb.Sign)
supr java.lang.Object

CLSS public static org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator$Token
 outer org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator
cons public init()
fld protected java.lang.String id
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Method method
fld protected org.netbeans.modules.websvc.saas.model.jaxb.Sign sign
meth public java.lang.String getId()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Method getMethod()
meth public org.netbeans.modules.websvc.saas.model.jaxb.Sign getSign()
meth public void setId(java.lang.String)
meth public void setMethod(org.netbeans.modules.websvc.saas.model.jaxb.Method)
meth public void setSign(org.netbeans.modules.websvc.saas.model.jaxb.Sign)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.jaxb.UseTemplates
cons public init()
fld protected org.netbeans.modules.websvc.saas.model.jaxb.TemplateType desktop
fld protected org.netbeans.modules.websvc.saas.model.jaxb.TemplateType nbModule
fld protected org.netbeans.modules.websvc.saas.model.jaxb.TemplateType web
fld protected org.netbeans.modules.websvc.saas.model.jaxb.TemplateType webEe7
meth public org.netbeans.modules.websvc.saas.model.jaxb.TemplateType getDesktop()
meth public org.netbeans.modules.websvc.saas.model.jaxb.TemplateType getNbModule()
meth public org.netbeans.modules.websvc.saas.model.jaxb.TemplateType getWeb()
meth public org.netbeans.modules.websvc.saas.model.jaxb.TemplateType getWebEe7()
meth public void setDesktop(org.netbeans.modules.websvc.saas.model.jaxb.TemplateType)
meth public void setNbModule(org.netbeans.modules.websvc.saas.model.jaxb.TemplateType)
meth public void setWeb(org.netbeans.modules.websvc.saas.model.jaxb.TemplateType)
meth public void setWebEe7(org.netbeans.modules.websvc.saas.model.jaxb.TemplateType)
supr java.lang.Object

CLSS abstract interface org.netbeans.modules.websvc.saas.model.jaxb.package-info

CLSS public org.netbeans.modules.websvc.saas.model.oauth.AuthorizationType
cons public init()
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.oauth.ParamType> param
fld protected org.netbeans.modules.websvc.saas.model.oauth.DynamicUrlType dynamicUrl
fld protected org.netbeans.modules.websvc.saas.model.oauth.FixedUrlType fixedUrl
meth public java.util.List<org.netbeans.modules.websvc.saas.model.oauth.ParamType> getParam()
meth public org.netbeans.modules.websvc.saas.model.oauth.DynamicUrlType getDynamicUrl()
meth public org.netbeans.modules.websvc.saas.model.oauth.FixedUrlType getFixedUrl()
meth public void setDynamicUrl(org.netbeans.modules.websvc.saas.model.oauth.DynamicUrlType)
meth public void setFixedUrl(org.netbeans.modules.websvc.saas.model.oauth.FixedUrlType)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.oauth.DynamicUrlType
cons public init()
fld protected java.lang.String authParamName
meth public java.lang.String getAuthParamName()
meth public void setAuthParamName(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.oauth.FixedUrlType
cons public init()
fld protected java.lang.String url
meth public java.lang.String getUrl()
meth public void setUrl(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.oauth.FlowType
cons public init()
fld protected org.netbeans.modules.websvc.saas.model.oauth.AuthorizationType authorization
fld protected org.netbeans.modules.websvc.saas.model.oauth.MethodType accessToken
fld protected org.netbeans.modules.websvc.saas.model.oauth.MethodType requestToken
meth public org.netbeans.modules.websvc.saas.model.oauth.AuthorizationType getAuthorization()
meth public org.netbeans.modules.websvc.saas.model.oauth.MethodType getAccessToken()
meth public org.netbeans.modules.websvc.saas.model.oauth.MethodType getRequestToken()
meth public void setAccessToken(org.netbeans.modules.websvc.saas.model.oauth.MethodType)
meth public void setAuthorization(org.netbeans.modules.websvc.saas.model.oauth.AuthorizationType)
meth public void setRequestToken(org.netbeans.modules.websvc.saas.model.oauth.MethodType)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.oauth.Metadata
cons public init()
fld protected java.lang.Boolean nonce
fld protected java.lang.Boolean timestamp
fld protected java.lang.String baseUrl
fld protected java.lang.String version
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.oauth.ParamType> param
fld protected org.netbeans.modules.websvc.saas.model.oauth.FlowType flow
fld protected org.netbeans.modules.websvc.saas.model.oauth.SignatureMethodType signatureMethod
meth public java.lang.Boolean isNonce()
meth public java.lang.Boolean isTimestamp()
meth public java.lang.String getBaseUrl()
meth public java.lang.String getVersion()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.oauth.ParamType> getParam()
meth public org.netbeans.modules.websvc.saas.model.oauth.FlowType getFlow()
meth public org.netbeans.modules.websvc.saas.model.oauth.SignatureMethodType getSignatureMethod()
meth public void setBaseUrl(java.lang.String)
meth public void setFlow(org.netbeans.modules.websvc.saas.model.oauth.FlowType)
meth public void setNonce(java.lang.Boolean)
meth public void setSignatureMethod(org.netbeans.modules.websvc.saas.model.oauth.SignatureMethodType)
meth public void setTimestamp(java.lang.Boolean)
meth public void setVersion(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.oauth.MethodType
cons public init()
fld protected java.lang.Boolean callback
fld protected java.lang.Boolean nonce
fld protected java.lang.Boolean timestamp
fld protected java.lang.Boolean verifier
fld protected java.lang.String methodName
fld protected java.lang.String requestParam
fld protected java.lang.String requestStyle
fld protected java.lang.String responseStyle
fld protected java.lang.String version
fld protected org.netbeans.modules.websvc.saas.model.oauth.SignatureMethodType signatureMethod
meth public java.lang.Boolean isCallback()
meth public java.lang.Boolean isNonce()
meth public java.lang.Boolean isTimestamp()
meth public java.lang.Boolean isVerifier()
meth public java.lang.String getMethodName()
meth public java.lang.String getRequestParam()
meth public java.lang.String getRequestStyle()
meth public java.lang.String getResponseStyle()
meth public java.lang.String getVersion()
meth public org.netbeans.modules.websvc.saas.model.oauth.SignatureMethodType getSignatureMethod()
meth public void setCallback(java.lang.Boolean)
meth public void setMethodName(java.lang.String)
meth public void setNonce(java.lang.Boolean)
meth public void setRequestParam(java.lang.String)
meth public void setRequestStyle(java.lang.String)
meth public void setResponseStyle(java.lang.String)
meth public void setSignatureMethod(org.netbeans.modules.websvc.saas.model.oauth.SignatureMethodType)
meth public void setTimestamp(java.lang.Boolean)
meth public void setVerifier(java.lang.Boolean)
meth public void setVersion(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.oauth.ObjectFactory
cons public init()
meth public javax.xml.bind.JAXBElement<org.netbeans.modules.websvc.saas.model.oauth.Metadata> createMetadata(org.netbeans.modules.websvc.saas.model.oauth.Metadata)
meth public org.netbeans.modules.websvc.saas.model.oauth.AuthorizationType createAuthorizationType()
meth public org.netbeans.modules.websvc.saas.model.oauth.DynamicUrlType createDynamicUrlType()
meth public org.netbeans.modules.websvc.saas.model.oauth.FixedUrlType createFixedUrlType()
meth public org.netbeans.modules.websvc.saas.model.oauth.FlowType createFlowType()
meth public org.netbeans.modules.websvc.saas.model.oauth.Metadata createMetadata()
meth public org.netbeans.modules.websvc.saas.model.oauth.MethodType createMethodType()
meth public org.netbeans.modules.websvc.saas.model.oauth.ParamType createParamType()
supr java.lang.Object
hfds _Metadata_QNAME

CLSS public org.netbeans.modules.websvc.saas.model.oauth.ParamType
cons public init()
fld protected java.lang.String oauthName
fld protected java.lang.String oauthPostfix
fld protected java.lang.String oauthPrefix
fld protected java.lang.String paramName
fld protected java.lang.String xpath
meth public java.lang.String getOauthName()
meth public java.lang.String getOauthPostfix()
meth public java.lang.String getOauthPrefix()
meth public java.lang.String getParamName()
meth public java.lang.String getXpath()
meth public void setOauthName(java.lang.String)
meth public void setOauthPostfix(java.lang.String)
meth public void setOauthPrefix(java.lang.String)
meth public void setParamName(java.lang.String)
meth public void setXpath(java.lang.String)
supr java.lang.Object

CLSS public final !enum org.netbeans.modules.websvc.saas.model.oauth.SignatureMethodType
fld public final static org.netbeans.modules.websvc.saas.model.oauth.SignatureMethodType HMAC_SHA_1
fld public final static org.netbeans.modules.websvc.saas.model.oauth.SignatureMethodType PLAINTEXT
meth public java.lang.String value()
meth public static org.netbeans.modules.websvc.saas.model.oauth.SignatureMethodType fromValue(java.lang.String)
meth public static org.netbeans.modules.websvc.saas.model.oauth.SignatureMethodType valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.saas.model.oauth.SignatureMethodType[] values()
supr java.lang.Enum<org.netbeans.modules.websvc.saas.model.oauth.SignatureMethodType>
hfds value

CLSS abstract interface org.netbeans.modules.websvc.saas.model.oauth.package-info

CLSS public org.netbeans.modules.websvc.saas.model.wadl.Application
cons public init()
fld protected java.util.List<java.lang.Object> any
fld protected java.util.List<java.lang.Object> resourceTypeOrMethodOrRepresentation
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> doc
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Resources> resources
fld protected org.netbeans.modules.websvc.saas.model.wadl.Grammars grammars
meth public java.util.List<java.lang.Object> getAny()
meth public java.util.List<java.lang.Object> getResourceTypeOrMethodOrRepresentation()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> getDoc()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Resources> getResources()
meth public org.netbeans.modules.websvc.saas.model.wadl.Grammars getGrammars()
meth public void setGrammars(org.netbeans.modules.websvc.saas.model.wadl.Grammars)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.wadl.Doc
cons public init()
fld protected java.lang.String title
fld protected java.util.List<java.lang.Object> content
meth public java.lang.String getTitle()
meth public java.util.List<java.lang.Object> getContent()
meth public java.util.Map<javax.xml.namespace.QName,java.lang.String> getOtherAttributes()
meth public void setTitle(java.lang.String)
supr java.lang.Object
hfds otherAttributes

CLSS public org.netbeans.modules.websvc.saas.model.wadl.Grammars
cons public init()
fld protected java.util.List<java.lang.Object> any
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> doc
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Include> include
meth public java.util.List<java.lang.Object> getAny()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> getDoc()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Include> getInclude()
supr java.lang.Object

CLSS public final !enum org.netbeans.modules.websvc.saas.model.wadl.HTTPMethods
fld public final static org.netbeans.modules.websvc.saas.model.wadl.HTTPMethods DELETE
fld public final static org.netbeans.modules.websvc.saas.model.wadl.HTTPMethods GET
fld public final static org.netbeans.modules.websvc.saas.model.wadl.HTTPMethods HEAD
fld public final static org.netbeans.modules.websvc.saas.model.wadl.HTTPMethods POST
fld public final static org.netbeans.modules.websvc.saas.model.wadl.HTTPMethods PUT
meth public java.lang.String value()
meth public static org.netbeans.modules.websvc.saas.model.wadl.HTTPMethods fromValue(java.lang.String)
meth public static org.netbeans.modules.websvc.saas.model.wadl.HTTPMethods valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.saas.model.wadl.HTTPMethods[] values()
supr java.lang.Enum<org.netbeans.modules.websvc.saas.model.wadl.HTTPMethods>

CLSS public org.netbeans.modules.websvc.saas.model.wadl.Include
cons public init()
fld protected java.lang.String href
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> doc
meth public java.lang.String getHref()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> getDoc()
meth public java.util.Map<javax.xml.namespace.QName,java.lang.String> getOtherAttributes()
meth public void setHref(java.lang.String)
supr java.lang.Object
hfds otherAttributes

CLSS public org.netbeans.modules.websvc.saas.model.wadl.Link
cons public init()
fld protected java.lang.String rel
fld protected java.lang.String resourceType
fld protected java.lang.String rev
fld protected java.util.List<java.lang.Object> any
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> doc
meth public java.lang.String getRel()
meth public java.lang.String getResourceType()
meth public java.lang.String getRev()
meth public java.util.List<java.lang.Object> getAny()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> getDoc()
meth public java.util.Map<javax.xml.namespace.QName,java.lang.String> getOtherAttributes()
meth public void setRel(java.lang.String)
meth public void setResourceType(java.lang.String)
meth public void setRev(java.lang.String)
supr java.lang.Object
hfds otherAttributes

CLSS public org.netbeans.modules.websvc.saas.model.wadl.Method
cons public init()
fld protected java.lang.String href
fld protected java.lang.String id
fld protected java.lang.String name
fld protected java.util.List<java.lang.Object> any
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> doc
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Response> response
fld protected org.netbeans.modules.websvc.saas.model.wadl.Request request
meth public java.lang.String getHref()
meth public java.lang.String getId()
meth public java.lang.String getName()
meth public java.util.List<java.lang.Object> getAny()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> getDoc()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Response> getResponse()
meth public java.util.Map<javax.xml.namespace.QName,java.lang.String> getOtherAttributes()
meth public org.netbeans.modules.websvc.saas.model.wadl.Request getRequest()
meth public void setHref(java.lang.String)
meth public void setId(java.lang.String)
meth public void setName(java.lang.String)
meth public void setRequest(org.netbeans.modules.websvc.saas.model.wadl.Request)
supr java.lang.Object
hfds otherAttributes

CLSS public org.netbeans.modules.websvc.saas.model.wadl.ObjectFactory
cons public init()
meth public org.netbeans.modules.websvc.saas.model.wadl.Application createApplication()
meth public org.netbeans.modules.websvc.saas.model.wadl.Doc createDoc()
meth public org.netbeans.modules.websvc.saas.model.wadl.Grammars createGrammars()
meth public org.netbeans.modules.websvc.saas.model.wadl.Include createInclude()
meth public org.netbeans.modules.websvc.saas.model.wadl.Link createLink()
meth public org.netbeans.modules.websvc.saas.model.wadl.Method createMethod()
meth public org.netbeans.modules.websvc.saas.model.wadl.Option createOption()
meth public org.netbeans.modules.websvc.saas.model.wadl.Param createParam()
meth public org.netbeans.modules.websvc.saas.model.wadl.Representation createRepresentation()
meth public org.netbeans.modules.websvc.saas.model.wadl.Request createRequest()
meth public org.netbeans.modules.websvc.saas.model.wadl.Resource createResource()
meth public org.netbeans.modules.websvc.saas.model.wadl.ResourceType createResourceType()
meth public org.netbeans.modules.websvc.saas.model.wadl.Resources createResources()
meth public org.netbeans.modules.websvc.saas.model.wadl.Response createResponse()
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.model.wadl.Option
cons public init()
fld protected java.lang.String mediaType
fld protected java.lang.String value
fld protected java.util.List<java.lang.Object> any
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> doc
meth public java.lang.String getMediaType()
meth public java.lang.String getValue()
meth public java.util.List<java.lang.Object> getAny()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> getDoc()
meth public java.util.Map<javax.xml.namespace.QName,java.lang.String> getOtherAttributes()
meth public void setMediaType(java.lang.String)
meth public void setValue(java.lang.String)
supr java.lang.Object
hfds otherAttributes

CLSS public org.netbeans.modules.websvc.saas.model.wadl.Param
cons public init()
fld protected java.lang.Boolean repeating
fld protected java.lang.Boolean required
fld protected java.lang.String _default
fld protected java.lang.String fixed
fld protected java.lang.String href
fld protected java.lang.String id
fld protected java.lang.String name
fld protected java.lang.String path
fld protected java.util.List<java.lang.Object> any
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> doc
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Option> option
fld protected javax.xml.namespace.QName type
fld protected org.netbeans.modules.websvc.saas.model.wadl.Link link
fld protected org.netbeans.modules.websvc.saas.model.wadl.ParamStyle style
meth public boolean isRepeating()
meth public boolean isRequired()
meth public java.lang.String getDefault()
meth public java.lang.String getFixed()
meth public java.lang.String getHref()
meth public java.lang.String getId()
meth public java.lang.String getName()
meth public java.lang.String getPath()
meth public java.util.List<java.lang.Object> getAny()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> getDoc()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Option> getOption()
meth public java.util.Map<javax.xml.namespace.QName,java.lang.String> getOtherAttributes()
meth public javax.xml.namespace.QName getType()
meth public org.netbeans.modules.websvc.saas.model.wadl.Link getLink()
meth public org.netbeans.modules.websvc.saas.model.wadl.ParamStyle getStyle()
meth public void setDefault(java.lang.String)
meth public void setFixed(java.lang.String)
meth public void setHref(java.lang.String)
meth public void setId(java.lang.String)
meth public void setLink(org.netbeans.modules.websvc.saas.model.wadl.Link)
meth public void setName(java.lang.String)
meth public void setPath(java.lang.String)
meth public void setRepeating(java.lang.Boolean)
meth public void setRequired(java.lang.Boolean)
meth public void setStyle(org.netbeans.modules.websvc.saas.model.wadl.ParamStyle)
meth public void setType(javax.xml.namespace.QName)
supr java.lang.Object
hfds otherAttributes

CLSS public final !enum org.netbeans.modules.websvc.saas.model.wadl.ParamStyle
fld public final static org.netbeans.modules.websvc.saas.model.wadl.ParamStyle HEADER
fld public final static org.netbeans.modules.websvc.saas.model.wadl.ParamStyle MATRIX
fld public final static org.netbeans.modules.websvc.saas.model.wadl.ParamStyle PLAIN
fld public final static org.netbeans.modules.websvc.saas.model.wadl.ParamStyle QUERY
fld public final static org.netbeans.modules.websvc.saas.model.wadl.ParamStyle TEMPLATE
meth public java.lang.String value()
meth public static org.netbeans.modules.websvc.saas.model.wadl.ParamStyle fromValue(java.lang.String)
meth public static org.netbeans.modules.websvc.saas.model.wadl.ParamStyle valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.saas.model.wadl.ParamStyle[] values()
supr java.lang.Enum<org.netbeans.modules.websvc.saas.model.wadl.ParamStyle>
hfds value

CLSS public org.netbeans.modules.websvc.saas.model.wadl.Representation
cons public init()
fld protected java.lang.String href
fld protected java.lang.String id
fld protected java.lang.String mediaType
fld protected java.util.List<java.lang.Object> any
fld protected java.util.List<java.lang.String> profile
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> doc
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Param> param
fld protected javax.xml.namespace.QName element
meth public java.lang.String getHref()
meth public java.lang.String getId()
meth public java.lang.String getMediaType()
meth public java.util.List<java.lang.Object> getAny()
meth public java.util.List<java.lang.String> getProfile()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> getDoc()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Param> getParam()
meth public java.util.Map<javax.xml.namespace.QName,java.lang.String> getOtherAttributes()
meth public javax.xml.namespace.QName getElement()
meth public void setElement(javax.xml.namespace.QName)
meth public void setHref(java.lang.String)
meth public void setId(java.lang.String)
meth public void setMediaType(java.lang.String)
supr java.lang.Object
hfds otherAttributes

CLSS public org.netbeans.modules.websvc.saas.model.wadl.Request
cons public init()
fld protected java.util.List<java.lang.Object> any
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> doc
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Param> param
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Representation> representation
meth public java.util.List<java.lang.Object> getAny()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> getDoc()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Param> getParam()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Representation> getRepresentation()
meth public java.util.Map<javax.xml.namespace.QName,java.lang.String> getOtherAttributes()
supr java.lang.Object
hfds otherAttributes

CLSS public org.netbeans.modules.websvc.saas.model.wadl.Resource
cons public init()
fld protected java.lang.String id
fld protected java.lang.String path
fld protected java.lang.String queryType
fld protected java.util.List<java.lang.Object> any
fld protected java.util.List<java.lang.Object> methodOrResource
fld protected java.util.List<java.lang.String> type
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> doc
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Param> param
meth public java.lang.String getId()
meth public java.lang.String getPath()
meth public java.lang.String getQueryType()
meth public java.util.List<java.lang.Object> getAny()
meth public java.util.List<java.lang.Object> getMethodOrResource()
meth public java.util.List<java.lang.String> getType()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> getDoc()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Param> getParam()
meth public java.util.Map<javax.xml.namespace.QName,java.lang.String> getOtherAttributes()
meth public void setId(java.lang.String)
meth public void setPath(java.lang.String)
meth public void setQueryType(java.lang.String)
supr java.lang.Object
hfds otherAttributes

CLSS public org.netbeans.modules.websvc.saas.model.wadl.ResourceType
cons public init()
fld protected java.lang.String id
fld protected java.util.List<java.lang.Object> any
fld protected java.util.List<java.lang.Object> methodOrResource
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> doc
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Param> param
meth public java.lang.String getId()
meth public java.util.List<java.lang.Object> getAny()
meth public java.util.List<java.lang.Object> getMethodOrResource()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> getDoc()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Param> getParam()
meth public java.util.Map<javax.xml.namespace.QName,java.lang.String> getOtherAttributes()
meth public void setId(java.lang.String)
supr java.lang.Object
hfds otherAttributes

CLSS public org.netbeans.modules.websvc.saas.model.wadl.Resources
cons public init()
fld protected java.lang.String base
fld protected java.util.List<java.lang.Object> any
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> doc
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Resource> resource
meth public java.lang.String getBase()
meth public java.util.List<java.lang.Object> getAny()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> getDoc()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Resource> getResource()
meth public java.util.Map<javax.xml.namespace.QName,java.lang.String> getOtherAttributes()
meth public void setBase(java.lang.String)
supr java.lang.Object
hfds otherAttributes

CLSS public org.netbeans.modules.websvc.saas.model.wadl.Response
cons public init()
fld protected java.util.List<java.lang.Long> status
fld protected java.util.List<java.lang.Object> any
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> doc
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Param> param
fld protected java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Representation> representation
meth public java.util.List<java.lang.Long> getStatus()
meth public java.util.List<java.lang.Object> getAny()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Doc> getDoc()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Param> getParam()
meth public java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Representation> getRepresentation()
meth public java.util.Map<javax.xml.namespace.QName,java.lang.String> getOtherAttributes()
supr java.lang.Object
hfds otherAttributes

CLSS abstract interface org.netbeans.modules.websvc.saas.model.wadl.package-info

CLSS public abstract interface org.netbeans.modules.websvc.saas.spi.ConsumerFlavorProvider
fld public final static java.awt.datatransfer.DataFlavor CUSTOM_METHOD_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor CUSTOM_METHOD_NODE_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor PORT_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor PORT_NODE_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor WADL_METHOD_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor WADL_METHOD_NODE_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor WSDL_METHOD_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor WSDL_METHOD_NODE_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor WSDL_SERVICE_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor WSDL_SERVICE_NODE_FLAVOR
meth public abstract java.awt.datatransfer.Transferable addDataFlavors(java.awt.datatransfer.Transferable)

CLSS public abstract interface org.netbeans.modules.websvc.saas.spi.MethodNodeActionsProvider
fld public final static javax.swing.Action[] EMPTY_ACTIONS
meth public abstract javax.swing.Action[] getMethodActions(org.openide.util.Lookup)

CLSS public abstract interface org.netbeans.modules.websvc.saas.spi.SaasNodeActionsProvider
fld public final static javax.swing.Action[] EMPTY_ACTIONS
meth public abstract javax.swing.Action[] getSaasActions(org.openide.util.Lookup)

CLSS public abstract interface org.netbeans.modules.websvc.saas.spi.SaasViewProvider
meth public abstract org.openide.nodes.Node getSaasView()

CLSS public abstract org.netbeans.modules.websvc.saas.spi.ServiceData
cons public init()
meth public abstract java.lang.Object getRawService()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getInfoPage()
meth public abstract java.lang.String getPackageName()
meth public abstract java.lang.String getProviderName()
meth public abstract java.lang.String getPurchaseLink()
meth public abstract java.lang.String getServiceName()
meth public abstract java.lang.String getUrl()
meth public abstract java.lang.String getVersion()
meth public abstract void setPackageName(java.lang.String)
supr java.lang.Object

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

CLSS public final static !enum org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData$Status
 outer org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData
fld public final static org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData$Status WSDL_RETRIEVED
fld public final static org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData$Status WSDL_RETRIEVING
fld public final static org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData$Status WSDL_SERVICE_COMPILED
fld public final static org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData$Status WSDL_SERVICE_COMPILE_FAILED
fld public final static org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData$Status WSDL_SERVICE_COMPILING
fld public final static org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData$Status WSDL_UNRETRIEVED
meth public static org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData$Status valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData$Status[] values()
supr java.lang.Enum<org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData$Status>

CLSS public abstract interface org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlDataManager
meth public abstract int getPrecedence()
meth public abstract org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData addWsdlData(java.lang.String,java.lang.String)
meth public abstract org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData findWsdlData(java.lang.String,java.lang.String)
meth public abstract org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData getWsdlData(java.lang.String,java.lang.String,boolean)
meth public abstract void refresh(org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData)
meth public abstract void removeWsdlData(java.lang.String,java.lang.String)
meth public abstract void setPrecedence(int)

CLSS public abstract org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceData
cons public init()
meth public boolean isInRepository()
meth public static boolean isInRepository(java.lang.String,java.lang.String)
supr org.netbeans.modules.websvc.saas.spi.ServiceData

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

CLSS public static org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceProxyDescriptor$JarEntry
 outer org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceProxyDescriptor
cons public init()
cons public init(java.lang.String,java.lang.String)
fld public final static java.lang.String PROXY_JAR_TYPE = "proxy"
fld public final static java.lang.String SRC_JAR_TYPE = "source"
meth public boolean equals(java.lang.Object)
meth public java.lang.String getName()
meth public java.lang.String getType()
meth public void setName(java.lang.String)
meth public void setType(java.lang.String)
supr java.lang.Object
hfds name,type

CLSS public org.netbeans.modules.websvc.saas.util.SaasTransferable<%0 extends java.lang.Object>
cons public init({org.netbeans.modules.websvc.saas.util.SaasTransferable%0},java.util.Set<java.awt.datatransfer.DataFlavor>)
fld public final static java.util.Set<java.awt.datatransfer.DataFlavor> CUSTOM_METHOD_FLAVORS
fld public final static java.util.Set<java.awt.datatransfer.DataFlavor> WADL_METHOD_FLAVORS
fld public final static java.util.Set<java.awt.datatransfer.DataFlavor> WSDL_METHOD_FLAVORS
fld public final static java.util.Set<java.awt.datatransfer.DataFlavor> WSDL_PORT_FLAVORS
fld public final static java.util.Set<java.awt.datatransfer.DataFlavor> WSDL_SERVICE_FLAVORS
intf java.awt.datatransfer.Transferable
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public static java.awt.datatransfer.Transferable addFlavors(java.awt.datatransfer.Transferable)
supr java.lang.Object
hfds flavors,transferData

CLSS public org.netbeans.modules.websvc.saas.util.SaasUtil
cons public init()
fld public final static java.lang.String APPLICATION_WADL = "resources/application.wadl"
fld public final static java.lang.String CATALOG = "catalog"
fld public final static java.lang.String DEFAULT_SERVICE_NAME = "Service"
fld public final static javax.xml.namespace.QName QNAME_GROUP
fld public final static javax.xml.namespace.QName QNAME_SAAS_SERVICES
innr public abstract interface static ReadInputStream
meth public static <%0 extends java.lang.Object> {%%0} loadJaxbObject(java.io.InputStream,java.lang.Class<{%%0}>) throws javax.xml.bind.JAXBException
meth public static <%0 extends java.lang.Object> {%%0} loadJaxbObject(java.io.InputStream,java.lang.Class<{%%0}>,boolean) throws javax.xml.bind.JAXBException
meth public static <%0 extends java.lang.Object> {%%0} loadJaxbObject(java.io.Reader,java.lang.Class<{%%0}>,boolean) throws javax.xml.bind.JAXBException
meth public static <%0 extends java.lang.Object> {%%0} loadJaxbObject(org.openide.filesystems.FileObject,java.lang.Class<{%%0}>,boolean) throws java.io.IOException
meth public static <%0 extends java.lang.Object> {%%0} loadJaxbObject(org.openide.filesystems.FileObject,java.lang.Class<{%%0}>,boolean,org.netbeans.modules.websvc.saas.util.SaasUtil$ReadInputStream) throws java.io.IOException
meth public static java.awt.Image loadIcon(org.netbeans.modules.websvc.saas.model.SaasGroup,int)
meth public static java.lang.String deriveDefaultPackageName(org.netbeans.modules.websvc.saas.model.Saas)
meth public static java.lang.String deriveFileName(java.lang.String)
meth public static java.lang.String dirOnlyPath(java.lang.String)
meth public static java.lang.String ensureUniqueServiceDirName(java.lang.String)
meth public static java.lang.String filenameFromPath(java.lang.String)
meth public static java.lang.String getSaasType(java.lang.String)
meth public static java.lang.String getSignature(org.netbeans.modules.websvc.saas.model.WadlSaasMethod)
meth public static java.lang.String getWadlServiceDirName(java.lang.String)
meth public static java.lang.String toValidJavaName(java.lang.String)
meth public static java.util.Collection<? extends org.netbeans.modules.websvc.saas.spi.MethodNodeActionsProvider> getMethodNodeActionsProviders()
meth public static java.util.Collection<? extends org.netbeans.modules.websvc.saas.spi.SaasNodeActionsProvider> getSaasNodeActionsProviders()
meth public static java.util.Set<java.lang.String> getMediaTypes(java.util.List<org.netbeans.modules.websvc.saas.model.wadl.Representation>)
meth public static java.util.Set<java.lang.String> getMediaTypesFromJAXBElement(java.util.List<javax.xml.bind.JAXBElement<org.netbeans.modules.websvc.saas.model.wadl.Representation>>)
meth public static javax.xml.transform.sax.SAXSource getSAXSourceWithXIncludeEnabled(java.io.InputStream)
meth public static javax.xml.transform.sax.SAXSource getSAXSourceWithXIncludeEnabled(java.io.Reader)
meth public static javax.xml.transform.sax.SAXSource getSAXSourceWithXIncludeEnabled(org.xml.sax.InputSource)
meth public static org.netbeans.modules.websvc.saas.model.Saas getServiceByUrl(org.netbeans.modules.websvc.saas.model.SaasGroup,java.lang.String)
meth public static org.netbeans.modules.websvc.saas.model.SaasGroup loadSaasGroup(java.io.InputStream) throws javax.xml.bind.JAXBException
meth public static org.netbeans.modules.websvc.saas.model.SaasGroup loadSaasGroup(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static org.netbeans.modules.websvc.saas.model.jaxb.SaasServices loadSaasServices(java.io.InputStream) throws javax.xml.bind.JAXBException
meth public static org.netbeans.modules.websvc.saas.model.jaxb.SaasServices loadSaasServices(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static org.netbeans.modules.websvc.saas.model.wadl.Application loadWadl(java.io.InputStream) throws javax.xml.bind.JAXBException
meth public static org.netbeans.modules.websvc.saas.model.wadl.Application loadWadl(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static org.netbeans.modules.websvc.saas.model.wadl.Method wadlMethodFromIdRef(org.netbeans.modules.websvc.saas.model.wadl.Application,java.lang.String)
meth public static org.netbeans.modules.websvc.saas.model.wadl.Method wadlMethodFromXPath(org.netbeans.modules.websvc.saas.model.wadl.Application,java.lang.String)
meth public static org.netbeans.modules.websvc.saas.model.wadl.Resource getParentResource(org.netbeans.modules.websvc.saas.model.wadl.Application,org.netbeans.modules.websvc.saas.model.wadl.Method)
meth public static org.openide.filesystems.FileObject extractWadlFile(org.netbeans.modules.websvc.saas.model.WadlSaas) throws java.io.IOException
meth public static org.openide.filesystems.FileObject retrieveWadlFile(org.netbeans.modules.websvc.saas.model.WadlSaas)
meth public static org.openide.filesystems.FileObject saveResourceAsFile(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static org.openide.filesystems.FileObject saveResourceAsFile(org.openide.filesystems.FileObject,java.lang.String,java.lang.String) throws java.io.IOException
meth public static void saveSaas(org.netbeans.modules.websvc.saas.model.Saas,org.openide.filesystems.FileObject) throws java.io.IOException,javax.xml.bind.JAXBException
meth public static void saveSaasGroup(org.netbeans.modules.websvc.saas.model.SaasGroup,java.io.File) throws java.io.IOException,javax.xml.bind.JAXBException
meth public static void saveSaasGroup(org.netbeans.modules.websvc.saas.model.SaasGroup,java.io.OutputStream) throws javax.xml.bind.JAXBException
supr java.lang.Object
hfds IS_READER,extensionsResult,methodsResult
hcls InputStreamJaxbReader

CLSS public abstract interface static org.netbeans.modules.websvc.saas.util.SaasUtil$ReadInputStream
 outer org.netbeans.modules.websvc.saas.util.SaasUtil
meth public abstract <%0 extends java.lang.Object> {%%0} loadJaxbObject(java.io.InputStream,java.lang.Class<{%%0}>,boolean) throws javax.xml.bind.JAXBException

CLSS public org.netbeans.modules.websvc.saas.util.TypeUtil
cons public init()
meth public static java.lang.String getParameterType(org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaParameter)
meth public static java.lang.String typeToString(java.lang.reflect.Type)
meth public static java.util.List<java.net.URL> buildClasspath(java.io.File,boolean) throws java.io.IOException
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.util.WsdlUtil
cons public init()
meth public static boolean hasProcessedImport()
meth public static boolean hasWsdlSupport()
meth public static boolean isJAXRPCAvailable()
meth public static java.lang.String getCatalogForWsdl(java.lang.String)
meth public static java.lang.String getServiceDirName(java.lang.String)
meth public static org.netbeans.api.project.libraries.Library getWebServiceSupportLibDef(boolean)
meth public static org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData addWsdlData(java.lang.String,java.lang.String)
meth public static org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData findWsdlData(java.lang.String,java.lang.String)
meth public static org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData getWsdlData(java.lang.String,java.lang.String,boolean)
meth public static void ensureImportExisting60Services()
meth public static void markImportProcessed()
meth public static void refreshWsdlData(org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData)
meth public static void removeWsdlData(java.lang.String)
meth public static void removeWsdlData(org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData)
supr java.lang.Object
hfds IMPORTED_MARK

CLSS public abstract interface org.netbeans.modules.xml.catalog.spi.CatalogDescriptor
 anno 0 java.lang.Deprecated()
intf org.netbeans.modules.xml.catalog.spi.CatalogDescriptorBase
meth public abstract java.awt.Image getIcon(int)

CLSS public abstract interface org.netbeans.modules.xml.catalog.spi.CatalogDescriptorBase
fld public final static java.lang.String PROP_CATALOG_DESC = "ca-desc"
fld public final static java.lang.String PROP_CATALOG_ICON = "ca-icon"
fld public final static java.lang.String PROP_CATALOG_NAME = "ca-name"
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getShortDescription()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.modules.xml.catalog.spi.CatalogReader
meth public abstract java.lang.String getSystemID(java.lang.String)
meth public abstract java.lang.String resolvePublic(java.lang.String)
meth public abstract java.lang.String resolveURI(java.lang.String)
meth public abstract java.util.Iterator getPublicIDs()
meth public abstract void addCatalogListener(org.netbeans.modules.xml.catalog.spi.CatalogListener)
meth public abstract void refresh()
meth public abstract void removeCatalogListener(org.netbeans.modules.xml.catalog.spi.CatalogListener)

CLSS public abstract interface org.xml.sax.EntityResolver
meth public abstract org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException

