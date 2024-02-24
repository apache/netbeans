#Signature file v4.1
#Version 1.48

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

CLSS public abstract interface org.netbeans.modules.websvc.design.configuration.WSConfiguration
fld public final static java.lang.String PROPERTY = "value"
fld public final static java.lang.String PROPERTY_ENABLE = "enabled"
meth public abstract boolean isEnabled()
meth public abstract boolean isSet()
meth public abstract java.awt.Component getComponent()
meth public abstract java.awt.Image getIcon()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getDisplayName()
meth public abstract void registerListener(java.beans.PropertyChangeListener)
meth public abstract void set()
meth public abstract void unregisterListener(java.beans.PropertyChangeListener)
meth public abstract void unset()

CLSS public abstract interface org.netbeans.modules.websvc.design.configuration.WSConfigurationProvider
meth public abstract org.netbeans.modules.websvc.design.configuration.WSConfiguration getWSConfiguration(org.netbeans.modules.websvc.api.jaxws.project.config.Service,org.openide.filesystems.FileObject)

CLSS public org.netbeans.modules.websvc.design.configuration.WSConfigurationProviderRegistry
meth public java.util.Set<org.netbeans.modules.websvc.design.configuration.WSConfigurationProvider> getWSConfigurationProviders()
meth public static org.netbeans.modules.websvc.design.configuration.WSConfigurationProviderRegistry getDefault()
meth public void register(org.netbeans.modules.websvc.design.configuration.WSConfigurationProvider)
meth public void unregister(org.netbeans.modules.websvc.design.configuration.WSConfigurationProvider)
supr java.lang.Object
hfds providers,registry

CLSS public org.netbeans.modules.websvc.design.javamodel.FaultModel
meth public boolean isEqualTo(org.netbeans.modules.websvc.design.javamodel.FaultModel)
meth public java.lang.String getFaultType()
meth public java.lang.String getName()
meth public java.lang.String getTargetNamespace()
supr java.lang.Object
hfds faultType,name,targetNamespace

CLSS public org.netbeans.modules.websvc.design.javamodel.JavadocModel
meth public boolean isEqualTo(org.netbeans.modules.websvc.design.javamodel.JavadocModel)
meth public java.lang.String getText()
supr java.lang.Object
hfds inlineJavadoc,paramJavadoc,returnJavadoc,text,throwsJavadoc

CLSS public org.netbeans.modules.websvc.design.javamodel.MethodModel
meth public boolean isEqualTo(org.netbeans.modules.websvc.design.javamodel.MethodModel)
meth public boolean isOneWay()
meth public java.lang.String getAction()
meth public java.lang.String getOperationName()
meth public java.util.List<org.netbeans.modules.websvc.design.javamodel.FaultModel> getFaults()
meth public java.util.List<org.netbeans.modules.websvc.design.javamodel.ParamModel> getParams()
meth public javax.xml.soap.SOAPMessage getSoapRequest()
meth public javax.xml.soap.SOAPMessage getSoapResponse()
meth public org.netbeans.api.java.source.ElementHandle getMethodHandle()
meth public org.netbeans.modules.websvc.design.javamodel.JavadocModel getJavadoc()
meth public org.netbeans.modules.websvc.design.javamodel.ResultModel getResult()
meth public org.openide.filesystems.FileObject getImplementationClass()
meth public void setAction(java.lang.String)
meth public void setJavadoc(java.lang.String)
meth public void setOneWay(boolean)
meth public void setOperationName(java.lang.String)
supr java.lang.Object
hfds action,faults,implementationClass,javaName,javadoc,methodHandle,oneWay,operationName,params,result,soapRequest,soapResponse

CLSS public org.netbeans.modules.websvc.design.javamodel.ParamModel
meth public boolean isEqualTo(org.netbeans.modules.websvc.design.javamodel.ParamModel)
meth public java.lang.String getName()
meth public java.lang.String getParamType()
meth public java.lang.String getPartName()
meth public java.lang.String getTargetNamespace()
meth public javax.jws.WebParam$Mode getMode()
meth public org.netbeans.api.java.source.ElementHandle getMethodHandle()
meth public org.openide.filesystems.FileObject getImplementationClass()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds implementationClass,javaName,methodHandle,mode,name,paramType,partName,targetNamespace

