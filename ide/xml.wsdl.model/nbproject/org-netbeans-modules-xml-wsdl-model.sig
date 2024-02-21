#Signature file v4.1
#Version 1.56.0

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

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface javax.swing.event.UndoableEditListener
intf java.util.EventListener
meth public abstract void undoableEditHappened(javax.swing.event.UndoableEditEvent)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.Binding
fld public final static java.lang.String BINDING_OPERATION_PROPERTY = "operation"
fld public final static java.lang.String TYPE_PROPERTY = "type"
intf org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent
meth public abstract java.util.Collection<org.netbeans.modules.xml.wsdl.model.BindingOperation> getBindingOperations()
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.wsdl.model.PortType> getType()
meth public abstract void addBindingOperation(org.netbeans.modules.xml.wsdl.model.BindingOperation)
meth public abstract void removeBindingOperation(org.netbeans.modules.xml.wsdl.model.BindingOperation)
meth public abstract void setType(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.wsdl.model.PortType>)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.BindingFault
intf org.netbeans.modules.xml.wsdl.model.WSDLComponent
intf org.netbeans.modules.xml.xam.Nameable<org.netbeans.modules.xml.wsdl.model.WSDLComponent>
meth public abstract org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Fault> getFault()
meth public abstract void setFault(org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Fault>)
 anno 0 java.lang.Deprecated()

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.BindingInput
intf org.netbeans.modules.xml.wsdl.model.WSDLComponent
intf org.netbeans.modules.xml.xam.Nameable<org.netbeans.modules.xml.wsdl.model.WSDLComponent>
meth public abstract org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Input> getInput()
meth public abstract void setInput(org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Input>)
 anno 0 java.lang.Deprecated()

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.BindingOperation
fld public final static java.lang.String BINDING_FAULT_PROPERTY = "fault"
fld public final static java.lang.String BINDING_INPUT_PROPERTY = "input"
fld public final static java.lang.String BINDING_OPERATION_PROPERTY = "name"
fld public final static java.lang.String BINDING_OUTPUT_PROPERTY = "output"
intf org.netbeans.modules.xml.wsdl.model.WSDLComponent
intf org.netbeans.modules.xml.xam.Nameable<org.netbeans.modules.xml.wsdl.model.WSDLComponent>
meth public abstract java.util.Collection<org.netbeans.modules.xml.wsdl.model.BindingFault> getBindingFaults()
meth public abstract org.netbeans.modules.xml.wsdl.model.BindingInput getBindingInput()
meth public abstract org.netbeans.modules.xml.wsdl.model.BindingOutput getBindingOutput()
meth public abstract org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Operation> getOperation()
meth public abstract void addBindingFault(org.netbeans.modules.xml.wsdl.model.BindingFault)
meth public abstract void removeBindingFault(org.netbeans.modules.xml.wsdl.model.BindingFault)
meth public abstract void setBindingInput(org.netbeans.modules.xml.wsdl.model.BindingInput)
meth public abstract void setBindingOutput(org.netbeans.modules.xml.wsdl.model.BindingOutput)
meth public abstract void setOperation(org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Operation>)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.BindingOutput
intf org.netbeans.modules.xml.wsdl.model.WSDLComponent
intf org.netbeans.modules.xml.xam.Nameable<org.netbeans.modules.xml.wsdl.model.WSDLComponent>
meth public abstract org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Output> getOutput()
meth public abstract void setOutput(org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Output>)
 anno 0 java.lang.Deprecated()

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.Definitions
fld public final static java.lang.String BINDING_PROPERTY = "binding"
fld public final static java.lang.String IMPORT_PROPERTY = "import"
fld public final static java.lang.String MESSAGE_PROPERTY = "message"
fld public final static java.lang.String PORT_TYPE_PROPERTY = "portType"
fld public final static java.lang.String SERVICE_PROPERTY = "service"
fld public final static java.lang.String TARGET_NAMESPACE_PROPERTY = "targetNamespace"
fld public final static java.lang.String TYPES_PROPERTY = "types"
intf org.netbeans.modules.xml.wsdl.model.WSDLComponent
intf org.netbeans.modules.xml.xam.Nameable<org.netbeans.modules.xml.wsdl.model.WSDLComponent>
meth public abstract java.lang.String getAnyAttribute(javax.xml.namespace.QName)
meth public abstract java.lang.String getTargetNamespace()
meth public abstract java.util.Collection<org.netbeans.modules.xml.wsdl.model.Binding> getBindings()
meth public abstract java.util.Collection<org.netbeans.modules.xml.wsdl.model.Import> getImports()
meth public abstract java.util.Collection<org.netbeans.modules.xml.wsdl.model.Message> getMessages()
meth public abstract java.util.Collection<org.netbeans.modules.xml.wsdl.model.PortType> getPortTypes()
meth public abstract java.util.Collection<org.netbeans.modules.xml.wsdl.model.Service> getServices()
meth public abstract org.netbeans.modules.xml.wsdl.model.Types getTypes()
meth public abstract void addBinding(org.netbeans.modules.xml.wsdl.model.Binding)
meth public abstract void addImport(org.netbeans.modules.xml.wsdl.model.Import)
meth public abstract void addMessage(org.netbeans.modules.xml.wsdl.model.Message)
meth public abstract void addPortType(org.netbeans.modules.xml.wsdl.model.PortType)
meth public abstract void addService(org.netbeans.modules.xml.wsdl.model.Service)
meth public abstract void removeBinding(org.netbeans.modules.xml.wsdl.model.Binding)
meth public abstract void removeImport(org.netbeans.modules.xml.wsdl.model.Import)
meth public abstract void removeMessage(org.netbeans.modules.xml.wsdl.model.Message)
meth public abstract void removePortType(org.netbeans.modules.xml.wsdl.model.PortType)
meth public abstract void removeService(org.netbeans.modules.xml.wsdl.model.Service)
meth public abstract void setAnyAttribute(javax.xml.namespace.QName,java.lang.String)
meth public abstract void setTargetNamespace(java.lang.String)
meth public abstract void setTypes(org.netbeans.modules.xml.wsdl.model.Types)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.Documentation
fld public final static java.lang.String CONTENT_PROPERTY = "content"
intf org.netbeans.modules.xml.wsdl.model.WSDLComponent
meth public abstract java.lang.String getAnyAttribute(javax.xml.namespace.QName)
meth public abstract java.lang.String getContentFragment()
meth public abstract java.lang.String getTextContent()
meth public abstract org.w3c.dom.Element getDocumentationElement()
meth public abstract void setAnyAttribute(javax.xml.namespace.QName,java.lang.String)
meth public abstract void setContentFragment(java.lang.String) throws java.io.IOException
meth public abstract void setDocumentationElement(org.w3c.dom.Element)
meth public abstract void setTextContent(java.lang.String)

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

CLSS public abstract interface static org.netbeans.modules.xml.wsdl.model.ExtensibilityElement$EmbeddedModel
 outer org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract org.netbeans.modules.xml.xam.dom.DocumentModel getEmbeddedModel()

CLSS public abstract interface static org.netbeans.modules.xml.wsdl.model.ExtensibilityElement$ParentSelector
 outer org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract boolean canBeAddedTo(org.netbeans.modules.xml.xam.Component)

