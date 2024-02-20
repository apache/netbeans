#Signature file v4.1
#Version 1.55

CLSS public abstract com.sun.codemodel.CodeWriter
cons public init()
fld protected java.lang.String encoding
meth public abstract java.io.OutputStream openBinary(com.sun.codemodel.JPackage,java.lang.String) throws java.io.IOException
meth public abstract void close() throws java.io.IOException
meth public java.io.Writer openSource(com.sun.codemodel.JPackage,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public com.sun.codemodel.writer.FileCodeWriter
cons public init(java.io.File) throws java.io.IOException
cons public init(java.io.File,boolean) throws java.io.IOException
cons public init(java.io.File,boolean,java.lang.String) throws java.io.IOException
cons public init(java.io.File,java.lang.String) throws java.io.IOException
meth protected java.io.File getFile(com.sun.codemodel.JPackage,java.lang.String) throws java.io.IOException
meth public java.io.OutputStream openBinary(com.sun.codemodel.JPackage,java.lang.String) throws java.io.IOException
meth public void close() throws java.io.IOException
supr com.sun.codemodel.CodeWriter
hfds readOnly,readonlyFiles,target

CLSS public abstract interface com.sun.istack.localization.Localizable
fld public final static java.lang.String NOT_LOCALIZABLE = "\u0000"
meth public abstract java.lang.Object[] getArguments()
meth public abstract java.lang.String getKey()
meth public abstract java.lang.String getResourceBundleName()
meth public abstract java.util.ResourceBundle getResourceBundle(java.util.Locale)

CLSS public com.sun.istack.localization.LocalizableMessageFactory
cons public init(java.lang.String)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,com.sun.istack.localization.LocalizableMessageFactory$ResourceBundleSupplier)
innr public abstract interface static ResourceBundleSupplier
meth public !varargs com.sun.istack.localization.Localizable getMessage(java.lang.String,java.lang.Object[])
supr java.lang.Object
hfds _bundlename,_rbSupplier

CLSS public abstract interface static com.sun.istack.localization.LocalizableMessageFactory$ResourceBundleSupplier
 outer com.sun.istack.localization.LocalizableMessageFactory
meth public abstract java.util.ResourceBundle getResourceBundle(java.util.Locale)

CLSS public abstract interface com.sun.tools.ws.api.wsdl.TWSDLExtension
meth public abstract com.sun.tools.ws.api.wsdl.TWSDLExtensible getParent()

CLSS public abstract com.sun.tools.ws.api.wsdl.TWSDLExtensionHandler
cons public init()
meth public boolean doHandleExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleBindingExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleDefinitionsExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleFaultExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleInputExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleOperationExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleOutputExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handlePortExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handlePortTypeExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleServiceExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleTypesExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public java.lang.String getNamespaceURI()
supr java.lang.Object

CLSS public com.sun.tools.ws.processor.ProcessorException
cons public !varargs init(java.lang.String,java.lang.Object[])
cons public init(java.lang.String)
cons public init(java.lang.Throwable)
meth public java.lang.String getDefaultResourceBundleName()
supr com.sun.xml.ws.util.exception.JAXWSExceptionBase

CLSS public abstract com.sun.tools.ws.processor.model.AbstractType
cons protected init()
cons protected init(javax.xml.namespace.QName)
cons protected init(javax.xml.namespace.QName,com.sun.tools.ws.processor.model.java.JavaType)
cons protected init(javax.xml.namespace.QName,com.sun.tools.ws.processor.model.java.JavaType,java.lang.String)
cons protected init(javax.xml.namespace.QName,java.lang.String)
meth public boolean isLiteralType()
meth public boolean isNillable()
meth public boolean isSOAPType()
meth public com.sun.tools.ws.processor.model.java.JavaType getJavaType()
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.String getVersion()
meth public java.util.Iterator getProperties()
meth public java.util.Map getPropertiesMap()
meth public javax.xml.namespace.QName getName()
meth public void removeProperty(java.lang.String)
meth public void setJavaType(com.sun.tools.ws.processor.model.java.JavaType)
meth public void setName(javax.xml.namespace.QName)
meth public void setPropertiesMap(java.util.Map)
meth public void setProperty(java.lang.String,java.lang.Object)
meth public void setVersion(java.lang.String)
supr java.lang.Object
hfds javaType,name,properties,version

CLSS public com.sun.tools.ws.processor.model.AsyncOperation
cons public init(com.sun.tools.ws.processor.model.Operation,com.sun.tools.ws.wsdl.framework.Entity)
cons public init(com.sun.tools.ws.wsdl.framework.Entity)
cons public init(javax.xml.namespace.QName,com.sun.tools.ws.wsdl.framework.Entity)
meth public boolean isAsync()
meth public com.sun.tools.ws.processor.model.AbstractType getResponseBeanType()
meth public com.sun.tools.ws.processor.model.AsyncOperationType getAsyncType()
meth public com.sun.tools.ws.processor.model.Operation getNormalOperation()
meth public com.sun.tools.ws.processor.model.java.JavaType getCallBackType()
meth public com.sun.tools.ws.processor.model.java.JavaType getResponseBeanJavaType()
meth public java.lang.String getJavaMethodName()
meth public void setAsyncType(com.sun.tools.ws.processor.model.AsyncOperationType)
meth public void setNormalOperation(com.sun.tools.ws.processor.model.Operation)
meth public void setResponseBean(com.sun.tools.ws.processor.model.AbstractType)
supr com.sun.tools.ws.processor.model.Operation
hfds _async,_asyncOpType,_responseBean,operation

CLSS public final com.sun.tools.ws.processor.model.AsyncOperationType
fld public final static com.sun.tools.ws.processor.model.AsyncOperationType CALLBACK
fld public final static com.sun.tools.ws.processor.model.AsyncOperationType POLLING
supr java.lang.Object

CLSS public com.sun.tools.ws.processor.model.Block
cons public init(javax.xml.namespace.QName,com.sun.tools.ws.processor.model.AbstractType,com.sun.tools.ws.wsdl.framework.Entity)
fld public final static int ATTACHMENT = 3
fld public final static int BODY = 1
fld public final static int HEADER = 2
fld public final static int UNBOUND = 0
meth public com.sun.tools.ws.processor.model.AbstractType getType()
meth public int getLocation()
meth public javax.xml.namespace.QName getName()
meth public void accept(com.sun.tools.ws.processor.model.ModelVisitor) throws java.lang.Exception
meth public void setLocation(int)
meth public void setType(com.sun.tools.ws.processor.model.AbstractType)
supr com.sun.tools.ws.processor.model.ModelObject
hfds location,name,type

CLSS public com.sun.tools.ws.processor.model.ExtendedModelVisitor
cons public init()
meth protected boolean shouldVisit(com.sun.tools.ws.processor.model.Port)
meth protected void postVisit(com.sun.tools.ws.processor.model.Fault) throws java.lang.Exception
meth protected void postVisit(com.sun.tools.ws.processor.model.Model) throws java.lang.Exception
meth protected void postVisit(com.sun.tools.ws.processor.model.Operation) throws java.lang.Exception
meth protected void postVisit(com.sun.tools.ws.processor.model.Port) throws java.lang.Exception
meth protected void postVisit(com.sun.tools.ws.processor.model.Request) throws java.lang.Exception
meth protected void postVisit(com.sun.tools.ws.processor.model.Response) throws java.lang.Exception
meth protected void postVisit(com.sun.tools.ws.processor.model.Service) throws java.lang.Exception
meth protected void preVisit(com.sun.tools.ws.processor.model.Fault) throws java.lang.Exception
meth protected void preVisit(com.sun.tools.ws.processor.model.Model) throws java.lang.Exception
meth protected void preVisit(com.sun.tools.ws.processor.model.Operation) throws java.lang.Exception
meth protected void preVisit(com.sun.tools.ws.processor.model.Port) throws java.lang.Exception
meth protected void preVisit(com.sun.tools.ws.processor.model.Request) throws java.lang.Exception
meth protected void preVisit(com.sun.tools.ws.processor.model.Response) throws java.lang.Exception
meth protected void preVisit(com.sun.tools.ws.processor.model.Service) throws java.lang.Exception
meth protected void visit(com.sun.tools.ws.processor.model.Parameter) throws java.lang.Exception
meth protected void visitBodyBlock(com.sun.tools.ws.processor.model.Block) throws java.lang.Exception
meth protected void visitFaultBlock(com.sun.tools.ws.processor.model.Block) throws java.lang.Exception
meth protected void visitHeaderBlock(com.sun.tools.ws.processor.model.Block) throws java.lang.Exception
meth public void visit(com.sun.tools.ws.processor.model.Model) throws java.lang.Exception
supr java.lang.Object

CLSS public com.sun.tools.ws.processor.model.Fault
cons public init(com.sun.tools.ws.wsdl.framework.Entity)
cons public init(java.lang.String,com.sun.tools.ws.wsdl.framework.Entity)
meth public boolean isWsdlException()
meth public com.sun.codemodel.JClass getExceptionClass()
meth public com.sun.tools.ws.processor.model.Block getBlock()
meth public com.sun.tools.ws.processor.model.java.JavaException getJavaException()
meth public java.lang.String getJavaMemberName()
meth public java.lang.String getName()
meth public java.lang.String getWsdlFaultName()
meth public java.util.Iterator getAllFaults()
meth public java.util.Iterator getSubfaults()
meth public java.util.Set getAllFaultsSet()
meth public java.util.Set getSubfaultsSet()
meth public javax.xml.namespace.QName getElementName()
meth public void accept(com.sun.tools.ws.processor.model.ModelVisitor) throws java.lang.Exception
meth public void setBlock(com.sun.tools.ws.processor.model.Block)
meth public void setElementName(javax.xml.namespace.QName)
meth public void setExceptionClass(com.sun.codemodel.JClass)
meth public void setJavaException(com.sun.tools.ws.processor.model.java.JavaException)
meth public void setJavaMemberName(java.lang.String)
meth public void setName(java.lang.String)
meth public void setSubfaultsSet(java.util.Set)
meth public void setWsdlException(boolean)
meth public void setWsdlFaultName(java.lang.String)
supr com.sun.tools.ws.processor.model.ModelObject
hfds block,elementName,exceptionClass,javaException,javaMemberName,name,subfaults,wsdlException,wsdlFaultName

CLSS public com.sun.tools.ws.processor.model.HeaderFault
cons public init(com.sun.tools.ws.wsdl.framework.Entity)
cons public init(java.lang.String,com.sun.tools.ws.wsdl.framework.Entity)
meth public java.lang.String getPart()
meth public javax.xml.namespace.QName getMessage()
meth public void setMessage(javax.xml.namespace.QName)
meth public void setPart(java.lang.String)
supr com.sun.tools.ws.processor.model.Fault
hfds _message,_part

CLSS public abstract com.sun.tools.ws.processor.model.Message
cons protected init(com.sun.tools.ws.wsdl.document.Message,com.sun.tools.ws.wscompile.ErrorReceiver)
meth public boolean isBodyEmpty()
meth public boolean isBodyEncoded()
meth public com.sun.tools.ws.processor.model.Parameter getParameterByName(java.lang.String)
meth public int getAttachmentBlockCount()
meth public int getBodyBlockCount()
meth public int getHeaderBlockCount()
meth public int getUnboundBlocksCount()
meth public java.util.Collection<com.sun.tools.ws.processor.model.Block> getHeaderBlockCollection()
meth public java.util.Iterator<com.sun.tools.ws.processor.model.Block> getAttachmentBlocks()
meth public java.util.Iterator<com.sun.tools.ws.processor.model.Block> getBodyBlocks()
meth public java.util.Iterator<com.sun.tools.ws.processor.model.Block> getHeaderBlocks()
meth public java.util.Iterator<com.sun.tools.ws.processor.model.Block> getUnboundBlocks()
meth public java.util.Iterator<com.sun.tools.ws.processor.model.Parameter> getParameters()
meth public java.util.List<com.sun.tools.ws.processor.model.Parameter> getParametersList()
meth public java.util.Map<javax.xml.namespace.QName,com.sun.tools.ws.processor.model.Block> getAttachmentBlocksMap()
meth public java.util.Map<javax.xml.namespace.QName,com.sun.tools.ws.processor.model.Block> getBodyBlocksMap()
meth public java.util.Map<javax.xml.namespace.QName,com.sun.tools.ws.processor.model.Block> getHeaderBlocksMap()
meth public java.util.Map<javax.xml.namespace.QName,com.sun.tools.ws.processor.model.Block> getUnboundBlocksMap()
meth public java.util.Set<com.sun.tools.ws.processor.model.Block> getAllBlocks()
meth public void addAttachmentBlock(com.sun.tools.ws.processor.model.Block)
meth public void addBodyBlock(com.sun.tools.ws.processor.model.Block)
meth public void addHeaderBlock(com.sun.tools.ws.processor.model.Block)
meth public void addParameter(com.sun.tools.ws.processor.model.Parameter)
meth public void addUnboundBlock(com.sun.tools.ws.processor.model.Block)
meth public void setAttachmentBlocksMap(java.util.Map<javax.xml.namespace.QName,com.sun.tools.ws.processor.model.Block>)
meth public void setBodyBlocksMap(java.util.Map<javax.xml.namespace.QName,com.sun.tools.ws.processor.model.Block>)
meth public void setHeaderBlocksMap(java.util.Map<javax.xml.namespace.QName,com.sun.tools.ws.processor.model.Block>)
meth public void setParametersList(java.util.List<com.sun.tools.ws.processor.model.Parameter>)
meth public void setUnboundBlocksMap(java.util.Map<javax.xml.namespace.QName,com.sun.tools.ws.processor.model.Block>)
supr com.sun.tools.ws.processor.model.ModelObject
hfds _attachmentBlocks,_bodyBlocks,_headerBlocks,_parameters,_parametersByName,_unboundBlocks

CLSS public com.sun.tools.ws.processor.model.Model
cons public init(com.sun.tools.ws.wsdl.framework.Entity)
cons public init(javax.xml.namespace.QName,com.sun.tools.ws.wsdl.framework.Entity)
meth public com.sun.tools.ws.processor.model.Service getServiceByName(javax.xml.namespace.QName)
meth public com.sun.tools.ws.processor.model.jaxb.JAXBModel getJAXBModel()
meth public java.lang.String getSource()
meth public java.lang.String getTargetNamespaceURI()
meth public java.util.Iterator getExtraTypes()
meth public java.util.List<com.sun.tools.ws.processor.model.Service> getServices()
meth public java.util.Set<com.sun.tools.ws.processor.model.AbstractType> getExtraTypesSet()
meth public javax.xml.namespace.QName getName()
meth public void accept(com.sun.tools.ws.processor.model.ModelVisitor) throws java.lang.Exception
meth public void addExtraType(com.sun.tools.ws.processor.model.AbstractType)
meth public void addService(com.sun.tools.ws.processor.model.Service)
meth public void setExtraTypesSet(java.util.Set<com.sun.tools.ws.processor.model.AbstractType>)
meth public void setJAXBModel(com.sun.tools.ws.processor.model.jaxb.JAXBModel)
meth public void setName(javax.xml.namespace.QName)
meth public void setServices(java.util.List<com.sun.tools.ws.processor.model.Service>)
meth public void setSource(java.lang.String)
meth public void setTargetNamespaceURI(java.lang.String)
supr com.sun.tools.ws.processor.model.ModelObject
hfds extraTypes,jaxBModel,name,services,servicesByName,source,targetNamespace

CLSS public com.sun.tools.ws.processor.model.ModelException
cons public !varargs init(java.lang.String,java.lang.Object[])
cons public init(com.sun.istack.localization.Localizable)
cons public init(java.lang.Throwable)
meth public java.lang.String getDefaultResourceBundleName()
supr com.sun.tools.ws.processor.ProcessorException

CLSS public abstract com.sun.tools.ws.processor.model.ModelObject
cons protected init(com.sun.tools.ws.wsdl.framework.Entity)
fld protected com.sun.tools.ws.wscompile.ErrorReceiver errorReceiver
meth public abstract void accept(com.sun.tools.ws.processor.model.ModelVisitor) throws java.lang.Exception
meth public com.sun.tools.ws.wsdl.framework.Entity getEntity()
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.String getJavaDoc()
meth public java.util.Iterator getProperties()
meth public java.util.Map getPropertiesMap()
meth public org.xml.sax.Locator getLocator()
meth public void removeProperty(java.lang.String)
meth public void setErrorReceiver(com.sun.tools.ws.wscompile.ErrorReceiver)
meth public void setJavaDoc(java.lang.String)
meth public void setPropertiesMap(java.util.Map)
meth public void setProperty(java.lang.String,java.lang.Object)
supr java.lang.Object
hfds _properties,entity,javaDoc