CLSS public abstract interface org.netbeans.modules.websvc.design.javamodel.ProjectService
meth public abstract java.lang.String getImplementationClass()
meth public abstract java.lang.String getLocalWsdlFile()
meth public abstract java.lang.String getWsdlUrl()
meth public abstract java.util.Collection<org.netbeans.modules.websvc.design.configuration.WSConfiguration> getConfigurations()
meth public abstract void cleanup() throws java.io.IOException

CLSS public org.netbeans.modules.websvc.design.javamodel.ResultModel
meth public boolean isEqualTo(org.netbeans.modules.websvc.design.javamodel.ResultModel)
meth public java.lang.String getName()
meth public java.lang.String getPartame()
meth public java.lang.String getResultType()
meth public java.lang.String getTargetNamespace()
supr java.lang.Object
hfds name,partName,resultType,targetNamespace

CLSS public abstract interface org.netbeans.modules.websvc.design.javamodel.ServiceChangeListener
meth public abstract void operationAdded(org.netbeans.modules.websvc.design.javamodel.MethodModel)
meth public abstract void operationChanged(org.netbeans.modules.websvc.design.javamodel.MethodModel,org.netbeans.modules.websvc.design.javamodel.MethodModel)
meth public abstract void operationRemoved(org.netbeans.modules.websvc.design.javamodel.MethodModel)
meth public abstract void propertyChanged(java.lang.String,java.lang.String,java.lang.String)

CLSS public org.netbeans.modules.websvc.design.javamodel.ServiceModel
fld public final static int STATUS_INCORRECT_SERVICE = 2
fld public final static int STATUS_NOT_SERVICE = 1
fld public final static int STATUS_OK = 0
meth public int getStatus()
meth public java.lang.String getEndpointInterface()
meth public java.lang.String getName()
meth public java.lang.String getPortName()
meth public java.lang.String getServiceName()
meth public java.lang.String getTargetNamespace()
meth public java.lang.String getWsdlLocation()
meth public java.util.List<org.netbeans.modules.websvc.design.javamodel.MethodModel> getOperations()
meth public org.openide.filesystems.FileObject getImplementationClass()
meth public static org.netbeans.modules.websvc.design.javamodel.ServiceModel getServiceModel(org.openide.filesystems.FileObject)
meth public void addServiceChangeListener(org.netbeans.modules.websvc.design.javamodel.ServiceChangeListener)
meth public void removeServiceChangeListener(org.netbeans.modules.websvc.design.javamodel.ServiceChangeListener)
meth public void setName(java.lang.String)
meth public void setPortName(java.lang.String)
meth public void setServiceName(java.lang.String)
meth public void setStatus(int)
meth public void setTargetNamespace(java.lang.String)
supr java.lang.Object
hfds FILE_CHANGE_RP,changeSource,endpointInterface,fcl,implementationClass,name,operations,portName,serviceChangeListeners,serviceName,status,targetNamespace,wsdlLocation
hcls AnnotationChangeListener

CLSS public org.netbeans.modules.websvc.design.javamodel.Utils
cons public init()
meth public static boolean isEqualTo(java.lang.String,java.lang.String)
meth public static java.lang.String getAttributeValue(org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
meth public static java.lang.String getCurrentJavaName(org.netbeans.modules.websvc.design.javamodel.MethodModel)
meth public static java.lang.String getFormatedDocument(javax.xml.soap.SOAPMessage)
meth public static org.netbeans.modules.websvc.api.jaxws.project.config.Service getService(org.netbeans.modules.websvc.design.javamodel.ProjectService)
meth public static org.netbeans.modules.websvc.design.javamodel.ProjectService getProjectService(org.openide.loaders.DataObject)
meth public static org.netbeans.modules.websvc.design.javamodel.ServiceModel populateModel(org.openide.filesystems.FileObject)
meth public static void invokeWsImport(org.netbeans.api.project.Project,java.lang.String)
meth public static void setJavadoc(org.openide.filesystems.FileObject,org.netbeans.modules.websvc.design.javamodel.MethodModel,java.lang.String)
supr java.lang.Object