CLSS public abstract interface static org.netbeans.modules.xml.wsdl.model.ExtensibilityElement$UpdaterProvider
 outer org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract <%0 extends org.netbeans.modules.xml.wsdl.model.ExtensibilityElement> org.netbeans.modules.xml.xam.ComponentUpdater<{%%0}> getComponentUpdater()

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.Fault
intf org.netbeans.modules.xml.wsdl.model.OperationParameter

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.Import
fld public final static java.lang.String LOCATION_PROPERTY = "location"
fld public final static java.lang.String NAMESPACE_URI_PROPERTY = "namespaceURI"
intf org.netbeans.modules.xml.wsdl.model.WSDLComponent
meth public abstract java.lang.String getLocation()
meth public abstract java.lang.String getNamespace()
meth public abstract org.netbeans.modules.xml.wsdl.model.WSDLModel getImportedWSDLModel() throws org.netbeans.modules.xml.xam.locator.CatalogModelException
meth public abstract void setLocation(java.lang.String)
meth public abstract void setNamespace(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.Input
intf org.netbeans.modules.xml.wsdl.model.OperationParameter

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.Message
fld public final static java.lang.String PART_PROPERTY = "part"
intf org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent
meth public abstract java.util.Collection<org.netbeans.modules.xml.wsdl.model.Part> getParts()
meth public abstract void addPart(org.netbeans.modules.xml.wsdl.model.Part)
meth public abstract void removePart(org.netbeans.modules.xml.wsdl.model.Part)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.NotificationOperation
intf org.netbeans.modules.xml.wsdl.model.Operation
meth public abstract org.netbeans.modules.xml.wsdl.model.Output getOutput()
meth public abstract void setOutput(org.netbeans.modules.xml.wsdl.model.Output)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.OneWayOperation
intf org.netbeans.modules.xml.wsdl.model.Operation
meth public abstract org.netbeans.modules.xml.wsdl.model.Input getInput()
meth public abstract void setInput(org.netbeans.modules.xml.wsdl.model.Input)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.Operation
fld public final static java.lang.String FAULT_PROPERTY = "fault"
fld public final static java.lang.String INPUT_PROPERTY = "input"
fld public final static java.lang.String OUTPUT_PROPERTY = "output"
fld public final static java.lang.String PARAMETER_ORDER_PROPERTY = "parameterOrder"
intf org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent
intf org.netbeans.modules.xml.wsdl.model.WSDLComponent
intf org.netbeans.modules.xml.xam.Nameable<org.netbeans.modules.xml.wsdl.model.WSDLComponent>
meth public abstract java.util.Collection<org.netbeans.modules.xml.wsdl.model.Fault> getFaults()
meth public abstract java.util.List<java.lang.String> getParameterOrder()
meth public abstract org.netbeans.modules.xml.wsdl.model.Input getInput()
meth public abstract org.netbeans.modules.xml.wsdl.model.Output getOutput()
meth public abstract void addFault(org.netbeans.modules.xml.wsdl.model.Fault)
meth public abstract void removeFault(org.netbeans.modules.xml.wsdl.model.Fault)
meth public abstract void setInput(org.netbeans.modules.xml.wsdl.model.Input)
meth public abstract void setOutput(org.netbeans.modules.xml.wsdl.model.Output)
meth public abstract void setParameterOrder(java.util.List<java.lang.String>)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.OperationParameter
fld public final static java.lang.String MESSAGE_PROPERTY = "message"
intf org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent
intf org.netbeans.modules.xml.wsdl.model.WSDLComponent
intf org.netbeans.modules.xml.xam.Nameable<org.netbeans.modules.xml.wsdl.model.WSDLComponent>
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.wsdl.model.Message> getMessage()
meth public abstract void setMessage(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.wsdl.model.Message>)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.Output
intf org.netbeans.modules.xml.wsdl.model.OperationParameter

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.Part
fld public final static java.lang.String ELEMENT_PROPERTY = "element"
fld public final static java.lang.String TYPE_PROPERTY = "type"
intf org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent
intf org.netbeans.modules.xml.xam.Nameable<org.netbeans.modules.xml.wsdl.model.WSDLComponent>
meth public abstract java.lang.String getAnyAttribute(javax.xml.namespace.QName)
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalElement> getElement()
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalType> getType()
meth public abstract void setAnyAttribute(javax.xml.namespace.QName,java.lang.String)
meth public abstract void setElement(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalElement>)
meth public abstract void setType(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalType>)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.Port
fld public final static java.lang.String BINDING_PROPERTY = "binding"
intf org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent
intf org.netbeans.modules.xml.xam.Nameable<org.netbeans.modules.xml.wsdl.model.WSDLComponent>
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.wsdl.model.Binding> getBinding()
meth public abstract void setBinding(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.wsdl.model.Binding>)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.PortType
fld public final static java.lang.String OPERATION_PROPERTY = "operation"
intf org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent
meth public abstract java.util.Collection<org.netbeans.modules.xml.wsdl.model.Operation> getOperations()
meth public abstract void addOperation(org.netbeans.modules.xml.wsdl.model.Operation)
meth public abstract void removeOperation(org.netbeans.modules.xml.wsdl.model.Operation)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.ReferenceableExtensibilityElement
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
intf org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent
intf org.netbeans.modules.xml.wsdl.model.WSDLComponent
intf org.netbeans.modules.xml.xam.Nameable<org.netbeans.modules.xml.wsdl.model.WSDLComponent>
intf org.netbeans.modules.xml.xam.NamedReferenceable<org.netbeans.modules.xml.wsdl.model.WSDLComponent>

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.RequestResponseOperation
intf org.netbeans.modules.xml.wsdl.model.Operation
meth public abstract org.netbeans.modules.xml.wsdl.model.Input getInput()
meth public abstract org.netbeans.modules.xml.wsdl.model.Output getOutput()
meth public abstract void setInput(org.netbeans.modules.xml.wsdl.model.Input)
meth public abstract void setOutput(org.netbeans.modules.xml.wsdl.model.Output)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.Service
fld public final static java.lang.String PORT_PROPERTY = "port"
intf org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent
intf org.netbeans.modules.xml.xam.Nameable<org.netbeans.modules.xml.wsdl.model.WSDLComponent>
meth public abstract java.util.Collection<org.netbeans.modules.xml.wsdl.model.Port> getPorts()
meth public abstract void addPort(org.netbeans.modules.xml.wsdl.model.Port)
meth public abstract void removePort(org.netbeans.modules.xml.wsdl.model.Port)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation
intf org.netbeans.modules.xml.wsdl.model.Operation
meth public abstract org.netbeans.modules.xml.wsdl.model.Input getInput()
meth public abstract org.netbeans.modules.xml.wsdl.model.Output getOutput()
meth public abstract void setInput(org.netbeans.modules.xml.wsdl.model.Input)
meth public abstract void setOutput(org.netbeans.modules.xml.wsdl.model.Output)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.Types
intf org.netbeans.modules.xml.wsdl.model.WSDLComponent
intf org.netbeans.modules.xml.xam.EmbeddableRoot$ForeignParent
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.Schema> getSchemas()

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

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory
intf org.netbeans.modules.xml.xam.dom.ComponentFactory<org.netbeans.modules.xml.wsdl.model.WSDLComponent>
meth public abstract org.netbeans.modules.xml.wsdl.model.Binding createBinding()
meth public abstract org.netbeans.modules.xml.wsdl.model.BindingFault createBindingFault()
meth public abstract org.netbeans.modules.xml.wsdl.model.BindingInput createBindingInput()
meth public abstract org.netbeans.modules.xml.wsdl.model.BindingOperation createBindingOperation()
meth public abstract org.netbeans.modules.xml.wsdl.model.BindingOutput createBindingOutput()
meth public abstract org.netbeans.modules.xml.wsdl.model.Documentation createDocumentation()
meth public abstract org.netbeans.modules.xml.wsdl.model.Fault createFault()
meth public abstract org.netbeans.modules.xml.wsdl.model.Import createImport()
meth public abstract org.netbeans.modules.xml.wsdl.model.Input createInput()
meth public abstract org.netbeans.modules.xml.wsdl.model.Message createMessage()
meth public abstract org.netbeans.modules.xml.wsdl.model.NotificationOperation createNotificationOperation()
meth public abstract org.netbeans.modules.xml.wsdl.model.OneWayOperation createOneWayOperation()
meth public abstract org.netbeans.modules.xml.wsdl.model.Output createOutput()
meth public abstract org.netbeans.modules.xml.wsdl.model.Part createPart()
meth public abstract org.netbeans.modules.xml.wsdl.model.Port createPort()
meth public abstract org.netbeans.modules.xml.wsdl.model.PortType createPortType()
meth public abstract org.netbeans.modules.xml.wsdl.model.RequestResponseOperation createRequestResponseOperation()
meth public abstract org.netbeans.modules.xml.wsdl.model.Service createService()
meth public abstract org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation createSolicitResponseOperation()
meth public abstract org.netbeans.modules.xml.wsdl.model.Types createTypes()
meth public abstract org.netbeans.modules.xml.wsdl.model.WSDLComponent create(org.netbeans.modules.xml.wsdl.model.WSDLComponent,javax.xml.namespace.QName)
meth public abstract org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress createSOAPAddress()
meth public abstract org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding createSOAPBinding()
meth public abstract org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody createSOAPBody()
meth public abstract org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault createSOAPFault()
meth public abstract org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader createSOAPHeader()
meth public abstract org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault createSOAPHeaderFault()
meth public abstract org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation createSOAPOperation()
meth public abstract org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema createWSDLSchema()

CLSS public abstract org.netbeans.modules.xml.wsdl.model.WSDLModel
cons protected init(org.netbeans.modules.xml.xam.ModelSource)
intf org.netbeans.modules.xml.xam.Referenceable
meth public abstract <%0 extends org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent> {%%0} findComponentByName(java.lang.String,java.lang.Class<{%%0}>)
meth public abstract <%0 extends org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent> {%%0} findComponentByName(javax.xml.namespace.QName,java.lang.Class<{%%0}>)
meth public abstract java.util.List<org.netbeans.modules.xml.schema.model.Schema> findSchemas(java.lang.String)
meth public abstract java.util.List<org.netbeans.modules.xml.wsdl.model.WSDLModel> findWSDLModel(java.lang.String)
meth public abstract org.netbeans.modules.xml.wsdl.model.Definitions getDefinitions()
meth public abstract org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory getFactory()
supr org.netbeans.modules.xml.xam.dom.AbstractDocumentModel<org.netbeans.modules.xml.wsdl.model.WSDLComponent>

CLSS public org.netbeans.modules.xml.wsdl.model.WSDLModelFactory
meth protected org.netbeans.modules.xml.wsdl.model.WSDLModel createModel(org.netbeans.modules.xml.xam.ModelSource)
meth public org.netbeans.modules.xml.wsdl.model.WSDLModel getModel(org.netbeans.modules.xml.xam.ModelSource)
meth public static org.netbeans.modules.xml.wsdl.model.WSDLModelFactory getDefault()
supr org.netbeans.modules.xml.xam.AbstractModelFactory<org.netbeans.modules.xml.wsdl.model.WSDLModel>
hfds wsdlModelFactory

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPAddress
fld public final static java.lang.String LOCATION_PROPERTY = "location"
intf org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPComponent
meth public abstract java.lang.String getLocation()
meth public abstract void setLocation(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPBinding
fld public final static java.lang.String VERB_PROPERTY = "verb"
innr public final static !enum Verb
intf org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPComponent
meth public abstract org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPBinding$Verb getVerb()
meth public abstract void setVerb(org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPBinding$Verb)

CLSS public final static !enum org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPBinding$Verb
 outer org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPBinding
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPBinding$Verb GET
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPBinding$Verb POST
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPBinding$Verb valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPBinding$Verb[] values()
supr java.lang.Enum<org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPBinding$Verb>
hfds tag

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPComponent
innr public abstract interface static Visitor
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract void accept(org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPComponent$Visitor)

CLSS public abstract interface static org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPComponent$Visitor
 outer org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPComponent
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPAddress)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPBinding)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPOperation)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPUrlEncoded)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPUrlReplacement)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPOperation
fld public final static java.lang.String LOCATION_PROPERTY = "location"
intf org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPComponent
meth public abstract java.lang.String getLocation()
meth public abstract void setLocation(java.lang.String)

