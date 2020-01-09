#Signature file v4.1
#Version 1.38

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

CLSS public abstract com.sun.org.apache.xerces.internal.dom.ChildNode
cons protected init(com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl)
cons public init()
fld protected com.sun.org.apache.xerces.internal.dom.ChildNode nextSibling
fld protected com.sun.org.apache.xerces.internal.dom.ChildNode previousSibling
meth public org.w3c.dom.Node cloneNode(boolean)
meth public org.w3c.dom.Node getNextSibling()
meth public org.w3c.dom.Node getParentNode()
meth public org.w3c.dom.Node getPreviousSibling()
supr com.sun.org.apache.xerces.internal.dom.NodeImpl
hfds fBufferStr,serialVersionUID

CLSS public com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
cons public init()
cons public init(boolean)
cons public init(org.w3c.dom.DocumentType)
cons public init(org.w3c.dom.DocumentType,boolean)
fld protected boolean allowGrammarAccess
fld protected boolean ancestorChecking
fld protected boolean errorChecking
fld protected boolean standalone
fld protected boolean xmlVersionChanged
fld protected com.sun.org.apache.xerces.internal.dom.DocumentTypeImpl docType
fld protected com.sun.org.apache.xerces.internal.dom.ElementImpl docElement
fld protected int changes
fld protected java.lang.String actualEncoding
fld protected java.lang.String encoding
fld protected java.lang.String fDocumentURI
fld protected java.lang.String version
fld protected java.util.Map<java.lang.String,org.w3c.dom.Node> identifiers
intf org.w3c.dom.Document
meth protected boolean dispatchEvent(com.sun.org.apache.xerces.internal.dom.NodeImpl,org.w3c.dom.events.Event)
meth protected boolean isKidOK(org.w3c.dom.Node,org.w3c.dom.Node)
meth protected final void checkDOMNSErr(java.lang.String,java.lang.String)
meth protected final void checkNamespaceWF(java.lang.String,int,int)
meth protected final void checkQName(java.lang.String,java.lang.String)
meth protected final void clearIdentifiers()
meth protected int changes()
meth protected int getNodeNumber()
meth protected int getNodeNumber(org.w3c.dom.Node)
meth protected java.lang.Object getUserData(com.sun.org.apache.xerces.internal.dom.NodeImpl)
meth protected java.util.Map<java.lang.String,com.sun.org.apache.xerces.internal.dom.ParentNode$UserDataRecord> getUserDataRecord(org.w3c.dom.Node)
meth protected void addEventListener(com.sun.org.apache.xerces.internal.dom.NodeImpl,java.lang.String,org.w3c.dom.events.EventListener,boolean)
meth protected void changed()
meth protected void cloneNode(com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl,boolean)
meth protected void copyEventListeners(com.sun.org.apache.xerces.internal.dom.NodeImpl,com.sun.org.apache.xerces.internal.dom.NodeImpl)
meth protected void removeEventListener(com.sun.org.apache.xerces.internal.dom.NodeImpl,java.lang.String,org.w3c.dom.events.EventListener,boolean)
meth protected void setUserData(com.sun.org.apache.xerces.internal.dom.NodeImpl,java.lang.Object)
meth protected void undeferChildren(org.w3c.dom.Node)
meth public boolean getAsync()
meth public boolean getErrorChecking()
meth public boolean getStandalone()
meth public boolean getStrictErrorChecking()
meth public boolean getXmlStandalone()
meth public boolean load(java.lang.String)
meth public boolean loadXML(java.lang.String)
meth public com.sun.org.apache.xerces.internal.dom.ElementDefinitionImpl createElementDefinition(java.lang.String)
meth public final org.w3c.dom.Document getOwnerDocument()
meth public final static boolean isValidQName(java.lang.String,java.lang.String,boolean)
meth public final static boolean isXMLName(java.lang.String,boolean)
meth public java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public java.lang.Object getFeature(java.lang.String,java.lang.String)
meth public java.lang.Object getUserData(org.w3c.dom.Node,java.lang.String)
meth public java.lang.Object setUserData(org.w3c.dom.Node,java.lang.String,java.lang.Object,org.w3c.dom.UserDataHandler)
meth public java.lang.String getBaseURI()
meth public java.lang.String getDocumentURI()
meth public java.lang.String getEncoding()
meth public java.lang.String getInputEncoding()
meth public java.lang.String getNodeName()
meth public java.lang.String getTextContent()
meth public java.lang.String getVersion()
meth public java.lang.String getXmlEncoding()
meth public java.lang.String getXmlVersion()
meth public java.lang.String saveXML(org.w3c.dom.Node)
meth public org.w3c.dom.Attr createAttribute(java.lang.String)
meth public org.w3c.dom.Attr createAttributeNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.Attr createAttributeNS(java.lang.String,java.lang.String,java.lang.String)
meth public org.w3c.dom.CDATASection createCDATASection(java.lang.String)
meth public org.w3c.dom.Comment createComment(java.lang.String)
meth public org.w3c.dom.DOMConfiguration getDomConfig()
meth public org.w3c.dom.DOMImplementation getImplementation()
meth public org.w3c.dom.DocumentFragment createDocumentFragment()
meth public org.w3c.dom.DocumentType createDocumentType(java.lang.String,java.lang.String,java.lang.String)
meth public org.w3c.dom.DocumentType getDoctype()
meth public org.w3c.dom.Element createElement(java.lang.String)
meth public org.w3c.dom.Element createElementNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.Element createElementNS(java.lang.String,java.lang.String,java.lang.String)
meth public org.w3c.dom.Element getDocumentElement()
meth public org.w3c.dom.Element getElementById(java.lang.String)
meth public org.w3c.dom.Element getIdentifier(java.lang.String)
meth public org.w3c.dom.Entity createEntity(java.lang.String)
meth public org.w3c.dom.EntityReference createEntityReference(java.lang.String)
meth public org.w3c.dom.Node adoptNode(org.w3c.dom.Node)
meth public org.w3c.dom.Node cloneNode(boolean)
meth public org.w3c.dom.Node importNode(org.w3c.dom.Node,boolean)
meth public org.w3c.dom.Node insertBefore(org.w3c.dom.Node,org.w3c.dom.Node)
meth public org.w3c.dom.Node removeChild(org.w3c.dom.Node)
meth public org.w3c.dom.Node renameNode(org.w3c.dom.Node,java.lang.String,java.lang.String)
meth public org.w3c.dom.Node replaceChild(org.w3c.dom.Node,org.w3c.dom.Node)
meth public org.w3c.dom.NodeList getElementsByTagName(java.lang.String)
meth public org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.Notation createNotation(java.lang.String)
meth public org.w3c.dom.ProcessingInstruction createProcessingInstruction(java.lang.String,java.lang.String)
meth public org.w3c.dom.Text createTextNode(java.lang.String)
meth public short getNodeType()
meth public void abort()
meth public void normalizeDocument()
meth public void putIdentifier(java.lang.String,org.w3c.dom.Element)
meth public void removeIdentifier(java.lang.String)
meth public void setAsync(boolean)
meth public void setDocumentURI(java.lang.String)
meth public void setEncoding(java.lang.String)
meth public void setErrorChecking(boolean)
meth public void setInputEncoding(java.lang.String)
meth public void setStandalone(boolean)
meth public void setStrictErrorChecking(boolean)
meth public void setTextContent(java.lang.String)
meth public void setVersion(java.lang.String)
meth public void setXmlEncoding(java.lang.String)
meth public void setXmlStandalone(boolean)
meth public void setXmlVersion(java.lang.String)
supr com.sun.org.apache.xerces.internal.dom.ParentNode
hfds documentNumber,domNormalizer,fConfiguration,fFreeNLCache,fXPathEvaluator,kidOK,nodeCounter,nodeTable,nodeUserData,serialPersistentFields,serialVersionUID,xml11Version

CLSS public com.sun.org.apache.xerces.internal.dom.DocumentFragmentImpl
cons public init()
cons public init(com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl)
intf org.w3c.dom.DocumentFragment
meth public java.lang.String getNodeName()
meth public short getNodeType()
meth public void normalize()
supr com.sun.org.apache.xerces.internal.dom.ParentNode
hfds serialVersionUID

CLSS public com.sun.org.apache.xerces.internal.dom.DocumentImpl
cons public init()
cons public init(boolean)
cons public init(org.w3c.dom.DocumentType)
cons public init(org.w3c.dom.DocumentType,boolean)
fld protected boolean mutationEvents
fld protected java.util.List<org.w3c.dom.ranges.Range> ranges
fld protected java.util.List<org.w3c.dom.traversal.NodeIterator> iterators
fld protected java.util.Map<com.sun.org.apache.xerces.internal.dom.NodeImpl,java.util.List<com.sun.org.apache.xerces.internal.dom.DocumentImpl$LEntry>> eventListeners
intf org.w3c.dom.events.DocumentEvent
intf org.w3c.dom.ranges.DocumentRange
intf org.w3c.dom.traversal.DocumentTraversal
meth protected boolean dispatchEvent(com.sun.org.apache.xerces.internal.dom.NodeImpl,org.w3c.dom.events.Event)
meth protected void addEventListener(com.sun.org.apache.xerces.internal.dom.NodeImpl,java.lang.String,org.w3c.dom.events.EventListener,boolean)
meth protected void copyEventListeners(com.sun.org.apache.xerces.internal.dom.NodeImpl,com.sun.org.apache.xerces.internal.dom.NodeImpl)
meth protected void dispatchAggregateEvents(com.sun.org.apache.xerces.internal.dom.NodeImpl,com.sun.org.apache.xerces.internal.dom.AttrImpl,java.lang.String,short)
meth protected void dispatchAggregateEvents(com.sun.org.apache.xerces.internal.dom.NodeImpl,com.sun.org.apache.xerces.internal.dom.DocumentImpl$EnclosingAttr)
meth protected void dispatchEventToSubtree(org.w3c.dom.Node,org.w3c.dom.events.Event)
meth protected void dispatchingEventToSubtree(org.w3c.dom.Node,org.w3c.dom.events.Event)
meth protected void removeEventListener(com.sun.org.apache.xerces.internal.dom.NodeImpl,java.lang.String,org.w3c.dom.events.EventListener,boolean)
meth protected void saveEnclosingAttr(com.sun.org.apache.xerces.internal.dom.NodeImpl)
meth public org.w3c.dom.DOMImplementation getImplementation()
meth public org.w3c.dom.Node cloneNode(boolean)
meth public org.w3c.dom.events.Event createEvent(java.lang.String)
meth public org.w3c.dom.ranges.Range createRange()
meth public org.w3c.dom.traversal.NodeIterator createNodeIterator(org.w3c.dom.Node,int,org.w3c.dom.traversal.NodeFilter,boolean)
meth public org.w3c.dom.traversal.NodeIterator createNodeIterator(org.w3c.dom.Node,short,org.w3c.dom.traversal.NodeFilter)
meth public org.w3c.dom.traversal.TreeWalker createTreeWalker(org.w3c.dom.Node,int,org.w3c.dom.traversal.NodeFilter,boolean)
meth public org.w3c.dom.traversal.TreeWalker createTreeWalker(org.w3c.dom.Node,short,org.w3c.dom.traversal.NodeFilter)
supr com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
hfds savedEnclosingAttr,serialPersistentFields,serialVersionUID
hcls EnclosingAttr,LEntry

CLSS public abstract com.sun.org.apache.xerces.internal.dom.NodeImpl
cons protected init(com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl)
cons public init()
fld protected com.sun.org.apache.xerces.internal.dom.NodeImpl ownerNode
fld protected final static short FIRSTCHILD = 16
fld protected final static short HASSTRING = 128
fld protected final static short ID = 512
fld protected final static short IGNORABLEWS = 64
fld protected final static short NORMALIZED = 256
fld protected final static short OWNED = 8
fld protected final static short READONLY = 1
fld protected final static short SPECIFIED = 32
fld protected final static short SYNCCHILDREN = 4
fld protected final static short SYNCDATA = 2
fld protected short flags
fld public final static short DOCUMENT_POSITION_CONTAINS = 8
fld public final static short DOCUMENT_POSITION_DISCONNECTED = 1
fld public final static short DOCUMENT_POSITION_FOLLOWING = 4
fld public final static short DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC = 32
fld public final static short DOCUMENT_POSITION_IS_CONTAINED = 16
fld public final static short DOCUMENT_POSITION_PRECEDING = 2
fld public final static short ELEMENT_DEFINITION_NODE = 21
fld public final static short TREE_POSITION_ANCESTOR = 4
fld public final static short TREE_POSITION_DESCENDANT = 8
fld public final static short TREE_POSITION_DISCONNECTED = 0
fld public final static short TREE_POSITION_EQUIVALENT = 16
fld public final static short TREE_POSITION_FOLLOWING = 2
fld public final static short TREE_POSITION_PRECEDING = 1
fld public final static short TREE_POSITION_SAME_NODE = 32
intf java.io.Serializable
intf java.lang.Cloneable
intf org.w3c.dom.Node
intf org.w3c.dom.NodeList
intf org.w3c.dom.events.EventTarget
meth protected int changes()
meth protected int getNodeNumber()
meth protected java.util.Map<java.lang.String,com.sun.org.apache.xerces.internal.dom.ParentNode$UserDataRecord> getUserDataRecord()
meth protected org.w3c.dom.Node getContainer()
meth protected void changed()
meth protected void synchronizeData()
meth public abstract java.lang.String getNodeName()
meth public abstract short getNodeType()
meth public boolean dispatchEvent(org.w3c.dom.events.Event)
meth public boolean getReadOnly()
meth public boolean hasAttributes()
meth public boolean hasChildNodes()
meth public boolean isDefaultNamespace(java.lang.String)
meth public boolean isEqualNode(org.w3c.dom.Node)
meth public boolean isSameNode(org.w3c.dom.Node)
meth public boolean isSupported(java.lang.String,java.lang.String)
meth public final void needsSyncChildren(boolean)
meth public int getLength()
meth public java.lang.Object getFeature(java.lang.String,java.lang.String)
meth public java.lang.Object getUserData()
meth public java.lang.Object getUserData(java.lang.String)
meth public java.lang.Object setUserData(java.lang.String,java.lang.Object,org.w3c.dom.UserDataHandler)
meth public java.lang.String getBaseURI()
meth public java.lang.String getLocalName()
meth public java.lang.String getNamespaceURI()
meth public java.lang.String getNodeValue()
meth public java.lang.String getPrefix()
meth public java.lang.String getTextContent()
meth public java.lang.String lookupNamespaceURI(java.lang.String)
meth public java.lang.String lookupPrefix(java.lang.String)
meth public java.lang.String toString()
meth public org.w3c.dom.Document getOwnerDocument()
meth public org.w3c.dom.NamedNodeMap getAttributes()
meth public org.w3c.dom.Node appendChild(org.w3c.dom.Node)
meth public org.w3c.dom.Node cloneNode(boolean)
meth public org.w3c.dom.Node getFirstChild()
meth public org.w3c.dom.Node getLastChild()
meth public org.w3c.dom.Node getNextSibling()
meth public org.w3c.dom.Node getParentNode()
meth public org.w3c.dom.Node getPreviousSibling()
meth public org.w3c.dom.Node insertBefore(org.w3c.dom.Node,org.w3c.dom.Node)
meth public org.w3c.dom.Node item(int)
meth public org.w3c.dom.Node removeChild(org.w3c.dom.Node)
meth public org.w3c.dom.Node replaceChild(org.w3c.dom.Node,org.w3c.dom.Node)
meth public org.w3c.dom.NodeList getChildNodes()
meth public short compareDocumentPosition(org.w3c.dom.Node)
meth public short compareTreePosition(org.w3c.dom.Node)
meth public void addEventListener(java.lang.String,org.w3c.dom.events.EventListener,boolean)
meth public void normalize()
meth public void removeEventListener(java.lang.String,org.w3c.dom.events.EventListener,boolean)
meth public void setNodeValue(java.lang.String)
meth public void setPrefix(java.lang.String)
meth public void setReadOnly(boolean,boolean)
meth public void setTextContent(java.lang.String)
meth public void setUserData(java.lang.Object)
supr java.lang.Object
hfds serialVersionUID

CLSS public abstract com.sun.org.apache.xerces.internal.dom.ParentNode
cons protected init(com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl)
cons public init()
fld protected com.sun.org.apache.xerces.internal.dom.ChildNode firstChild
fld protected com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl ownerDocument
fld protected java.io.Serializable fNodeListCache
innr protected UserDataRecord
meth protected final org.w3c.dom.NodeList getChildNodesUnoptimized()
meth protected void synchronizeChildren()
meth public boolean hasChildNodes()
meth public boolean isEqualNode(org.w3c.dom.Node)
meth public int getLength()
meth public java.lang.String getTextContent()
meth public org.w3c.dom.Document getOwnerDocument()
meth public org.w3c.dom.Node cloneNode(boolean)
meth public org.w3c.dom.Node getFirstChild()
meth public org.w3c.dom.Node getLastChild()
meth public org.w3c.dom.Node insertBefore(org.w3c.dom.Node,org.w3c.dom.Node)
meth public org.w3c.dom.Node item(int)
meth public org.w3c.dom.Node removeChild(org.w3c.dom.Node)
meth public org.w3c.dom.Node replaceChild(org.w3c.dom.Node,org.w3c.dom.Node)
meth public org.w3c.dom.NodeList getChildNodes()
meth public void normalize()
meth public void setReadOnly(boolean,boolean)
meth public void setTextContent(java.lang.String)
supr com.sun.org.apache.xerces.internal.dom.ChildNode
hfds serialVersionUID

CLSS public abstract com.sun.tools.ws.api.TJavaGeneratorExtension
cons public init()
meth public abstract void writeMethodAnnotations(com.sun.tools.ws.api.wsdl.TWSDLOperation,com.sun.codemodel.JMethod)
supr java.lang.Object

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

CLSS public com.sun.tools.ws.processor.generator.CustomExceptionGenerator
cons public init()
meth public com.sun.tools.ws.processor.generator.GeneratorBase getGenerator(com.sun.tools.ws.processor.model.Model,com.sun.tools.ws.wscompile.WsimportOptions,com.sun.tools.ws.wscompile.ErrorReceiver)
meth public static void generate(com.sun.tools.ws.processor.model.Model,com.sun.tools.ws.wscompile.WsimportOptions,com.sun.tools.ws.wscompile.ErrorReceiver)
meth public void visit(com.sun.tools.ws.processor.model.Fault) throws java.lang.Exception
supr com.sun.tools.ws.processor.generator.GeneratorBase
hfds faults

CLSS public abstract com.sun.tools.ws.processor.generator.GeneratorBase
cons protected init()
fld protected boolean donotOverride
fld protected com.sun.codemodel.JCodeModel cm
fld protected com.sun.tools.ws.processor.model.Model model
fld protected com.sun.tools.ws.wscompile.ErrorReceiver receiver
fld protected com.sun.tools.ws.wscompile.WsimportOptions options
fld protected java.lang.String wsdlLocation
intf com.sun.tools.ws.processor.model.ModelVisitor
meth protected com.sun.codemodel.JDefinedClass getClass(java.lang.String,com.sun.codemodel.ClassType) throws com.sun.codemodel.JClassAlreadyExistsException
meth protected void log(java.lang.String)
meth protected void writeHandlerConfig(java.lang.String,com.sun.codemodel.JDefinedClass,com.sun.tools.ws.wscompile.WsimportOptions)
meth public java.util.List<java.lang.String> getJAXWSClassComment()
meth public static java.util.List<java.lang.String> getJAXWSClassComment(java.lang.String)
meth public void doGeneration()
meth public void init(com.sun.tools.ws.processor.model.Model,com.sun.tools.ws.wscompile.WsimportOptions,com.sun.tools.ws.wscompile.ErrorReceiver)
meth public void visit(com.sun.tools.ws.processor.model.Block) throws java.lang.Exception
meth public void visit(com.sun.tools.ws.processor.model.Fault) throws java.lang.Exception
meth public void visit(com.sun.tools.ws.processor.model.Model) throws java.lang.Exception
meth public void visit(com.sun.tools.ws.processor.model.Operation) throws java.lang.Exception
meth public void visit(com.sun.tools.ws.processor.model.Parameter) throws java.lang.Exception
meth public void visit(com.sun.tools.ws.processor.model.Port) throws java.lang.Exception
meth public void visit(com.sun.tools.ws.processor.model.Request) throws java.lang.Exception
meth public void visit(com.sun.tools.ws.processor.model.Response) throws java.lang.Exception
meth public void visit(com.sun.tools.ws.processor.model.Service) throws java.lang.Exception
supr java.lang.Object
hfds destDir,targetVersion

CLSS public final !enum com.sun.tools.ws.processor.generator.GeneratorConstants
fld public final static com.sun.tools.ws.processor.generator.GeneratorConstants DOTC
fld public final static com.sun.tools.ws.processor.generator.GeneratorConstants FAULT_CLASS_MEMBER_NAME
fld public final static com.sun.tools.ws.processor.generator.GeneratorConstants GET
fld public final static com.sun.tools.ws.processor.generator.GeneratorConstants IS
fld public final static com.sun.tools.ws.processor.generator.GeneratorConstants JAVA_SRC_SUFFIX
fld public final static com.sun.tools.ws.processor.generator.GeneratorConstants QNAME_SUFFIX
fld public final static com.sun.tools.ws.processor.generator.GeneratorConstants RESPONSE
fld public final static com.sun.tools.ws.processor.generator.GeneratorConstants SIG_INNERCLASS
meth public java.lang.String getValue()
meth public static com.sun.tools.ws.processor.generator.GeneratorConstants valueOf(java.lang.String)
meth public static com.sun.tools.ws.processor.generator.GeneratorConstants[] values()
supr java.lang.Enum<com.sun.tools.ws.processor.generator.GeneratorConstants>
hfds value

CLSS public com.sun.tools.ws.processor.generator.GeneratorException
cons public !varargs init(java.lang.String,java.lang.Object[])
cons public init(java.lang.Throwable)
meth public java.lang.String getDefaultResourceBundleName()
supr com.sun.tools.ws.processor.ProcessorException

CLSS public abstract com.sun.tools.ws.processor.generator.GeneratorExtension
cons public init()
meth public boolean validateOption(java.lang.String)
meth public java.lang.String getBindingValue(java.lang.String,com.sun.xml.ws.api.SOAPVersion)
meth public void writeWebServiceAnnotation(com.sun.tools.ws.processor.model.Model,com.sun.codemodel.JCodeModel,com.sun.codemodel.JDefinedClass,com.sun.tools.ws.processor.model.Port)
meth public void writeWebServiceClientAnnotation(com.sun.tools.ws.wscompile.WsimportOptions,com.sun.codemodel.JCodeModel,com.sun.codemodel.JDefinedClass)
supr java.lang.Object

CLSS public com.sun.tools.ws.processor.generator.GeneratorUtil
cons public init()
meth public static boolean classExists(com.sun.tools.ws.wscompile.Options,java.lang.String)
supr java.lang.Object

CLSS public final com.sun.tools.ws.processor.generator.JavaGeneratorExtensionFacade
meth public void writeMethodAnnotations(com.sun.tools.ws.api.wsdl.TWSDLOperation,com.sun.codemodel.JMethod)
supr com.sun.tools.ws.api.TJavaGeneratorExtension
hfds extensions

CLSS public final com.sun.tools.ws.processor.generator.JwsImplGenerator
meth public static boolean moveToImplDestDir(java.util.List<java.lang.String>,com.sun.tools.ws.wscompile.WsimportOptions,com.sun.tools.ws.wscompile.ErrorReceiver)
meth public static java.util.List<java.lang.String> generate(com.sun.tools.ws.processor.model.Model,com.sun.tools.ws.wscompile.WsimportOptions,com.sun.tools.ws.wscompile.ErrorReceiver)
meth public void visit(com.sun.tools.ws.processor.model.Service)
supr com.sun.tools.ws.processor.generator.GeneratorBase
hfds TRANSLATION_MAP,implFiles
hcls ImplFile

CLSS public final com.sun.tools.ws.processor.generator.Names
meth public static boolean isJavaReservedWord(java.lang.String)
meth public static java.lang.String customExceptionClassName(com.sun.tools.ws.processor.model.Fault)
meth public static java.lang.String customJavaTypeClassName(com.sun.tools.ws.processor.model.java.JavaInterface)
meth public static java.lang.String getExceptionClassMemberName()
meth public static java.lang.String getJavaMemberReadMethod(com.sun.tools.ws.processor.model.java.JavaStructureMember)
meth public static java.lang.String getJavaReserverVarialbeName(java.lang.String)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public static java.lang.String getPackageName(java.lang.String)
meth public static java.lang.String getPortName(com.sun.tools.ws.processor.model.Port)
meth public static java.lang.String getResponseName(java.lang.String)
meth public static java.lang.String stripQualifier(java.lang.String)
supr java.lang.Object
hfds RESERVED_WORDS

CLSS public com.sun.tools.ws.processor.generator.SeiGenerator
cons public init()
meth public !varargs static void generate(com.sun.tools.ws.processor.model.Model,com.sun.tools.ws.wscompile.WsimportOptions,com.sun.tools.ws.wscompile.ErrorReceiver,com.sun.tools.ws.api.TJavaGeneratorExtension[])
meth public !varargs void init(com.sun.tools.ws.processor.model.Model,com.sun.tools.ws.wscompile.WsimportOptions,com.sun.tools.ws.wscompile.ErrorReceiver,com.sun.tools.ws.api.TJavaGeneratorExtension[])
meth public void visit(com.sun.tools.ws.processor.model.Model) throws java.lang.Exception
meth public void visit(com.sun.tools.ws.processor.model.Service) throws java.lang.Exception
supr com.sun.tools.ws.processor.generator.GeneratorBase
hfds extension,extensionHandlers,isDocStyle,sameParamStyle,serviceNS

CLSS public com.sun.tools.ws.processor.generator.ServiceGenerator
meth public static void generate(com.sun.tools.ws.processor.model.Model,com.sun.tools.ws.wscompile.WsimportOptions,com.sun.tools.ws.wscompile.ErrorReceiver)
meth public void visit(com.sun.tools.ws.processor.model.Service)
supr com.sun.tools.ws.processor.generator.GeneratorBase

CLSS public com.sun.tools.ws.processor.generator.W3CAddressingJavaGeneratorExtension
cons public init()
meth public void writeMethodAnnotations(com.sun.tools.ws.api.wsdl.TWSDLOperation,com.sun.codemodel.JMethod)
supr com.sun.tools.ws.api.TJavaGeneratorExtension

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
meth public com.sun.tools.ws.processor.model.Fault getParentFault()
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
hfds block,elementName,exceptionClass,javaException,javaMemberName,name,parentFault,subfaults,wsdlException,wsdlFaultName

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
cons public init(com.sun.xml.ws.util.localization.Localizable)
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

CLSS public abstract interface com.sun.tools.ws.processor.model.exporter.ExternalObject
meth public abstract java.lang.String getType()
meth public abstract void saveTo(org.xml.sax.ContentHandler)

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

CLSS public com.sun.tools.ws.processor.model.jaxb.JAXBElementMember
cons public init()
cons public init(javax.xml.namespace.QName,com.sun.tools.ws.processor.model.jaxb.JAXBType)
cons public init(javax.xml.namespace.QName,com.sun.tools.ws.processor.model.jaxb.JAXBType,com.sun.tools.ws.processor.model.java.JavaStructureMember)
meth public boolean isInherited()
meth public boolean isRepeated()
meth public com.sun.tools.ws.processor.model.java.JavaStructureMember getJavaStructureMember()
meth public com.sun.tools.ws.processor.model.jaxb.JAXBProperty getProperty()
meth public com.sun.tools.ws.processor.model.jaxb.JAXBType getType()
meth public javax.xml.namespace.QName getName()
meth public void setInherited(boolean)
meth public void setJavaStructureMember(com.sun.tools.ws.processor.model.java.JavaStructureMember)
meth public void setName(javax.xml.namespace.QName)
meth public void setProperty(com.sun.tools.ws.processor.model.jaxb.JAXBProperty)
meth public void setRepeated(boolean)
meth public void setType(com.sun.tools.ws.processor.model.jaxb.JAXBType)
supr java.lang.Object
hfds JAXB_UNIQUE_PARRAM,_javaStructureMember,_name,_prop,_repeated,_type,isInherited

CLSS public com.sun.tools.ws.processor.model.jaxb.JAXBMapping
cons public init()
meth public com.sun.tools.ws.processor.model.jaxb.JAXBTypeAndAnnotation getType()
meth public java.util.List<com.sun.tools.ws.processor.model.jaxb.JAXBProperty> getWrapperStyleDrilldown()
meth public javax.xml.namespace.QName getElementName()
meth public void setElementName(javax.xml.namespace.QName)
supr java.lang.Object
hfds elementName,type,wrapperStyleDrilldown

CLSS public com.sun.tools.ws.processor.model.jaxb.JAXBModel
cons public init()
cons public init(com.sun.tools.xjc.api.JAXBModel)
meth public com.sun.tools.ws.processor.model.jaxb.JAXBMapping get(java.lang.String)
meth public com.sun.tools.ws.processor.model.jaxb.JAXBMapping get(javax.xml.namespace.QName)
meth public com.sun.tools.xjc.api.J2SJAXBModel getJ2SJAXBModel()
meth public com.sun.tools.xjc.api.JAXBModel getRawJAXBModel()
meth public com.sun.tools.xjc.api.S2JJAXBModel getS2JJAXBModel()
meth public java.util.List<com.sun.tools.ws.processor.model.jaxb.JAXBMapping> getMappings()
meth public java.util.Set<java.lang.String> getGeneratedClassNames()
meth public void setGeneratedClassNames(java.util.Set<java.lang.String>)
meth public void setMappings(java.util.List<com.sun.tools.ws.processor.model.jaxb.JAXBMapping>)
supr java.lang.Object
hfds byClassName,byQName,generatedClassNames,mappings,rawJAXBModel

CLSS public com.sun.tools.ws.processor.model.jaxb.JAXBProperty
cons public init()
meth public com.sun.tools.ws.processor.model.jaxb.JAXBTypeAndAnnotation getType()
meth public java.lang.String getName()
meth public javax.xml.namespace.QName getElementName()
meth public javax.xml.namespace.QName getRawTypeName()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds elementName,name,rawTypeName,type

CLSS public com.sun.tools.ws.processor.model.jaxb.JAXBStructuredType
cons public init()
cons public init(com.sun.tools.ws.processor.model.jaxb.JAXBType)
cons public init(javax.xml.namespace.QName)
cons public init(javax.xml.namespace.QName,com.sun.tools.ws.processor.model.java.JavaStructureType)
meth public boolean isUnwrapped()
meth public com.sun.tools.ws.processor.model.jaxb.JAXBStructuredType getParentType()
meth public int getElementMembersCount()
meth public java.util.Iterator getElementMembers()
meth public java.util.Iterator getSubtypes()
meth public java.util.List getElementMembersList()
meth public java.util.Set getSubtypesSet()
meth public void add(com.sun.tools.ws.processor.model.jaxb.JAXBElementMember)
meth public void addSubtype(com.sun.tools.ws.processor.model.jaxb.JAXBStructuredType)
meth public void setElementMembersList(java.util.List)
meth public void setParentType(com.sun.tools.ws.processor.model.jaxb.JAXBStructuredType)
meth public void setSubtypesSet(java.util.Set)
supr com.sun.tools.ws.processor.model.jaxb.JAXBType
hfds _elementMembers,_elementMembersByName,_parentType,_subtypes

CLSS public com.sun.tools.ws.processor.model.jaxb.JAXBType
cons public init()
cons public init(com.sun.tools.ws.processor.model.jaxb.JAXBType)
cons public init(javax.xml.namespace.QName,com.sun.tools.ws.processor.model.java.JavaType)
cons public init(javax.xml.namespace.QName,com.sun.tools.ws.processor.model.java.JavaType,com.sun.tools.ws.processor.model.jaxb.JAXBMapping,com.sun.tools.ws.processor.model.jaxb.JAXBModel)
meth public boolean hasWrapperChildren()
meth public boolean isLiteralType()
meth public boolean isUnwrappable()
meth public boolean isUnwrapped()
meth public com.sun.tools.ws.processor.model.jaxb.JAXBMapping getJaxbMapping()
meth public com.sun.tools.ws.processor.model.jaxb.JAXBModel getJaxbModel()
meth public java.util.List<com.sun.tools.ws.processor.model.jaxb.JAXBProperty> getWrapperChildren()
meth public void accept(com.sun.tools.ws.processor.model.jaxb.JAXBTypeVisitor) throws java.lang.Exception
meth public void setJaxbMapping(com.sun.tools.ws.processor.model.jaxb.JAXBMapping)
meth public void setJaxbModel(com.sun.tools.ws.processor.model.jaxb.JAXBModel)
meth public void setUnwrapped(boolean)
meth public void setWrapperChildren(java.util.List<com.sun.tools.ws.processor.model.jaxb.JAXBProperty>)
supr com.sun.tools.ws.processor.model.AbstractType
hfds jaxbMapping,jaxbModel,unwrapped,wrapperChildren

CLSS public com.sun.tools.ws.processor.model.jaxb.JAXBTypeAndAnnotation
cons public init(com.sun.codemodel.JType)
cons public init(com.sun.tools.xjc.api.TypeAndAnnotation)
cons public init(com.sun.tools.xjc.api.TypeAndAnnotation,com.sun.codemodel.JType)
meth public com.sun.codemodel.JType getType()
meth public com.sun.tools.xjc.api.TypeAndAnnotation getTypeAnn()
meth public java.lang.String getName()
meth public void annotate(com.sun.codemodel.JAnnotatable)
meth public void setType(com.sun.codemodel.JType)
meth public void setTypeAnn(com.sun.tools.xjc.api.TypeAndAnnotation)
supr java.lang.Object
hfds type,typeAnn

CLSS public abstract interface com.sun.tools.ws.processor.model.jaxb.JAXBTypeVisitor
meth public abstract void visit(com.sun.tools.ws.processor.model.jaxb.JAXBType) throws java.lang.Exception
meth public abstract void visit(com.sun.tools.ws.processor.model.jaxb.RpcLitStructure) throws java.lang.Exception

CLSS public com.sun.tools.ws.processor.model.jaxb.RpcLitMember
cons public init()
cons public init(javax.xml.namespace.QName,java.lang.String)
cons public init(javax.xml.namespace.QName,java.lang.String,javax.xml.namespace.QName)
meth public java.lang.String getJavaTypeName()
meth public javax.xml.namespace.QName getSchemaTypeName()
meth public void setJavaTypeName(java.lang.String)
meth public void setSchemaTypeName(javax.xml.namespace.QName)
supr com.sun.tools.ws.processor.model.AbstractType
hfds javaTypeName,schemaTypeName

