#Signature file v4.1
#Version 1.53

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

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.BindingCustomization
fld public final static java.lang.String ENABLE_MIME_CONTENT_PROPERTY = "enableMIMEContent"
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract org.netbeans.modules.websvc.api.customization.model.EnableMIMEContent getEnableMIMEContent()
meth public abstract void removeEnableMIMEContent(org.netbeans.modules.websvc.api.customization.model.EnableMIMEContent)
meth public abstract void setEnableMIMEContent(org.netbeans.modules.websvc.api.customization.model.EnableMIMEContent)

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.BindingOperationCustomization
fld public final static java.lang.String ENABLE_MIME_CONTENT_PROPERTY = "enableMIMEContent"
fld public final static java.lang.String JAVA_PARAMETER_PROPERTY = "parameter"
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract java.util.Collection<org.netbeans.modules.websvc.api.customization.model.JavaParameter> getJavaParameters()
meth public abstract org.netbeans.modules.websvc.api.customization.model.EnableMIMEContent getEnableMIMEContent()
meth public abstract void addJavaParameter(org.netbeans.modules.websvc.api.customization.model.JavaParameter)
meth public abstract void removeEnableMIMEContent(org.netbeans.modules.websvc.api.customization.model.EnableMIMEContent)
meth public abstract void removeJavaParameter(org.netbeans.modules.websvc.api.customization.model.JavaParameter)
meth public abstract void setEnableMIMEContent(org.netbeans.modules.websvc.api.customization.model.EnableMIMEContent)

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.CustomizationComponent
intf org.netbeans.modules.xml.xam.dom.DocumentComponent<org.netbeans.modules.websvc.api.customization.model.CustomizationComponent>