CLSS public final !enum org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPQName
fld public final static java.lang.String HTTP_NS_PREFIX = "http"
fld public final static java.lang.String HTTP_NS_URI = "http://schemas.xmlsoap.org/wsdl/http/"
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPQName ADDRESS
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPQName BINDING
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPQName OPERATION
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPQName URLENCODED
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPQName URLREPLACEMENT
meth public javax.xml.namespace.QName getQName()
meth public static java.util.Set<javax.xml.namespace.QName> getQNames()
meth public static javax.xml.namespace.QName createHTTPQName(java.lang.String)
meth public static org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPQName valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPQName[] values()
supr java.lang.Enum<org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPQName>
hfds qName,qnames

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPUrlEncoded
intf org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPComponent

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPUrlReplacement
intf org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPComponent

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress
fld public final static java.lang.String LOCATION_PROPERTY = "location"
intf org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent
meth public abstract java.lang.String getLocation()
meth public abstract void setLocation(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding
fld public final static java.lang.String STYLE_PROPERTY = "style"
fld public final static java.lang.String TRANSPORT_URI_PROPERTY = "transportURI"
innr public final static !enum Style
intf org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent
meth public abstract java.lang.String getTransportURI()
meth public abstract org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding$Style getStyle()
meth public abstract void setStyle(org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding$Style)
meth public abstract void setTransportURI(java.lang.String)

CLSS public final static !enum org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding$Style
 outer org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding$Style DOCUMENT
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding$Style RPC
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding$Style valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding$Style[] values()
supr java.lang.Enum<org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding$Style>
hfds tag

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody
fld public final static java.lang.String PARTS_PROPERTY = "parts"
intf org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase
meth public abstract java.util.List<java.lang.String> getParts()
meth public abstract java.util.List<org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Part>> getPartRefs()
meth public abstract void addPart(int,java.lang.String)
meth public abstract void addPart(java.lang.String)
meth public abstract void addPartRef(int,org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Part>)
meth public abstract void addPartRef(org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Part>)
meth public abstract void removePart(java.lang.String)
meth public abstract void removePartRef(org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Part>)
meth public abstract void setPartRefs(java.util.List<org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Part>>)
meth public abstract void setParts(java.util.List<java.lang.String>)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent
innr public abstract interface static Visitor
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract void accept(org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent$Visitor)

CLSS public abstract interface static org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent$Visitor
 outer org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault
fld public final static java.lang.String NAME_PROPERTY = "name"
intf org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Fault> getFault()
meth public abstract void setFault(org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Fault>)
meth public abstract void setName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader
fld public final static java.lang.String HEADER_FAULT_PROPERTY = "headerFault"
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement$UpdaterProvider
intf org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderBase
meth public abstract java.util.Collection<org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault> getSOAPHeaderFaults()
meth public abstract void addSOAPHeaderFault(org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault)
meth public abstract void removeSOAPHeaderFault(org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderBase
fld public final static java.lang.String MESSAGE_PROPERTY = "message"
fld public final static java.lang.String PART_PROPERTY = "part"
intf org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase
meth public abstract java.lang.String getPart()
meth public abstract org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Part> getPartRef()
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.wsdl.model.Message> getMessage()
meth public abstract void setMessage(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.wsdl.model.Message>)
meth public abstract void setPart(java.lang.String)
meth public abstract void setPartRef(org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Part>)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault
intf org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderBase

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase
fld public final static java.lang.String ENCODING_STYLE_PROPERTY = "encodingStyle"
fld public final static java.lang.String NAMESPACE_PROPERTY = "namespace"
fld public final static java.lang.String USE_PROPERTY = "use"
innr public final static !enum Use
intf org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent
meth public abstract java.lang.String getNamespace()
meth public abstract java.util.Collection<java.lang.String> getEncodingStyles()
meth public abstract org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase$Use getUse()
meth public abstract void addEncodingStyle(java.lang.String)
meth public abstract void removeEncodingStyle(java.lang.String)
meth public abstract void setNamespace(java.lang.String)
meth public abstract void setUse(org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase$Use)

CLSS public final static !enum org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase$Use
 outer org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase$Use ENCODED
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase$Use LITERAL
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase$Use valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase$Use[] values()
supr java.lang.Enum<org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase$Use>
hfds tag

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation
fld public final static java.lang.String SOAP_ACTION_PROPERTY = "soapAction"
fld public final static java.lang.String STYLE_PROPERTY = "style"
intf org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent
meth public abstract java.lang.String getSoapAction()
meth public abstract org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding$Style getStyle()
meth public abstract void setSoapAction(java.lang.String)
meth public abstract void setStyle(org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding$Style)

CLSS public final !enum org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName
fld public final static java.lang.String SOAP_NS_PREFIX = "soap"
fld public final static java.lang.String SOAP_NS_URI = "http://schemas.xmlsoap.org/wsdl/soap/"
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName ADDRESS
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName BINDING
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName BODY
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName FAULT
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName HEADER
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName HEADER_FAULT
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName OPERATION
meth public javax.xml.namespace.QName getQName()
meth public static java.util.Set<javax.xml.namespace.QName> getQNames()
meth public static javax.xml.namespace.QName createSOAPQName(java.lang.String)
meth public static org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName[] values()
supr java.lang.Enum<org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName>
hfds qName,qnames

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Address
fld public final static java.lang.String LOCATION_PROPERTY = "location"
intf org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Component
meth public abstract java.lang.String getLocation()
meth public abstract void setLocation(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Binding
fld public final static java.lang.String STYLE_PROPERTY = "style"
fld public final static java.lang.String TRANSPORT_URI_PROPERTY = "transportURI"
innr public final static !enum Style
intf org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Component
meth public abstract java.lang.String getTransportURI()
meth public abstract org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Binding$Style getStyle()
meth public abstract void setStyle(org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Binding$Style)
meth public abstract void setTransportURI(java.lang.String)

CLSS public final static !enum org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Binding$Style
 outer org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Binding
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Binding$Style DOCUMENT
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Binding$Style RPC
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Binding$Style valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Binding$Style[] values()
supr java.lang.Enum<org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Binding$Style>
hfds tag

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Body
fld public final static java.lang.String PARTS_PROPERTY = "parts"
intf org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12MessageBase
meth public abstract java.util.List<java.lang.String> getParts()
meth public abstract java.util.List<org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Part>> getPartRefs()
meth public abstract void addPart(int,java.lang.String)
meth public abstract void addPart(java.lang.String)
meth public abstract void addPartRef(int,org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Part>)
meth public abstract void addPartRef(org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Part>)
meth public abstract void removePart(java.lang.String)
meth public abstract void removePartRef(org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Part>)
meth public abstract void setPartRefs(java.util.List<org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Part>>)
meth public abstract void setParts(java.util.List<java.lang.String>)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Component
innr public abstract interface static Visitor
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
meth public abstract void accept(org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Component$Visitor)

CLSS public abstract interface static org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Component$Visitor
 outer org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Component
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Address)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Binding)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Body)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Fault)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Header)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12HeaderFault)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Operation)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Fault
fld public final static java.lang.String NAME_PROPERTY = "name"
intf org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12MessageBase
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Fault> getFault()
meth public abstract void setFault(org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Fault>)
meth public abstract void setName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Header
fld public final static java.lang.String HEADER_FAULT_PROPERTY = "headerFault"
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement$UpdaterProvider
intf org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12HeaderBase
meth public abstract java.util.Collection<org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12HeaderFault> getSOAPHeaderFaults()
meth public abstract void addSOAPHeaderFault(org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12HeaderFault)
meth public abstract void removeSOAPHeaderFault(org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12HeaderFault)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12HeaderBase
fld public final static java.lang.String MESSAGE_PROPERTY = "message"
fld public final static java.lang.String PART_PROPERTY = "part"
intf org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12MessageBase
meth public abstract java.lang.String getPart()
meth public abstract org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Part> getPartRef()
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.wsdl.model.Message> getMessage()
meth public abstract void setMessage(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.wsdl.model.Message>)
meth public abstract void setPart(java.lang.String)
meth public abstract void setPartRef(org.netbeans.modules.xml.xam.Reference<org.netbeans.modules.xml.wsdl.model.Part>)

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12HeaderFault
intf org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12HeaderBase

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12MessageBase
fld public final static java.lang.String ENCODING_STYLE_PROPERTY = "encodingStyle"
fld public final static java.lang.String NAMESPACE_PROPERTY = "namespace"
fld public final static java.lang.String USE_PROPERTY = "use"
innr public final static !enum Use
intf org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Component
meth public abstract java.lang.String getNamespace()
meth public abstract java.util.Collection<java.lang.String> getEncodingStyles()
meth public abstract org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12MessageBase$Use getUse()
meth public abstract void addEncodingStyle(java.lang.String)
meth public abstract void removeEncodingStyle(java.lang.String)
meth public abstract void setNamespace(java.lang.String)
meth public abstract void setUse(org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12MessageBase$Use)