CLSS public com.sun.tools.ws.processor.model.jaxb.RpcLitStructure
cons public init()
cons public init(javax.xml.namespace.QName,com.sun.tools.ws.processor.model.jaxb.JAXBModel)
cons public init(javax.xml.namespace.QName,com.sun.tools.ws.processor.model.jaxb.JAXBModel,java.util.List<com.sun.tools.ws.processor.model.jaxb.RpcLitMember>)
meth public boolean isLiteralType()
meth public com.sun.tools.ws.processor.model.jaxb.JAXBModel getJaxbModel()
meth public java.util.List<com.sun.tools.ws.processor.model.jaxb.RpcLitMember> getRpcLitMembers()
meth public java.util.List<com.sun.tools.ws.processor.model.jaxb.RpcLitMember> setRpcLitMembers(java.util.List<com.sun.tools.ws.processor.model.jaxb.RpcLitMember>)
meth public void accept(com.sun.tools.ws.processor.model.jaxb.JAXBTypeVisitor) throws java.lang.Exception
meth public void addRpcLitMember(com.sun.tools.ws.processor.model.jaxb.RpcLitMember)
meth public void setJaxbModel(com.sun.tools.ws.processor.model.jaxb.JAXBModel)
supr com.sun.tools.ws.processor.model.AbstractType
hfds jaxbModel,members

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
cons public init(com.sun.xml.ws.util.localization.Localizable)
cons public init(java.lang.Throwable)
meth public java.lang.String getDefaultResourceBundleName()
supr com.sun.tools.ws.processor.ProcessorException

CLSS public com.sun.tools.ws.processor.modeler.annotation.AnnotationProcessorContext
cons public init()
innr public static SeiContext
meth public boolean isModelCompleted()
meth public com.sun.tools.ws.processor.modeler.annotation.AnnotationProcessorContext$SeiContext getSeiContext(javax.lang.model.element.Name)
meth public com.sun.tools.ws.processor.modeler.annotation.AnnotationProcessorContext$SeiContext getSeiContext(javax.lang.model.element.TypeElement)
meth public int getRound()
meth public java.util.Collection<com.sun.tools.ws.processor.modeler.annotation.AnnotationProcessorContext$SeiContext> getSeiContexts()
meth public static boolean isEncoded(com.sun.tools.ws.processor.model.Model)
meth public void addSeiContext(javax.lang.model.element.Name,com.sun.tools.ws.processor.modeler.annotation.AnnotationProcessorContext$SeiContext)
meth public void incrementRound()
meth public void setModelCompleted(boolean)
supr java.lang.Object
hfds modelCompleted,round,seiContextMap

CLSS public static com.sun.tools.ws.processor.modeler.annotation.AnnotationProcessorContext$SeiContext
 outer com.sun.tools.ws.processor.modeler.annotation.AnnotationProcessorContext
cons public init(javax.lang.model.element.Name)
meth public boolean getImplementsSei()
meth public com.sun.tools.ws.processor.modeler.annotation.FaultInfo getExceptionBeanName(javax.lang.model.element.Name)
meth public com.sun.tools.ws.processor.modeler.annotation.WrapperInfo getReqOperationWrapper(javax.lang.model.element.ExecutableElement)
meth public com.sun.tools.ws.processor.modeler.annotation.WrapperInfo getResOperationWrapper(javax.lang.model.element.ExecutableElement)
meth public java.lang.String getNamespaceUri()
meth public java.lang.String methodToString(javax.lang.model.element.ExecutableElement)
meth public javax.lang.model.element.Name getSeiImplName()
meth public void addExceptionBeanEntry(javax.lang.model.element.Name,com.sun.tools.ws.processor.modeler.annotation.FaultInfo,com.sun.tools.ws.processor.modeler.annotation.ModelBuilder)
meth public void clearExceptionMap()
meth public void setImplementsSei(boolean)
meth public void setNamespaceUri(java.lang.String)
meth public void setReqWrapperOperation(javax.lang.model.element.ExecutableElement,com.sun.tools.ws.processor.modeler.annotation.WrapperInfo)
meth public void setResWrapperOperation(javax.lang.model.element.ExecutableElement,com.sun.tools.ws.processor.modeler.annotation.WrapperInfo)
meth public void setSeiImplName(javax.lang.model.element.Name)
supr java.lang.Object
hfds exceptionBeanMap,implementsSei,namespaceUri,reqOperationWrapperMap,resOperationWrapperMap,seiImplName,seiName

CLSS public com.sun.tools.ws.processor.modeler.annotation.FaultInfo
cons public init()
cons public init(com.sun.tools.ws.processor.modeler.annotation.TypeMoniker,boolean)
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
fld public boolean isWsdlException
fld public com.sun.tools.ws.processor.modeler.annotation.TypeMoniker beanTypeMoniker
fld public java.lang.String beanName
fld public javax.xml.namespace.QName elementName
meth public boolean isWsdlException()
meth public com.sun.tools.ws.processor.modeler.annotation.TypeMoniker getBeanTypeMoniker()
meth public java.lang.String getBeanName()
meth public javax.xml.namespace.QName getElementName()
meth public void setBeanName(java.lang.String)
meth public void setBeanTypeMoniker(com.sun.tools.ws.processor.modeler.annotation.TypeMoniker)
meth public void setElementName(javax.xml.namespace.QName)
meth public void setIsWsdlException(boolean)
supr java.lang.Object

CLSS public com.sun.tools.ws.processor.modeler.annotation.MakeSafeTypeVisitor
cons public init(javax.annotation.processing.ProcessingEnvironment)
meth protected javax.lang.model.type.TypeMirror defaultAction(javax.lang.model.type.TypeMirror,javax.lang.model.util.Types)
meth public javax.lang.model.type.TypeMirror visitDeclared(javax.lang.model.type.DeclaredType,javax.lang.model.util.Types)
meth public javax.lang.model.type.TypeMirror visitNoType(javax.lang.model.type.NoType,javax.lang.model.util.Types)
supr javax.lang.model.util.SimpleTypeVisitor6<javax.lang.model.type.TypeMirror,javax.lang.model.util.Types>
hfds collectionType,mapType

CLSS public abstract interface com.sun.tools.ws.processor.modeler.annotation.ModelBuilder
meth public abstract boolean canOverWriteClass(java.lang.String)
meth public abstract boolean checkAndSetProcessed(javax.lang.model.element.TypeElement)
meth public abstract boolean isRemote(javax.lang.model.element.TypeElement)
meth public abstract boolean isServiceException(javax.lang.model.type.TypeMirror)
meth public abstract com.sun.tools.ws.wscompile.WsgenOptions getOptions()
meth public abstract java.io.File getSourceDir()
meth public abstract java.lang.String getOperationName(javax.lang.model.element.Name)
meth public abstract javax.annotation.processing.ProcessingEnvironment getProcessingEnvironment()
meth public abstract javax.lang.model.type.TypeMirror getHolderValueType(javax.lang.model.type.TypeMirror)
meth public abstract void log(java.lang.String)
meth public abstract void processError(java.lang.String)
meth public abstract void processError(java.lang.String,javax.lang.model.element.Element)
meth public abstract void processWarning(java.lang.String)

CLSS public abstract interface com.sun.tools.ws.processor.modeler.annotation.TypeMoniker
meth public abstract javax.lang.model.type.TypeMirror create(javax.annotation.processing.ProcessingEnvironment)

CLSS public com.sun.tools.ws.processor.modeler.annotation.TypeMonikerFactory
cons public init()
meth public static com.sun.tools.ws.processor.modeler.annotation.TypeMoniker getTypeMoniker(java.lang.String)
meth public static com.sun.tools.ws.processor.modeler.annotation.TypeMoniker getTypeMoniker(javax.lang.model.type.TypeMirror)
supr java.lang.Object
hcls ArrayTypeMoniker,DeclaredTypeMoniker,PrimitiveTypeMoniker,StringMoniker

CLSS public com.sun.tools.ws.processor.modeler.annotation.WebServiceAp
 anno 0 javax.annotation.processing.SupportedAnnotationTypes(java.lang.String[] value=["javax.jws.HandlerChain", "javax.jws.Oneway", "javax.jws.WebMethod", "javax.jws.WebParam", "javax.jws.WebResult", "javax.jws.WebService", "javax.jws.soap.InitParam", "javax.jws.soap.SOAPBinding", "javax.jws.soap.SOAPMessageHandler", "javax.jws.soap.SOAPMessageHandlers", "javax.xml.ws.BindingType", "javax.xml.ws.RequestWrapper", "javax.xml.ws.ResponseWrapper", "javax.xml.ws.ServiceMode", "javax.xml.ws.WebEndpoint", "javax.xml.ws.WebFault", "javax.xml.ws.WebServiceClient", "javax.xml.ws.WebServiceProvider", "javax.xml.ws.WebServiceRef"])
 anno 0 javax.annotation.processing.SupportedOptions(java.lang.String[] value=["doNotOverWrite", "ignoreNoWebServiceFoundWarning"])
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_6)
cons public init()
cons public init(com.sun.tools.ws.wscompile.WsgenOptions,java.io.PrintStream)
fld protected com.sun.tools.ws.processor.modeler.annotation.AnnotationProcessorContext context
fld public final static java.lang.String DO_NOT_OVERWRITE = "doNotOverWrite"
fld public final static java.lang.String IGNORE_NO_WEB_SERVICE_FOUND_WARNING = "ignoreNoWebServiceFoundWarning"
intf com.sun.tools.ws.processor.modeler.annotation.ModelBuilder
meth protected void report(java.lang.String)
meth public boolean canOverWriteClass(java.lang.String)
meth public boolean checkAndSetProcessed(javax.lang.model.element.TypeElement)
meth public boolean isRemote(javax.lang.model.element.TypeElement)
meth public boolean isServiceException(javax.lang.model.type.TypeMirror)
meth public boolean process(java.util.Set<? extends javax.lang.model.element.TypeElement>,javax.annotation.processing.RoundEnvironment)
meth public com.sun.tools.ws.wscompile.WsgenOptions getOptions()
meth public java.io.File getSourceDir()
meth public java.lang.String getOperationName(javax.lang.model.element.Name)
meth public javax.annotation.processing.ProcessingEnvironment getProcessingEnvironment()
meth public javax.lang.model.type.TypeMirror getHolderValueType(javax.lang.model.type.TypeMirror)
meth public void init(javax.annotation.processing.ProcessingEnvironment)
meth public void log(java.lang.String)
meth public void processError(java.lang.String)
meth public void processError(java.lang.String,javax.lang.model.element.Element)
meth public void processWarning(java.lang.String)
supr javax.annotation.processing.AbstractProcessor
hfds defHolderElement,doNotOverWrite,exceptionElement,ignoreNoWebServiceFoundWarning,isCommandLineInvocation,options,out,processedTypeElements,remoteElement,remoteExceptionElement,runtimeExceptionElement,sourceDir

CLSS public final !enum com.sun.tools.ws.processor.modeler.annotation.WebServiceConstants
fld public final static com.sun.tools.ws.processor.modeler.annotation.WebServiceConstants BEAN
fld public final static com.sun.tools.ws.processor.modeler.annotation.WebServiceConstants FAULT_INFO
fld public final static com.sun.tools.ws.processor.modeler.annotation.WebServiceConstants JAXWS_PACKAGE_PD
fld public final static com.sun.tools.ws.processor.modeler.annotation.WebServiceConstants PD_JAXWS_PACKAGE_PD
fld public final static com.sun.tools.ws.processor.modeler.annotation.WebServiceConstants RESPONSE
fld public final static com.sun.tools.ws.processor.modeler.annotation.WebServiceConstants SERVICE
meth public java.lang.String getValue()
meth public static com.sun.tools.ws.processor.modeler.annotation.WebServiceConstants valueOf(java.lang.String)
meth public static com.sun.tools.ws.processor.modeler.annotation.WebServiceConstants[] values()
supr java.lang.Enum<com.sun.tools.ws.processor.modeler.annotation.WebServiceConstants>
hfds value

CLSS public abstract com.sun.tools.ws.processor.modeler.annotation.WebServiceVisitor
cons public init(com.sun.tools.ws.processor.modeler.annotation.ModelBuilder,com.sun.tools.ws.processor.modeler.annotation.AnnotationProcessorContext)
fld protected boolean endpointReferencesInterface
fld protected boolean hasWebMethods
fld protected boolean processingSei
fld protected boolean pushedSoapBinding
fld protected boolean wrapped
fld protected com.sun.tools.ws.processor.model.Port port
fld protected com.sun.tools.ws.processor.modeler.annotation.AnnotationProcessorContext context
fld protected com.sun.tools.ws.processor.modeler.annotation.AnnotationProcessorContext$SeiContext seiContext
fld protected com.sun.tools.ws.processor.modeler.annotation.ModelBuilder builder
fld protected com.sun.tools.ws.wsdl.document.soap.SOAPStyle soapStyle
fld protected java.lang.String portName
fld protected java.lang.String serviceName
fld protected java.lang.String typeNamespace
fld protected java.lang.String wsdlNamespace
fld protected java.util.Set<java.lang.String> processedMethods
fld protected java.util.Stack<javax.jws.soap.SOAPBinding> soapBindingStack
fld protected javax.jws.soap.SOAPBinding typeElementSoapBinding
fld protected javax.lang.model.element.Name endpointInterfaceName
fld protected javax.lang.model.element.Name packageName
fld protected javax.lang.model.element.Name serviceImplName
fld protected javax.lang.model.element.TypeElement typeElement
innr protected static MySoapBinding
meth protected abstract void processMethod(javax.lang.model.element.ExecutableElement,javax.jws.WebMethod)
meth protected abstract void processWebService(javax.jws.WebService,javax.lang.model.element.TypeElement)
meth protected boolean classImplementsSei(javax.lang.model.element.TypeElement,javax.lang.model.element.TypeElement)
meth protected boolean hasWebMethods(javax.lang.model.element.TypeElement)
meth protected boolean isDocLitWrapped()
meth protected boolean isEquivalentModes(javax.jws.WebParam$Mode,javax.jws.WebParam$Mode)
meth protected boolean isHolder(javax.lang.model.element.VariableElement)
meth protected boolean isLegalImplementation(javax.jws.WebService,javax.lang.model.element.TypeElement)
meth protected boolean isLegalMethod(javax.lang.model.element.ExecutableElement,javax.lang.model.element.TypeElement)
meth protected boolean isLegalParameter(javax.lang.model.element.VariableElement,javax.lang.model.element.ExecutableElement,javax.lang.model.element.TypeElement,int)
meth protected boolean isLegalSei(javax.lang.model.element.TypeElement)
meth protected boolean isLegalType(javax.lang.model.type.TypeMirror)
meth protected boolean isValidOneWayMethod(javax.lang.model.element.ExecutableElement,javax.lang.model.element.TypeElement)
meth protected boolean methodsAreLegal(javax.lang.model.element.TypeElement)
meth protected boolean processedMethod(javax.lang.model.element.ExecutableElement)
meth protected boolean pushSoapBinding(javax.jws.soap.SOAPBinding,javax.lang.model.element.Element,javax.lang.model.element.TypeElement)
meth protected boolean sameMethod(javax.lang.model.element.ExecutableElement,javax.lang.model.element.ExecutableElement)
meth protected boolean shouldProcessMethod(javax.lang.model.element.ExecutableElement,javax.jws.WebMethod)
meth protected boolean shouldProcessWebService(javax.jws.WebService,javax.lang.model.element.TypeElement)
meth protected int getModeParameterCount(javax.lang.model.element.ExecutableElement,javax.jws.WebParam$Mode)
meth protected java.lang.String getNamespace(javax.lang.model.element.PackageElement)
meth protected javax.jws.soap.SOAPBinding popSoapBinding()
meth protected javax.lang.model.element.VariableElement getOutParameter(javax.lang.model.element.ExecutableElement)
meth protected void checkForInvalidImplAnnotation(javax.lang.model.element.Element,java.lang.Class)
meth protected void checkForInvalidSeiAnnotation(javax.lang.model.element.TypeElement,java.lang.Class)
meth protected void postProcessWebService(javax.jws.WebService,javax.lang.model.element.TypeElement)
meth protected void preProcessWebService(javax.jws.WebService,javax.lang.model.element.TypeElement)
meth protected void processMethods(javax.lang.model.element.TypeElement)
meth protected void verifyImplAnnotations(javax.lang.model.element.TypeElement)
meth protected void verifySeiAnnotations(javax.jws.WebService,javax.lang.model.element.TypeElement)
meth public java.lang.Void visitExecutable(javax.lang.model.element.ExecutableElement,java.lang.Object)
meth public java.lang.Void visitType(javax.lang.model.element.TypeElement,java.lang.Object)
meth public static boolean sameStyle(javax.jws.soap.SOAPBinding$Style,com.sun.tools.ws.wsdl.document.soap.SOAPStyle)
supr javax.lang.model.util.SimpleElementVisitor6<java.lang.Void,java.lang.Object>
hfds NO_TYPE_VISITOR
hcls NoTypeVisitor

CLSS protected static com.sun.tools.ws.processor.modeler.annotation.WebServiceVisitor$MySoapBinding
 outer com.sun.tools.ws.processor.modeler.annotation.WebServiceVisitor
cons protected init()
intf javax.jws.soap.SOAPBinding
meth public java.lang.Class<? extends java.lang.annotation.Annotation> annotationType()
meth public javax.jws.soap.SOAPBinding$ParameterStyle parameterStyle()
meth public javax.jws.soap.SOAPBinding$Style style()
meth public javax.jws.soap.SOAPBinding$Use use()
supr java.lang.Object

CLSS public com.sun.tools.ws.processor.modeler.annotation.WebServiceWrapperGenerator
cons public init(com.sun.tools.ws.processor.modeler.annotation.ModelBuilder,com.sun.tools.ws.processor.modeler.annotation.AnnotationProcessorContext)
meth protected boolean isWSDLException(java.util.Collection<com.sun.tools.ws.processor.modeler.annotation.MemberInfo>,javax.lang.model.element.TypeElement)
meth protected com.sun.codemodel.JDefinedClass getCMClass(java.lang.String,com.sun.codemodel.ClassType)
meth protected void doPostProcessWebService(javax.jws.WebService,javax.lang.model.element.TypeElement)
meth protected void postProcessWebService(javax.jws.WebService,javax.lang.model.element.TypeElement)
meth protected void processMethod(javax.lang.model.element.ExecutableElement,javax.jws.WebMethod)
meth protected void processWebService(javax.jws.WebService,javax.lang.model.element.TypeElement)
supr com.sun.tools.ws.processor.modeler.annotation.WebServiceVisitor
hfds FIELD_FACTORY,ap_generator,cm,makeSafeVisitor,processedExceptions,wrapperNames
hcls ApWrapperBeanGenerator,FieldFactory

CLSS public com.sun.tools.ws.processor.modeler.annotation.WrapperInfo
cons public init()
cons public init(java.lang.String)
fld public java.lang.String wrapperName
meth public java.lang.String getWrapperName()
meth public void setWrapperName(java.lang.String)
supr java.lang.Object

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
fld protected boolean useWSIBasicProfile
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
innr public ProcessSOAPOperationInfo
innr public static WSDLExceptionInfo
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

CLSS public com.sun.tools.ws.processor.modeler.wsdl.WSDLModelerBase$ProcessSOAPOperationInfo
 outer com.sun.tools.ws.processor.modeler.wsdl.WSDLModelerBase
cons public init(com.sun.tools.ws.processor.modeler.wsdl.WSDLModelerBase,com.sun.tools.ws.processor.model.Port,com.sun.tools.ws.wsdl.document.Port,com.sun.tools.ws.wsdl.document.Operation,com.sun.tools.ws.wsdl.document.BindingOperation,com.sun.tools.ws.wsdl.document.soap.SOAPBinding,com.sun.tools.ws.wsdl.document.WSDLDocument,boolean,java.util.Map)
fld public boolean hasOverloadedOperations
fld public com.sun.tools.ws.processor.model.Operation operation
fld public com.sun.tools.ws.processor.model.Port modelPort
fld public com.sun.tools.ws.wsdl.document.BindingOperation bindingOperation
fld public com.sun.tools.ws.wsdl.document.Operation portTypeOperation
fld public com.sun.tools.ws.wsdl.document.Port port
fld public com.sun.tools.ws.wsdl.document.WSDLDocument document
fld public com.sun.tools.ws.wsdl.document.soap.SOAPBinding soapBinding
fld public java.lang.String uniqueOperationName
fld public java.util.Map headers
supr java.lang.Object

CLSS public static com.sun.tools.ws.processor.modeler.wsdl.WSDLModelerBase$WSDLExceptionInfo
 outer com.sun.tools.ws.processor.modeler.wsdl.WSDLModelerBase
cons public init()
fld public java.lang.String exceptionType
fld public java.lang.String wsdlMessagePartName
fld public java.util.HashMap constructorOrder
fld public javax.xml.namespace.QName wsdlMessage
supr java.lang.Object

CLSS public com.sun.tools.ws.processor.util.ClassNameCollector
cons public init()
intf com.sun.tools.ws.processor.model.jaxb.JAXBTypeVisitor
meth protected boolean shouldVisit(com.sun.tools.ws.processor.model.Port)
meth protected void postVisit(com.sun.tools.ws.processor.model.Model) throws java.lang.Exception
meth protected void postVisit(com.sun.tools.ws.processor.model.Port) throws java.lang.Exception
meth protected void preVisit(com.sun.tools.ws.processor.model.Fault) throws java.lang.Exception
meth protected void preVisit(com.sun.tools.ws.processor.model.Port) throws java.lang.Exception
meth protected void preVisit(com.sun.tools.ws.processor.model.Service) throws java.lang.Exception
meth protected void processPort11x(com.sun.tools.ws.processor.model.Port)
meth protected void visit(com.sun.tools.ws.processor.model.Parameter) throws java.lang.Exception
meth protected void visitBlock(com.sun.tools.ws.processor.model.Block) throws java.lang.Exception
meth protected void visitBodyBlock(com.sun.tools.ws.processor.model.Block) throws java.lang.Exception
meth protected void visitFaultBlock(com.sun.tools.ws.processor.model.Block) throws java.lang.Exception
meth protected void visitHeaderBlock(com.sun.tools.ws.processor.model.Block) throws java.lang.Exception
meth public java.util.Set getConflictingClassNames()
meth public java.util.Set<java.lang.String> getExceptionClassNames()
meth public java.util.Set<java.lang.String> getJaxbGeneratedClassNames()
meth public java.util.Set<java.lang.String> getSeiClassNames()
meth public void process(com.sun.tools.ws.processor.model.Model)
meth public void visit(com.sun.tools.ws.processor.model.jaxb.JAXBType) throws java.lang.Exception
meth public void visit(com.sun.tools.ws.processor.model.jaxb.RpcLitStructure) throws java.lang.Exception
supr com.sun.tools.ws.processor.model.ExtendedModelVisitor
hfds _allClassNames,_conflictingClassNames,_exceptionClassNames,_exceptions,_jaxbGeneratedClassNames,_portTypeNames,_seiClassNames,_wsdlBindingNames,doneVisitingJAXBModel

CLSS public com.sun.tools.ws.processor.util.DirectoryUtil
cons public init()
meth public static java.io.File getOutputDirectoryFor(java.lang.String,java.io.File)
meth public static java.lang.String getRelativePathfromCommonBase(java.io.File,java.io.File) throws java.io.IOException
supr java.lang.Object

