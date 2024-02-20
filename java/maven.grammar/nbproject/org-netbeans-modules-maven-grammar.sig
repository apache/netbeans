#Signature file v4.1
#Version 1.70.0

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

CLSS public abstract org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar
cons public init(org.netbeans.modules.xml.api.model.GrammarEnvironment)
innr protected abstract static AbstractResultNode
innr protected static ComplexElement
innr protected static ExpressionValueTextElement
innr public static MyElement
innr public static MyTextElement
innr public static PartialTextElement
intf org.netbeans.modules.xml.api.model.GrammarQuery
meth protected abstract java.io.InputStream getSchemaStream()
meth protected final java.util.Enumeration<org.netbeans.modules.xml.api.model.GrammarResult> createTextValueList(java.lang.String[],org.netbeans.modules.xml.api.model.HintContext)
meth protected final org.apache.maven.project.MavenProject getMavenProject()
meth protected final org.jdom2.Element findElement(org.jdom2.Element,java.lang.String)
meth protected final org.jdom2.Element findNonTypedContent(org.jdom2.Element)
meth protected final org.jdom2.Element findTypeContent(java.lang.String,org.jdom2.Element)
meth protected final org.netbeans.modules.xml.api.model.GrammarEnvironment getEnvironment()
meth protected final void processSequence(java.lang.String,org.jdom2.Element,java.util.Vector<org.netbeans.modules.xml.api.model.GrammarResult>)
meth protected java.util.Enumeration<org.netbeans.modules.xml.api.model.GrammarResult> getDynamicValueCompletion(java.lang.String,org.netbeans.modules.xml.api.model.HintContext,org.jdom2.Element)
meth protected java.util.List<org.netbeans.modules.xml.api.model.GrammarResult> getDynamicCompletion(java.lang.String,org.netbeans.modules.xml.api.model.HintContext,org.jdom2.Element)
meth public boolean hasCustomizer(org.netbeans.modules.xml.api.model.HintContext)
meth public boolean isAllowed(java.util.Enumeration)
meth public java.awt.Component getCustomizer(org.netbeans.modules.xml.api.model.HintContext)
meth public java.util.Enumeration<org.netbeans.modules.xml.api.model.GrammarResult> queryAttributes(org.netbeans.modules.xml.api.model.HintContext)
meth public java.util.Enumeration<org.netbeans.modules.xml.api.model.GrammarResult> queryElements(org.netbeans.modules.xml.api.model.HintContext)
meth public java.util.Enumeration<org.netbeans.modules.xml.api.model.GrammarResult> queryEntities(java.lang.String)
meth public java.util.Enumeration<org.netbeans.modules.xml.api.model.GrammarResult> queryNotations(java.lang.String)
meth public java.util.Enumeration<org.netbeans.modules.xml.api.model.GrammarResult> queryValues(org.netbeans.modules.xml.api.model.HintContext)
meth public org.netbeans.modules.xml.api.model.GrammarResult queryDefault(org.netbeans.modules.xml.api.model.HintContext)
meth public org.openide.nodes.Node$Property[] getProperties(org.netbeans.modules.xml.api.model.HintContext)
supr java.lang.Object
hfds environment,schemaDoc

CLSS protected abstract static org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar$AbstractResultNode
 outer org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar
cons protected init()
intf org.netbeans.modules.xml.api.model.GrammarResult
meth public boolean isEmptyElement()
meth public java.lang.String getDescription()
meth public java.lang.String getDisplayName()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public javax.swing.Icon getIcon(int)
meth public void setDescription(java.lang.String)
meth public void setIcon(javax.swing.Icon)
supr org.netbeans.modules.xml.spi.dom.AbstractNode
hfds desc,icon

CLSS protected static org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar$ComplexElement
 outer org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar
intf org.w3c.dom.Element
meth public boolean hasChildNodes()
meth public java.lang.String getDisplayName()
meth public java.lang.String getNodeName()
meth public java.lang.String getTagName()
meth public org.w3c.dom.Node getFirstChild()
meth public org.w3c.dom.Node getLastChild()
meth public org.w3c.dom.NodeList getChildNodes()
meth public short getNodeType()
supr org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar$AbstractResultNode
hfds display,list,name

CLSS protected static org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar$ExpressionValueTextElement
 outer org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar
cons public init(java.lang.String,java.lang.String,int)
cons public init(java.lang.String,java.lang.String,java.lang.String)
meth public int getLength()
meth public java.lang.String getNodeValue()
supr org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar$MyTextElement
hfds delLen,suffix

CLSS public static org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar$MyElement
 outer org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar
cons public init(java.lang.String)
intf org.w3c.dom.Element
meth public java.lang.String getNodeName()
meth public java.lang.String getTagName()
meth public short getNodeType()
supr org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar$AbstractResultNode
hfds name

CLSS public static org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar$MyTextElement
 outer org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar
cons public init(java.lang.String,java.lang.String)
fld protected final java.lang.String name
fld protected final java.lang.String prefix
intf org.w3c.dom.Text
meth public java.lang.String getNodeName()
meth public java.lang.String getNodeValue()
meth public java.lang.String getTagName()
meth public short getNodeType()
supr org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar$AbstractResultNode

CLSS public static org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar$PartialTextElement
 outer org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar
cons public init()
intf org.w3c.dom.Text
meth public java.lang.String getNodeName()
meth public java.lang.String getNodeValue()
meth public java.lang.String getTagName()
meth public short getNodeType()
supr org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar$AbstractResultNode

CLSS public abstract interface org.netbeans.modules.maven.grammar.spi.GrammarExtensionProvider
meth public abstract java.util.Enumeration<org.netbeans.modules.xml.api.model.GrammarResult> getDynamicValueCompletion(java.lang.String,org.netbeans.modules.xml.api.model.HintContext,org.jdom2.Element)
meth public abstract java.util.List<org.netbeans.modules.xml.api.model.GrammarResult> getDynamicCompletion(java.lang.String,org.netbeans.modules.xml.api.model.HintContext,org.jdom2.Element)
 anno 0 org.netbeans.api.annotations.common.NonNull()

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

CLSS public abstract interface org.netbeans.modules.xml.api.model.GrammarResult
intf org.w3c.dom.Node
meth public abstract boolean isEmptyElement()
meth public abstract java.lang.String getDescription()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getDisplayName()
meth public abstract javax.swing.Icon getIcon(int)

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

CLSS public abstract interface org.w3c.dom.CharacterData
intf org.w3c.dom.Node
meth public abstract int getLength()
meth public abstract java.lang.String getData()
meth public abstract java.lang.String substringData(int,int)
meth public abstract void appendData(java.lang.String)
meth public abstract void deleteData(int,int)
meth public abstract void insertData(int,java.lang.String)
meth public abstract void replaceData(int,int,java.lang.String)
meth public abstract void setData(java.lang.String)

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

CLSS public abstract interface org.w3c.dom.Text
intf org.w3c.dom.CharacterData
meth public abstract boolean isElementContentWhitespace()
meth public abstract java.lang.String getWholeText()
meth public abstract org.w3c.dom.Text replaceWholeText(java.lang.String)
meth public abstract org.w3c.dom.Text splitText(int)