CLSS public final static !enum org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12MessageBase$Use
 outer org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12MessageBase
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12MessageBase$Use ENCODED
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12MessageBase$Use LITERAL
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12MessageBase$Use valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12MessageBase$Use[] values()
supr java.lang.Enum<org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12MessageBase$Use>
hfds tag

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Operation
fld public final static java.lang.String SOAP_ACTION_PROPERTY = "soapAction"
fld public final static java.lang.String SOAP_ACTION_REQUIRED_PROPERTY = "soapActionRequired"
fld public final static java.lang.String STYLE_PROPERTY = "style"
intf org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Component
meth public abstract java.lang.String getSoapAction()
meth public abstract java.lang.String getSoapActionRequired()
meth public abstract org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Binding$Style getStyle()
meth public abstract void setSoapAction(java.lang.String)
meth public abstract void setSoapActionRequired(java.lang.String)
meth public abstract void setStyle(org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Binding$Style)

CLSS public final !enum org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12QName
fld public final static java.lang.String SOAP_NS_PREFIX = "soap12"
fld public final static java.lang.String SOAP_NS_URI = "http://schemas.xmlsoap.org/wsdl/soap12/"
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12QName ADDRESS
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12QName BINDING
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12QName BODY
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12QName FAULT
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12QName HEADER
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12QName HEADER_FAULT
fld public final static org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12QName OPERATION
meth public javax.xml.namespace.QName getQName()
meth public static java.util.Set<javax.xml.namespace.QName> getQNames()
meth public static javax.xml.namespace.QName createSOAPQName(java.lang.String)
meth public static org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12QName valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12QName[] values()
supr java.lang.Enum<org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12QName>
hfds qName,qnames

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement$EmbeddedModel
meth public abstract org.netbeans.modules.xml.schema.model.SchemaModel getSchemaModel()

CLSS public abstract org.netbeans.modules.xml.wsdl.model.spi.ElementFactory
cons public init()
meth public abstract java.util.Set<javax.xml.namespace.QName> getElementQNames()
meth public abstract org.netbeans.modules.xml.wsdl.model.WSDLComponent create(org.netbeans.modules.xml.wsdl.model.WSDLComponent,org.w3c.dom.Element)
supr java.lang.Object

