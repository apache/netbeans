#Signature file v4.1
#Version 1.59

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

CLSS public abstract interface org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation<%0 extends java.lang.Object>
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> runReadActionWhenReady(org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction<{org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation%0},{%%0}>) throws java.io.IOException
meth public abstract <%0 extends java.lang.Object> {%%0} runReadAction(org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction<{org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation%0},{%%0}>) throws java.io.IOException
meth public abstract boolean isReady()

CLSS public abstract interface org.netbeans.modules.websvc.rest.model.api.HttpMethod
intf org.netbeans.modules.websvc.rest.model.api.RestMethodDescription
meth public abstract java.lang.String getConsumeMime()
meth public abstract java.lang.String getPath()
meth public abstract java.lang.String getProduceMime()
meth public abstract java.lang.String getType()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getQueryParams()

CLSS public abstract interface org.netbeans.modules.websvc.rest.model.api.RestApplication
meth public abstract java.lang.String getApplicationClass()
meth public abstract java.lang.String getApplicationPath()

CLSS public abstract interface org.netbeans.modules.websvc.rest.model.api.RestApplicationModel
intf org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation<org.netbeans.modules.websvc.rest.model.api.RestApplications>

CLSS public abstract interface org.netbeans.modules.websvc.rest.model.api.RestApplications
meth public abstract java.util.List<org.netbeans.modules.websvc.rest.model.api.RestApplication> getRestApplications()