CLSS public org.netbeans.modules.websvc.api.customization.model.CustomizationComponentFactory
meth public org.netbeans.modules.websvc.api.customization.model.BindingCustomization createBindingCustomization(org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth public org.netbeans.modules.websvc.api.customization.model.BindingOperationCustomization createBindingOperationCustomization(org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth public org.netbeans.modules.websvc.api.customization.model.DefinitionsCustomization createDefinitionsCustomization(org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth public org.netbeans.modules.websvc.api.customization.model.EnableAsyncMapping createEnableAsyncMapping(org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth public org.netbeans.modules.websvc.api.customization.model.EnableMIMEContent createEnableMIMEContent(org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth public org.netbeans.modules.websvc.api.customization.model.EnableWrapperStyle createEnableWrapperStyle(org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth public org.netbeans.modules.websvc.api.customization.model.JavaClass createJavaClass(org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth public org.netbeans.modules.websvc.api.customization.model.JavaDoc createJavaDoc(org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth public org.netbeans.modules.websvc.api.customization.model.JavaException createJavaException(org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth public org.netbeans.modules.websvc.api.customization.model.JavaMethod createJavaMethod(org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth public org.netbeans.modules.websvc.api.customization.model.JavaPackage createJavaPackage(org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth public org.netbeans.modules.websvc.api.customization.model.JavaParameter createJavaParameter(org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth public org.netbeans.modules.websvc.api.customization.model.PortCustomization createPortCustomization(org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth public org.netbeans.modules.websvc.api.customization.model.PortTypeCustomization createPortTypeCustomization(org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth public org.netbeans.modules.websvc.api.customization.model.PortTypeOperationCustomization createPortTypeOperationCustomization(org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth public org.netbeans.modules.websvc.api.customization.model.PortTypeOperationFaultCustomization createPortTypeOperationFaultCustomization(org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth public org.netbeans.modules.websvc.api.customization.model.Provider createProvider(org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth public org.netbeans.modules.websvc.api.customization.model.ServiceCustomization createServiceCustomization(org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth public static org.netbeans.modules.websvc.api.customization.model.CustomizationComponentFactory getDefault()
supr java.lang.Object
hfds factory

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.DefinitionsCustomization
fld public final static java.lang.String ENABLE_ASYNC_MAPPING_PROPERTY = "enableAsyncMapping"
fld public final static java.lang.String ENABLE_MIME_CONTENT_PROPERTY = "enableMIMEContent"
fld public final static java.lang.String ENABLE_WRAPPER_STYLE_PROPERTY = "enableWrapperStyle"
fld public final static java.lang.String HANDLER_CHAINS_PROPERTY = "handlerChains"
fld public final static java.lang.String PACKAGE_PROPERTY = "package"
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract org.netbeans.modules.websvc.api.customization.model.EnableAsyncMapping getEnableAsyncMapping()
meth public abstract org.netbeans.modules.websvc.api.customization.model.EnableMIMEContent getEnableMIMEContent()
meth public abstract org.netbeans.modules.websvc.api.customization.model.EnableWrapperStyle getEnableWrapperStyle()
meth public abstract org.netbeans.modules.websvc.api.customization.model.JavaPackage getPackage()
meth public abstract void removeEnableAsyncMapping(org.netbeans.modules.websvc.api.customization.model.EnableAsyncMapping)
meth public abstract void removeEnableMIMEContent(org.netbeans.modules.websvc.api.customization.model.EnableMIMEContent)
meth public abstract void removeEnableWrapperStyle(org.netbeans.modules.websvc.api.customization.model.EnableWrapperStyle)
meth public abstract void removePackage(org.netbeans.modules.websvc.api.customization.model.JavaPackage)
meth public abstract void setEnableAsyncMapping(org.netbeans.modules.websvc.api.customization.model.EnableAsyncMapping)
meth public abstract void setEnableMIMEContent(org.netbeans.modules.websvc.api.customization.model.EnableMIMEContent)
meth public abstract void setEnableWrapperStyle(org.netbeans.modules.websvc.api.customization.model.EnableWrapperStyle)
meth public abstract void setPackage(org.netbeans.modules.websvc.api.customization.model.JavaPackage)

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.EnableAsyncMapping
fld public final static java.lang.String ENABLE_ASYNC_MAPPING_VALUE_PROPERTY = "enableAsyncMappingValue"
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract boolean isEnabled()
meth public abstract void setEnabled(boolean)

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.EnableMIMEContent
fld public final static java.lang.String ENABLE_MIME_CONTENT_VALUE_PROPERTY = "enableMIMEContentValue"
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract boolean isEnabled()
meth public abstract void setEnabled(boolean)

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.EnableWrapperStyle
fld public final static java.lang.String ENABLE_WRAPPER_STYLE_VALUE_PROPERTY = "enableWrapperStyleValue"
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract boolean isEnabled()
meth public abstract void setEnabled(boolean)

CLSS public final !enum org.netbeans.modules.websvc.api.customization.model.JAXWSQName
fld public final static java.lang.String JAXWS_NS_PREFIX = "jaxws"
fld public final static java.lang.String JAXWS_NS_URI = "http://java.sun.com/xml/ns/jaxws"
fld public final static org.netbeans.modules.websvc.api.customization.model.JAXWSQName BINDINGS
fld public final static org.netbeans.modules.websvc.api.customization.model.JAXWSQName CLASS
fld public final static org.netbeans.modules.websvc.api.customization.model.JAXWSQName ENABLEASYNCMAPPING
fld public final static org.netbeans.modules.websvc.api.customization.model.JAXWSQName ENABLEMIMECONTENT
fld public final static org.netbeans.modules.websvc.api.customization.model.JAXWSQName ENABLEWRAPPERSTYLE
fld public final static org.netbeans.modules.websvc.api.customization.model.JAXWSQName JAVADOC
fld public final static org.netbeans.modules.websvc.api.customization.model.JAXWSQName JAVAEXCEPTION
fld public final static org.netbeans.modules.websvc.api.customization.model.JAXWSQName METHOD
fld public final static org.netbeans.modules.websvc.api.customization.model.JAXWSQName PACKAGE
fld public final static org.netbeans.modules.websvc.api.customization.model.JAXWSQName PARAMETER
fld public final static org.netbeans.modules.websvc.api.customization.model.JAXWSQName PROVIDER
meth public javax.xml.namespace.QName getQName()
meth public static java.util.Set<javax.xml.namespace.QName> getQNames()
meth public static javax.xml.namespace.QName createJAXWSQName(java.lang.String)
meth public static org.netbeans.modules.websvc.api.customization.model.JAXWSQName valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.api.customization.model.JAXWSQName[] values()
supr java.lang.Enum<org.netbeans.modules.websvc.api.customization.model.JAXWSQName>
hfds qName,qnames

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.JavaClass
intf org.netbeans.modules.websvc.api.customization.model.JavaEntity

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.JavaDoc
fld public final static java.lang.String CONTENT_PROPERTY = "content"
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract java.lang.String getTextContent()
meth public abstract void setTextContent(java.lang.String)

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.JavaEntity
fld public final static java.lang.String JAVADOC_PROPERTY = "javadoc"
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
intf org.netbeans.modules.xml.xam.Nameable<org.netbeans.modules.xml.wsdl.model.WSDLComponent>
meth public abstract org.netbeans.modules.websvc.api.customization.model.JavaDoc getJavaDoc()
meth public abstract void removeJavaDoc(org.netbeans.modules.websvc.api.customization.model.JavaDoc)
meth public abstract void setJavaDoc(org.netbeans.modules.websvc.api.customization.model.JavaDoc)

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.JavaException
fld public final static java.lang.String JAVA_CLASS_PROPERTY = "class"
fld public final static java.lang.String PART_PROPERTY = "part"
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract java.lang.String getPart()
meth public abstract org.netbeans.modules.websvc.api.customization.model.JavaClass getJavaClass()
meth public abstract void removeJavaClass(org.netbeans.modules.websvc.api.customization.model.JavaClass)
meth public abstract void setJavaClass(org.netbeans.modules.websvc.api.customization.model.JavaClass)
meth public abstract void setPart(java.lang.String)

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.JavaMethod
intf org.netbeans.modules.websvc.api.customization.model.JavaEntity

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.JavaPackage
intf org.netbeans.modules.websvc.api.customization.model.JavaEntity

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.JavaParameter
fld public final static java.lang.String CHILD_ELEMENT_NAME_PROPERTY = "childElementName"
fld public final static java.lang.String PART_PROPERTY = "part"
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
intf org.netbeans.modules.xml.xam.Nameable<org.netbeans.modules.xml.wsdl.model.WSDLComponent>
meth public abstract java.lang.String getChildElementName()
meth public abstract java.lang.String getPart()
meth public abstract void setChildElementName(java.lang.String)
meth public abstract void setPart(java.lang.String)

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.PortCustomization
fld public final static java.lang.String JAVA_METHOD_PROPERTY = "method"
fld public final static java.lang.String PROVIDER_PROPERTY = "provider"
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract org.netbeans.modules.websvc.api.customization.model.JavaMethod getJavaMethod()
meth public abstract org.netbeans.modules.websvc.api.customization.model.Provider getProvider()
meth public abstract void removeJavaMethod(org.netbeans.modules.websvc.api.customization.model.JavaMethod)
meth public abstract void removeProvider(org.netbeans.modules.websvc.api.customization.model.Provider)
meth public abstract void setJavaMethod(org.netbeans.modules.websvc.api.customization.model.JavaMethod)
meth public abstract void setProvider(org.netbeans.modules.websvc.api.customization.model.Provider)

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.PortTypeCustomization
fld public final static java.lang.String ENABLE_ASYNC_MAPPING_PROPERTY = "enableAsyncMapping"
fld public final static java.lang.String ENABLE_WRAPPER_STYLE_PROPERTY = "enableWrapperStyle"
fld public final static java.lang.String JAVA_CLASS_PROPERTY = "class"
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract org.netbeans.modules.websvc.api.customization.model.EnableAsyncMapping getEnableAsyncMapping()
meth public abstract org.netbeans.modules.websvc.api.customization.model.EnableWrapperStyle getEnableWrapperStyle()
meth public abstract org.netbeans.modules.websvc.api.customization.model.JavaClass getJavaClass()
meth public abstract void removeEnableAsyncMapping(org.netbeans.modules.websvc.api.customization.model.EnableAsyncMapping)
meth public abstract void removeEnableWrapperStyle(org.netbeans.modules.websvc.api.customization.model.EnableWrapperStyle)
meth public abstract void removeJavaClass(org.netbeans.modules.websvc.api.customization.model.JavaClass)
meth public abstract void setEnableAsyncMapping(org.netbeans.modules.websvc.api.customization.model.EnableAsyncMapping)
meth public abstract void setEnableWrapperStyle(org.netbeans.modules.websvc.api.customization.model.EnableWrapperStyle)
meth public abstract void setJavaClass(org.netbeans.modules.websvc.api.customization.model.JavaClass)

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.PortTypeOperationCustomization
fld public final static java.lang.String ENABLE_ASYNC_MAPPING_PROPERTY = "enableAsyncMapping"
fld public final static java.lang.String ENABLE_WRAPPER_STYLE_PROPERTY = "enableWrapperStyle"
fld public final static java.lang.String JAVA_METHOD_PROPERTY = "method"
fld public final static java.lang.String JAVA_PARAMETER_PROPERTY = "parameter"
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract java.util.Collection<org.netbeans.modules.websvc.api.customization.model.JavaParameter> getJavaParameters()
meth public abstract org.netbeans.modules.websvc.api.customization.model.EnableAsyncMapping getEnableAsyncMapping()
meth public abstract org.netbeans.modules.websvc.api.customization.model.EnableWrapperStyle getEnableWrapperStyle()
meth public abstract org.netbeans.modules.websvc.api.customization.model.JavaMethod getJavaMethod()
meth public abstract void addJavaParameter(org.netbeans.modules.websvc.api.customization.model.JavaParameter)
meth public abstract void removeEnableAsyncMapping(org.netbeans.modules.websvc.api.customization.model.EnableAsyncMapping)
meth public abstract void removeEnableWrapperStyle(org.netbeans.modules.websvc.api.customization.model.EnableWrapperStyle)
meth public abstract void removeJavaMethod(org.netbeans.modules.websvc.api.customization.model.JavaMethod)
meth public abstract void removeJavaParameter(org.netbeans.modules.websvc.api.customization.model.JavaParameter)
meth public abstract void setEnableAsyncMapping(org.netbeans.modules.websvc.api.customization.model.EnableAsyncMapping)
meth public abstract void setEnableWrapperStyle(org.netbeans.modules.websvc.api.customization.model.EnableWrapperStyle)
meth public abstract void setJavaMethod(org.netbeans.modules.websvc.api.customization.model.JavaMethod)

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.PortTypeOperationFaultCustomization
fld public final static java.lang.String JAVA_CLASS_PROPERTY = "class"
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract org.netbeans.modules.websvc.api.customization.model.JavaClass getJavaClass()
meth public abstract void removeJavaClass(org.netbeans.modules.websvc.api.customization.model.JavaClass)
meth public abstract void setJavaClass(org.netbeans.modules.websvc.api.customization.model.JavaClass)

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.Provider
fld public final static java.lang.String ENABLE_PROVIDER_PROPERTY = "enableProvider"
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract boolean isEnabled()
meth public abstract void setEnabled(boolean)

CLSS public abstract interface org.netbeans.modules.websvc.api.customization.model.ServiceCustomization
fld public final static java.lang.String JAVA_CLASS_PROPERTY = "class"
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract org.netbeans.modules.websvc.api.customization.model.JavaClass getJavaClass()
meth public abstract void removeJavaClass(org.netbeans.modules.websvc.api.customization.model.JavaClass)
meth public abstract void setJavaClass(org.netbeans.modules.websvc.api.customization.model.JavaClass)

CLSS public abstract interface org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponent
intf org.netbeans.modules.xml.xam.dom.DocumentComponent<org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponent>
meth public abstract org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModel getModel()

CLSS public abstract interface org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponentFactory
intf org.netbeans.modules.xml.xam.dom.ComponentFactory<org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponent>
meth public abstract org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandler createHandler()
meth public abstract org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChain createHandlerChain()
meth public abstract org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChains createHandlerChains()
meth public abstract org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerClass createHandlerClass()
meth public abstract org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerName createHandlerName()
meth public abstract org.netbeans.modules.websvc.api.jaxws.bindings.DefinitionsBindings createDefinitionsBindings()
meth public abstract org.netbeans.modules.websvc.api.jaxws.bindings.GlobalBindings createGlobalBindings()

CLSS public abstract interface org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandler
fld public final static java.lang.String HANDLER_CLASS_PROPERTY = "handler_class"
fld public final static java.lang.String HANDLER_NAME_PROPERTY = "handler_name"
intf org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponent
meth public abstract org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerClass getHandlerClass()
meth public abstract org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerName getHandlerName()
meth public abstract void removeHandlerClass(org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerClass)
meth public abstract void setHandlerClass(org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerClass)
meth public abstract void setHandlerName(org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerName)

CLSS public abstract interface org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChain
fld public final static java.lang.String HANDLER_PROPERTY = "handler"
intf org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponent
meth public abstract java.util.Collection<org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandler> getHandlers()
meth public abstract void addHandler(org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandler)
meth public abstract void removeHandler(org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandler)

CLSS public abstract interface org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChains
fld public final static java.lang.String HANDLER_CHAIN_PROPERTY = "handler_chain"
intf org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponent
meth public abstract java.util.Collection<org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChain> getHandlerChains()
meth public abstract void addHandlerChain(org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChain)
meth public abstract void removeHandlerChain(org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChain)

CLSS public abstract interface org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerClass
fld public final static java.lang.String HANDLER_CLASS_NAME_PROPERTY = "handler_class_name"
intf org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponent
meth public abstract java.lang.String getClassName()
meth public abstract void setClassName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerName
fld public final static java.lang.String HANDLER_NAME_PROPERTY = "handler_name"
intf org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponent
meth public abstract java.lang.String getHandlerName()
meth public abstract void setHandlerName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModel
intf org.netbeans.modules.xml.xam.dom.DocumentModel<org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponent>
meth public abstract org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponentFactory getFactory()
meth public abstract org.netbeans.modules.websvc.api.jaxws.bindings.GlobalBindings getGlobalBindings()

CLSS public org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModelFactory
meth protected org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModel createModel(org.netbeans.modules.xml.xam.ModelSource)
meth public org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModel getModel(org.netbeans.modules.xml.xam.ModelSource)
meth public static org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModelFactory getDefault()
supr org.netbeans.modules.xml.xam.AbstractModelFactory<org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModel>
hfds bindingsModelFactory

CLSS public abstract interface org.netbeans.modules.websvc.api.jaxws.bindings.DefinitionsBindings
fld public final static java.lang.String HANDLER_CHAINS_PROPERTY = "handler_chains"
fld public final static java.lang.String NODE_PROPERTY = "node"
intf org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponent
meth public abstract java.lang.String getNode()
meth public abstract org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChains getHandlerChains()
meth public abstract void setHandlerChains(org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChains)
meth public abstract void setNode(java.lang.String)

CLSS public abstract interface org.netbeans.modules.websvc.api.jaxws.bindings.GlobalBindings
fld public final static java.lang.String DEFINITIONS_BINDINGS_PROPERTY = "definitions_bindings"
fld public final static java.lang.String WSDL_LOCATION_PROPERTY = "wsdlLocation"
intf org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponent
meth public abstract java.lang.String getWsdlLocation()
meth public abstract org.netbeans.modules.websvc.api.jaxws.bindings.DefinitionsBindings getDefinitionsBindings()
meth public abstract void setDefinitionsBindings(org.netbeans.modules.websvc.api.jaxws.bindings.DefinitionsBindings)
meth public abstract void setWsdlLocation(java.lang.String)

CLSS public org.netbeans.modules.websvc.api.jaxws.project.CatalogUtils
cons public init()
meth public static org.netbeans.modules.websvc.jaxws.catalog.CatalogModel getCatalogModel(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static void copyCatalogEntriesForAllClients(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel) throws java.io.IOException
meth public static void copyCatalogEntriesForClient(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static void updateCatalogEntriesForClient(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.websvc.api.jaxws.project.JAXWSVersionProvider
fld public final static java.lang.String JAXWS20 = "jaxws20"
fld public final static java.lang.String JAXWS21 = "jaxws21"
fld public final static java.lang.String JAXWS22 = "jaxws22"
meth public abstract java.lang.String getJAXWSVersion()

CLSS public abstract interface org.netbeans.modules.websvc.api.jaxws.project.JaxWsBuildScriptExtensionProvider
fld public final static java.lang.String JAXWS_EXTENSION = "jaxws"
meth public abstract void addJaxWsExtension(org.netbeans.api.project.ant.AntBuildExtender) throws java.io.IOException
meth public abstract void handleJaxWsModelChanges(org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel) throws java.io.IOException
meth public abstract void removeJaxWsExtension(org.netbeans.api.project.ant.AntBuildExtender) throws java.io.IOException

CLSS public org.netbeans.modules.websvc.api.jaxws.project.LogUtils
cons public init()
fld public final static java.lang.String USG_WEBSVC_DETECTED = "USG_WEBSVC_DETECTED"
fld public final static java.lang.String WS_STACK_JAXRPC = "JAX-RPC"
fld public final static java.lang.String WS_STACK_JAXRS = "JAX-RS"
fld public final static java.lang.String WS_STACK_JAXWS = "JAX-WS"
meth public static void logWsDetect(java.lang.Object[])
supr java.lang.Object
hfds USG_LOGGER_WEBSVC

CLSS public org.netbeans.modules.websvc.api.jaxws.project.WSUtils
cons public init()
fld public final static java.lang.String JAX_WS_ENDORSED = "JAX-WS-ENDORSED"
meth public static boolean hasClients(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static boolean hasServiceOrClient(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static java.lang.String findProperServiceName(java.lang.String,org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel)
meth public static java.lang.String getJAXWSVersion(java.io.File)
meth public static java.lang.String getPackageNameForWsdl(java.io.File)
meth public static java.util.Properties identifyWsimport(org.netbeans.spi.project.support.ant.AntProjectHelper)
meth public static org.netbeans.api.project.libraries.Library createJaxWsApiLibrary() throws java.io.IOException
meth public static org.netbeans.spi.project.support.ant.EditableProperties getEditableProperties(org.netbeans.api.project.Project,java.lang.String) throws java.io.IOException
meth public static org.openide.filesystems.FileObject backupAndGenerateJaxWs(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.RuntimeException) throws java.io.IOException
meth public static org.openide.filesystems.FileObject createJaxWsFileObject(org.netbeans.api.project.Project) throws java.io.IOException
meth public static org.openide.filesystems.FileObject findJaxWsFileObject(org.netbeans.api.project.Project)
meth public static org.openide.filesystems.FileObject retrieveJaxWsCatalogFromResource(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static org.openide.filesystems.FileObject retrieveResource(org.openide.filesystems.FileObject,java.net.URI) throws java.io.IOException,java.net.URISyntaxException
meth public static void addJaxWsApiEndorsed(org.netbeans.api.project.Project,org.openide.filesystems.FileObject) throws java.io.IOException
meth public static void copyFiles(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject) throws java.io.IOException
meth public static void generateSunJaxwsFile(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static void removeImplClass(org.netbeans.api.project.Project,java.lang.String)
meth public static void retrieveHandlerConfigFromResource(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static void retrieveJaxWsFromResource(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static void storeEditableProperties(org.netbeans.api.project.Project,java.lang.String,org.netbeans.spi.project.support.ant.EditableProperties) throws java.io.IOException
supr java.lang.Object
hfds DEFAULT_PACKAGE_NAME,ENDORSED,JAX_WS_XML_PATH,SUN_DOMAIN_12_DTD_SUFFIX,SUN_DOMAIN_13_DTD_SUFFIX,WSIMPORT_BAD_VERSION

CLSS public abstract interface org.netbeans.modules.websvc.api.jaxws.project.WebServiceNotifier
meth public abstract void serviceAdded(java.lang.String,java.lang.String)
meth public abstract void serviceRemoved(java.lang.String)

CLSS public org.netbeans.modules.websvc.api.jaxws.project.config.Binding
cons public init(org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding)
meth public java.lang.String getFileName()
meth public java.lang.String getOriginalFileUrl()
meth public void setFileName(java.lang.String)
meth public void setOriginalFileUrl(java.lang.String)
supr java.lang.Object
hfds binding

CLSS public final org.netbeans.modules.websvc.api.jaxws.project.config.Client
meth public boolean isPackageNameForceReplace()
meth public int sizeJvmArgs()
meth public java.lang.Boolean getUseDispatch()
meth public java.lang.String getCatalogFile()
meth public java.lang.String getHandlerBindingFile()
meth public java.lang.String getLocalWsdlFile()
meth public java.lang.String getName()
meth public java.lang.String getPackageName()
meth public java.lang.String getWsdlUrl()
meth public java.lang.String[] getJvmArgs()
meth public org.netbeans.modules.websvc.api.jaxws.project.config.Binding getBindingByFileName(java.lang.String)
meth public org.netbeans.modules.websvc.api.jaxws.project.config.Binding newBinding()
meth public org.netbeans.modules.websvc.api.jaxws.project.config.Binding[] getBindings()
meth public org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOptions getWsImportOptions()
meth public org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOptions newWsimportOptions()
meth public void addBinding(org.netbeans.modules.websvc.api.jaxws.project.config.Binding)
meth public void removeBinding(org.netbeans.modules.websvc.api.jaxws.project.config.Binding)
meth public void setBindings(org.netbeans.modules.websvc.api.jaxws.project.config.Binding[])
meth public void setCatalogFile(java.lang.String)
meth public void setHandlerBindingFile(java.lang.String)
meth public void setJvmArgs(java.lang.String[])
meth public void setLocalWsdlFile(java.lang.String)
meth public void setName(java.lang.String)
meth public void setPackageName(java.lang.String)
meth public void setPackageNameForceReplace(boolean)
meth public void setUseDispatch(java.lang.Boolean)
meth public void setWsdlUrl(java.lang.String)
meth public void setWsimportOptions(org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOptions)
supr java.lang.Object
hfds client

CLSS public org.netbeans.modules.websvc.api.jaxws.project.config.ClientAlreadyExistsExeption
cons public init(java.lang.String)
meth public java.lang.String getName()
supr java.lang.Exception
hfds name

CLSS public org.netbeans.modules.websvc.api.jaxws.project.config.Endpoint
cons public init(org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoint)
meth public java.lang.String getEndpointName()
meth public java.lang.String getImplementation()
meth public java.lang.String getUrlPattern()
meth public org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoint getOriginal()
meth public void setEndpointName(java.lang.String)
meth public void setImplementation(java.lang.String)
meth public void setUrlPattern(java.lang.String)
supr java.lang.Object
hfds endpoint

CLSS public org.netbeans.modules.websvc.api.jaxws.project.config.Endpoints
cons public init(org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoints)
meth public org.netbeans.modules.websvc.api.jaxws.project.config.Endpoint findEndpointByImplementation(java.lang.String)
meth public org.netbeans.modules.websvc.api.jaxws.project.config.Endpoint findEndpointByName(java.lang.String)
meth public org.netbeans.modules.websvc.api.jaxws.project.config.Endpoint newEndpoint()
meth public org.netbeans.modules.websvc.api.jaxws.project.config.Endpoint[] getEndpoints()
meth public void addEnpoint(org.netbeans.modules.websvc.api.jaxws.project.config.Endpoint)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void merge(org.netbeans.modules.websvc.api.jaxws.project.config.Endpoints)
meth public void removeEndpoint(org.netbeans.modules.websvc.api.jaxws.project.config.Endpoint)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void write(java.io.OutputStream) throws java.io.IOException
supr java.lang.Object
hfds endpoints

CLSS public org.netbeans.modules.websvc.api.jaxws.project.config.EndpointsProvider
meth public org.netbeans.modules.websvc.api.jaxws.project.config.Endpoints getEndpoints(java.io.InputStream) throws java.io.IOException
meth public org.netbeans.modules.websvc.api.jaxws.project.config.Endpoints getEndpoints(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static org.netbeans.modules.websvc.api.jaxws.project.config.EndpointsProvider getDefault()
supr java.lang.Object
hfds provider

CLSS public org.netbeans.modules.websvc.api.jaxws.project.config.Handler
cons public init(org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.Handler)
meth public java.lang.String getHandlerClass()
meth public java.lang.String getHandlerName()
meth public void setHandlerClass(java.lang.String)
meth public void setHandlerName(java.lang.String)
supr java.lang.Object
hfds handler

CLSS public org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChain
cons public init(org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChain)
meth public boolean removeHandler(java.lang.String)
meth public java.lang.String getHandlerChainName()
meth public org.netbeans.modules.websvc.api.jaxws.project.config.Handler findHandlerByName(java.lang.String)
meth public org.netbeans.modules.websvc.api.jaxws.project.config.Handler newHandler()
meth public org.netbeans.modules.websvc.api.jaxws.project.config.Handler[] getHandlers()
meth public void addHandler(java.lang.String,java.lang.String)
meth public void setHandlerChainName(java.lang.String)
supr java.lang.Object
hfds chain

CLSS public org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChains
cons public init(org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChains)
meth public org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChain findHandlerChainByName(java.lang.String)
meth public org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChain newChain()
meth public org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChain[] getHandlerChains()
meth public void addHandlerChain(java.lang.String,org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChain)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void merge(org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChains)
meth public void removeHandlerChain(org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChain)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void write(java.io.OutputStream) throws java.io.IOException
supr java.lang.Object
hfds handlerChains

CLSS public org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChainsProvider
meth public org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChains getHandlerChains(java.io.InputStream) throws java.io.IOException
meth public org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChains getHandlerChains(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChainsProvider getDefault()
supr java.lang.Object
hfds provider

CLSS public abstract interface org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel
innr public abstract interface static ServiceListener
meth public abstract boolean removeClient(java.lang.String)
meth public abstract boolean removeService(java.lang.String)
meth public abstract boolean removeServiceByClassName(java.lang.String)
meth public abstract java.lang.Boolean getJsr109()
meth public abstract org.netbeans.modules.websvc.api.jaxws.project.config.Client addClient(java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.websvc.api.jaxws.project.config.ClientAlreadyExistsExeption
meth public abstract org.netbeans.modules.websvc.api.jaxws.project.config.Client findClientByName(java.lang.String)
meth public abstract org.netbeans.modules.websvc.api.jaxws.project.config.Client findClientByWsdlUrl(java.lang.String)
meth public abstract org.netbeans.modules.websvc.api.jaxws.project.config.Client[] getClients()
meth public abstract org.netbeans.modules.websvc.api.jaxws.project.config.Service addService(java.lang.String,java.lang.String) throws org.netbeans.modules.websvc.api.jaxws.project.config.ServiceAlreadyExistsExeption
meth public abstract org.netbeans.modules.websvc.api.jaxws.project.config.Service addService(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.websvc.api.jaxws.project.config.ServiceAlreadyExistsExeption
meth public abstract org.netbeans.modules.websvc.api.jaxws.project.config.Service findServiceByImplementationClass(java.lang.String)
meth public abstract org.netbeans.modules.websvc.api.jaxws.project.config.Service findServiceByName(java.lang.String)
meth public abstract org.netbeans.modules.websvc.api.jaxws.project.config.Service[] getServices()
meth public abstract org.openide.filesystems.FileObject getJaxWsFile()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void addServiceListener(org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel$ServiceListener)
meth public abstract void merge(org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removeServiceListener(org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel$ServiceListener)
meth public abstract void setJaxWsFile(org.openide.filesystems.FileObject)
meth public abstract void setJsr109(java.lang.Boolean)
meth public abstract void write() throws java.io.IOException
meth public abstract void write(java.io.OutputStream) throws java.io.IOException

CLSS public abstract interface static org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel$ServiceListener
 outer org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel
meth public abstract void serviceAdded(java.lang.String,java.lang.String)
meth public abstract void serviceRemoved(java.lang.String)

CLSS public org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModelProvider
meth public org.netbeans.modules.websvc.api.jaxws.project.config.Client createClient(java.lang.Object)
meth public org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel getJaxWsModel(java.io.InputStream) throws java.io.IOException
meth public org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel getJaxWsModel(org.openide.filesystems.FileObject) throws java.io.IOException
meth public org.netbeans.modules.websvc.api.jaxws.project.config.Service createService(java.lang.Object)
meth public static org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModelProvider getDefault()
supr java.lang.Object
hfds provider

CLSS public final org.netbeans.modules.websvc.api.jaxws.project.config.Service
meth public boolean isPackageNameForceReplace()
meth public boolean isUseProvider()
meth public int sizeJvmArgs()
meth public java.lang.String getCatalogFile()
meth public java.lang.String getHandlerBindingFile()
meth public java.lang.String getImplementationClass()
meth public java.lang.String getLocalWsdlFile()
meth public java.lang.String getName()
meth public java.lang.String getPackageName()
meth public java.lang.String getPortName()
meth public java.lang.String getServiceName()
meth public java.lang.String getWsdlUrl()
meth public java.lang.String[] getJvmArgs()
meth public org.netbeans.modules.websvc.api.jaxws.project.config.Binding getBindingByFileName(java.lang.String)
meth public org.netbeans.modules.websvc.api.jaxws.project.config.Binding newBinding()
meth public org.netbeans.modules.websvc.api.jaxws.project.config.Binding[] getBindings()
meth public org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOptions getWsImportOptions()
meth public org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOptions newWsimportOptions()
meth public void addBinding(org.netbeans.modules.websvc.api.jaxws.project.config.Binding)
meth public void removeBinding(org.netbeans.modules.websvc.api.jaxws.project.config.Binding)
meth public void setBindings(org.netbeans.modules.websvc.api.jaxws.project.config.Binding[])
meth public void setCatalogFile(java.lang.String)
meth public void setHandlerBindingFile(java.lang.String)
meth public void setImplementationClass(java.lang.String)
meth public void setJvmArgs(java.lang.String[])
meth public void setLocalWsdlFile(java.lang.String)
meth public void setName(java.lang.String)
meth public void setPackageName(java.lang.String)
meth public void setPackageNameForceReplace(boolean)
meth public void setPortName(java.lang.String)
meth public void setServiceName(java.lang.String)
meth public void setUseProvider(java.lang.Boolean)
meth public void setWsdlUrl(java.lang.String)
meth public void setWsimportOptions(org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOptions)
supr java.lang.Object
hfds service

CLSS public org.netbeans.modules.websvc.api.jaxws.project.config.ServiceAlreadyExistsExeption
cons public init(java.lang.String)
meth public java.lang.String getName()
supr java.lang.Exception
hfds name

CLSS public org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOption
cons public init(org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.WsimportOption)
meth public java.lang.Boolean getJaxbOption()
meth public java.lang.String getWsimportOptionName()
meth public java.lang.String getWsimportOptionValue()
meth public org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.WsimportOption getOriginal()
meth public void setJaxbOption(java.lang.Boolean)
meth public void setWsimportOptionName(java.lang.String)
meth public void setWsimportOptionValue(java.lang.String)
supr java.lang.Object
hfds wsimportOption

CLSS public org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOptions
cons public init(org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.WsimportOptions)
meth public org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOption newWsimportOption()
meth public org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOption[] getWsimportOptions()
meth public org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.WsimportOptions getOriginal()
meth public void addWsimportOption(org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOption)
meth public void clearWsimportOptions()
meth public void removeWsimportOption(org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOption)
supr java.lang.Object
hfds wsimportOptions

CLSS public org.netbeans.modules.websvc.api.jaxws.wsdlmodel.JaxwsWsdlModelProvider
cons public init()
intf org.netbeans.modules.websvc.jaxwsmodelapi.wsdlmodel.WsdlModelProvider
meth public boolean canAccept(java.net.URL)
meth public java.lang.String getEffectivePackageName()
meth public java.lang.Throwable getCreationException()
meth public org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel getWsdlModel(java.net.URL,java.lang.String,java.net.URL,boolean)
meth public static boolean isRPCEncoded(org.netbeans.modules.xml.wsdl.model.WSDLModel)
supr java.lang.Object
hfds creationException,packageName

CLSS public abstract interface org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlChangeListener
meth public abstract void wsdlModelChanged(org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel,org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel)

CLSS public abstract interface org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlErrorHandler
innr public static AbortException
meth public abstract void error(org.xml.sax.SAXParseException) throws org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlErrorHandler$AbortException
meth public abstract void fatalError(org.xml.sax.SAXParseException) throws org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlErrorHandler$AbortException
meth public abstract void info(org.xml.sax.SAXParseException)
meth public abstract void warning(org.xml.sax.SAXParseException) throws org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlErrorHandler$AbortException

CLSS public static org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlErrorHandler$AbortException
 outer org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlErrorHandler
cons public init()
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel
intf org.netbeans.modules.websvc.jaxwsmodelapi.wsdlmodel.WsdlModel
meth public java.lang.Object getInternalJAXWSModel()
meth public java.util.List<org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService> getServices()
meth public org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService getServiceByName(java.lang.String)
supr java.lang.Object
hfds model

CLSS public abstract interface org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener
meth public abstract void modelCreated(org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel)

CLSS public org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler
fld protected java.util.Properties properties
meth public java.lang.String getPackageName()
meth public java.lang.Throwable getCreationException()
meth public java.net.URL getCatalog()
meth public java.net.URL getWsdlUrl()
meth public java.net.URL[] getJAXBBindings()
meth public org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel getAndWaitForWsdlModel()
meth public org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel getAndWaitForWsdlModel(boolean)
meth public org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel getWsdlModel()
meth public void addWsdlChangeListener(org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlChangeListener)
meth public void generateWsdlModel(org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener)
meth public void generateWsdlModel(org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener,boolean)
meth public void generateWsdlModel(org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener,org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlErrorHandler)
meth public void removeWsdlChangeListener(org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlChangeListener)
meth public void setCatalog(java.net.URL)
meth public void setJAXBBindings(java.net.URL[])
meth public void setPackageName(java.lang.String)
supr java.lang.Object
hfds bindingFiles,bindings,catalog,creationException,entityResolver,ideWSDLModeler,listenersSize,modelListeners,packageName,task,wsdlChangeListeners,wsdlModel,wsdlUrl
hcls CatchFirstErrorHandler,IdeErrorReceiver

CLSS public org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory
meth public org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler getWsdlModeler(java.net.URL)
meth public static org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory getDefault()
supr java.lang.Object
hfds factory,modelers

CLSS public org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation
cons public init(com.sun.tools.ws.processor.model.Operation)
intf org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation
meth public int getOperationType()
meth public java.lang.Object getInternalJAXWSOperation()
meth public java.lang.String getJavaName()
meth public java.lang.String getName()
meth public java.lang.String getOperationName()
meth public java.lang.String getReturnTypeName()
meth public java.util.Iterator<java.lang.String> getExceptions()
meth public java.util.List<org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlParameter> getParameters()
meth public org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaMethod getJavaMethod()
meth public void setOperationName(java.lang.String)
supr java.lang.Object
hfds operation,operationName

CLSS public org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlParameter
cons public init(com.sun.tools.ws.processor.model.java.JavaParameter)
intf org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter
meth public boolean isHolder()
meth public java.lang.Object getInternalJAXWSParameter()
meth public java.lang.String getHolderName()
meth public java.lang.String getName()
meth public java.lang.String getTypeName()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds name,parameter

CLSS public org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort
intf org.netbeans.modules.websvc.jaxwsmodelapi.WSPort
meth public boolean isProvider()
meth public java.lang.Object getInternalJAXWSPort()
meth public java.lang.String getAddress()
meth public java.lang.String getJavaName()
meth public java.lang.String getName()
meth public java.lang.String getNamespaceURI()
meth public java.lang.String getPortGetter()
meth public java.lang.String getSOAPVersion()
meth public java.lang.String getStyle()
meth public java.util.List<org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation> getOperations()
meth public void setSOAPVersion(java.lang.String)
supr java.lang.Object
hfds port,soapVersion

CLSS public org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService
cons public init()
intf org.netbeans.modules.websvc.jaxwsmodelapi.WSService
meth public java.lang.Object getInternalJAXWSService()
meth public java.lang.String getJavaName()
meth public java.lang.String getName()
meth public java.lang.String getNamespaceURI()
meth public java.util.List<org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort> getPorts()
meth public org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort getPortByName(java.lang.String)
supr java.lang.Object
hfds service

CLSS public org.netbeans.modules.websvc.api.wseditor.InvalidDataException
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getLocalizedMessage()
supr java.lang.Exception
hfds localizedMessage,serialVersionUID

CLSS public abstract interface org.netbeans.modules.websvc.api.wseditor.SaveSetter
meth public abstract void setDirty()

CLSS public abstract interface org.netbeans.modules.websvc.api.wseditor.WSEditor
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getTitle()
meth public abstract javax.swing.JComponent createWSEditorComponent(org.openide.nodes.Node) throws org.netbeans.modules.websvc.api.wseditor.InvalidDataException
meth public abstract void cancel(org.openide.nodes.Node)
meth public abstract void save(org.openide.nodes.Node)

CLSS public org.netbeans.modules.websvc.api.wseditor.WSEditorProviderRegistry
meth public java.util.Set<org.netbeans.modules.websvc.spi.wseditor.WSEditorProvider> getEditorProviders()
meth public static org.netbeans.modules.websvc.api.wseditor.WSEditorProviderRegistry getDefault()
meth public void register(org.netbeans.modules.websvc.spi.wseditor.WSEditorProvider)
meth public void unregister(org.netbeans.modules.websvc.spi.wseditor.WSEditorProvider)
supr java.lang.Object
hfds editors,registry

CLSS public abstract interface org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation
fld public final static int TYPE_ASYNC_CALLBACK = 2
fld public final static int TYPE_ASYNC_POLLING = 1
fld public final static int TYPE_NORMAL = 0
meth public abstract int getOperationType()
meth public abstract java.lang.Object getInternalJAXWSOperation()
meth public abstract java.lang.String getJavaName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getOperationName()
meth public abstract java.lang.String getReturnTypeName()
meth public abstract java.util.Iterator<java.lang.String> getExceptions()
meth public abstract java.util.List<? extends org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter> getParameters()
meth public abstract org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaMethod getJavaMethod()

CLSS public abstract interface org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter
meth public abstract boolean isHolder()
meth public abstract java.lang.Object getInternalJAXWSParameter()
meth public abstract java.lang.String getHolderName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getTypeName()

CLSS public abstract interface org.netbeans.modules.websvc.jaxwsmodelapi.WSPort
fld public final static java.lang.String SOAP_VERSION_11 = "http://schemas.xmlsoap.org/wsdl/soap/"
fld public final static java.lang.String SOAP_VERSION_12 = "http://schemas.xmlsoap.org/wsdl/soap12/"
fld public final static java.lang.String STYLE_DOCUMENT = "document"
fld public final static java.lang.String STYLE_RPC = "rpc"
meth public abstract boolean isProvider()
meth public abstract java.lang.Object getInternalJAXWSPort()
meth public abstract java.lang.String getAddress()
meth public abstract java.lang.String getJavaName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getNamespaceURI()
meth public abstract java.lang.String getPortGetter()
meth public abstract java.lang.String getSOAPVersion()
meth public abstract java.lang.String getStyle()
meth public abstract java.util.List<? extends org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation> getOperations()

CLSS public abstract interface org.netbeans.modules.websvc.jaxwsmodelapi.WSService
meth public abstract java.lang.Object getInternalJAXWSService()
meth public abstract java.lang.String getJavaName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getNamespaceURI()
meth public abstract java.util.List<? extends org.netbeans.modules.websvc.jaxwsmodelapi.WSPort> getPorts()
meth public abstract org.netbeans.modules.websvc.jaxwsmodelapi.WSPort getPortByName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.websvc.jaxwsmodelapi.wsdlmodel.WsdlModel
meth public abstract java.util.List<? extends org.netbeans.modules.websvc.jaxwsmodelapi.WSService> getServices()
meth public abstract org.netbeans.modules.websvc.jaxwsmodelapi.WSService getServiceByName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.websvc.jaxwsmodelapi.wsdlmodel.WsdlModelProvider
meth public abstract boolean canAccept(java.net.URL)
meth public abstract java.lang.String getEffectivePackageName()
meth public abstract java.lang.Throwable getCreationException()
meth public abstract org.netbeans.modules.websvc.jaxwsmodelapi.wsdlmodel.WsdlModel getWsdlModel(java.net.URL,java.lang.String,java.net.URL,boolean)

CLSS public abstract interface org.netbeans.modules.websvc.spi.wseditor.WSEditorProvider
meth public abstract boolean enable(org.openide.nodes.Node)
meth public abstract org.netbeans.modules.websvc.api.wseditor.WSEditor createWSEditor(org.openide.util.Lookup)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
fld public final static java.lang.String CONTENT_FRAGMENT_PROPERTY = "content"
innr public abstract interface static EmbeddedModel
innr public abstract interface static ParentSelector
innr public abstract interface static UpdaterProvider
intf org.netbeans.modules.xml.wsdl.model.WSDLComponent
meth public abstract java.lang.String getAnyAttribute(javax.xml.namespace.QName)
meth public abstract java.lang.String getAttribute(java.lang.String)
meth public abstract java.lang.String getContentFragment()
meth public abstract java.util.List<org.netbeans.modules.xml.wsdl.model.ExtensibilityElement> getAnyElements()
meth public abstract javax.xml.namespace.QName getQName()
meth public abstract void addAnyElement(org.netbeans.modules.xml.wsdl.model.ExtensibilityElement,int)
meth public abstract void removeAnyElement(org.netbeans.modules.xml.wsdl.model.ExtensibilityElement)
meth public abstract void setAnyAttribute(javax.xml.namespace.QName,java.lang.String)
meth public abstract void setAttribute(java.lang.String,java.lang.String)
meth public abstract void setContentFragment(java.lang.String) throws java.io.IOException

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.WSDLComponent
fld public final static java.lang.String DOCUMENTATION_PROPERTY = "documentation"
fld public final static java.lang.String EXTENSIBILITY_ELEMENT_PROPERTY = "extensibilityElement"
intf org.netbeans.modules.xml.xam.dom.DocumentComponent<org.netbeans.modules.xml.wsdl.model.WSDLComponent>
meth public abstract <%0 extends org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent> org.netbeans.modules.xml.xam.dom.NamedComponentReference<{%%0}> createSchemaReference({%%0},java.lang.Class<{%%0}>)
meth public abstract <%0 extends org.netbeans.modules.xml.wsdl.model.ExtensibilityElement> java.util.List<{%%0}> getExtensibilityElements(java.lang.Class<{%%0}>)
meth public abstract <%0 extends org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent> org.netbeans.modules.xml.xam.dom.NamedComponentReference<{%%0}> createReferenceTo({%%0},java.lang.Class<{%%0}>)
meth public abstract java.util.List<org.netbeans.modules.xml.wsdl.model.ExtensibilityElement> getExtensibilityElements()
meth public abstract java.util.Map<javax.xml.namespace.QName,java.lang.String> getAttributeMap()
meth public abstract org.netbeans.modules.xml.wsdl.model.Documentation getDocumentation()
meth public abstract org.netbeans.modules.xml.wsdl.model.WSDLModel getModel()
meth public abstract void accept(org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor)
meth public abstract void addExtensibilityElement(org.netbeans.modules.xml.wsdl.model.ExtensibilityElement)
meth public abstract void removeExtensibilityElement(org.netbeans.modules.xml.wsdl.model.ExtensibilityElement)
meth public abstract void setDocumentation(org.netbeans.modules.xml.wsdl.model.Documentation)

CLSS public abstract org.netbeans.modules.xml.xam.AbstractModelFactory<%0 extends org.netbeans.modules.xml.xam.Model>
cons public init()
fld public final static int DELAY_DIRTY = 1000
fld public final static int DELAY_SYNCER = 2000
fld public final static java.lang.String MODEL_LOADED_PROPERTY = "modelLoaded"
meth protected abstract {org.netbeans.modules.xml.xam.AbstractModelFactory%0} createModel(org.netbeans.modules.xml.xam.ModelSource)
meth protected java.lang.Object getKey(org.netbeans.modules.xml.xam.ModelSource)
meth protected {org.netbeans.modules.xml.xam.AbstractModelFactory%0} getModel(org.netbeans.modules.xml.xam.ModelSource)
meth public java.util.List<{org.netbeans.modules.xml.xam.AbstractModelFactory%0}> getModels()
meth public static org.netbeans.modules.xml.xam.spi.ModelAccessProvider getAccessProvider()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public {org.netbeans.modules.xml.xam.AbstractModelFactory%0} createFreshModel(org.netbeans.modules.xml.xam.ModelSource)
supr java.lang.Object
hfds LOG,SYNCER,cachedModels,factories,propSupport

CLSS public abstract interface org.netbeans.modules.xml.xam.Component<%0 extends org.netbeans.modules.xml.xam.Component>
meth public abstract <%0 extends {org.netbeans.modules.xml.xam.Component%0}> java.util.List<{%%0}> getChildren(java.lang.Class<{%%0}>)
meth public abstract boolean canPaste(org.netbeans.modules.xml.xam.Component)
meth public abstract java.util.List<{org.netbeans.modules.xml.xam.Component%0}> getChildren()
meth public abstract java.util.List<{org.netbeans.modules.xml.xam.Component%0}> getChildren(java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.Component%0}>>)
meth public abstract org.netbeans.modules.xml.xam.Component copy({org.netbeans.modules.xml.xam.Component%0})
meth public abstract org.netbeans.modules.xml.xam.Model getModel()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract {org.netbeans.modules.xml.xam.Component%0} getParent()

CLSS public abstract interface org.netbeans.modules.xml.xam.Model<%0 extends org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.Model%0}>>
fld public final static java.lang.String STATE_PROPERTY = "state"
innr public final static !enum State
intf org.netbeans.modules.xml.xam.Referenceable
meth public abstract boolean inSync()
meth public abstract boolean isIntransaction()
meth public abstract boolean startTransaction()
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
meth public abstract org.netbeans.modules.xml.xam.Model$State getState()
meth public abstract org.netbeans.modules.xml.xam.ModelSource getModelSource()
meth public abstract void addChildComponent(org.netbeans.modules.xml.xam.Component,org.netbeans.modules.xml.xam.Component,int)
meth public abstract void addComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void addUndoableRefactorListener(javax.swing.event.UndoableEditListener)
meth public abstract void endTransaction()
meth public abstract void removeChildComponent(org.netbeans.modules.xml.xam.Component)
meth public abstract void removeComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void removeUndoableRefactorListener(javax.swing.event.UndoableEditListener)
meth public abstract void sync() throws java.io.IOException

CLSS public abstract interface org.netbeans.modules.xml.xam.Nameable<%0 extends org.netbeans.modules.xml.xam.Component>
intf org.netbeans.modules.xml.xam.Named<{org.netbeans.modules.xml.xam.Nameable%0}>
meth public abstract void setName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.xam.Named<%0 extends org.netbeans.modules.xml.xam.Component>
fld public final static java.lang.String NAME_PROPERTY = "name"
intf org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.Named%0}>
meth public abstract java.lang.String getName()

CLSS public abstract interface org.netbeans.modules.xml.xam.Referenceable

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.ComponentFactory<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent<{org.netbeans.modules.xml.xam.dom.ComponentFactory%0}>>
meth public abstract {org.netbeans.modules.xml.xam.dom.ComponentFactory%0} create(org.w3c.dom.Element,{org.netbeans.modules.xml.xam.dom.ComponentFactory%0})

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.DocumentComponent<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent>
fld public final static java.lang.String TEXT_CONTENT_PROPERTY = "textContent"
intf org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.dom.DocumentComponent%0}>
meth public abstract boolean isInDocumentModel()
meth public abstract boolean referencesSameNode(org.w3c.dom.Node)
meth public abstract int findAttributePosition(java.lang.String)
meth public abstract int findPosition()
meth public abstract java.lang.String getAttribute(org.netbeans.modules.xml.xam.dom.Attribute)
meth public abstract org.w3c.dom.Element getPeer()
meth public abstract void setAttribute(java.lang.String,org.netbeans.modules.xml.xam.dom.Attribute,java.lang.Object)
meth public abstract {org.netbeans.modules.xml.xam.dom.DocumentComponent%0} findChildComponent(org.w3c.dom.Element)

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.DocumentModel<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent<{org.netbeans.modules.xml.xam.dom.DocumentModel%0}>>
intf org.netbeans.modules.xml.xam.Model<{org.netbeans.modules.xml.xam.dom.DocumentModel%0}>
meth public abstract boolean areSameNodes(org.w3c.dom.Node,org.w3c.dom.Node)
meth public abstract java.lang.String getXPathExpression(org.netbeans.modules.xml.xam.dom.DocumentComponent)
meth public abstract org.netbeans.modules.xml.xam.dom.DocumentComponent findComponent(int)
meth public abstract org.w3c.dom.Document getDocument()
meth public abstract {org.netbeans.modules.xml.xam.dom.DocumentModel%0} createComponent({org.netbeans.modules.xml.xam.dom.DocumentModel%0},org.w3c.dom.Element)
meth public abstract {org.netbeans.modules.xml.xam.dom.DocumentModel%0} getRootComponent()

