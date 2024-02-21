#Signature file v4.1
#Version 1.66.0

CLSS public abstract interface java.io.Serializable

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

CLSS public org.netbeans.modules.xml.api.EncodingUtil
cons public init()
meth public static boolean isValidEncoding(java.lang.String)
meth public static java.io.Reader getUnicodeReader(java.io.InputStream,java.lang.String)
meth public static java.lang.String detectEncoding(java.io.InputStream) throws java.io.IOException
meth public static java.lang.String detectEncoding(javax.swing.text.Document) throws java.io.IOException
meth public static java.lang.String getIANA2JavaMapping(java.lang.String)
meth public static java.lang.String getJava2IANAMapping(java.lang.String)
meth public static java.lang.String getProjectEncoding(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds EXPECTED_PROLOG_LENGTH,UTF8_DEFAULT,encodingIANA2JavaMap,encodingIANAAliasesMap,encodingIANADescriptionMap,encodingJava2IANAMap,logger

CLSS public final org.netbeans.modules.xml.api.XmlFileEncodingQueryImpl
meth public java.nio.charset.Charset getEncoding(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.xml.api.XmlFileEncodingQueryImpl singleton()
supr org.netbeans.spi.queries.FileEncodingQueryImplementation
hfds DECODER_SELECTED,ENCODER_SELECTED,LOG,singleton
hcls XMLCharset,XMLDecoder,XMLEncoder

CLSS public final org.netbeans.modules.xml.api.model.DTDUtil
cons public init()
meth public static org.netbeans.modules.xml.api.model.GrammarQuery parseDTD(boolean,org.xml.sax.InputSource)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.xml.api.model.DescriptionSource
meth public abstract boolean isExternal()
meth public abstract java.lang.String getDescription()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.net.URL getContentURL()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.xml.api.model.DescriptionSource resolveLink(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.xml.api.model.ExtendedGrammarQuery
intf org.netbeans.modules.xml.api.model.GrammarQuery
meth public abstract java.util.List<java.lang.String> getResolvedEntities()

CLSS public final org.netbeans.modules.xml.api.model.GrammarEnvironment
cons public init(java.util.Enumeration,org.xml.sax.InputSource,org.openide.filesystems.FileObject)
meth public java.util.Enumeration getDocumentChildren()
meth public org.openide.filesystems.FileObject getFileObject()
meth public org.xml.sax.InputSource getInputSource()
supr java.lang.Object
hfds documentChildren,fileObject,inputSource

CLSS public abstract interface org.netbeans.modules.xml.api.model.GrammarQuery
meth public abstract boolean hasCustomizer(org.netbeans.modules.xml.api.model.HintContext)
meth public abstract boolean isAllowed(java.util.Enumeration<org.netbeans.modules.xml.api.model.GrammarResult>)
meth public abstract java.awt.Component getCustomizer(org.netbeans.modules.xml.api.model.HintContext)
meth public abstract java.util.Enumeration<org.netbeans.modules.xml.api.model.GrammarResult> queryAttributes(org.netbeans.modules.xml.api.model.HintContext)
meth public abstract java.util.Enumeration<org.netbeans.modules.xml.api.model.GrammarResult> queryElements(org.netbeans.modules.xml.api.model.HintContext)
meth public abstract java.util.Enumeration<org.netbeans.modules.xml.api.model.GrammarResult> queryEntities(java.lang.String)
meth public abstract java.util.Enumeration<org.netbeans.modules.xml.api.model.GrammarResult> queryNotations(java.lang.String)
meth public abstract java.util.Enumeration<org.netbeans.modules.xml.api.model.GrammarResult> queryValues(org.netbeans.modules.xml.api.model.HintContext)
meth public abstract org.netbeans.modules.xml.api.model.GrammarResult queryDefault(org.netbeans.modules.xml.api.model.HintContext)
meth public abstract org.openide.nodes.Node$Property<?>[] getProperties(org.netbeans.modules.xml.api.model.HintContext)

CLSS public abstract org.netbeans.modules.xml.api.model.GrammarQueryManager
cons public init()
meth public abstract java.beans.FeatureDescriptor getDescriptor()
meth public abstract java.util.Enumeration enabled(org.netbeans.modules.xml.api.model.GrammarEnvironment)
meth public abstract org.netbeans.modules.xml.api.model.GrammarQuery getGrammar(org.netbeans.modules.xml.api.model.GrammarEnvironment)
meth public static org.netbeans.modules.xml.api.model.GrammarQueryManager getDefault()
supr java.lang.Object
hfds instance
hcls DefaultQueryManager

CLSS public abstract interface org.netbeans.modules.xml.api.model.GrammarResult
intf org.w3c.dom.Node
meth public abstract boolean isEmptyElement()
meth public abstract java.lang.String getDescription()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getDisplayName()
meth public abstract javax.swing.Icon getIcon(int)

CLSS public abstract interface org.netbeans.modules.xml.api.model.HintContext
intf org.w3c.dom.Node
meth public abstract java.lang.String getCurrentPrefix()

CLSS public abstract org.netbeans.modules.xml.spi.dom.AbstractNode
cons public init()
intf org.w3c.dom.Node
meth public abstract short getNodeType()
meth public boolean getSpecified()
meth public boolean getStrictErrorChecking()
meth public boolean getXmlStandalone()
meth public boolean hasAttribute(java.lang.String)
meth public boolean hasAttributeNS(java.lang.String,java.lang.String)
meth public boolean hasAttributes()
meth public boolean hasChildNodes()
meth public boolean isDefaultNamespace(java.lang.String)
meth public boolean isElementContentWhitespace()
meth public boolean isEqualNode(org.w3c.dom.Node)
meth public boolean isId()
meth public boolean isSameNode(org.w3c.dom.Node)
meth public boolean isSupported(java.lang.String,java.lang.String)
meth public int getLength()
meth public java.lang.Object getFeature(java.lang.String,java.lang.String)
meth public java.lang.Object getUserData(java.lang.String)
meth public java.lang.Object setUserData(java.lang.String,java.lang.Object,org.w3c.dom.UserDataHandler)
meth public java.lang.String getAttribute(java.lang.String)
meth public java.lang.String getAttributeNS(java.lang.String,java.lang.String)
meth public java.lang.String getBaseURI()
meth public java.lang.String getData()
meth public java.lang.String getDocumentURI()
meth public java.lang.String getInputEncoding()
meth public java.lang.String getLocalName()
meth public java.lang.String getName()
meth public java.lang.String getNamespaceURI()
meth public java.lang.String getNodeName()
meth public java.lang.String getNodeValue()
meth public java.lang.String getPrefix()
meth public java.lang.String getPublicId()
meth public java.lang.String getSystemId()
meth public java.lang.String getTagName()
meth public java.lang.String getTextContent()
meth public java.lang.String getValue()
meth public java.lang.String getWholeText()
meth public java.lang.String getXmlEncoding()
meth public java.lang.String getXmlVersion()
meth public java.lang.String lookupNamespaceURI(java.lang.String)
meth public java.lang.String lookupPrefix(java.lang.String)
meth public java.lang.String substringData(int,int)
meth public org.w3c.dom.Attr getAttributeNode(java.lang.String)
meth public org.w3c.dom.Attr getAttributeNodeNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.Attr removeAttributeNode(org.w3c.dom.Attr)
meth public org.w3c.dom.Attr setAttributeNode(org.w3c.dom.Attr)
meth public org.w3c.dom.Attr setAttributeNodeNS(org.w3c.dom.Attr)
meth public org.w3c.dom.DOMConfiguration getDomConfig()
meth public org.w3c.dom.Document getOwnerDocument()
meth public org.w3c.dom.Element getOwnerElement()
meth public org.w3c.dom.NamedNodeMap getAttributes()
meth public org.w3c.dom.Node adoptNode(org.w3c.dom.Node)
meth public org.w3c.dom.Node appendChild(org.w3c.dom.Node)
meth public org.w3c.dom.Node cloneNode(boolean)
meth public org.w3c.dom.Node getFirstChild()
meth public org.w3c.dom.Node getLastChild()
meth public org.w3c.dom.Node getNextSibling()
meth public org.w3c.dom.Node getParentNode()
meth public org.w3c.dom.Node getPreviousSibling()
meth public org.w3c.dom.Node insertBefore(org.w3c.dom.Node,org.w3c.dom.Node)
meth public org.w3c.dom.Node removeChild(org.w3c.dom.Node)
meth public org.w3c.dom.Node renameNode(org.w3c.dom.Node,java.lang.String,java.lang.String)
meth public org.w3c.dom.Node replaceChild(org.w3c.dom.Node,org.w3c.dom.Node)
meth public org.w3c.dom.NodeList getChildNodes()
meth public org.w3c.dom.NodeList getElementsByTagName(java.lang.String)
meth public org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.Text replaceWholeText(java.lang.String)
meth public org.w3c.dom.Text splitText(int)
meth public org.w3c.dom.TypeInfo getSchemaTypeInfo()
meth public short compareDocumentPosition(org.w3c.dom.Node)
meth public void appendData(java.lang.String)
meth public void deleteData(int,int)
meth public void insertData(int,java.lang.String)
meth public void normalize()
meth public void normalizeDocument()
meth public void removeAttribute(java.lang.String)
meth public void removeAttributeNS(java.lang.String,java.lang.String)
meth public void replaceData(int,int,java.lang.String)
meth public void setAttribute(java.lang.String,java.lang.String)
meth public void setAttributeNS(java.lang.String,java.lang.String,java.lang.String)
meth public void setData(java.lang.String)
meth public void setDocumentURI(java.lang.String)
meth public void setIdAttribute(java.lang.String,boolean)
meth public void setIdAttributeNS(java.lang.String,java.lang.String,boolean)
meth public void setIdAttributeNode(org.w3c.dom.Attr,boolean)
meth public void setNodeValue(java.lang.String)
meth public void setPrefix(java.lang.String)
meth public void setStrictErrorChecking(boolean)
meth public void setTextContent(java.lang.String)
meth public void setValue(java.lang.String)
meth public void setXmlStandalone(boolean)
meth public void setXmlVersion(java.lang.String)
supr java.lang.Object

CLSS public final org.netbeans.modules.xml.spi.dom.NamedNodeMapImpl
cons public init(java.util.Map)
fld public final static org.w3c.dom.NamedNodeMap EMPTY
intf org.w3c.dom.NamedNodeMap
meth public int getLength()
meth public java.lang.String toString()
meth public org.w3c.dom.Node getNamedItem(java.lang.String)
meth public org.w3c.dom.Node getNamedItemNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.Node item(int)
meth public org.w3c.dom.Node removeNamedItem(java.lang.String)
meth public org.w3c.dom.Node removeNamedItemNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.Node setNamedItem(org.w3c.dom.Node)
meth public org.w3c.dom.Node setNamedItemNS(org.w3c.dom.Node)
meth public static java.lang.Object createKey(java.lang.String)
meth public static java.lang.Object createKey(java.lang.String,java.lang.String)
supr java.lang.Object
hfds peer

CLSS public final org.netbeans.modules.xml.spi.dom.NodeListImpl
cons public init(java.util.List)
fld public final static org.w3c.dom.NodeList EMPTY
intf org.w3c.dom.NodeList
meth public int getLength()
meth public java.lang.String toString()
meth public org.w3c.dom.Node item(int)
supr java.lang.Object
hfds peer

CLSS public final org.netbeans.modules.xml.spi.dom.ROException
cons public init()
supr org.w3c.dom.DOMException
hfds serialVersionUID

CLSS public final org.netbeans.modules.xml.spi.dom.UOException
cons public init()
supr org.w3c.dom.DOMException
hfds serialVersionUID

CLSS public abstract org.netbeans.spi.queries.FileEncodingQueryImplementation
cons public init()
meth protected static void throwUnknownEncoding()
meth public abstract java.nio.charset.Charset getEncoding(org.openide.filesystems.FileObject)
supr java.lang.Object

CLSS public org.w3c.dom.DOMException
cons public init(short,java.lang.String)
fld public final static short DOMSTRING_SIZE_ERR = 2
fld public final static short HIERARCHY_REQUEST_ERR = 3
fld public final static short INDEX_SIZE_ERR = 1
fld public final static short INUSE_ATTRIBUTE_ERR = 10
fld public final static short INVALID_ACCESS_ERR = 15
fld public final static short INVALID_CHARACTER_ERR = 5
fld public final static short INVALID_MODIFICATION_ERR = 13
fld public final static short INVALID_STATE_ERR = 11
fld public final static short NAMESPACE_ERR = 14
fld public final static short NOT_FOUND_ERR = 8
fld public final static short NOT_SUPPORTED_ERR = 9
fld public final static short NO_DATA_ALLOWED_ERR = 6
fld public final static short NO_MODIFICATION_ALLOWED_ERR = 7
fld public final static short SYNTAX_ERR = 12
fld public final static short TYPE_MISMATCH_ERR = 17
fld public final static short VALIDATION_ERR = 16
fld public final static short WRONG_DOCUMENT_ERR = 4
fld public short code
supr java.lang.RuntimeException

CLSS public abstract interface org.w3c.dom.NamedNodeMap
meth public abstract int getLength()
meth public abstract org.w3c.dom.Node getNamedItem(java.lang.String)
meth public abstract org.w3c.dom.Node getNamedItemNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.Node item(int)
meth public abstract org.w3c.dom.Node removeNamedItem(java.lang.String)
meth public abstract org.w3c.dom.Node removeNamedItemNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.Node setNamedItem(org.w3c.dom.Node)
meth public abstract org.w3c.dom.Node setNamedItemNS(org.w3c.dom.Node)

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