CLSS public org.netbeans.modules.websvc.rest.model.api.RestConstants
cons public init()
fld public final static java.lang.String APPLICATION_PATH = "javax.ws.rs.ApplicationPath"
fld public final static java.lang.String APPLICATION_PATH_JAKARTA = "jakarta.ws.rs.ApplicationPath"
fld public final static java.lang.String CONSUME_MIME = "javax.ws.rs.Consumes"
fld public final static java.lang.String CONSUME_MIME_ANNOTATION = "Consumes"
fld public final static java.lang.String CONSUME_MIME_JAKARTA = "jakarta.ws.rs.Consumes"
fld public final static java.lang.String CONTEXT = "javax.ws.rs.core.Context"
fld public final static java.lang.String CONTEXT_ANNOTATION = "Context"
fld public final static java.lang.String CONTEXT_JAKARTA = "jakarta.ws.rs.core.Context"
fld public final static java.lang.String DEFAULT_VALUE = "javax.ws.rs.DefaultValue"
fld public final static java.lang.String DEFAULT_VALUE_ANNOTATION = "DefaultValue"
fld public final static java.lang.String DEFAULT_VALUE_JAKARTA = "jakarta.ws.rs.DefaultValue"
fld public final static java.lang.String DELETE = "javax.ws.rs.DELETE"
fld public final static java.lang.String DELETE_ANNOTATION = "DELETE"
fld public final static java.lang.String DELETE_JAKARTA = "jakarta.ws.rs.DELETE"
fld public final static java.lang.String EJB = "javax.ejb.EJB"
fld public final static java.lang.String EJB_JAKARTA = "jakarta.ejb.EJB"
fld public final static java.lang.String ENTITY_TYPE = "javax.ws.rs.Entity"
fld public final static java.lang.String ENTITY_TYPE_JAKARTA = "jakarta.ws.rs.Entity"
fld public final static java.lang.String GET = "javax.ws.rs.GET"
fld public final static java.lang.String GET_ANNOTATION = "GET"
fld public final static java.lang.String GET_CLASSES = "getClasses"
fld public final static java.lang.String GET_JAKARTA = "jakarta.ws.rs.GET"
fld public final static java.lang.String GET_REST_RESOURCE_CLASSES2 = "addRestResourceClasses"
fld public final static java.lang.String HTTP_RESPONSE = "javax.ws.rs.core.Response"
fld public final static java.lang.String HTTP_RESPONSE_JAKARTA = "jakarta.ws.rs.core.Response"
fld public final static java.lang.String JERSEY_API_PACKAGE = "com.sun.jersey.api."
fld public final static java.lang.String JERSEY_PACKAGE = "com.sun.jersey."
fld public final static java.lang.String JERSEY_SPI_PACKAGE = "com.sun.jersey.spi."
fld public final static java.lang.String JavaEE5_EJB_PACKAGE = "javax.ejb."
fld public final static java.lang.String JavaEE5_EJB_PACKAGE_JAKARTA = "jakarta.ejb."
fld public final static java.lang.String PATH = "javax.ws.rs.Path"
fld public final static java.lang.String PATH_ANNOTATION = "Path"
fld public final static java.lang.String PATH_JAKARTA = "jakarta.ws.rs.Path"
fld public final static java.lang.String PATH_PARAM = "javax.ws.rs.PathParam"
fld public final static java.lang.String PATH_PARAM_ANNOTATION = "PathParam"
fld public final static java.lang.String PATH_PARAM_JAKARTA = "jakarta.ws.rs.PathParam"
fld public final static java.lang.String POST = "javax.ws.rs.POST"
fld public final static java.lang.String POST_ANNOTATION = "POST"
fld public final static java.lang.String POST_JAKARTA = "jakarta.ws.rs.POST"
fld public final static java.lang.String PRODUCE_MIME = "javax.ws.rs.Produces"
fld public final static java.lang.String PRODUCE_MIME_ANNOTATION = "Produces"
fld public final static java.lang.String PRODUCE_MIME_JAKARTA = "jakarta.ws.rs.Produces"
fld public final static java.lang.String PROVIDER_ANNOTATION = "javax.ws.rs.ext.Provider"
fld public final static java.lang.String PROVIDER_ANNOTATION_JAKARTA = "jakarta.ws.rs.ext.Provider"
fld public final static java.lang.String PUT = "javax.ws.rs.PUT"
fld public final static java.lang.String PUT_ANNOTATION = "PUT"
fld public final static java.lang.String PUT_JAKARTA = "jakarta.ws.rs.PUT"
fld public final static java.lang.String QUERY_PARAM = "javax.ws.rs.QueryParam"
fld public final static java.lang.String QUERY_PARAM_ANNOTATION = "QueryParam"
fld public final static java.lang.String QUERY_PARAM_JAKARTA = "jakarta.ws.rs.QueryParam"
fld public final static java.lang.String RESOURCE_CONTEXT = "com.sun.jersey.api.core.ResourceContext"
fld public final static java.lang.String RESPONSE_BUILDER = "javax.ws.rs.core.Response.Builder"
fld public final static java.lang.String RESPONSE_BUILDER_JAKARTA = "jakarta.ws.rs.core.Response.Builder"
fld public final static java.lang.String REST_API_CORE_PACKAGE = "javax.ws.rs.core."
fld public final static java.lang.String REST_API_CORE_PACKAGE_JAKARTA = "jakarta.ws.rs.core."
fld public final static java.lang.String REST_API_PACKAGE = "javax.ws.rs."
fld public final static java.lang.String REST_API_PACKAGE_JAKARTA = "jakarta.ws.rs."
fld public final static java.lang.String SINGLETON = "com.sun.jersey.spi.resource.Singleton"
fld public final static java.lang.String SINGLETON_ANNOTATION = "Singleton"
fld public final static java.lang.String STATELESS = "javax.ejb.Stateless"
fld public final static java.lang.String STATELESS_ANNOTATION = "Stateless"
fld public final static java.lang.String STATELESS_JAKARTA = "jakarta.ejb.Stateless"
fld public final static java.lang.String URI_BUILDER = "javax.ws.rs.core.UriBuilder"
fld public final static java.lang.String URI_BUILDER_JAKARTA = "jakarta.ws.rs.core.UriBuilder"
fld public final static java.lang.String URI_INFO = "javax.ws.rs.core.UriInfo"
fld public final static java.lang.String URI_INFO_JAKARTA = "jakarta.ws.rs.core.UriInfo"
fld public final static java.lang.String WEB_APPLICATION_EXCEPTION = "javax.ws.rs.WebApplicationException"
fld public final static java.lang.String WEB_APPLICATION_EXCEPTION_JAKARTA = "jakarta.ws.rs.WebApplicationException"
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.websvc.rest.model.api.RestMethodDescription
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getReturnType()

CLSS public abstract interface org.netbeans.modules.websvc.rest.model.api.RestProviderDescription
meth public abstract java.lang.String getClassName()
meth public abstract org.openide.filesystems.FileObject getFile()

CLSS public abstract interface org.netbeans.modules.websvc.rest.model.api.RestServiceDescription
meth public abstract boolean isRest()
meth public abstract java.lang.String getClassName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getUriTemplate()
meth public abstract java.util.List<org.netbeans.modules.websvc.rest.model.api.RestMethodDescription> getMethods()
meth public abstract org.openide.filesystems.FileObject getFile()