CLSS public com.sun.tools.ws.processor.util.IndentingWriter
cons public init(java.io.Writer)
cons public init(java.io.Writer,int)
meth protected boolean canEncode(java.lang.String)
meth protected void checkWrite() throws java.io.IOException
meth protected void indentIn()
meth protected void indentOut()
meth public void newLine() throws java.io.IOException
meth public void p(java.lang.Object) throws java.io.IOException
meth public void p(java.lang.String) throws java.io.IOException
meth public void p(java.lang.String,java.lang.String) throws java.io.IOException
meth public void p(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public void p(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public void p(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public void pF(java.lang.String,java.lang.Object[]) throws java.io.IOException
meth public void pFln(java.lang.String,java.lang.Object[]) throws java.io.IOException
meth public void pI()
meth public void pI(int)
meth public void pM(java.lang.String) throws java.io.IOException
meth public void pMO(java.lang.String) throws java.io.IOException
meth public void pMOln(java.lang.String) throws java.io.IOException
meth public void pMln(java.lang.String) throws java.io.IOException
meth public void pMlnI(java.lang.String) throws java.io.IOException
meth public void pO()
meth public void pO(int)
meth public void pO(java.lang.Object) throws java.io.IOException
meth public void pO(java.lang.String) throws java.io.IOException
meth public void pOln(java.lang.Object) throws java.io.IOException
meth public void pOln(java.lang.String) throws java.io.IOException
meth public void pOlnI(java.lang.Object) throws java.io.IOException
meth public void pOlnI(java.lang.String) throws java.io.IOException
meth public void pln() throws java.io.IOException
meth public void pln(java.lang.Object) throws java.io.IOException
meth public void pln(java.lang.String) throws java.io.IOException
meth public void pln(java.lang.String,java.lang.String) throws java.io.IOException
meth public void pln(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public void pln(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public void pln(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public void plnI(java.lang.Object) throws java.io.IOException
meth public void plnI(java.lang.String) throws java.io.IOException
meth public void write(char[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
meth public void write(java.lang.String,int,int) throws java.io.IOException
supr java.io.BufferedWriter
hfds beginningOfLine,currentIndent,indentStep

CLSS public com.sun.tools.ws.util.xml.XmlUtil
cons public init()
meth public static boolean matchesTagNS(org.w3c.dom.Element,java.lang.String,java.lang.String)
meth public static boolean matchesTagNS(org.w3c.dom.Element,javax.xml.namespace.QName)
supr com.sun.xml.ws.util.xml.XmlUtil

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
hfds password,url,user

CLSS public com.sun.tools.ws.wscompile.BadCommandLineException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
meth public com.sun.tools.ws.wscompile.Options getOptions()
 anno 0 com.sun.istack.Nullable()
meth public void initOptions(com.sun.tools.ws.wscompile.Options)
supr java.lang.Exception
hfds options

CLSS public com.sun.tools.ws.wscompile.DefaultAuthenticator
cons public init(com.sun.tools.ws.wscompile.ErrorReceiver,java.io.File) throws com.sun.tools.ws.wscompile.BadCommandLineException
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
fld public final static java.lang.String defaultAuthfile
meth protected java.net.PasswordAuthentication getPasswordAuthentication()
supr java.net.Authenticator
hfds authFile,authInfo,errReceiver,giveError,proxyPasswd,proxyUser

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
cons public init(java.io.File,com.sun.tools.ws.wscompile.Options) throws java.io.IOException
meth public java.io.Writer openSource(com.sun.codemodel.JPackage,java.lang.String) throws java.io.IOException
meth public void close() throws java.io.IOException
supr com.sun.tools.ws.wscompile.WSCodeWriter
hfds filer,w

CLSS public com.sun.tools.ws.wscompile.Options
cons public init()
fld public boolean debug
fld public boolean debugMode
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
fld public java.io.File targetDir
fld public java.lang.String classpath
fld public java.lang.String encoding
fld public javax.annotation.processing.Filer filer
innr public final static !enum Target
innr public final static WeAreDone
meth protected int parseArguments(java.lang.String[],int) throws com.sun.tools.ws.wscompile.BadCommandLineException
meth protected void addFile(java.lang.String) throws com.sun.tools.ws.wscompile.BadCommandLineException
meth public boolean isExtensionMode()
meth public java.lang.ClassLoader getClassLoader()
meth public java.lang.Iterable<java.io.File> getGeneratedFiles()
meth public java.lang.String requireArgument(java.lang.String,java.lang.String[],int) throws com.sun.tools.ws.wscompile.BadCommandLineException
meth public static java.net.URL fileToURL(java.io.File)
meth public static java.net.URL[] pathToURLs(java.lang.String)
meth public void addGeneratedFile(java.io.File)
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
fld public boolean protocolSet
fld public final static java.lang.String X_SOAP12 = "Xsoap1.2"
fld public java.io.File nonclassDestDir
fld public java.io.File wsgenReport
fld public java.lang.Class endpoint
fld public java.lang.String protocol
fld public java.util.Map<java.lang.String,java.lang.String> nonstdProtocols
fld public java.util.Set<java.lang.String> protocols
fld public javax.xml.namespace.QName portName
fld public javax.xml.namespace.QName serviceName
meth protected int parseArguments(java.lang.String[],int) throws com.sun.tools.ws.wscompile.BadCommandLineException
meth protected void addFile(java.lang.String)
meth public void validate() throws com.sun.tools.ws.wscompile.BadCommandLineException
supr com.sun.tools.ws.wscompile.Options
hfds HTTP,PORTNAME_OPTION,SERVICENAME_OPTION,SOAP11,endpoints,isImplClass,noWebServiceEndpoint

CLSS public com.sun.tools.ws.wscompile.WsgenTool
cons public init(java.io.OutputStream)
cons public init(java.io.OutputStream,com.sun.xml.ws.api.server.Container)
meth protected void usage(com.sun.tools.ws.wscompile.WsgenOptions)
meth public boolean buildModel(java.lang.String,com.sun.tools.ws.wscompile.WsgenTool$Listener) throws com.sun.tools.ws.wscompile.BadCommandLineException
meth public boolean run(java.lang.String[])
supr java.lang.Object
hfds container,options,out,round
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
fld public boolean disableSSLHostnameVerification
fld public boolean isGenerateJWS
fld public boolean noAddressingBbinding
fld public boolean useBaseResourceAndURLToLoadWSDL
fld public final java.util.List<com.sun.tools.ws.wscompile.Plugin> activePlugins
fld public java.io.File authFile
fld public java.io.File implDestDir
fld public java.lang.String clientjar
fld public java.lang.String defaultPackage
fld public java.lang.String implPortName
fld public java.lang.String implServiceName
fld public java.lang.String wsdlLocation
fld public java.util.HashMap<java.lang.String,java.lang.String> extensionOptions
fld public java.util.List<java.lang.String> cmdlineJars
fld public org.xml.sax.EntityResolver entityResolver
meth protected void addFile(java.lang.String) throws com.sun.tools.ws.wscompile.BadCommandLineException
meth public com.sun.codemodel.JCodeModel getCodeModel()
meth public com.sun.tools.xjc.api.SchemaCompiler getSchemaCompiler()
meth public final void parseArguments(java.lang.String[]) throws com.sun.tools.ws.wscompile.BadCommandLineException
meth public final void parseBindings(com.sun.tools.ws.wscompile.ErrorReceiver)
meth public int parseArguments(java.lang.String[],int) throws com.sun.tools.ws.wscompile.BadCommandLineException
meth public java.lang.String getExtensionOption(java.lang.String)
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
hfds allPlugins,bindingFiles,codeModel,handlerConfigs,jaxbCustomBindings,jaxwsCustomBindings,schemaCompiler,schemas,wsdls
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
hfds WSIMPORT,container,out

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
hfds errorReceiver,forest,options,xpath,xpf

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
cons public init(java.util.Map<java.lang.String,com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler>)
cons public init(java.util.Map<java.lang.String,com.sun.tools.ws.wsdl.parser.AbstractExtensionHandler>,com.sun.tools.ws.wscompile.ErrorReceiver)
meth protected javax.xml.namespace.QName getWSDLExtensionQName()
meth public boolean handlePortExtension(com.sun.tools.ws.api.wsdl.TWSDLParserContext,com.sun.tools.ws.api.wsdl.TWSDLExtensible,org.w3c.dom.Element)
meth public java.lang.String getNamespaceURI()
supr com.sun.tools.ws.wsdl.parser.W3CAddressingExtensionHandler

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
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
supr java.lang.Object
hfds errorReceiver,options,parentResolver

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

CLSS public com.sun.xml.messaging.saaj.SOAPExceptionImpl
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
meth public java.lang.String getMessage()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
supr javax.xml.soap.SOAPException
hfds cause

CLSS public com.sun.xml.messaging.saaj.soap.AttachmentPartImpl
cons public init()
cons public init(org.jvnet.mimepull.MIMEPart)
fld protected final static java.util.logging.Logger log
meth public boolean equals(java.lang.Object)
meth public byte[] getRawContentBytes() throws javax.xml.soap.SOAPException
meth public int getSize() throws javax.xml.soap.SOAPException
meth public java.io.InputStream getBase64Content() throws javax.xml.soap.SOAPException
meth public java.io.InputStream getRawContent() throws javax.xml.soap.SOAPException
meth public java.lang.Object getContent() throws javax.xml.soap.SOAPException
meth public java.lang.String[] getMimeHeader(java.lang.String)
meth public java.util.Iterator getAllMimeHeaders()
meth public java.util.Iterator getMatchingMimeHeaders(java.lang.String[])
meth public java.util.Iterator getNonMatchingMimeHeaders(java.lang.String[])
meth public javax.activation.DataHandler getDataHandler() throws javax.xml.soap.SOAPException
meth public javax.xml.soap.MimeHeaders getMimeHeaders()
meth public static void copyMimeHeaders(com.sun.xml.messaging.saaj.packaging.mime.internet.MimeBodyPart,com.sun.xml.messaging.saaj.soap.AttachmentPartImpl) throws javax.xml.soap.SOAPException
meth public static void copyMimeHeaders(javax.xml.soap.MimeHeaders,com.sun.xml.messaging.saaj.packaging.mime.internet.MimeBodyPart) throws javax.xml.soap.SOAPException
meth public void addMimeHeader(java.lang.String,java.lang.String)
meth public void clearContent()
meth public void removeAllMimeHeaders()
meth public void removeMimeHeader(java.lang.String)
meth public void setBase64Content(java.io.InputStream,java.lang.String) throws javax.xml.soap.SOAPException
meth public void setContent(java.lang.Object,java.lang.String)
meth public void setDataHandler(javax.activation.DataHandler)
meth public void setMimeHeader(java.lang.String,java.lang.String)
meth public void setRawContent(java.io.InputStream,java.lang.String) throws javax.xml.soap.SOAPException
meth public void setRawContentBytes(byte[],int,int,java.lang.String) throws javax.xml.soap.SOAPException
supr javax.xml.soap.AttachmentPart
hfds dataHandler,headers,mimePart,rawContent

CLSS public abstract interface com.sun.xml.messaging.saaj.soap.Envelope
intf javax.xml.soap.SOAPEnvelope
meth public abstract javax.xml.transform.Source getContent()
meth public abstract void output(java.io.OutputStream) throws java.io.IOException
meth public abstract void output(java.io.OutputStream,boolean) throws java.io.IOException

CLSS public com.sun.xml.messaging.saaj.soap.EnvelopeFactory
cons public init()
fld protected final static java.util.logging.Logger log
meth public static com.sun.xml.messaging.saaj.soap.Envelope createEnvelope(javax.xml.transform.Source,com.sun.xml.messaging.saaj.soap.SOAPPartImpl) throws javax.xml.soap.SOAPException
supr java.lang.Object
hfds parserPool

CLSS public com.sun.xml.messaging.saaj.soap.FastInfosetDataContentHandler
cons public init()
fld public final java.lang.String STR_SRC = "org.jvnet.fastinfoset.FastInfosetSource"
intf javax.activation.DataContentHandler
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getContent(javax.activation.DataSource) throws java.io.IOException
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor,javax.activation.DataSource) throws java.io.IOException
meth public void writeTo(java.lang.Object,java.lang.String,java.io.OutputStream) throws java.io.IOException
supr java.lang.Object

CLSS public com.sun.xml.messaging.saaj.soap.GifDataContentHandler
cons public init()
intf javax.activation.DataContentHandler
meth protected javax.activation.ActivationDataFlavor getDF()
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getContent(javax.activation.DataSource) throws java.io.IOException
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor,javax.activation.DataSource) throws java.io.IOException
meth public void writeTo(java.lang.Object,java.lang.String,java.io.OutputStream) throws java.io.IOException
supr java.awt.Component
hfds myDF

CLSS public com.sun.xml.messaging.saaj.soap.ImageDataContentHandler
cons public init()
fld protected final static java.util.logging.Logger log
intf javax.activation.DataContentHandler
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getContent(javax.activation.DataSource) throws java.io.IOException
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor,javax.activation.DataSource) throws java.io.IOException
meth public void writeTo(java.lang.Object,java.lang.String,java.io.OutputStream) throws java.io.IOException
supr java.awt.Component
hfds flavor

CLSS public com.sun.xml.messaging.saaj.soap.JpegDataContentHandler
cons public init()
fld public final java.lang.String STR_SRC = "java.awt.Image"
intf javax.activation.DataContentHandler
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getContent(javax.activation.DataSource)
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor,javax.activation.DataSource)
meth public void writeTo(java.lang.Object,java.lang.String,java.io.OutputStream) throws java.io.IOException
supr java.awt.Component

CLSS public com.sun.xml.messaging.saaj.soap.MessageFactoryImpl
cons public init()
fld protected boolean lazyAttachments
fld protected final static java.util.logging.Logger log
fld protected java.io.OutputStream listener
meth protected final static java.lang.String getContentType(javax.xml.soap.MimeHeaders)
meth public java.io.OutputStream listen(java.io.OutputStream)
meth public javax.xml.soap.SOAPMessage createMessage() throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPMessage createMessage(boolean,boolean) throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPMessage createMessage(javax.xml.soap.MimeHeaders,java.io.InputStream) throws java.io.IOException,javax.xml.soap.SOAPException
meth public void setLazyAttachmentOptimization(boolean)
supr javax.xml.soap.MessageFactory

CLSS public abstract com.sun.xml.messaging.saaj.soap.MessageImpl
cons protected init()
cons protected init(boolean,boolean)
cons protected init(javax.xml.soap.MimeHeaders,com.sun.xml.messaging.saaj.packaging.mime.internet.ContentType,int,java.io.InputStream) throws com.sun.xml.messaging.saaj.SOAPExceptionImpl
cons protected init(javax.xml.soap.MimeHeaders,java.io.InputStream) throws com.sun.xml.messaging.saaj.SOAPExceptionImpl
cons protected init(javax.xml.soap.SOAPMessage)
fld protected boolean acceptFastInfoset
fld protected boolean attachmentsInitialized
fld protected boolean isFastInfoset
fld protected boolean saved
fld protected byte[] messageBytes
fld protected com.sun.xml.messaging.saaj.packaging.mime.internet.ContentType contentType
fld protected com.sun.xml.messaging.saaj.packaging.mime.internet.MimeMultipart mmp
fld protected com.sun.xml.messaging.saaj.packaging.mime.internet.MimeMultipart multiPart
fld protected com.sun.xml.messaging.saaj.soap.SOAPPartImpl soapPartImpl
fld protected com.sun.xml.messaging.saaj.util.FinalArrayList attachments
fld protected final static int FI_ENCODED_FLAG = 16
fld protected final static int MIME_MULTIPART_FLAG = 2
fld protected final static int MIME_MULTIPART_XOP_SOAP1_1_FLAG = 6
fld protected final static int MIME_MULTIPART_XOP_SOAP1_2_FLAG = 10
fld protected final static int PLAIN_XML_FLAG = 1
fld protected final static int SOAP1_1_FLAG = 4
fld protected final static int SOAP1_2_FLAG = 8
fld protected final static int XOP_FLAG = 13
fld protected final static java.util.logging.Logger log
fld protected int messageByteCount
fld protected java.util.HashMap properties
fld protected javax.xml.soap.MimeHeaders headers
fld public final static java.lang.String CONTENT_ID = "Content-ID"
fld public final static java.lang.String CONTENT_LOCATION = "Content-Location"
intf javax.xml.soap.SOAPConstants
meth protected abstract boolean isCorrectSoapVersion(int)
meth protected abstract java.lang.String getExpectedAcceptHeader()
meth protected abstract java.lang.String getExpectedContentType()
meth protected static boolean isSoap1_1Content(int)
meth protected static boolean isSoap1_2Content(int)
meth public abstract javax.xml.soap.SOAPPart getSOAPPart()
meth public boolean acceptFastInfoset()
meth public boolean isFastInfoset()
meth public boolean saveRequired()
meth public int countAttachments()
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.String getAction()
meth public java.lang.String getBaseType()
meth public java.lang.String getCharset()
meth public java.lang.String getContentDescription()
meth public java.lang.String getContentType()
meth public java.util.Iterator getAttachments()
meth public java.util.Iterator getAttachments(javax.xml.soap.MimeHeaders)
meth public javax.xml.soap.AttachmentPart createAttachmentPart()
meth public javax.xml.soap.AttachmentPart getAttachment(javax.xml.soap.SOAPElement) throws javax.xml.soap.SOAPException
meth public javax.xml.soap.MimeHeaders getMimeHeaders()
meth public javax.xml.soap.SOAPBody getSOAPBody() throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPHeader getSOAPHeader() throws javax.xml.soap.SOAPException
meth public void addAttachmentPart(javax.xml.soap.AttachmentPart)
meth public void removeAllAttachments()
meth public void removeAttachments(javax.xml.soap.MimeHeaders)
meth public void saveChanges() throws javax.xml.soap.SOAPException
meth public void setAction(java.lang.String)
meth public void setBaseType(java.lang.String)
meth public void setCharset(java.lang.String)
meth public void setContentDescription(java.lang.String)
meth public void setContentType(java.lang.String)
meth public void setIsFastInfoset(boolean)
meth public void setLazyAttachments(boolean)
meth public void setProperty(java.lang.String,java.lang.Object)
meth public void writeTo(java.io.OutputStream) throws java.io.IOException,javax.xml.soap.SOAPException
supr javax.xml.soap.SOAPMessage
hfds inputStreamAfterSaveChanges,lazyAttachments,nullIter,optimizeAttachmentProcessing,switchOffBM,switchOffLazyAttachment,useMimePull
hcls MimeMatchingIterator

CLSS public com.sun.xml.messaging.saaj.soap.MultipartDataContentHandler
cons public init()
intf javax.activation.DataContentHandler
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getContent(javax.activation.DataSource)
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor,javax.activation.DataSource)
meth public void writeTo(java.lang.Object,java.lang.String,java.io.OutputStream) throws java.io.IOException
supr java.lang.Object
hfds myDF

CLSS public com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl
cons public init()
fld protected final static java.util.logging.Logger log
meth protected javax.xml.soap.MessageFactory newMessageFactory(java.lang.String) throws javax.xml.soap.SOAPException
meth protected javax.xml.soap.SOAPFactory newSOAPFactory(java.lang.String) throws javax.xml.soap.SOAPException
supr javax.xml.soap.SAAJMetaFactory

CLSS public abstract interface com.sun.xml.messaging.saaj.soap.SOAPDocument
meth public abstract com.sun.xml.messaging.saaj.soap.SOAPDocumentImpl getDocument()
meth public abstract com.sun.xml.messaging.saaj.soap.SOAPPartImpl getSOAPPart()

CLSS public com.sun.xml.messaging.saaj.soap.SOAPDocumentFragment
cons public init()
cons public init(com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl)
supr com.sun.org.apache.xerces.internal.dom.DocumentFragmentImpl

CLSS public com.sun.xml.messaging.saaj.soap.SOAPDocumentImpl
cons public init(com.sun.xml.messaging.saaj.soap.SOAPPartImpl)
fld protected final static java.util.logging.Logger log
intf com.sun.xml.messaging.saaj.soap.SOAPDocument
meth protected org.w3c.dom.Element doGetDocumentElement()
meth public com.sun.xml.messaging.saaj.soap.SOAPDocumentImpl getDocument()
meth public com.sun.xml.messaging.saaj.soap.SOAPPartImpl getSOAPPart()
meth public org.w3c.dom.Attr createAttribute(java.lang.String)
meth public org.w3c.dom.Attr createAttributeNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.CDATASection createCDATASection(java.lang.String)
meth public org.w3c.dom.Comment createComment(java.lang.String)
meth public org.w3c.dom.DOMImplementation getImplementation()
meth public org.w3c.dom.DocumentFragment createDocumentFragment()
meth public org.w3c.dom.DocumentType getDoctype()
meth public org.w3c.dom.Element createElement(java.lang.String)
meth public org.w3c.dom.Element createElementNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.Element getDocumentElement()
meth public org.w3c.dom.Element getElementById(java.lang.String)
meth public org.w3c.dom.EntityReference createEntityReference(java.lang.String)
meth public org.w3c.dom.Node cloneNode(boolean)
meth public org.w3c.dom.Node importNode(org.w3c.dom.Node,boolean)
meth public org.w3c.dom.NodeList getElementsByTagName(java.lang.String)
meth public org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.ProcessingInstruction createProcessingInstruction(java.lang.String,java.lang.String)
meth public org.w3c.dom.Text createTextNode(java.lang.String)
meth public void cloneNode(com.sun.xml.messaging.saaj.soap.SOAPDocumentImpl,boolean)
supr com.sun.org.apache.xerces.internal.dom.DocumentImpl
hfds XMLNS,enclosingSOAPPart

CLSS public abstract com.sun.xml.messaging.saaj.soap.SOAPFactoryImpl
cons public init()
fld protected final static java.util.logging.Logger log
meth protected abstract com.sun.xml.messaging.saaj.soap.SOAPDocumentImpl createDocument()
meth public javax.xml.soap.Detail createDetail() throws javax.xml.soap.SOAPException
meth public javax.xml.soap.Name createName(java.lang.String) throws javax.xml.soap.SOAPException
meth public javax.xml.soap.Name createName(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPElement createElement(java.lang.String) throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPElement createElement(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPElement createElement(javax.xml.namespace.QName) throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPElement createElement(javax.xml.soap.Name) throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPElement createElement(org.w3c.dom.Element) throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPFault createFault() throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPFault createFault(java.lang.String,javax.xml.namespace.QName) throws javax.xml.soap.SOAPException
supr javax.xml.soap.SOAPFactory

CLSS public com.sun.xml.messaging.saaj.soap.SOAPIOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
supr java.io.IOException
hfds soapException

CLSS public abstract com.sun.xml.messaging.saaj.soap.SOAPPartImpl
cons protected init()
cons protected init(com.sun.xml.messaging.saaj.soap.MessageImpl)
fld protected boolean omitXmlDecl
fld protected com.sun.xml.messaging.saaj.soap.Envelope envelope
fld protected com.sun.xml.messaging.saaj.soap.MessageImpl message
fld protected com.sun.xml.messaging.saaj.soap.SOAPDocumentImpl document
fld protected final static java.util.logging.Logger log
fld protected java.lang.String sourceCharsetEncoding
fld protected javax.xml.soap.MimeHeaders headers
fld protected javax.xml.transform.Source source
intf com.sun.xml.messaging.saaj.soap.SOAPDocument
meth protected abstract com.sun.xml.messaging.saaj.soap.Envelope createEmptyEnvelope(java.lang.String) throws javax.xml.soap.SOAPException
meth protected abstract com.sun.xml.messaging.saaj.soap.Envelope createEnvelopeFromSource() throws javax.xml.soap.SOAPException
meth protected abstract com.sun.xml.messaging.saaj.soap.SOAPPartImpl duplicateType()
meth protected abstract java.lang.String getContentType()
meth protected com.sun.xml.messaging.saaj.soap.SOAPPartImpl doCloneNode()
meth protected com.sun.xml.messaging.saaj.util.XMLDeclarationParser lookForXmlDecl() throws javax.xml.soap.SOAPException
meth protected java.lang.String getContentTypeString()
meth protected void doGetDocumentElement()
meth protected void lookForEnvelope() throws javax.xml.soap.SOAPException
meth public boolean getStrictErrorChecking()
meth public boolean getXmlStandalone()
meth public boolean hasAttributes()
meth public boolean hasChildNodes()
meth public boolean isDefaultNamespace(java.lang.String)
meth public boolean isEqualNode(org.w3c.dom.Node)
meth public boolean isFastInfoset()
meth public boolean isSameNode(org.w3c.dom.Node)
meth public boolean isSupported(java.lang.String,java.lang.String)
meth public com.sun.xml.messaging.saaj.soap.SOAPDocumentImpl getDocument()
meth public com.sun.xml.messaging.saaj.soap.SOAPPartImpl getSOAPPart()
meth public java.io.InputStream getContentAsStream() throws java.io.IOException
meth public java.lang.Object getFeature(java.lang.String,java.lang.String)
meth public java.lang.Object getUserData(java.lang.String)
meth public java.lang.Object setUserData(java.lang.String,java.lang.Object,org.w3c.dom.UserDataHandler)
meth public java.lang.String getBaseURI()
meth public java.lang.String getDocumentURI()
meth public java.lang.String getInputEncoding()
meth public java.lang.String getLocalName()
meth public java.lang.String getNamespaceURI()
meth public java.lang.String getNodeName()
meth public java.lang.String getNodeValue()
meth public java.lang.String getPrefix()
meth public java.lang.String getSourceCharsetEncoding()
meth public java.lang.String getTextContent()
meth public java.lang.String getValue()
meth public java.lang.String getXmlEncoding()
meth public java.lang.String getXmlVersion()
meth public java.lang.String lookupNamespaceURI(java.lang.String)
meth public java.lang.String lookupPrefix(java.lang.String)
meth public java.lang.String[] getMimeHeader(java.lang.String)
meth public java.util.Iterator getAllMimeHeaders()
meth public java.util.Iterator getMatchingMimeHeaders(java.lang.String[])
meth public java.util.Iterator getNonMatchingMimeHeaders(java.lang.String[])
meth public javax.xml.soap.SOAPElement getParentElement()
meth public javax.xml.soap.SOAPEnvelope getEnvelope() throws javax.xml.soap.SOAPException
meth public javax.xml.transform.Source getContent() throws javax.xml.soap.SOAPException
meth public org.w3c.dom.Attr createAttribute(java.lang.String)
meth public org.w3c.dom.Attr createAttributeNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.CDATASection createCDATASection(java.lang.String)
meth public org.w3c.dom.Comment createComment(java.lang.String)
meth public org.w3c.dom.DOMConfiguration getDomConfig()
meth public org.w3c.dom.DOMImplementation getImplementation()
meth public org.w3c.dom.Document getOwnerDocument()
meth public org.w3c.dom.DocumentFragment createDocumentFragment()
meth public org.w3c.dom.DocumentType getDoctype()
meth public org.w3c.dom.Element createElement(java.lang.String)
meth public org.w3c.dom.Element createElementNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.Element getDocumentElement()
meth public org.w3c.dom.Element getElementById(java.lang.String)
meth public org.w3c.dom.EntityReference createEntityReference(java.lang.String)
meth public org.w3c.dom.NamedNodeMap getAttributes()
meth public org.w3c.dom.Node adoptNode(org.w3c.dom.Node)
meth public org.w3c.dom.Node appendChild(org.w3c.dom.Node)
meth public org.w3c.dom.Node cloneNode(boolean)
meth public org.w3c.dom.Node getFirstChild()
meth public org.w3c.dom.Node getLastChild()
meth public org.w3c.dom.Node getNextSibling()
meth public org.w3c.dom.Node getParentNode()
meth public org.w3c.dom.Node getPreviousSibling()
meth public org.w3c.dom.Node importNode(org.w3c.dom.Node,boolean)
meth public org.w3c.dom.Node insertBefore(org.w3c.dom.Node,org.w3c.dom.Node)
meth public org.w3c.dom.Node removeChild(org.w3c.dom.Node)
meth public org.w3c.dom.Node renameNode(org.w3c.dom.Node,java.lang.String,java.lang.String)
meth public org.w3c.dom.Node replaceChild(org.w3c.dom.Node,org.w3c.dom.Node)
meth public org.w3c.dom.NodeList getChildNodes()
meth public org.w3c.dom.NodeList getElementsByTagName(java.lang.String)
meth public org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.ProcessingInstruction createProcessingInstruction(java.lang.String,java.lang.String)
meth public org.w3c.dom.Text createTextNode(java.lang.String)
meth public short compareDocumentPosition(org.w3c.dom.Node)
meth public short getNodeType()
meth public void addMimeHeader(java.lang.String,java.lang.String)
meth public void detachNode()
meth public void normalize()
meth public void normalizeDocument()
meth public void recycleNode()
meth public void removeAllMimeHeaders()
meth public void removeMimeHeader(java.lang.String)
meth public void setContent(javax.xml.transform.Source) throws javax.xml.soap.SOAPException
meth public void setDocumentURI(java.lang.String)
meth public void setMimeHeader(java.lang.String,java.lang.String)
meth public void setNodeValue(java.lang.String)
meth public void setParentElement(javax.xml.soap.SOAPElement) throws javax.xml.soap.SOAPException
meth public void setPrefix(java.lang.String)
meth public void setSourceCharsetEncoding(java.lang.String)
meth public void setStrictErrorChecking(boolean)
meth public void setTextContent(java.lang.String)
meth public void setValue(java.lang.String)
meth public void setXmlStandalone(boolean)
meth public void setXmlVersion(java.lang.String)
supr javax.xml.soap.SOAPPart
hfds lazyContentLength,sourceWasSet

CLSS public com.sun.xml.messaging.saaj.soap.SOAPVersionMismatchException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr com.sun.xml.messaging.saaj.SOAPExceptionImpl

CLSS public com.sun.xml.messaging.saaj.soap.StringDataContentHandler
cons public init()
intf javax.activation.DataContentHandler
meth protected javax.activation.ActivationDataFlavor getDF()
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getContent(javax.activation.DataSource) throws java.io.IOException
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor,javax.activation.DataSource) throws java.io.IOException
meth public void writeTo(java.lang.Object,java.lang.String,java.io.OutputStream) throws java.io.IOException
supr java.lang.Object
hfds myDF

CLSS public com.sun.xml.messaging.saaj.soap.XmlDataContentHandler
cons public init() throws java.lang.ClassNotFoundException
fld public final java.lang.String STR_SRC = "javax.xml.transform.stream.StreamSource"
intf javax.activation.DataContentHandler
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getContent(javax.activation.DataSource) throws java.io.IOException
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor,javax.activation.DataSource) throws java.io.IOException
meth public void writeTo(java.lang.Object,java.lang.String,java.io.OutputStream) throws java.io.IOException
supr java.lang.Object
hfds streamSourceClass

CLSS public abstract interface com.sun.xml.ws.Closeable
intf java.io.Closeable
meth public abstract void close()

CLSS public abstract interface com.sun.xml.ws.addressing.v200408.MemberSubmissionAddressingConstants
fld public final static java.lang.String ACTION_NOT_SUPPORTED_TEXT = "The \u0022%s\u0022 cannot be processed at the receiver."
fld public final static java.lang.String ANONYMOUS_EPR = "<EndpointReference xmlns=\u0022http://schemas.xmlsoap.org/ws/2004/08/addressing\u0022>\n    <Address>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</Address>\n</EndpointReference>"
fld public final static java.lang.String DESTINATION_UNREACHABLE_TEXT = "No route can be determined to reach the destination role defined by the WS-Addressing To."
fld public final static java.lang.String ENDPOINT_UNAVAILABLE_TEXT = "The endpoint is unable to process the message at this time."
fld public final static java.lang.String INVALID_MAP_TEXT = "A message information header is not valid and the message cannot be processed."
fld public final static java.lang.String MAP_REQUIRED_TEXT = "A required message information header, To, MessageID, or Action, is not present."
fld public final static java.lang.String MEX_METADATA_DIALECT_ATTRIBUTE = "Dialect"
fld public final static java.lang.String MEX_METADATA_DIALECT_VALUE = "http://schemas.xmlsoap.org/wsdl/"
fld public final static java.lang.String WSA_ADDRESS_NAME = "Address"
fld public final static java.lang.String WSA_ANONYMOUS_ADDRESS = "http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous"
fld public final static java.lang.String WSA_DEFAULT_FAULT_ACTION = "http://schemas.xmlsoap.org/ws/2004/08/addressing/fault"
fld public final static java.lang.String WSA_NAMESPACE_NAME = "http://schemas.xmlsoap.org/ws/2004/08/addressing"
fld public final static java.lang.String WSA_NAMESPACE_POLICY_NAME = "http://schemas.xmlsoap.org/ws/2004/08/addressing/policy"
fld public final static java.lang.String WSA_NAMESPACE_WSDL_NAME = "http://schemas.xmlsoap.org/ws/2004/08/addressing"
fld public final static java.lang.String WSA_NONE_ADDRESS = ""
fld public final static java.lang.String WSA_PORTNAME_NAME = "PortName"
fld public final static java.lang.String WSA_PORTTYPE_NAME = "PortType"
fld public final static java.lang.String WSA_SERVICENAME_NAME = "ServiceName"
fld public final static javax.xml.namespace.QName ACTION_NOT_SUPPORTED_QNAME
fld public final static javax.xml.namespace.QName DESTINATION_UNREACHABLE_QNAME
fld public final static javax.xml.namespace.QName ENDPOINT_UNAVAILABLE_QNAME
fld public final static javax.xml.namespace.QName FAULT_DETAIL_QNAME
fld public final static javax.xml.namespace.QName INVALID_MAP_QNAME
fld public final static javax.xml.namespace.QName MAP_REQUIRED_QNAME
fld public final static javax.xml.namespace.QName MEX_METADATA
fld public final static javax.xml.namespace.QName MEX_METADATA_SECTION
fld public final static javax.xml.namespace.QName PROBLEM_ACTION_QNAME
fld public final static javax.xml.namespace.QName PROBLEM_HEADER_QNAME_QNAME
fld public final static javax.xml.namespace.QName WSA_ADDRESS_QNAME

CLSS public abstract com.sun.xml.ws.api.BindingID
cons public init()
fld public final static com.sun.xml.ws.api.BindingID XML_HTTP
fld public final static com.sun.xml.ws.api.BindingID$SOAPHTTPImpl SOAP11_HTTP
fld public final static com.sun.xml.ws.api.BindingID$SOAPHTTPImpl SOAP11_HTTP_MTOM
fld public final static com.sun.xml.ws.api.BindingID$SOAPHTTPImpl SOAP12_HTTP
fld public final static com.sun.xml.ws.api.BindingID$SOAPHTTPImpl SOAP12_HTTP_MTOM
fld public final static com.sun.xml.ws.api.BindingID$SOAPHTTPImpl X_SOAP12_HTTP
meth public !varargs final com.sun.xml.ws.api.WSBinding createBinding(javax.xml.ws.WebServiceFeature[])
 anno 0 com.sun.istack.NotNull()
meth public abstract com.sun.xml.ws.api.SOAPVersion getSOAPVersion()
meth public abstract com.sun.xml.ws.api.pipe.Codec createEncoder(com.sun.xml.ws.api.WSBinding)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public abstract java.lang.String toString()
meth public boolean canGenerateWSDL()
meth public boolean equals(java.lang.Object)
meth public com.sun.xml.ws.binding.WebServiceFeatureList createBuiltinFeatureList()
meth public final com.sun.xml.ws.api.WSBinding createBinding()
 anno 0 com.sun.istack.NotNull()
meth public final com.sun.xml.ws.api.WSBinding createBinding(com.sun.xml.ws.api.WSFeatureList)
 anno 0 com.sun.istack.NotNull()
meth public int hashCode()
meth public java.lang.String getParameter(java.lang.String,java.lang.String)
meth public java.lang.String getTransport()
 anno 0 com.sun.istack.NotNull()
meth public static com.sun.xml.ws.api.BindingID parse(java.lang.Class<?>)
 anno 0 com.sun.istack.NotNull()
meth public static com.sun.xml.ws.api.BindingID parse(java.lang.String)
 anno 0 com.sun.istack.NotNull()
supr java.lang.Object
hfds REST_HTTP
hcls Impl,SOAPHTTPImpl

CLSS public abstract com.sun.xml.ws.api.BindingIDFactory
cons public init()
meth public abstract com.sun.xml.ws.api.BindingID parse(java.lang.String)
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.NotNull()
supr java.lang.Object

CLSS public abstract interface com.sun.xml.ws.api.Cancelable
meth public abstract void cancel(boolean)

CLSS public abstract interface com.sun.xml.ws.api.Component
meth public abstract <%0 extends java.lang.Object> {%%0} getSPI(java.lang.Class<{%%0}>)
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.NotNull()

CLSS public abstract interface com.sun.xml.ws.api.ComponentEx
intf com.sun.xml.ws.api.Component
meth public abstract <%0 extends java.lang.Object> java.lang.Iterable<{%%0}> getIterableSPI(java.lang.Class<{%%0}>)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()

CLSS public com.sun.xml.ws.api.ComponentFeature
cons public init(com.sun.xml.ws.api.Component)
cons public init(com.sun.xml.ws.api.Component,com.sun.xml.ws.api.ComponentFeature$Target)
innr public final static !enum Target
intf com.sun.xml.ws.api.ServiceSharedFeatureMarker
meth public com.sun.xml.ws.api.Component getComponent()
meth public com.sun.xml.ws.api.ComponentFeature$Target getTarget()
meth public java.lang.String getID()
supr javax.xml.ws.WebServiceFeature
hfds component,target

CLSS public final static !enum com.sun.xml.ws.api.ComponentFeature$Target
 outer com.sun.xml.ws.api.ComponentFeature
fld public final static com.sun.xml.ws.api.ComponentFeature$Target CONTAINER
fld public final static com.sun.xml.ws.api.ComponentFeature$Target ENDPOINT
fld public final static com.sun.xml.ws.api.ComponentFeature$Target SERVICE
fld public final static com.sun.xml.ws.api.ComponentFeature$Target STUB
meth public static com.sun.xml.ws.api.ComponentFeature$Target valueOf(java.lang.String)
meth public static com.sun.xml.ws.api.ComponentFeature$Target[] values()
supr java.lang.Enum<com.sun.xml.ws.api.ComponentFeature$Target>

CLSS public abstract interface com.sun.xml.ws.api.ComponentRegistry
intf com.sun.xml.ws.api.Component
meth public abstract java.util.Set<com.sun.xml.ws.api.Component> getComponents()
 anno 0 com.sun.istack.NotNull()

CLSS public abstract com.sun.xml.ws.api.DistributedPropertySet
cons public init()
intf org.jvnet.ws.message.DistributedPropertySet
meth public <%0 extends org.jvnet.ws.message.PropertySet> {%%0} getSatellite(java.lang.Class<{%%0}>)
 anno 0 com.sun.istack.Nullable()
meth public boolean supports(java.lang.Object)
meth public java.lang.Object get(java.lang.Object)
meth public java.lang.Object put(java.lang.String,java.lang.Object)
meth public java.lang.Object remove(java.lang.Object)
meth public void addSatellite(com.sun.xml.ws.api.PropertySet)
 anno 1 com.sun.istack.NotNull()
meth public void addSatellite(java.lang.Class,com.sun.xml.ws.api.PropertySet)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public void addSatellite(java.lang.Class,org.jvnet.ws.message.PropertySet)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public void addSatellite(org.jvnet.ws.message.PropertySet)
meth public void copySatelliteInto(com.sun.xml.ws.api.DistributedPropertySet)
 anno 1 com.sun.istack.NotNull()
meth public void copySatelliteInto(org.jvnet.ws.message.MessageContext)
meth public void removeSatellite(org.jvnet.ws.message.PropertySet)
supr com.sun.xml.ws.api.PropertySet
hfds satellites

CLSS public final com.sun.xml.ws.api.EndpointAddress
cons public init(java.lang.String) throws java.net.URISyntaxException
cons public init(java.net.URI)
meth public java.lang.String toString()
meth public java.net.URI getURI()
meth public java.net.URL getURL()
meth public java.net.URLConnection openConnection() throws java.io.IOException
meth public static com.sun.xml.ws.api.EndpointAddress create(java.lang.String)
supr java.lang.Object
hfds dontUseProxyMethod,proxy,stringForm,uri,url

CLSS public abstract interface !annotation com.sun.xml.ws.api.FeatureConstructor
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String[] value()

CLSS public abstract interface com.sun.xml.ws.api.ImpliesWebServiceFeature
meth public abstract void implyFeatures(com.sun.xml.ws.api.WSFeatureList)

CLSS public abstract com.sun.xml.ws.api.PropertySet
cons protected init()
innr protected abstract interface static Accessor
innr protected final static PropertyMap
intf org.jvnet.ws.message.PropertySet
meth protected abstract com.sun.xml.ws.api.PropertySet$PropertyMap getPropertyMap()
meth protected static com.sun.xml.ws.api.PropertySet$PropertyMap parse(java.lang.Class)
meth public boolean supports(java.lang.Object)
meth public final boolean containsKey(java.lang.Object)
meth public final java.util.Map<java.lang.String,java.lang.Object> createMapView()
meth public java.lang.Object get(java.lang.Object)
meth public java.lang.Object put(java.lang.String,java.lang.Object)
meth public java.lang.Object remove(java.lang.Object)
supr java.lang.Object
hfds mapViewCore
hcls FieldAccessor,MethodAccessor

CLSS protected abstract interface static com.sun.xml.ws.api.PropertySet$Accessor
 outer com.sun.xml.ws.api.PropertySet
meth public abstract boolean hasValue(com.sun.xml.ws.api.PropertySet)
meth public abstract java.lang.Object get(com.sun.xml.ws.api.PropertySet)
meth public abstract java.lang.String getName()
meth public abstract void set(com.sun.xml.ws.api.PropertySet,java.lang.Object)

CLSS protected final static com.sun.xml.ws.api.PropertySet$PropertyMap
 outer com.sun.xml.ws.api.PropertySet
cons protected init()
supr java.util.HashMap<java.lang.String,com.sun.xml.ws.api.PropertySet$Accessor>

CLSS public abstract com.sun.xml.ws.api.ResourceLoader
cons public init()
meth public abstract java.net.URL getResource(java.lang.String) throws java.net.MalformedURLException
supr java.lang.Object

CLSS public final !enum com.sun.xml.ws.api.SOAPVersion
fld public final java.lang.String contentType
fld public final java.lang.String httpBindingId
fld public final java.lang.String implicitRole
fld public final java.lang.String nsUri
fld public final java.lang.String roleAttributeName
fld public final java.util.Set<java.lang.String> implicitRoleSet
fld public final java.util.Set<java.lang.String> requiredRoles
fld public final javax.xml.namespace.QName faultCodeClient
fld public final javax.xml.namespace.QName faultCodeMustUnderstand
fld public final javax.xml.namespace.QName faultCodeServer
fld public final javax.xml.soap.MessageFactory saajMessageFactory
fld public final javax.xml.soap.SOAPFactory saajSoapFactory
fld public final static com.sun.xml.ws.api.SOAPVersion SOAP_11
fld public final static com.sun.xml.ws.api.SOAPVersion SOAP_12
meth public java.lang.String toString()
meth public javax.xml.soap.MessageFactory getMessageFactory()
meth public javax.xml.soap.SOAPFactory getSOAPFactory()
meth public static com.sun.xml.ws.api.SOAPVersion fromHttpBinding(java.lang.String)
meth public static com.sun.xml.ws.api.SOAPVersion fromNsUri(java.lang.String)
meth public static com.sun.xml.ws.api.SOAPVersion valueOf(java.lang.String)
meth public static com.sun.xml.ws.api.SOAPVersion[] values()
supr java.lang.Enum<com.sun.xml.ws.api.SOAPVersion>
hfds saajFactoryString

CLSS public abstract interface com.sun.xml.ws.api.ServiceSharedFeatureMarker

CLSS public abstract interface com.sun.xml.ws.api.WSBinding
intf javax.xml.ws.Binding
meth public abstract <%0 extends javax.xml.ws.WebServiceFeature> {%%0} getFeature(java.lang.Class<{%%0}>)
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.NotNull()
meth public abstract <%0 extends javax.xml.ws.WebServiceFeature> {%%0} getOperationFeature(java.lang.Class<{%%0}>,javax.xml.namespace.QName)
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public abstract boolean isFeatureEnabled(java.lang.Class<? extends javax.xml.ws.WebServiceFeature>)
 anno 1 com.sun.istack.NotNull()
meth public abstract boolean isOperationFeatureEnabled(java.lang.Class<? extends javax.xml.ws.WebServiceFeature>,javax.xml.namespace.QName)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public abstract com.sun.xml.ws.api.BindingID getBindingId()
 anno 0 com.sun.istack.NotNull()
meth public abstract com.sun.xml.ws.api.SOAPVersion getSOAPVersion()
meth public abstract com.sun.xml.ws.api.WSFeatureList getFaultMessageFeatures(javax.xml.namespace.QName,javax.xml.namespace.QName)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public abstract com.sun.xml.ws.api.WSFeatureList getFeatures()
 anno 0 com.sun.istack.NotNull()
meth public abstract com.sun.xml.ws.api.WSFeatureList getInputMessageFeatures(javax.xml.namespace.QName)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public abstract com.sun.xml.ws.api.WSFeatureList getOperationFeatures(javax.xml.namespace.QName)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public abstract com.sun.xml.ws.api.WSFeatureList getOutputMessageFeatures(javax.xml.namespace.QName)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public abstract com.sun.xml.ws.api.addressing.AddressingVersion getAddressingVersion()
meth public abstract java.util.List<javax.xml.ws.handler.Handler> getHandlerChain()
 anno 0 com.sun.istack.NotNull()
meth public abstract java.util.Set<javax.xml.namespace.QName> getKnownHeaders()
 anno 0 com.sun.istack.NotNull()

CLSS public abstract com.sun.xml.ws.api.WSDLLocator
cons public init()
meth public abstract java.net.URL locateWSDL(java.lang.Class<javax.xml.ws.Service>,java.lang.String) throws java.net.MalformedURLException
supr java.lang.Object

CLSS public abstract interface com.sun.xml.ws.api.WSFeatureList
intf java.lang.Iterable<javax.xml.ws.WebServiceFeature>
meth public abstract <%0 extends javax.xml.ws.WebServiceFeature> {%%0} get(java.lang.Class<{%%0}>)
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.NotNull()
meth public abstract boolean isEnabled(java.lang.Class<? extends javax.xml.ws.WebServiceFeature>)
 anno 1 com.sun.istack.NotNull()
meth public abstract javax.xml.ws.WebServiceFeature[] toArray()
 anno 0 com.sun.istack.NotNull()
meth public abstract void mergeFeatures(java.lang.Iterable<javax.xml.ws.WebServiceFeature>,boolean)
 anno 1 com.sun.istack.NotNull()
meth public abstract void mergeFeatures(javax.xml.ws.WebServiceFeature[],boolean)
 anno 1 com.sun.istack.NotNull()

CLSS public abstract com.sun.xml.ws.api.WSService
cons protected init()
fld protected final static com.sun.xml.ws.api.WSService$InitParams EMPTY_PARAMS
fld protected final static java.lang.ThreadLocal<com.sun.xml.ws.api.WSService$InitParams> INIT_PARAMS
innr public final static InitParams
intf com.sun.xml.ws.api.ComponentRegistry
meth public <%0 extends java.lang.Object> {%%0} getSPI(java.lang.Class<{%%0}>)
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.NotNull()
meth public abstract !varargs <%0 extends java.lang.Object> javax.xml.ws.Dispatch<{%%0}> createDispatch(javax.xml.namespace.QName,com.sun.xml.ws.api.addressing.WSEndpointReference,java.lang.Class<{%%0}>,javax.xml.ws.Service$Mode,javax.xml.ws.WebServiceFeature[])
meth public abstract !varargs <%0 extends java.lang.Object> {%%0} getPort(com.sun.xml.ws.api.addressing.WSEndpointReference,java.lang.Class<{%%0}>,javax.xml.ws.WebServiceFeature[])
meth public abstract !varargs javax.xml.ws.Dispatch<java.lang.Object> createDispatch(javax.xml.namespace.QName,com.sun.xml.ws.api.addressing.WSEndpointReference,javax.xml.bind.JAXBContext,javax.xml.ws.Service$Mode,javax.xml.ws.WebServiceFeature[])
meth public abstract com.sun.xml.ws.api.server.Container getContainer()
 anno 0 com.sun.istack.NotNull()
meth public java.util.Set<com.sun.xml.ws.api.Component> getComponents()
 anno 0 com.sun.istack.NotNull()
meth public static com.sun.xml.ws.api.WSService create()
meth public static com.sun.xml.ws.api.WSService create(java.net.URL,javax.xml.namespace.QName)
meth public static com.sun.xml.ws.api.WSService create(javax.xml.namespace.QName)
meth public static com.sun.xml.ws.api.WSService unwrap(javax.xml.ws.Service)
meth public static javax.xml.ws.Service create(java.net.URL,javax.xml.namespace.QName,com.sun.xml.ws.api.WSService$InitParams)
supr javax.xml.ws.spi.ServiceDelegate
hfds components

CLSS public final static com.sun.xml.ws.api.WSService$InitParams
 outer com.sun.xml.ws.api.WSService
cons public init()
meth public com.sun.xml.ws.api.server.Container getContainer()
meth public void setContainer(com.sun.xml.ws.api.server.Container)
supr java.lang.Object
hfds container

CLSS public com.sun.xml.ws.api.WebServiceFeatureFactory
cons public init()
meth public static com.sun.xml.ws.api.WSFeatureList getWSFeatureList(java.lang.Iterable<java.lang.annotation.Annotation>)
meth public static javax.xml.ws.WebServiceFeature getWebServiceFeature(java.lang.annotation.Annotation)
supr java.lang.Object

CLSS public abstract !enum com.sun.xml.ws.api.addressing.AddressingVersion
fld public final com.sun.xml.ws.api.addressing.AddressingVersion$EPR eprType
fld public final com.sun.xml.ws.api.addressing.WSEndpointReference anonymousEpr
fld public final java.lang.String actionNotSupportedText
fld public final java.lang.String anonymousUri
 anno 0 com.sun.istack.NotNull()
fld public final java.lang.String noneUri
 anno 0 com.sun.istack.NotNull()
fld public final java.lang.String nsUri
fld public final java.lang.String policyNsUri
fld public final java.lang.String wsdlNsUri
fld public final javax.xml.namespace.QName actionMismatchTag
fld public final javax.xml.namespace.QName actionNotSupportedTag
fld public final javax.xml.namespace.QName actionTag
fld public final javax.xml.namespace.QName faultDetailTag
fld public final javax.xml.namespace.QName faultToTag
fld public final javax.xml.namespace.QName fault_missingAddressInEpr
fld public final javax.xml.namespace.QName fromTag
fld public final javax.xml.namespace.QName invalidAddressTag
fld public final javax.xml.namespace.QName invalidCardinalityTag
fld public final javax.xml.namespace.QName invalidMapTag
fld public final javax.xml.namespace.QName isReferenceParameterTag
fld public final javax.xml.namespace.QName mapRequiredTag
fld public final javax.xml.namespace.QName messageIDTag
fld public final javax.xml.namespace.QName problemActionTag
fld public final javax.xml.namespace.QName problemHeaderQNameTag
fld public final javax.xml.namespace.QName relatesToTag
fld public final javax.xml.namespace.QName replyToTag
fld public final javax.xml.namespace.QName toTag
fld public final javax.xml.namespace.QName wsdlActionTag
fld public final javax.xml.namespace.QName wsdlAnonymousTag
fld public final javax.xml.namespace.QName wsdlExtensionTag
fld public final static com.sun.xml.ws.api.addressing.AddressingVersion MEMBER
fld public final static com.sun.xml.ws.api.addressing.AddressingVersion W3C
fld public final static java.lang.String UNSET_INPUT_ACTION = "http://jax-ws.dev.java.net/addressing/input-action-not-set"
fld public final static java.lang.String UNSET_OUTPUT_ACTION = "http://jax-ws.dev.java.net/addressing/output-action-not-set"
fld public final static javax.xml.namespace.QName fault_duplicateAddressInEpr
innr public final static EPR
meth public abstract boolean isReferenceParameter(java.lang.String)
meth public abstract com.sun.xml.ws.addressing.WsaTubeHelper getWsaHelper(com.sun.xml.ws.api.model.wsdl.WSDLPort,com.sun.xml.ws.api.model.SEIModel,com.sun.xml.ws.api.WSBinding)
meth public abstract java.lang.Class<? extends javax.xml.ws.WebServiceFeature> getFeatureClass()
meth public abstract java.lang.String getInvalidMapText()
meth public abstract java.lang.String getMapRequiredText()
meth public abstract java.lang.String getPrefix()
meth public abstract java.lang.String getWsdlPrefix()
meth public final java.lang.String getAnonymousUri()
meth public final java.lang.String getNoneUri()
meth public java.lang.String getDefaultFaultAction()
meth public java.lang.String getNsUri()
meth public static boolean isEnabled(com.sun.xml.ws.api.WSBinding)
meth public static boolean isRequired(com.sun.xml.ws.api.WSBinding)
meth public static boolean isRequired(javax.xml.ws.WebServiceFeature)
meth public static com.sun.xml.ws.api.addressing.AddressingVersion fromBinding(com.sun.xml.ws.api.WSBinding)
 anno 0 com.sun.istack.Nullable()
meth public static com.sun.xml.ws.api.addressing.AddressingVersion fromFeature(javax.xml.ws.WebServiceFeature)
meth public static com.sun.xml.ws.api.addressing.AddressingVersion fromNsUri(java.lang.String)
meth public static com.sun.xml.ws.api.addressing.AddressingVersion fromPort(com.sun.xml.ws.api.model.wsdl.WSDLPort)
meth public static com.sun.xml.ws.api.addressing.AddressingVersion fromSpecClass(java.lang.Class<? extends javax.xml.ws.EndpointReference>)
 anno 0 com.sun.istack.NotNull()
meth public static com.sun.xml.ws.api.addressing.AddressingVersion valueOf(java.lang.String)
meth public static com.sun.xml.ws.api.addressing.AddressingVersion[] values()
meth public static javax.xml.ws.WebServiceFeature getFeature(java.lang.String,boolean,boolean)
 anno 0 com.sun.istack.NotNull()
supr java.lang.Enum<com.sun.xml.ws.api.addressing.AddressingVersion>
hfds EXTENDED_FAULT_NAMESPACE

CLSS public final static com.sun.xml.ws.api.addressing.AddressingVersion$EPR
 outer com.sun.xml.ws.api.addressing.AddressingVersion
cons public init(java.lang.Class<? extends javax.xml.ws.EndpointReference>,java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.xml.namespace.QName,java.lang.String,java.lang.String)
fld public final java.lang.Class<? extends javax.xml.ws.EndpointReference> eprClass
fld public final java.lang.String address
fld public final java.lang.String portName
fld public final java.lang.String portTypeName
fld public final java.lang.String referenceParameters
fld public final java.lang.String referenceProperties
fld public final java.lang.String serviceName
fld public final javax.xml.namespace.QName wsdlMetadata
supr java.lang.Object

CLSS public com.sun.xml.ws.api.addressing.NonAnonymousResponseProcessor
cons protected init()
meth public com.sun.xml.ws.api.message.Packet process(com.sun.xml.ws.api.message.Packet)
meth public static com.sun.xml.ws.api.addressing.NonAnonymousResponseProcessor getDefault()
supr java.lang.Object
hfds DEFAULT

CLSS public com.sun.xml.ws.api.addressing.OneWayFeature
 anno 0 org.glassfish.gmbal.ManagedData(java.lang.String name="")
cons public init()
cons public init(boolean)
cons public init(boolean,com.sun.xml.ws.api.addressing.WSEndpointReference)
cons public init(boolean,com.sun.xml.ws.api.addressing.WSEndpointReference,com.sun.xml.ws.api.addressing.WSEndpointReference,java.lang.String)
fld public final static java.lang.String ID = "http://java.sun.com/xml/ns/jaxws/addressing/oneway"
meth public boolean hasSslEprs()
meth public boolean isUseAsyncWithSyncInvoke()
meth public com.sun.xml.ws.api.addressing.WSEndpointReference getFaultTo()
meth public com.sun.xml.ws.api.addressing.WSEndpointReference getFaultTo(boolean)
meth public com.sun.xml.ws.api.addressing.WSEndpointReference getFrom()
 anno 0 org.glassfish.gmbal.ManagedAttribute(java.lang.String id="")
meth public com.sun.xml.ws.api.addressing.WSEndpointReference getReplyTo()
 anno 0 org.glassfish.gmbal.ManagedAttribute(java.lang.String id="")
meth public com.sun.xml.ws.api.addressing.WSEndpointReference getReplyTo(boolean)
meth public com.sun.xml.ws.api.addressing.WSEndpointReference getSslFaultTo()
meth public com.sun.xml.ws.api.addressing.WSEndpointReference getSslReplyTo()
meth public java.lang.String getID()
 anno 0 org.glassfish.gmbal.ManagedAttribute(java.lang.String id="")
meth public java.lang.String getRelatesToID()
 anno 0 org.glassfish.gmbal.ManagedAttribute(java.lang.String id="")
meth public static com.sun.xml.ws.api.addressing.WSEndpointReference enableSslForEpr(com.sun.xml.ws.api.addressing.WSEndpointReference,java.lang.String,int)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.Nullable()
meth public void setFaultTo(com.sun.xml.ws.api.addressing.WSEndpointReference)
meth public void setFrom(com.sun.xml.ws.api.addressing.WSEndpointReference)
meth public void setRelatesToID(java.lang.String)
meth public void setReplyTo(com.sun.xml.ws.api.addressing.WSEndpointReference)
meth public void setSslFaultTo(com.sun.xml.ws.api.addressing.WSEndpointReference)
meth public void setSslReplyTo(com.sun.xml.ws.api.addressing.WSEndpointReference)
meth public void setUseAsyncWithSyncInvoke(boolean)
supr javax.xml.ws.WebServiceFeature
hfds faultTo,from,relatesToID,replyTo,sslFaultTo,sslReplyTo,useAsyncWithSyncInvoke

CLSS public final com.sun.xml.ws.api.addressing.WSEndpointReference
cons public init(com.sun.xml.stream.buffer.XMLStreamBuffer,com.sun.xml.ws.api.addressing.AddressingVersion)
cons public init(com.sun.xml.ws.api.addressing.AddressingVersion,java.lang.String,javax.xml.namespace.QName,javax.xml.namespace.QName,javax.xml.namespace.QName,java.util.List<org.w3c.dom.Element>,java.lang.String,java.lang.String,java.util.List<org.w3c.dom.Element>,java.util.List<org.w3c.dom.Element>,java.util.Map<javax.xml.namespace.QName,java.lang.String>)
 anno 1 com.sun.istack.NotNull()
 anno 10 com.sun.istack.Nullable()
 anno 11 com.sun.istack.Nullable()
 anno 2 com.sun.istack.NotNull()
 anno 3 com.sun.istack.Nullable()
 anno 4 com.sun.istack.Nullable()
 anno 5 com.sun.istack.Nullable()
 anno 6 com.sun.istack.Nullable()
 anno 7 com.sun.istack.Nullable()
 anno 8 com.sun.istack.Nullable()
 anno 9 com.sun.istack.Nullable()
cons public init(com.sun.xml.ws.api.addressing.AddressingVersion,java.lang.String,javax.xml.namespace.QName,javax.xml.namespace.QName,javax.xml.namespace.QName,java.util.List<org.w3c.dom.Element>,java.lang.String,java.util.List<org.w3c.dom.Element>)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
 anno 3 com.sun.istack.Nullable()
 anno 4 com.sun.istack.Nullable()
 anno 5 com.sun.istack.Nullable()
 anno 6 com.sun.istack.Nullable()
 anno 7 com.sun.istack.Nullable()
 anno 8 com.sun.istack.Nullable()
cons public init(com.sun.xml.ws.api.addressing.AddressingVersion,java.lang.String,javax.xml.namespace.QName,javax.xml.namespace.QName,javax.xml.namespace.QName,java.util.List<org.w3c.dom.Element>,java.lang.String,java.util.List<org.w3c.dom.Element>,java.util.Collection<com.sun.xml.ws.api.addressing.WSEndpointReference$EPRExtension>,java.util.Map<javax.xml.namespace.QName,java.lang.String>)
 anno 1 com.sun.istack.NotNull()
 anno 10 com.sun.istack.Nullable()
 anno 2 com.sun.istack.NotNull()
 anno 3 com.sun.istack.Nullable()
 anno 4 com.sun.istack.Nullable()
 anno 5 com.sun.istack.Nullable()
 anno 6 com.sun.istack.Nullable()
 anno 7 com.sun.istack.Nullable()
 anno 8 com.sun.istack.Nullable()
 anno 9 com.sun.istack.Nullable()
cons public init(java.io.InputStream,com.sun.xml.ws.api.addressing.AddressingVersion) throws javax.xml.stream.XMLStreamException
cons public init(java.lang.String,com.sun.xml.ws.api.addressing.AddressingVersion)
cons public init(java.net.URI,com.sun.xml.ws.api.addressing.AddressingVersion)
cons public init(java.net.URL,com.sun.xml.ws.api.addressing.AddressingVersion)
cons public init(javax.xml.stream.XMLStreamReader,com.sun.xml.ws.api.addressing.AddressingVersion) throws javax.xml.stream.XMLStreamException
cons public init(javax.xml.ws.EndpointReference)
cons public init(javax.xml.ws.EndpointReference,com.sun.xml.ws.api.addressing.AddressingVersion)
innr public Metadata
innr public abstract static EPRExtension
intf com.sun.xml.ws.api.model.wsdl.WSDLExtension
meth public !varargs <%0 extends java.lang.Object> javax.xml.ws.Dispatch<{%%0}> createDispatch(javax.xml.ws.Service,java.lang.Class<{%%0}>,javax.xml.ws.Service$Mode,javax.xml.ws.WebServiceFeature[])
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
 anno 3 com.sun.istack.NotNull()
meth public !varargs <%0 extends java.lang.Object> {%%0} getPort(javax.xml.ws.Service,java.lang.Class<{%%0}>,javax.xml.ws.WebServiceFeature[])
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public !varargs javax.xml.ws.Dispatch<java.lang.Object> createDispatch(javax.xml.ws.Service,javax.xml.bind.JAXBContext,javax.xml.ws.Service$Mode,javax.xml.ws.WebServiceFeature[])
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
 anno 3 com.sun.istack.NotNull()
meth public <%0 extends javax.xml.ws.EndpointReference> {%%0} toSpec(java.lang.Class<{%%0}>)
 anno 0 com.sun.istack.NotNull()
meth public boolean isAnonymous()
meth public boolean isNone()
meth public com.sun.xml.ws.api.addressing.AddressingVersion getVersion()
 anno 0 com.sun.istack.NotNull()
meth public com.sun.xml.ws.api.addressing.WSEndpointReference createWithAddress(java.lang.String)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public com.sun.xml.ws.api.addressing.WSEndpointReference createWithAddress(java.net.URI)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public com.sun.xml.ws.api.addressing.WSEndpointReference createWithAddress(java.net.URL)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public com.sun.xml.ws.api.addressing.WSEndpointReference$EPRExtension getEPRExtension(javax.xml.namespace.QName) throws javax.xml.stream.XMLStreamException
 anno 0 com.sun.istack.Nullable()
meth public com.sun.xml.ws.api.addressing.WSEndpointReference$Metadata getMetaData()
 anno 0 com.sun.istack.NotNull()
meth public com.sun.xml.ws.api.message.Header createHeader(javax.xml.namespace.QName)
meth public java.lang.String getAddress()
 anno 0 com.sun.istack.NotNull()
meth public java.lang.String toString()
meth public java.util.Collection<com.sun.xml.ws.api.addressing.WSEndpointReference$EPRExtension> getEPRExtensions() throws javax.xml.stream.XMLStreamException
 anno 0 com.sun.istack.NotNull()
meth public javax.xml.namespace.QName getName()
meth public javax.xml.stream.XMLStreamReader read(java.lang.String) throws javax.xml.stream.XMLStreamException
 anno 1 com.sun.istack.NotNull()
meth public javax.xml.transform.Source asSource(java.lang.String)
 anno 1 com.sun.istack.NotNull()
meth public javax.xml.ws.EndpointReference toSpec()
 anno 0 com.sun.istack.NotNull()
meth public static com.sun.xml.ws.api.addressing.WSEndpointReference create(javax.xml.ws.EndpointReference)
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.Nullable()
meth public void addReferenceParameters(com.sun.xml.ws.api.message.HeaderList)
meth public void addReferenceParametersToList(com.sun.xml.ws.api.message.HeaderList)
meth public void writeTo(java.lang.String,javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public void writeTo(java.lang.String,org.xml.sax.ContentHandler,org.xml.sax.ErrorHandler,boolean) throws org.xml.sax.SAXException
 anno 1 com.sun.istack.NotNull()
supr java.lang.Object
hfds EMPTY_ARRAY,address,infoset,referenceParameters,rootElement,rootEprExtensions,version
hcls SAXBufferProcessorImpl

CLSS public abstract static com.sun.xml.ws.api.addressing.WSEndpointReference$EPRExtension
 outer com.sun.xml.ws.api.addressing.WSEndpointReference
cons public init()
meth public abstract javax.xml.namespace.QName getQName()
meth public abstract javax.xml.stream.XMLStreamReader readAsXMLStreamReader() throws javax.xml.stream.XMLStreamException
supr java.lang.Object

CLSS public com.sun.xml.ws.api.addressing.WSEndpointReference$Metadata
 outer com.sun.xml.ws.api.addressing.WSEndpointReference
meth public java.lang.String getWsdliLocation()
 anno 0 com.sun.istack.Nullable()
meth public javax.xml.namespace.QName getPortName()
 anno 0 com.sun.istack.Nullable()
meth public javax.xml.namespace.QName getPortTypeName()
 anno 0 com.sun.istack.Nullable()
meth public javax.xml.namespace.QName getServiceName()
 anno 0 com.sun.istack.Nullable()
meth public javax.xml.transform.Source getWsdlSource()
 anno 0 com.sun.istack.Nullable()
supr java.lang.Object
hfds portName,portTypeName,serviceName,wsdlSource,wsdliLocation

CLSS abstract interface com.sun.xml.ws.api.addressing.package-info

CLSS public abstract interface com.sun.xml.ws.api.ha.StickyFeature

CLSS public abstract interface com.sun.xml.ws.api.message.Attachment
meth public abstract byte[] asByteArray()
meth public abstract java.io.InputStream asInputStream()
meth public abstract java.lang.String getContentId()
 anno 0 com.sun.istack.NotNull()
meth public abstract java.lang.String getContentType()
meth public abstract javax.activation.DataHandler asDataHandler()
meth public abstract javax.xml.transform.Source asSource()
meth public abstract void writeTo(java.io.OutputStream) throws java.io.IOException
meth public abstract void writeTo(javax.xml.soap.SOAPMessage) throws javax.xml.soap.SOAPException

CLSS public abstract interface com.sun.xml.ws.api.message.AttachmentEx
innr public abstract interface static MimeHeader
intf com.sun.xml.ws.api.message.Attachment
meth public abstract java.util.Iterator<com.sun.xml.ws.api.message.AttachmentEx$MimeHeader> getMimeHeaders()
 anno 0 com.sun.istack.NotNull()

CLSS public abstract interface static com.sun.xml.ws.api.message.AttachmentEx$MimeHeader
 outer com.sun.xml.ws.api.message.AttachmentEx
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getValue()

CLSS public abstract interface com.sun.xml.ws.api.message.AttachmentSet
intf java.lang.Iterable<com.sun.xml.ws.api.message.Attachment>
meth public abstract boolean isEmpty()
meth public abstract com.sun.xml.ws.api.message.Attachment get(java.lang.String)
 anno 0 com.sun.istack.Nullable()
meth public abstract void add(com.sun.xml.ws.api.message.Attachment)

CLSS public abstract com.sun.xml.ws.api.message.ExceptionHasMessage
cons public !varargs init(java.lang.String,java.lang.Object[])
meth public abstract com.sun.xml.ws.api.message.Message getFaultMessage()
supr com.sun.xml.ws.util.exception.JAXWSExceptionBase

CLSS public com.sun.xml.ws.api.message.FilterMessageImpl
cons protected init(com.sun.xml.ws.api.message.Message)
meth protected boolean hasAttachments()
meth public <%0 extends java.lang.Object> {%%0} readPayloadAsJAXB(com.sun.xml.bind.api.Bridge<{%%0}>) throws javax.xml.bind.JAXBException
meth public <%0 extends java.lang.Object> {%%0} readPayloadAsJAXB(com.sun.xml.ws.spi.db.XMLBridge<{%%0}>) throws javax.xml.bind.JAXBException
meth public <%0 extends java.lang.Object> {%%0} readPayloadAsJAXB(javax.xml.bind.Unmarshaller) throws javax.xml.bind.JAXBException
meth public boolean hasHeaders()
meth public boolean hasPayload()
meth public boolean isFault()
meth public boolean isOneWay(com.sun.xml.ws.api.model.wsdl.WSDLPort)
 anno 1 com.sun.istack.NotNull()
meth public com.sun.xml.ws.api.message.AttachmentSet getAttachments()
 anno 0 com.sun.istack.NotNull()
meth public com.sun.xml.ws.api.message.HeaderList getHeaders()
 anno 0 com.sun.istack.NotNull()
meth public com.sun.xml.ws.api.message.Message copy()
meth public java.lang.String getID(com.sun.xml.ws.api.WSBinding)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public java.lang.String getID(com.sun.xml.ws.api.addressing.AddressingVersion,com.sun.xml.ws.api.SOAPVersion)
 anno 0 com.sun.istack.NotNull()
meth public java.lang.String getPayloadLocalPart()
 anno 0 com.sun.istack.Nullable()
meth public java.lang.String getPayloadNamespaceURI()
meth public javax.xml.namespace.QName getFirstDetailEntryName()
 anno 0 com.sun.istack.Nullable()
meth public javax.xml.soap.SOAPMessage readAsSOAPMessage() throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPMessage readAsSOAPMessage(com.sun.xml.ws.api.message.Packet,boolean) throws javax.xml.soap.SOAPException
meth public javax.xml.stream.XMLStreamReader readPayload() throws javax.xml.stream.XMLStreamException
meth public javax.xml.transform.Source readEnvelopeAsSource()
meth public javax.xml.transform.Source readPayloadAsSource()
meth public void consume()
meth public void writePayloadTo(javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
meth public void writeTo(javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
meth public void writeTo(org.xml.sax.ContentHandler,org.xml.sax.ErrorHandler) throws org.xml.sax.SAXException
supr com.sun.xml.ws.api.message.Message
hfds delegate

CLSS public abstract interface com.sun.xml.ws.api.message.Header
meth public abstract <%0 extends java.lang.Object> {%%0} readAsJAXB(com.sun.xml.bind.api.Bridge<{%%0}>) throws javax.xml.bind.JAXBException
meth public abstract <%0 extends java.lang.Object> {%%0} readAsJAXB(com.sun.xml.ws.spi.db.XMLBridge<{%%0}>) throws javax.xml.bind.JAXBException
meth public abstract <%0 extends java.lang.Object> {%%0} readAsJAXB(javax.xml.bind.Unmarshaller) throws javax.xml.bind.JAXBException
meth public abstract boolean isIgnorable(com.sun.xml.ws.api.SOAPVersion,java.util.Set<java.lang.String>)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public abstract boolean isRelay()
meth public abstract com.sun.xml.ws.api.addressing.WSEndpointReference readAsEPR(com.sun.xml.ws.api.addressing.AddressingVersion) throws javax.xml.stream.XMLStreamException
 anno 0 com.sun.istack.NotNull()
meth public abstract java.lang.String getAttribute(java.lang.String,java.lang.String)
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public abstract java.lang.String getAttribute(javax.xml.namespace.QName)
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.NotNull()
meth public abstract java.lang.String getLocalPart()
 anno 0 com.sun.istack.NotNull()
meth public abstract java.lang.String getNamespaceURI()
 anno 0 com.sun.istack.NotNull()
meth public abstract java.lang.String getRole(com.sun.xml.ws.api.SOAPVersion)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public abstract java.lang.String getStringContent()
 anno 0 com.sun.istack.NotNull()
meth public abstract javax.xml.stream.XMLStreamReader readHeader() throws javax.xml.stream.XMLStreamException
meth public abstract void writeTo(javax.xml.soap.SOAPMessage) throws javax.xml.soap.SOAPException
meth public abstract void writeTo(javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
meth public abstract void writeTo(org.xml.sax.ContentHandler,org.xml.sax.ErrorHandler) throws org.xml.sax.SAXException

CLSS public com.sun.xml.ws.api.message.HeaderList
cons public init()
cons public init(com.sun.xml.ws.api.message.HeaderList)
meth protected com.sun.xml.ws.api.message.Header removeInternal(int)
meth protected void addInternal(int,com.sun.xml.ws.api.message.Header)
meth public !varargs void addAll(com.sun.xml.ws.api.message.Header[])
meth public boolean add(com.sun.xml.ws.api.message.Header)
meth public boolean addOrReplace(com.sun.xml.ws.api.message.Header)
meth public boolean isUnderstood(int)
meth public boolean remove(java.lang.Object)
meth public com.sun.xml.ws.api.addressing.WSEndpointReference getFaultTo(com.sun.xml.ws.api.addressing.AddressingVersion,com.sun.xml.ws.api.SOAPVersion)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public com.sun.xml.ws.api.addressing.WSEndpointReference getReplyTo(com.sun.xml.ws.api.addressing.AddressingVersion,com.sun.xml.ws.api.SOAPVersion)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public com.sun.xml.ws.api.message.Header get(int)
meth public com.sun.xml.ws.api.message.Header get(java.lang.String,java.lang.String)
meth public com.sun.xml.ws.api.message.Header get(java.lang.String,java.lang.String,boolean)
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public com.sun.xml.ws.api.message.Header get(javax.xml.namespace.QName)
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.NotNull()
meth public com.sun.xml.ws.api.message.Header get(javax.xml.namespace.QName,boolean)
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.NotNull()
meth public com.sun.xml.ws.api.message.Header remove(int)
meth public com.sun.xml.ws.api.message.Header remove(java.lang.String,java.lang.String)
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public com.sun.xml.ws.api.message.Header remove(javax.xml.namespace.QName)
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.NotNull()
meth public int size()
meth public java.lang.String getAction(com.sun.xml.ws.api.addressing.AddressingVersion,com.sun.xml.ws.api.SOAPVersion)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public java.lang.String getMessageID(com.sun.xml.ws.api.addressing.AddressingVersion,com.sun.xml.ws.api.SOAPVersion)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public java.lang.String getRelatesTo(com.sun.xml.ws.api.addressing.AddressingVersion,com.sun.xml.ws.api.SOAPVersion)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public java.lang.String getTo(com.sun.xml.ws.api.addressing.AddressingVersion,com.sun.xml.ws.api.SOAPVersion)
meth public java.util.Iterator<com.sun.xml.ws.api.message.Header> getHeaders(java.lang.String)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public java.util.Iterator<com.sun.xml.ws.api.message.Header> getHeaders(java.lang.String,boolean)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public java.util.Iterator<com.sun.xml.ws.api.message.Header> getHeaders(java.lang.String,java.lang.String)
meth public java.util.Iterator<com.sun.xml.ws.api.message.Header> getHeaders(java.lang.String,java.lang.String,boolean)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public java.util.Iterator<com.sun.xml.ws.api.message.Header> getHeaders(javax.xml.namespace.QName,boolean)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public static com.sun.xml.ws.api.message.HeaderList copy(com.sun.xml.ws.api.message.HeaderList)
meth public void fillRequestAddressingHeaders(com.sun.xml.ws.api.message.Packet,com.sun.xml.ws.api.addressing.AddressingVersion,com.sun.xml.ws.api.SOAPVersion,boolean,java.lang.String)
meth public void fillRequestAddressingHeaders(com.sun.xml.ws.api.message.Packet,com.sun.xml.ws.api.addressing.AddressingVersion,com.sun.xml.ws.api.SOAPVersion,boolean,java.lang.String,boolean)
meth public void fillRequestAddressingHeaders(com.sun.xml.ws.api.model.wsdl.WSDLPort,com.sun.xml.ws.api.WSBinding,com.sun.xml.ws.api.message.Packet)
 anno 2 com.sun.istack.NotNull()
meth public void readResponseAddressingHeaders(com.sun.xml.ws.api.model.wsdl.WSDLPort,com.sun.xml.ws.api.WSBinding)
meth public void understood(com.sun.xml.ws.api.message.Header)
 anno 1 com.sun.istack.NotNull()
meth public void understood(int)
supr java.util.ArrayList<com.sun.xml.ws.api.message.Header>
hfds moreUnderstoodBits,serialVersionUID,understoodBits

CLSS public abstract com.sun.xml.ws.api.message.Headers
meth public static com.sun.xml.ws.api.message.Header create(com.sun.xml.bind.api.Bridge,java.lang.Object)
meth public static com.sun.xml.ws.api.message.Header create(com.sun.xml.ws.api.SOAPVersion,javax.xml.bind.Marshaller,java.lang.Object)
meth public static com.sun.xml.ws.api.message.Header create(com.sun.xml.ws.api.SOAPVersion,javax.xml.bind.Marshaller,javax.xml.namespace.QName,java.lang.Object)
meth public static com.sun.xml.ws.api.message.Header create(com.sun.xml.ws.api.SOAPVersion,javax.xml.stream.XMLStreamReader) throws javax.xml.stream.XMLStreamException
meth public static com.sun.xml.ws.api.message.Header create(com.sun.xml.ws.api.SOAPVersion,org.w3c.dom.Element)
meth public static com.sun.xml.ws.api.message.Header create(com.sun.xml.ws.spi.db.BindingContext,java.lang.Object)
meth public static com.sun.xml.ws.api.message.Header create(com.sun.xml.ws.spi.db.XMLBridge,java.lang.Object)
meth public static com.sun.xml.ws.api.message.Header create(javax.xml.bind.JAXBContext,java.lang.Object)
meth public static com.sun.xml.ws.api.message.Header create(javax.xml.namespace.QName,java.lang.String)
meth public static com.sun.xml.ws.api.message.Header create(javax.xml.soap.SOAPHeaderElement)
meth public static com.sun.xml.ws.api.message.Header create(org.w3c.dom.Element)
meth public static com.sun.xml.ws.api.message.Header createMustUnderstand(com.sun.xml.ws.api.SOAPVersion,javax.xml.namespace.QName,java.lang.String)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
 anno 3 com.sun.istack.NotNull()
supr java.lang.Object

CLSS public abstract com.sun.xml.ws.api.message.Message
cons public init()
fld protected com.sun.xml.ws.api.message.AttachmentSet attachmentSet
meth protected boolean hasAttachments()
meth public abstract <%0 extends java.lang.Object> {%%0} readPayloadAsJAXB(com.sun.xml.bind.api.Bridge<{%%0}>) throws javax.xml.bind.JAXBException
meth public abstract <%0 extends java.lang.Object> {%%0} readPayloadAsJAXB(com.sun.xml.ws.spi.db.XMLBridge<{%%0}>) throws javax.xml.bind.JAXBException
meth public abstract <%0 extends java.lang.Object> {%%0} readPayloadAsJAXB(javax.xml.bind.Unmarshaller) throws javax.xml.bind.JAXBException
meth public abstract boolean hasHeaders()
meth public abstract boolean hasPayload()
meth public abstract com.sun.xml.ws.api.message.HeaderList getHeaders()
 anno 0 com.sun.istack.NotNull()
meth public abstract com.sun.xml.ws.api.message.Message copy()
meth public abstract java.lang.String getPayloadLocalPart()
 anno 0 com.sun.istack.Nullable()
meth public abstract java.lang.String getPayloadNamespaceURI()
meth public abstract javax.xml.soap.SOAPMessage readAsSOAPMessage() throws javax.xml.soap.SOAPException
meth public abstract javax.xml.stream.XMLStreamReader readPayload() throws javax.xml.stream.XMLStreamException
meth public abstract javax.xml.transform.Source readEnvelopeAsSource()
meth public abstract javax.xml.transform.Source readPayloadAsSource()
meth public abstract void writePayloadTo(javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
meth public abstract void writeTo(javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
meth public abstract void writeTo(org.xml.sax.ContentHandler,org.xml.sax.ErrorHandler) throws org.xml.sax.SAXException
meth public boolean isFault()
meth public boolean isOneWay(com.sun.xml.ws.api.model.wsdl.WSDLPort)
 anno 1 com.sun.istack.NotNull()
meth public com.sun.xml.ws.api.message.AttachmentSet getAttachments()
 anno 0 com.sun.istack.NotNull()
meth public final com.sun.xml.ws.api.model.JavaMethod getMethod(com.sun.xml.ws.api.model.SEIModel)
 anno 0 com.sun.istack.Nullable()
 anno 0 java.lang.Deprecated()
 anno 1 com.sun.istack.NotNull()
meth public final com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation getOperation(com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType)
 anno 0 com.sun.istack.Nullable()
 anno 0 java.lang.Deprecated()
 anno 1 com.sun.istack.NotNull()
meth public final com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation getOperation(com.sun.xml.ws.api.model.wsdl.WSDLPort)
 anno 0 com.sun.istack.Nullable()
 anno 0 java.lang.Deprecated()
 anno 1 com.sun.istack.NotNull()
meth public final void assertOneWay(boolean)
meth public java.lang.String getID(com.sun.xml.ws.api.WSBinding)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public java.lang.String getID(com.sun.xml.ws.api.addressing.AddressingVersion,com.sun.xml.ws.api.SOAPVersion)
 anno 0 com.sun.istack.NotNull()
meth public javax.xml.namespace.QName getFirstDetailEntryName()
 anno 0 com.sun.istack.Nullable()
meth public javax.xml.soap.SOAPMessage readAsSOAPMessage(com.sun.xml.ws.api.message.Packet,boolean) throws javax.xml.soap.SOAPException
meth public static java.lang.String generateMessageID()
meth public void consume()
supr java.lang.Object
hfds isOneWay,operation

CLSS public abstract com.sun.xml.ws.api.message.Messages
meth public static com.sun.xml.ws.api.message.Message create(com.sun.xml.stream.buffer.XMLStreamBuffer)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public static com.sun.xml.ws.api.message.Message create(com.sun.xml.ws.api.SOAPVersion,javax.xml.ws.ProtocolException,javax.xml.namespace.QName)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
 anno 3 com.sun.istack.Nullable()
meth public static com.sun.xml.ws.api.message.Message create(java.lang.String,com.sun.xml.ws.api.addressing.AddressingVersion,com.sun.xml.ws.api.SOAPVersion)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
 anno 3 com.sun.istack.NotNull()
meth public static com.sun.xml.ws.api.message.Message create(java.lang.Throwable,com.sun.xml.ws.api.SOAPVersion)
meth public static com.sun.xml.ws.api.message.Message create(javax.xml.bind.JAXBContext,java.lang.Object,com.sun.xml.ws.api.SOAPVersion)
meth public static com.sun.xml.ws.api.message.Message create(javax.xml.bind.Marshaller,java.lang.Object,com.sun.xml.ws.api.SOAPVersion)
meth public static com.sun.xml.ws.api.message.Message create(javax.xml.soap.SOAPFault)
meth public static com.sun.xml.ws.api.message.Message create(javax.xml.soap.SOAPMessage)
meth public static com.sun.xml.ws.api.message.Message create(javax.xml.stream.XMLStreamReader)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public static com.sun.xml.ws.api.message.Message create(javax.xml.transform.Source,com.sun.xml.ws.api.SOAPVersion)
meth public static com.sun.xml.ws.api.message.Message create(org.w3c.dom.Element)
meth public static com.sun.xml.ws.api.message.Message createAddressingFaultMessage(com.sun.xml.ws.api.WSBinding,com.sun.xml.ws.api.message.Packet,javax.xml.namespace.QName)
meth public static com.sun.xml.ws.api.message.Message createAddressingFaultMessage(com.sun.xml.ws.api.WSBinding,javax.xml.namespace.QName)
meth public static com.sun.xml.ws.api.message.Message createEmpty(com.sun.xml.ws.api.SOAPVersion)
meth public static com.sun.xml.ws.api.message.Message createRaw(javax.xml.bind.JAXBContext,java.lang.Object,com.sun.xml.ws.api.SOAPVersion)
meth public static com.sun.xml.ws.api.message.Message createUsingPayload(javax.xml.stream.XMLStreamReader,com.sun.xml.ws.api.SOAPVersion)
meth public static com.sun.xml.ws.api.message.Message createUsingPayload(javax.xml.transform.Source,com.sun.xml.ws.api.SOAPVersion)
meth public static com.sun.xml.ws.api.message.Message createUsingPayload(org.w3c.dom.Element,com.sun.xml.ws.api.SOAPVersion)
supr java.lang.Object

CLSS public final com.sun.xml.ws.api.message.Packet
cons public init()
cons public init(com.sun.xml.ws.api.message.Message)
fld public boolean isAdapterDeliversNonAnonymousResponse
fld public boolean wasTransportSecure
fld public com.sun.xml.ws.api.Component component
fld public com.sun.xml.ws.api.EndpointAddress endpointAddress
fld public com.sun.xml.ws.api.server.TransportBackChannel transportBackChannel
 anno 0 com.sun.istack.Nullable()
fld public com.sun.xml.ws.api.server.WSEndpoint endpoint
fld public com.sun.xml.ws.api.server.WebServiceContextDelegate webServiceContextDelegate
fld public com.sun.xml.ws.client.ContentNegotiation contentNegotiation
fld public com.sun.xml.ws.client.HandlerConfiguration handlerConfig
fld public final java.util.Map<java.lang.String,java.lang.Object> invocationProperties
fld public final static java.lang.String HA_INFO = "com.sun.xml.ws.api.message.packet.hainfo"
fld public final static java.lang.String INBOUND_TRANSPORT_HEADERS = "com.sun.xml.ws.api.message.packet.inbound.transport.headers"
fld public final static java.lang.String OUTBOUND_TRANSPORT_HEADERS = "com.sun.xml.ws.api.message.packet.outbound.transport.headers"
fld public java.lang.Boolean expectReply
fld public java.lang.Boolean isOneWay
 anno 0 java.lang.Deprecated()
fld public java.lang.Boolean isSynchronousMEP
fld public java.lang.Boolean nonNullAsyncHandlerGiven
fld public java.lang.String acceptableMimeTypes
fld public java.lang.String soapAction
fld public javax.xml.ws.BindingProvider proxy
intf org.jvnet.ws.message.MessageContext
meth protected com.sun.xml.ws.api.PropertySet$PropertyMap getPropertyMap()
meth public com.sun.xml.ws.api.WSBinding getBinding()
meth public com.sun.xml.ws.api.message.Message getMessage()
meth public com.sun.xml.ws.api.message.Packet copy(boolean)
meth public com.sun.xml.ws.api.message.Packet createClientResponse(com.sun.xml.ws.api.message.Message)
meth public com.sun.xml.ws.api.message.Packet createResponse(com.sun.xml.ws.api.message.Message)
 anno 0 java.lang.Deprecated()
meth public com.sun.xml.ws.api.message.Packet createServerResponse(com.sun.xml.ws.api.message.Message,com.sun.xml.ws.api.addressing.AddressingVersion,com.sun.xml.ws.api.SOAPVersion,java.lang.String)
 anno 1 com.sun.istack.Nullable()
 anno 2 com.sun.istack.NotNull()
 anno 3 com.sun.istack.NotNull()
 anno 4 com.sun.istack.NotNull()
meth public com.sun.xml.ws.api.message.Packet createServerResponse(com.sun.xml.ws.api.message.Message,com.sun.xml.ws.api.model.wsdl.WSDLPort,com.sun.xml.ws.api.model.SEIModel,com.sun.xml.ws.api.WSBinding)
 anno 1 com.sun.istack.Nullable()
 anno 2 com.sun.istack.Nullable()
 anno 3 com.sun.istack.Nullable()
 anno 4 com.sun.istack.NotNull()
meth public com.sun.xml.ws.api.message.Packet relateServerResponse(com.sun.xml.ws.api.message.Packet,com.sun.xml.ws.api.model.wsdl.WSDLPort,com.sun.xml.ws.api.model.SEIModel,com.sun.xml.ws.api.WSBinding)
 anno 1 com.sun.istack.Nullable()
 anno 2 com.sun.istack.Nullable()
 anno 3 com.sun.istack.Nullable()
 anno 4 com.sun.istack.NotNull()
meth public com.sun.xml.ws.api.server.TransportBackChannel keepTransportBackChannelOpen()
meth public final java.util.Set<java.lang.String> getApplicationScopePropertyNames(boolean)
meth public final java.util.Set<java.lang.String> getHandlerScopePropertyNames(boolean)
meth public final javax.xml.namespace.QName getWSDLOperation()
 anno 0 com.sun.istack.Nullable()
meth public java.lang.String getContentNegotiationString()
meth public java.lang.String getEndPointAddressString()
meth public java.lang.String toShortString()
meth public java.lang.String toString()
meth public java.util.List<org.w3c.dom.Element> getReferenceParameters()
 anno 0 com.sun.istack.NotNull()
meth public javax.xml.soap.SOAPMessage getSOAPMessage() throws javax.xml.soap.SOAPException
meth public void setContentNegotiationString(java.lang.String)
meth public void setEndPointAddressString(java.lang.String)
meth public void setMessage(com.sun.xml.ws.api.message.Message)
meth public void setResponseMessage(com.sun.xml.ws.api.message.Packet,com.sun.xml.ws.api.message.Message,com.sun.xml.ws.api.addressing.AddressingVersion,com.sun.xml.ws.api.SOAPVersion,java.lang.String)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.Nullable()
 anno 3 com.sun.istack.NotNull()
 anno 4 com.sun.istack.NotNull()
 anno 5 com.sun.istack.NotNull()
meth public void setWSDLOperation(javax.xml.namespace.QName)
supr com.sun.xml.ws.api.DistributedPropertySet
hfds LOGGER,handlerScopePropertyNames,message,model,wsdlOperation

CLSS public com.sun.xml.ws.api.message.SuppressAutomaticWSARequestHeadersFeature
cons public init()
meth public java.lang.String getID()
supr javax.xml.ws.WebServiceFeature

CLSS abstract interface com.sun.xml.ws.api.message.package-info

CLSS public abstract interface com.sun.xml.ws.api.model.wsdl.WSDLExtension
meth public abstract javax.xml.namespace.QName getName()

CLSS abstract interface com.sun.xml.ws.api.package-info

CLSS public abstract interface !annotation com.sun.xml.ws.api.server.InstanceResolverAnnotation
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<? extends com.sun.xml.ws.api.server.InstanceResolver> value()

CLSS public abstract com.sun.xml.ws.api.streaming.XMLStreamReaderFactory
cons public init()
innr public abstract interface static RecycleAware
innr public final static Default
innr public final static Woodstox
innr public final static Zephyr
innr public static NoLock
meth public abstract javax.xml.stream.XMLStreamReader doCreate(java.lang.String,java.io.InputStream,boolean)
meth public abstract javax.xml.stream.XMLStreamReader doCreate(java.lang.String,java.io.Reader,boolean)
meth public abstract void doRecycle(javax.xml.stream.XMLStreamReader)
meth public static com.sun.xml.ws.api.streaming.XMLStreamReaderFactory get()
meth public static javax.xml.stream.XMLStreamReader create(java.lang.String,java.io.InputStream,boolean)
 anno 1 com.sun.istack.Nullable()
meth public static javax.xml.stream.XMLStreamReader create(java.lang.String,java.io.InputStream,java.lang.String,boolean)
 anno 1 com.sun.istack.Nullable()
 anno 3 com.sun.istack.Nullable()
meth public static javax.xml.stream.XMLStreamReader create(java.lang.String,java.io.Reader,boolean)
 anno 1 com.sun.istack.Nullable()
meth public static javax.xml.stream.XMLStreamReader create(org.xml.sax.InputSource,boolean)
meth public static void recycle(javax.xml.stream.XMLStreamReader)
meth public static void set(com.sun.xml.ws.api.streaming.XMLStreamReaderFactory)
supr java.lang.Object
hfds LOGGER,theInstance

CLSS public abstract interface static com.sun.xml.ws.api.streaming.XMLStreamReaderFactory$RecycleAware
 outer com.sun.xml.ws.api.streaming.XMLStreamReaderFactory
meth public abstract void onRecycled()

CLSS public abstract com.sun.xml.ws.api.streaming.XMLStreamWriterFactory
cons public init()
innr public abstract interface static RecycleAware
innr public final static Default
innr public final static NoLock
innr public final static Zephyr
meth public abstract javax.xml.stream.XMLStreamWriter doCreate(java.io.OutputStream)
meth public abstract javax.xml.stream.XMLStreamWriter doCreate(java.io.OutputStream,java.lang.String)
meth public abstract void doRecycle(javax.xml.stream.XMLStreamWriter)
meth public static com.sun.xml.ws.api.streaming.XMLStreamWriterFactory get()
 anno 0 com.sun.istack.NotNull()
meth public static javax.xml.stream.XMLStreamWriter create(java.io.OutputStream)
meth public static javax.xml.stream.XMLStreamWriter create(java.io.OutputStream,java.lang.String)
meth public static javax.xml.stream.XMLStreamWriter createXMLStreamWriter(java.io.OutputStream)
meth public static javax.xml.stream.XMLStreamWriter createXMLStreamWriter(java.io.OutputStream,java.lang.String)
meth public static javax.xml.stream.XMLStreamWriter createXMLStreamWriter(java.io.OutputStream,java.lang.String,boolean)
meth public static void recycle(javax.xml.stream.XMLStreamWriter)
meth public static void set(com.sun.xml.ws.api.streaming.XMLStreamWriterFactory)
 anno 1 com.sun.istack.NotNull()
supr java.lang.Object
hfds LOGGER,theInstance
hcls HasEncodingWriter

CLSS public abstract interface static com.sun.xml.ws.api.streaming.XMLStreamWriterFactory$RecycleAware
 outer com.sun.xml.ws.api.streaming.XMLStreamWriterFactory
meth public abstract void onRecycled()

CLSS public final com.sun.xml.ws.developer.BindingTypeFeature
 anno 0 org.glassfish.gmbal.ManagedData(java.lang.String name="")
cons public init(java.lang.String)
fld public final static java.lang.String ID = "http://jax-ws.dev.java.net/features/binding"
meth public java.lang.String getBindingId()
 anno 0 org.glassfish.gmbal.ManagedAttribute(java.lang.String id="")
meth public java.lang.String getID()
 anno 0 org.glassfish.gmbal.ManagedAttribute(java.lang.String id="")
supr javax.xml.ws.WebServiceFeature
hfds bindingId

CLSS public final com.sun.xml.ws.developer.EPRRecipe
cons public init()
meth public !varargs com.sun.xml.ws.developer.EPRRecipe addMetadata(javax.xml.transform.Source[])
meth public !varargs com.sun.xml.ws.developer.EPRRecipe addReferenceParameters(com.sun.xml.ws.api.message.Header[])
meth public com.sun.xml.ws.developer.EPRRecipe addMetadata(java.lang.Iterable<? extends javax.xml.transform.Source>)
meth public com.sun.xml.ws.developer.EPRRecipe addMetadata(javax.xml.transform.Source)
meth public com.sun.xml.ws.developer.EPRRecipe addReferenceParameter(com.sun.xml.ws.api.message.Header)
meth public com.sun.xml.ws.developer.EPRRecipe addReferenceParameters(java.lang.Iterable<? extends com.sun.xml.ws.api.message.Header>)
meth public java.util.List<com.sun.xml.ws.api.message.Header> getReferenceParameters()
 anno 0 com.sun.istack.NotNull()
meth public java.util.List<javax.xml.transform.Source> getMetadata()
 anno 0 com.sun.istack.NotNull()
supr java.lang.Object
hfds metadata,referenceParameters

CLSS public final com.sun.xml.ws.developer.HttpConfigFeature
cons public init()
cons public init(java.net.CookieHandler)
fld public final static java.lang.String ID = "http://jax-ws.java.net/features/http-config"
meth public java.lang.String getID()
meth public java.net.CookieHandler getCookieHandler()
supr javax.xml.ws.WebServiceFeature
hfds cookieJar,cookieManagerConstructor,cookiePolicy

CLSS public abstract interface com.sun.xml.ws.developer.JAXBContextFactory
fld public final static com.sun.xml.ws.developer.JAXBContextFactory DEFAULT
meth public abstract com.sun.xml.bind.api.JAXBRIContext createJAXBContext(com.sun.xml.ws.api.model.SEIModel,java.util.List<java.lang.Class>,java.util.List<com.sun.xml.bind.api.TypeReference>) throws javax.xml.bind.JAXBException
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
 anno 3 com.sun.istack.NotNull()

CLSS public abstract interface com.sun.xml.ws.developer.JAXWSProperties
fld public final static java.lang.String ADDRESSING_ACTION = "com.sun.xml.ws.api.addressing.action"
fld public final static java.lang.String ADDRESSING_FROM = "com.sun.xml.ws.api.addressing.from"
fld public final static java.lang.String ADDRESSING_MESSAGEID = "com.sun.xml.ws.api.addressing.messageId"
fld public final static java.lang.String ADDRESSING_TO = "com.sun.xml.ws.api.addressing.to"
fld public final static java.lang.String CONNECT_TIMEOUT = "com.sun.xml.ws.connect.timeout"
fld public final static java.lang.String CONTENT_NEGOTIATION_PROPERTY = "com.sun.xml.ws.client.ContentNegotiation"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String HOSTNAME_VERIFIER = "com.sun.xml.ws.transport.https.client.hostname.verifier"
fld public final static java.lang.String HTTP_CLIENT_STREAMING_CHUNK_SIZE = "com.sun.xml.ws.transport.http.client.streaming.chunk.size"
fld public final static java.lang.String HTTP_EXCHANGE = "com.sun.xml.ws.http.exchange"
fld public final static java.lang.String HTTP_REQUEST_URL = "com.sun.xml.ws.transport.http.servlet.requestURL"
fld public final static java.lang.String INBOUND_HEADER_LIST_PROPERTY = "com.sun.xml.ws.api.message.HeaderList"
fld public final static java.lang.String MTOM_THRESHOLOD_VALUE = "com.sun.xml.ws.common.MtomThresholdValue"
fld public final static java.lang.String REQUEST_TIMEOUT = "com.sun.xml.ws.request.timeout"
fld public final static java.lang.String REST_BINDING = "http://jax-ws.dev.java.net/rest"
fld public final static java.lang.String SSL_SOCKET_FACTORY = "com.sun.xml.ws.transport.https.client.SSLSocketFactory"
fld public final static java.lang.String WSENDPOINT = "com.sun.xml.ws.api.server.WSEndpoint"

CLSS public abstract interface !annotation com.sun.xml.ws.developer.MemberSubmissionAddressing
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
 anno 0 javax.xml.ws.spi.WebServiceFeatureAnnotation(java.lang.Class<? extends javax.xml.ws.WebServiceFeature> bean=class com.sun.xml.ws.developer.MemberSubmissionAddressingFeature, java.lang.String id="http://java.sun.com/xml/ns/jaxws/2004/08/addressing")
innr public final static !enum Validation
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault boolean required()
meth public abstract !hasdefault com.sun.xml.ws.developer.MemberSubmissionAddressing$Validation validation()

CLSS public final static !enum com.sun.xml.ws.developer.MemberSubmissionAddressing$Validation
 outer com.sun.xml.ws.developer.MemberSubmissionAddressing
fld public final static com.sun.xml.ws.developer.MemberSubmissionAddressing$Validation LAX
fld public final static com.sun.xml.ws.developer.MemberSubmissionAddressing$Validation STRICT
meth public static com.sun.xml.ws.developer.MemberSubmissionAddressing$Validation valueOf(java.lang.String)
meth public static com.sun.xml.ws.developer.MemberSubmissionAddressing$Validation[] values()
supr java.lang.Enum<com.sun.xml.ws.developer.MemberSubmissionAddressing$Validation>

CLSS public com.sun.xml.ws.developer.MemberSubmissionAddressingFeature
 anno 0 org.glassfish.gmbal.ManagedData(java.lang.String name="")
cons public init()
cons public init(boolean)
cons public init(boolean,boolean)
cons public init(boolean,boolean,com.sun.xml.ws.developer.MemberSubmissionAddressing$Validation)
fld public final static java.lang.String ID = "http://java.sun.com/xml/ns/jaxws/2004/08/addressing"
fld public final static java.lang.String IS_REQUIRED = "ADDRESSING_IS_REQUIRED"
meth public boolean isRequired()
 anno 0 org.glassfish.gmbal.ManagedAttribute(java.lang.String id="")
meth public com.sun.xml.ws.developer.MemberSubmissionAddressing$Validation getValidation()
 anno 0 org.glassfish.gmbal.ManagedAttribute(java.lang.String id="")
meth public java.lang.String getID()
 anno 0 org.glassfish.gmbal.ManagedAttribute(java.lang.String id="")
meth public void setRequired(boolean)
meth public void setValidation(com.sun.xml.ws.developer.MemberSubmissionAddressing$Validation)
supr javax.xml.ws.WebServiceFeature
hfds required,validation

CLSS public final com.sun.xml.ws.developer.MemberSubmissionEndpointReference
cons public init()
cons public init(javax.xml.transform.Source)
 anno 1 com.sun.istack.NotNull()
fld protected final static java.lang.String MSNS = "http://schemas.xmlsoap.org/ws/2004/08/addressing"
fld public com.sun.xml.ws.developer.MemberSubmissionEndpointReference$Address addr
fld public com.sun.xml.ws.developer.MemberSubmissionEndpointReference$AttributedQName portTypeName
fld public com.sun.xml.ws.developer.MemberSubmissionEndpointReference$Elements referenceParameters
fld public com.sun.xml.ws.developer.MemberSubmissionEndpointReference$Elements referenceProperties
fld public com.sun.xml.ws.developer.MemberSubmissionEndpointReference$ServiceNameType serviceName
fld public java.util.List<org.w3c.dom.Element> elements
fld public java.util.Map<javax.xml.namespace.QName,java.lang.String> attributes
innr public static Address
innr public static AttributedQName
innr public static Elements
innr public static ServiceNameType
intf com.sun.xml.ws.addressing.v200408.MemberSubmissionAddressingConstants
meth public javax.xml.transform.Source toWSDLSource()
meth public void writeTo(javax.xml.transform.Result)
supr javax.xml.ws.EndpointReference
hfds msjc

CLSS public static com.sun.xml.ws.developer.MemberSubmissionEndpointReference$Address
 outer com.sun.xml.ws.developer.MemberSubmissionEndpointReference
cons public init()
fld public java.lang.String uri
fld public java.util.Map<javax.xml.namespace.QName,java.lang.String> attributes
supr java.lang.Object

CLSS public static com.sun.xml.ws.developer.MemberSubmissionEndpointReference$AttributedQName
 outer com.sun.xml.ws.developer.MemberSubmissionEndpointReference
cons public init()
fld public java.util.Map<javax.xml.namespace.QName,java.lang.String> attributes
fld public javax.xml.namespace.QName name
supr java.lang.Object

CLSS public static com.sun.xml.ws.developer.MemberSubmissionEndpointReference$Elements
 outer com.sun.xml.ws.developer.MemberSubmissionEndpointReference
cons public init()
fld public java.util.List<org.w3c.dom.Element> elements
supr java.lang.Object

CLSS public static com.sun.xml.ws.developer.MemberSubmissionEndpointReference$ServiceNameType
 outer com.sun.xml.ws.developer.MemberSubmissionEndpointReference
cons public init()
fld public java.lang.String portName
supr com.sun.xml.ws.developer.MemberSubmissionEndpointReference$AttributedQName

CLSS public abstract interface !annotation com.sun.xml.ws.developer.SchemaValidation
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
 anno 0 javax.xml.ws.spi.WebServiceFeatureAnnotation(java.lang.Class<? extends javax.xml.ws.WebServiceFeature> bean=class com.sun.xml.ws.developer.SchemaValidationFeature, java.lang.String id="http://jax-ws.dev.java.net/features/schema-validation")
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean inbound()
meth public abstract !hasdefault boolean outbound()
meth public abstract !hasdefault java.lang.Class<? extends com.sun.xml.ws.developer.ValidationErrorHandler> handler()

CLSS public com.sun.xml.ws.developer.SchemaValidationFeature
 anno 0 org.glassfish.gmbal.ManagedData(java.lang.String name="")
cons public init()
cons public init(boolean,boolean)
cons public init(boolean,boolean,java.lang.Class<? extends com.sun.xml.ws.developer.ValidationErrorHandler>)
cons public init(java.lang.Class<? extends com.sun.xml.ws.developer.ValidationErrorHandler>)
fld public final static java.lang.String ID = "http://jax-ws.dev.java.net/features/schema-validation"
meth public boolean isInbound()
meth public boolean isOutbound()
meth public java.lang.Class<? extends com.sun.xml.ws.developer.ValidationErrorHandler> getErrorHandler()
 anno 0 org.glassfish.gmbal.ManagedAttribute(java.lang.String id="")
meth public java.lang.String getID()
 anno 0 org.glassfish.gmbal.ManagedAttribute(java.lang.String id="")
supr javax.xml.ws.WebServiceFeature
hfds clazz,inbound,outbound

CLSS public abstract interface !annotation com.sun.xml.ws.developer.Serialization
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
 anno 0 javax.xml.ws.spi.WebServiceFeatureAnnotation(java.lang.Class<? extends javax.xml.ws.WebServiceFeature> bean=class com.sun.xml.ws.developer.SerializationFeature, java.lang.String id="http://jax-ws.java.net/features/serialization")
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String encoding()

CLSS public com.sun.xml.ws.developer.SerializationFeature
cons public init()
cons public init(java.lang.String)
fld public final static java.lang.String ID = "http://jax-ws.java.net/features/serialization"
meth public java.lang.String getEncoding()
meth public java.lang.String getID()
supr javax.xml.ws.WebServiceFeature
hfds encoding

CLSS public com.sun.xml.ws.developer.ServerSideException
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getMessage()
meth public java.lang.String toString()
supr java.lang.Exception
hfds className

CLSS public abstract interface !annotation com.sun.xml.ws.developer.Stateful
 anno 0 com.sun.xml.ws.api.server.InstanceResolverAnnotation(java.lang.Class<? extends com.sun.xml.ws.api.server.InstanceResolver> value=class com.sun.xml.ws.server.StatefulInstanceResolver)
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
 anno 0 javax.xml.ws.spi.WebServiceFeatureAnnotation(java.lang.Class<? extends javax.xml.ws.WebServiceFeature> bean=class com.sun.xml.ws.developer.StatefulFeature, java.lang.String id="http://jax-ws.dev.java.net/features/stateful")
intf java.lang.annotation.Annotation

CLSS public com.sun.xml.ws.developer.StatefulFeature
 anno 0 org.glassfish.gmbal.ManagedData(java.lang.String name="")
cons public init()
fld public final static java.lang.String ID = "http://jax-ws.dev.java.net/features/stateful"
intf com.sun.xml.ws.api.ha.StickyFeature
meth public java.lang.String getID()
 anno 0 org.glassfish.gmbal.ManagedAttribute(java.lang.String id="")
supr javax.xml.ws.WebServiceFeature

CLSS public abstract interface com.sun.xml.ws.developer.StatefulWebServiceManager<%0 extends java.lang.Object>
innr public abstract interface static Callback
meth public abstract <%0 extends javax.xml.ws.EndpointReference> {%%0} export(java.lang.Class<{%%0}>,com.sun.xml.ws.api.message.Packet,{com.sun.xml.ws.developer.StatefulWebServiceManager%0})
 anno 0 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public abstract <%0 extends javax.xml.ws.EndpointReference> {%%0} export(java.lang.Class<{%%0}>,com.sun.xml.ws.api.message.Packet,{com.sun.xml.ws.developer.StatefulWebServiceManager%0},com.sun.xml.ws.developer.EPRRecipe)
 anno 0 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public abstract <%0 extends javax.xml.ws.EndpointReference> {%%0} export(java.lang.Class<{%%0}>,java.lang.String,{com.sun.xml.ws.developer.StatefulWebServiceManager%0})
 anno 0 com.sun.istack.NotNull()
meth public abstract <%0 extends javax.xml.ws.EndpointReference> {%%0} export(java.lang.Class<{%%0}>,javax.xml.ws.WebServiceContext,{com.sun.xml.ws.developer.StatefulWebServiceManager%0})
 anno 0 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public abstract <%0 extends javax.xml.ws.EndpointReference> {%%0} export(java.lang.Class<{%%0}>,{com.sun.xml.ws.developer.StatefulWebServiceManager%0})
 anno 0 com.sun.istack.NotNull()
meth public abstract <%0 extends javax.xml.ws.EndpointReference> {%%0} export(java.lang.Class<{%%0}>,{com.sun.xml.ws.developer.StatefulWebServiceManager%0},com.sun.xml.ws.developer.EPRRecipe)
 anno 0 com.sun.istack.NotNull()
 anno 3 com.sun.istack.Nullable()
meth public abstract javax.xml.ws.wsaddressing.W3CEndpointReference export({com.sun.xml.ws.developer.StatefulWebServiceManager%0})
 anno 0 com.sun.istack.NotNull()
meth public abstract void setFallbackInstance({com.sun.xml.ws.developer.StatefulWebServiceManager%0})
meth public abstract void setTimeout(long,com.sun.xml.ws.developer.StatefulWebServiceManager$Callback<{com.sun.xml.ws.developer.StatefulWebServiceManager%0}>)
 anno 2 com.sun.istack.Nullable()
meth public abstract void touch({com.sun.xml.ws.developer.StatefulWebServiceManager%0})
meth public abstract void unexport({com.sun.xml.ws.developer.StatefulWebServiceManager%0})
 anno 1 com.sun.istack.Nullable()
meth public abstract {com.sun.xml.ws.developer.StatefulWebServiceManager%0} resolve(javax.xml.ws.EndpointReference)
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.NotNull()

CLSS public abstract interface static com.sun.xml.ws.developer.StatefulWebServiceManager$Callback<%0 extends java.lang.Object>
 outer com.sun.xml.ws.developer.StatefulWebServiceManager
meth public abstract void onTimeout({com.sun.xml.ws.developer.StatefulWebServiceManager$Callback%0},com.sun.xml.ws.developer.StatefulWebServiceManager<{com.sun.xml.ws.developer.StatefulWebServiceManager$Callback%0}>)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()

CLSS public abstract interface !annotation com.sun.xml.ws.developer.StreamingAttachment
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
 anno 0 javax.xml.ws.spi.WebServiceFeatureAnnotation(java.lang.Class<? extends javax.xml.ws.WebServiceFeature> bean=class com.sun.xml.ws.developer.StreamingAttachmentFeature, java.lang.String id="http://jax-ws.dev.java.net/features/mime")
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean parseEagerly()
meth public abstract !hasdefault java.lang.String dir()
meth public abstract !hasdefault long memoryThreshold()

CLSS public final com.sun.xml.ws.developer.StreamingAttachmentFeature
 anno 0 org.glassfish.gmbal.ManagedData(java.lang.String name="")
cons public init()
cons public init(java.lang.String,boolean,long)
 anno 1 com.sun.istack.Nullable()
fld public final static java.lang.String ID = "http://jax-ws.dev.java.net/features/mime"
meth public java.lang.String getID()
 anno 0 org.glassfish.gmbal.ManagedAttribute(java.lang.String id="")
meth public org.jvnet.mimepull.MIMEConfig getConfig()
 anno 0 org.glassfish.gmbal.ManagedAttribute(java.lang.String id="")
meth public void setDir(java.lang.String)
meth public void setMemoryThreshold(long)
meth public void setParseEagerly(boolean)
supr javax.xml.ws.WebServiceFeature
hfds config,dir,memoryThreshold,parseEagerly

CLSS public abstract com.sun.xml.ws.developer.StreamingDataHandler
cons public init(java.lang.Object,java.lang.String)
cons public init(java.net.URL)
cons public init(javax.activation.DataSource)
supr org.jvnet.staxex.StreamingDataHandler

CLSS public abstract interface !annotation com.sun.xml.ws.developer.UsesJAXBContext
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
 anno 0 javax.xml.ws.spi.WebServiceFeatureAnnotation(java.lang.Class<? extends javax.xml.ws.WebServiceFeature> bean=class com.sun.xml.ws.developer.UsesJAXBContextFeature, java.lang.String id="http://jax-ws.dev.java.net/features/uses-jaxb-context")
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<? extends com.sun.xml.ws.developer.JAXBContextFactory> value()

CLSS public com.sun.xml.ws.developer.UsesJAXBContextFeature
 anno 0 org.glassfish.gmbal.ManagedData(java.lang.String name="")
cons public init(com.sun.xml.bind.api.JAXBRIContext)
 anno 1 com.sun.istack.Nullable()
cons public init(com.sun.xml.ws.developer.JAXBContextFactory)
 anno 1 com.sun.istack.Nullable()
cons public init(java.lang.Class<? extends com.sun.xml.ws.developer.JAXBContextFactory>)
 anno 1 com.sun.istack.NotNull()
fld public final static java.lang.String ID = "http://jax-ws.dev.java.net/features/uses-jaxb-context"
meth public com.sun.xml.ws.developer.JAXBContextFactory getFactory()
 anno 0 com.sun.istack.Nullable()
 anno 0 org.glassfish.gmbal.ManagedAttribute(java.lang.String id="")
meth public java.lang.String getID()
 anno 0 org.glassfish.gmbal.ManagedAttribute(java.lang.String id="")
supr javax.xml.ws.WebServiceFeature
hfds factory

CLSS public abstract com.sun.xml.ws.developer.ValidationErrorHandler
cons public init()
fld protected com.sun.xml.ws.api.message.Packet packet
intf org.xml.sax.ErrorHandler
meth public void setPacket(com.sun.xml.ws.api.message.Packet)
supr java.lang.Object

CLSS public abstract interface com.sun.xml.ws.developer.WSBindingProvider
intf com.sun.xml.ws.api.ComponentRegistry
intf java.io.Closeable
intf javax.xml.ws.BindingProvider
meth public abstract !varargs void setOutboundHeaders(com.sun.xml.ws.api.message.Header[])
meth public abstract !varargs void setOutboundHeaders(java.lang.Object[])
meth public abstract com.sun.xml.ws.api.addressing.WSEndpointReference getWSEndpointReference()
meth public abstract com.sun.xml.ws.api.client.WSPortInfo getPortInfo()
meth public abstract java.util.List<com.sun.xml.ws.api.message.Header> getInboundHeaders()
meth public abstract org.glassfish.gmbal.ManagedObjectManager getManagedObjectManager()
 anno 0 com.sun.istack.NotNull()
meth public abstract void setAddress(java.lang.String)
meth public abstract void setOutboundHeaders(java.util.List<com.sun.xml.ws.api.message.Header>)

CLSS abstract interface com.sun.xml.ws.developer.package-info

CLSS public com.sun.xml.ws.encoding.soap.DeserializationException
cons public !varargs init(java.lang.String,java.lang.Object[])
cons public init(com.sun.xml.ws.util.localization.Localizable)
cons public init(java.lang.Throwable)
meth public java.lang.String getDefaultResourceBundleName()
supr com.sun.xml.ws.util.exception.JAXWSExceptionBase

CLSS public com.sun.xml.ws.encoding.soap.SOAP12Constants
cons public init()
fld public final static java.lang.String URI_ENCODING = "http://schemas.xmlsoap.org/soap/encoding/"
fld public final static java.lang.String URI_ENVELOPE = "http://www.w3.org/2003/05/soap-envelope"
fld public final static java.lang.String URI_HTTP = "http://www.w3.org/2003/05/soap/bindings/HTTP/"
fld public final static java.lang.String URI_SOAP_RPC = "http://www.w3.org/2002/06/soap-rpc"
fld public final static javax.xml.namespace.QName FAULT_CODE_BAD_ARGUMENTS
fld public final static javax.xml.namespace.QName FAULT_CODE_DATA_ENCODING_UNKNOWN
fld public final static javax.xml.namespace.QName FAULT_CODE_MISUNDERSTOOD
fld public final static javax.xml.namespace.QName FAULT_CODE_MUST_UNDERSTAND
fld public final static javax.xml.namespace.QName FAULT_CODE_PROCEDURE_NOT_PRESENT
fld public final static javax.xml.namespace.QName FAULT_CODE_VERSION_MISMATCH
fld public final static javax.xml.namespace.QName QNAME_ENVELOPE_ENCODINGSTYLE
fld public final static javax.xml.namespace.QName QNAME_FAULT_CODE
fld public final static javax.xml.namespace.QName QNAME_FAULT_DETAIL
fld public final static javax.xml.namespace.QName QNAME_FAULT_NODE
fld public final static javax.xml.namespace.QName QNAME_FAULT_REASON
fld public final static javax.xml.namespace.QName QNAME_FAULT_REASON_TEXT
fld public final static javax.xml.namespace.QName QNAME_FAULT_ROLE
fld public final static javax.xml.namespace.QName QNAME_FAULT_SUBCODE
fld public final static javax.xml.namespace.QName QNAME_FAULT_VALUE
fld public final static javax.xml.namespace.QName QNAME_MUSTUNDERSTAND
fld public final static javax.xml.namespace.QName QNAME_NOT_UNDERSTOOD
fld public final static javax.xml.namespace.QName QNAME_ROLE
fld public final static javax.xml.namespace.QName QNAME_SOAP_BODY
fld public final static javax.xml.namespace.QName QNAME_SOAP_ENVELOPE
fld public final static javax.xml.namespace.QName QNAME_SOAP_FAULT
fld public final static javax.xml.namespace.QName QNAME_SOAP_HEADER
fld public final static javax.xml.namespace.QName QNAME_SOAP_RESULT
fld public final static javax.xml.namespace.QName QNAME_SOAP_RPC
fld public final static javax.xml.namespace.QName QNAME_UPGRADE
fld public final static javax.xml.namespace.QName QNAME_UPGRADE_SUPPORTED_ENVELOPE
supr java.lang.Object

CLSS public com.sun.xml.ws.encoding.soap.SOAPConstants
cons public init()
fld public final static java.lang.String NS_WSDL_SOAP = "http://schemas.xmlsoap.org/wsdl/soap/"
fld public final static java.lang.String URI_ENCODING = "http://schemas.xmlsoap.org/soap/encoding/"
fld public final static java.lang.String URI_ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope/"
fld public final static java.lang.String URI_HTTP = "http://schemas.xmlsoap.org/soap/http"
fld public final static javax.xml.namespace.QName FAULT_CODE_BAD_ARGUMENTS
fld public final static javax.xml.namespace.QName FAULT_CODE_DATA_ENCODING_UNKNOWN
fld public final static javax.xml.namespace.QName FAULT_CODE_MUST_UNDERSTAND
fld public final static javax.xml.namespace.QName FAULT_CODE_PROCEDURE_NOT_PRESENT
fld public final static javax.xml.namespace.QName FAULT_CODE_VERSION_MISMATCH
fld public final static javax.xml.namespace.QName QNAME_ENVELOPE_ENCODINGSTYLE
fld public final static javax.xml.namespace.QName QNAME_MUSTUNDERSTAND
fld public final static javax.xml.namespace.QName QNAME_ROLE
fld public final static javax.xml.namespace.QName QNAME_SOAP_BODY
fld public final static javax.xml.namespace.QName QNAME_SOAP_ENVELOPE
fld public final static javax.xml.namespace.QName QNAME_SOAP_FAULT
fld public final static javax.xml.namespace.QName QNAME_SOAP_FAULT_ACTOR
fld public final static javax.xml.namespace.QName QNAME_SOAP_FAULT_CODE
fld public final static javax.xml.namespace.QName QNAME_SOAP_FAULT_DETAIL
fld public final static javax.xml.namespace.QName QNAME_SOAP_FAULT_STRING
fld public final static javax.xml.namespace.QName QNAME_SOAP_HEADER
supr java.lang.Object

CLSS public com.sun.xml.ws.encoding.soap.SerializationException
cons public !varargs init(java.lang.String,java.lang.Object[])
cons public init(com.sun.xml.ws.util.localization.Localizable)
cons public init(java.lang.Throwable)
meth public java.lang.String getDefaultResourceBundleName()
supr com.sun.xml.ws.util.exception.JAXWSExceptionBase

CLSS public abstract interface com.sun.xml.ws.encoding.soap.SerializerConstants
fld public final static boolean DONT_ENCODE_TYPE = false
fld public final static boolean DONT_SERIALIZE_AS_REF = false
fld public final static boolean ENCODE_TYPE = true
fld public final static boolean NOT_NULLABLE = false
fld public final static boolean NOT_REFERENCEABLE = false
fld public final static boolean NULLABLE = true
fld public final static boolean REFERENCEABLE = true
fld public final static boolean REFERENCED_INSTANCE = true
fld public final static boolean SERIALIZE_AS_REF = true
fld public final static boolean UNREFERENCED_INSTANCE = false

CLSS public com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants
cons public init()
fld public final static java.lang.String ACTOR_NEXT = "http://www.w3.org/2003/05/soap-envelope/role/next"
fld public final static java.lang.String ATTR_ACTOR = "role"
fld public final static java.lang.String ATTR_ENCODING_STYLE = "encodingStyle"
fld public final static java.lang.String ATTR_MISUNDERSTOOD = "missUnderstood"
fld public final static java.lang.String ATTR_MUST_UNDERSTAND = "mustUnderstand"
fld public final static java.lang.String ATTR_NOT_UNDERSTOOD_QNAME = "qname"
fld public final static java.lang.String ENCODING = "http://www.w3.org/2003/05/soap-encoding"
fld public final static java.lang.String ENVELOPE = "http://www.w3.org/2003/05/soap-envelope"
fld public final static java.lang.String ROLE_NEXT = "http://www.w3.org/2003/05/soap-envelope/role/next"
fld public final static java.lang.String ROLE_NONE = "http://www.w3.org/2003/05/soap-envelope/role/none"
fld public final static java.lang.String ROLE_ULTIMATE_RECEIVER = "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver"
fld public final static java.lang.String SOAP_RPC = "http://www.w3.org/2002/06/soap-rpc"
fld public final static java.lang.String SOAP_UPGRADE = "http://www.w3.org/2002/06/soap-upgrade"
fld public final static java.lang.String TAG_BODY = "Body"
fld public final static java.lang.String TAG_ENVELOPE = "Envelope"
fld public final static java.lang.String TAG_HEADER = "Header"
fld public final static java.lang.String TAG_NOT_UNDERSTOOD = "NotUnderstood"
fld public final static java.lang.String TAG_RESULT = "result"
fld public final static java.lang.String TRANSPORT_HTTP = "http://www.w3.org/2003/05/soap/bindings/HTTP/"
fld public final static java.lang.String XML_NS = "http://www.w3.org/XML/1998/namespace"
fld public final static java.lang.String XSD = "http://www.w3.org/2001/XMLSchema"
fld public final static java.lang.String XSI = "http://www.w3.org/2001/XMLSchema-instance"
supr java.lang.Object

CLSS public com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants
cons public init()
fld public final static java.lang.String ACTOR_NEXT = "http://schemas.xmlsoap.org/soap/actor/next"
fld public final static java.lang.String ATTR_ACTOR = "actor"
fld public final static java.lang.String ATTR_ENCODING_STYLE = "encodingStyle"
fld public final static java.lang.String ATTR_MUST_UNDERSTAND = "mustUnderstand"
fld public final static java.lang.String ENCODING = "http://schemas.xmlsoap.org/soap/encoding/"
fld public final static java.lang.String ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope/"
fld public final static java.lang.String NSPREFIX_SOAP_ENVELOPE = "soapenv"
fld public final static java.lang.String TAG_BODY = "Body"
fld public final static java.lang.String TAG_ENVELOPE = "Envelope"
fld public final static java.lang.String TAG_FAULT = "Fault"
fld public final static java.lang.String TAG_HEADER = "Header"
fld public final static java.lang.String TRANSPORT_HTTP = "http://schemas.xmlsoap.org/soap/http"
fld public final static java.lang.String XMLNS = "http://www.w3.org/XML/1998/namespace"
fld public final static java.lang.String XSD = "http://www.w3.org/2001/XMLSchema"
fld public final static java.lang.String XSI = "http://www.w3.org/2001/XMLSchema-instance"
supr java.lang.Object

CLSS abstract interface com.sun.xml.ws.package-info

CLSS public final !enum com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion
fld public final static com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion v1_2
fld public final static com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion v1_5
meth public java.lang.String getDefaultNamespacePrefix()
meth public java.lang.String toString()
meth public javax.xml.namespace.QName asQName(com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken)
meth public static com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion getLatestVersion()
meth public static com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion resolveVersion(java.lang.String)
meth public static com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion resolveVersion(javax.xml.namespace.QName)
meth public static com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion valueOf(java.lang.String)
meth public static com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion[] values()
meth public static com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken resolveAsToken(javax.xml.namespace.QName)
supr java.lang.Enum<com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion>
hfds defaultNsPrefix,nsUri,tokenToQNameCache

CLSS public final !enum com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken
fld public final static com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken All
fld public final static com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken Digest
fld public final static com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken DigestAlgorithm
fld public final static com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken ExactlyOne
fld public final static com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken Ignorable
fld public final static com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken Name
fld public final static com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken Optional
fld public final static com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken Policy
fld public final static com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken PolicyReference
fld public final static com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken PolicyUris
fld public final static com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken UNKNOWN
fld public final static com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken Uri
fld public final static com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken UsingPolicy
meth public boolean isElement()
meth public java.lang.String toString()
meth public static com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken resolveToken(java.lang.String)
meth public static com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken valueOf(java.lang.String)
meth public static com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken[] values()
supr java.lang.Enum<com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken>
hfds element,tokenName

CLSS public com.sun.xml.ws.spi.ProviderImpl
cons public init()
fld public final static com.sun.xml.ws.spi.ProviderImpl INSTANCE
meth public !varargs <%0 extends java.lang.Object> {%%0} getPort(javax.xml.ws.EndpointReference,java.lang.Class<{%%0}>,javax.xml.ws.WebServiceFeature[])
meth public !varargs javax.xml.ws.Endpoint createAndPublishEndpoint(java.lang.String,java.lang.Object,javax.xml.ws.WebServiceFeature[])
meth public !varargs javax.xml.ws.Endpoint createEndpoint(java.lang.String,java.lang.Class,javax.xml.ws.spi.Invoker,javax.xml.ws.WebServiceFeature[])
meth public !varargs javax.xml.ws.Endpoint createEndpoint(java.lang.String,java.lang.Object,javax.xml.ws.WebServiceFeature[])
meth public !varargs javax.xml.ws.spi.ServiceDelegate createServiceDelegate(java.net.URL,javax.xml.namespace.QName,java.lang.Class,javax.xml.ws.WebServiceFeature[])
meth public javax.xml.ws.Endpoint createAndPublishEndpoint(java.lang.String,java.lang.Object)
meth public javax.xml.ws.Endpoint createEndpoint(java.lang.String,java.lang.Object)
meth public javax.xml.ws.EndpointReference readEndpointReference(javax.xml.transform.Source)
meth public javax.xml.ws.spi.ServiceDelegate createServiceDelegate(java.net.URL,javax.xml.namespace.QName,java.lang.Class)
meth public javax.xml.ws.spi.ServiceDelegate createServiceDelegate(javax.xml.transform.Source,javax.xml.namespace.QName,java.lang.Class)
meth public javax.xml.ws.wsaddressing.W3CEndpointReference createW3CEndpointReference(java.lang.String,javax.xml.namespace.QName,javax.xml.namespace.QName,java.util.List<org.w3c.dom.Element>,java.lang.String,java.util.List<org.w3c.dom.Element>)
meth public javax.xml.ws.wsaddressing.W3CEndpointReference createW3CEndpointReference(java.lang.String,javax.xml.namespace.QName,javax.xml.namespace.QName,javax.xml.namespace.QName,java.util.List<org.w3c.dom.Element>,java.lang.String,java.util.List<org.w3c.dom.Element>,java.util.List<org.w3c.dom.Element>,java.util.Map<javax.xml.namespace.QName,java.lang.String>)
supr javax.xml.ws.spi.Provider
hfds eprjc

CLSS public com.sun.xml.ws.util.ASCIIUtility
meth public static byte[] getBytes(java.io.InputStream) throws java.io.IOException
meth public static byte[] getBytes(java.lang.String)
meth public static int parseInt(byte[],int,int,int)
meth public static java.lang.String toString(byte[],int,int)
meth public static void copyStream(java.io.InputStream,java.io.OutputStream) throws java.io.IOException
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
meth public static org.w3c.dom.Node createDOMNode(java.io.InputStream)
meth public static void serializeNode(org.w3c.dom.Element,javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
meth public static void writeTagWithAttributes(org.w3c.dom.Element,javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
supr java.lang.Object
hfds db

CLSS public com.sun.xml.ws.util.FastInfosetReflection
cons public init()
fld public final static java.lang.reflect.Constructor fiStAXDocumentParser_new
fld public final static java.lang.reflect.Method fiStAXDocumentParser_setInputStream
fld public final static java.lang.reflect.Method fiStAXDocumentParser_setStringInterning
supr java.lang.Object

CLSS public com.sun.xml.ws.util.FastInfosetUtil
cons public init()
meth public static javax.xml.stream.XMLStreamReader createFIStreamReader(java.io.InputStream)
supr java.lang.Object

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

CLSS public com.sun.xml.ws.util.NamespaceSupport
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
hfds closed,fileStream,memStream,readAll
hcls FileStream,MemoryStream

CLSS public com.sun.xml.ws.util.ReadOnlyPropertyException
cons public init(java.lang.String)
meth public java.lang.String getPropertyName()
supr java.lang.IllegalArgumentException
hfds propertyName

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
meth public static <%0 extends java.lang.Object> com.sun.xml.ws.util.ServiceFinder<{%%0}> find(java.lang.Class<{%%0}>,com.sun.xml.ws.api.Component)
 anno 1 com.sun.istack.NotNull()
meth public static <%0 extends java.lang.Object> com.sun.xml.ws.util.ServiceFinder<{%%0}> find(java.lang.Class<{%%0}>,java.lang.ClassLoader)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.Nullable()
meth public static <%0 extends java.lang.Object> com.sun.xml.ws.util.ServiceFinder<{%%0}> find(java.lang.Class<{%%0}>,java.lang.ClassLoader,com.sun.xml.ws.api.Component)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.Nullable()
meth public {com.sun.xml.ws.util.ServiceFinder%0}[] toArray()
supr java.lang.Object
hfds classLoader,component,prefix,serviceClass
hcls ComponentExWrapper,CompositeIterator,LazyIterator

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
cons public init(com.sun.xml.ws.util.localization.Localizable)
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
cons protected init(com.sun.xml.ws.util.localization.Localizable)
cons protected init(com.sun.xml.ws.util.localization.Localizable,java.lang.Throwable)
cons protected init(java.lang.String)
cons protected init(java.lang.Throwable)
intf com.sun.xml.ws.util.localization.Localizable
meth protected abstract java.lang.String getDefaultResourceBundleName()
meth public final java.lang.Object[] getArguments()
meth public final java.lang.String getKey()
meth public final java.lang.String getResourceBundleName()
meth public java.lang.String getMessage()
supr javax.xml.ws.WebServiceException
hfds msg,serialVersionUID

CLSS public com.sun.xml.ws.util.exception.LocatableWebServiceException
cons public !varargs init(java.lang.String,java.lang.Throwable,org.xml.sax.Locator[])
cons public !varargs init(java.lang.String,org.xml.sax.Locator[])
cons public !varargs init(java.lang.Throwable,org.xml.sax.Locator[])
cons public init(java.lang.String,java.lang.Throwable,javax.xml.stream.XMLStreamReader)
cons public init(java.lang.String,javax.xml.stream.XMLStreamReader)
cons public init(java.lang.Throwable,javax.xml.stream.XMLStreamReader)
meth public java.util.List<org.xml.sax.Locator> getLocation()
 anno 0 com.sun.istack.NotNull()
supr javax.xml.ws.WebServiceException
hfds location

CLSS public abstract interface com.sun.xml.ws.util.localization.Localizable
fld public final static java.lang.String NOT_LOCALIZABLE
meth public abstract java.lang.Object[] getArguments()
meth public abstract java.lang.String getKey()
meth public abstract java.lang.String getResourceBundleName()

CLSS public final com.sun.xml.ws.util.localization.LocalizableImpl
cons public init(java.lang.String,java.lang.Object[],java.lang.String)
intf com.sun.xml.ws.util.localization.Localizable
meth public java.lang.Object[] getArguments()
meth public java.lang.String getKey()
meth public java.lang.String getResourceBundleName()
supr java.lang.Object
hfds arguments,key,resourceBundleName

CLSS public final com.sun.xml.ws.util.localization.LocalizableMessage
cons public !varargs init(java.lang.String,java.lang.String,java.lang.Object[])
intf com.sun.xml.ws.util.localization.Localizable
meth public java.lang.Object[] getArguments()
meth public java.lang.String getKey()
meth public java.lang.String getResourceBundleName()
supr java.lang.Object
hfds _args,_bundlename,_key

CLSS public com.sun.xml.ws.util.localization.LocalizableMessageFactory
cons public init(java.lang.String)
meth public !varargs com.sun.xml.ws.util.localization.Localizable getMessage(java.lang.String,java.lang.Object[])
supr java.lang.Object
hfds _bundlename

CLSS public com.sun.xml.ws.util.localization.Localizer
cons public init()
cons public init(java.util.Locale)
meth public java.lang.String localize(com.sun.xml.ws.util.localization.Localizable)
meth public java.util.Locale getLocale()
supr java.lang.Object
hfds _locale,_resourceBundles

CLSS public final com.sun.xml.ws.util.localization.NullLocalizable
cons public init(java.lang.String)
intf com.sun.xml.ws.util.localization.Localizable
meth public java.lang.Object[] getArguments()
meth public java.lang.String getKey()
meth public java.lang.String getResourceBundleName()
supr java.lang.Object
hfds msg

CLSS public final com.sun.xml.ws.util.xml.CDATA
cons public init(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getText()
supr java.lang.Object
hfds _text

CLSS public com.sun.xml.ws.util.xml.ContentHandlerToXMLStreamWriter
cons public init(javax.xml.stream.XMLStreamWriter)
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
supr org.xml.sax.helpers.DefaultHandler
hfds prefixBindings,staxWriter

CLSS public final com.sun.xml.ws.util.xml.DummyLocation
fld public final static javax.xml.stream.Location INSTANCE
intf javax.xml.stream.Location
meth public int getCharacterOffset()
meth public int getColumnNumber()
meth public int getLineNumber()
meth public java.lang.String getPublicId()
meth public java.lang.String getSystemId()
supr java.lang.Object

CLSS public com.sun.xml.ws.util.xml.NamedNodeMapIterator
cons public init(org.w3c.dom.NamedNodeMap)
fld protected int _index
fld protected org.w3c.dom.NamedNodeMap _map
intf java.util.Iterator
meth public boolean hasNext()
meth public java.lang.Object next()
meth public void remove()
supr java.lang.Object

CLSS public com.sun.xml.ws.util.xml.NodeListIterator
cons public init(org.w3c.dom.NodeList)
fld protected int _index
fld protected org.w3c.dom.NodeList _list
intf java.util.Iterator
meth public boolean hasNext()
meth public java.lang.Object next()
meth public void remove()
supr java.lang.Object

CLSS public com.sun.xml.ws.util.xml.StAXResult
cons public init(javax.xml.stream.XMLStreamWriter)
supr javax.xml.transform.sax.SAXResult

CLSS public com.sun.xml.ws.util.xml.StAXSource
cons public init(javax.xml.stream.XMLStreamReader,boolean)
cons public init(javax.xml.stream.XMLStreamReader,boolean,java.lang.String[])
 anno 3 com.sun.istack.NotNull()
supr javax.xml.transform.sax.SAXSource
hfds pseudoParser,reader,repeater,staxReader

CLSS public com.sun.xml.ws.util.xml.XMLStreamReaderFilter
cons public init(javax.xml.stream.XMLStreamReader)
fld protected javax.xml.stream.XMLStreamReader reader
intf com.sun.xml.ws.api.streaming.XMLStreamReaderFactory$RecycleAware
intf javax.xml.stream.XMLStreamReader
meth public boolean hasName()
meth public boolean hasNext() throws javax.xml.stream.XMLStreamException
meth public boolean hasText()
meth public boolean isAttributeSpecified(int)
meth public boolean isCharacters()
meth public boolean isEndElement()
meth public boolean isStandalone()
meth public boolean isStartElement()
meth public boolean isWhiteSpace()
meth public boolean standaloneSet()
meth public char[] getTextCharacters()
meth public int getAttributeCount()
meth public int getEventType()
meth public int getNamespaceCount()
meth public int getTextCharacters(int,char[],int,int) throws javax.xml.stream.XMLStreamException
meth public int getTextLength()
meth public int getTextStart()
meth public int next() throws javax.xml.stream.XMLStreamException
meth public int nextTag() throws javax.xml.stream.XMLStreamException
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.String getAttributeLocalName(int)
meth public java.lang.String getAttributeNamespace(int)
meth public java.lang.String getAttributePrefix(int)
meth public java.lang.String getAttributeType(int)
meth public java.lang.String getAttributeValue(int)
meth public java.lang.String getAttributeValue(java.lang.String,java.lang.String)
meth public java.lang.String getCharacterEncodingScheme()
meth public java.lang.String getElementText() throws javax.xml.stream.XMLStreamException
meth public java.lang.String getEncoding()
meth public java.lang.String getLocalName()
meth public java.lang.String getNamespacePrefix(int)
meth public java.lang.String getNamespaceURI()
meth public java.lang.String getNamespaceURI(int)
meth public java.lang.String getNamespaceURI(java.lang.String)
meth public java.lang.String getPIData()
meth public java.lang.String getPITarget()
meth public java.lang.String getPrefix()
meth public java.lang.String getText()
meth public java.lang.String getVersion()
meth public javax.xml.namespace.NamespaceContext getNamespaceContext()
meth public javax.xml.namespace.QName getAttributeName(int)
meth public javax.xml.namespace.QName getName()
meth public javax.xml.stream.Location getLocation()
meth public void close() throws javax.xml.stream.XMLStreamException
meth public void onRecycled()
meth public void require(int,java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
supr java.lang.Object

CLSS public com.sun.xml.ws.util.xml.XMLStreamReaderToXMLStreamWriter
cons public init()
fld protected javax.xml.stream.XMLStreamReader in
fld protected javax.xml.stream.XMLStreamWriter out
meth protected void handleAttribute(int) throws javax.xml.stream.XMLStreamException
meth protected void handleCDATA() throws javax.xml.stream.XMLStreamException
meth protected void handleCharacters() throws javax.xml.stream.XMLStreamException
meth protected void handleComment() throws javax.xml.stream.XMLStreamException
meth protected void handleDTD() throws javax.xml.stream.XMLStreamException
meth protected void handleEndElement() throws javax.xml.stream.XMLStreamException
meth protected void handleEntityReference() throws javax.xml.stream.XMLStreamException
meth protected void handlePI() throws javax.xml.stream.XMLStreamException
meth protected void handleSpace() throws javax.xml.stream.XMLStreamException
meth protected void handleStartElement() throws javax.xml.stream.XMLStreamException
meth public void bridge(javax.xml.stream.XMLStreamReader,javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
supr java.lang.Object
hfds BUF_SIZE,buf

CLSS public com.sun.xml.ws.util.xml.XMLStreamWriterFilter
cons public init(javax.xml.stream.XMLStreamWriter)
fld protected javax.xml.stream.XMLStreamWriter writer
intf com.sun.xml.ws.api.streaming.XMLStreamWriterFactory$RecycleAware
intf javax.xml.stream.XMLStreamWriter
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.String getPrefix(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public javax.xml.namespace.NamespaceContext getNamespaceContext()
meth public void close() throws javax.xml.stream.XMLStreamException
meth public void flush() throws javax.xml.stream.XMLStreamException
meth public void onRecycled()
meth public void setDefaultNamespace(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void setNamespaceContext(javax.xml.namespace.NamespaceContext) throws javax.xml.stream.XMLStreamException
meth public void setPrefix(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeAttribute(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeAttribute(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeAttribute(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeCData(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeCharacters(char[],int,int) throws javax.xml.stream.XMLStreamException
meth public void writeCharacters(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeComment(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeDTD(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeDefaultNamespace(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeEmptyElement(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeEmptyElement(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeEmptyElement(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeEndDocument() throws javax.xml.stream.XMLStreamException
meth public void writeEndElement() throws javax.xml.stream.XMLStreamException
meth public void writeEntityRef(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeNamespace(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeProcessingInstruction(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeProcessingInstruction(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeStartDocument() throws javax.xml.stream.XMLStreamException
meth public void writeStartDocument(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeStartDocument(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeStartElement(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeStartElement(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeStartElement(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
supr java.lang.Object

CLSS public com.sun.xml.ws.util.xml.XmlUtil
cons public init()
fld public final static org.xml.sax.ErrorHandler DRACONIAN_ERROR_HANDLER
meth public static <%0 extends javax.xml.transform.Result> {%%0} identityTransform(javax.xml.transform.Source,{%%0}) throws java.io.IOException,javax.xml.parsers.ParserConfigurationException,javax.xml.transform.TransformerException,org.xml.sax.SAXException
meth public static java.io.InputStream getUTF8Stream(java.lang.String)
meth public static java.lang.String getAttributeNSOrNull(org.w3c.dom.Element,java.lang.String,java.lang.String)
meth public static java.lang.String getAttributeNSOrNull(org.w3c.dom.Element,javax.xml.namespace.QName)
meth public static java.lang.String getAttributeOrNull(org.w3c.dom.Element,java.lang.String)
meth public static java.lang.String getLocalPart(java.lang.String)
meth public static java.lang.String getPrefix(java.lang.String)
meth public static java.lang.String getTextForNode(org.w3c.dom.Node)
meth public static java.util.Iterator getAllAttributes(org.w3c.dom.Element)
meth public static java.util.Iterator getAllChildren(org.w3c.dom.Element)
meth public static java.util.List<java.lang.String> parseTokenList(java.lang.String)
meth public static javax.xml.transform.Transformer newTransformer()
meth public static org.xml.sax.EntityResolver createDefaultCatalogResolver()
meth public static org.xml.sax.EntityResolver createEntityResolver(java.net.URL)
 anno 1 com.sun.istack.Nullable()
supr java.lang.Object
hfds LEXICAL_HANDLER_PROPERTY,saxParserFactory,transformerFactory

CLSS public abstract interface com.sun.xml.xsom.parser.XMLParser
meth public abstract void parse(org.xml.sax.InputSource,org.xml.sax.ContentHandler,org.xml.sax.ErrorHandler,org.xml.sax.EntityResolver) throws java.io.IOException,org.xml.sax.SAXException

CLSS public abstract java.awt.Component
cons protected init()
fld protected javax.accessibility.AccessibleContext accessibleContext
fld public final static float BOTTOM_ALIGNMENT = 1.0
fld public final static float CENTER_ALIGNMENT = 0.5
fld public final static float LEFT_ALIGNMENT = 0.0
fld public final static float RIGHT_ALIGNMENT = 1.0
fld public final static float TOP_ALIGNMENT = 0.0
innr protected BltBufferStrategy
innr protected FlipBufferStrategy
innr protected abstract AccessibleAWTComponent
innr public final static !enum BaselineResizeBehavior
intf java.awt.MenuContainer
intf java.awt.image.ImageObserver
intf java.io.Serializable
meth protected boolean requestFocus(boolean)
meth protected boolean requestFocusInWindow(boolean)
meth protected final void disableEvents(long)
meth protected final void enableEvents(long)
meth protected java.awt.AWTEvent coalesceEvents(java.awt.AWTEvent,java.awt.AWTEvent)
meth protected java.lang.String paramString()
meth protected void firePropertyChange(java.lang.String,boolean,boolean)
meth protected void firePropertyChange(java.lang.String,int,int)
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void processComponentEvent(java.awt.event.ComponentEvent)
meth protected void processEvent(java.awt.AWTEvent)
meth protected void processFocusEvent(java.awt.event.FocusEvent)
meth protected void processHierarchyBoundsEvent(java.awt.event.HierarchyEvent)
meth protected void processHierarchyEvent(java.awt.event.HierarchyEvent)
meth protected void processInputMethodEvent(java.awt.event.InputMethodEvent)
meth protected void processKeyEvent(java.awt.event.KeyEvent)
meth protected void processMouseEvent(java.awt.event.MouseEvent)
meth protected void processMouseMotionEvent(java.awt.event.MouseEvent)
meth protected void processMouseWheelEvent(java.awt.event.MouseWheelEvent)
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public boolean action(java.awt.Event,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public boolean areFocusTraversalKeysSet(int)
meth public boolean contains(int,int)
meth public boolean contains(java.awt.Point)
meth public boolean getFocusTraversalKeysEnabled()
meth public boolean getIgnoreRepaint()
meth public boolean gotFocus(java.awt.Event,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public boolean handleEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public boolean hasFocus()
meth public boolean imageUpdate(java.awt.Image,int,int,int,int,int)
meth public boolean inside(int,int)
 anno 0 java.lang.Deprecated()
meth public boolean isBackgroundSet()
meth public boolean isCursorSet()
meth public boolean isDisplayable()
meth public boolean isDoubleBuffered()
meth public boolean isEnabled()
meth public boolean isFocusCycleRoot(java.awt.Container)
meth public boolean isFocusOwner()
meth public boolean isFocusTraversable()
 anno 0 java.lang.Deprecated()
meth public boolean isFocusable()
meth public boolean isFontSet()
meth public boolean isForegroundSet()
meth public boolean isLightweight()
meth public boolean isMaximumSizeSet()
meth public boolean isMinimumSizeSet()
meth public boolean isOpaque()
meth public boolean isPreferredSizeSet()
meth public boolean isShowing()
meth public boolean isValid()
meth public boolean isVisible()
meth public boolean keyDown(java.awt.Event,int)
 anno 0 java.lang.Deprecated()
meth public boolean keyUp(java.awt.Event,int)
 anno 0 java.lang.Deprecated()
meth public boolean lostFocus(java.awt.Event,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public boolean mouseDown(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated()
meth public boolean mouseDrag(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated()
meth public boolean mouseEnter(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated()
meth public boolean mouseExit(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated()
meth public boolean mouseMove(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated()
meth public boolean mouseUp(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated()
meth public boolean postEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public boolean prepareImage(java.awt.Image,int,int,java.awt.image.ImageObserver)
meth public boolean prepareImage(java.awt.Image,java.awt.image.ImageObserver)
meth public boolean requestFocusInWindow()
meth public final java.lang.Object getTreeLock()
meth public final void dispatchEvent(java.awt.AWTEvent)
meth public float getAlignmentX()
meth public float getAlignmentY()
meth public int checkImage(java.awt.Image,int,int,java.awt.image.ImageObserver)
meth public int checkImage(java.awt.Image,java.awt.image.ImageObserver)
meth public int getBaseline(int,int)
meth public int getHeight()
meth public int getWidth()
meth public int getX()
meth public int getY()
meth public java.awt.Color getBackground()
meth public java.awt.Color getForeground()
meth public java.awt.Component getComponentAt(int,int)
meth public java.awt.Component getComponentAt(java.awt.Point)
meth public java.awt.Component locate(int,int)
 anno 0 java.lang.Deprecated()
meth public java.awt.Component$BaselineResizeBehavior getBaselineResizeBehavior()
meth public java.awt.ComponentOrientation getComponentOrientation()
meth public java.awt.Container getFocusCycleRootAncestor()
meth public java.awt.Container getParent()
meth public java.awt.Cursor getCursor()
meth public java.awt.Dimension getMaximumSize()
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredSize()
meth public java.awt.Dimension getSize()
meth public java.awt.Dimension getSize(java.awt.Dimension)
meth public java.awt.Dimension minimumSize()
 anno 0 java.lang.Deprecated()
meth public java.awt.Dimension preferredSize()
 anno 0 java.lang.Deprecated()
meth public java.awt.Dimension size()
 anno 0 java.lang.Deprecated()
meth public java.awt.Font getFont()
meth public java.awt.FontMetrics getFontMetrics(java.awt.Font)
meth public java.awt.Graphics getGraphics()
meth public java.awt.GraphicsConfiguration getGraphicsConfiguration()
meth public java.awt.Image createImage(int,int)
meth public java.awt.Image createImage(java.awt.image.ImageProducer)
meth public java.awt.Point getLocation()
meth public java.awt.Point getLocation(java.awt.Point)
meth public java.awt.Point getLocationOnScreen()
meth public java.awt.Point getMousePosition()
meth public java.awt.Point location()
 anno 0 java.lang.Deprecated()
meth public java.awt.Rectangle bounds()
 anno 0 java.lang.Deprecated()
meth public java.awt.Rectangle getBounds()
meth public java.awt.Rectangle getBounds(java.awt.Rectangle)
meth public java.awt.Toolkit getToolkit()
meth public java.awt.dnd.DropTarget getDropTarget()
meth public java.awt.event.ComponentListener[] getComponentListeners()
meth public java.awt.event.FocusListener[] getFocusListeners()
meth public java.awt.event.HierarchyBoundsListener[] getHierarchyBoundsListeners()
meth public java.awt.event.HierarchyListener[] getHierarchyListeners()
meth public java.awt.event.InputMethodListener[] getInputMethodListeners()
meth public java.awt.event.KeyListener[] getKeyListeners()
meth public java.awt.event.MouseListener[] getMouseListeners()
meth public java.awt.event.MouseMotionListener[] getMouseMotionListeners()
meth public java.awt.event.MouseWheelListener[] getMouseWheelListeners()
meth public java.awt.im.InputContext getInputContext()
meth public java.awt.im.InputMethodRequests getInputMethodRequests()
meth public java.awt.image.ColorModel getColorModel()
meth public java.awt.image.VolatileImage createVolatileImage(int,int)
meth public java.awt.image.VolatileImage createVolatileImage(int,int,java.awt.ImageCapabilities) throws java.awt.AWTException
meth public java.awt.peer.ComponentPeer getPeer()
 anno 0 java.lang.Deprecated()
meth public java.beans.PropertyChangeListener[] getPropertyChangeListeners()
meth public java.beans.PropertyChangeListener[] getPropertyChangeListeners(java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Locale getLocale()
meth public java.util.Set<java.awt.AWTKeyStroke> getFocusTraversalKeys(int)
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public void add(java.awt.PopupMenu)
meth public void addComponentListener(java.awt.event.ComponentListener)
meth public void addFocusListener(java.awt.event.FocusListener)
meth public void addHierarchyBoundsListener(java.awt.event.HierarchyBoundsListener)
meth public void addHierarchyListener(java.awt.event.HierarchyListener)
meth public void addInputMethodListener(java.awt.event.InputMethodListener)
meth public void addKeyListener(java.awt.event.KeyListener)
meth public void addMouseListener(java.awt.event.MouseListener)
meth public void addMouseMotionListener(java.awt.event.MouseMotionListener)
meth public void addMouseWheelListener(java.awt.event.MouseWheelListener)
meth public void addNotify()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void applyComponentOrientation(java.awt.ComponentOrientation)
meth public void deliverEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public void disable()
 anno 0 java.lang.Deprecated()
meth public void doLayout()
meth public void enable()
 anno 0 java.lang.Deprecated()
meth public void enable(boolean)
 anno 0 java.lang.Deprecated()
meth public void enableInputMethods(boolean)
meth public void firePropertyChange(java.lang.String,byte,byte)
meth public void firePropertyChange(java.lang.String,char,char)
meth public void firePropertyChange(java.lang.String,double,double)
meth public void firePropertyChange(java.lang.String,float,float)
meth public void firePropertyChange(java.lang.String,long,long)
meth public void firePropertyChange(java.lang.String,short,short)
meth public void hide()
 anno 0 java.lang.Deprecated()
meth public void invalidate()
meth public void layout()
 anno 0 java.lang.Deprecated()
meth public void list()
meth public void list(java.io.PrintStream)
meth public void list(java.io.PrintStream,int)
meth public void list(java.io.PrintWriter)
meth public void list(java.io.PrintWriter,int)
meth public void move(int,int)
 anno 0 java.lang.Deprecated()
meth public void nextFocus()
 anno 0 java.lang.Deprecated()
meth public void paint(java.awt.Graphics)
meth public void paintAll(java.awt.Graphics)
meth public void print(java.awt.Graphics)
meth public void printAll(java.awt.Graphics)
meth public void remove(java.awt.MenuComponent)
meth public void removeComponentListener(java.awt.event.ComponentListener)
meth public void removeFocusListener(java.awt.event.FocusListener)
meth public void removeHierarchyBoundsListener(java.awt.event.HierarchyBoundsListener)
meth public void removeHierarchyListener(java.awt.event.HierarchyListener)
meth public void removeInputMethodListener(java.awt.event.InputMethodListener)
meth public void removeKeyListener(java.awt.event.KeyListener)
meth public void removeMouseListener(java.awt.event.MouseListener)
meth public void removeMouseMotionListener(java.awt.event.MouseMotionListener)
meth public void removeMouseWheelListener(java.awt.event.MouseWheelListener)
meth public void removeNotify()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void repaint()
meth public void repaint(int,int,int,int)
meth public void repaint(long)
meth public void repaint(long,int,int,int,int)
meth public void requestFocus()
meth public void reshape(int,int,int,int)
 anno 0 java.lang.Deprecated()
meth public void resize(int,int)
 anno 0 java.lang.Deprecated()
meth public void resize(java.awt.Dimension)
 anno 0 java.lang.Deprecated()
meth public void revalidate()
meth public void setBackground(java.awt.Color)
meth public void setBounds(int,int,int,int)
meth public void setBounds(java.awt.Rectangle)
meth public void setComponentOrientation(java.awt.ComponentOrientation)
meth public void setCursor(java.awt.Cursor)
meth public void setDropTarget(java.awt.dnd.DropTarget)
meth public void setEnabled(boolean)
meth public void setFocusTraversalKeys(int,java.util.Set<? extends java.awt.AWTKeyStroke>)
meth public void setFocusTraversalKeysEnabled(boolean)
meth public void setFocusable(boolean)
meth public void setFont(java.awt.Font)
meth public void setForeground(java.awt.Color)
meth public void setIgnoreRepaint(boolean)
meth public void setLocale(java.util.Locale)
meth public void setLocation(int,int)
meth public void setLocation(java.awt.Point)
meth public void setMaximumSize(java.awt.Dimension)
meth public void setMinimumSize(java.awt.Dimension)
meth public void setName(java.lang.String)
meth public void setPreferredSize(java.awt.Dimension)
meth public void setSize(int,int)
meth public void setSize(java.awt.Dimension)
meth public void setVisible(boolean)
meth public void show()
 anno 0 java.lang.Deprecated()
meth public void show(boolean)
 anno 0 java.lang.Deprecated()
meth public void transferFocus()
meth public void transferFocusBackward()
meth public void transferFocusUpCycle()
meth public void update(java.awt.Graphics)
meth public void validate()
supr java.lang.Object
hfds FOCUS_TRAVERSABLE_DEFAULT,FOCUS_TRAVERSABLE_SET,FOCUS_TRAVERSABLE_UNKNOWN,LOCK,acc,actionListenerK,adjustmentListenerK,appContext,autoFocusTransferOnDisposal,background,backgroundEraseDisabled,boundsOp,bufferStrategy,changeSupport,coalesceEventsParams,coalesceMap,coalescingEnabled,componentListener,componentListenerK,componentOrientation,componentSerializedDataVersion,compoundShape,containerListenerK,cursor,dropTarget,enabled,eventCache,eventLog,eventMask,focusListener,focusListenerK,focusLog,focusTraversalKeyPropertyNames,focusTraversalKeys,focusTraversalKeysEnabled,focusable,font,foreground,graphicsConfig,height,hierarchyBoundsListener,hierarchyBoundsListenerK,hierarchyListener,hierarchyListenerK,ignoreRepaint,incRate,inputMethodListener,inputMethodListenerK,isAddNotifyComplete,isFocusTraversableOverridden,isInc,isPacked,itemListenerK,keyListener,keyListenerK,locale,log,maxSize,maxSizeSet,minSize,minSizeSet,mixingCutoutRegion,mixingLog,mouseListener,mouseListenerK,mouseMotionListener,mouseMotionListenerK,mouseWheelListener,mouseWheelListenerK,name,nameExplicitlySet,newEventsOnly,objectLock,ownedWindowK,parent,peer,peerFont,popups,prefSize,prefSizeSet,requestFocusController,serialVersionUID,textListenerK,valid,visible,width,windowClosingException,windowFocusListenerK,windowListenerK,windowStateListenerK,x,y
hcls AWTTreeLock,BltSubRegionBufferStrategy,DummyRequestFocusController,FlipSubRegionBufferStrategy,ProxyCapabilities,SingleBufferStrategy

CLSS public abstract interface java.awt.MenuContainer
meth public abstract boolean postEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public abstract java.awt.Font getFont()
meth public abstract void remove(java.awt.MenuComponent)

CLSS public abstract interface java.awt.datatransfer.Transferable
meth public abstract boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public abstract java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public abstract java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException

CLSS public abstract interface java.awt.image.ImageObserver
fld public final static int ABORT = 128
fld public final static int ALLBITS = 32
fld public final static int ERROR = 64
fld public final static int FRAMEBITS = 16
fld public final static int HEIGHT = 2
fld public final static int PROPERTIES = 4
fld public final static int SOMEBITS = 8
fld public final static int WIDTH = 1
meth public abstract boolean imageUpdate(java.awt.Image,int,int,int,int,int)

CLSS public java.io.BufferedWriter
cons public init(java.io.Writer)
cons public init(java.io.Writer,int)
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void newLine() throws java.io.IOException
meth public void write(char[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
meth public void write(java.lang.String,int,int) throws java.io.IOException
supr java.io.Writer
hfds cb,defaultCharBufferSize,lineSeparator,nChars,nextChar,out

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

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

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
hfds MAX_SKIP_BUFFER_SIZE

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

CLSS public abstract java.io.Writer
cons protected init()
cons protected init(java.lang.Object)
fld protected java.lang.Object lock
intf java.io.Closeable
intf java.io.Flushable
intf java.lang.Appendable
meth public abstract void close() throws java.io.IOException
meth public abstract void flush() throws java.io.IOException
meth public abstract void write(char[],int,int) throws java.io.IOException
meth public java.io.Writer append(char) throws java.io.IOException
meth public java.io.Writer append(java.lang.CharSequence) throws java.io.IOException
meth public java.io.Writer append(java.lang.CharSequence,int,int) throws java.io.IOException
meth public void write(char[]) throws java.io.IOException
meth public void write(int) throws java.io.IOException
meth public void write(java.lang.String) throws java.io.IOException
meth public void write(java.lang.String,int,int) throws java.io.IOException
supr java.lang.Object
hfds WRITE_BUFFER_SIZE,writeBuffer

CLSS public abstract interface java.lang.Appendable
meth public abstract java.lang.Appendable append(char) throws java.io.IOException
meth public abstract java.lang.Appendable append(java.lang.CharSequence) throws java.io.IOException
meth public abstract java.lang.Appendable append(java.lang.CharSequence,int,int) throws java.io.IOException

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract interface java.lang.Cloneable

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
hfds name,ordinal

CLSS public java.lang.Error
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

CLSS public java.lang.IllegalArgumentException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException
hfds serialVersionUID

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
hfds serialVersionUID

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
hfds CAUSE_CAPTION,EMPTY_THROWABLE_ARRAY,NULL_CAUSE_MESSAGE,SELF_SUPPRESSION_MESSAGE,SUPPRESSED_CAPTION,SUPPRESSED_SENTINEL,UNASSIGNED_STACK,backtrace,cause,detailMessage,serialVersionUID,stackTrace,suppressedExceptions
hcls PrintStreamOrWriter,SentinelHolder,WrappedPrintStream,WrappedPrintWriter

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

CLSS public abstract java.net.Authenticator
cons public init()
innr public final static !enum RequestorType
meth protected final int getRequestingPort()
meth protected final java.lang.String getRequestingHost()
meth protected final java.lang.String getRequestingPrompt()
meth protected final java.lang.String getRequestingProtocol()
meth protected final java.lang.String getRequestingScheme()
meth protected final java.net.InetAddress getRequestingSite()
meth protected java.net.Authenticator$RequestorType getRequestorType()
meth protected java.net.PasswordAuthentication getPasswordAuthentication()
meth protected java.net.URL getRequestingURL()
meth public static java.net.PasswordAuthentication requestPasswordAuthentication(java.lang.String,java.net.InetAddress,int,java.lang.String,java.lang.String,java.lang.String)
meth public static java.net.PasswordAuthentication requestPasswordAuthentication(java.lang.String,java.net.InetAddress,int,java.lang.String,java.lang.String,java.lang.String,java.net.URL,java.net.Authenticator$RequestorType)
meth public static java.net.PasswordAuthentication requestPasswordAuthentication(java.net.InetAddress,int,java.lang.String,java.lang.String,java.lang.String)
meth public static void setDefault(java.net.Authenticator)
supr java.lang.Object
hfds requestingAuthType,requestingHost,requestingPort,requestingPrompt,requestingProtocol,requestingScheme,requestingSite,requestingURL,theAuthenticator

CLSS public abstract java.util.AbstractCollection<%0 extends java.lang.Object>
cons protected init()
intf java.util.Collection<{java.util.AbstractCollection%0}>
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract int size()
meth public abstract java.util.Iterator<{java.util.AbstractCollection%0}> iterator()
meth public boolean add({java.util.AbstractCollection%0})
meth public boolean addAll(java.util.Collection<? extends {java.util.AbstractCollection%0}>)
meth public boolean contains(java.lang.Object)
meth public boolean containsAll(java.util.Collection<?>)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean retainAll(java.util.Collection<?>)
meth public java.lang.Object[] toArray()
meth public java.lang.String toString()
meth public void clear()
supr java.lang.Object
hfds MAX_ARRAY_SIZE

CLSS public abstract java.util.AbstractList<%0 extends java.lang.Object>
cons protected init()
fld protected int modCount
intf java.util.List<{java.util.AbstractList%0}>
meth protected void removeRange(int,int)
meth public abstract {java.util.AbstractList%0} get(int)
meth public boolean add({java.util.AbstractList%0})
meth public boolean addAll(int,java.util.Collection<? extends {java.util.AbstractList%0}>)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public int indexOf(java.lang.Object)
meth public int lastIndexOf(java.lang.Object)
meth public java.util.Iterator<{java.util.AbstractList%0}> iterator()
meth public java.util.List<{java.util.AbstractList%0}> subList(int,int)
meth public java.util.ListIterator<{java.util.AbstractList%0}> listIterator()
meth public java.util.ListIterator<{java.util.AbstractList%0}> listIterator(int)
meth public void add(int,{java.util.AbstractList%0})
meth public void clear()
meth public {java.util.AbstractList%0} remove(int)
meth public {java.util.AbstractList%0} set(int,{java.util.AbstractList%0})
supr java.util.AbstractCollection<{java.util.AbstractList%0}>
hcls Itr,ListItr

CLSS public abstract java.util.AbstractMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init()
innr public static SimpleEntry
innr public static SimpleImmutableEntry
intf java.util.Map<{java.util.AbstractMap%0},{java.util.AbstractMap%1}>
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.AbstractMap%0},{java.util.AbstractMap%1}>> entrySet()
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public int size()
meth public java.lang.String toString()
meth public java.util.Collection<{java.util.AbstractMap%1}> values()
meth public java.util.Set<{java.util.AbstractMap%0}> keySet()
meth public void clear()
meth public void putAll(java.util.Map<? extends {java.util.AbstractMap%0},? extends {java.util.AbstractMap%1}>)
meth public {java.util.AbstractMap%1} get(java.lang.Object)
meth public {java.util.AbstractMap%1} put({java.util.AbstractMap%0},{java.util.AbstractMap%1})
meth public {java.util.AbstractMap%1} remove(java.lang.Object)
supr java.lang.Object
hfds keySet,values

CLSS public java.util.ArrayList<%0 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(java.util.Collection<? extends {java.util.ArrayList%0}>)
intf java.io.Serializable
intf java.lang.Cloneable
intf java.util.List<{java.util.ArrayList%0}>
intf java.util.RandomAccess
meth protected void removeRange(int,int)
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public boolean add({java.util.ArrayList%0})
meth public boolean addAll(int,java.util.Collection<? extends {java.util.ArrayList%0}>)
meth public boolean addAll(java.util.Collection<? extends {java.util.ArrayList%0}>)
meth public boolean contains(java.lang.Object)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean removeIf(java.util.function.Predicate<? super {java.util.ArrayList%0}>)
meth public boolean retainAll(java.util.Collection<?>)
meth public int indexOf(java.lang.Object)
meth public int lastIndexOf(java.lang.Object)
meth public int size()
meth public java.lang.Object clone()
meth public java.lang.Object[] toArray()
meth public java.util.Iterator<{java.util.ArrayList%0}> iterator()
meth public java.util.List<{java.util.ArrayList%0}> subList(int,int)
meth public java.util.ListIterator<{java.util.ArrayList%0}> listIterator()
meth public java.util.ListIterator<{java.util.ArrayList%0}> listIterator(int)
meth public java.util.Spliterator<{java.util.ArrayList%0}> spliterator()
meth public void add(int,{java.util.ArrayList%0})
meth public void clear()
meth public void ensureCapacity(int)
meth public void forEach(java.util.function.Consumer<? super {java.util.ArrayList%0}>)
meth public void replaceAll(java.util.function.UnaryOperator<{java.util.ArrayList%0}>)
meth public void sort(java.util.Comparator<? super {java.util.ArrayList%0}>)
meth public void trimToSize()
meth public {java.util.ArrayList%0} get(int)
meth public {java.util.ArrayList%0} remove(int)
meth public {java.util.ArrayList%0} set(int,{java.util.ArrayList%0})
supr java.util.AbstractList<{java.util.ArrayList%0}>
hfds DEFAULTCAPACITY_EMPTY_ELEMENTDATA,DEFAULT_CAPACITY,EMPTY_ELEMENTDATA,MAX_ARRAY_SIZE,elementData,serialVersionUID,size
hcls ArrayListSpliterator,Itr,ListItr,SubList

CLSS public abstract interface java.util.Collection<%0 extends java.lang.Object>
intf java.lang.Iterable<{java.util.Collection%0}>
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.Collection%0})
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.Collection%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.Collection%0}> iterator()
meth public abstract void clear()
meth public boolean removeIf(java.util.function.Predicate<? super {java.util.Collection%0}>)
meth public java.util.Spliterator<{java.util.Collection%0}> spliterator()
meth public java.util.stream.Stream<{java.util.Collection%0}> parallelStream()
meth public java.util.stream.Stream<{java.util.Collection%0}> stream()

CLSS public java.util.HashMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(int,float)
cons public init(java.util.Map<? extends {java.util.HashMap%0},? extends {java.util.HashMap%1}>)
intf java.io.Serializable
intf java.lang.Cloneable
intf java.util.Map<{java.util.HashMap%0},{java.util.HashMap%1}>
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object,java.lang.Object)
meth public boolean replace({java.util.HashMap%0},{java.util.HashMap%1},{java.util.HashMap%1})
meth public int size()
meth public java.lang.Object clone()
meth public java.util.Collection<{java.util.HashMap%1}> values()
meth public java.util.Set<java.util.Map$Entry<{java.util.HashMap%0},{java.util.HashMap%1}>> entrySet()
meth public java.util.Set<{java.util.HashMap%0}> keySet()
meth public void clear()
meth public void forEach(java.util.function.BiConsumer<? super {java.util.HashMap%0},? super {java.util.HashMap%1}>)
meth public void putAll(java.util.Map<? extends {java.util.HashMap%0},? extends {java.util.HashMap%1}>)
meth public void replaceAll(java.util.function.BiFunction<? super {java.util.HashMap%0},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} compute({java.util.HashMap%0},java.util.function.BiFunction<? super {java.util.HashMap%0},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} computeIfAbsent({java.util.HashMap%0},java.util.function.Function<? super {java.util.HashMap%0},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} computeIfPresent({java.util.HashMap%0},java.util.function.BiFunction<? super {java.util.HashMap%0},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} get(java.lang.Object)
meth public {java.util.HashMap%1} getOrDefault(java.lang.Object,{java.util.HashMap%1})
meth public {java.util.HashMap%1} merge({java.util.HashMap%0},{java.util.HashMap%1},java.util.function.BiFunction<? super {java.util.HashMap%1},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} put({java.util.HashMap%0},{java.util.HashMap%1})
meth public {java.util.HashMap%1} putIfAbsent({java.util.HashMap%0},{java.util.HashMap%1})
meth public {java.util.HashMap%1} remove(java.lang.Object)
meth public {java.util.HashMap%1} replace({java.util.HashMap%0},{java.util.HashMap%1})
supr java.util.AbstractMap<{java.util.HashMap%0},{java.util.HashMap%1}>
hfds DEFAULT_INITIAL_CAPACITY,DEFAULT_LOAD_FACTOR,MAXIMUM_CAPACITY,MIN_TREEIFY_CAPACITY,TREEIFY_THRESHOLD,UNTREEIFY_THRESHOLD,entrySet,loadFactor,modCount,serialVersionUID,size,table,threshold
hcls EntryIterator,EntrySet,EntrySpliterator,HashIterator,HashMapSpliterator,KeyIterator,KeySet,KeySpliterator,Node,TreeNode,ValueIterator,ValueSpliterator,Values

CLSS public abstract interface java.util.Iterator<%0 extends java.lang.Object>
meth public abstract boolean hasNext()
meth public abstract {java.util.Iterator%0} next()
meth public void forEachRemaining(java.util.function.Consumer<? super {java.util.Iterator%0}>)
meth public void remove()

CLSS public abstract interface java.util.List<%0 extends java.lang.Object>
intf java.util.Collection<{java.util.List%0}>
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.List%0})
meth public abstract boolean addAll(int,java.util.Collection<? extends {java.util.List%0}>)
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.List%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int indexOf(java.lang.Object)
meth public abstract int lastIndexOf(java.lang.Object)
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.List%0}> iterator()
meth public abstract java.util.List<{java.util.List%0}> subList(int,int)
meth public abstract java.util.ListIterator<{java.util.List%0}> listIterator()
meth public abstract java.util.ListIterator<{java.util.List%0}> listIterator(int)
meth public abstract void add(int,{java.util.List%0})
meth public abstract void clear()
meth public abstract {java.util.List%0} get(int)
meth public abstract {java.util.List%0} remove(int)
meth public abstract {java.util.List%0} set(int,{java.util.List%0})
meth public java.util.Spliterator<{java.util.List%0}> spliterator()
meth public void replaceAll(java.util.function.UnaryOperator<{java.util.List%0}>)
meth public void sort(java.util.Comparator<? super {java.util.List%0}>)

CLSS public abstract interface java.util.Map<%0 extends java.lang.Object, %1 extends java.lang.Object>
innr public abstract interface static Entry
meth public abstract boolean containsKey(java.lang.Object)
meth public abstract boolean containsValue(java.lang.Object)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.util.Collection<{java.util.Map%1}> values()
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.Map%0},{java.util.Map%1}>> entrySet()
meth public abstract java.util.Set<{java.util.Map%0}> keySet()
meth public abstract void clear()
meth public abstract void putAll(java.util.Map<? extends {java.util.Map%0},? extends {java.util.Map%1}>)
meth public abstract {java.util.Map%1} get(java.lang.Object)
meth public abstract {java.util.Map%1} put({java.util.Map%0},{java.util.Map%1})
meth public abstract {java.util.Map%1} remove(java.lang.Object)
meth public boolean remove(java.lang.Object,java.lang.Object)
meth public boolean replace({java.util.Map%0},{java.util.Map%1},{java.util.Map%1})
meth public void forEach(java.util.function.BiConsumer<? super {java.util.Map%0},? super {java.util.Map%1}>)
meth public void replaceAll(java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} compute({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfAbsent({java.util.Map%0},java.util.function.Function<? super {java.util.Map%0},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfPresent({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} getOrDefault(java.lang.Object,{java.util.Map%1})
meth public {java.util.Map%1} merge({java.util.Map%0},{java.util.Map%1},java.util.function.BiFunction<? super {java.util.Map%1},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} putIfAbsent({java.util.Map%0},{java.util.Map%1})
meth public {java.util.Map%1} replace({java.util.Map%0},{java.util.Map%1})

CLSS public abstract interface java.util.RandomAccess

CLSS public abstract interface java.util.concurrent.Future<%0 extends java.lang.Object>
meth public abstract boolean cancel(boolean)
meth public abstract boolean isCancelled()
meth public abstract boolean isDone()
meth public abstract {java.util.concurrent.Future%0} get() throws java.lang.InterruptedException,java.util.concurrent.ExecutionException
meth public abstract {java.util.concurrent.Future%0} get(long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException,java.util.concurrent.TimeoutException

CLSS public abstract interface javax.activation.DataContentHandler
meth public abstract java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public abstract java.lang.Object getContent(javax.activation.DataSource) throws java.io.IOException
meth public abstract java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor,javax.activation.DataSource) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public abstract void writeTo(java.lang.Object,java.lang.String,java.io.OutputStream) throws java.io.IOException

CLSS public javax.activation.DataHandler
cons public init(java.lang.Object,java.lang.String)
cons public init(java.net.URL)
cons public init(javax.activation.DataSource)
intf java.awt.datatransfer.Transferable
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.io.InputStream getInputStream() throws java.io.IOException
meth public java.io.OutputStream getOutputStream() throws java.io.IOException
meth public java.lang.Object getBean(javax.activation.CommandInfo)
meth public java.lang.Object getContent() throws java.io.IOException
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public java.lang.String getContentType()
meth public java.lang.String getName()
meth public javax.activation.CommandInfo getCommand(java.lang.String)
meth public javax.activation.CommandInfo[] getAllCommands()
meth public javax.activation.CommandInfo[] getPreferredCommands()
meth public javax.activation.DataSource getDataSource()
meth public static void setDataContentHandlerFactory(javax.activation.DataContentHandlerFactory)
meth public void setCommandMap(javax.activation.CommandMap)
meth public void writeTo(java.io.OutputStream) throws java.io.IOException
supr java.lang.Object
hfds class$javax$activation$DataHandler,currentCommandMap,dataContentHandler,dataSource,emptyFlavors,factory,factoryDCH,objDataSource,object,objectMimeType,oldFactory,shortType,transferFlavors

CLSS public abstract interface javax.activation.DataSource
meth public abstract java.io.InputStream getInputStream() throws java.io.IOException
meth public abstract java.io.OutputStream getOutputStream() throws java.io.IOException
meth public abstract java.lang.String getContentType()
meth public abstract java.lang.String getName()

CLSS public abstract javax.annotation.processing.AbstractProcessor
cons protected init()
fld protected javax.annotation.processing.ProcessingEnvironment processingEnv
intf javax.annotation.processing.Processor
meth protected boolean isInitialized()
meth public abstract boolean process(java.util.Set<? extends javax.lang.model.element.TypeElement>,javax.annotation.processing.RoundEnvironment)
meth public java.lang.Iterable<? extends javax.annotation.processing.Completion> getCompletions(javax.lang.model.element.Element,javax.lang.model.element.AnnotationMirror,javax.lang.model.element.ExecutableElement,java.lang.String)
meth public java.util.Set<java.lang.String> getSupportedAnnotationTypes()
meth public java.util.Set<java.lang.String> getSupportedOptions()
meth public javax.lang.model.SourceVersion getSupportedSourceVersion()
meth public void init(javax.annotation.processing.ProcessingEnvironment)
supr java.lang.Object
hfds initialized

CLSS public abstract interface javax.annotation.processing.Processor
meth public abstract boolean process(java.util.Set<? extends javax.lang.model.element.TypeElement>,javax.annotation.processing.RoundEnvironment)
meth public abstract java.lang.Iterable<? extends javax.annotation.processing.Completion> getCompletions(javax.lang.model.element.Element,javax.lang.model.element.AnnotationMirror,javax.lang.model.element.ExecutableElement,java.lang.String)
meth public abstract java.util.Set<java.lang.String> getSupportedAnnotationTypes()
meth public abstract java.util.Set<java.lang.String> getSupportedOptions()
meth public abstract javax.lang.model.SourceVersion getSupportedSourceVersion()
meth public abstract void init(javax.annotation.processing.ProcessingEnvironment)

CLSS public abstract interface !annotation javax.annotation.processing.SupportedAnnotationTypes
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String[] value()

CLSS public abstract interface !annotation javax.annotation.processing.SupportedOptions
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String[] value()

CLSS public abstract interface !annotation javax.annotation.processing.SupportedSourceVersion
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract javax.lang.model.SourceVersion value()

CLSS public abstract interface !annotation javax.jws.soap.SOAPBinding
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
innr public final static !enum ParameterStyle
innr public final static !enum Style
innr public final static !enum Use
intf java.lang.annotation.Annotation
meth public abstract !hasdefault javax.jws.soap.SOAPBinding$ParameterStyle parameterStyle()
meth public abstract !hasdefault javax.jws.soap.SOAPBinding$Style style()
meth public abstract !hasdefault javax.jws.soap.SOAPBinding$Use use()

CLSS public abstract interface javax.lang.model.element.ElementVisitor<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract {javax.lang.model.element.ElementVisitor%0} visit(javax.lang.model.element.Element)
meth public abstract {javax.lang.model.element.ElementVisitor%0} visit(javax.lang.model.element.Element,{javax.lang.model.element.ElementVisitor%1})
meth public abstract {javax.lang.model.element.ElementVisitor%0} visitExecutable(javax.lang.model.element.ExecutableElement,{javax.lang.model.element.ElementVisitor%1})
meth public abstract {javax.lang.model.element.ElementVisitor%0} visitPackage(javax.lang.model.element.PackageElement,{javax.lang.model.element.ElementVisitor%1})
meth public abstract {javax.lang.model.element.ElementVisitor%0} visitType(javax.lang.model.element.TypeElement,{javax.lang.model.element.ElementVisitor%1})
meth public abstract {javax.lang.model.element.ElementVisitor%0} visitTypeParameter(javax.lang.model.element.TypeParameterElement,{javax.lang.model.element.ElementVisitor%1})
meth public abstract {javax.lang.model.element.ElementVisitor%0} visitUnknown(javax.lang.model.element.Element,{javax.lang.model.element.ElementVisitor%1})
meth public abstract {javax.lang.model.element.ElementVisitor%0} visitVariable(javax.lang.model.element.VariableElement,{javax.lang.model.element.ElementVisitor%1})

CLSS public abstract interface javax.lang.model.type.TypeVisitor<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract {javax.lang.model.type.TypeVisitor%0} visit(javax.lang.model.type.TypeMirror)
meth public abstract {javax.lang.model.type.TypeVisitor%0} visit(javax.lang.model.type.TypeMirror,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitArray(javax.lang.model.type.ArrayType,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitDeclared(javax.lang.model.type.DeclaredType,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitError(javax.lang.model.type.ErrorType,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitExecutable(javax.lang.model.type.ExecutableType,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitIntersection(javax.lang.model.type.IntersectionType,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitNoType(javax.lang.model.type.NoType,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitNull(javax.lang.model.type.NullType,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitPrimitive(javax.lang.model.type.PrimitiveType,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitTypeVariable(javax.lang.model.type.TypeVariable,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitUnion(javax.lang.model.type.UnionType,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitUnknown(javax.lang.model.type.TypeMirror,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitWildcard(javax.lang.model.type.WildcardType,{javax.lang.model.type.TypeVisitor%1})

CLSS public abstract javax.lang.model.util.AbstractElementVisitor6<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_6)
cons protected init()
intf javax.lang.model.element.ElementVisitor<{javax.lang.model.util.AbstractElementVisitor6%0},{javax.lang.model.util.AbstractElementVisitor6%1}>
meth public final {javax.lang.model.util.AbstractElementVisitor6%0} visit(javax.lang.model.element.Element)
meth public final {javax.lang.model.util.AbstractElementVisitor6%0} visit(javax.lang.model.element.Element,{javax.lang.model.util.AbstractElementVisitor6%1})
meth public {javax.lang.model.util.AbstractElementVisitor6%0} visitUnknown(javax.lang.model.element.Element,{javax.lang.model.util.AbstractElementVisitor6%1})
supr java.lang.Object

CLSS public abstract javax.lang.model.util.AbstractTypeVisitor6<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init()
intf javax.lang.model.type.TypeVisitor<{javax.lang.model.util.AbstractTypeVisitor6%0},{javax.lang.model.util.AbstractTypeVisitor6%1}>
meth public final {javax.lang.model.util.AbstractTypeVisitor6%0} visit(javax.lang.model.type.TypeMirror)
meth public final {javax.lang.model.util.AbstractTypeVisitor6%0} visit(javax.lang.model.type.TypeMirror,{javax.lang.model.util.AbstractTypeVisitor6%1})
meth public {javax.lang.model.util.AbstractTypeVisitor6%0} visitIntersection(javax.lang.model.type.IntersectionType,{javax.lang.model.util.AbstractTypeVisitor6%1})
meth public {javax.lang.model.util.AbstractTypeVisitor6%0} visitUnion(javax.lang.model.type.UnionType,{javax.lang.model.util.AbstractTypeVisitor6%1})
meth public {javax.lang.model.util.AbstractTypeVisitor6%0} visitUnknown(javax.lang.model.type.TypeMirror,{javax.lang.model.util.AbstractTypeVisitor6%1})
supr java.lang.Object

CLSS public javax.lang.model.util.SimpleElementVisitor6<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_6)
cons protected init()
cons protected init({javax.lang.model.util.SimpleElementVisitor6%0})
fld protected final {javax.lang.model.util.SimpleElementVisitor6%0} DEFAULT_VALUE
meth protected {javax.lang.model.util.SimpleElementVisitor6%0} defaultAction(javax.lang.model.element.Element,{javax.lang.model.util.SimpleElementVisitor6%1})
meth public {javax.lang.model.util.SimpleElementVisitor6%0} visitExecutable(javax.lang.model.element.ExecutableElement,{javax.lang.model.util.SimpleElementVisitor6%1})
meth public {javax.lang.model.util.SimpleElementVisitor6%0} visitPackage(javax.lang.model.element.PackageElement,{javax.lang.model.util.SimpleElementVisitor6%1})
meth public {javax.lang.model.util.SimpleElementVisitor6%0} visitType(javax.lang.model.element.TypeElement,{javax.lang.model.util.SimpleElementVisitor6%1})
meth public {javax.lang.model.util.SimpleElementVisitor6%0} visitTypeParameter(javax.lang.model.element.TypeParameterElement,{javax.lang.model.util.SimpleElementVisitor6%1})
meth public {javax.lang.model.util.SimpleElementVisitor6%0} visitVariable(javax.lang.model.element.VariableElement,{javax.lang.model.util.SimpleElementVisitor6%1})
supr javax.lang.model.util.AbstractElementVisitor6<{javax.lang.model.util.SimpleElementVisitor6%0},{javax.lang.model.util.SimpleElementVisitor6%1}>

CLSS public javax.lang.model.util.SimpleTypeVisitor6<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_6)
cons protected init()
cons protected init({javax.lang.model.util.SimpleTypeVisitor6%0})
fld protected final {javax.lang.model.util.SimpleTypeVisitor6%0} DEFAULT_VALUE
meth protected {javax.lang.model.util.SimpleTypeVisitor6%0} defaultAction(javax.lang.model.type.TypeMirror,{javax.lang.model.util.SimpleTypeVisitor6%1})
meth public {javax.lang.model.util.SimpleTypeVisitor6%0} visitArray(javax.lang.model.type.ArrayType,{javax.lang.model.util.SimpleTypeVisitor6%1})
meth public {javax.lang.model.util.SimpleTypeVisitor6%0} visitDeclared(javax.lang.model.type.DeclaredType,{javax.lang.model.util.SimpleTypeVisitor6%1})
meth public {javax.lang.model.util.SimpleTypeVisitor6%0} visitError(javax.lang.model.type.ErrorType,{javax.lang.model.util.SimpleTypeVisitor6%1})
meth public {javax.lang.model.util.SimpleTypeVisitor6%0} visitExecutable(javax.lang.model.type.ExecutableType,{javax.lang.model.util.SimpleTypeVisitor6%1})
meth public {javax.lang.model.util.SimpleTypeVisitor6%0} visitNoType(javax.lang.model.type.NoType,{javax.lang.model.util.SimpleTypeVisitor6%1})
meth public {javax.lang.model.util.SimpleTypeVisitor6%0} visitNull(javax.lang.model.type.NullType,{javax.lang.model.util.SimpleTypeVisitor6%1})
meth public {javax.lang.model.util.SimpleTypeVisitor6%0} visitPrimitive(javax.lang.model.type.PrimitiveType,{javax.lang.model.util.SimpleTypeVisitor6%1})
meth public {javax.lang.model.util.SimpleTypeVisitor6%0} visitTypeVariable(javax.lang.model.type.TypeVariable,{javax.lang.model.util.SimpleTypeVisitor6%1})
meth public {javax.lang.model.util.SimpleTypeVisitor6%0} visitWildcard(javax.lang.model.type.WildcardType,{javax.lang.model.util.SimpleTypeVisitor6%1})
supr javax.lang.model.util.AbstractTypeVisitor6<{javax.lang.model.util.SimpleTypeVisitor6%0},{javax.lang.model.util.SimpleTypeVisitor6%1}>

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

CLSS public abstract interface !annotation javax.xml.bind.annotation.XmlTransient
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD, METHOD, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract interface javax.xml.namespace.NamespaceContext
meth public abstract java.lang.String getNamespaceURI(java.lang.String)
meth public abstract java.lang.String getPrefix(java.lang.String)
meth public abstract java.util.Iterator getPrefixes(java.lang.String)

CLSS public abstract javax.xml.soap.AttachmentPart
cons public init()
meth public abstract byte[] getRawContentBytes() throws javax.xml.soap.SOAPException
meth public abstract int getSize() throws javax.xml.soap.SOAPException
meth public abstract java.io.InputStream getBase64Content() throws javax.xml.soap.SOAPException
meth public abstract java.io.InputStream getRawContent() throws javax.xml.soap.SOAPException
meth public abstract java.lang.Object getContent() throws javax.xml.soap.SOAPException
meth public abstract java.lang.String[] getMimeHeader(java.lang.String)
meth public abstract java.util.Iterator getAllMimeHeaders()
meth public abstract java.util.Iterator getMatchingMimeHeaders(java.lang.String[])
meth public abstract java.util.Iterator getNonMatchingMimeHeaders(java.lang.String[])
meth public abstract javax.activation.DataHandler getDataHandler() throws javax.xml.soap.SOAPException
meth public abstract void addMimeHeader(java.lang.String,java.lang.String)
meth public abstract void clearContent()
meth public abstract void removeAllMimeHeaders()
meth public abstract void removeMimeHeader(java.lang.String)
meth public abstract void setBase64Content(java.io.InputStream,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract void setContent(java.lang.Object,java.lang.String)
meth public abstract void setDataHandler(javax.activation.DataHandler)
meth public abstract void setMimeHeader(java.lang.String,java.lang.String)
meth public abstract void setRawContent(java.io.InputStream,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract void setRawContentBytes(byte[],int,int,java.lang.String) throws javax.xml.soap.SOAPException
meth public java.lang.String getContentId()
meth public java.lang.String getContentLocation()
meth public java.lang.String getContentType()
meth public void setContentId(java.lang.String)
meth public void setContentLocation(java.lang.String)
meth public void setContentType(java.lang.String)
supr java.lang.Object

CLSS public abstract javax.xml.soap.MessageFactory
cons public init()
meth public abstract javax.xml.soap.SOAPMessage createMessage() throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPMessage createMessage(javax.xml.soap.MimeHeaders,java.io.InputStream) throws java.io.IOException,javax.xml.soap.SOAPException
meth public static javax.xml.soap.MessageFactory newInstance() throws javax.xml.soap.SOAPException
meth public static javax.xml.soap.MessageFactory newInstance(java.lang.String) throws javax.xml.soap.SOAPException
supr java.lang.Object
hfds DEFAULT_MESSAGE_FACTORY,MESSAGE_FACTORY_PROPERTY

CLSS public abstract interface javax.xml.soap.Node
intf org.w3c.dom.Node
meth public abstract java.lang.String getValue()
meth public abstract javax.xml.soap.SOAPElement getParentElement()
meth public abstract void detachNode()
meth public abstract void recycleNode()
meth public abstract void setParentElement(javax.xml.soap.SOAPElement) throws javax.xml.soap.SOAPException
meth public abstract void setValue(java.lang.String)

CLSS public abstract javax.xml.soap.SAAJMetaFactory
cons protected init()
meth protected abstract javax.xml.soap.MessageFactory newMessageFactory(java.lang.String) throws javax.xml.soap.SOAPException
meth protected abstract javax.xml.soap.SOAPFactory newSOAPFactory(java.lang.String) throws javax.xml.soap.SOAPException
supr java.lang.Object
hfds DEFAULT_META_FACTORY_CLASS,META_FACTORY_CLASS_PROPERTY

CLSS public abstract interface javax.xml.soap.SOAPConstants
fld public final static java.lang.String DEFAULT_SOAP_PROTOCOL = "SOAP 1.1 Protocol"
fld public final static java.lang.String DYNAMIC_SOAP_PROTOCOL = "Dynamic Protocol"
fld public final static java.lang.String SOAP_1_1_CONTENT_TYPE = "text/xml"
fld public final static java.lang.String SOAP_1_1_PROTOCOL = "SOAP 1.1 Protocol"
fld public final static java.lang.String SOAP_1_2_CONTENT_TYPE = "application/soap+xml"
fld public final static java.lang.String SOAP_1_2_PROTOCOL = "SOAP 1.2 Protocol"
fld public final static java.lang.String SOAP_ENV_PREFIX = "env"
fld public final static java.lang.String URI_NS_SOAP_1_1_ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope/"
fld public final static java.lang.String URI_NS_SOAP_1_2_ENCODING = "http://www.w3.org/2003/05/soap-encoding"
fld public final static java.lang.String URI_NS_SOAP_1_2_ENVELOPE = "http://www.w3.org/2003/05/soap-envelope"
fld public final static java.lang.String URI_NS_SOAP_ENCODING = "http://schemas.xmlsoap.org/soap/encoding/"
fld public final static java.lang.String URI_NS_SOAP_ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope/"
fld public final static java.lang.String URI_SOAP_1_2_ROLE_NEXT = "http://www.w3.org/2003/05/soap-envelope/role/next"
fld public final static java.lang.String URI_SOAP_1_2_ROLE_NONE = "http://www.w3.org/2003/05/soap-envelope/role/none"
fld public final static java.lang.String URI_SOAP_1_2_ROLE_ULTIMATE_RECEIVER = "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver"
fld public final static java.lang.String URI_SOAP_ACTOR_NEXT = "http://schemas.xmlsoap.org/soap/actor/next"
fld public final static javax.xml.namespace.QName SOAP_DATAENCODINGUNKNOWN_FAULT
fld public final static javax.xml.namespace.QName SOAP_MUSTUNDERSTAND_FAULT
fld public final static javax.xml.namespace.QName SOAP_RECEIVER_FAULT
fld public final static javax.xml.namespace.QName SOAP_SENDER_FAULT
fld public final static javax.xml.namespace.QName SOAP_VERSIONMISMATCH_FAULT

CLSS public abstract interface javax.xml.soap.SOAPElement
intf javax.xml.soap.Node
intf org.w3c.dom.Element
meth public abstract boolean removeAttribute(javax.xml.namespace.QName)
meth public abstract boolean removeAttribute(javax.xml.soap.Name)
meth public abstract boolean removeNamespaceDeclaration(java.lang.String)
meth public abstract java.lang.String getAttributeValue(javax.xml.namespace.QName)
meth public abstract java.lang.String getAttributeValue(javax.xml.soap.Name)
meth public abstract java.lang.String getEncodingStyle()
meth public abstract java.lang.String getNamespaceURI(java.lang.String)
meth public abstract java.util.Iterator getAllAttributes()
meth public abstract java.util.Iterator getAllAttributesAsQNames()
meth public abstract java.util.Iterator getChildElements()
meth public abstract java.util.Iterator getChildElements(javax.xml.namespace.QName)
meth public abstract java.util.Iterator getChildElements(javax.xml.soap.Name)
meth public abstract java.util.Iterator getNamespacePrefixes()
meth public abstract java.util.Iterator getVisibleNamespacePrefixes()
meth public abstract javax.xml.namespace.QName createQName(java.lang.String,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.namespace.QName getElementQName()
meth public abstract javax.xml.soap.Name getElementName()
meth public abstract javax.xml.soap.SOAPElement addAttribute(javax.xml.namespace.QName,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement addAttribute(javax.xml.soap.Name,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement addChildElement(java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement addChildElement(java.lang.String,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement addChildElement(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement addChildElement(javax.xml.namespace.QName) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement addChildElement(javax.xml.soap.Name) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement addChildElement(javax.xml.soap.SOAPElement) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement addNamespaceDeclaration(java.lang.String,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement addTextNode(java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement setElementQName(javax.xml.namespace.QName) throws javax.xml.soap.SOAPException
meth public abstract void removeContents()
meth public abstract void setEncodingStyle(java.lang.String) throws javax.xml.soap.SOAPException

CLSS public abstract interface javax.xml.soap.SOAPEnvelope
intf javax.xml.soap.SOAPElement
meth public abstract javax.xml.soap.Name createName(java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.Name createName(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPBody addBody() throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPBody getBody() throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPHeader addHeader() throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPHeader getHeader() throws javax.xml.soap.SOAPException

CLSS public javax.xml.soap.SOAPException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
meth public java.lang.String getMessage()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
supr java.lang.Exception
hfds cause

CLSS public abstract javax.xml.soap.SOAPFactory
cons public init()
meth public abstract javax.xml.soap.Detail createDetail() throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.Name createName(java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.Name createName(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement createElement(java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement createElement(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement createElement(javax.xml.soap.Name) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPFault createFault() throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPFault createFault(java.lang.String,javax.xml.namespace.QName) throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPElement createElement(javax.xml.namespace.QName) throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPElement createElement(org.w3c.dom.Element) throws javax.xml.soap.SOAPException
meth public static javax.xml.soap.SOAPFactory newInstance() throws javax.xml.soap.SOAPException
meth public static javax.xml.soap.SOAPFactory newInstance(java.lang.String) throws javax.xml.soap.SOAPException
supr java.lang.Object
hfds SOAP_FACTORY_PROPERTY

CLSS public abstract javax.xml.soap.SOAPMessage
cons public init()
fld public final static java.lang.String CHARACTER_SET_ENCODING = "javax.xml.soap.character-set-encoding"
fld public final static java.lang.String WRITE_XML_DECLARATION = "javax.xml.soap.write-xml-declaration"
meth public abstract boolean saveRequired()
meth public abstract int countAttachments()
meth public abstract java.lang.String getContentDescription()
meth public abstract java.util.Iterator getAttachments()
meth public abstract java.util.Iterator getAttachments(javax.xml.soap.MimeHeaders)
meth public abstract javax.xml.soap.AttachmentPart createAttachmentPart()
meth public abstract javax.xml.soap.AttachmentPart getAttachment(javax.xml.soap.SOAPElement) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.MimeHeaders getMimeHeaders()
meth public abstract javax.xml.soap.SOAPPart getSOAPPart()
meth public abstract void addAttachmentPart(javax.xml.soap.AttachmentPart)
meth public abstract void removeAllAttachments()
meth public abstract void removeAttachments(javax.xml.soap.MimeHeaders)
meth public abstract void saveChanges() throws javax.xml.soap.SOAPException
meth public abstract void setContentDescription(java.lang.String)
meth public abstract void writeTo(java.io.OutputStream) throws java.io.IOException,javax.xml.soap.SOAPException
meth public java.lang.Object getProperty(java.lang.String) throws javax.xml.soap.SOAPException
meth public javax.xml.soap.AttachmentPart createAttachmentPart(java.lang.Object,java.lang.String)
meth public javax.xml.soap.AttachmentPart createAttachmentPart(javax.activation.DataHandler)
meth public javax.xml.soap.SOAPBody getSOAPBody() throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPHeader getSOAPHeader() throws javax.xml.soap.SOAPException
meth public void setProperty(java.lang.String,java.lang.Object) throws javax.xml.soap.SOAPException
supr java.lang.Object

CLSS public abstract javax.xml.soap.SOAPPart
cons public init()
intf javax.xml.soap.Node
intf org.w3c.dom.Document
meth public abstract java.lang.String[] getMimeHeader(java.lang.String)
meth public abstract java.util.Iterator getAllMimeHeaders()
meth public abstract java.util.Iterator getMatchingMimeHeaders(java.lang.String[])
meth public abstract java.util.Iterator getNonMatchingMimeHeaders(java.lang.String[])
meth public abstract javax.xml.soap.SOAPEnvelope getEnvelope() throws javax.xml.soap.SOAPException
meth public abstract javax.xml.transform.Source getContent() throws javax.xml.soap.SOAPException
meth public abstract void addMimeHeader(java.lang.String,java.lang.String)
meth public abstract void removeAllMimeHeaders()
meth public abstract void removeMimeHeader(java.lang.String)
meth public abstract void setContent(javax.xml.transform.Source) throws javax.xml.soap.SOAPException
meth public abstract void setMimeHeader(java.lang.String,java.lang.String)
meth public java.lang.String getContentId()
meth public java.lang.String getContentLocation()
meth public void setContentId(java.lang.String)
meth public void setContentLocation(java.lang.String)
supr java.lang.Object

CLSS public abstract interface javax.xml.stream.Location
meth public abstract int getCharacterOffset()
meth public abstract int getColumnNumber()
meth public abstract int getLineNumber()
meth public abstract java.lang.String getPublicId()
meth public abstract java.lang.String getSystemId()

CLSS public abstract interface javax.xml.stream.XMLStreamConstants
fld public final static int ATTRIBUTE = 10
fld public final static int CDATA = 12
fld public final static int CHARACTERS = 4
fld public final static int COMMENT = 5
fld public final static int DTD = 11
fld public final static int END_DOCUMENT = 8
fld public final static int END_ELEMENT = 2
fld public final static int ENTITY_DECLARATION = 15
fld public final static int ENTITY_REFERENCE = 9
fld public final static int NAMESPACE = 13
fld public final static int NOTATION_DECLARATION = 14
fld public final static int PROCESSING_INSTRUCTION = 3
fld public final static int SPACE = 6
fld public final static int START_DOCUMENT = 7
fld public final static int START_ELEMENT = 1

CLSS public abstract interface javax.xml.stream.XMLStreamReader
intf javax.xml.stream.XMLStreamConstants
meth public abstract boolean hasName()
meth public abstract boolean hasNext() throws javax.xml.stream.XMLStreamException
meth public abstract boolean hasText()
meth public abstract boolean isAttributeSpecified(int)
meth public abstract boolean isCharacters()
meth public abstract boolean isEndElement()
meth public abstract boolean isStandalone()
meth public abstract boolean isStartElement()
meth public abstract boolean isWhiteSpace()
meth public abstract boolean standaloneSet()
meth public abstract char[] getTextCharacters()
meth public abstract int getAttributeCount()
meth public abstract int getEventType()
meth public abstract int getNamespaceCount()
meth public abstract int getTextCharacters(int,char[],int,int) throws javax.xml.stream.XMLStreamException
meth public abstract int getTextLength()
meth public abstract int getTextStart()
meth public abstract int next() throws javax.xml.stream.XMLStreamException
meth public abstract int nextTag() throws javax.xml.stream.XMLStreamException
meth public abstract java.lang.Object getProperty(java.lang.String)
meth public abstract java.lang.String getAttributeLocalName(int)
meth public abstract java.lang.String getAttributeNamespace(int)
meth public abstract java.lang.String getAttributePrefix(int)
meth public abstract java.lang.String getAttributeType(int)
meth public abstract java.lang.String getAttributeValue(int)
meth public abstract java.lang.String getAttributeValue(java.lang.String,java.lang.String)
meth public abstract java.lang.String getCharacterEncodingScheme()
meth public abstract java.lang.String getElementText() throws javax.xml.stream.XMLStreamException
meth public abstract java.lang.String getEncoding()
meth public abstract java.lang.String getLocalName()
meth public abstract java.lang.String getNamespacePrefix(int)
meth public abstract java.lang.String getNamespaceURI()
meth public abstract java.lang.String getNamespaceURI(int)
meth public abstract java.lang.String getNamespaceURI(java.lang.String)
meth public abstract java.lang.String getPIData()
meth public abstract java.lang.String getPITarget()
meth public abstract java.lang.String getPrefix()
meth public abstract java.lang.String getText()
meth public abstract java.lang.String getVersion()
meth public abstract javax.xml.namespace.NamespaceContext getNamespaceContext()
meth public abstract javax.xml.namespace.QName getAttributeName(int)
meth public abstract javax.xml.namespace.QName getName()
meth public abstract javax.xml.stream.Location getLocation()
meth public abstract void close() throws javax.xml.stream.XMLStreamException
meth public abstract void require(int,java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException

CLSS public abstract interface javax.xml.stream.XMLStreamWriter
meth public abstract java.lang.Object getProperty(java.lang.String)
meth public abstract java.lang.String getPrefix(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract javax.xml.namespace.NamespaceContext getNamespaceContext()
meth public abstract void close() throws javax.xml.stream.XMLStreamException
meth public abstract void flush() throws javax.xml.stream.XMLStreamException
meth public abstract void setDefaultNamespace(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void setNamespaceContext(javax.xml.namespace.NamespaceContext) throws javax.xml.stream.XMLStreamException
meth public abstract void setPrefix(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeAttribute(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeAttribute(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeAttribute(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeCData(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeCharacters(char[],int,int) throws javax.xml.stream.XMLStreamException
meth public abstract void writeCharacters(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeComment(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeDTD(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeDefaultNamespace(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeEmptyElement(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeEmptyElement(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeEmptyElement(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeEndDocument() throws javax.xml.stream.XMLStreamException
meth public abstract void writeEndElement() throws javax.xml.stream.XMLStreamException
meth public abstract void writeEntityRef(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeNamespace(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeProcessingInstruction(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeProcessingInstruction(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeStartDocument() throws javax.xml.stream.XMLStreamException
meth public abstract void writeStartDocument(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeStartDocument(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeStartElement(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeStartElement(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeStartElement(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException

CLSS public abstract interface javax.xml.transform.Result
fld public final static java.lang.String PI_DISABLE_OUTPUT_ESCAPING = "javax.xml.transform.disable-output-escaping"
fld public final static java.lang.String PI_ENABLE_OUTPUT_ESCAPING = "javax.xml.transform.enable-output-escaping"
meth public abstract java.lang.String getSystemId()
meth public abstract void setSystemId(java.lang.String)

CLSS public abstract interface javax.xml.transform.Source
meth public abstract java.lang.String getSystemId()
meth public abstract void setSystemId(java.lang.String)

CLSS public javax.xml.transform.sax.SAXResult
cons public init()
cons public init(org.xml.sax.ContentHandler)
fld public final static java.lang.String FEATURE = "http://javax.xml.transform.sax.SAXResult/feature"
intf javax.xml.transform.Result
meth public java.lang.String getSystemId()
meth public org.xml.sax.ContentHandler getHandler()
meth public org.xml.sax.ext.LexicalHandler getLexicalHandler()
meth public void setHandler(org.xml.sax.ContentHandler)
meth public void setLexicalHandler(org.xml.sax.ext.LexicalHandler)
meth public void setSystemId(java.lang.String)
supr java.lang.Object
hfds handler,lexhandler,systemId

CLSS public javax.xml.transform.sax.SAXSource
cons public init()
cons public init(org.xml.sax.InputSource)
cons public init(org.xml.sax.XMLReader,org.xml.sax.InputSource)
fld public final static java.lang.String FEATURE = "http://javax.xml.transform.sax.SAXSource/feature"
intf javax.xml.transform.Source
meth public java.lang.String getSystemId()
meth public org.xml.sax.InputSource getInputSource()
meth public org.xml.sax.XMLReader getXMLReader()
meth public static org.xml.sax.InputSource sourceToInputSource(javax.xml.transform.Source)
meth public void setInputSource(org.xml.sax.InputSource)
meth public void setSystemId(java.lang.String)
meth public void setXMLReader(org.xml.sax.XMLReader)
supr java.lang.Object
hfds inputSource,reader

CLSS public abstract interface javax.xml.ws.Binding
meth public abstract java.lang.String getBindingID()
meth public abstract java.util.List<javax.xml.ws.handler.Handler> getHandlerChain()
meth public abstract void setHandlerChain(java.util.List<javax.xml.ws.handler.Handler>)

CLSS public abstract interface javax.xml.ws.BindingProvider
fld public final static java.lang.String ENDPOINT_ADDRESS_PROPERTY = "javax.xml.ws.service.endpoint.address"
fld public final static java.lang.String PASSWORD_PROPERTY = "javax.xml.ws.security.auth.password"
fld public final static java.lang.String SESSION_MAINTAIN_PROPERTY = "javax.xml.ws.session.maintain"
fld public final static java.lang.String SOAPACTION_URI_PROPERTY = "javax.xml.ws.soap.http.soapaction.uri"
fld public final static java.lang.String SOAPACTION_USE_PROPERTY = "javax.xml.ws.soap.http.soapaction.use"
fld public final static java.lang.String USERNAME_PROPERTY = "javax.xml.ws.security.auth.username"
meth public abstract <%0 extends javax.xml.ws.EndpointReference> {%%0} getEndpointReference(java.lang.Class<{%%0}>)
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getRequestContext()
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getResponseContext()
meth public abstract javax.xml.ws.Binding getBinding()
meth public abstract javax.xml.ws.EndpointReference getEndpointReference()

CLSS public abstract javax.xml.ws.EndpointReference
cons protected init()
meth public !varargs <%0 extends java.lang.Object> {%%0} getPort(java.lang.Class<{%%0}>,javax.xml.ws.WebServiceFeature[])
meth public abstract void writeTo(javax.xml.transform.Result)
meth public java.lang.String toString()
meth public static javax.xml.ws.EndpointReference readFrom(javax.xml.transform.Source)
supr java.lang.Object

CLSS public javax.xml.ws.WebServiceException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

CLSS public abstract javax.xml.ws.WebServiceFeature
cons protected init()
fld protected boolean enabled
meth public abstract java.lang.String getID()
meth public boolean isEnabled()
supr java.lang.Object

CLSS public abstract javax.xml.ws.spi.Provider
cons protected init()
fld public final static java.lang.String JAXWSPROVIDER_PROPERTY = "javax.xml.ws.spi.Provider"
meth public !varargs javax.xml.ws.Endpoint createAndPublishEndpoint(java.lang.String,java.lang.Object,javax.xml.ws.WebServiceFeature[])
meth public !varargs javax.xml.ws.Endpoint createEndpoint(java.lang.String,java.lang.Class<?>,javax.xml.ws.spi.Invoker,javax.xml.ws.WebServiceFeature[])
meth public !varargs javax.xml.ws.Endpoint createEndpoint(java.lang.String,java.lang.Object,javax.xml.ws.WebServiceFeature[])
meth public !varargs javax.xml.ws.spi.ServiceDelegate createServiceDelegate(java.net.URL,javax.xml.namespace.QName,java.lang.Class<? extends javax.xml.ws.Service>,javax.xml.ws.WebServiceFeature[])
meth public abstract !varargs <%0 extends java.lang.Object> {%%0} getPort(javax.xml.ws.EndpointReference,java.lang.Class<{%%0}>,javax.xml.ws.WebServiceFeature[])
meth public abstract javax.xml.ws.Endpoint createAndPublishEndpoint(java.lang.String,java.lang.Object)
meth public abstract javax.xml.ws.Endpoint createEndpoint(java.lang.String,java.lang.Object)
meth public abstract javax.xml.ws.EndpointReference readEndpointReference(javax.xml.transform.Source)
meth public abstract javax.xml.ws.spi.ServiceDelegate createServiceDelegate(java.net.URL,javax.xml.namespace.QName,java.lang.Class<? extends javax.xml.ws.Service>)
meth public abstract javax.xml.ws.wsaddressing.W3CEndpointReference createW3CEndpointReference(java.lang.String,javax.xml.namespace.QName,javax.xml.namespace.QName,java.util.List<org.w3c.dom.Element>,java.lang.String,java.util.List<org.w3c.dom.Element>)
meth public javax.xml.ws.wsaddressing.W3CEndpointReference createW3CEndpointReference(java.lang.String,javax.xml.namespace.QName,javax.xml.namespace.QName,javax.xml.namespace.QName,java.util.List<org.w3c.dom.Element>,java.lang.String,java.util.List<org.w3c.dom.Element>,java.util.List<org.w3c.dom.Element>,java.util.Map<javax.xml.namespace.QName,java.lang.String>)
meth public static javax.xml.ws.spi.Provider provider()
supr java.lang.Object
hfds DEFAULT_JAXWSPROVIDER,iteratorMethod,loadMethod

CLSS public abstract javax.xml.ws.spi.ServiceDelegate
cons protected init()
meth public abstract !varargs <%0 extends java.lang.Object> javax.xml.ws.Dispatch<{%%0}> createDispatch(javax.xml.namespace.QName,java.lang.Class<{%%0}>,javax.xml.ws.Service$Mode,javax.xml.ws.WebServiceFeature[])
meth public abstract !varargs <%0 extends java.lang.Object> javax.xml.ws.Dispatch<{%%0}> createDispatch(javax.xml.ws.EndpointReference,java.lang.Class<{%%0}>,javax.xml.ws.Service$Mode,javax.xml.ws.WebServiceFeature[])
meth public abstract !varargs <%0 extends java.lang.Object> {%%0} getPort(java.lang.Class<{%%0}>,javax.xml.ws.WebServiceFeature[])
meth public abstract !varargs <%0 extends java.lang.Object> {%%0} getPort(javax.xml.namespace.QName,java.lang.Class<{%%0}>,javax.xml.ws.WebServiceFeature[])
meth public abstract !varargs <%0 extends java.lang.Object> {%%0} getPort(javax.xml.ws.EndpointReference,java.lang.Class<{%%0}>,javax.xml.ws.WebServiceFeature[])
meth public abstract !varargs javax.xml.ws.Dispatch<java.lang.Object> createDispatch(javax.xml.namespace.QName,javax.xml.bind.JAXBContext,javax.xml.ws.Service$Mode,javax.xml.ws.WebServiceFeature[])
meth public abstract !varargs javax.xml.ws.Dispatch<java.lang.Object> createDispatch(javax.xml.ws.EndpointReference,javax.xml.bind.JAXBContext,javax.xml.ws.Service$Mode,javax.xml.ws.WebServiceFeature[])
meth public abstract <%0 extends java.lang.Object> javax.xml.ws.Dispatch<{%%0}> createDispatch(javax.xml.namespace.QName,java.lang.Class<{%%0}>,javax.xml.ws.Service$Mode)
meth public abstract <%0 extends java.lang.Object> {%%0} getPort(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} getPort(javax.xml.namespace.QName,java.lang.Class<{%%0}>)
meth public abstract java.net.URL getWSDLDocumentLocation()
meth public abstract java.util.Iterator<javax.xml.namespace.QName> getPorts()
meth public abstract java.util.concurrent.Executor getExecutor()
meth public abstract javax.xml.namespace.QName getServiceName()
meth public abstract javax.xml.ws.Dispatch<java.lang.Object> createDispatch(javax.xml.namespace.QName,javax.xml.bind.JAXBContext,javax.xml.ws.Service$Mode)
meth public abstract javax.xml.ws.handler.HandlerResolver getHandlerResolver()
meth public abstract void addPort(javax.xml.namespace.QName,java.lang.String,java.lang.String)
meth public abstract void setExecutor(java.util.concurrent.Executor)
meth public abstract void setHandlerResolver(javax.xml.ws.handler.HandlerResolver)
supr java.lang.Object

CLSS public abstract interface !annotation javax.xml.ws.spi.WebServiceFeatureAnnotation
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<? extends javax.xml.ws.WebServiceFeature> bean()
meth public abstract java.lang.String id()

CLSS public abstract interface !annotation org.glassfish.gmbal.ManagedData
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String name()

CLSS public abstract org.jvnet.staxex.StreamingDataHandler
cons public init(java.lang.Object,java.lang.String)
cons public init(java.net.URL)
cons public init(javax.activation.DataSource)
intf java.io.Closeable
meth public abstract java.io.InputStream readOnce() throws java.io.IOException
meth public abstract void close() throws java.io.IOException
meth public abstract void moveTo(java.io.File) throws java.io.IOException
supr javax.activation.DataHandler

CLSS public abstract interface org.jvnet.ws.message.DistributedPropertySet
intf org.jvnet.ws.message.PropertySet
meth public abstract <%0 extends org.jvnet.ws.message.PropertySet> {%%0} getSatellite(java.lang.Class<{%%0}>)
 anno 0 com.sun.istack.Nullable()
meth public abstract void addSatellite(java.lang.Class,org.jvnet.ws.message.PropertySet)
meth public abstract void addSatellite(org.jvnet.ws.message.PropertySet)
meth public abstract void copySatelliteInto(org.jvnet.ws.message.MessageContext)
meth public abstract void removeSatellite(org.jvnet.ws.message.PropertySet)

CLSS public abstract interface org.jvnet.ws.message.MessageContext
intf org.jvnet.ws.message.DistributedPropertySet
meth public abstract <%0 extends org.jvnet.ws.message.PropertySet> {%%0} getSatellite(java.lang.Class<{%%0}>)
meth public abstract javax.xml.soap.SOAPMessage getSOAPMessage() throws javax.xml.soap.SOAPException
meth public abstract void addSatellite(org.jvnet.ws.message.PropertySet)
meth public abstract void copySatelliteInto(org.jvnet.ws.message.MessageContext)
meth public abstract void removeSatellite(org.jvnet.ws.message.PropertySet)

CLSS public abstract interface org.jvnet.ws.message.PropertySet
innr public abstract interface static !annotation Property
meth public abstract boolean containsKey(java.lang.Object)
meth public abstract boolean supports(java.lang.Object)
meth public abstract java.lang.Object get(java.lang.Object)
meth public abstract java.lang.Object put(java.lang.String,java.lang.Object)
meth public abstract java.lang.Object remove(java.lang.Object)
meth public abstract java.util.Map<java.lang.String,java.lang.Object> createMapView()

CLSS public abstract interface org.w3c.dom.Document
intf org.w3c.dom.Node
meth public abstract boolean getStrictErrorChecking()
meth public abstract boolean getXmlStandalone()
meth public abstract java.lang.String getDocumentURI()
meth public abstract java.lang.String getInputEncoding()
meth public abstract java.lang.String getXmlEncoding()
meth public abstract java.lang.String getXmlVersion()
meth public abstract org.w3c.dom.Attr createAttribute(java.lang.String)
meth public abstract org.w3c.dom.Attr createAttributeNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.CDATASection createCDATASection(java.lang.String)
meth public abstract org.w3c.dom.Comment createComment(java.lang.String)
meth public abstract org.w3c.dom.DOMConfiguration getDomConfig()
meth public abstract org.w3c.dom.DOMImplementation getImplementation()
meth public abstract org.w3c.dom.DocumentFragment createDocumentFragment()
meth public abstract org.w3c.dom.DocumentType getDoctype()
meth public abstract org.w3c.dom.Element createElement(java.lang.String)
meth public abstract org.w3c.dom.Element createElementNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.Element getDocumentElement()
meth public abstract org.w3c.dom.Element getElementById(java.lang.String)
meth public abstract org.w3c.dom.EntityReference createEntityReference(java.lang.String)
meth public abstract org.w3c.dom.Node adoptNode(org.w3c.dom.Node)
meth public abstract org.w3c.dom.Node importNode(org.w3c.dom.Node,boolean)
meth public abstract org.w3c.dom.Node renameNode(org.w3c.dom.Node,java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.NodeList getElementsByTagName(java.lang.String)
meth public abstract org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.ProcessingInstruction createProcessingInstruction(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.Text createTextNode(java.lang.String)
meth public abstract void normalizeDocument()
meth public abstract void setDocumentURI(java.lang.String)
meth public abstract void setStrictErrorChecking(boolean)
meth public abstract void setXmlStandalone(boolean)
meth public abstract void setXmlVersion(java.lang.String)

CLSS public abstract interface org.w3c.dom.DocumentFragment
intf org.w3c.dom.Node

CLSS public abstract interface org.w3c.dom.Element
intf org.w3c.dom.Node
meth public abstract boolean hasAttribute(java.lang.String)
meth public abstract boolean hasAttributeNS(java.lang.String,java.lang.String)
meth public abstract java.lang.String getAttribute(java.lang.String)
meth public abstract java.lang.String getAttributeNS(java.lang.String,java.lang.String)
meth public abstract java.lang.String getTagName()
meth public abstract org.w3c.dom.Attr getAttributeNode(java.lang.String)
meth public abstract org.w3c.dom.Attr getAttributeNodeNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.Attr removeAttributeNode(org.w3c.dom.Attr)
meth public abstract org.w3c.dom.Attr setAttributeNode(org.w3c.dom.Attr)
meth public abstract org.w3c.dom.Attr setAttributeNodeNS(org.w3c.dom.Attr)
meth public abstract org.w3c.dom.NodeList getElementsByTagName(java.lang.String)
meth public abstract org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.TypeInfo getSchemaTypeInfo()
meth public abstract void removeAttribute(java.lang.String)
meth public abstract void removeAttributeNS(java.lang.String,java.lang.String)
meth public abstract void setAttribute(java.lang.String,java.lang.String)
meth public abstract void setAttributeNS(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void setIdAttribute(java.lang.String,boolean)
meth public abstract void setIdAttributeNS(java.lang.String,java.lang.String,boolean)
meth public abstract void setIdAttributeNode(org.w3c.dom.Attr,boolean)

CLSS public abstract interface org.w3c.dom.Node
fld public final static short ATTRIBUTE_NODE = 2
fld public final static short CDATA_SECTION_NODE = 4
fld public final static short COMMENT_NODE = 8
fld public final static short DOCUMENT_FRAGMENT_NODE = 11
fld public final static short DOCUMENT_NODE = 9
fld public final static short DOCUMENT_POSITION_CONTAINED_BY = 16
fld public final static short DOCUMENT_POSITION_CONTAINS = 8
fld public final static short DOCUMENT_POSITION_DISCONNECTED = 1
fld public final static short DOCUMENT_POSITION_FOLLOWING = 4
fld public final static short DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC = 32
fld public final static short DOCUMENT_POSITION_PRECEDING = 2
fld public final static short DOCUMENT_TYPE_NODE = 10
fld public final static short ELEMENT_NODE = 1
fld public final static short ENTITY_NODE = 6
fld public final static short ENTITY_REFERENCE_NODE = 5
fld public final static short NOTATION_NODE = 12
fld public final static short PROCESSING_INSTRUCTION_NODE = 7
fld public final static short TEXT_NODE = 3
meth public abstract boolean hasAttributes()
meth public abstract boolean hasChildNodes()
meth public abstract boolean isDefaultNamespace(java.lang.String)
meth public abstract boolean isEqualNode(org.w3c.dom.Node)
meth public abstract boolean isSameNode(org.w3c.dom.Node)
meth public abstract boolean isSupported(java.lang.String,java.lang.String)
meth public abstract java.lang.Object getFeature(java.lang.String,java.lang.String)
meth public abstract java.lang.Object getUserData(java.lang.String)
meth public abstract java.lang.Object setUserData(java.lang.String,java.lang.Object,org.w3c.dom.UserDataHandler)
meth public abstract java.lang.String getBaseURI()
meth public abstract java.lang.String getLocalName()
meth public abstract java.lang.String getNamespaceURI()
meth public abstract java.lang.String getNodeName()
meth public abstract java.lang.String getNodeValue()
meth public abstract java.lang.String getPrefix()
meth public abstract java.lang.String getTextContent()
meth public abstract java.lang.String lookupNamespaceURI(java.lang.String)
meth public abstract java.lang.String lookupPrefix(java.lang.String)
meth public abstract org.w3c.dom.Document getOwnerDocument()
meth public abstract org.w3c.dom.NamedNodeMap getAttributes()
meth public abstract org.w3c.dom.Node appendChild(org.w3c.dom.Node)
meth public abstract org.w3c.dom.Node cloneNode(boolean)
meth public abstract org.w3c.dom.Node getFirstChild()
meth public abstract org.w3c.dom.Node getLastChild()
meth public abstract org.w3c.dom.Node getNextSibling()
meth public abstract org.w3c.dom.Node getParentNode()
meth public abstract org.w3c.dom.Node getPreviousSibling()
meth public abstract org.w3c.dom.Node insertBefore(org.w3c.dom.Node,org.w3c.dom.Node)
meth public abstract org.w3c.dom.Node removeChild(org.w3c.dom.Node)
meth public abstract org.w3c.dom.Node replaceChild(org.w3c.dom.Node,org.w3c.dom.Node)
meth public abstract org.w3c.dom.NodeList getChildNodes()
meth public abstract short compareDocumentPosition(org.w3c.dom.Node)
meth public abstract short getNodeType()
meth public abstract void normalize()
meth public abstract void setNodeValue(java.lang.String)
meth public abstract void setPrefix(java.lang.String)
meth public abstract void setTextContent(java.lang.String)

CLSS public abstract interface org.w3c.dom.NodeList
meth public abstract int getLength()
meth public abstract org.w3c.dom.Node item(int)

CLSS public abstract interface org.w3c.dom.events.DocumentEvent
meth public abstract org.w3c.dom.events.Event createEvent(java.lang.String)

CLSS public abstract interface org.w3c.dom.events.EventTarget
meth public abstract boolean dispatchEvent(org.w3c.dom.events.Event)
meth public abstract void addEventListener(java.lang.String,org.w3c.dom.events.EventListener,boolean)
meth public abstract void removeEventListener(java.lang.String,org.w3c.dom.events.EventListener,boolean)

CLSS public abstract interface org.w3c.dom.ranges.DocumentRange
meth public abstract org.w3c.dom.ranges.Range createRange()

CLSS public abstract interface org.w3c.dom.traversal.DocumentTraversal
meth public abstract org.w3c.dom.traversal.NodeIterator createNodeIterator(org.w3c.dom.Node,int,org.w3c.dom.traversal.NodeFilter,boolean)
meth public abstract org.w3c.dom.traversal.TreeWalker createTreeWalker(org.w3c.dom.Node,int,org.w3c.dom.traversal.NodeFilter,boolean)

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

CLSS public org.xml.sax.helpers.DefaultHandler
cons public init()
intf org.xml.sax.ContentHandler
intf org.xml.sax.DTDHandler
intf org.xml.sax.EntityResolver
intf org.xml.sax.ErrorHandler
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
supr java.lang.Object

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
hfds contentHandler,dtdHandler,entityResolver,errorHandler,locator,parent

