#Signature file v4.1
#Version 3.52

CLSS public abstract interface java.io.Serializable

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

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.web.jsps.parserapi.ELNode
cons public init()
innr public static ELText
innr public static Function
innr public static Nodes
innr public static Root
innr public static Text
innr public static Visitor
meth public abstract void accept(org.netbeans.modules.web.jsps.parserapi.ELNode$Visitor) throws javax.servlet.jsp.JspException
supr java.lang.Object

CLSS public static org.netbeans.modules.web.jsps.parserapi.ELNode$ELText
 outer org.netbeans.modules.web.jsps.parserapi.ELNode
cons public init(java.lang.String)
meth public java.lang.String getText()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.ELNode$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.ELNode
hfds text

CLSS public static org.netbeans.modules.web.jsps.parserapi.ELNode$Function
 outer org.netbeans.modules.web.jsps.parserapi.ELNode
meth public java.lang.String getMethodName()
meth public java.lang.String getName()
meth public java.lang.String getPrefix()
meth public java.lang.String getUri()
meth public java.lang.String[] getParameters()
meth public javax.servlet.jsp.tagext.FunctionInfo getFunctionInfo()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.ELNode$Visitor) throws javax.servlet.jsp.JspException
meth public void setFunctionInfo(javax.servlet.jsp.tagext.FunctionInfo)
meth public void setMethodName(java.lang.String)
meth public void setParameters(java.lang.String[])
meth public void setUri(java.lang.String)
supr org.netbeans.modules.web.jsps.parserapi.ELNode
hfds functionInfo,methodName,name,parameters,prefix,uri

CLSS public static org.netbeans.modules.web.jsps.parserapi.ELNode$Nodes
 outer org.netbeans.modules.web.jsps.parserapi.ELNode
cons public init()
meth public boolean containsEL()
meth public boolean isEmpty()
meth public java.lang.String getMapName()
meth public java.util.Iterator<org.netbeans.modules.web.jsps.parserapi.ELNode> iterator()
meth public void add(org.netbeans.modules.web.jsps.parserapi.ELNode)
meth public void setMapName(java.lang.String)
meth public void visit(org.netbeans.modules.web.jsps.parserapi.ELNode$Visitor) throws javax.servlet.jsp.JspException
supr java.lang.Object
hfds list,mapName

CLSS public static org.netbeans.modules.web.jsps.parserapi.ELNode$Root
 outer org.netbeans.modules.web.jsps.parserapi.ELNode
cons public init(org.netbeans.modules.web.jsps.parserapi.ELNode$Nodes)
meth public org.netbeans.modules.web.jsps.parserapi.ELNode$Nodes getExpression()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.ELNode$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.ELNode
hfds expr

CLSS public static org.netbeans.modules.web.jsps.parserapi.ELNode$Text
 outer org.netbeans.modules.web.jsps.parserapi.ELNode
cons public init(java.lang.String)
meth public java.lang.String getText()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.ELNode$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.ELNode
hfds text

CLSS public static org.netbeans.modules.web.jsps.parserapi.ELNode$Visitor
 outer org.netbeans.modules.web.jsps.parserapi.ELNode
cons public init()
meth public void visit(org.netbeans.modules.web.jsps.parserapi.ELNode$ELText) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.ELNode$Function) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.ELNode$Root) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.ELNode$Text) throws javax.servlet.jsp.JspException
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.web.jsps.parserapi.JspParserAPI
fld public final static int ERROR_IGNORE = 1
fld public final static int ERROR_REPORT_ACCURATE = 3
fld public final static int ERROR_REPORT_ANY = 2
fld public final static java.lang.String TAG_MIME_TYPE = "text/x-tag"
innr public final static ErrorDescriptor
innr public final static JspOpenInfo
innr public final static ParseResult
meth public abstract java.net.URLClassLoader getModuleClassLoader(org.netbeans.modules.web.api.webmodule.WebModule)
meth public abstract java.util.Map<java.lang.String,java.lang.String[]> getTaglibMap(org.netbeans.modules.web.api.webmodule.WebModule) throws java.io.IOException
meth public abstract org.netbeans.modules.web.jsps.parserapi.JspParserAPI$JspOpenInfo getJspOpenInfo(org.openide.filesystems.FileObject,org.netbeans.modules.web.api.webmodule.WebModule,boolean)
meth public abstract org.netbeans.modules.web.jsps.parserapi.JspParserAPI$ParseResult analyzePage(org.openide.filesystems.FileObject,org.netbeans.modules.web.api.webmodule.WebModule,int)
meth public abstract void addTldChangeListener(org.netbeans.modules.web.jsps.parserapi.TldChangeListener)
meth public abstract void removeTldChangeListener(org.netbeans.modules.web.jsps.parserapi.TldChangeListener)