CLSS public abstract interface com.sun.tools.ws.processor.model.ModelProperties
fld public final static java.lang.String PROPERTY_ANONYMOUS_ARRAY_JAVA_TYPE = "com.sun.xml.ws.processor.model.AnonymousArrayJavaType"
fld public final static java.lang.String PROPERTY_ANONYMOUS_ARRAY_TYPE_NAME = "com.sun.xml.ws.processor.model.AnonymousArrayTypeName"
fld public final static java.lang.String PROPERTY_ANONYMOUS_TYPE_NAME = "com.sun.xml.ws.processor.model.AnonymousTypeName"
fld public final static java.lang.String PROPERTY_CLIENT_CONTACTINFOLIST_CLASS_NAME = "com.sun.xml.ws.processor.model.ClientContactInfoListClassName"
fld public final static java.lang.String PROPERTY_CLIENT_ENCODER_DECODER_CLASS_NAME = "com.sun.xml.ws.processor.model.ClientEncoderClassName"
fld public final static java.lang.String PROPERTY_DELEGATE_CLASS_NAME = "com.sun.xml.ws.processor.model.DelegateClassName"
fld public final static java.lang.String PROPERTY_EPTFF_CLASS_NAME = "com.sun.xml.ws.processor.model.EPTFFClassName"
fld public final static java.lang.String PROPERTY_JAVA_PORT_NAME = "com.sun.xml.ws.processor.model.JavaPortName"
fld public final static java.lang.String PROPERTY_MODELER_NAME = "com.sun.xml.ws.processor.model.ModelerName"
fld public final static java.lang.String PROPERTY_PARAM_MESSAGE_PART_NAME = "com.sun.xml.ws.processor.model.ParamMessagePartName"
fld public final static java.lang.String PROPERTY_PTIE_CLASS_NAME = "com.sun.xml.ws.processor.model.PtieClassName"
fld public final static java.lang.String PROPERTY_SED_CLASS_NAME = "com.sun.xml.ws.processor.model.SEDClassName"
fld public final static java.lang.String PROPERTY_STUB_CLASS_NAME = "com.sun.xml.ws.processor.model.StubClassName"
fld public final static java.lang.String PROPERTY_STUB_OLD_CLASS_NAME = "com.sun.xml.ws.processor.model.StubOldClassName"
fld public final static java.lang.String PROPERTY_TIE_CLASS_NAME = "com.sun.xml.ws.processor.model.TieClassName"
fld public final static java.lang.String PROPERTY_WSDL_BINDING_NAME = "com.sun.xml.ws.processor.model.WSDLBindingName"
fld public final static java.lang.String PROPERTY_WSDL_MESSAGE_NAME = "com.sun.xml.ws.processor.model.WSDLMessageName"
fld public final static java.lang.String PROPERTY_WSDL_PORT_NAME = "com.sun.xml.ws.processor.model.WSDLPortName"
fld public final static java.lang.String PROPERTY_WSDL_PORT_TYPE_NAME = "com.sun.xml.ws.processor.model.WSDLPortTypeName"
fld public final static java.lang.String WSDL_MODELER_NAME = "com.sun.xml.ws.processor.modeler.wsdl.WSDLModeler"

CLSS public abstract interface com.sun.tools.ws.processor.model.ModelVisitor
meth public abstract void visit(com.sun.tools.ws.processor.model.Block) throws java.lang.Exception
meth public abstract void visit(com.sun.tools.ws.processor.model.Fault) throws java.lang.Exception
meth public abstract void visit(com.sun.tools.ws.processor.model.Model) throws java.lang.Exception
meth public abstract void visit(com.sun.tools.ws.processor.model.Operation) throws java.lang.Exception
meth public abstract void visit(com.sun.tools.ws.processor.model.Parameter) throws java.lang.Exception
meth public abstract void visit(com.sun.tools.ws.processor.model.Port) throws java.lang.Exception
meth public abstract void visit(com.sun.tools.ws.processor.model.Request) throws java.lang.Exception
meth public abstract void visit(com.sun.tools.ws.processor.model.Response) throws java.lang.Exception
meth public abstract void visit(com.sun.tools.ws.processor.model.Service) throws java.lang.Exception

CLSS public com.sun.tools.ws.processor.model.Operation
cons public init(com.sun.tools.ws.processor.model.Operation,com.sun.tools.ws.wsdl.framework.Entity)
cons public init(com.sun.tools.ws.wsdl.framework.Entity)
cons public init(javax.xml.namespace.QName,com.sun.tools.ws.wsdl.framework.Entity)
meth public boolean isOverloaded()
meth public boolean isWrapped()
meth public com.sun.tools.ws.processor.model.Request getRequest()
meth public com.sun.tools.ws.processor.model.Response getResponse()
meth public com.sun.tools.ws.processor.model.java.JavaMethod getJavaMethod()
meth public com.sun.tools.ws.wsdl.document.Operation getWSDLPortTypeOperation()
meth public com.sun.tools.ws.wsdl.document.soap.SOAPStyle getStyle()
meth public com.sun.tools.ws.wsdl.document.soap.SOAPUse getUse()
meth public int getFaultCount()
meth public java.lang.String getCustomizedName()
meth public java.lang.String getJavaMethodName()
meth public java.lang.String getSOAPAction()
meth public java.lang.String getUniqueName()
meth public java.util.Iterator<com.sun.tools.ws.processor.model.Fault> getAllFaults()
meth public java.util.Iterator<com.sun.tools.ws.processor.model.Fault> getFaults()
meth public java.util.Set<com.sun.tools.ws.processor.model.Block> getAllFaultBlocks()
meth public java.util.Set<com.sun.tools.ws.processor.model.Fault> getAllFaultsSet()
meth public java.util.Set<com.sun.tools.ws.processor.model.Fault> getFaultsSet()
meth public javax.xml.namespace.QName getName()
meth public void accept(com.sun.tools.ws.processor.model.ModelVisitor) throws java.lang.Exception
meth public void addFault(com.sun.tools.ws.processor.model.Fault)
meth public void setCustomizedName(java.lang.String)
meth public void setFaultsSet(java.util.Set<com.sun.tools.ws.processor.model.Fault>)
meth public void setJavaMethod(com.sun.tools.ws.processor.model.java.JavaMethod)
meth public void setName(javax.xml.namespace.QName)
meth public void setRequest(com.sun.tools.ws.processor.model.Request)
meth public void setResponse(com.sun.tools.ws.processor.model.Response)
meth public void setSOAPAction(java.lang.String)
meth public void setStyle(com.sun.tools.ws.wsdl.document.soap.SOAPStyle)
meth public void setUniqueName(java.lang.String)
meth public void setUse(com.sun.tools.ws.wsdl.document.soap.SOAPUse)
meth public void setWSDLPortTypeOperation(com.sun.tools.ws.wsdl.document.Operation)
meth public void setWrapped(boolean)
supr com.sun.tools.ws.processor.model.ModelObject
hfds _faultNames,_faults,_isWrapped,_javaMethod,_name,_request,_response,_soapAction,_style,_uniqueName,_use,customizedName,wsdlOperation

CLSS public com.sun.tools.ws.processor.model.Parameter
cons public init(java.lang.String,com.sun.tools.ws.wsdl.framework.Entity)
meth public boolean isEmbedded()
meth public boolean isIN()
meth public boolean isINOUT()
meth public boolean isOUT()
meth public boolean isReturn()
meth public com.sun.tools.ws.processor.model.AbstractType getType()
meth public com.sun.tools.ws.processor.model.Block getBlock()
meth public com.sun.tools.ws.processor.model.Parameter getLinkedParameter()
meth public com.sun.tools.ws.processor.model.java.JavaParameter getJavaParameter()
meth public int getParameterIndex()
meth public java.lang.String getCustomName()
meth public java.lang.String getEntityName()
meth public java.lang.String getName()
meth public java.lang.String getTypeName()
meth public java.util.List<java.lang.String> getAnnotations()
meth public void accept(com.sun.tools.ws.processor.model.ModelVisitor) throws java.lang.Exception
meth public void setAnnotations(java.util.List<java.lang.String>)
meth public void setBlock(com.sun.tools.ws.processor.model.Block)
meth public void setCustomName(java.lang.String)
meth public void setEmbedded(boolean)
meth public void setJavaParameter(com.sun.tools.ws.processor.model.java.JavaParameter)
meth public void setLinkedParameter(com.sun.tools.ws.processor.model.Parameter)
meth public void setMode(javax.jws.WebParam$Mode)
meth public void setName(java.lang.String)
meth public void setParameterIndex(int)
meth public void setType(com.sun.tools.ws.processor.model.AbstractType)
meth public void setTypeName(java.lang.String)
supr com.sun.tools.ws.processor.model.ModelObject
hfds annotations,block,customName,embedded,entityName,javaParameter,link,mode,name,parameterOrderPosition,type,typeName

CLSS public com.sun.tools.ws.processor.model.Port
cons public init(com.sun.tools.ws.wsdl.framework.Entity)
cons public init(javax.xml.namespace.QName,com.sun.tools.ws.wsdl.framework.Entity)
fld public java.util.Map<javax.xml.namespace.QName,com.sun.tools.ws.wsdl.document.PortType> portTypes
meth public boolean isProvider()
meth public boolean isWrapped()
meth public com.sun.tools.ws.processor.model.Operation getOperationByUniqueName(java.lang.String)
meth public com.sun.tools.ws.processor.model.java.JavaInterface getJavaInterface()
meth public com.sun.tools.ws.wsdl.document.soap.SOAPStyle getStyle()
meth public java.lang.String getAddress()
meth public java.lang.String getPortGetter()
meth public java.lang.String getServiceImplName()
meth public java.util.List<com.sun.tools.ws.processor.model.Operation> getOperations()
meth public javax.xml.namespace.QName getName()
meth public void accept(com.sun.tools.ws.processor.model.ModelVisitor) throws java.lang.Exception
meth public void addOperation(com.sun.tools.ws.processor.model.Operation)
meth public void setAddress(java.lang.String)
meth public void setJavaInterface(com.sun.tools.ws.processor.model.java.JavaInterface)
meth public void setName(javax.xml.namespace.QName)
meth public void setOperations(java.util.List<com.sun.tools.ws.processor.model.Operation>)
meth public void setPortGetter(java.lang.String)
meth public void setServiceImplName(java.lang.String)
meth public void setStyle(com.sun.tools.ws.wsdl.document.soap.SOAPStyle)
meth public void setWrapped(boolean)
supr com.sun.tools.ws.processor.model.ModelObject
hfds _address,_isWrapped,_javaInterface,_name,_operations,_serviceImplName,_style,operationsByName,portGetter

CLSS public com.sun.tools.ws.processor.model.Request
cons public init(com.sun.tools.ws.wsdl.document.Message,com.sun.tools.ws.wscompile.ErrorReceiver)
meth public void accept(com.sun.tools.ws.processor.model.ModelVisitor) throws java.lang.Exception
supr com.sun.tools.ws.processor.model.Message

CLSS public com.sun.tools.ws.processor.model.Response
cons public init(com.sun.tools.ws.wsdl.document.Message,com.sun.tools.ws.wscompile.ErrorReceiver)
meth public int getFaultBlockCount()
meth public java.util.Iterator getFaultBlocks()
meth public java.util.Map getFaultBlocksMap()
meth public void accept(com.sun.tools.ws.processor.model.ModelVisitor) throws java.lang.Exception
meth public void addFaultBlock(com.sun.tools.ws.processor.model.Block)
meth public void setFaultBlocksMap(java.util.Map)
supr com.sun.tools.ws.processor.model.Message
hfds _faultBlocks

CLSS public com.sun.tools.ws.processor.model.Service
cons public init(com.sun.tools.ws.wsdl.framework.Entity)
cons public init(javax.xml.namespace.QName,com.sun.tools.ws.processor.model.java.JavaInterface,com.sun.tools.ws.wsdl.framework.Entity)
meth public com.sun.tools.ws.processor.model.Port getPortByName(javax.xml.namespace.QName)
meth public com.sun.tools.ws.processor.model.java.JavaInterface getJavaInterface()
meth public com.sun.tools.ws.processor.model.java.JavaInterface getJavaIntf()
meth public java.util.List<com.sun.tools.ws.processor.model.Port> getPorts()
meth public javax.xml.namespace.QName getName()
meth public void accept(com.sun.tools.ws.processor.model.ModelVisitor) throws java.lang.Exception
meth public void addPort(com.sun.tools.ws.processor.model.Port)
meth public void setJavaInterface(com.sun.tools.ws.processor.model.java.JavaInterface)
meth public void setName(javax.xml.namespace.QName)
meth public void setPorts(java.util.List<com.sun.tools.ws.processor.model.Port>)
supr com.sun.tools.ws.processor.model.ModelObject
hfds javaInterface,name,ports,portsByName

CLSS public com.sun.tools.ws.processor.model.java.JavaArrayType
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String,com.sun.tools.ws.processor.model.java.JavaType)
meth public com.sun.tools.ws.processor.model.java.JavaType getElementType()
meth public java.lang.String getElementName()
meth public java.lang.String getSOAPArrayHolderName()
meth public void setElementName(java.lang.String)
meth public void setElementType(com.sun.tools.ws.processor.model.java.JavaType)
meth public void setSOAPArrayHolderName(java.lang.String)
supr com.sun.tools.ws.processor.model.java.JavaType
hfds elementName,elementType,soapArrayHolderName

CLSS public com.sun.tools.ws.processor.model.java.JavaException
cons public init()
cons public init(java.lang.String,boolean,java.lang.Object)
supr com.sun.tools.ws.processor.model.java.JavaStructureType

CLSS public com.sun.tools.ws.processor.model.java.JavaInterface
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
meth public boolean hasInterface(java.lang.String)
meth public boolean hasMethod(com.sun.tools.ws.processor.model.java.JavaMethod)
meth public java.lang.String getFormalName()
meth public java.lang.String getImpl()
meth public java.lang.String getJavaDoc()
meth public java.lang.String getName()
meth public java.lang.String getRealName()
meth public java.lang.String getSimpleName()
meth public java.util.Iterator getInterfaces()
meth public java.util.Iterator getMethods()
meth public java.util.List getInterfacesList()
meth public java.util.List getMethodsList()
meth public void addInterface(java.lang.String)
meth public void addMethod(com.sun.tools.ws.processor.model.java.JavaMethod)
meth public void setFormalName(java.lang.String)
meth public void setImpl(java.lang.String)
meth public void setInterfacesList(java.util.List)
meth public void setJavaDoc(java.lang.String)
meth public void setMethodsList(java.util.List)
meth public void setRealName(java.lang.String)
supr java.lang.Object
hfds impl,interfaces,javadoc,methods,name,realName

CLSS public com.sun.tools.ws.processor.model.java.JavaMethod
cons public init(java.lang.String,com.sun.tools.ws.wscompile.WsimportOptions,com.sun.tools.ws.wscompile.ErrorReceiver)
meth public com.sun.tools.ws.processor.model.java.JavaType getReturnType()
meth public java.lang.String getName()
meth public java.util.Iterator<java.lang.String> getExceptions()
meth public java.util.List<com.sun.tools.ws.processor.model.java.JavaParameter> getParametersList()
meth public void addException(java.lang.String)
meth public void addParameter(com.sun.tools.ws.processor.model.java.JavaParameter)
meth public void setReturnType(com.sun.tools.ws.processor.model.java.JavaType)
supr java.lang.Object
hfds errorReceiver,exceptions,name,options,parameters,returnType

CLSS public com.sun.tools.ws.processor.model.java.JavaParameter
cons public init()
cons public init(java.lang.String,com.sun.tools.ws.processor.model.java.JavaType,com.sun.tools.ws.processor.model.Parameter)
cons public init(java.lang.String,com.sun.tools.ws.processor.model.java.JavaType,com.sun.tools.ws.processor.model.Parameter,boolean)
meth public boolean isHolder()
meth public com.sun.tools.ws.processor.model.Parameter getParameter()
meth public com.sun.tools.ws.processor.model.java.JavaType getType()
meth public java.lang.String getHolderName()
meth public java.lang.String getName()
meth public void setHolder(boolean)
meth public void setHolderName(java.lang.String)
meth public void setName(java.lang.String)
meth public void setParameter(com.sun.tools.ws.processor.model.Parameter)
meth public void setType(com.sun.tools.ws.processor.model.java.JavaType)
supr java.lang.Object
hfds holder,holderName,name,parameter,type

CLSS public com.sun.tools.ws.processor.model.java.JavaSimpleType
cons public init()
cons public init(com.sun.tools.ws.processor.model.jaxb.JAXBTypeAndAnnotation)
cons public init(java.lang.String,java.lang.String)
supr com.sun.tools.ws.processor.model.java.JavaType

CLSS public com.sun.tools.ws.processor.model.java.JavaStructureMember
cons public init()
cons public init(java.lang.String,com.sun.tools.ws.processor.model.java.JavaType,java.lang.Object)
cons public init(java.lang.String,com.sun.tools.ws.processor.model.java.JavaType,java.lang.Object,boolean)
meth public boolean isInherited()
meth public boolean isPublic()
meth public com.sun.tools.ws.processor.model.java.JavaType getType()
meth public int getConstructorPos()
meth public java.lang.Object getOwner()
meth public java.lang.String getDeclaringClass()
meth public java.lang.String getName()
meth public java.lang.String getReadMethod()
meth public java.lang.String getWriteMethod()
meth public void setConstructorPos(int)
meth public void setDeclaringClass(java.lang.String)
meth public void setInherited(boolean)
meth public void setName(java.lang.String)
meth public void setOwner(java.lang.Object)
meth public void setPublic(boolean)
meth public void setReadMethod(java.lang.String)
meth public void setType(com.sun.tools.ws.processor.model.java.JavaType)
meth public void setWriteMethod(java.lang.String)
supr java.lang.Object
hfds constructorPos,declaringClass,isInherited,isPublic,name,owner,readMethod,type,writeMethod

CLSS public com.sun.tools.ws.processor.model.java.JavaStructureType
cons public init()
cons public init(java.lang.String,boolean,java.lang.Object)
meth public boolean isAbstract()
meth public com.sun.tools.ws.processor.model.java.JavaStructureMember getMemberByName(java.lang.String)
meth public com.sun.tools.ws.processor.model.java.JavaStructureType getSuperclass()
meth public int getMembersCount()
meth public java.lang.Object getOwner()
meth public java.util.Iterator getAllSubclasses()
meth public java.util.Iterator getMembers()
meth public java.util.Iterator getSubclasses()
meth public java.util.List<com.sun.tools.ws.processor.model.java.JavaStructureMember> getMembersList()
meth public java.util.Set getAllSubclassesSet()
meth public java.util.Set getSubclassesSet()
meth public void add(com.sun.tools.ws.processor.model.java.JavaStructureMember)
meth public void addSubclass(com.sun.tools.ws.processor.model.java.JavaStructureType)
meth public void setAbstract(boolean)
meth public void setMembersList(java.util.List<com.sun.tools.ws.processor.model.java.JavaStructureMember>)
meth public void setOwner(java.lang.Object)
meth public void setSubclassesSet(java.util.Set)
meth public void setSuperclass(com.sun.tools.ws.processor.model.java.JavaStructureType)
supr com.sun.tools.ws.processor.model.java.JavaType
hfds isAbstract,members,membersByName,owner,subclasses,superclass