CLSS public org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement
cons public init(org.netbeans.modules.xml.wsdl.model.WSDLModel,javax.xml.namespace.QName)
cons public init(org.netbeans.modules.xml.wsdl.model.WSDLModel,org.w3c.dom.Element)
innr public static StringAttribute
intf org.netbeans.modules.xml.wsdl.model.ExtensibilityElement$ParentSelector
meth public boolean canBeAddedTo(org.netbeans.modules.xml.xam.Component)
meth public java.lang.String getAttribute(java.lang.String)
meth public java.lang.String getContentFragment()
meth public java.util.List<org.netbeans.modules.xml.wsdl.model.ExtensibilityElement> getAnyElements()
meth public void accept(org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor)
meth public void addAnyElement(org.netbeans.modules.xml.wsdl.model.ExtensibilityElement,int)
meth public void removeAnyElement(org.netbeans.modules.xml.wsdl.model.ExtensibilityElement)
meth public void setAttribute(java.lang.String,java.lang.String)
meth public void setContentFragment(java.lang.String) throws java.io.IOException
supr org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase

CLSS public static org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement$StringAttribute
 outer org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement
cons public init(java.lang.String)
intf org.netbeans.modules.xml.xam.dom.Attribute
meth public java.lang.Class getMemberType()
meth public java.lang.Class getType()
meth public java.lang.String getName()
supr java.lang.Object
hfds name

CLSS public abstract org.netbeans.modules.xml.wsdl.model.spi.NamedExtensibilityElementBase
cons public init(org.netbeans.modules.xml.wsdl.model.WSDLModel,org.w3c.dom.Element)
intf org.netbeans.modules.xml.xam.Nameable<org.netbeans.modules.xml.wsdl.model.WSDLComponent>
meth public java.lang.String getName()
meth public void setName(java.lang.String)
supr org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement

CLSS public abstract org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase
cons public init(org.netbeans.modules.xml.wsdl.model.WSDLModel,org.w3c.dom.Element)
intf org.netbeans.modules.xml.wsdl.model.WSDLComponent
meth protected <%0 extends org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent> org.netbeans.modules.xml.xam.dom.NamedComponentReference<{%%0}> resolveSchemaReference(java.lang.Class<{%%0}>,org.netbeans.modules.xml.xam.dom.Attribute)
meth protected <%0 extends org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent> org.netbeans.modules.xml.xam.dom.NamedComponentReference<{%%0}> resolveGlobalReference(java.lang.Class<{%%0}>,org.netbeans.modules.xml.xam.dom.Attribute)
meth protected java.lang.Object getAttributeValueOf(org.netbeans.modules.xml.xam.dom.Attribute,java.lang.String)
meth protected static org.w3c.dom.Element createNewElement(javax.xml.namespace.QName,org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth protected static org.w3c.dom.Element createPrefixedElement(javax.xml.namespace.QName,org.netbeans.modules.xml.wsdl.model.WSDLModel)
meth protected void populateChildren(java.util.List<org.netbeans.modules.xml.wsdl.model.WSDLComponent>)
meth public <%0 extends org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent> org.netbeans.modules.xml.xam.dom.NamedComponentReference<{%%0}> createSchemaReference({%%0},java.lang.Class<{%%0}>)
meth public <%0 extends org.netbeans.modules.xml.wsdl.model.ExtensibilityElement> java.util.List<{%%0}> getExtensibilityElements(java.lang.Class<{%%0}>)
meth public <%0 extends org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent> org.netbeans.modules.xml.xam.dom.NamedComponentReference<{%%0}> createReferenceTo({%%0},java.lang.Class<{%%0}>)
meth public boolean canPaste(org.netbeans.modules.xml.xam.Component)
meth public java.lang.String toString(javax.xml.namespace.QName)
meth public java.util.List<org.netbeans.modules.xml.wsdl.model.ExtensibilityElement> getExtensibilityElements()
meth public org.netbeans.modules.xml.wsdl.model.Documentation getDocumentation()
meth public org.netbeans.modules.xml.wsdl.model.WSDLModel getModel()
meth public org.netbeans.modules.xml.wsdl.model.WSDLModel getWSDLModel()
meth public void addExtensibilityElement(org.netbeans.modules.xml.wsdl.model.ExtensibilityElement)
meth public void removeExtensibilityElement(org.netbeans.modules.xml.wsdl.model.ExtensibilityElement)
meth public void setDocumentation(org.netbeans.modules.xml.wsdl.model.Documentation)
supr org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent<org.netbeans.modules.xml.wsdl.model.WSDLComponent>

CLSS public org.netbeans.modules.xml.wsdl.model.visitor.ChildVisitor
cons public init()
meth protected void visitComponent(org.netbeans.modules.xml.wsdl.model.WSDLComponent)
supr org.netbeans.modules.xml.wsdl.model.visitor.DefaultVisitor

CLSS public org.netbeans.modules.xml.wsdl.model.visitor.DefaultVisitor
cons public init()
intf org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor
meth protected void visitComponent(org.netbeans.modules.xml.wsdl.model.WSDLComponent)
meth public void visit(org.netbeans.modules.xml.wsdl.model.Binding)
meth public void visit(org.netbeans.modules.xml.wsdl.model.BindingFault)
meth public void visit(org.netbeans.modules.xml.wsdl.model.BindingInput)
meth public void visit(org.netbeans.modules.xml.wsdl.model.BindingOperation)
meth public void visit(org.netbeans.modules.xml.wsdl.model.BindingOutput)
meth public void visit(org.netbeans.modules.xml.wsdl.model.Definitions)
meth public void visit(org.netbeans.modules.xml.wsdl.model.Documentation)
meth public void visit(org.netbeans.modules.xml.wsdl.model.ExtensibilityElement)
meth public void visit(org.netbeans.modules.xml.wsdl.model.Fault)
meth public void visit(org.netbeans.modules.xml.wsdl.model.Import)
meth public void visit(org.netbeans.modules.xml.wsdl.model.Input)
meth public void visit(org.netbeans.modules.xml.wsdl.model.Message)
meth public void visit(org.netbeans.modules.xml.wsdl.model.NotificationOperation)
meth public void visit(org.netbeans.modules.xml.wsdl.model.OneWayOperation)
meth public void visit(org.netbeans.modules.xml.wsdl.model.Output)
meth public void visit(org.netbeans.modules.xml.wsdl.model.Part)
meth public void visit(org.netbeans.modules.xml.wsdl.model.Port)
meth public void visit(org.netbeans.modules.xml.wsdl.model.PortType)
meth public void visit(org.netbeans.modules.xml.wsdl.model.RequestResponseOperation)
meth public void visit(org.netbeans.modules.xml.wsdl.model.Service)
meth public void visit(org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation)
meth public void visit(org.netbeans.modules.xml.wsdl.model.Types)
supr java.lang.Object

CLSS public org.netbeans.modules.xml.wsdl.model.visitor.FindReferencedVisitor<%0 extends org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent>
cons public init(org.netbeans.modules.xml.wsdl.model.Definitions)
meth public void visit(org.netbeans.modules.xml.wsdl.model.Binding)
meth public void visit(org.netbeans.modules.xml.wsdl.model.ExtensibilityElement)
meth public void visit(org.netbeans.modules.xml.wsdl.model.Message)
meth public void visit(org.netbeans.modules.xml.wsdl.model.NotificationOperation)
meth public void visit(org.netbeans.modules.xml.wsdl.model.OneWayOperation)
meth public void visit(org.netbeans.modules.xml.wsdl.model.Part)
meth public void visit(org.netbeans.modules.xml.wsdl.model.Port)
meth public void visit(org.netbeans.modules.xml.wsdl.model.PortType)
meth public void visit(org.netbeans.modules.xml.wsdl.model.RequestResponseOperation)
meth public void visit(org.netbeans.modules.xml.wsdl.model.Service)
meth public void visit(org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation)
meth public {org.netbeans.modules.xml.wsdl.model.visitor.FindReferencedVisitor%0} find(java.lang.String,java.lang.Class<{org.netbeans.modules.xml.wsdl.model.visitor.FindReferencedVisitor%0}>)
supr org.netbeans.modules.xml.wsdl.model.visitor.DefaultVisitor
hfds localName,referenced,root,type

CLSS public org.netbeans.modules.xml.wsdl.model.visitor.FindWSDLComponent
cons public init()
meth protected void visitComponent(org.netbeans.modules.xml.wsdl.model.WSDLComponent)
meth public org.netbeans.modules.xml.wsdl.model.WSDLComponent findComponent(org.netbeans.modules.xml.wsdl.model.WSDLComponent,java.lang.String)
meth public org.netbeans.modules.xml.wsdl.model.WSDLComponent findComponent(org.netbeans.modules.xml.wsdl.model.WSDLComponent,org.w3c.dom.Element)
meth public static <%0 extends org.netbeans.modules.xml.wsdl.model.WSDLComponent> {%%0} findComponent(java.lang.Class<{%%0}>,org.netbeans.modules.xml.wsdl.model.WSDLComponent,java.lang.String)
supr org.netbeans.modules.xml.wsdl.model.visitor.ChildVisitor
hfds result,xmlNode

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.visitor.WSDLModelVisitor
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.WSDLModel)