CLSS public final static org.netbeans.modules.web.jsps.parserapi.JspParserAPI$ErrorDescriptor
 outer org.netbeans.modules.web.jsps.parserapi.JspParserAPI
cons public init(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,int,int,java.lang.String,java.lang.String)
fld protected final int column
fld protected final int line
fld protected final java.lang.String errorMessage
fld protected final java.lang.String referenceText
fld protected final org.openide.filesystems.FileObject source
fld protected final org.openide.filesystems.FileObject wmRoot
meth public int getColumn()
meth public int getLine()
meth public java.lang.String getErrorMessage()
meth public java.lang.String getReferenceText()
meth public java.lang.String toString()
meth public org.openide.filesystems.FileObject getSource()
supr java.lang.Object

CLSS public final static org.netbeans.modules.web.jsps.parserapi.JspParserAPI$JspOpenInfo
 outer org.netbeans.modules.web.jsps.parserapi.JspParserAPI
cons public init(boolean,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public boolean isXmlSyntax()
meth public int hashCode()
meth public java.lang.String getEncoding()
meth public java.lang.String toString()
supr java.lang.Object
hfds encoding,isXml

CLSS public final static org.netbeans.modules.web.jsps.parserapi.JspParserAPI$ParseResult
 outer org.netbeans.modules.web.jsps.parserapi.JspParserAPI
cons public init(org.netbeans.modules.web.jsps.parserapi.JspParserAPI$ErrorDescriptor[])
cons public init(org.netbeans.modules.web.jsps.parserapi.PageInfo,org.netbeans.modules.web.jsps.parserapi.Node$Nodes)
cons public init(org.netbeans.modules.web.jsps.parserapi.PageInfo,org.netbeans.modules.web.jsps.parserapi.Node$Nodes,org.netbeans.modules.web.jsps.parserapi.JspParserAPI$ErrorDescriptor[])
fld protected final boolean parsedOK
fld protected final org.netbeans.modules.web.jsps.parserapi.JspParserAPI$ErrorDescriptor[] errors
fld protected final org.netbeans.modules.web.jsps.parserapi.Node$Nodes nodes
fld protected final org.netbeans.modules.web.jsps.parserapi.PageInfo pageInfo
meth public boolean isParsingSuccess()
meth public java.lang.String toString()
meth public org.netbeans.modules.web.jsps.parserapi.JspParserAPI$ErrorDescriptor[] getErrors()
meth public org.netbeans.modules.web.jsps.parserapi.Node$Nodes getNodes()
meth public org.netbeans.modules.web.jsps.parserapi.PageInfo getPageInfo()
supr java.lang.Object

CLSS public final org.netbeans.modules.web.jsps.parserapi.JspParserFactory
meth public static org.netbeans.modules.web.jsps.parserapi.JspParserAPI getJspParser()
supr java.lang.Object
hfds parser

CLSS public final org.netbeans.modules.web.jsps.parserapi.Mark
cons public init(java.lang.String,int,int)
meth public boolean equals(java.lang.Object)
meth public int getColumnNumber()
meth public int getLineNumber()
meth public java.lang.String getFile()
meth public java.lang.String toShortString()
meth public java.lang.String toString()
supr java.lang.Object
hfds col,fileName,line

CLSS public abstract org.netbeans.modules.web.jsps.parserapi.Node
fld protected int beginJavaLine
fld protected int endJavaLine
fld protected java.lang.String localName
fld protected java.lang.String qName
fld protected java.lang.String text
fld protected org.netbeans.modules.web.jsps.parserapi.Mark startMark
fld protected org.netbeans.modules.web.jsps.parserapi.Node parent
fld protected org.netbeans.modules.web.jsps.parserapi.Node$Nodes body
fld protected org.netbeans.modules.web.jsps.parserapi.Node$Nodes namedAttributeNodes
fld protected org.xml.sax.Attributes attrs
fld protected org.xml.sax.Attributes nonTaglibXmlnsAttrs
fld protected org.xml.sax.Attributes taglibAttrs
fld public final static java.lang.String ATTRIBUTE_ACTION = "attribute"
fld public final static java.lang.String ATTRIBUTE_DIRECTIVE_ACTION = "directive.attribute"
fld public final static java.lang.String BODY_ACTION = "body"
fld public final static java.lang.String DECLARATION_ACTION = "declaration"
fld public final static java.lang.String DIRECTIVE_ACTION = "directive."
fld public final static java.lang.String DOBODY_ACTION = "doBody"
fld public final static java.lang.String ELEMENT_ACTION = "element"
fld public final static java.lang.String EXPRESSION_ACTION = "expression"
fld public final static java.lang.String FALLBACK_ACTION = "fallback"
fld public final static java.lang.String FORWARD_ACTION = "forward"
fld public final static java.lang.String GET_PROPERTY_ACTION = "getProperty"
fld public final static java.lang.String INCLUDE_ACTION = "include"
fld public final static java.lang.String INCLUDE_DIRECTIVE_ACTION = "directive.include"
fld public final static java.lang.String INVOKE_ACTION = "invoke"
fld public final static java.lang.String JSP_ATTRIBUTE_ACTION = "jsp:attribute"
fld public final static java.lang.String JSP_ATTRIBUTE_DIRECTIVE_ACTION = "jsp:directive.attribute"
fld public final static java.lang.String JSP_BODY_ACTION = "jsp:body"
fld public final static java.lang.String JSP_DECLARATION_ACTION = "jsp:declaration"
fld public final static java.lang.String JSP_DOBODY_ACTION = "jsp:doBody"
fld public final static java.lang.String JSP_ELEMENT_ACTION = "jsp:element"
fld public final static java.lang.String JSP_EXPRESSION_ACTION = "jsp:expression"
fld public final static java.lang.String JSP_FALLBACK_ACTION = "jsp:fallback"
fld public final static java.lang.String JSP_FORWARD_ACTION = "jsp:forward"
fld public final static java.lang.String JSP_GET_PROPERTY_ACTION = "jsp:getProperty"
fld public final static java.lang.String JSP_INCLUDE_ACTION = "jsp:include"
fld public final static java.lang.String JSP_INCLUDE_DIRECTIVE_ACTION = "jsp:directive.include"
fld public final static java.lang.String JSP_INVOKE_ACTION = "jsp:invoke"
fld public final static java.lang.String JSP_OUTPUT_ACTION = "jsp:output"
fld public final static java.lang.String JSP_PAGE_DIRECTIVE_ACTION = "jsp:directive.page"
fld public final static java.lang.String JSP_PARAMS_ACTION = "jsp:params"
fld public final static java.lang.String JSP_PARAM_ACTION = "jsp:param"
fld public final static java.lang.String JSP_PLUGIN_ACTION = "jsp:plugin"
fld public final static java.lang.String JSP_ROOT_ACTION = "jsp:root"
fld public final static java.lang.String JSP_SCRIPTLET_ACTION = "jsp:scriptlet"
fld public final static java.lang.String JSP_SET_PROPERTY_ACTION = "jsp:setProperty"
fld public final static java.lang.String JSP_TAGLIB_DIRECTIVE_ACTION = "jsp:taglib"
fld public final static java.lang.String JSP_TAG_DIRECTIVE_ACTION = "jsp:directive.tag"
fld public final static java.lang.String JSP_TEXT_ACTION = "jsp:text"
fld public final static java.lang.String JSP_TEXT_ACTION_END = "</jsp:text>"
fld public final static java.lang.String JSP_URI = "http://java.sun.com/JSP/Page"
fld public final static java.lang.String JSP_USE_BEAN_ACTION = "jsp:useBean"
fld public final static java.lang.String JSP_VARIABLE_DIRECTIVE_ACTION = "jsp:directive.variable"
fld public final static java.lang.String OUTPUT_ACTION = "output"
fld public final static java.lang.String PAGE_DIRECTIVE_ACTION = "directive.page"
fld public final static java.lang.String PARAMS_ACTION = "params"
fld public final static java.lang.String PARAM_ACTION = "param"
fld public final static java.lang.String PLUGIN_ACTION = "plugin"
fld public final static java.lang.String ROOT_ACTION = "root"
fld public final static java.lang.String SCRIPTLET_ACTION = "scriptlet"
fld public final static java.lang.String SET_PROPERTY_ACTION = "setProperty"
fld public final static java.lang.String TAGLIB_DIRECTIVE_ACTION = "taglib"
fld public final static java.lang.String TAG_DIRECTIVE_ACTION = "directive.tag"
fld public final static java.lang.String TEXT_ACTION = "text"
fld public final static java.lang.String URN_JSPTAGDIR = "urn:jsptagdir:"
fld public final static java.lang.String URN_JSPTLD = "urn:jsptld:"
fld public final static java.lang.String USE_BEAN_ACTION = "useBean"
fld public final static java.lang.String VARIABLE_DIRECTIVE_ACTION = "directive.variable"
innr public abstract static ScriptingElement
innr public static AttributeDirective
innr public static AttributeGenerator
innr public static ChildInfo
innr public static Comment
innr public static CustomTag
innr public static Declaration
innr public static DoBodyAction
innr public static ELExpression
innr public static Expression
innr public static FallBackAction
innr public static ForwardAction
innr public static GetProperty
innr public static IncludeAction
innr public static IncludeDirective
innr public static InvokeAction
innr public static JspAttribute
innr public static JspBody
innr public static JspElement
innr public static JspOutput
innr public static JspRoot
innr public static JspText
innr public static NamedAttribute
innr public static Nodes
innr public static PageDirective
innr public static ParamAction
innr public static ParamsAction
innr public static PlugIn
innr public static Root
innr public static Scriptlet
innr public static SetProperty
innr public static TagDirective
innr public static TaglibDirective
innr public static TemplateText
innr public static UninterpretedTag
innr public static UseBean
innr public static VariableDirective
innr public static Visitor
meth public boolean isDummy()
meth public int getBeginJavaLine()
meth public int getEndJavaLine()
meth public java.lang.String getAttributeValue(java.lang.String)
meth public java.lang.String getLocalName()
meth public java.lang.String getQName()
meth public java.lang.String getText()
meth public java.lang.String getTextAttribute(java.lang.String)
meth public org.netbeans.modules.web.jsps.parserapi.Mark getStart()
meth public org.netbeans.modules.web.jsps.parserapi.Node getParent()
meth public org.netbeans.modules.web.jsps.parserapi.Node$NamedAttribute getNamedAttributeNode(java.lang.String)
meth public org.netbeans.modules.web.jsps.parserapi.Node$Nodes getBody()
meth public org.netbeans.modules.web.jsps.parserapi.Node$Nodes getNamedAttributeNodes()
meth public org.netbeans.modules.web.jsps.parserapi.Node$Root getRoot()
meth public org.xml.sax.Attributes getAttributes()
meth public org.xml.sax.Attributes getNonTaglibXmlnsAttributes()
meth public org.xml.sax.Attributes getTaglibAttributes()
meth public void setAttributes(org.xml.sax.Attributes)
meth public void setBeginJavaLine(int)
meth public void setBody(org.netbeans.modules.web.jsps.parserapi.Node$Nodes)
meth public void setEndJavaLine(int)
supr java.lang.Object
hfds ZERO_VARIABLE_INFO,isDummy

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$AttributeDirective
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.Node

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$AttributeGenerator
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(org.netbeans.modules.web.jsps.parserapi.Mark,java.lang.String,org.netbeans.modules.web.jsps.parserapi.Node$CustomTag)
meth public java.lang.String getName()
meth public org.netbeans.modules.web.jsps.parserapi.Node$CustomTag getTag()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.Node
hfds name,tag

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$ChildInfo
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init()
meth public boolean hasIncludeAction()
meth public boolean hasParamAction()
meth public boolean hasScriptingVars()
meth public boolean hasSetProperty()
meth public boolean hasUseBean()
meth public boolean isScriptless()
meth public void setHasIncludeAction(boolean)
meth public void setHasParamAction(boolean)
meth public void setHasScriptingVars(boolean)
meth public void setHasSetProperty(boolean)
meth public void setHasUseBean(boolean)
meth public void setScriptless(boolean)
supr java.lang.Object
hfds hasIncludeAction,hasParamAction,hasScriptingVars,hasSetProperty,hasUseBean,scriptless

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$Comment
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.Node

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$CustomTag
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node,javax.servlet.jsp.tagext.TagFileInfo)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node,javax.servlet.jsp.tagext.TagInfo,java.lang.Class)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node,javax.servlet.jsp.tagext.TagFileInfo)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node,javax.servlet.jsp.tagext.TagInfo,java.lang.Class)
meth public boolean checkIfAttributeIsJspFragment(java.lang.String)
meth public boolean hasEmptyBody()
meth public boolean implementsBodyTag()
meth public boolean implementsDynamicAttributes()
meth public boolean implementsIterationTag()
meth public boolean implementsSimpleTag()
meth public boolean implementsTryCatchFinally()
meth public boolean isTagFile()
meth public int getCustomNestingLevel()
meth public java.lang.Class getTagHandlerClass()
meth public java.lang.Integer getNumCount()
meth public java.lang.String getPrefix()
meth public java.lang.String getTagHandlerPoolName()
meth public java.lang.String getURI()
meth public java.util.Vector getScriptingVars(int)
meth public javax.servlet.jsp.tagext.TagData getTagData()
meth public javax.servlet.jsp.tagext.TagFileInfo getTagFileInfo()
meth public javax.servlet.jsp.tagext.TagInfo getTagInfo()
meth public javax.servlet.jsp.tagext.TagVariableInfo[] getTagVariableInfos()
meth public javax.servlet.jsp.tagext.VariableInfo[] getVariableInfos()
meth public org.netbeans.modules.web.jsps.parserapi.Node$ChildInfo getChildInfo()
meth public org.netbeans.modules.web.jsps.parserapi.Node$CustomTag getCustomTagParent()
meth public org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute[] getJspAttributes()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
meth public void setCustomTagParent(org.netbeans.modules.web.jsps.parserapi.Node$CustomTag)
meth public void setJspAttributes(org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute[])
meth public void setNumCount(java.lang.Integer)
meth public void setScriptingVars(java.util.Vector,int)
meth public void setTagData(javax.servlet.jsp.tagext.TagData)
meth public void setTagHandlerClass(java.lang.Class)
meth public void setTagHandlerPoolName(java.lang.String)
supr org.netbeans.modules.web.jsps.parserapi.Node
hfds atBeginScriptingVars,atEndScriptingVars,childInfo,customNestingLevel,customTagParent,implementsBodyTag,implementsDynamicAttributes,implementsIterationTag,implementsSimpleTag,implementsTryCatchFinally,jspAttrs,nestedScriptingVars,numCount,prefix,tagData,tagFileInfo,tagHandlerClass,tagHandlerPoolName,tagInfo,uri,varInfos

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$Declaration
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.Node$ScriptingElement

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$DoBodyAction
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.Node

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$ELExpression
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public org.netbeans.modules.web.jsps.parserapi.ELNode$Nodes getEL()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
meth public void setEL(org.netbeans.modules.web.jsps.parserapi.ELNode$Nodes)
supr org.netbeans.modules.web.jsps.parserapi.Node
hfds el

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$Expression
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.Node$ScriptingElement

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$FallBackAction
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.Node

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$ForwardAction
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute getPage()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
meth public void setPage(org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute)
supr org.netbeans.modules.web.jsps.parserapi.Node
hfds page

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$GetProperty
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.Node

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$IncludeAction
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute getPage()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
meth public void setPage(org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute)
supr org.netbeans.modules.web.jsps.parserapi.Node
hfds page

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$IncludeDirective
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.Node

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$InvokeAction
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.Node

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute
 outer org.netbeans.modules.web.jsps.parserapi.Node