CLSS public abstract com.sun.tools.ws.processor.model.java.JavaType
cons public init()
cons public init(com.sun.tools.ws.processor.model.jaxb.JAXBTypeAndAnnotation)
cons public init(java.lang.String,boolean,java.lang.String)
cons public init(java.lang.String,boolean,java.lang.String,java.lang.String)
meth public boolean isHolder()
meth public boolean isHolderPresent()
meth public boolean isPresent()
meth public com.sun.tools.ws.processor.model.jaxb.JAXBTypeAndAnnotation getType()
meth public java.lang.String getFormalName()
meth public java.lang.String getHolderName()
meth public java.lang.String getInitString()
meth public java.lang.String getName()
meth public java.lang.String getRealName()
meth public void doSetName(java.lang.String)
meth public void setFormalName(java.lang.String)
meth public void setHolder(boolean)
meth public void setHolderName(java.lang.String)
meth public void setHolderPresent(boolean)
meth public void setInitString(java.lang.String)
meth public void setPresent(boolean)
meth public void setRealName(java.lang.String)
supr java.lang.Object
hfds holder,holderName,holderPresent,initString,name,present,realName,type

CLSS public final com.sun.tools.ws.processor.modeler.JavaSimpleTypeCreator
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType BIG_INTEGER_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType BOOLEAN_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType BOXED_BOOLEAN_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType BOXED_BYTE_ARRAY_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType BOXED_BYTE_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType BOXED_DOUBLE_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType BOXED_FLOAT_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType BOXED_INTEGER_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType BOXED_LONG_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType BOXED_SHORT_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType BYTE_ARRAY_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType BYTE_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType CALENDAR_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType DATA_HANDLER_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType DATE_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType DECIMAL_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType DOUBLE_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType FLOAT_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType IMAGE_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType INT_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType LONG_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType MIME_MULTIPART_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType OBJECT_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType QNAME_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType SHORT_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType SOAPELEMENT_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType SOURCE_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType STRING_ARRAY_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType STRING_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType URI_JAVATYPE
fld public final static com.sun.tools.ws.processor.model.java.JavaSimpleType VOID_JAVATYPE
meth public static com.sun.tools.ws.processor.model.java.JavaSimpleType getJavaSimpleType(java.lang.String)
supr java.lang.Object
hfds JAVA_TYPES

CLSS public abstract interface com.sun.tools.ws.processor.modeler.Modeler
meth public abstract com.sun.tools.ws.processor.model.Model buildModel()

CLSS public final !enum com.sun.tools.ws.processor.modeler.ModelerConstants
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants ARRAY_LIST_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants ARRAY_STR
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants BIGDECIMAL_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants BIGINTEGER_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants BOOLEAN_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants BOXED_BOOLEAN_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants BOXED_BYTE_ARRAY_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants BOXED_BYTE_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants BOXED_CHAR_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants BOXED_DOUBLE_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants BOXED_FLOAT_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants BOXED_INTEGER_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants BOXED_LONG_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants BOXED_SHORT_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants BYTE_ARRAY_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants BYTE_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants CALENDAR_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants CHAR_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants CLASS_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants COLLECTION_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants DATA_HANDLER_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants DATE_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants DOUBLE_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants FALSE_STR
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants FLOAT_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants HASHTABLE_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants HASH_MAP_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants HASH_SET_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants IMAGE_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants INT_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants IOEXCEPTION_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants JAX_WS_MAP_ENTRY_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants LINKED_LIST_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants LIST_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants LONG_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants MAP_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants MIME_MULTIPART_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants NULL_STR
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants OBJECT_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants PROPERTIES_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants QNAME_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants SET_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants SHORT_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants SOAPELEMENT_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants SOURCE_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants STACK_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants STRING_ARRAY_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants STRING_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants TREE_MAP_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants TREE_SET_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants URI_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants VECTOR_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants VOID_CLASSNAME
fld public final static com.sun.tools.ws.processor.modeler.ModelerConstants ZERO_STR
meth public java.lang.String getValue()
meth public static com.sun.tools.ws.processor.modeler.ModelerConstants valueOf(java.lang.String)
meth public static com.sun.tools.ws.processor.modeler.ModelerConstants[] values()
supr java.lang.Enum<com.sun.tools.ws.processor.modeler.ModelerConstants>
hfds value

CLSS public com.sun.tools.ws.processor.modeler.ModelerException
cons public !varargs init(java.lang.String,java.lang.Object[])
cons public init(com.sun.istack.localization.Localizable)
cons public init(java.lang.String)
cons public init(java.lang.Throwable)
meth public java.lang.String getDefaultResourceBundleName()
supr com.sun.tools.ws.processor.ProcessorException

CLSS public com.sun.tools.ws.processor.modeler.wsdl.ClassNameAllocatorImpl
cons public init(com.sun.tools.ws.processor.util.ClassNameCollector)
intf com.sun.tools.xjc.api.ClassNameAllocator
meth public java.lang.String assignClassName(java.lang.String,java.lang.String)
meth public java.util.Set<java.lang.String> getJaxbGeneratedClasses()
supr java.lang.Object
hfds TYPE_SUFFIX,classNameCollector,jaxbClasses

CLSS public com.sun.tools.ws.processor.modeler.wsdl.ConsoleErrorReporter
cons public init(java.io.OutputStream)
cons public init(java.io.PrintStream)
meth public boolean hasError()
meth public void debug(org.xml.sax.SAXParseException)
meth public void enableDebugging()
meth public void error(org.xml.sax.SAXParseException)
meth public void fatalError(org.xml.sax.SAXParseException)
meth public void info(org.xml.sax.SAXParseException)
meth public void warning(org.xml.sax.SAXParseException)
supr com.sun.tools.ws.wscompile.ErrorReceiver
hfds debug,hasError,output

CLSS public com.sun.tools.ws.processor.modeler.wsdl.JAXBModelBuilder
cons public init(com.sun.tools.ws.wscompile.WsimportOptions,com.sun.tools.ws.processor.util.ClassNameCollector,com.sun.tools.ws.wsdl.parser.MetadataFinder,com.sun.tools.ws.wscompile.ErrorReceiver)
fld protected final static org.xml.sax.helpers.LocatorImpl NULL_LOCATOR
meth protected com.sun.tools.xjc.api.SchemaCompiler getJAXBSchemaCompiler()
meth protected void bind()
meth public com.sun.tools.ws.processor.model.jaxb.JAXBModel getJAXBModel()
meth public com.sun.tools.ws.processor.model.jaxb.JAXBType getJAXBType(javax.xml.namespace.QName)
meth public com.sun.tools.xjc.api.TypeAndAnnotation getElementTypeAndAnn(javax.xml.namespace.QName)
supr java.lang.Object
hfds _classNameAllocator,errReceiver,forest,jaxbModel,options,schemaCompiler

CLSS public com.sun.tools.ws.processor.modeler.wsdl.PseudoSchemaBuilder
meth public static java.util.List<org.xml.sax.InputSource> build(com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler,com.sun.tools.ws.wscompile.WsimportOptions,com.sun.tools.ws.wscompile.ErrorReceiver)
supr java.lang.Object
hfds asyncRespBeanBinding,bindingNameToPortMap,buf,memberSubmissionEPR,options,schemas,sysId,w3ceprSchemaBinding,wsdlDocument,wsdlModeler

CLSS public com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler
cons public init(com.sun.tools.ws.wscompile.WsimportOptions,com.sun.tools.ws.wscompile.ErrorReceiver,com.sun.tools.ws.wsdl.parser.MetadataFinder)
innr protected final static !enum StyleAndUse
meth protected boolean createJavaExceptionFromLiteralType(com.sun.tools.ws.processor.model.Fault,com.sun.tools.ws.processor.model.Port,java.lang.String)
meth protected boolean isAsync(com.sun.tools.ws.wsdl.document.PortType,com.sun.tools.ws.wsdl.document.Operation)
meth protected boolean isConflictingExceptionClassName(java.lang.String)
meth protected boolean isConflictingPortClassName(java.lang.String)
meth protected boolean isConflictingServiceClassName(java.lang.String)
meth protected boolean isRequestResponse()
meth protected boolean isSingleInOutPart(java.util.Set,com.sun.tools.ws.wsdl.document.MessagePart)
meth protected boolean isUnwrappable()
meth protected boolean processPort(com.sun.tools.ws.wsdl.document.Port,com.sun.tools.ws.processor.model.Service,com.sun.tools.ws.wsdl.document.WSDLDocument)
meth protected boolean setMessagePartsBinding(com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler$StyleAndUse)
meth protected boolean setMessagePartsBinding(com.sun.tools.ws.wsdl.document.soap.SOAPBody,com.sun.tools.ws.wsdl.document.Message,com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler$StyleAndUse,boolean)
meth protected boolean validateWSDLBindingStyle(com.sun.tools.ws.wsdl.document.Binding)
meth protected com.sun.tools.ws.processor.model.Operation processLiteralSOAPOperation(com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler$StyleAndUse)
meth protected com.sun.tools.ws.processor.model.Operation processSOAPOperation()
meth protected com.sun.tools.ws.processor.modeler.wsdl.JAXBModelBuilder getJAXBModelBuilder()
meth protected java.lang.String getAsyncOperationName(com.sun.tools.ws.processor.model.Operation)
meth protected java.lang.String getClassName(com.sun.tools.ws.processor.model.Port,java.lang.String)
meth protected java.lang.String getJavaNameForOperation(com.sun.tools.ws.processor.model.Operation)
meth protected java.lang.String getJavaNameOfSEI(com.sun.tools.ws.processor.model.Port)
meth protected java.lang.String getJavaPackage()
meth protected java.lang.String getServiceInterfaceName(javax.xml.namespace.QName,com.sun.tools.ws.wsdl.document.Service)
meth protected java.util.List<com.sun.tools.ws.wsdl.document.MessagePart> getParameterOrder()
meth protected java.util.List<java.lang.String> getAsynParameterOrder()
meth protected static void setDocumentationIfPresent(com.sun.tools.ws.processor.model.ModelObject,com.sun.tools.ws.wsdl.document.Documentation)
meth protected void buildJAXBModel(com.sun.tools.ws.wsdl.document.WSDLDocument)
meth protected void createJavaInterfaceForPort(com.sun.tools.ws.processor.model.Port,boolean)
meth protected void createJavaInterfaceForProviderPort(com.sun.tools.ws.processor.model.Port)
meth protected void createJavaMethodForOperation(com.sun.tools.ws.processor.model.Port,com.sun.tools.ws.processor.model.Operation,com.sun.tools.ws.processor.model.java.JavaInterface)
meth protected void handleLiteralSOAPFault(com.sun.tools.ws.processor.model.Response,java.util.Set)
meth protected void handleLiteralSOAPHeaders(com.sun.tools.ws.processor.model.Request,com.sun.tools.ws.processor.model.Response,java.util.Iterator,java.util.Set,java.util.List<java.lang.String>,boolean)
 anno 5 com.sun.istack.NotNull()
meth protected void processService(com.sun.tools.ws.wsdl.document.Service,com.sun.tools.ws.processor.model.Model,com.sun.tools.ws.wsdl.document.WSDLDocument)
meth public com.sun.tools.ws.processor.model.Model buildModel()
supr com.sun.tools.ws.processor.modeler.wsdl.WSDLModelerBase
hfds VOID_BODYBLOCK,classNameCollector,explicitDefaultPackage,jaxbModelBuilder,uniqueBodyBlocks

CLSS protected final static !enum com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler$StyleAndUse
 outer com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler
fld public final static com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler$StyleAndUse DOC_LITERAL
fld public final static com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler$StyleAndUse RPC_LITERAL
meth public static com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler$StyleAndUse valueOf(java.lang.String)
meth public static com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler$StyleAndUse[] values()
supr java.lang.Enum<com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler$StyleAndUse>

CLSS public abstract com.sun.tools.ws.processor.modeler.wsdl.WSDLModelerBase
cons public init(com.sun.tools.ws.wscompile.WsimportOptions,com.sun.tools.ws.wscompile.ErrorReceiver,com.sun.tools.ws.wsdl.parser.MetadataFinder)
fld protected com.sun.tools.ws.processor.modeler.wsdl.WSDLModelerBase$ProcessSOAPOperationInfo info
fld protected com.sun.tools.ws.wsdl.document.WSDLDocument document
fld protected com.sun.tools.ws.wsdl.parser.MetadataFinder forest
fld protected com.sun.tools.ws.wsdl.parser.WSDLParser parser
fld protected final com.sun.tools.ws.wscompile.ErrorReceiverFilter errReceiver
fld protected final com.sun.tools.ws.wscompile.WsimportOptions options
fld protected final static java.lang.String OPERATION_HAS_VOID_RETURN_TYPE = "com.sun.xml.ws.processor.modeler.wsdl.operationHasVoidReturnType"
fld protected final static java.lang.String WSDL_PARAMETER_ORDER = "com.sun.xml.ws.processor.modeler.wsdl.parameterOrder"
fld protected final static org.xml.sax.helpers.LocatorImpl NULL_LOCATOR
fld protected java.util.Map _faultTypeToStructureMap
fld protected java.util.Map<java.lang.String,com.sun.tools.ws.processor.model.java.JavaException> _javaExceptions
fld protected java.util.Map<javax.xml.namespace.QName,com.sun.tools.ws.processor.model.Port> _bindingNameToPortMap
fld public final static java.lang.String MESSAGE_HAS_MIME_MULTIPART_RELATED_BINDING = "com.sun.xml.ws.processor.modeler.wsdl.mimeMultipartRelatedBinding"
fld public final static java.lang.String WSDL_RESULT_PARAMETER = "com.sun.xml.ws.processor.modeler.wsdl.resultParameter"
innr public static ProcessSOAPOperationInfo
intf com.sun.tools.ws.processor.modeler.Modeler
meth protected boolean isConflictingClassName(java.lang.String)
meth protected boolean isConflictingExceptionClassName(java.lang.String)
meth protected boolean isConflictingPortClassName(java.lang.String)
meth protected boolean isConflictingServiceClassName(java.lang.String)
meth protected boolean isConflictingStubClassName(java.lang.String)
meth protected boolean isConflictingTieClassName(java.lang.String)
meth protected boolean isProvider(com.sun.tools.ws.wsdl.document.Port)
meth protected boolean isRequestMimeMultipart()
meth protected boolean isResponseMimeMultipart()
meth protected boolean isStyleAndPartMatch(com.sun.tools.ws.wsdl.document.soap.SOAPOperation,com.sun.tools.ws.wsdl.document.MessagePart)
meth protected boolean validateBodyParts(com.sun.tools.ws.wsdl.document.BindingOperation)
meth protected boolean validateMimeParts(java.lang.Iterable<com.sun.tools.ws.wsdl.document.mime.MIMEPart>)
meth protected com.sun.tools.ws.api.wsdl.TWSDLExtension getAnyExtensionOfType(com.sun.tools.ws.api.wsdl.TWSDLExtensible,java.lang.Class)
meth protected com.sun.tools.ws.wsdl.document.Message getInputMessage()
meth protected com.sun.tools.ws.wsdl.document.Message getOutputMessage()
meth protected com.sun.tools.ws.wsdl.document.soap.SOAPBody getSOAPRequestBody()
meth protected com.sun.tools.ws.wsdl.document.soap.SOAPBody getSOAPResponseBody()
meth protected java.lang.Iterable<com.sun.tools.ws.wsdl.document.mime.MIMEPart> getMimeParts(com.sun.tools.ws.api.wsdl.TWSDLExtensible)
meth protected java.lang.String getLiteralJavaMemberName(com.sun.tools.ws.processor.model.Fault)
meth protected java.lang.String getRequestNamespaceURI(com.sun.tools.ws.wsdl.document.soap.SOAPBody)
meth protected java.lang.String getResponseNamespaceURI(com.sun.tools.ws.wsdl.document.soap.SOAPBody)
meth protected java.lang.String getUniqueClassName(java.lang.String)
meth protected java.lang.String getUniqueName(com.sun.tools.ws.wsdl.document.Operation,boolean)
meth protected java.lang.String makePackageQualified(java.lang.String)
meth protected java.util.List<com.sun.tools.ws.wsdl.document.MessagePart> getMessageParts(com.sun.tools.ws.wsdl.document.soap.SOAPBody,com.sun.tools.ws.wsdl.document.Message,boolean)
meth protected java.util.List<com.sun.tools.ws.wsdl.document.MessagePart> getMimeContentParts(com.sun.tools.ws.wsdl.document.Message,com.sun.tools.ws.api.wsdl.TWSDLExtensible)
meth protected java.util.List<com.sun.tools.ws.wsdl.document.mime.MIMEContent> getMimeContents(com.sun.tools.ws.api.wsdl.TWSDLExtensible,com.sun.tools.ws.wsdl.document.Message,java.lang.String)
meth protected java.util.List<com.sun.tools.ws.wsdl.document.mime.MIMEContent> getMimeContents(com.sun.tools.ws.wsdl.document.mime.MIMEPart)
meth protected java.util.List<com.sun.tools.ws.wsdl.document.soap.SOAPHeader> getHeaderExtensions(com.sun.tools.ws.api.wsdl.TWSDLExtensible)
meth protected java.util.List<java.lang.String> getAlternateMimeTypes(java.util.List<com.sun.tools.ws.wsdl.document.mime.MIMEContent>)
meth protected java.util.Set getDuplicateFaultNames()
meth protected static boolean tokenListContains(java.lang.String,java.lang.String)
meth protected static com.sun.tools.ws.api.wsdl.TWSDLExtension getExtensionOfType(com.sun.tools.ws.api.wsdl.TWSDLExtensible,java.lang.Class)
meth protected static com.sun.tools.ws.wsdl.document.Message findMessage(javax.xml.namespace.QName,com.sun.tools.ws.wsdl.document.WSDLDocument)
meth protected static javax.xml.namespace.QName getQNameOf(com.sun.tools.ws.wsdl.framework.GloballyKnown)
meth protected void applyPortMethodCustomization(com.sun.tools.ws.processor.model.Port,com.sun.tools.ws.wsdl.document.Port)
meth protected void error(com.sun.tools.ws.wsdl.framework.Entity,java.lang.String)
meth protected void warning(com.sun.tools.ws.wsdl.framework.Entity,java.lang.String)
supr java.lang.Object
hfds _conflictingClassNames,numPasses,reqResNames

