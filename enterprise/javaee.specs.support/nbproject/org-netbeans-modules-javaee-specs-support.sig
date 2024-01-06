#Signature file v4.1
#Version 1.49

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

CLSS public final org.netbeans.modules.javaee.specs.support.api.EjbSupport
meth public boolean isEjb31LiteSupported(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.javaee.specs.support.api.EjbSupport getInstance(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds impl

CLSS public org.netbeans.modules.javaee.specs.support.api.JaxRpc
cons public init()
innr public final static !enum Feature
innr public final static !enum Tool
supr java.lang.Object

CLSS public final static !enum org.netbeans.modules.javaee.specs.support.api.JaxRpc$Feature
 outer org.netbeans.modules.javaee.specs.support.api.JaxRpc
fld public final static org.netbeans.modules.javaee.specs.support.api.JaxRpc$Feature JSR109
intf org.netbeans.modules.websvc.wsstack.api.WSStack$Feature
meth public java.lang.String getName()
meth public static org.netbeans.modules.javaee.specs.support.api.JaxRpc$Feature valueOf(java.lang.String)
meth public static org.netbeans.modules.javaee.specs.support.api.JaxRpc$Feature[] values()
supr java.lang.Enum<org.netbeans.modules.javaee.specs.support.api.JaxRpc$Feature>

CLSS public final static !enum org.netbeans.modules.javaee.specs.support.api.JaxRpc$Tool
 outer org.netbeans.modules.javaee.specs.support.api.JaxRpc
fld public final static org.netbeans.modules.javaee.specs.support.api.JaxRpc$Tool WCOMPILE
intf org.netbeans.modules.websvc.wsstack.api.WSStack$Tool
meth public java.lang.String getName()
meth public static org.netbeans.modules.javaee.specs.support.api.JaxRpc$Tool valueOf(java.lang.String)
meth public static org.netbeans.modules.javaee.specs.support.api.JaxRpc$Tool[] values()
supr java.lang.Enum<org.netbeans.modules.javaee.specs.support.api.JaxRpc$Tool>

CLSS public org.netbeans.modules.javaee.specs.support.api.JaxRpcStackSupport
cons public init()
meth public static org.netbeans.modules.websvc.wsstack.api.WSStack<org.netbeans.modules.javaee.specs.support.api.JaxRpc> getIdeJaxWsStack()
meth public static org.netbeans.modules.websvc.wsstack.api.WSStack<org.netbeans.modules.javaee.specs.support.api.JaxRpc> getJaxWsStack(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform)
meth public static org.netbeans.modules.websvc.wsstack.api.WSTool getJaxWsStackTool(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform,org.netbeans.modules.javaee.specs.support.api.JaxRpc$Tool)
supr java.lang.Object
hcls RpcAccessor

CLSS public final org.netbeans.modules.javaee.specs.support.api.JaxRsStackSupport
meth public boolean addJsr311Api(org.netbeans.api.project.Project)
meth public boolean extendsJerseyProjectClasspath(org.netbeans.api.project.Project)
meth public boolean isBundled(java.lang.String)
meth public static org.netbeans.modules.javaee.specs.support.api.JaxRsStackSupport getDefault()
meth public static org.netbeans.modules.javaee.specs.support.api.JaxRsStackSupport getInstance(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void configureCustomJersey(org.netbeans.api.project.Project)
meth public void removeJaxRsLibraries(org.netbeans.api.project.Project)
supr java.lang.Object
hfds impl

CLSS public org.netbeans.modules.javaee.specs.support.api.JaxWs
cons public init(org.netbeans.modules.javaee.specs.support.api.JaxWs$UriDescriptor)
innr public abstract interface static UriDescriptor
innr public final static !enum Feature
innr public final static !enum Tool
meth public org.netbeans.modules.javaee.specs.support.api.JaxWs$UriDescriptor getWsUriDescriptor()
supr java.lang.Object
hfds uriDescriptor

CLSS public final static !enum org.netbeans.modules.javaee.specs.support.api.JaxWs$Feature
 outer org.netbeans.modules.javaee.specs.support.api.JaxWs
fld public final static org.netbeans.modules.javaee.specs.support.api.JaxWs$Feature JSR109
fld public final static org.netbeans.modules.javaee.specs.support.api.JaxWs$Feature SERVICE_REF_INJECTION
fld public final static org.netbeans.modules.javaee.specs.support.api.JaxWs$Feature TESTER_PAGE
fld public final static org.netbeans.modules.javaee.specs.support.api.JaxWs$Feature WSIT
intf org.netbeans.modules.websvc.wsstack.api.WSStack$Feature
meth public java.lang.String getName()
meth public static org.netbeans.modules.javaee.specs.support.api.JaxWs$Feature valueOf(java.lang.String)
meth public static org.netbeans.modules.javaee.specs.support.api.JaxWs$Feature[] values()
supr java.lang.Enum<org.netbeans.modules.javaee.specs.support.api.JaxWs$Feature>

CLSS public final static !enum org.netbeans.modules.javaee.specs.support.api.JaxWs$Tool
 outer org.netbeans.modules.javaee.specs.support.api.JaxWs
fld public final static org.netbeans.modules.javaee.specs.support.api.JaxWs$Tool WSGEN
fld public final static org.netbeans.modules.javaee.specs.support.api.JaxWs$Tool WSIMPORT
intf org.netbeans.modules.websvc.wsstack.api.WSStack$Tool
meth public java.lang.String getName()
meth public static org.netbeans.modules.javaee.specs.support.api.JaxWs$Tool valueOf(java.lang.String)
meth public static org.netbeans.modules.javaee.specs.support.api.JaxWs$Tool[] values()
supr java.lang.Enum<org.netbeans.modules.javaee.specs.support.api.JaxWs$Tool>

CLSS public abstract interface static org.netbeans.modules.javaee.specs.support.api.JaxWs$UriDescriptor
 outer org.netbeans.modules.javaee.specs.support.api.JaxWs
meth public abstract java.lang.String getDescriptorUri(java.lang.String,java.lang.String,java.lang.String,boolean)
meth public abstract java.lang.String getServiceUri(java.lang.String,java.lang.String,java.lang.String,boolean)
meth public abstract java.lang.String getTesterPageUri(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean)

CLSS public final org.netbeans.modules.javaee.specs.support.api.JaxWsPoliciesSupport
meth public boolean supports(org.openide.filesystems.FileObject,org.openide.util.Lookup)
meth public java.lang.String getId()
meth public java.util.List<java.lang.String> getClientPolicyIds()
meth public java.util.List<java.lang.String> getServicePolicyIds()
meth public java.util.Map<java.lang.String,java.lang.String> getPolicyDescriptions()
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform getPlatform()
meth public org.openide.util.Lookup getLookup(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.javaee.specs.support.api.JaxWsPoliciesSupport getInstance(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void extendsProjectClasspath(org.netbeans.api.project.Project,java.util.Collection<java.lang.String>)
supr java.lang.Object
hfds impl,platform

CLSS public org.netbeans.modules.javaee.specs.support.api.JaxWsStackSupport
cons public init()
meth public static org.netbeans.modules.websvc.wsstack.api.WSStack<org.netbeans.modules.javaee.specs.support.api.JaxWs> getIdeJaxWsStack()
meth public static org.netbeans.modules.websvc.wsstack.api.WSStack<org.netbeans.modules.javaee.specs.support.api.JaxWs> getJaxWsStack(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform)
meth public static org.netbeans.modules.websvc.wsstack.api.WSStack<org.netbeans.modules.javaee.specs.support.api.JaxWs> getJdkJaxWsStack()
meth public static org.netbeans.modules.websvc.wsstack.api.WSTool getJaxWsStackTool(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform,org.netbeans.modules.javaee.specs.support.api.JaxWs$Tool)
supr java.lang.Object
hcls WsAccessor

CLSS public final org.netbeans.modules.javaee.specs.support.api.JmsSupport
meth public boolean useDestinationLookup()
meth public boolean useMappedName()
meth public java.lang.String activationConfigProperty()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.javaee.specs.support.api.JmsSupport getInstance(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform)
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds DEFAULT,impl

CLSS public final org.netbeans.modules.javaee.specs.support.api.JpaProvider
meth public boolean isDefault()
meth public boolean isJpa1Supported()
meth public boolean isJpa21Supported()
meth public boolean isJpa22Supported()
meth public boolean isJpa2Supported()
meth public boolean isJpa30Supported()
meth public boolean isJpa31Supported()
meth public boolean isJpa32Supported()
meth public java.lang.String getClassName()
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.modules.javaee.specs.support.api.JpaSupport
meth public java.util.Set<org.netbeans.modules.javaee.specs.support.api.JpaProvider> getProviders()
meth public org.netbeans.modules.javaee.specs.support.api.JpaProvider getDefaultProvider()
meth public static org.netbeans.modules.javaee.specs.support.api.JpaSupport getInstance(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.modules.javaee.specs.support.api.util.JndiNamespacesDefinition
fld public final static java.lang.String APPLICATION_NAMESPACE = "java:app"
fld public final static java.lang.String GLOBAL_NAMESPACE = "java:global"
fld public final static java.lang.String MODULE_NAMESPACE = "java:module"
meth public static java.lang.String getNamespace(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static java.lang.String normalize(java.lang.String,java.lang.String)
supr java.lang.Object
hfds DEFAULT_PREFIX,PREFIXES

CLSS public abstract interface org.netbeans.modules.javaee.specs.support.spi.EjbSupportImplementation
meth public abstract boolean isEjb31LiteSupported(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform)

CLSS public abstract interface org.netbeans.modules.javaee.specs.support.spi.JaxRsStackSupportImplementation
meth public abstract boolean addJsr311Api(org.netbeans.api.project.Project)
meth public abstract boolean extendsJerseyProjectClasspath(org.netbeans.api.project.Project)
meth public abstract boolean isBundled(java.lang.String)
meth public abstract void configureCustomJersey(org.netbeans.api.project.Project)
meth public abstract void removeJaxRsLibraries(org.netbeans.api.project.Project)

CLSS public abstract interface org.netbeans.modules.javaee.specs.support.spi.JaxWsPoliciesSupportImplementation
meth public abstract boolean supports(org.openide.filesystems.FileObject,org.openide.util.Lookup)
meth public abstract java.lang.String getId()
meth public abstract java.util.List<java.lang.String> getClientPolicyIds()
meth public abstract java.util.List<java.lang.String> getServicePolicyIds()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getPolicyDescriptions()
meth public abstract org.openide.util.Lookup getLookup(org.openide.filesystems.FileObject)
meth public abstract void extendsProjectClasspath(org.netbeans.api.project.Project,java.util.Collection<java.lang.String>)

CLSS public abstract interface org.netbeans.modules.javaee.specs.support.spi.JmsSupportImplementation
meth public abstract boolean useDestinationLookup()
meth public abstract boolean useMappedName()
meth public abstract java.lang.String activationConfigProperty()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public final org.netbeans.modules.javaee.specs.support.spi.JpaProviderFactory
cons public init()
innr public abstract static Accessor
meth public static org.netbeans.modules.javaee.specs.support.api.JpaProvider createJpaProvider(java.lang.String,boolean,boolean,boolean,boolean,boolean,boolean,boolean,boolean)
meth public static org.netbeans.modules.javaee.specs.support.api.JpaProvider createJpaProvider(org.netbeans.modules.javaee.specs.support.spi.JpaProviderImplementation)
supr java.lang.Object

CLSS public abstract static org.netbeans.modules.javaee.specs.support.spi.JpaProviderFactory$Accessor
 outer org.netbeans.modules.javaee.specs.support.spi.JpaProviderFactory
cons public init()
meth public abstract org.netbeans.modules.javaee.specs.support.api.JpaProvider createJpaProvider(org.netbeans.modules.javaee.specs.support.spi.JpaProviderImplementation)
meth public static org.netbeans.modules.javaee.specs.support.spi.JpaProviderFactory$Accessor getDefault()
meth public static void setDefault(org.netbeans.modules.javaee.specs.support.spi.JpaProviderFactory$Accessor)
supr java.lang.Object
hfds accessor

CLSS public abstract interface org.netbeans.modules.javaee.specs.support.spi.JpaProviderImplementation
meth public abstract boolean isDefault()
meth public abstract boolean isJpa1Supported()
meth public abstract boolean isJpa21Supported()
meth public abstract boolean isJpa22Supported()
meth public abstract boolean isJpa2Supported()
meth public abstract boolean isJpa30Supported()
meth public abstract boolean isJpa31Supported()
meth public abstract boolean isJpa32Supported()
meth public abstract java.lang.String getClassName()

CLSS public abstract interface org.netbeans.modules.javaee.specs.support.spi.JpaSupportImplementation
meth public abstract java.util.Set<org.netbeans.modules.javaee.specs.support.api.JpaProvider> getProviders()
meth public abstract org.netbeans.modules.javaee.specs.support.api.JpaProvider getDefaultProvider()

CLSS public final org.netbeans.modules.websvc.wsstack.api.WSStack<%0 extends java.lang.Object>
innr public abstract interface static Feature
innr public abstract interface static Tool
innr public final static !enum Source
meth public boolean isFeatureSupported(org.netbeans.modules.websvc.wsstack.api.WSStack$Feature)
meth public org.netbeans.modules.websvc.wsstack.api.WSStack$Source getSource()
meth public org.netbeans.modules.websvc.wsstack.api.WSStackVersion getVersion()
meth public org.netbeans.modules.websvc.wsstack.api.WSTool getWSTool(org.netbeans.modules.websvc.wsstack.api.WSStack$Tool)
meth public static <%0 extends java.lang.Object> org.netbeans.modules.websvc.wsstack.api.WSStack<{%%0}> findWSStack(org.openide.util.Lookup,java.lang.Class<{%%0}>)
meth public {org.netbeans.modules.websvc.wsstack.api.WSStack%0} get()
supr java.lang.Object
hfds impl,stackDescriptor,stackSource

CLSS public abstract interface static org.netbeans.modules.websvc.wsstack.api.WSStack$Feature
 outer org.netbeans.modules.websvc.wsstack.api.WSStack
meth public abstract java.lang.String getName()

CLSS public abstract interface static org.netbeans.modules.websvc.wsstack.api.WSStack$Tool
 outer org.netbeans.modules.websvc.wsstack.api.WSStack
meth public abstract java.lang.String getName()