meth public boolean isDynamic()
meth public boolean isELInterpreterInput()
meth public boolean isExpression()
meth public boolean isLiteral()
meth public boolean isNamedAttribute()
meth public java.lang.String getLocalName()
meth public java.lang.String getName()
meth public java.lang.String getURI()
meth public java.lang.String getValue()
meth public org.netbeans.modules.web.jsps.parserapi.ELNode$Nodes getEL()
meth public org.netbeans.modules.web.jsps.parserapi.Node$NamedAttribute getNamedAttributeNode()
supr java.lang.Object
hfds dynamic,el,expression,localName,namedAttribute,namedAttributeNode,qName,uri,value

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$JspBody
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public org.netbeans.modules.web.jsps.parserapi.Node$ChildInfo getChildInfo()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.Node
hfds childInfo

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$JspElement
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute getNameAttribute()
meth public org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute[] getJspAttributes()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
meth public void setJspAttributes(org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute[])
meth public void setNameAttribute(org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute)
supr org.netbeans.modules.web.jsps.parserapi.Node
hfds jspAttrs,nameAttr

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$JspOutput
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.Node

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$JspRoot
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.Node

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$JspText
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.Node

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$NamedAttribute
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public boolean isTrim()
meth public java.lang.String getLocalName()
meth public java.lang.String getName()
meth public java.lang.String getPrefix()
meth public java.lang.String getText()
meth public org.netbeans.modules.web.jsps.parserapi.Node$ChildInfo getChildInfo()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.Node
hfds childInfo,localName,name,prefix,trim

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$Nodes
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init()
cons public init(java.util.List<org.netbeans.modules.web.jsps.parserapi.Node>)
cons public init(org.netbeans.modules.web.jsps.parserapi.Node$Root)
meth public int size()
meth public java.lang.String toString()
meth public org.netbeans.modules.web.jsps.parserapi.Node getNode(int)
meth public org.netbeans.modules.web.jsps.parserapi.Node$Root getRoot()
meth public void add(org.netbeans.modules.web.jsps.parserapi.Node)
meth public void remove(org.netbeans.modules.web.jsps.parserapi.Node)
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr java.lang.Object
hfds list,root

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$PageDirective
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public java.util.List<java.lang.String> getImports()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
meth public void addImport(java.lang.String)
supr org.netbeans.modules.web.jsps.parserapi.Node
hfds imports

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$ParamAction
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute getValue()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
meth public void setValue(org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute)
supr org.netbeans.modules.web.jsps.parserapi.Node
hfds value

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$ParamsAction
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.Node

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$PlugIn
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute getHeight()
meth public org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute getWidth()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
meth public void setHeight(org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute)
meth public void setWidth(org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute)
supr org.netbeans.modules.web.jsps.parserapi.Node
hfds height,width

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$Root
 outer org.netbeans.modules.web.jsps.parserapi.Node