CLSS public abstract interface org.netbeans.modules.websvc.rest.model.api.RestServices
fld public final static java.lang.String PROP_PROVIDERS = "providers"
fld public final static java.lang.String PROP_SERVICES = "/restservices"
intf org.netbeans.modules.j2ee.dd.api.common.RootInterface
meth public abstract int sizeRestServiceDescription()
meth public abstract java.util.Collection<? extends org.netbeans.modules.websvc.rest.model.api.RestProviderDescription> getProviders()
meth public abstract org.netbeans.modules.websvc.rest.model.api.RestServiceDescription getRestServiceDescription(java.lang.String)
meth public abstract org.netbeans.modules.websvc.rest.model.api.RestServiceDescription[] getRestServiceDescription()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void disablePropertyChangeListener()
meth public abstract void enablePropertyChangeListener()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata
meth public abstract org.netbeans.modules.websvc.rest.model.api.RestServiceDescription findRestServiceByName(java.lang.String)
meth public abstract org.netbeans.modules.websvc.rest.model.api.RestServices getRoot()

CLSS public abstract interface org.netbeans.modules.websvc.rest.model.api.RestServicesModel
intf org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation<org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata>
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void disablePropertyChangeListener()
meth public abstract void enablePropertyChangeListener()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.modules.websvc.rest.model.api.SubResourceLocator
intf org.netbeans.modules.websvc.rest.model.api.RestMethodDescription
meth public abstract java.lang.String getResourceType()
meth public abstract java.lang.String getUriTemplate()