CLSS public org.netbeans.modules.xml.wsdl.model.visitor.WSDLUtilities
meth public static void visitRecursively(org.netbeans.modules.xml.wsdl.model.WSDLModel,org.netbeans.modules.xml.wsdl.model.visitor.WSDLModelVisitor)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.Binding)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.BindingFault)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.BindingInput)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.BindingOperation)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.BindingOutput)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.Definitions)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.Documentation)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.ExtensibilityElement)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.Fault)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.Import)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.Input)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.Message)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.NotificationOperation)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.OneWayOperation)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.Output)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.Part)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.Port)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.PortType)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.RequestResponseOperation)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.Service)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation)
meth public abstract void visit(org.netbeans.modules.xml.wsdl.model.Types)

CLSS public abstract org.netbeans.modules.xml.wsdl.validator.spi.ValidatorSchemaFactory
cons public init()
meth public abstract java.lang.String getNamespaceURI()
meth public abstract javax.xml.transform.Source getSchemaSource()
meth public org.w3c.dom.ls.LSResourceResolver getLSResourceResolver()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.xml.xam.AbstractComponent<%0 extends org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.AbstractComponent%0}>>
cons public init(org.netbeans.modules.xml.xam.AbstractModel)
intf org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.AbstractComponent%0}>
meth protected <%0 extends {org.netbeans.modules.xml.xam.AbstractComponent%0}> {%%0} getChild(java.lang.Class<{%%0}>)
meth protected abstract void appendChildQuietly({org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.List<{org.netbeans.modules.xml.xam.AbstractComponent%0}>)
meth protected abstract void insertAtIndexQuietly({org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.List<{org.netbeans.modules.xml.xam.AbstractComponent%0}>,int)
meth protected abstract void populateChildren(java.util.List<{org.netbeans.modules.xml.xam.AbstractComponent%0}>)
meth protected abstract void removeChildQuietly({org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.List<{org.netbeans.modules.xml.xam.AbstractComponent%0}>)
meth protected final boolean isChildrenInitialized()
meth protected void addAfter(java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>)
meth protected void addBefore(java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>)
meth protected void appendChild(java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0})
meth protected void checkNullOrDuplicateChild({org.netbeans.modules.xml.xam.AbstractComponent%0})
meth protected void fireChildAdded()
meth protected void fireChildRemoved()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void fireValueChanged()
meth protected void insertAtIndex(java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},int,java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>)
meth protected void setChild(java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>,java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>)
meth protected void setChild(java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>,java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>,boolean)
meth protected void setChildAfter(java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>,java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>)
meth protected void setChildBefore(java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>,java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>)
meth protected void setModel(org.netbeans.modules.xml.xam.AbstractModel)
meth protected void setParent({org.netbeans.modules.xml.xam.AbstractComponent%0})
meth protected void verifyWrite()
meth public <%0 extends {org.netbeans.modules.xml.xam.AbstractComponent%0}> java.util.List<{%%0}> getChildren(java.lang.Class<{%%0}>)
meth public boolean canPaste(org.netbeans.modules.xml.xam.Component)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public int getChildrenCount()
meth public java.util.List<{org.netbeans.modules.xml.xam.AbstractComponent%0}> getChildren()
meth public java.util.List<{org.netbeans.modules.xml.xam.AbstractComponent%0}> getChildren(java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>)
meth public org.netbeans.modules.xml.xam.AbstractModel getModel()
meth public void checkChildrenPopulated()
meth public void insertAtIndex(java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},int)
meth public void removeChild(java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0})
meth public void removeComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public {org.netbeans.modules.xml.xam.AbstractComponent%0} getParent()
supr java.lang.Object
hfds children,model,parent
hcls DelegateListener

CLSS public abstract org.netbeans.modules.xml.xam.AbstractModel<%0 extends org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.AbstractModel%0}>>
cons public init(org.netbeans.modules.xml.xam.ModelSource)
fld protected org.netbeans.modules.xml.xam.AbstractModel$ModelUndoableEditSupport ues
innr protected ModelUndoableEdit
innr protected ModelUndoableEditSupport
intf javax.swing.event.UndoableEditListener
intf org.netbeans.modules.xml.xam.Model<{org.netbeans.modules.xml.xam.AbstractModel%0}>
meth protected boolean needsSync()
meth protected javax.swing.undo.CompoundEdit createModelUndoableEdit()
meth protected void endTransaction(boolean)
meth protected void finishTransaction()
meth protected void refresh()
meth protected void setInSync(boolean)
meth protected void setInUndoRedo(boolean)
meth protected void setState(org.netbeans.modules.xml.xam.Model$State)
meth protected void syncCompleted()
meth protected void syncStarted()
meth protected void transactionCompleted()
meth protected void transactionStarted()
meth public abstract org.netbeans.modules.xml.xam.ModelAccess getAccess()
meth public boolean inSync()
meth public boolean inUndoRedo()
meth public boolean isAutoSyncActive()
meth public boolean isIntransaction()
meth public boolean startTransaction()
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
meth public boolean startedFiringEvents()
meth public org.netbeans.modules.xml.xam.Model$State getState()
meth public org.netbeans.modules.xml.xam.ModelSource getModelSource()
meth public void addComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void addUndoableRefactorListener(javax.swing.event.UndoableEditListener)
meth public void endTransaction()
meth public void fireComponentChangedEvent(org.netbeans.modules.xml.xam.ComponentEvent)
meth public void firePropertyChangeEvent(java.beans.PropertyChangeEvent)
meth public void removeComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void removeUndoableRefactorListener(javax.swing.event.UndoableEditListener)
meth public void rollbackTransaction()
meth public void setAutoSyncActive(boolean)
meth public void sync() throws java.io.IOException
meth public void undoableEditHappened(javax.swing.event.UndoableEditEvent)
meth public void validateWrite()
supr java.lang.Object
hfds RP,componentListeners,inSync,inUndoRedo,logger,pcs,savedUndoableEditListeners,source,status,transaction
hcls Transaction

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

CLSS public abstract interface org.netbeans.modules.xml.xam.EmbeddableRoot
innr public abstract interface static ForeignParent
meth public abstract org.netbeans.modules.xml.xam.Component getForeignParent()
meth public abstract void setForeignParent(org.netbeans.modules.xml.xam.Component)

CLSS public abstract interface static org.netbeans.modules.xml.xam.EmbeddableRoot$ForeignParent
 outer org.netbeans.modules.xml.xam.EmbeddableRoot
meth public abstract java.util.List<org.netbeans.modules.xml.xam.EmbeddableRoot> getAdoptedChildren()

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