meth public boolean isDefaultPageEncoding()
meth public boolean isEncodingSpecifiedInProlog()
meth public boolean isXmlSyntax()
meth public java.lang.String getJspConfigPageEncoding()
meth public java.lang.String getPageEncoding()
meth public org.netbeans.modules.web.jsps.parserapi.Node$Root getParentRoot()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
meth public void setIsDefaultPageEncoding(boolean)
meth public void setIsEncodingSpecifiedInProlog(boolean)
meth public void setJspConfigPageEncoding(java.lang.String)
meth public void setPageEncoding(java.lang.String)
supr org.netbeans.modules.web.jsps.parserapi.Node
hfds isDefaultPageEncoding,isEncodingSpecifiedInProlog,isXmlSyntax,jspConfigPageEnc,pageEnc,parentRoot

CLSS public abstract static org.netbeans.modules.web.jsps.parserapi.Node$ScriptingElement
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(java.lang.String,java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public java.lang.String getText()
supr org.netbeans.modules.web.jsps.parserapi.Node

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$Scriptlet
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.Node$ScriptingElement

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$SetProperty
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute getValue()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
meth public void setValue(org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute)
supr org.netbeans.modules.web.jsps.parserapi.Node
hfds value

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$TagDirective
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public java.util.List<java.lang.String> getImports()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
meth public void addImport(java.lang.String)
supr org.netbeans.modules.web.jsps.parserapi.Node
hfds imports

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$TaglibDirective
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.Node

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$TemplateText
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public boolean isAllSpace()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
meth public void ltrim()
meth public void rtrim()
supr org.netbeans.modules.web.jsps.parserapi.Node

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$UninterpretedTag
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute[] getJspAttributes()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
meth public void setJspAttributes(org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute[])
supr org.netbeans.modules.web.jsps.parserapi.Node
hfds jspAttrs

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$UseBean
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute getBeanName()
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
meth public void setBeanName(org.netbeans.modules.web.jsps.parserapi.Node$JspAttribute)
supr org.netbeans.modules.web.jsps.parserapi.Node
hfds beanName

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$VariableDirective
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init(java.lang.String,org.xml.sax.Attributes,org.xml.sax.Attributes,org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
cons public init(org.xml.sax.Attributes,org.netbeans.modules.web.jsps.parserapi.Mark,org.netbeans.modules.web.jsps.parserapi.Node)
meth public void accept(org.netbeans.modules.web.jsps.parserapi.Node$Visitor) throws javax.servlet.jsp.JspException
supr org.netbeans.modules.web.jsps.parserapi.Node

CLSS public static org.netbeans.modules.web.jsps.parserapi.Node$Visitor
 outer org.netbeans.modules.web.jsps.parserapi.Node
cons public init()
meth protected void doVisit(org.netbeans.modules.web.jsps.parserapi.Node) throws javax.servlet.jsp.JspException
meth protected void visitBody(org.netbeans.modules.web.jsps.parserapi.Node) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$AttributeDirective) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$AttributeGenerator) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$Comment) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$CustomTag) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$Declaration) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$DoBodyAction) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$ELExpression) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$Expression) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$FallBackAction) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$ForwardAction) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$GetProperty) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$IncludeAction) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$IncludeDirective) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$InvokeAction) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$JspBody) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$JspElement) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$JspOutput) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$JspRoot) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$JspText) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$NamedAttribute) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$PageDirective) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$ParamAction) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$ParamsAction) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$PlugIn) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$Root) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$Scriptlet) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$SetProperty) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$TagDirective) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$TaglibDirective) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$TemplateText) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$UninterpretedTag) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$UseBean) throws javax.servlet.jsp.JspException
meth public void visit(org.netbeans.modules.web.jsps.parserapi.Node$VariableDirective) throws javax.servlet.jsp.JspException
supr java.lang.Object