CLSS public static com.sun.tools.ws.processor.modeler.wsdl.WSDLModelerBase$ProcessSOAPOperationInfo
 outer com.sun.tools.ws.processor.modeler.wsdl.WSDLModelerBase
cons public init(com.sun.tools.ws.processor.model.Port,com.sun.tools.ws.wsdl.document.Port,com.sun.tools.ws.wsdl.document.Operation,com.sun.tools.ws.wsdl.document.BindingOperation,com.sun.tools.ws.wsdl.document.soap.SOAPBinding,com.sun.tools.ws.wsdl.document.WSDLDocument,boolean,java.util.Map)
fld public boolean hasOverloadedOperations
fld public com.sun.tools.ws.processor.model.Operation operation
fld public com.sun.tools.ws.processor.model.Port modelPort
fld public com.sun.tools.ws.wsdl.document.BindingOperation bindingOperation
fld public com.sun.tools.ws.wsdl.document.Operation portTypeOperation
fld public com.sun.tools.ws.wsdl.document.Port port
fld public com.sun.tools.ws.wsdl.document.WSDLDocument document
fld public com.sun.tools.ws.wsdl.document.soap.SOAPBinding soapBinding
fld public java.util.Map headers
supr java.lang.Object

CLSS public com.sun.tools.ws.wscompile.AbortException
cons public init()
supr java.lang.RuntimeException

CLSS public final com.sun.tools.ws.wscompile.AuthInfo
cons public init(java.net.URL,java.lang.String,java.lang.String)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
 anno 3 com.sun.istack.NotNull()
meth public boolean matchingHost(java.net.URL)
 anno 1 com.sun.istack.NotNull()
meth public java.lang.String getPassword()
meth public java.lang.String getUser()
supr java.lang.Object
hfds password,urlPattern,user

CLSS public com.sun.tools.ws.wscompile.BadCommandLineException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
meth public com.sun.tools.ws.wscompile.Options getOptions()
 anno 0 com.sun.istack.Nullable()
meth public void initOptions(com.sun.tools.ws.wscompile.Options)
supr java.lang.Exception
hfds options

CLSS public abstract com.sun.tools.ws.wscompile.ErrorReceiver
cons public init()
intf com.sun.tools.xjc.api.ErrorListener
intf org.xml.sax.ErrorHandler
meth protected final java.lang.String getLocationString(org.xml.sax.SAXParseException)
meth public abstract void debug(org.xml.sax.SAXParseException)
meth public abstract void error(org.xml.sax.SAXParseException)
meth public abstract void fatalError(org.xml.sax.SAXParseException)
meth public abstract void info(org.xml.sax.SAXParseException)
meth public abstract void warning(org.xml.sax.SAXParseException)
meth public final void debug(java.lang.String)
meth public final void error(java.lang.String,java.lang.Exception)
meth public final void error(org.xml.sax.Locator,java.lang.String)
meth public final void error(org.xml.sax.Locator,java.lang.String,java.lang.Exception)
meth public final void warning(org.xml.sax.Locator,java.lang.String)
 anno 1 com.sun.istack.Nullable()
meth public void error(java.lang.Exception)
meth public void pollAbort()
supr java.lang.Object

CLSS public com.sun.tools.ws.wscompile.ErrorReceiverFilter
cons public init()
cons public init(com.sun.tools.xjc.api.ErrorListener)
meth public final boolean hadError()
meth public void debug(org.xml.sax.SAXParseException)
meth public void error(org.xml.sax.SAXParseException)
meth public void fatalError(org.xml.sax.SAXParseException)
meth public void info(org.xml.sax.SAXParseException)
meth public void reset()
meth public void setErrorReceiver(com.sun.tools.xjc.api.ErrorListener)
meth public void warning(org.xml.sax.SAXParseException)
supr com.sun.tools.ws.wscompile.ErrorReceiver
hfds core,hadError

CLSS public com.sun.tools.ws.wscompile.FilerCodeWriter
cons public init(com.sun.tools.ws.wscompile.Options) throws java.io.IOException
meth public java.io.OutputStream openBinary(com.sun.codemodel.JPackage,java.lang.String) throws java.io.IOException
meth public java.io.Writer openSource(com.sun.codemodel.JPackage,java.lang.String) throws java.io.IOException
meth public void close() throws java.io.IOException
supr com.sun.codemodel.CodeWriter
hfds options,w

CLSS public com.sun.tools.ws.wscompile.Options
cons public init()
fld public boolean debug
fld public boolean debugMode
fld public boolean disableXmlSecurity
fld public boolean keep
fld public boolean nocompile
fld public boolean quiet
fld public boolean verbose
fld public com.sun.tools.ws.wscompile.Options$Target target
fld public final static int EXTENSION = 2
fld public final static int STRICT = 1
fld public int compatibilityMode
fld public java.io.File destDir
fld public java.io.File sourceDir
fld public java.lang.String classpath
fld public java.lang.String encoding
fld public java.util.List<java.lang.String> javacOptions
fld public javax.annotation.processing.Filer filer
innr public final static !enum Target
innr public final static WeAreDone
meth protected int parseArguments(java.lang.String[],int) throws com.sun.tools.ws.wscompile.BadCommandLineException
meth protected void addFile(java.lang.String) throws com.sun.tools.ws.wscompile.BadCommandLineException
meth protected void disableXmlSecurity()
meth public boolean isExtensionMode()
meth public java.lang.ClassLoader getClassLoader()
meth public java.lang.Iterable<java.io.File> getGeneratedFiles()
meth public java.lang.String requireArgument(java.lang.String,java.lang.String[],int) throws com.sun.tools.ws.wscompile.BadCommandLineException
meth public java.util.List<java.lang.String> getJavacOptions(java.util.List<java.lang.String>,com.sun.tools.ws.wscompile.WsimportListener)
meth public static java.net.URL fileToURL(java.io.File)
meth public static java.net.URL[] pathToURLs(java.lang.String)
meth public void addGeneratedFile(java.io.File)
 anno 0 java.lang.Deprecated()
meth public void addGeneratedFile(javax.tools.FileObject)
meth public void deleteGeneratedFiles()
meth public void parseArguments(java.lang.String[]) throws com.sun.tools.ws.wscompile.BadCommandLineException
meth public void removeGeneratedFiles()
supr java.lang.Object
hfds classLoader,generatedFiles

CLSS public final static !enum com.sun.tools.ws.wscompile.Options$Target
 outer com.sun.tools.ws.wscompile.Options
fld public final static com.sun.tools.ws.wscompile.Options$Target V2_0
fld public final static com.sun.tools.ws.wscompile.Options$Target V2_1
fld public final static com.sun.tools.ws.wscompile.Options$Target V2_2
meth public boolean isLaterThan(com.sun.tools.ws.wscompile.Options$Target)
meth public java.lang.String getVersion()
meth public static com.sun.tools.ws.wscompile.Options$Target getDefault()
meth public static com.sun.tools.ws.wscompile.Options$Target getLoadedAPIVersion()
meth public static com.sun.tools.ws.wscompile.Options$Target parse(java.lang.String)
meth public static com.sun.tools.ws.wscompile.Options$Target valueOf(java.lang.String)
meth public static com.sun.tools.ws.wscompile.Options$Target[] values()
supr java.lang.Enum<com.sun.tools.ws.wscompile.Options$Target>
hfds LOADED_API_VERSION

CLSS public final static com.sun.tools.ws.wscompile.Options$WeAreDone
 outer com.sun.tools.ws.wscompile.Options
cons public init()
supr com.sun.tools.ws.wscompile.BadCommandLineException

CLSS public abstract com.sun.tools.ws.wscompile.Plugin
cons public init()
meth public abstract boolean run(com.sun.tools.ws.processor.model.Model,com.sun.tools.ws.wscompile.WsimportOptions,com.sun.tools.ws.wscompile.ErrorReceiver) throws org.xml.sax.SAXException
meth public abstract java.lang.String getOptionName()
meth public abstract java.lang.String getUsage()
meth public int parseArgument(com.sun.tools.ws.wscompile.Options,java.lang.String[],int) throws com.sun.tools.ws.wscompile.BadCommandLineException,java.io.IOException
meth public void onActivated(com.sun.tools.ws.wscompile.Options) throws com.sun.tools.ws.wscompile.BadCommandLineException
supr java.lang.Object

CLSS public com.sun.tools.ws.wscompile.WSCodeWriter
cons public init(java.io.File,com.sun.tools.ws.wscompile.Options) throws java.io.IOException
meth protected java.io.File getFile(com.sun.codemodel.JPackage,java.lang.String) throws java.io.IOException
supr com.sun.codemodel.writer.FileCodeWriter
hfds options

CLSS public com.sun.tools.ws.wscompile.WsgenOptions
cons public init()
fld public boolean doNotOverWrite
fld public boolean genWsdl
fld public boolean inlineSchemas
fld public boolean nosource
fld public boolean protocolSet
fld public final static java.lang.String X_SOAP12 = "Xsoap1.2"
fld public java.io.File nonclassDestDir
fld public java.io.File wsgenReport
fld public java.lang.Class endpoint
fld public java.lang.String protocol
fld public java.util.List<java.lang.String> externalMetadataFiles
fld public java.util.Map<java.lang.String,java.lang.String> nonstdProtocols
fld public java.util.Set<java.lang.String> protocols
fld public javax.xml.namespace.QName portName
fld public javax.xml.namespace.QName serviceName
meth protected int parseArguments(java.lang.String[],int) throws com.sun.tools.ws.wscompile.BadCommandLineException
meth protected void addFile(java.lang.String)
meth public void validate() throws com.sun.tools.ws.wscompile.BadCommandLineException
supr com.sun.tools.ws.wscompile.Options
hfds HTTP,NOSOURCE_OPTION,PORTNAME_OPTION,SERVICENAME_OPTION,SOAP11,endpoints,isImplClass

CLSS public com.sun.tools.ws.wscompile.WsgenTool
cons public init(java.io.OutputStream)
cons public init(java.io.OutputStream,com.sun.xml.ws.api.server.Container)
meth protected void usage(com.sun.tools.ws.wscompile.Options)
meth public boolean buildModel(java.lang.String,com.sun.tools.ws.wscompile.WsgenTool$Listener) throws com.sun.tools.ws.wscompile.BadCommandLineException
meth public boolean run(java.lang.String[])
supr java.lang.Object
hfds container,options,out
hcls Listener,ReportOutput

CLSS public com.sun.tools.ws.wscompile.WsimportListener
cons public init()
intf com.sun.tools.xjc.api.ErrorListener
meth public boolean isCanceled()
meth public void debug(org.xml.sax.SAXParseException)
meth public void error(org.xml.sax.SAXParseException)
meth public void fatalError(org.xml.sax.SAXParseException)
meth public void generatedFile(java.lang.String)
meth public void info(org.xml.sax.SAXParseException)
meth public void message(java.lang.String)
meth public void warning(org.xml.sax.SAXParseException)
supr java.lang.Object

CLSS public com.sun.tools.ws.wscompile.WsimportOptions
cons public init()
fld public boolean additionalHeaders
fld public boolean disableAuthenticator
fld public boolean disableSSLHostnameVerification
fld public boolean isGenerateJWS
fld public boolean noAddressingBbinding
fld public boolean useBaseResourceAndURLToLoadWSDL
fld public final java.util.List<com.sun.tools.ws.wscompile.Plugin> activePlugins
fld public final static java.lang.String defaultAuthfile
fld public java.io.File authFile
fld public java.io.File implDestDir
fld public java.lang.String clientjar
fld public java.lang.String defaultPackage
fld public java.lang.String implPortName
fld public java.lang.String implServiceName
fld public java.lang.String proxyAuth
fld public java.lang.String wsdlLocation
fld public java.util.HashMap<java.lang.String,java.lang.String> extensionOptions
fld public java.util.List<java.lang.String> cmdlineJars
fld public org.xml.sax.EntityResolver entityResolver
meth protected void addFile(java.lang.String) throws com.sun.tools.ws.wscompile.BadCommandLineException
meth protected void disableXmlSecurity()
meth public com.sun.codemodel.JCodeModel getCodeModel()
meth public com.sun.tools.xjc.api.SchemaCompiler getSchemaCompiler()
meth public final void parseArguments(java.lang.String[]) throws com.sun.tools.ws.wscompile.BadCommandLineException
meth public final void parseBindings(com.sun.tools.ws.wscompile.ErrorReceiver)
meth public int parseArguments(java.lang.String[],int) throws com.sun.tools.ws.wscompile.BadCommandLineException
meth public java.lang.String getExtensionOption(java.lang.String)
meth public java.lang.String getModuleName()
meth public java.util.List<com.sun.tools.ws.wscompile.Plugin> getAllPlugins()
meth public org.w3c.dom.Element getHandlerChainConfiguration()
meth public org.xml.sax.InputSource[] getSchemaBindings()
meth public org.xml.sax.InputSource[] getSchemas()
meth public org.xml.sax.InputSource[] getWSDLBindings()
meth public org.xml.sax.InputSource[] getWSDLs()
meth public void addBindings(java.lang.String) throws com.sun.tools.ws.wscompile.BadCommandLineException
meth public void addGrammarRecursive(java.io.File)
meth public void addHandlerChainConfiguration(org.w3c.dom.Element)
meth public void addSchema(java.io.File)
meth public void addSchema(org.xml.sax.InputSource)
meth public void addSchemmaBindFile(org.xml.sax.InputSource)
meth public void addWSDL(java.io.File)
meth public void addWSDL(org.xml.sax.InputSource)
meth public void addWSDLBindFile(org.xml.sax.InputSource)
meth public void setCodeModel(com.sun.codemodel.JCodeModel)
meth public void validate() throws com.sun.tools.ws.wscompile.BadCommandLineException
supr com.sun.tools.ws.wscompile.Options
hfds allPlugins,bindingFiles,codeModel,handlerConfigs,javaModule,jaxbCustomBindings,jaxwsCustomBindings,proxyHost,proxyPort,schemaCompiler,schemas,wsdls
hcls ByteStream,RereadInputSource,RereadInputStream

CLSS public com.sun.tools.ws.wscompile.WsimportTool
cons public init(java.io.OutputStream)
cons public init(java.io.OutputStream,com.sun.xml.ws.api.server.Container)
fld protected com.sun.tools.ws.wscompile.WsimportOptions options
innr protected Listener
innr protected Receiver
meth protected boolean compileGeneratedClasses(com.sun.tools.ws.wscompile.ErrorReceiver,com.sun.tools.ws.wscompile.WsimportListener)
meth protected boolean generateCode(com.sun.tools.ws.wscompile.WsimportTool$Listener,com.sun.tools.ws.wscompile.WsimportTool$Receiver,com.sun.tools.ws.processor.model.Model,boolean) throws java.io.IOException
meth protected boolean run(java.lang.String[],com.sun.tools.ws.wscompile.WsimportTool$Listener,com.sun.tools.ws.wscompile.WsimportTool$Receiver)
meth protected com.sun.tools.ws.processor.model.Model buildWsdlModel(com.sun.tools.ws.wscompile.WsimportTool$Listener,com.sun.tools.ws.wscompile.WsimportTool$Receiver) throws com.sun.tools.ws.wscompile.BadCommandLineException,java.io.IOException,javax.xml.stream.XMLStreamException
meth protected void parseArguments(java.lang.String[],com.sun.tools.ws.wscompile.WsimportTool$Listener,com.sun.tools.ws.wscompile.WsimportTool$Receiver) throws com.sun.tools.ws.wscompile.BadCommandLineException
meth protected void usage(com.sun.tools.ws.wscompile.Options)
meth public boolean run(java.lang.String[])
meth public void setEntityResolver(org.xml.sax.EntityResolver)
supr java.lang.Object
hfds JAXWS_MODULE,WSIMPORT,container,out

CLSS protected com.sun.tools.ws.wscompile.WsimportTool$Listener
 outer com.sun.tools.ws.wscompile.WsimportTool
cons protected init(com.sun.tools.ws.wscompile.WsimportTool)
meth public void debug(org.xml.sax.SAXParseException)
meth public void enableDebugging()
meth public void error(org.xml.sax.SAXParseException)
meth public void fatalError(org.xml.sax.SAXParseException)
meth public void generatedFile(java.lang.String)
meth public void info(org.xml.sax.SAXParseException)
meth public void message(java.lang.String)
meth public void warning(org.xml.sax.SAXParseException)
supr com.sun.tools.ws.wscompile.WsimportListener
hfds cer

CLSS protected com.sun.tools.ws.wscompile.WsimportTool$Receiver
 outer com.sun.tools.ws.wscompile.WsimportTool
cons public init(com.sun.tools.ws.wscompile.WsimportTool,com.sun.tools.ws.wscompile.WsimportTool$Listener)
meth public void debug(org.xml.sax.SAXParseException)
meth public void info(org.xml.sax.SAXParseException)
meth public void pollAbort()
meth public void warning(org.xml.sax.SAXParseException)
supr com.sun.tools.ws.wscompile.ErrorReceiverFilter
hfds listener

CLSS public com.sun.tools.ws.wsdl.document.soap.SOAP12Binding
cons public init(org.xml.sax.Locator)
meth public javax.xml.namespace.QName getElementName()
supr com.sun.tools.ws.wsdl.document.soap.SOAPBinding