CLSS public abstract org.netbeans.modules.xml.xam.ModelAccess
cons public init()
meth public abstract org.netbeans.modules.xml.xam.Model$State sync() throws java.io.IOException
meth public abstract void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void finishUndoRedo()
meth public abstract void flush()
meth public abstract void prepareForUndoRedo()
meth public abstract void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public boolean isAutoSync()
meth public long dirtyIntervalMillis()
meth public void prepareSync()
meth public void setAutoSync(boolean)
meth public void unsetDirty()
supr java.lang.Object
hfds autoSync

CLSS public abstract interface org.netbeans.modules.xml.xam.Nameable<%0 extends org.netbeans.modules.xml.xam.Component>
intf org.netbeans.modules.xml.xam.Named<{org.netbeans.modules.xml.xam.Nameable%0}>
meth public abstract void setName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.xam.Named<%0 extends org.netbeans.modules.xml.xam.Component>
fld public final static java.lang.String NAME_PROPERTY = "name"
intf org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.Named%0}>
meth public abstract java.lang.String getName()

CLSS public abstract interface org.netbeans.modules.xml.xam.NamedReferenceable<%0 extends org.netbeans.modules.xml.xam.Component>
intf org.netbeans.modules.xml.xam.Named<{org.netbeans.modules.xml.xam.NamedReferenceable%0}>
intf org.netbeans.modules.xml.xam.Referenceable

CLSS public abstract interface org.netbeans.modules.xml.xam.Referenceable