CLSS public org.netbeans.modules.websvc.rest.model.spi.RestServicesMetadataModelFactory
meth public static org.netbeans.modules.websvc.rest.model.api.RestApplicationModel createApplicationMetadataModel(org.netbeans.modules.j2ee.dd.spi.MetadataUnit,org.netbeans.api.project.Project)
meth public static org.netbeans.modules.websvc.rest.model.api.RestServicesModel createMetadataModel(org.netbeans.modules.j2ee.dd.spi.MetadataUnit,org.netbeans.api.project.Project)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.rest.spi.MiscUtilities
cons public init()
meth public static boolean hasApplicationResourceClass(org.netbeans.modules.websvc.rest.spi.RestSupport,java.lang.String)
meth public static boolean isJavaEE6AndHigher(org.netbeans.api.project.Project)
meth public static com.sun.source.tree.ClassTree createAddResourceClasses(org.netbeans.api.java.source.TreeMaker,com.sun.source.tree.ClassTree,org.netbeans.api.java.source.CompilationController,java.lang.String,boolean) throws java.io.IOException
meth public static java.lang.String createBodyForGetClassesMethod(org.netbeans.modules.websvc.rest.spi.RestSupport)
meth public static java.lang.String getApplicationPathFromDD(org.netbeans.modules.j2ee.dd.api.web.WebApp)
meth public static java.lang.String getContextRootURL(org.netbeans.api.project.Project)
meth public static org.netbeans.modules.j2ee.dd.api.web.ServletMapping25 getRestServletMapping(org.netbeans.modules.j2ee.dd.api.web.WebApp)
meth public static org.netbeans.modules.j2ee.deployment.common.api.Datasource getDatasource(org.netbeans.api.project.Project,java.lang.String)
meth public static org.openide.filesystems.FileObject copyFile(java.io.File,java.lang.String,java.lang.String[],boolean) throws java.io.IOException
meth public static org.openide.filesystems.FileObject findSourceRoot(org.netbeans.api.project.Project)
meth public static org.openide.filesystems.FileObject generateTestClient(java.io.File) throws java.io.IOException
meth public static org.openide.filesystems.FileObject getApplicationContextXml(org.netbeans.api.project.Project)
meth public static org.openide.filesystems.FileObject modifyFile(org.openide.filesystems.FileObject,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException
meth public static void addCORSFilter(org.netbeans.modules.websvc.rest.spi.RestSupport,java.lang.String,java.lang.String)
meth public static void addInitParam(org.netbeans.modules.websvc.rest.spi.RestSupport,java.lang.String,java.lang.String)
meth public static void copyFile(java.io.File,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.netbeans.modules.websvc.rest.spi.RestSupport
cons public init(org.netbeans.api.project.Project)
fld protected final static java.lang.String GFV31_RESTLIB = "restlib_gfv31ee6"
fld protected final static java.lang.String GFV3_RESTLIB = "restlib_gfv3ee6"
fld protected final static java.lang.String JERSEY_SPRING_JAR_PATTERN = "jersey-spring.*\u005c.jar"
fld public final static int PROJECT_TYPE_DESKTOP = 0
fld public final static int PROJECT_TYPE_NB_MODULE = 2
fld public final static int PROJECT_TYPE_WEB = 1
fld public final static java.lang.String BASE_URL_TOKEN = "___BASE_URL___"
fld public final static java.lang.String COMMAND_DEPLOY = "run-deploy"
fld public final static java.lang.String COMMAND_TEST_RESTBEANS = "test-restbeans"
fld public final static java.lang.String CONFIG_TYPE_DD = "dd"
fld public final static java.lang.String CONFIG_TYPE_IDE = "ide"
fld public final static java.lang.String CONFIG_TYPE_USER = "user"
fld public final static java.lang.String CONTAINER_RESPONSE_FILTER = "com.sun.jersey.spi.container.ContainerResponseFilters"
fld public final static java.lang.String GFV2_SERVER_TYPE = "J2EE"
fld public final static java.lang.String GFV3_SERVER_TYPE = "gfv3"
fld public final static java.lang.String J2EE_SERVER_TYPE = "j2ee.server.type"
fld public final static java.lang.String JAX_RS_APPLICATION_CLASS = "javax.ws.rs.core.Application"
fld public final static java.lang.String JERSEY_API_LOCATION = "modules/ext/rest"
fld public final static java.lang.String JERSEY_CONFIG_IDE = "ide"
fld public final static java.lang.String JERSEY_CONFIG_SERVER = "server"
fld public final static java.lang.String JSR311_JAR_PATTERN = "jsr311-api.*\u005c.jar"
fld public final static java.lang.String JTA_USER_TRANSACTION_CLASS = "javax/transaction/UserTransaction.class"
fld public final static java.lang.String PARAM_WEB_RESOURCE_CLASS = "webresourceclass"
fld public final static java.lang.String PROP_APPLICATION_PATH = "rest.application.path"
fld public final static java.lang.String PROP_BASE_URL_TOKEN = "base.url.token"
fld public final static java.lang.String PROP_RESTBEANS_TEST_DIR = "restbeans.test.dir"
fld public final static java.lang.String PROP_RESTBEANS_TEST_FILE = "restbeans.test.file"
fld public final static java.lang.String PROP_RESTBEANS_TEST_URL = "restbeans.test.url"
fld public final static java.lang.String PROP_REST_CONFIG_TYPE = "rest.config.type"
fld public final static java.lang.String PROP_REST_JERSEY = "rest.jersey.type"
fld public final static java.lang.String PROP_REST_RESOURCES_PATH = "rest.resources.path"
fld public final static java.lang.String PROP_SWDP_CLASSPATH = "libs.swdp.classpath"
fld public final static java.lang.String RESTAPI_LIBRARY = "restapi"
fld public final static java.lang.String RESTBEANS_TEST_DIR = "build/generated-sources/rest-test"
fld public final static java.lang.String REST_API_JAR = "jsr311-api.jar"
fld public final static java.lang.String REST_RI_JAR = "jersey"
fld public final static java.lang.String REST_SERVLET_ADAPTOR = "ServletAdaptor"
fld public final static java.lang.String REST_SERVLET_ADAPTOR_CLASS = "com.sun.jersey.spi.container.servlet.ServletContainer"
fld public final static java.lang.String REST_SERVLET_ADAPTOR_CLASS_2_0 = "org.glassfish.jersey.servlet.ServletContainer"
fld public final static java.lang.String REST_SERVLET_ADAPTOR_CLASS_OLD = "com.sun.ws.rest.impl.container.servlet.ServletAdaptor"
fld public final static java.lang.String REST_SERVLET_ADAPTOR_MAPPING = "/resources/*"
fld public final static java.lang.String REST_SPRING_SERVLET_ADAPTOR_CLASS = "com.sun.jersey.spi.spring.container.servlet.SpringServlet"
fld public final static java.lang.String SWDP_LIBRARY = "restlib"
fld public final static java.lang.String TEST_RESBEANS = "test-resbeans"
fld public final static java.lang.String TEST_RESBEANS_CSS = "test-resbeans.css"
fld public final static java.lang.String TEST_RESBEANS_CSS2 = "css_master-all.css"
fld public final static java.lang.String TEST_RESBEANS_HTML = "test-resbeans.html"
fld public final static java.lang.String TEST_RESBEANS_JS = "test-resbeans.js"
fld public final static java.lang.String TOMCAT_SERVER_TYPE = "tomcat"
fld public final static java.lang.String WEB_RESOURCE_CLASS = "webresources.WebResources"
innr public final static !enum RestConfig
meth protected abstract void extendBuildScripts() throws java.io.IOException
meth protected abstract void handleSpring() throws java.io.IOException
meth protected void addJerseySpringJar() throws java.io.IOException
meth protected void extendJerseyClasspath()
meth public !varargs void configure(java.lang.String[]) throws java.io.IOException
meth public abstract java.io.File getLocalTargetTestRest()
meth public abstract java.lang.String getApplicationPathFromDialog(java.util.List<org.netbeans.modules.websvc.rest.model.api.RestApplication>)
meth public abstract org.openide.filesystems.FileObject generateTestClient(java.io.File,java.lang.String) throws java.io.IOException
meth public abstract void deploy() throws java.io.IOException
meth public abstract void logResourceCreation()
meth public boolean hasJTASupport()
meth public boolean hasJaxRsOnClasspath(boolean)
meth public boolean hasJersey1(boolean)
meth public boolean hasJersey2(boolean)
meth public boolean hasJerseyServlet()
meth public boolean hasServerJerseyLibrary()
meth public boolean hasSpringSupport()
meth public boolean isEE5()
meth public boolean isEE6()
meth public boolean isEE7()
meth public boolean isEE8()
meth public boolean isEESpecWithJaxRS()
meth public boolean isJakartaEE10()
meth public boolean isJakartaEE8()
meth public boolean isJakartaEE9()
meth public boolean isJakartaEE91()
meth public boolean isRestSupportOn()
meth public boolean isServerGFV2()
meth public boolean isServerGFV3()
meth public boolean isServerTomcat()
meth public final void ensureRestDevelopmentReady(org.netbeans.modules.websvc.rest.spi.RestSupport$RestConfig) throws java.io.IOException
meth public int getProjectType()
meth public java.lang.String getApplicationPath()
meth public java.lang.String getBaseURL()
meth public java.lang.String getProjectProperty(java.lang.String)
meth public java.lang.String getServerType()
meth public java.util.List<org.netbeans.modules.websvc.rest.model.api.RestApplication> getRestApplications()
meth public org.netbeans.api.project.Project getProject()
meth public org.netbeans.modules.j2ee.dd.api.web.WebApp getWebApp() throws java.io.IOException
meth public org.netbeans.modules.javaee.specs.support.api.JaxRsStackSupport getJaxRsStackSupport()
meth public org.netbeans.modules.websvc.rest.model.api.RestApplicationModel getRestApplicationsModel()
meth public org.netbeans.modules.websvc.rest.model.api.RestServicesModel getRestServicesModel()
meth public org.netbeans.spi.project.support.ant.AntProjectHelper getAntProjectHelper()
meth public org.openide.filesystems.FileObject getPersistenceXml()
meth public void removeProjectProperties(java.lang.String[])
meth public void setPrivateProjectProperty(java.lang.String,java.lang.String)
meth public void setProjectProperty(java.lang.String,java.lang.String)
supr java.lang.Object
hfds RP,applicationSubclassGenerator,helper,project,restApplicationModel,restModelListener,restServicesModel,webXmlUpdater

CLSS public final static !enum org.netbeans.modules.websvc.rest.spi.RestSupport$RestConfig
 outer org.netbeans.modules.websvc.rest.spi.RestSupport
fld public final static org.netbeans.modules.websvc.rest.spi.RestSupport$RestConfig DD
fld public final static org.netbeans.modules.websvc.rest.spi.RestSupport$RestConfig IDE
fld public final static org.netbeans.modules.websvc.rest.spi.RestSupport$RestConfig USER
meth public java.lang.String getAppClassName()
meth public static org.netbeans.modules.websvc.rest.spi.RestSupport$RestConfig valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.rest.spi.RestSupport$RestConfig[] values()
meth public void setAppClassName(java.lang.String)
supr java.lang.Enum<org.netbeans.modules.websvc.rest.spi.RestSupport$RestConfig>
hfds appClassName