CLSS public abstract interface com.sun.tools.ws.wsdl.document.soap.SOAP12Constants
fld public final static java.lang.String NS_SOAP_ENCODING = "http://schemas.xmlsoap.org/soap/encoding/"
fld public final static java.lang.String NS_WSDL_SOAP = "http://schemas.xmlsoap.org/wsdl/soap12/"
fld public final static java.lang.String URI_SOAP_TRANSPORT_HTTP = "http://www.w3.org/2003/05/soap/bindings/HTTP/"
fld public final static javax.xml.namespace.QName QNAME_ADDRESS
fld public final static javax.xml.namespace.QName QNAME_ATTR_ARRAY_SIZE
fld public final static javax.xml.namespace.QName QNAME_ATTR_ARRAY_TYPE
fld public final static javax.xml.namespace.QName QNAME_ATTR_GROUP_COMMON_ATTRIBUTES
fld public final static javax.xml.namespace.QName QNAME_ATTR_HREF
fld public final static javax.xml.namespace.QName QNAME_ATTR_ID
fld public final static javax.xml.namespace.QName QNAME_ATTR_ITEM_TYPE
fld public final static javax.xml.namespace.QName QNAME_ATTR_OFFSET
fld public final static javax.xml.namespace.QName QNAME_ATTR_POSITION
fld public final static javax.xml.namespace.QName QNAME_BINDING
fld public final static javax.xml.namespace.QName QNAME_BODY
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_ANY_URI
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_BASE64_BINARY
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_BOOLEAN
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_BYTE
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_DATE
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_DATE_TIME
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_DECIMAL
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_DOUBLE
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_DURATION
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_ENTITIES
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_ENTITY
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_FLOAT
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_G_DAY
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_G_MONTH
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_G_MONTH_DAY
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_G_YEAR
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_G_YEAR_MONTH
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_HEX_BINARY
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_ID
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_IDREF
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_IDREFS
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_INT
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_INTEGER
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_LONG
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_NAME
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_NCNAME
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_NEGATIVE_INTEGER
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_NMTOKEN
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_NMTOKENS
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_NON_NEGATIVE_INTEGER
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_NON_POSITIVE_INTEGER
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_NORMALIZED_STRING
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_NOTATION
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_POSITIVE_INTEGER
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_QNAME
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_SHORT
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_STRING
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_TIME
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_TOKEN
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_UNSIGNED_BYTE
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_UNSIGNED_INT
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_UNSIGNED_LONG
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_UNSIGNED_SHORT
fld public final static javax.xml.namespace.QName QNAME_FAULT
fld public final static javax.xml.namespace.QName QNAME_HEADER
fld public final static javax.xml.namespace.QName QNAME_HEADERFAULT
fld public final static javax.xml.namespace.QName QNAME_OPERATION
fld public final static javax.xml.namespace.QName QNAME_TYPE_ANY_URI
fld public final static javax.xml.namespace.QName QNAME_TYPE_ARRAY
fld public final static javax.xml.namespace.QName QNAME_TYPE_BASE64
fld public final static javax.xml.namespace.QName QNAME_TYPE_BASE64_BINARY
fld public final static javax.xml.namespace.QName QNAME_TYPE_BOOLEAN
fld public final static javax.xml.namespace.QName QNAME_TYPE_BYTE
fld public final static javax.xml.namespace.QName QNAME_TYPE_DATE
fld public final static javax.xml.namespace.QName QNAME_TYPE_DATE_TIME
fld public final static javax.xml.namespace.QName QNAME_TYPE_DECIMAL
fld public final static javax.xml.namespace.QName QNAME_TYPE_DOUBLE
fld public final static javax.xml.namespace.QName QNAME_TYPE_DURATION
fld public final static javax.xml.namespace.QName QNAME_TYPE_ENTITIES
fld public final static javax.xml.namespace.QName QNAME_TYPE_ENTITY
fld public final static javax.xml.namespace.QName QNAME_TYPE_FLOAT
fld public final static javax.xml.namespace.QName QNAME_TYPE_G_DAY
fld public final static javax.xml.namespace.QName QNAME_TYPE_G_MONTH
fld public final static javax.xml.namespace.QName QNAME_TYPE_G_MONTH_DAY
fld public final static javax.xml.namespace.QName QNAME_TYPE_G_YEAR
fld public final static javax.xml.namespace.QName QNAME_TYPE_G_YEAR_MONTH
fld public final static javax.xml.namespace.QName QNAME_TYPE_HEX_BINARY
fld public final static javax.xml.namespace.QName QNAME_TYPE_ID
fld public final static javax.xml.namespace.QName QNAME_TYPE_IDREF
fld public final static javax.xml.namespace.QName QNAME_TYPE_IDREFS
fld public final static javax.xml.namespace.QName QNAME_TYPE_INT
fld public final static javax.xml.namespace.QName QNAME_TYPE_INTEGER
fld public final static javax.xml.namespace.QName QNAME_TYPE_LANGUAGE
fld public final static javax.xml.namespace.QName QNAME_TYPE_LONG
fld public final static javax.xml.namespace.QName QNAME_TYPE_NAME
fld public final static javax.xml.namespace.QName QNAME_TYPE_NCNAME
fld public final static javax.xml.namespace.QName QNAME_TYPE_NEGATIVE_INTEGER
fld public final static javax.xml.namespace.QName QNAME_TYPE_NMTOKEN
fld public final static javax.xml.namespace.QName QNAME_TYPE_NMTOKENS
fld public final static javax.xml.namespace.QName QNAME_TYPE_NON_NEGATIVE_INTEGER
fld public final static javax.xml.namespace.QName QNAME_TYPE_NON_POSITIVE_INTEGER
fld public final static javax.xml.namespace.QName QNAME_TYPE_NORMALIZED_STRING
fld public final static javax.xml.namespace.QName QNAME_TYPE_NOTATION
fld public final static javax.xml.namespace.QName QNAME_TYPE_POSITIVE_INTEGER
fld public final static javax.xml.namespace.QName QNAME_TYPE_QNAME
fld public final static javax.xml.namespace.QName QNAME_TYPE_SHORT
fld public final static javax.xml.namespace.QName QNAME_TYPE_STRING
fld public final static javax.xml.namespace.QName QNAME_TYPE_TIME
fld public final static javax.xml.namespace.QName QNAME_TYPE_TOKEN
fld public final static javax.xml.namespace.QName QNAME_TYPE_UNSIGNED_BYTE
fld public final static javax.xml.namespace.QName QNAME_TYPE_UNSIGNED_INT
fld public final static javax.xml.namespace.QName QNAME_TYPE_UNSIGNED_LONG
fld public final static javax.xml.namespace.QName QNAME_TYPE_UNSIGNED_SHORT

CLSS public com.sun.tools.ws.wsdl.document.soap.SOAPAddress
cons public init(org.xml.sax.Locator)
meth public java.lang.String getLocation()
meth public javax.xml.namespace.QName getElementName()
meth public void setLocation(java.lang.String)
meth public void validateThis()
supr com.sun.tools.ws.wsdl.framework.ExtensionImpl
hfds _location

CLSS public com.sun.tools.ws.wsdl.document.soap.SOAPBinding
cons public init(org.xml.sax.Locator)
meth public boolean isDocument()
meth public boolean isRPC()
meth public com.sun.tools.ws.wsdl.document.soap.SOAPStyle getStyle()
meth public java.lang.String getTransport()
meth public javax.xml.namespace.QName getElementName()
meth public void setStyle(com.sun.tools.ws.wsdl.document.soap.SOAPStyle)
meth public void setTransport(java.lang.String)
meth public void validateThis()
supr com.sun.tools.ws.wsdl.framework.ExtensionImpl
hfds _style,_transport

CLSS public com.sun.tools.ws.wsdl.document.soap.SOAPBody
cons public init(org.xml.sax.Locator)
meth public boolean isEncoded()
meth public boolean isLiteral()
meth public com.sun.tools.ws.wsdl.document.soap.SOAPUse getUse()
meth public java.lang.String getEncodingStyle()
meth public java.lang.String getNamespace()
meth public java.lang.String getParts()
meth public javax.xml.namespace.QName getElementName()
meth public void setEncodingStyle(java.lang.String)
meth public void setNamespace(java.lang.String)
meth public void setParts(java.lang.String)
meth public void setUse(com.sun.tools.ws.wsdl.document.soap.SOAPUse)
meth public void validateThis()
supr com.sun.tools.ws.wsdl.framework.ExtensionImpl
hfds _encodingStyle,_namespace,_parts,_use

CLSS public abstract interface com.sun.tools.ws.wsdl.document.soap.SOAPConstants
fld public final static java.lang.String NS_SOAP_ENCODING = "http://schemas.xmlsoap.org/soap/encoding/"
fld public final static java.lang.String NS_WSDL_SOAP = "http://schemas.xmlsoap.org/wsdl/soap/"
fld public final static java.lang.String URI_ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope/"
fld public final static java.lang.String URI_SOAP_TRANSPORT_HTTP = "http://schemas.xmlsoap.org/soap/http"
fld public final static javax.xml.namespace.QName QNAME_ADDRESS
fld public final static javax.xml.namespace.QName QNAME_ATTR_ARRAY_TYPE
fld public final static javax.xml.namespace.QName QNAME_ATTR_GROUP_COMMON_ATTRIBUTES
fld public final static javax.xml.namespace.QName QNAME_ATTR_HREF
fld public final static javax.xml.namespace.QName QNAME_ATTR_ID
fld public final static javax.xml.namespace.QName QNAME_ATTR_OFFSET
fld public final static javax.xml.namespace.QName QNAME_ATTR_POSITION
fld public final static javax.xml.namespace.QName QNAME_BINDING
fld public final static javax.xml.namespace.QName QNAME_BODY
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_ANY_URI
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_BASE64_BINARY
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_BOOLEAN
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_BYTE
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_DATE
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_DATE_TIME
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_DECIMAL
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_DOUBLE
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_DURATION
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_ENTITIES
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_ENTITY
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_FLOAT
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_G_DAY
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_G_MONTH
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_G_MONTH_DAY
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_G_YEAR
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_G_YEAR_MONTH
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_HEX_BINARY
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_ID
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_IDREF
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_IDREFS
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_INT
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_INTEGER
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_LONG
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_NAME
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_NCNAME
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_NEGATIVE_INTEGER
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_NMTOKEN
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_NMTOKENS
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_NON_NEGATIVE_INTEGER
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_NON_POSITIVE_INTEGER
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_NORMALIZED_STRING
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_NOTATION
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_POSITIVE_INTEGER
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_QNAME
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_SHORT
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_STRING
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_TIME
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_TOKEN
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_UNSIGNED_BYTE
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_UNSIGNED_INT
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_UNSIGNED_LONG
fld public final static javax.xml.namespace.QName QNAME_ELEMENT_UNSIGNED_SHORT
fld public final static javax.xml.namespace.QName QNAME_FAULT
fld public final static javax.xml.namespace.QName QNAME_HEADER
fld public final static javax.xml.namespace.QName QNAME_HEADERFAULT
fld public final static javax.xml.namespace.QName QNAME_MUSTUNDERSTAND
fld public final static javax.xml.namespace.QName QNAME_OPERATION
fld public final static javax.xml.namespace.QName QNAME_TYPE_ANY_URI
fld public final static javax.xml.namespace.QName QNAME_TYPE_ARRAY
fld public final static javax.xml.namespace.QName QNAME_TYPE_BASE64
fld public final static javax.xml.namespace.QName QNAME_TYPE_BASE64_BINARY
fld public final static javax.xml.namespace.QName QNAME_TYPE_BOOLEAN
fld public final static javax.xml.namespace.QName QNAME_TYPE_BYTE
fld public final static javax.xml.namespace.QName QNAME_TYPE_DATE
fld public final static javax.xml.namespace.QName QNAME_TYPE_DATE_TIME
fld public final static javax.xml.namespace.QName QNAME_TYPE_DECIMAL
fld public final static javax.xml.namespace.QName QNAME_TYPE_DOUBLE
fld public final static javax.xml.namespace.QName QNAME_TYPE_DURATION
fld public final static javax.xml.namespace.QName QNAME_TYPE_ENTITIES
fld public final static javax.xml.namespace.QName QNAME_TYPE_ENTITY
fld public final static javax.xml.namespace.QName QNAME_TYPE_FLOAT
fld public final static javax.xml.namespace.QName QNAME_TYPE_G_DAY
fld public final static javax.xml.namespace.QName QNAME_TYPE_G_MONTH
fld public final static javax.xml.namespace.QName QNAME_TYPE_G_MONTH_DAY
fld public final static javax.xml.namespace.QName QNAME_TYPE_G_YEAR
fld public final static javax.xml.namespace.QName QNAME_TYPE_G_YEAR_MONTH
fld public final static javax.xml.namespace.QName QNAME_TYPE_HEX_BINARY
fld public final static javax.xml.namespace.QName QNAME_TYPE_ID
fld public final static javax.xml.namespace.QName QNAME_TYPE_IDREF
fld public final static javax.xml.namespace.QName QNAME_TYPE_IDREFS
fld public final static javax.xml.namespace.QName QNAME_TYPE_INT
fld public final static javax.xml.namespace.QName QNAME_TYPE_INTEGER
fld public final static javax.xml.namespace.QName QNAME_TYPE_LANGUAGE
fld public final static javax.xml.namespace.QName QNAME_TYPE_LONG
fld public final static javax.xml.namespace.QName QNAME_TYPE_NAME
fld public final static javax.xml.namespace.QName QNAME_TYPE_NCNAME
fld public final static javax.xml.namespace.QName QNAME_TYPE_NEGATIVE_INTEGER
fld public final static javax.xml.namespace.QName QNAME_TYPE_NMTOKEN
fld public final static javax.xml.namespace.QName QNAME_TYPE_NMTOKENS
fld public final static javax.xml.namespace.QName QNAME_TYPE_NON_NEGATIVE_INTEGER
fld public final static javax.xml.namespace.QName QNAME_TYPE_NON_POSITIVE_INTEGER
fld public final static javax.xml.namespace.QName QNAME_TYPE_NORMALIZED_STRING
fld public final static javax.xml.namespace.QName QNAME_TYPE_NOTATION
fld public final static javax.xml.namespace.QName QNAME_TYPE_POSITIVE_INTEGER
fld public final static javax.xml.namespace.QName QNAME_TYPE_QNAME
fld public final static javax.xml.namespace.QName QNAME_TYPE_SHORT
fld public final static javax.xml.namespace.QName QNAME_TYPE_STRING
fld public final static javax.xml.namespace.QName QNAME_TYPE_TIME
fld public final static javax.xml.namespace.QName QNAME_TYPE_TOKEN
fld public final static javax.xml.namespace.QName QNAME_TYPE_UNSIGNED_BYTE
fld public final static javax.xml.namespace.QName QNAME_TYPE_UNSIGNED_INT
fld public final static javax.xml.namespace.QName QNAME_TYPE_UNSIGNED_LONG
fld public final static javax.xml.namespace.QName QNAME_TYPE_UNSIGNED_SHORT

CLSS public com.sun.tools.ws.wsdl.document.soap.SOAPFault
cons public init(org.xml.sax.Locator)
meth public boolean isEncoded()
meth public boolean isLiteral()
meth public com.sun.tools.ws.wsdl.document.soap.SOAPUse getUse()
meth public java.lang.String getEncodingStyle()
meth public java.lang.String getName()
meth public java.lang.String getNamespace()
meth public javax.xml.namespace.QName getElementName()
meth public void setEncodingStyle(java.lang.String)
meth public void setName(java.lang.String)
meth public void setNamespace(java.lang.String)
meth public void setUse(com.sun.tools.ws.wsdl.document.soap.SOAPUse)
meth public void validateThis()
supr com.sun.tools.ws.wsdl.framework.ExtensionImpl
hfds _encodingStyle,_name,_namespace,_use

CLSS public com.sun.tools.ws.wsdl.document.soap.SOAPHeader
cons public init(org.xml.sax.Locator)
meth public boolean isEncoded()
meth public boolean isLiteral()
meth public com.sun.tools.ws.wsdl.document.soap.SOAPUse getUse()
meth public java.lang.String getEncodingStyle()
meth public java.lang.String getNamespace()
meth public java.lang.String getPart()
meth public java.util.Iterator faults()
meth public javax.xml.namespace.QName getElementName()
meth public javax.xml.namespace.QName getMessage()
meth public void accept(com.sun.tools.ws.wsdl.framework.ExtensionVisitor) throws java.lang.Exception
meth public void add(com.sun.tools.ws.wsdl.document.soap.SOAPHeaderFault)
meth public void setEncodingStyle(java.lang.String)
meth public void setMessage(javax.xml.namespace.QName)
meth public void setNamespace(java.lang.String)
meth public void setPart(java.lang.String)
meth public void setUse(com.sun.tools.ws.wsdl.document.soap.SOAPUse)
meth public void validateThis()
meth public void withAllQNamesDo(com.sun.tools.ws.wsdl.framework.QNameAction)
meth public void withAllSubEntitiesDo(com.sun.tools.ws.wsdl.framework.EntityAction)
supr com.sun.tools.ws.wsdl.framework.ExtensionImpl
hfds _encodingStyle,_faults,_message,_namespace,_part,_use

CLSS public com.sun.tools.ws.wsdl.document.soap.SOAPHeaderFault
cons public init(org.xml.sax.Locator)
meth public boolean isEncoded()
meth public boolean isLiteral()
meth public com.sun.tools.ws.wsdl.document.soap.SOAPUse getUse()
meth public java.lang.String getEncodingStyle()
meth public java.lang.String getNamespace()
meth public java.lang.String getPart()
meth public javax.xml.namespace.QName getElementName()
meth public javax.xml.namespace.QName getMessage()
meth public void setEncodingStyle(java.lang.String)
meth public void setMessage(javax.xml.namespace.QName)
meth public void setNamespace(java.lang.String)
meth public void setPart(java.lang.String)
meth public void setUse(com.sun.tools.ws.wsdl.document.soap.SOAPUse)
meth public void validateThis()
meth public void withAllQNamesDo(com.sun.tools.ws.wsdl.framework.QNameAction)
supr com.sun.tools.ws.wsdl.framework.ExtensionImpl
hfds _encodingStyle,_message,_namespace,_part,_use

CLSS public com.sun.tools.ws.wsdl.document.soap.SOAPOperation
cons public init(org.xml.sax.Locator)
meth public boolean isDocument()
meth public boolean isRPC()
meth public com.sun.tools.ws.wsdl.document.soap.SOAPStyle getStyle()
meth public java.lang.String getSOAPAction()
meth public javax.xml.namespace.QName getElementName()
meth public void setSOAPAction(java.lang.String)
meth public void setStyle(com.sun.tools.ws.wsdl.document.soap.SOAPStyle)
meth public void validateThis()
supr com.sun.tools.ws.wsdl.framework.ExtensionImpl
hfds _soapAction,_style