CLSS public abstract org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent<{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0}>>
cons public init(org.netbeans.modules.xml.xam.dom.AbstractDocumentModel,org.w3c.dom.Element)
innr public static PrefixAttribute
intf org.netbeans.modules.xml.xam.dom.DocumentComponent2<{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0}>
intf org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater
meth protected <%0 extends org.w3c.dom.Node> void updateReference(org.w3c.dom.Element,java.util.List<{%%0}>)
meth protected abstract java.lang.Object getAttributeValueOf(org.netbeans.modules.xml.xam.dom.Attribute,java.lang.String)
meth protected abstract void populateChildren(java.util.List<{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0}>)
meth protected int findDomainIndex(org.w3c.dom.Element)
meth protected int getNodeIndexOf(org.w3c.dom.Node,org.w3c.dom.Node)
meth protected java.lang.String ensureUnique(java.lang.String,java.lang.String)
meth protected java.lang.String getChildElementText(javax.xml.namespace.QName)
meth protected java.lang.String getLeadingText({org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0})
meth protected java.lang.String getNamespaceURI()
meth protected java.lang.String getPrefixedName(java.lang.String,java.lang.String)
meth protected java.lang.String getPrefixedName(java.lang.String,java.lang.String,java.lang.String,boolean)
meth protected java.lang.String getPrefixedName(javax.xml.namespace.QName,boolean)
meth protected java.lang.String getText()
meth protected java.lang.String getText({org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0},boolean,boolean)
meth protected java.lang.String getTrailingText({org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0})
meth protected java.lang.String getXmlFragment()
meth protected org.netbeans.modules.xml.xam.ModelSource resolveModel(java.lang.String) throws org.netbeans.modules.xml.xam.locator.CatalogModelException
meth protected org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent getEffectiveParent()
meth protected org.netbeans.modules.xml.xam.dom.Attribute createPrefixAttribute(java.lang.String)
meth protected org.netbeans.modules.xml.xam.dom.DocumentModelAccess getAccess()
meth protected org.w3c.dom.Element getChildElement(javax.xml.namespace.QName)
meth protected void appendChildQuietly({org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0},java.util.List<{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0}>)
meth protected void ensureValueNamespaceDeclared(java.lang.String,java.lang.String,java.lang.String)
meth protected void fireChildAdded()
meth protected void fireChildRemoved()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void fireValueChanged()
meth protected void insertAtIndexQuietly({org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0},java.util.List<{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0}>,int)
meth protected void removeAttributeQuietly(org.w3c.dom.Element,java.lang.String)
meth protected void removeChildQuietly({org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0},java.util.List<{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0}>)
meth protected void setAttributeQuietly(org.netbeans.modules.xml.xam.dom.Attribute,java.lang.Object)
meth protected void setChildElementText(java.lang.String,java.lang.String,javax.xml.namespace.QName)
meth protected void setLeadingText(java.lang.String,java.lang.String,{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0})
meth protected void setQNameAttribute(java.lang.String,javax.xml.namespace.QName,java.lang.String)
meth protected void setText(java.lang.String,java.lang.String)
meth protected void setText(java.lang.String,java.lang.String,{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0},boolean,boolean)
meth protected void setTrailingText(java.lang.String,java.lang.String,{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0})
meth protected void setXmlFragment(java.lang.String,java.lang.String) throws java.io.IOException
meth protected void updatePeer(java.lang.String,org.w3c.dom.Element)
meth protected void verifyWrite()
meth public <%0 extends org.w3c.dom.Node> void updateReference(java.util.List<{%%0}>)
meth public boolean isInDocumentModel()
meth public boolean referencesSameNode(org.w3c.dom.Node)
meth public int findAttributePosition(java.lang.String)
meth public int findEndPosition()
meth public int findPosition()
meth public java.lang.String getAnyAttribute(javax.xml.namespace.QName)
meth public java.lang.String getAttribute(org.netbeans.modules.xml.xam.dom.Attribute)
meth public java.lang.String getXmlFragmentInclusive()
meth public java.lang.String lookupNamespaceURI(java.lang.String)
meth public java.lang.String lookupNamespaceURI(java.lang.String,boolean)
meth public java.lang.String lookupPrefix(java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.String> getPrefixes()
meth public java.util.Map<javax.xml.namespace.QName,java.lang.String> getAttributeMap()
meth public javax.xml.namespace.QName getQName()
meth public org.netbeans.modules.xml.xam.dom.AbstractDocumentModel getModel()
meth public org.netbeans.modules.xml.xam.dom.DocumentComponent copy({org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0})
meth public org.w3c.dom.Element getPeer()
meth public static java.lang.String getText(org.w3c.dom.Element)
meth public static javax.xml.namespace.QName getQName(org.w3c.dom.Node)
meth public void addPrefix(java.lang.String,java.lang.String)
meth public void removePrefix(java.lang.String)
meth public void setAnyAttribute(javax.xml.namespace.QName,java.lang.String)
meth public void setAttribute(java.lang.String,org.netbeans.modules.xml.xam.dom.Attribute,java.lang.Object)
meth public void updateReference(org.w3c.dom.Element)
meth public {org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0} findChildComponent(org.w3c.dom.Element)
meth public {org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0} findChildComponentByIdentity(org.w3c.dom.Element)
supr org.netbeans.modules.xml.xam.AbstractComponent<{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0}>
hfds node

CLSS public abstract org.netbeans.modules.xml.xam.dom.AbstractDocumentModel<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent<{org.netbeans.modules.xml.xam.dom.AbstractDocumentModel%0}>>
cons public init(org.netbeans.modules.xml.xam.ModelSource)
fld protected org.netbeans.modules.xml.xam.dom.DocumentModelAccess access
intf org.netbeans.modules.xml.xam.dom.DocumentModel<{org.netbeans.modules.xml.xam.dom.AbstractDocumentModel%0}>
meth protected abstract org.netbeans.modules.xml.xam.ComponentUpdater<{org.netbeans.modules.xml.xam.dom.AbstractDocumentModel%0}> getComponentUpdater()
meth protected boolean isDomainElement(org.w3c.dom.Node)
meth protected boolean needsSync()
meth protected static java.lang.String toLocalName(java.lang.String)
meth protected void firePropertyChangedEvents(org.netbeans.modules.xml.xam.dom.SyncUnit)
meth protected void firePropertyChangedEvents(org.netbeans.modules.xml.xam.dom.SyncUnit,org.w3c.dom.Element)
meth protected void refresh()
meth protected void setIdentifyingAttributes()
meth protected void syncCompleted()
meth protected void syncStarted()
meth public abstract {org.netbeans.modules.xml.xam.dom.AbstractDocumentModel%0} createRootComponent(org.w3c.dom.Element)
meth public boolean areSameNodes(org.w3c.dom.Node,org.w3c.dom.Node)
meth public java.lang.String getXPathExpression(org.netbeans.modules.xml.xam.dom.DocumentComponent)
meth public java.util.Map<javax.xml.namespace.QName,java.util.List<javax.xml.namespace.QName>> getQNameValuedAttributes()
meth public java.util.Set<java.lang.String> getElementNames()
meth public java.util.Set<javax.xml.namespace.QName> getQNames()
meth public javax.swing.text.Document getBaseDocument()
meth public org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent findComponent(org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent,java.util.List<org.w3c.dom.Element>,int)
meth public org.netbeans.modules.xml.xam.dom.ChangeInfo prepareChangeInfo(java.util.List<? extends org.w3c.dom.Node>,java.util.List<? extends org.w3c.dom.Node>)
meth public org.netbeans.modules.xml.xam.dom.ChangeInfo prepareChangeInfo(java.util.List<org.w3c.dom.Node>)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.modules.xml.xam.dom.DocumentComponent findComponent(int)
meth public org.netbeans.modules.xml.xam.dom.DocumentComponent findComponent(java.util.List<org.w3c.dom.Element>)
meth public org.netbeans.modules.xml.xam.dom.DocumentComponent findComponent(org.w3c.dom.Element)
meth public org.netbeans.modules.xml.xam.dom.DocumentModelAccess getAccess()
meth public org.netbeans.modules.xml.xam.dom.SyncUnit prepareSyncUnit(org.netbeans.modules.xml.xam.dom.ChangeInfo,org.netbeans.modules.xml.xam.dom.SyncUnit)
meth public org.w3c.dom.Document getDocument()
meth public static org.netbeans.modules.xml.xam.spi.DocumentModelAccessProvider getAccessProvider()
meth public void addChildComponent(org.netbeans.modules.xml.xam.Component,org.netbeans.modules.xml.xam.Component,int)
meth public void processSyncUnit(org.netbeans.modules.xml.xam.dom.SyncUnit)
meth public void removeChildComponent(org.netbeans.modules.xml.xam.Component)
supr org.netbeans.modules.xml.xam.AbstractModel<{org.netbeans.modules.xml.xam.dom.AbstractDocumentModel%0}>
hfds accessPrivate,docListener,elementNames,getAccessLock,needsSync,swingDocument
hcls DocumentChangeListener,WeakDocumentListener

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.Attribute
meth public abstract java.lang.Class getMemberType()
meth public abstract java.lang.Class getType()
meth public abstract java.lang.String getName()

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

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.DocumentComponent2<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent>
intf org.netbeans.modules.xml.xam.dom.DocumentComponent<{org.netbeans.modules.xml.xam.dom.DocumentComponent2%0}>
meth public abstract int findEndPosition()

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.DocumentModel<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent<{org.netbeans.modules.xml.xam.dom.DocumentModel%0}>>
intf org.netbeans.modules.xml.xam.Model<{org.netbeans.modules.xml.xam.dom.DocumentModel%0}>
meth public abstract boolean areSameNodes(org.w3c.dom.Node,org.w3c.dom.Node)
meth public abstract java.lang.String getXPathExpression(org.netbeans.modules.xml.xam.dom.DocumentComponent)
meth public abstract org.netbeans.modules.xml.xam.dom.DocumentComponent findComponent(int)
meth public abstract org.w3c.dom.Document getDocument()
meth public abstract {org.netbeans.modules.xml.xam.dom.DocumentModel%0} createComponent({org.netbeans.modules.xml.xam.dom.DocumentModel%0},org.w3c.dom.Element)
meth public abstract {org.netbeans.modules.xml.xam.dom.DocumentModel%0} getRootComponent()

CLSS public abstract org.netbeans.modules.xml.xam.dom.DocumentModelAccess
cons public init()
innr public abstract interface static NodeUpdater
meth public abstract boolean areSameNodes(org.w3c.dom.Node,org.w3c.dom.Node)
meth public abstract int findPosition(org.w3c.dom.Node)
meth public abstract int getElementIndexOf(org.w3c.dom.Node,org.w3c.dom.Element)
meth public abstract java.lang.String getXPath(org.w3c.dom.Document,org.w3c.dom.Element)
meth public abstract java.lang.String getXmlFragment(org.w3c.dom.Element)
meth public abstract java.util.List<org.w3c.dom.Element> getPathFromRoot(org.w3c.dom.Document,org.w3c.dom.Element)
meth public abstract java.util.List<org.w3c.dom.Node> findNodes(org.w3c.dom.Document,java.lang.String)
meth public abstract java.util.Map<javax.xml.namespace.QName,java.lang.String> getAttributeMap(org.w3c.dom.Element)
meth public abstract org.netbeans.modules.xml.xam.dom.ElementIdentity getElementIdentity()
meth public abstract org.w3c.dom.Document getDocumentRoot()
meth public abstract org.w3c.dom.Element duplicate(org.w3c.dom.Element)
meth public abstract org.w3c.dom.Element getContainingElement(int)
meth public abstract org.w3c.dom.Node findNode(org.w3c.dom.Document,java.lang.String)
meth public abstract org.w3c.dom.Node getNewEventNode(java.beans.PropertyChangeEvent)
meth public abstract org.w3c.dom.Node getNewEventParentNode(java.beans.PropertyChangeEvent)
meth public abstract org.w3c.dom.Node getOldEventNode(java.beans.PropertyChangeEvent)
meth public abstract org.w3c.dom.Node getOldEventParentNode(java.beans.PropertyChangeEvent)
meth public abstract void addMergeEventHandler(java.beans.PropertyChangeListener)
meth public abstract void appendChild(org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void insertBefore(org.w3c.dom.Node,org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void removeAttribute(org.w3c.dom.Element,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void removeChild(org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void removeMergeEventHandler(java.beans.PropertyChangeListener)
meth public abstract void replaceChild(org.w3c.dom.Node,org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void setAttribute(org.w3c.dom.Element,java.lang.String,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void setPrefix(org.w3c.dom.Element,java.lang.String)
meth public abstract void setText(org.w3c.dom.Element,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void setXmlFragment(org.w3c.dom.Element,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater) throws java.io.IOException
meth public java.lang.String getCurrentDocumentText()
meth public java.lang.String getXmlFragmentInclusive(org.w3c.dom.Element)
meth public java.lang.String lookupNamespaceURI(org.w3c.dom.Node,java.util.List<? extends org.w3c.dom.Node>)
meth public java.lang.String normalizeUndefinedAttributeValue(java.lang.String)
meth public long dirtyIntervalMillis()
meth public org.netbeans.modules.xml.xam.dom.AbstractDocumentModel getModel()
meth public void addQNameValuedAttributes(java.util.Map<javax.xml.namespace.QName,java.util.List<javax.xml.namespace.QName>>)
meth public void removeChildren(org.w3c.dom.Node,java.util.Collection<org.w3c.dom.Node>,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void reorderChildren(org.w3c.dom.Element,int[],org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void setDirty()
meth public void unsetDirty()
supr org.netbeans.modules.xml.xam.ModelAccess
hfds dirtyTimeMillis

CLSS public abstract interface static org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater
 outer org.netbeans.modules.xml.xam.dom.DocumentModelAccess
meth public abstract <%0 extends org.w3c.dom.Node> void updateReference(java.util.List<{%%0}>)
meth public abstract void updateReference(org.w3c.dom.Element)

