#Signature file v4.1
#Version 1.45

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

CLSS public final org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport
fld public final static java.lang.String PROPERTY_SERVICE_ADDED = "service-added"
fld public final static java.lang.String PROPERTY_SERVICE_REMOVED = "service-removed"
meth public java.net.URL getCatalog()
meth public java.util.List<org.netbeans.modules.websvc.jaxws.light.api.JaxWsService> getServices()
meth public org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata> getWebservicesMetadataModel()
meth public org.openide.filesystems.FileObject getBindingsFolder(boolean)
meth public org.openide.filesystems.FileObject getDeploymentDescriptorFolder()
meth public org.openide.filesystems.FileObject getWsdlFolder(boolean)
meth public static org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport getJAXWSLightSupport(org.openide.filesystems.FileObject)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addService(org.netbeans.modules.websvc.jaxws.light.api.JaxWsService)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeService(org.netbeans.modules.websvc.jaxws.light.api.JaxWsService)
meth public void runAtomic(java.lang.Runnable)
supr java.lang.Object
hfds impl,myLock,myReadLock,myWriteLoick,propertyChangeSupport

CLSS public org.netbeans.modules.websvc.jaxws.light.api.JaxWsService
cons public init(java.lang.String,boolean)
cons public init(java.lang.String,java.lang.String)
meth public boolean isServiceProvider()
meth public java.lang.String getHandlerBindingFile()
meth public java.lang.String getId()
meth public java.lang.String getImplementationClass()
meth public java.lang.String getLocalWsdl()
meth public java.lang.String getPortName()
meth public java.lang.String getServiceName()
meth public java.lang.String getWsdlLocation()
meth public java.lang.String getWsdlUrl()
meth public void setHandlerBindingFile(java.lang.String)
meth public void setId(java.lang.String)
meth public void setImplementationClass(java.lang.String)
meth public void setLocalWsdl(java.lang.String)
meth public void setPortName(java.lang.String)
meth public void setServiceName(java.lang.String)
meth public void setServiceProvider(boolean)
meth public void setWsdlLocation(java.lang.String)
meth public void setWsdlUrl(java.lang.String)
supr java.lang.Object
hfds handlerBindingFile,id,implementationClass,localWsdl,portName,serviceName,serviceProvider,wsdlLocation,wsdlUrl

CLSS public final org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportFactory
meth public static org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport createJAXWSSupport(org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportImpl)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportImpl
meth public abstract java.net.URL getCatalog()
meth public abstract java.util.List<org.netbeans.modules.websvc.jaxws.light.api.JaxWsService> getServices()
meth public abstract org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata> getWebservicesMetadataModel()
meth public abstract org.openide.filesystems.FileObject getBindingsFolder(boolean)
meth public abstract org.openide.filesystems.FileObject getDeploymentDescriptorFolder()
meth public abstract org.openide.filesystems.FileObject getWsdlFolder(boolean)
meth public abstract void addService(org.netbeans.modules.websvc.jaxws.light.api.JaxWsService)
meth public abstract void removeService(org.netbeans.modules.websvc.jaxws.light.api.JaxWsService)

CLSS public abstract interface org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportProvider
meth public abstract org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport findJAXWSSupport()