CLSS public final com.sun.tools.ws.wsdl.document.soap.SOAPStyle
fld public final static com.sun.tools.ws.wsdl.document.soap.SOAPStyle DOCUMENT
fld public final static com.sun.tools.ws.wsdl.document.soap.SOAPStyle RPC
supr java.lang.Object

CLSS public final com.sun.tools.ws.wsdl.document.soap.SOAPUse
fld public final static com.sun.tools.ws.wsdl.document.soap.SOAPUse ENCODED
fld public final static com.sun.tools.ws.wsdl.document.soap.SOAPUse LITERAL
supr java.lang.Object

CLSS public abstract interface com.sun.tools.ws.wsdl.framework.Elemental
meth public abstract javax.xml.namespace.QName getElementName()
meth public abstract org.xml.sax.Locator getLocator()

CLSS public abstract com.sun.tools.ws.wsdl.framework.Entity
cons public init(org.xml.sax.Locator)
fld protected com.sun.tools.ws.wscompile.ErrorReceiver errorReceiver
intf com.sun.tools.ws.wsdl.framework.Elemental
meth protected void failValidation(java.lang.String)
meth protected void failValidation(java.lang.String,java.lang.String)
meth public abstract void validateThis()
meth public java.lang.Object getProperty(java.lang.String)
meth public org.xml.sax.Locator getLocator()
meth public void removeProperty(java.lang.String)
meth public void setErrorReceiver(com.sun.tools.ws.wscompile.ErrorReceiver)
meth public void setProperty(java.lang.String,java.lang.Object)
meth public void withAllEntityReferencesDo(com.sun.tools.ws.wsdl.framework.EntityReferenceAction)
meth public void withAllQNamesDo(com.sun.tools.ws.wsdl.framework.QNameAction)
meth public void withAllSubEntitiesDo(com.sun.tools.ws.wsdl.framework.EntityAction)
supr java.lang.Object
hfds _properties,locator

CLSS public abstract interface com.sun.tools.ws.wsdl.framework.EntityReferenceValidator
meth public abstract boolean isValid(com.sun.tools.ws.wsdl.framework.Kind,javax.xml.namespace.QName)

CLSS public abstract com.sun.tools.ws.wsdl.framework.ExtensionImpl
cons public init(org.xml.sax.Locator)
intf com.sun.tools.ws.api.wsdl.TWSDLExtension
meth public com.sun.tools.ws.api.wsdl.TWSDLExtensible getParent()
meth public void accept(com.sun.tools.ws.wsdl.framework.ExtensionVisitor) throws java.lang.Exception
meth public void setParent(com.sun.tools.ws.api.wsdl.TWSDLExtensible)
supr com.sun.tools.ws.wsdl.framework.Entity
hfds _parent

CLSS public abstract com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler
cons public init(java.util.Map<java.lang.String,com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler>)
meth protected boolean handleMIMEPartExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean doHandleExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public java.util.Map<java.lang.String,com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler> getExtensionHandlers()
supr com.sun.tools.ws.api.wsdl.TWSDLExtensionHandler
hfds extensionHandlers,unmodExtenHandlers

CLSS public abstract com.sun.tools.ws.wsdl.parser.AbstractReferenceFinderImpl
cons protected init(com.sun.tools.ws.wsdl.parser.DOMForest)
fld protected final com.sun.tools.ws.wsdl.parser.DOMForest parent
meth protected abstract java.lang.String findExternalResource(java.lang.String,java.lang.String,org.xml.sax.Attributes)
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
supr org.xml.sax.helpers.XMLFilterImpl
hfds locator

CLSS public abstract interface com.sun.tools.ws.wsdl.parser.Constants
fld public final static java.lang.String ATTRVALUE_ = ""
fld public final static java.lang.String ATTRVALUE_ALL = "#all"
fld public final static java.lang.String ATTRVALUE_ANY = "##any"
fld public final static java.lang.String ATTRVALUE_DOCUMENT = "document"
fld public final static java.lang.String ATTRVALUE_ENCODED = "encoded"
fld public final static java.lang.String ATTRVALUE_EXTENSION = "extension"
fld public final static java.lang.String ATTRVALUE_LAX = "lax"
fld public final static java.lang.String ATTRVALUE_LIST = "list"
fld public final static java.lang.String ATTRVALUE_LITERAL = "literal"
fld public final static java.lang.String ATTRVALUE_LOCAL = "##local"
fld public final static java.lang.String ATTRVALUE_OPTIONAL = "optional"
fld public final static java.lang.String ATTRVALUE_OTHER = "##other"
fld public final static java.lang.String ATTRVALUE_PROHIBITED = "prohibited"
fld public final static java.lang.String ATTRVALUE_QUALIFIED = "qualified"
fld public final static java.lang.String ATTRVALUE_REQUIRED = "required"
fld public final static java.lang.String ATTRVALUE_RESTRICTION = "restriction"
fld public final static java.lang.String ATTRVALUE_RPC = "rpc"
fld public final static java.lang.String ATTRVALUE_SKIP = "skip"
fld public final static java.lang.String ATTRVALUE_STRICT = "strict"
fld public final static java.lang.String ATTRVALUE_SUBSTITUTION = "substitution"
fld public final static java.lang.String ATTRVALUE_TARGET_NAMESPACE = "##targetNamespace"
fld public final static java.lang.String ATTRVALUE_UNBOUNDED = "unbounded"
fld public final static java.lang.String ATTRVALUE_UNION = "union"
fld public final static java.lang.String ATTRVALUE_UNQUALIFIED = "unqualified"
fld public final static java.lang.String ATTR_ = ""
fld public final static java.lang.String ATTR_ABSTRACT = "abstract"
fld public final static java.lang.String ATTR_ATTRIBUTE_FORM_DEFAULT = "attributeFormDefault"
fld public final static java.lang.String ATTR_BASE = "base"
fld public final static java.lang.String ATTR_BINDING = "binding"
fld public final static java.lang.String ATTR_BLOCK = "block"
fld public final static java.lang.String ATTR_BLOCK_DEFAULT = "blockDefault"
fld public final static java.lang.String ATTR_DEFAULT = "default"
fld public final static java.lang.String ATTR_ELEMENT = "element"
fld public final static java.lang.String ATTR_ELEMENT_FORM_DEFAULT = "elementFormDefault"
fld public final static java.lang.String ATTR_ENCODING_STYLE = "encodingStyle"
fld public final static java.lang.String ATTR_FINAL = "final"
fld public final static java.lang.String ATTR_FINAL_DEFAULT = "finalDefault"
fld public final static java.lang.String ATTR_FIXED = "fixed"
fld public final static java.lang.String ATTR_FORM = "form"
fld public final static java.lang.String ATTR_ID = "id"
fld public final static java.lang.String ATTR_ITEM_TYPE = "itemType"
fld public final static java.lang.String ATTR_LOCATION = "location"
fld public final static java.lang.String ATTR_MAX_OCCURS = "maxOccurs"
fld public final static java.lang.String ATTR_MEMBER_TYPES = "memberTypes"
fld public final static java.lang.String ATTR_MESSAGE = "message"
fld public final static java.lang.String ATTR_MIN_OCCURS = "minOccurs"
fld public final static java.lang.String ATTR_MIXED = "mixed"
fld public final static java.lang.String ATTR_NAME = "name"
fld public final static java.lang.String ATTR_NAMESPACE = "namespace"
fld public final static java.lang.String ATTR_NILLABLE = "nillable"
fld public final static java.lang.String ATTR_PARAMETER_ORDER = "parameterOrder"
fld public final static java.lang.String ATTR_PART = "part"
fld public final static java.lang.String ATTR_PARTS = "parts"
fld public final static java.lang.String ATTR_PROCESS_CONTENTS = "processContents"
fld public final static java.lang.String ATTR_PUBLIC = "public"
fld public final static java.lang.String ATTR_REF = "ref"
fld public final static java.lang.String ATTR_REFER = "refer"
fld public final static java.lang.String ATTR_REQUIRED = "required"
fld public final static java.lang.String ATTR_SCHEMA_LOCATION = "schemaLocation"
fld public final static java.lang.String ATTR_SOAP_ACTION = "soapAction"
fld public final static java.lang.String ATTR_STYLE = "style"
fld public final static java.lang.String ATTR_SUBSTITUTION_GROUP = "substitutionGroup"
fld public final static java.lang.String ATTR_SYSTEM = "system"
fld public final static java.lang.String ATTR_TARGET_NAMESPACE = "targetNamespace"
fld public final static java.lang.String ATTR_TRANSPORT = "transport"
fld public final static java.lang.String ATTR_TYPE = "type"
fld public final static java.lang.String ATTR_USE = "use"
fld public final static java.lang.String ATTR_VALUE = "value"
fld public final static java.lang.String ATTR_VERB = "verb"
fld public final static java.lang.String ATTR_VERSION = "version"
fld public final static java.lang.String ATTR_XPATH = "xpath"
fld public final static java.lang.String FALSE = "false"
fld public final static java.lang.String NS_ = ""
fld public final static java.lang.String NS_WSDL = "http://schemas.xmlsoap.org/wsdl/"
fld public final static java.lang.String NS_WSDL_HTTP = "http://schemas.xmlsoap.org/wsdl/http/"
fld public final static java.lang.String NS_WSDL_MIME = "http://schemas.xmlsoap.org/wsdl/mime/"
fld public final static java.lang.String NS_WSDL_SOAP = "http://schemas.xmlsoap.org/wsdl/soap/"
fld public final static java.lang.String NS_WSDL_SOAP12 = "http://schemas.xmlsoap.org/wsdl/soap12/"
fld public final static java.lang.String NS_XML = "http://www.w3.org/XML/1998/namespace"
fld public final static java.lang.String NS_XMLNS = "http://www.w3.org/2000/xmlns/"
fld public final static java.lang.String NS_XSD = "http://www.w3.org/2001/XMLSchema"
fld public final static java.lang.String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance"
fld public final static java.lang.String TAG_ = ""
fld public final static java.lang.String TAG_BINDING = "binding"
fld public final static java.lang.String TAG_DEFINITIONS = "definitions"
fld public final static java.lang.String TAG_DOCUMENTATION = "documentation"
fld public final static java.lang.String TAG_FAULT = "fault"
fld public final static java.lang.String TAG_INPUT = "input"
fld public final static java.lang.String TAG_MESSAGE = "message"
fld public final static java.lang.String TAG_OPERATION = "operation"
fld public final static java.lang.String TAG_OUTPUT = "output"
fld public final static java.lang.String TAG_PART = "part"
fld public final static java.lang.String TAG_PORT = "port"
fld public final static java.lang.String TAG_PORT_TYPE = "portType"
fld public final static java.lang.String TAG_SERVICE = "service"
fld public final static java.lang.String TAG_TYPES = "types"
fld public final static java.lang.String TRUE = "true"
fld public final static java.lang.String XMLNS = "xmlns"

CLSS public com.sun.tools.ws.wsdl.parser.DOMForest
cons public init(com.sun.tools.ws.wsdl.parser.InternalizationLogic,org.xml.sax.EntityResolver,com.sun.tools.ws.wscompile.WsimportOptions,com.sun.tools.ws.wscompile.ErrorReceiver)
 anno 2 com.sun.istack.NotNull()
fld protected final com.sun.tools.ws.wscompile.ErrorReceiver errorReceiver
fld protected final com.sun.tools.ws.wscompile.WsimportOptions options
fld protected final com.sun.tools.ws.wsdl.parser.InternalizationLogic logic
fld protected final java.util.List<org.w3c.dom.Element> inlinedSchemaElements
fld protected final java.util.Map<java.lang.String,org.w3c.dom.Document> core
fld protected final java.util.Set<java.lang.String> externalReferences
fld protected final java.util.Set<java.lang.String> rootDocuments
fld protected final org.xml.sax.EntityResolver entityResolver
fld protected java.util.Map<java.lang.String,java.lang.String> resolvedCache
fld public final com.sun.tools.xjc.reader.internalizer.LocatorTable locatorTable
fld public final java.util.Set<org.w3c.dom.Element> outerMostBindings
innr public abstract interface static Handler
meth public java.lang.String getFirstRootDocument()
meth public java.lang.String getSystemId(org.w3c.dom.Document)
meth public java.lang.String[] listSystemIDs()
meth public java.util.List<org.w3c.dom.Element> getInlinedSchemaElement()
meth public java.util.Map<java.lang.String,java.lang.String> getReferencedEntityMap()
meth public java.util.Set<java.lang.String> getExternalReferences()
meth public java.util.Set<java.lang.String> getRootDocuments()
meth public org.w3c.dom.Document get(java.lang.String)
meth public org.w3c.dom.Document parse(java.lang.String,boolean) throws java.io.IOException,org.xml.sax.SAXException
meth public org.w3c.dom.Document parse(org.xml.sax.InputSource,boolean) throws java.io.IOException,org.xml.sax.SAXException
 anno 0 com.sun.istack.NotNull()
meth public void addExternalReferences(java.lang.String)
meth public void dump(java.io.OutputStream) throws java.io.IOException
supr java.lang.Object
hfds documentBuilder,parserFactory

CLSS public abstract interface static com.sun.tools.ws.wsdl.parser.DOMForest$Handler
 outer com.sun.tools.ws.wsdl.parser.DOMForest
intf org.xml.sax.ContentHandler
meth public abstract org.w3c.dom.Document getDocument()

CLSS public com.sun.tools.ws.wsdl.parser.DOMForestParser
cons public init(com.sun.tools.ws.wsdl.parser.DOMForest,com.sun.xml.xsom.parser.XMLParser)
intf com.sun.xml.xsom.parser.XMLParser
meth public void parse(org.xml.sax.InputSource,org.xml.sax.ContentHandler,org.xml.sax.EntityResolver,org.xml.sax.ErrorHandler) throws java.io.IOException,org.xml.sax.SAXException
meth public void parse(org.xml.sax.InputSource,org.xml.sax.ContentHandler,org.xml.sax.ErrorHandler,org.xml.sax.EntityResolver) throws java.io.IOException,org.xml.sax.SAXException
supr java.lang.Object
hfds fallbackParser,forest,scanner

CLSS public com.sun.tools.ws.wsdl.parser.DOMForestScanner
cons public init(com.sun.tools.ws.wsdl.parser.DOMForest)
meth public void scan(org.w3c.dom.Document,org.xml.sax.ContentHandler) throws org.xml.sax.SAXException
meth public void scan(org.w3c.dom.Element,org.xml.sax.ContentHandler) throws org.xml.sax.SAXException
supr java.lang.Object
hfds forest
hcls LocationResolver

CLSS public com.sun.tools.ws.wsdl.parser.HTTPExtensionHandler
cons public init(java.util.Map<java.lang.String,com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler>)
meth public boolean handleBindingExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleDefinitionsExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleFaultExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleInputExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleOperationExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleOutputExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handlePortExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handlePortTypeExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleServiceExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleTypesExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public java.lang.String getNamespaceURI()
supr com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler

CLSS public abstract interface com.sun.tools.ws.wsdl.parser.InternalizationLogic
meth public abstract boolean checkIfValidTargetNode(com.sun.tools.ws.wsdl.parser.DOMForest,org.w3c.dom.Element,org.w3c.dom.Element)
meth public abstract org.w3c.dom.Element refineSchemaTarget(org.w3c.dom.Element)
meth public abstract org.w3c.dom.Element refineWSDLTarget(org.w3c.dom.Element)
meth public abstract org.xml.sax.helpers.XMLFilterImpl createExternalReferenceFinder(com.sun.tools.ws.wsdl.parser.DOMForest)

CLSS public com.sun.tools.ws.wsdl.parser.Internalizer
cons public init(com.sun.tools.ws.wsdl.parser.DOMForest,com.sun.tools.ws.wscompile.WsimportOptions,com.sun.tools.ws.wscompile.ErrorReceiver)
meth public org.w3c.dom.Element refineSchemaTarget(org.w3c.dom.Element)
meth public org.w3c.dom.Element refineWSDLTarget(org.w3c.dom.Element)
meth public void transform()
supr java.lang.Object
hfds errorReceiver,forest,xpath,xpf

CLSS public com.sun.tools.ws.wsdl.parser.JAXWSBindingExtensionHandler
cons public init(java.util.Map<java.lang.String,com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler>)
meth public boolean handleBindingExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleDefinitionsExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleFaultExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleOperationExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handlePortExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handlePortTypeExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleServiceExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public java.lang.String getNamespaceURI()
supr com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler
hfds xpath,xpf

CLSS public com.sun.tools.ws.wsdl.parser.MIMEExtensionHandler
cons public init(java.util.Map<java.lang.String,com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler>)
meth protected boolean handleInputOutputExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth protected boolean handleMIMEPartExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth protected com.sun.tools.ws.wsdl.document.mime.MIMEContent parseMIMEContent(com.sun.tools.ws.api.wsdl.TWSDLParserContext,org.w3c.dom.Element)
meth protected com.sun.tools.ws.wsdl.document.mime.MIMEXml parseMIMEXml(com.sun.tools.ws.api.wsdl.TWSDLParserContext,org.w3c.dom.Element)
meth public boolean doHandleExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public java.lang.String getNamespaceURI()
supr com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler

CLSS public com.sun.tools.ws.wsdl.parser.MemberSubmissionAddressingExtensionHandler
cons public init(java.util.Map<java.lang.String,com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler>,com.sun.tools.ws.wscompile.ErrorReceiver,boolean)
meth protected javax.xml.namespace.QName getWSDLExtensionQName()
meth public boolean handleFaultExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleInputExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleOutputExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handlePortExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public java.lang.String getNamespaceURI()
supr com.sun.tools.ws.wsdl.parser.W3CAddressingExtensionHandler
hfds errReceiver,extensionModeOn

CLSS public final com.sun.tools.ws.wsdl.parser.MetadataFinder
cons public init(com.sun.tools.ws.wsdl.parser.InternalizationLogic,com.sun.tools.ws.wscompile.WsimportOptions,com.sun.tools.ws.wscompile.ErrorReceiver)
fld public boolean isMexMetadata
innr public static WSEntityResolver
meth public java.lang.String getRootWSDL()
 anno 0 com.sun.istack.Nullable()