CLSS public abstract org.netbeans.modules.web.jsps.parserapi.PageInfo
cons public init(java.util.Map<java.lang.String,javax.servlet.jsp.tagext.TagLibraryInfo>,java.util.Map<java.lang.String,java.lang.String>,java.util.Map<java.lang.String,java.util.LinkedList<java.lang.String>>,java.util.Map,java.util.List<java.lang.String>,java.util.List<java.lang.String>,java.util.List,java.util.List,java.util.List<java.lang.String>,java.util.Set<java.lang.String>)
fld public final static java.lang.String JSP_SERVLET_BASE = "javax.servlet.http.HttpServlet"
innr public abstract interface static BeanData
meth public boolean containsPrefix(java.lang.String)
meth public boolean hasJspRoot()
meth public boolean hasTaglib(java.lang.String)
meth public boolean isAutoFlush()
meth public boolean isELIgnored()
meth public boolean isErrorPage()
meth public boolean isJspPrefixHijacked()
meth public boolean isPluginDeclared(java.lang.String)
meth public boolean isScriptingInvalid()
meth public boolean isScriptless()
meth public boolean isSession()
meth public boolean isTagFile()
meth public boolean isThreadSafe()
meth public int getBuffer()
meth public java.lang.String functionInfoToString(javax.servlet.jsp.tagext.FunctionInfo,java.lang.String)
meth public java.lang.String getAutoFlush()
meth public java.lang.String getBufferValue()
meth public java.lang.String getContentType()
meth public java.lang.String getDoctypeName()
meth public java.lang.String getDoctypePublic()
meth public java.lang.String getDoctypeSystem()
meth public java.lang.String getErrorPage()
meth public java.lang.String getExtends()
meth public java.lang.String getExtends(boolean)
meth public java.lang.String getInfo()
meth public java.lang.String getIsELIgnored()
meth public java.lang.String getIsErrorPage()
meth public java.lang.String getIsThreadSafe()
meth public java.lang.String getLanguage()
meth public java.lang.String getLanguage(boolean)
meth public java.lang.String getOmitXmlDecl()
meth public java.lang.String getSession()
meth public java.lang.String getURI(java.lang.String)
meth public java.lang.String tagFileToString(javax.servlet.jsp.tagext.TagFileInfo,java.lang.String)
meth public java.lang.String tagInfoToString(javax.servlet.jsp.tagext.TagInfo,java.lang.String)
meth public java.lang.String tagLibraryInfoToString(javax.servlet.jsp.tagext.TagLibraryInfo,java.lang.String)
meth public java.lang.String toString()
meth public java.util.Collection<javax.servlet.jsp.tagext.TagLibraryInfo> getTaglibs()
meth public java.util.List getIncludeCoda()
meth public java.util.List getIncludePrelude()
meth public java.util.List<java.lang.String> getDependants()
meth public java.util.List<java.lang.String> getImports()
meth public java.util.Map getApproxXmlPrefixMapper()
meth public java.util.Map getXMLPrefixMapper()
meth public java.util.Map<java.lang.String,java.lang.String> getJspPrefixMapper()
meth public java.util.Map<java.lang.String,javax.servlet.jsp.tagext.TagLibraryInfo> getTagLibraries()
meth public javax.servlet.jsp.tagext.TagInfo getTagInfo()
meth public javax.servlet.jsp.tagext.TagLibraryInfo getTaglib(java.lang.String)
meth public org.netbeans.modules.web.jsps.parserapi.PageInfo$BeanData[] getBeans()
meth public void addDependant(java.lang.String)
meth public void addImport(java.lang.String)
meth public void addImports(java.util.List<java.lang.String>)
meth public void addPrefix(java.lang.String)
meth public void addPrefixMapping(java.lang.String,java.lang.String)
meth public void addTaglib(java.lang.String,javax.servlet.jsp.tagext.TagLibraryInfo)
meth public void popPrefixMapping(java.lang.String)
meth public void pushPrefixMapping(java.lang.String,java.lang.String)
meth public void setAutoFlush(java.lang.String) throws javax.servlet.jsp.JspException
meth public void setBeans(org.netbeans.modules.web.jsps.parserapi.PageInfo$BeanData[])
meth public void setBufferValue(java.lang.String) throws javax.servlet.jsp.JspException
meth public void setContentType(java.lang.String)
meth public void setDoctypeName(java.lang.String)
meth public void setDoctypePublic(java.lang.String)
meth public void setDoctypeSystem(java.lang.String)
meth public void setELIgnored(boolean)
meth public void setErrorPage(java.lang.String)
meth public void setExtends(java.lang.String)
meth public void setHasJspRoot(boolean)
meth public void setIncludeCoda(java.util.Vector)
meth public void setIncludePrelude(java.util.Vector)
meth public void setInfo(java.lang.String)
meth public void setIsELIgnored(java.lang.String) throws javax.servlet.jsp.JspException
meth public void setIsErrorPage(java.lang.String) throws javax.servlet.jsp.JspException
meth public void setIsJspPrefixHijacked(boolean)
meth public void setIsThreadSafe(java.lang.String) throws javax.servlet.jsp.JspException
meth public void setLanguage(java.lang.String)
meth public void setOmitXmlDecl(java.lang.String)
meth public void setScriptingInvalid(boolean)
meth public void setScriptless(boolean)
meth public void setSession(java.lang.String) throws javax.servlet.jsp.JspException
meth public void setTagFile(boolean)
meth public void setTagInfo(javax.servlet.jsp.tagext.TagInfo)
supr java.lang.Object
hfds TAG_FILE_INFO_COMPARATOR,approxXmlPrefixMapper,autoFlush,beans,buffer,bufferValue,contentType,defaultExtends,defaultLanguage,dependants,doctypeName,doctypePublic,doctypeSystem,errorPage,hasJspRoot,imports,includeCoda,includePrelude,info,isAutoFlush,isELIgnored,isELIgnoredValue,isErrorPage,isErrorPageValue,isJspPrefixHijacked,isSession,isTagFile,isThreadSafe,isThreadSafeValue,jspPrefixMapper,language,omitXmlDecl,pluginDcls,prefixes,scriptingInvalid,scriptless,session,tagInfo,taglibsMap,xmlPrefixMapper,xtends

CLSS public abstract interface static org.netbeans.modules.web.jsps.parserapi.PageInfo$BeanData
 outer org.netbeans.modules.web.jsps.parserapi.PageInfo
meth public abstract java.lang.String getClassName()
meth public abstract java.lang.String getId()

CLSS public final org.netbeans.modules.web.jsps.parserapi.TldChangeEvent
cons public init(java.lang.Object,org.netbeans.modules.web.api.webmodule.WebModule)
meth public org.netbeans.modules.web.api.webmodule.WebModule getWebModule()
supr java.util.EventObject
hfds serialVersionUID,webModule

CLSS public abstract interface org.netbeans.modules.web.jsps.parserapi.TldChangeListener
intf java.util.EventListener
meth public abstract void tldChange(org.netbeans.modules.web.jsps.parserapi.TldChangeEvent)

