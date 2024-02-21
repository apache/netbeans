#Signature file v4.1
#Version 1.45

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

CLSS public final org.netbeans.modules.websvc.project.api.ServiceDescriptor
meth public java.net.URI getRelativeURI()
meth public java.net.URL getRuntimeLocation()
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.modules.websvc.project.api.WebService
innr public final static !enum Type
meth public boolean isServiceProvider()
meth public java.lang.String getIdentifier()
meth public org.netbeans.modules.websvc.project.api.ServiceDescriptor getServiceDescriptor()
meth public org.netbeans.modules.websvc.project.api.WebService$Type getServiceType()
meth public org.openide.nodes.Node createNode()
supr java.lang.Object
hfds serviceImpl

CLSS public final static !enum org.netbeans.modules.websvc.project.api.WebService$Type
 outer org.netbeans.modules.websvc.project.api.WebService
fld public final static org.netbeans.modules.websvc.project.api.WebService$Type REST
fld public final static org.netbeans.modules.websvc.project.api.WebService$Type SOAP
meth public static org.netbeans.modules.websvc.project.api.WebService$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.project.api.WebService$Type[] values()
supr java.lang.Enum<org.netbeans.modules.websvc.project.api.WebService$Type>

CLSS public final org.netbeans.modules.websvc.project.api.WebServiceData
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.util.List<org.netbeans.modules.websvc.project.api.WebService> getServiceConsumers()
meth public java.util.List<org.netbeans.modules.websvc.project.api.WebService> getServiceProviders()
meth public static org.netbeans.modules.websvc.project.api.WebServiceData getWebServiceData(org.netbeans.api.project.Project)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds wsProvider

CLSS public org.netbeans.modules.websvc.project.spi.LookupMergerSupport
cons public init()
meth public static org.netbeans.spi.project.LookupMerger<org.netbeans.modules.websvc.project.spi.WebServiceDataProvider> createWebServiceDataProviderMerger()
supr java.lang.Object
hcls WebServiceDataProviderImpl,WebServiceDataProviderMerger

CLSS public abstract interface org.netbeans.modules.websvc.project.spi.ServiceDescriptorImplementation
meth public abstract java.net.URI getRelativeURI()
meth public abstract java.net.URL getRuntimeLocation()

CLSS public abstract interface org.netbeans.modules.websvc.project.spi.WebServiceDataProvider
meth public abstract java.util.List<org.netbeans.modules.websvc.project.api.WebService> getServiceConsumers()
meth public abstract java.util.List<org.netbeans.modules.websvc.project.api.WebService> getServiceProviders()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public final org.netbeans.modules.websvc.project.spi.WebServiceFactory
meth public static org.netbeans.modules.websvc.project.api.ServiceDescriptor createWebServiceDescriptor(org.netbeans.modules.websvc.project.spi.ServiceDescriptorImplementation)
meth public static org.netbeans.modules.websvc.project.api.WebService createWebService(org.netbeans.modules.websvc.project.spi.WebServiceImplementation)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.websvc.project.spi.WebServiceImplementation
meth public abstract boolean isServiceProvider()
meth public abstract java.lang.String getIdentifier()
meth public abstract org.netbeans.modules.websvc.project.api.ServiceDescriptor getServiceDescriptor()
meth public abstract org.netbeans.modules.websvc.project.api.WebService$Type getServiceType()
meth public abstract org.openide.nodes.Node createNode()