meth public java.util.Set<java.lang.String> getRootWSDLs()
 anno 0 com.sun.istack.NotNull()
meth public void parseWSDL()
supr com.sun.tools.ws.wsdl.parser.DOMForest
hfds rootWSDL,rootWsdls
hcls HttpClientVerifier

CLSS public static com.sun.tools.ws.wsdl.parser.MetadataFinder$WSEntityResolver
 outer com.sun.tools.ws.wsdl.parser.MetadataFinder
cons public init(com.sun.tools.ws.wscompile.WsimportOptions,com.sun.tools.ws.wscompile.ErrorReceiver)
intf org.xml.sax.EntityResolver
meth protected void finalize() throws java.lang.Throwable
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
supr java.lang.Object
hfds c,doReset,errorReceiver,options

CLSS public com.sun.tools.ws.wsdl.parser.NamespaceContextImpl
cons public init(org.w3c.dom.Element)
intf javax.xml.namespace.NamespaceContext
meth public java.lang.String getNamespaceURI(java.lang.String)
meth public java.lang.String getPrefix(java.lang.String)
meth public java.util.Iterator getPrefixes(java.lang.String)
supr java.lang.Object
hfds e

CLSS public com.sun.tools.ws.wsdl.parser.Policy12ExtensionHandler
cons public init()
meth public boolean handleBindingExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleDefinitionsExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleFaultExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleInputExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleOperationExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleOutputExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handlePortExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handlePortTypeExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleServiceExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public java.lang.String getNamespaceURI()
supr com.sun.tools.ws.api.wsdl.TWSDLExtensionHandler

CLSS public com.sun.tools.ws.wsdl.parser.Policy15ExtensionHandler
cons public init()
meth public boolean handleBindingExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleDefinitionsExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleFaultExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleInputExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleOperationExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleOutputExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handlePortExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handlePortTypeExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleServiceExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public java.lang.String getNamespaceURI()
supr com.sun.tools.ws.api.wsdl.TWSDLExtensionHandler

CLSS public com.sun.tools.ws.wsdl.parser.SOAP12ExtensionHandler
cons public init(java.util.Map<java.lang.String,com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler>)
meth protected com.sun.tools.ws.wsdl.document.soap.SOAPBinding getSOAPBinding(org.xml.sax.Locator)
meth protected javax.xml.namespace.QName getAddressQName()
meth protected javax.xml.namespace.QName getBindingQName()
meth protected javax.xml.namespace.QName getBodyQName()
meth protected javax.xml.namespace.QName getFaultQName()
meth protected javax.xml.namespace.QName getHeaderQName()
meth protected javax.xml.namespace.QName getHeaderfaultQName()
meth protected javax.xml.namespace.QName getOperationQName()
meth public java.lang.String getNamespaceURI()
supr com.sun.tools.ws.wsdl.parser.SOAPExtensionHandler

CLSS public com.sun.tools.ws.wsdl.parser.SOAPEntityReferenceValidator
cons public init()
intf com.sun.tools.ws.wsdl.framework.EntityReferenceValidator
meth public boolean isValid(com.sun.tools.ws.wsdl.framework.Kind,javax.xml.namespace.QName)
supr java.lang.Object
hfds _validAttributes,_validElements,_validTypes

CLSS public com.sun.tools.ws.wsdl.parser.SOAPExtensionHandler
cons public init(java.util.Map<java.lang.String,com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler>)
meth protected boolean handleInputOutputExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth protected boolean handleMIMEPartExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth protected com.sun.tools.ws.wsdl.document.soap.SOAPBinding getSOAPBinding(org.xml.sax.Locator)
meth protected javax.xml.namespace.QName getAddressQName()
meth protected javax.xml.namespace.QName getBindingQName()
meth protected javax.xml.namespace.QName getBodyQName()
meth protected javax.xml.namespace.QName getFaultQName()
meth protected javax.xml.namespace.QName getHeaderQName()
meth protected javax.xml.namespace.QName getHeaderfaultQName()
meth protected javax.xml.namespace.QName getOperationQName()
meth public boolean handleBindingExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleDefinitionsExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleFaultExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleInputExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleOperationExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleOutputExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handlePortExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handlePortTypeExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleServiceExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleTypesExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public java.lang.String getNamespaceURI()
supr com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler

CLSS public com.sun.tools.ws.wsdl.parser.Util
cons public init()
meth public static boolean isTagName(org.w3c.dom.Element,javax.xml.namespace.QName)
meth public static java.lang.String getRequiredAttribute(org.w3c.dom.Element,java.lang.String)
meth public static java.lang.String processSystemIdWithBase(java.lang.String,java.lang.String)
meth public static org.w3c.dom.Element nextElement(java.util.Iterator)
meth public static org.w3c.dom.Element nextElementIgnoringCharacterContent(java.util.Iterator)
meth public static void fail(java.lang.String)
meth public static void fail(java.lang.String,java.lang.Object[])
meth public static void fail(java.lang.String,java.lang.String)
meth public static void fail(java.lang.String,java.lang.String,java.lang.String)
meth public static void verifyTag(org.w3c.dom.Element,java.lang.String)
meth public static void verifyTagNS(org.w3c.dom.Element,java.lang.String,java.lang.String)
meth public static void verifyTagNS(org.w3c.dom.Element,javax.xml.namespace.QName)
meth public static void verifyTagNSRootElement(org.w3c.dom.Element,javax.xml.namespace.QName)
supr java.lang.Object

CLSS public com.sun.tools.ws.wsdl.parser.VersionChecker
cons public init(org.xml.sax.ContentHandler,org.xml.sax.ErrorHandler,org.xml.sax.EntityResolver)
cons public init(org.xml.sax.XMLReader)
meth public void endDocument() throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
supr org.xml.sax.helpers.XMLFilterImpl
hfds VERSIONS,locator,rootTagStart,seenBindings,seenRoot,version

CLSS public com.sun.tools.ws.wsdl.parser.W3CAddressingExtensionHandler
cons public init(java.util.Map<java.lang.String,com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler>)
cons public init(java.util.Map<java.lang.String,com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler>,com.sun.tools.ws.wscompile.ErrorReceiver)
meth protected javax.xml.namespace.QName getWSDLExtensionQName()
meth public boolean handleBindingExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handlePortExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public java.lang.String getNamespaceURI()
supr com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler

CLSS public com.sun.tools.ws.wsdl.parser.W3CAddressingMetadataExtensionHandler
cons public init(java.util.Map<java.lang.String,com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler>,com.sun.tools.ws.wscompile.ErrorReceiver)
meth public boolean handleFaultExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleInputExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public boolean handleOutputExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public java.lang.String getNamespaceURI()
supr com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler
hfds errReceiver

CLSS public com.sun.tools.ws.wsdl.parser.WSDLInternalizationLogic
cons public init()
intf com.sun.tools.ws.wsdl.parser.InternalizationLogic
meth public boolean checkIfValidTargetNode(com.sun.tools.ws.wsdl.parser.DOMForest,org.w3c.dom.Element,org.w3c.dom.Element)
meth public org.w3c.dom.Element refineSchemaTarget(org.w3c.dom.Element)
meth public org.w3c.dom.Element refineWSDLTarget(org.w3c.dom.Element)
meth public org.xml.sax.helpers.XMLFilterImpl createExternalReferenceFinder(com.sun.tools.ws.wsdl.parser.DOMForest)
supr java.lang.Object
hcls ReferenceFinder

CLSS public com.sun.tools.ws.wsdl.parser.WSDLParser
cons public init(com.sun.tools.ws.wscompile.WsimportOptions,com.sun.tools.ws.wscompile.ErrorReceiverFilter,com.sun.tools.ws.wsdl.parser.MetadataFinder)
meth public com.sun.tools.ws.wsdl.document.WSDLDocument parse() throws java.io.IOException,org.xml.sax.SAXException
meth public com.sun.tools.ws.wsdl.parser.MetadataFinder getDOMForest()
meth public void addParserListener(com.sun.tools.ws.wsdl.framework.ParserListener)
supr java.lang.Object
hfds errReceiver,extensionHandlers,forest,listeners,options

CLSS public abstract interface com.sun.tools.xjc.api.ClassNameAllocator
meth public abstract java.lang.String assignClassName(java.lang.String,java.lang.String)

CLSS public abstract interface com.sun.tools.xjc.api.ErrorListener
intf com.sun.xml.bind.api.ErrorListener
meth public abstract void error(org.xml.sax.SAXParseException)
meth public abstract void fatalError(org.xml.sax.SAXParseException)
meth public abstract void info(org.xml.sax.SAXParseException)
meth public abstract void warning(org.xml.sax.SAXParseException)

CLSS public abstract interface com.sun.xml.bind.api.ErrorListener
intf org.xml.sax.ErrorHandler
meth public abstract void error(org.xml.sax.SAXParseException)
meth public abstract void fatalError(org.xml.sax.SAXParseException)
meth public abstract void info(org.xml.sax.SAXParseException)
meth public abstract void warning(org.xml.sax.SAXParseException)

CLSS public com.sun.xml.ws.util.ASCIIUtility
meth public static int parseInt(byte[],int,int,int)
meth public static java.lang.String toString(byte[],int,int)
meth public static void copyStream(java.io.InputStream,java.io.OutputStream) throws java.io.IOException
supr java.lang.Object

CLSS public com.sun.xml.ws.util.AuthUtil
cons public init()
meth public static void setAuthenticator(java.net.Authenticator,java.net.HttpURLConnection)
supr java.lang.Object

CLSS public com.sun.xml.ws.util.ByteArrayBuffer
cons public init()
cons public init(byte[])
cons public init(byte[],int)
cons public init(int)
fld protected byte[] buf
meth public final byte[] getRawData()
meth public final byte[] toByteArray()
meth public final int size()
meth public final java.io.InputStream newInputStream()
meth public final java.io.InputStream newInputStream(int,int)
meth public final void reset()
meth public final void write(byte[],int,int)
meth public final void write(int)
meth public final void write(java.io.InputStream) throws java.io.IOException
meth public final void writeTo(java.io.OutputStream) throws java.io.IOException
meth public java.lang.String toString()
meth public void close() throws java.io.IOException
supr java.io.OutputStream
hfds CHUNK_SIZE,count

CLSS public final com.sun.xml.ws.util.ByteArrayDataSource
cons public init(byte[],int,int,java.lang.String)
cons public init(byte[],int,java.lang.String)
cons public init(byte[],java.lang.String)
intf javax.activation.DataSource
meth public java.io.InputStream getInputStream()
meth public java.io.OutputStream getOutputStream()
meth public java.lang.String getContentType()
meth public java.lang.String getName()
supr java.lang.Object
hfds buf,contentType,len,start

CLSS public com.sun.xml.ws.util.CompletedFuture<%0 extends java.lang.Object>
cons public init({com.sun.xml.ws.util.CompletedFuture%0},java.lang.Throwable)
intf java.util.concurrent.Future<{com.sun.xml.ws.util.CompletedFuture%0}>
meth public boolean cancel(boolean)
meth public boolean isCancelled()
meth public boolean isDone()
meth public {com.sun.xml.ws.util.CompletedFuture%0} get() throws java.util.concurrent.ExecutionException
meth public {com.sun.xml.ws.util.CompletedFuture%0} get(long,java.util.concurrent.TimeUnit) throws java.util.concurrent.ExecutionException
supr java.lang.Object
hfds re,v

CLSS public com.sun.xml.ws.util.Constants
cons public init()
fld public final static java.lang.String LoggingDomain = "com.sun.xml.ws"
supr java.lang.Object

CLSS public com.sun.xml.ws.util.DOMUtil
cons public init()
meth public static java.util.List<org.w3c.dom.Element> getChildElements(org.w3c.dom.Node)
 anno 0 com.sun.istack.NotNull()
meth public static org.w3c.dom.Document createDom()
meth public static org.w3c.dom.Element getFirstChild(org.w3c.dom.Element,java.lang.String,java.lang.String)
meth public static org.w3c.dom.Element getFirstElementChild(org.w3c.dom.Node)
 anno 0 com.sun.istack.Nullable()
meth public static void serializeNode(org.w3c.dom.Element,javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
meth public static void writeTagWithAttributes(org.w3c.dom.Element,javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
supr java.lang.Object
hfds db

CLSS public com.sun.xml.ws.util.FastInfosetUtil
meth public static boolean isFastInfosetSource(javax.xml.transform.Source)
meth public static com.sun.xml.ws.api.pipe.Codec getFICodec()
meth public static com.sun.xml.ws.api.pipe.Codec getFICodec(com.sun.xml.ws.api.pipe.StreamSOAPCodec,com.sun.xml.ws.api.SOAPVersion)
meth public static javax.xml.stream.XMLStreamReader createFIStreamReader(javax.xml.transform.Source)
supr java.lang.Object
hfds LOG,fi
hcls FISupport

CLSS public com.sun.xml.ws.util.HandlerAnnotationInfo
cons public init()
meth public java.util.List<javax.xml.ws.handler.Handler> getHandlers()
meth public java.util.Set<java.lang.String> getRoles()
meth public void setHandlers(java.util.List<javax.xml.ws.handler.Handler>)
meth public void setRoles(java.util.Set<java.lang.String>)
supr java.lang.Object
hfds handlers,roles

CLSS public com.sun.xml.ws.util.HandlerAnnotationProcessor
cons public init()
meth public static com.sun.xml.ws.handler.HandlerChainsModel buildHandlerChainsModel(java.lang.Class<?>)
meth public static com.sun.xml.ws.util.HandlerAnnotationInfo buildHandlerInfo(java.lang.Class<?>,javax.xml.namespace.QName,javax.xml.namespace.QName,com.sun.xml.ws.api.WSBinding)
 anno 1 com.sun.istack.NotNull()
supr java.lang.Object
hfds logger

CLSS public abstract com.sun.xml.ws.util.InjectionPlan<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
innr public static FieldInjectionPlan
innr public static MethodInjectionPlan
meth public abstract void inject({com.sun.xml.ws.util.InjectionPlan%0},{com.sun.xml.ws.util.InjectionPlan%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> com.sun.xml.ws.util.InjectionPlan<{%%0},{%%1}> buildInjectionPlan(java.lang.Class<? extends {%%0}>,java.lang.Class<{%%1}>,boolean)
meth public void inject({com.sun.xml.ws.util.InjectionPlan%0},java.util.concurrent.Callable<{com.sun.xml.ws.util.InjectionPlan%1}>)
supr java.lang.Object
hcls Compositor

CLSS public static com.sun.xml.ws.util.InjectionPlan$FieldInjectionPlan<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer com.sun.xml.ws.util.InjectionPlan
cons public init(java.lang.reflect.Field)
meth public void inject({com.sun.xml.ws.util.InjectionPlan$FieldInjectionPlan%0},{com.sun.xml.ws.util.InjectionPlan$FieldInjectionPlan%1})
supr com.sun.xml.ws.util.InjectionPlan<{com.sun.xml.ws.util.InjectionPlan$FieldInjectionPlan%0},{com.sun.xml.ws.util.InjectionPlan$FieldInjectionPlan%1}>
hfds field

CLSS public static com.sun.xml.ws.util.InjectionPlan$MethodInjectionPlan<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer com.sun.xml.ws.util.InjectionPlan
cons public init(java.lang.reflect.Method)
meth public void inject({com.sun.xml.ws.util.InjectionPlan$MethodInjectionPlan%0},{com.sun.xml.ws.util.InjectionPlan$MethodInjectionPlan%1})
supr com.sun.xml.ws.util.InjectionPlan<{com.sun.xml.ws.util.InjectionPlan$MethodInjectionPlan%0},{com.sun.xml.ws.util.InjectionPlan$MethodInjectionPlan%1}>
hfds method

CLSS public final com.sun.xml.ws.util.JAXWSUtils
cons public init()
meth public static boolean matchQNames(javax.xml.namespace.QName,javax.xml.namespace.QName)
meth public static java.lang.String absolutize(java.lang.String)
meth public static java.lang.String getFileOrURLName(java.lang.String)
meth public static java.lang.String getUUID()
meth public static java.net.URL getEncodedURL(java.lang.String) throws java.net.MalformedURLException
meth public static java.net.URL getFileOrURL(java.lang.String) throws java.io.IOException
meth public static void checkAbsoluteness(java.lang.String)
supr java.lang.Object

CLSS public com.sun.xml.ws.util.MetadataUtil
cons public init()
meth public static java.util.Map<java.lang.String,com.sun.xml.ws.api.server.SDDocument> getMetadataClosure(java.lang.String,com.sun.xml.ws.wsdl.SDDocumentResolver,boolean)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
supr java.lang.Object

CLSS public final com.sun.xml.ws.util.MethodUtil
meth protected java.lang.Class<?> findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected java.lang.Class<?> loadClass(java.lang.String,boolean) throws java.lang.ClassNotFoundException
meth protected java.security.PermissionCollection getPermissions(java.security.CodeSource)
meth public static java.lang.Object invoke(java.lang.reflect.Method,java.lang.Object,java.lang.Object[]) throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
supr java.security.SecureClassLoader
hfds DEFAULT_BUFFER_SIZE,MAX_BUFFER_SIZE,PROXY_PACKAGE,TRAMPOLINE,WS_UTIL_PKG,bounce

CLSS public com.sun.xml.ws.util.MrJarUtil
cons public init()
meth public static boolean getNoPoolProperty(java.lang.String)
supr java.lang.Object

CLSS public final com.sun.xml.ws.util.NamespaceSupport
cons public init()
cons public init(com.sun.xml.ws.util.NamespaceSupport)
fld public final static java.lang.String XMLNS = "http://www.w3.org/XML/1998/namespace"
meth public boolean declarePrefix(java.lang.String,java.lang.String)
meth public java.lang.Iterable<java.lang.String> getDeclaredPrefixes()
meth public java.lang.Iterable<java.lang.String> getPrefixes()
meth public java.lang.String getPrefix(java.lang.String)
meth public java.lang.String getURI(java.lang.String)
meth public java.lang.String[] processName(java.lang.String,java.lang.String[],boolean)
meth public java.util.Iterator getPrefixes(java.lang.String)
meth public void popContext()
meth public void pushContext()
meth public void reset()
meth public void slideContextDown()
meth public void slideContextUp()
supr java.lang.Object
hfds EMPTY_ENUMERATION,contextPos,contexts,currentContext
hcls Context

CLSS public com.sun.xml.ws.util.NoCloseInputStream
cons public init(java.io.InputStream)
meth public void close() throws java.io.IOException
meth public void doClose() throws java.io.IOException
supr java.io.FilterInputStream

CLSS public com.sun.xml.ws.util.NoCloseOutputStream
cons public init(java.io.OutputStream)
meth public void close() throws java.io.IOException
meth public void doClose() throws java.io.IOException
supr java.io.FilterOutputStream

CLSS public abstract com.sun.xml.ws.util.Pool<%0 extends java.lang.Object>
cons public init()
innr public final static Marshaller
innr public final static TubePool
innr public final static Unmarshaller
meth protected abstract {com.sun.xml.ws.util.Pool%0} create()
meth public final void recycle({com.sun.xml.ws.util.Pool%0})
meth public final {com.sun.xml.ws.util.Pool%0} take()
supr java.lang.Object
hfds queue

CLSS public final static com.sun.xml.ws.util.Pool$Marshaller
 outer com.sun.xml.ws.util.Pool
cons public init(javax.xml.bind.JAXBContext)
meth protected javax.xml.bind.Marshaller create()
supr com.sun.xml.ws.util.Pool<javax.xml.bind.Marshaller>
hfds context

CLSS public final static com.sun.xml.ws.util.Pool$TubePool
 outer com.sun.xml.ws.util.Pool
cons public init(com.sun.xml.ws.api.pipe.Tube)
meth protected com.sun.xml.ws.api.pipe.Tube create()
meth public final com.sun.xml.ws.api.pipe.Tube takeMaster()
 anno 0 java.lang.Deprecated()
supr com.sun.xml.ws.util.Pool<com.sun.xml.ws.api.pipe.Tube>
hfds master

CLSS public final static com.sun.xml.ws.util.Pool$Unmarshaller
 outer com.sun.xml.ws.util.Pool
cons public init(javax.xml.bind.JAXBContext)
meth protected javax.xml.bind.Unmarshaller create()
supr com.sun.xml.ws.util.Pool<javax.xml.bind.Unmarshaller>
hfds context

CLSS public final com.sun.xml.ws.util.QNameMap<%0 extends java.lang.Object>
cons public init()
innr public final static Entry
meth public boolean containsKey(java.lang.String,java.lang.String)
 anno 1 com.sun.istack.NotNull()
meth public boolean isEmpty()
meth public com.sun.xml.ws.util.QNameMap$Entry<{com.sun.xml.ws.util.QNameMap%0}> getOne()
meth public com.sun.xml.ws.util.QNameMap<{com.sun.xml.ws.util.QNameMap%0}> putAll(com.sun.xml.ws.util.QNameMap<? extends {com.sun.xml.ws.util.QNameMap%0}>)
meth public com.sun.xml.ws.util.QNameMap<{com.sun.xml.ws.util.QNameMap%0}> putAll(java.util.Map<javax.xml.namespace.QName,? extends {com.sun.xml.ws.util.QNameMap%0}>)
meth public int size()
meth public java.lang.Iterable<{com.sun.xml.ws.util.QNameMap%0}> values()
meth public java.lang.String toString()
meth public java.util.Collection<javax.xml.namespace.QName> keySet()
meth public java.util.Set<com.sun.xml.ws.util.QNameMap$Entry<{com.sun.xml.ws.util.QNameMap%0}>> entrySet()
meth public void put(java.lang.String,java.lang.String,{com.sun.xml.ws.util.QNameMap%0})
meth public void put(javax.xml.namespace.QName,{com.sun.xml.ws.util.QNameMap%0})
meth public {com.sun.xml.ws.util.QNameMap%0} get(java.lang.String,java.lang.String)
 anno 1 com.sun.istack.NotNull()
meth public {com.sun.xml.ws.util.QNameMap%0} get(javax.xml.namespace.QName)
supr java.lang.Object
hfds DEFAULT_INITIAL_CAPACITY,DEFAULT_LOAD_FACTOR,MAXIMUM_CAPACITY,entrySet,size,table,threshold,views
hcls EntryIterator,EntrySet,HashIterator,ValueIterator

CLSS public final static com.sun.xml.ws.util.QNameMap$Entry<%0 extends java.lang.Object>
 outer com.sun.xml.ws.util.QNameMap
fld public final java.lang.String localName
fld public final java.lang.String nsUri
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public javax.xml.namespace.QName createQName()
meth public {com.sun.xml.ws.util.QNameMap$Entry%0} getValue()
meth public {com.sun.xml.ws.util.QNameMap$Entry%0} setValue({com.sun.xml.ws.util.QNameMap$Entry%0})
supr java.lang.Object
hfds hash,next,value

CLSS public com.sun.xml.ws.util.ReadAllStream
cons public init()
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void readAll(java.io.InputStream,long) throws java.io.IOException
supr java.io.InputStream
hfds LOGGER,closed,fileStream,memStream,readAll
hcls FileStream,MemoryStream

CLSS public final com.sun.xml.ws.util.RuntimeVersion
cons public init()
fld public final static com.sun.xml.ws.util.Version VERSION
meth public java.lang.String getVersion()
supr java.lang.Object

CLSS public com.sun.xml.ws.util.ServiceConfigurationError
cons public init(java.lang.String)
cons public init(java.lang.Throwable)
supr java.lang.Error

CLSS public final com.sun.xml.ws.util.ServiceFinder<%0 extends java.lang.Object>
intf java.lang.Iterable<{com.sun.xml.ws.util.ServiceFinder%0}>
meth public java.util.Iterator<{com.sun.xml.ws.util.ServiceFinder%0}> iterator()
meth public static <%0 extends java.lang.Object> com.sun.xml.ws.util.ServiceFinder<{%%0}> find(java.lang.Class<{%%0}>)
 anno 1 com.sun.istack.NotNull()
meth public static <%0 extends java.lang.Object> com.sun.xml.ws.util.ServiceFinder<{%%0}> find(java.lang.Class<{%%0}>,com.sun.xml.ws.api.Component)
 anno 1 com.sun.istack.NotNull()
meth public static <%0 extends java.lang.Object> com.sun.xml.ws.util.ServiceFinder<{%%0}> find(java.lang.Class<{%%0}>,com.sun.xml.ws.api.Component,java.util.ServiceLoader<{%%0}>)
 anno 1 com.sun.istack.NotNull()
 anno 3 com.sun.istack.NotNull()
meth public static <%0 extends java.lang.Object> com.sun.xml.ws.util.ServiceFinder<{%%0}> find(java.lang.Class<{%%0}>,java.lang.ClassLoader)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.Nullable()
meth public static <%0 extends java.lang.Object> com.sun.xml.ws.util.ServiceFinder<{%%0}> find(java.lang.Class<{%%0}>,java.lang.ClassLoader,com.sun.xml.ws.api.Component)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.Nullable()
meth public static <%0 extends java.lang.Object> com.sun.xml.ws.util.ServiceFinder<{%%0}> find(java.lang.Class<{%%0}>,java.util.ServiceLoader<{%%0}>)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public {com.sun.xml.ws.util.ServiceFinder%0}[] toArray()
supr java.lang.Object
hfds component,serviceClass,serviceLoader
hcls ComponentExWrapper,CompositeIterator

CLSS public com.sun.xml.ws.util.StreamUtils
cons public init()
meth public static java.io.InputStream hasSomeData(java.io.InputStream)
supr java.lang.Object

CLSS public com.sun.xml.ws.util.StringUtils
cons public init()
meth public static java.lang.String capitalize(java.lang.String)
meth public static java.lang.String decapitalize(java.lang.String)
supr java.lang.Object

CLSS public com.sun.xml.ws.util.UtilException
cons public !varargs init(java.lang.String,java.lang.Object[])
cons public init(com.sun.istack.localization.Localizable)
cons public init(java.lang.Throwable)
meth public java.lang.String getDefaultResourceBundleName()
supr com.sun.xml.ws.util.exception.JAXWSExceptionBase

CLSS public final com.sun.xml.ws.util.Version
fld public final java.lang.String BUILD_ID
fld public final java.lang.String BUILD_VERSION
fld public final java.lang.String MAJOR_VERSION
fld public final java.lang.String SVN_REVISION
fld public final static com.sun.xml.ws.util.Version RUNTIME_VERSION
meth public java.lang.String toString()
meth public static com.sun.xml.ws.util.Version create(java.io.InputStream)
supr java.lang.Object

CLSS public final com.sun.xml.ws.util.VersionUtil
cons public init()
fld public final static java.lang.String JAXWS_VERSION_20 = "2.0"
fld public final static java.lang.String JAXWS_VERSION_DEFAULT = "2.0"
meth public static boolean isValidVersion(java.lang.String)
meth public static boolean isVersion20(java.lang.String)
meth public static int compare(java.lang.String,java.lang.String)
meth public static int[] getCanonicalVersion(java.lang.String)
meth public static java.lang.String getValidVersionString()
supr java.lang.Object

CLSS public abstract com.sun.xml.ws.util.exception.JAXWSExceptionBase
cons protected !varargs init(java.lang.String,java.lang.Object[])
 anno 0 java.lang.Deprecated()
cons protected init(com.sun.istack.localization.Localizable)
cons protected init(com.sun.istack.localization.Localizable,java.lang.Throwable)
cons protected init(java.lang.String)
cons protected init(java.lang.Throwable)
intf com.sun.istack.localization.Localizable
intf com.sun.istack.localization.LocalizableMessageFactory$ResourceBundleSupplier
meth protected abstract java.lang.String getDefaultResourceBundleName()
meth public final java.lang.Object[] getArguments()
meth public final java.lang.String getKey()
meth public final java.lang.String getResourceBundleName()
meth public java.lang.String getMessage()
meth public java.util.ResourceBundle getResourceBundle(java.util.Locale)
supr javax.xml.ws.WebServiceException
hfds msg,serialVersionUID

CLSS public abstract interface com.sun.xml.xsom.parser.XMLParser
meth public abstract void parse(org.xml.sax.InputSource,org.xml.sax.ContentHandler,org.xml.sax.ErrorHandler,org.xml.sax.EntityResolver) throws java.io.IOException,org.xml.sax.SAXException

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public java.io.FilterInputStream
cons protected init(java.io.InputStream)
fld protected volatile java.io.InputStream in
meth public boolean markSupported()
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[]) throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr java.io.InputStream

CLSS public java.io.FilterOutputStream
cons public init(java.io.OutputStream)
fld protected java.io.OutputStream out
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.OutputStream

CLSS public abstract interface java.io.Flushable
meth public abstract void flush() throws java.io.IOException

CLSS public abstract java.io.InputStream
cons public init()
intf java.io.Closeable
meth public abstract int read() throws java.io.IOException
meth public boolean markSupported()
meth public int available() throws java.io.IOException
meth public int read(byte[]) throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr java.lang.Object

CLSS public abstract java.io.OutputStream
cons public init()
intf java.io.Closeable
intf java.io.Flushable
meth public abstract void write(int) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract java.lang.ClassLoader
cons protected init()
cons protected init(java.lang.ClassLoader)
meth protected final java.lang.Class<?> defineClass(byte[],int,int)
 anno 0 java.lang.Deprecated()
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int)
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int,java.security.ProtectionDomain)
meth protected final java.lang.Class<?> defineClass(java.lang.String,java.nio.ByteBuffer,java.security.ProtectionDomain)
meth protected final java.lang.Class<?> findLoadedClass(java.lang.String)
meth protected final java.lang.Class<?> findSystemClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected final void resolveClass(java.lang.Class<?>)
meth protected final void setSigners(java.lang.Class<?>,java.lang.Object[])
meth protected java.lang.Class<?> findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected java.lang.Class<?> loadClass(java.lang.String,boolean) throws java.lang.ClassNotFoundException
meth protected java.lang.Object getClassLoadingLock(java.lang.String)
meth protected java.lang.Package definePackage(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.net.URL)
meth protected java.lang.Package getPackage(java.lang.String)
meth protected java.lang.Package[] getPackages()
meth protected java.lang.String findLibrary(java.lang.String)
meth protected java.net.URL findResource(java.lang.String)
meth protected java.util.Enumeration<java.net.URL> findResources(java.lang.String) throws java.io.IOException
meth protected static boolean registerAsParallelCapable()
meth public final java.lang.ClassLoader getParent()
meth public java.io.InputStream getResourceAsStream(java.lang.String)
meth public java.lang.Class<?> loadClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public java.net.URL getResource(java.lang.String)
meth public java.util.Enumeration<java.net.URL> getResources(java.lang.String) throws java.io.IOException
meth public static java.io.InputStream getSystemResourceAsStream(java.lang.String)
meth public static java.lang.ClassLoader getSystemClassLoader()
meth public static java.net.URL getSystemResource(java.lang.String)
meth public static java.util.Enumeration<java.net.URL> getSystemResources(java.lang.String) throws java.io.IOException
meth public void clearAssertionStatus()
meth public void setClassAssertionStatus(java.lang.String,boolean)
meth public void setDefaultAssertionStatus(boolean)
meth public void setPackageAssertionStatus(java.lang.String,boolean)
supr java.lang.Object

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

CLSS public java.lang.Error
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

CLSS public abstract interface java.lang.Iterable<%0 extends java.lang.Object>
meth public abstract java.util.Iterator<{java.lang.Iterable%0}> iterator()
meth public java.util.Spliterator<{java.lang.Iterable%0}> spliterator()
meth public void forEach(java.util.function.Consumer<? super {java.lang.Iterable%0}>)

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

CLSS public java.lang.RuntimeException
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

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

CLSS public java.security.SecureClassLoader
cons protected init()
cons protected init(java.lang.ClassLoader)
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int,java.security.CodeSource)
meth protected final java.lang.Class<?> defineClass(java.lang.String,java.nio.ByteBuffer,java.security.CodeSource)
meth protected java.security.PermissionCollection getPermissions(java.security.CodeSource)
supr java.lang.ClassLoader

CLSS public abstract interface java.util.concurrent.Future<%0 extends java.lang.Object>
meth public abstract boolean cancel(boolean)
meth public abstract boolean isCancelled()
meth public abstract boolean isDone()
meth public abstract {java.util.concurrent.Future%0} get() throws java.lang.InterruptedException,java.util.concurrent.ExecutionException
meth public abstract {java.util.concurrent.Future%0} get(long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException,java.util.concurrent.TimeoutException

CLSS public abstract interface javax.activation.DataSource
meth public abstract java.io.InputStream getInputStream() throws java.io.IOException
meth public abstract java.io.OutputStream getOutputStream() throws java.io.IOException
meth public abstract java.lang.String getContentType()
meth public abstract java.lang.String getName()

CLSS public abstract interface javax.xml.namespace.NamespaceContext
meth public abstract java.lang.String getNamespaceURI(java.lang.String)
meth public abstract java.lang.String getPrefix(java.lang.String)
meth public abstract java.util.Iterator getPrefixes(java.lang.String)

CLSS public javax.xml.ws.WebServiceException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

CLSS public abstract interface org.xml.sax.ContentHandler
meth public abstract void characters(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void endDocument() throws org.xml.sax.SAXException
meth public abstract void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void setDocumentLocator(org.xml.sax.Locator)
meth public abstract void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void startDocument() throws org.xml.sax.SAXException
meth public abstract void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public abstract void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.DTDHandler
meth public abstract void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.EntityResolver
meth public abstract org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.ErrorHandler
meth public abstract void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.XMLFilter
intf org.xml.sax.XMLReader
meth public abstract org.xml.sax.XMLReader getParent()
meth public abstract void setParent(org.xml.sax.XMLReader)

CLSS public abstract interface org.xml.sax.XMLReader
meth public abstract boolean getFeature(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public abstract java.lang.Object getProperty(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public abstract org.xml.sax.ContentHandler getContentHandler()
meth public abstract org.xml.sax.DTDHandler getDTDHandler()
meth public abstract org.xml.sax.EntityResolver getEntityResolver()
meth public abstract org.xml.sax.ErrorHandler getErrorHandler()
meth public abstract void parse(java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public abstract void parse(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public abstract void setContentHandler(org.xml.sax.ContentHandler)
meth public abstract void setDTDHandler(org.xml.sax.DTDHandler)
meth public abstract void setEntityResolver(org.xml.sax.EntityResolver)
meth public abstract void setErrorHandler(org.xml.sax.ErrorHandler)
meth public abstract void setFeature(java.lang.String,boolean) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public abstract void setProperty(java.lang.String,java.lang.Object) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException

CLSS public org.xml.sax.helpers.XMLFilterImpl
cons public init()
cons public init(org.xml.sax.XMLReader)
intf org.xml.sax.ContentHandler
intf org.xml.sax.DTDHandler
intf org.xml.sax.EntityResolver
intf org.xml.sax.ErrorHandler
intf org.xml.sax.XMLFilter
meth public boolean getFeature(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public java.lang.Object getProperty(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public org.xml.sax.ContentHandler getContentHandler()
meth public org.xml.sax.DTDHandler getDTDHandler()
meth public org.xml.sax.EntityResolver getEntityResolver()
meth public org.xml.sax.ErrorHandler getErrorHandler()
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public org.xml.sax.XMLReader getParent()
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void parse(java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void parse(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setContentHandler(org.xml.sax.ContentHandler)
meth public void setDTDHandler(org.xml.sax.DTDHandler)
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void setEntityResolver(org.xml.sax.EntityResolver)
meth public void setErrorHandler(org.xml.sax.ErrorHandler)
meth public void setFeature(java.lang.String,boolean) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public void setParent(org.xml.sax.XMLReader)
meth public void setProperty(java.lang.String,java.lang.Object) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
supr java.lang.Object

