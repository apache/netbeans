/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.web.jsps.parserapi;

import java.util.*;
import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.JspException;
import org.xml.sax.Attributes;
//import org.apache.jasper.compiler.tagplugin.TagPluginContext;


/**
 * An internal data representation of a JSP page or a JSP docuement (XML).
 * Also included here is a visitor class for tranversing nodes.
 *
 * NOTE : Copied over from org.apache.jasper.compiler.Node.
 *
 * @author Kin-man Chung
 * @author Jan Luehe
 * @author Shawn Bayern
 * @author Mark Roth
 */

public abstract class Node {
    
    // BEGIN copied over from TagConstants
    public static final String JSP_URI = "http://java.sun.com/JSP/Page";

    public static final String DIRECTIVE_ACTION = "directive.";

    public static final String ROOT_ACTION = "root";
    public static final String JSP_ROOT_ACTION = "jsp:root";

    public static final String PAGE_DIRECTIVE_ACTION = "directive.page";
    public static final String JSP_PAGE_DIRECTIVE_ACTION = "jsp:directive.page";

    public static final String INCLUDE_DIRECTIVE_ACTION = "directive.include";
    public static final String JSP_INCLUDE_DIRECTIVE_ACTION = "jsp:directive.include";

    public static final String DECLARATION_ACTION = "declaration";
    public static final String JSP_DECLARATION_ACTION = "jsp:declaration";

    public static final String SCRIPTLET_ACTION = "scriptlet";
    public static final String JSP_SCRIPTLET_ACTION = "jsp:scriptlet";

    public static final String EXPRESSION_ACTION = "expression";
    public static final String JSP_EXPRESSION_ACTION = "jsp:expression";

    public static final String USE_BEAN_ACTION = "useBean";
    public static final String JSP_USE_BEAN_ACTION = "jsp:useBean";

    public static final String SET_PROPERTY_ACTION = "setProperty";
    public static final String JSP_SET_PROPERTY_ACTION = "jsp:setProperty";

    public static final String GET_PROPERTY_ACTION = "getProperty";
    public static final String JSP_GET_PROPERTY_ACTION = "jsp:getProperty";

    public static final String INCLUDE_ACTION = "include";
    public static final String JSP_INCLUDE_ACTION = "jsp:include";

    public static final String FORWARD_ACTION = "forward";
    public static final String JSP_FORWARD_ACTION = "jsp:forward";

    public static final String PARAM_ACTION = "param";
    public static final String JSP_PARAM_ACTION = "jsp:param";

    public static final String PARAMS_ACTION = "params";
    public static final String JSP_PARAMS_ACTION = "jsp:params";

    public static final String PLUGIN_ACTION = "plugin";
    public static final String JSP_PLUGIN_ACTION = "jsp:plugin";

    public static final String FALLBACK_ACTION = "fallback";
    public static final String JSP_FALLBACK_ACTION = "jsp:fallback";

    public static final String TEXT_ACTION = "text";
    public static final String JSP_TEXT_ACTION = "jsp:text";
    public static final String JSP_TEXT_ACTION_END = "</jsp:text>";

    public static final String ATTRIBUTE_ACTION = "attribute";
    public static final String JSP_ATTRIBUTE_ACTION = "jsp:attribute";

    public static final String BODY_ACTION = "body";
    public static final String JSP_BODY_ACTION = "jsp:body";

    public static final String ELEMENT_ACTION = "element";
    public static final String JSP_ELEMENT_ACTION = "jsp:element";

    public static final String OUTPUT_ACTION = "output";
    public static final String JSP_OUTPUT_ACTION = "jsp:output";

    public static final String TAGLIB_DIRECTIVE_ACTION = "taglib";
    public static final String JSP_TAGLIB_DIRECTIVE_ACTION = "jsp:taglib";

    /*
     * Tag Files
     */
    public static final String INVOKE_ACTION = "invoke";
    public static final String JSP_INVOKE_ACTION = "jsp:invoke";

    public static final String DOBODY_ACTION = "doBody";
    public static final String JSP_DOBODY_ACTION = "jsp:doBody";

    /*
     * Tag File Directives
     */
    public static final String TAG_DIRECTIVE_ACTION = "directive.tag";
    public static final String JSP_TAG_DIRECTIVE_ACTION = "jsp:directive.tag";

    public static final String ATTRIBUTE_DIRECTIVE_ACTION = "directive.attribute";
    public static final String JSP_ATTRIBUTE_DIRECTIVE_ACTION = "jsp:directive.attribute";

    public static final String VARIABLE_DIRECTIVE_ACTION = "directive.variable";
    public static final String JSP_VARIABLE_DIRECTIVE_ACTION = "jsp:directive.variable";

    /*
     * Directive attributes
     */
    public static final String URN_JSPTAGDIR = "urn:jsptagdir:";
    public static final String URN_JSPTLD = "urn:jsptld:";
    // END copied over from TagConstants

    
    private static final VariableInfo[] ZERO_VARIABLE_INFO = { };
    
    protected Attributes attrs;

    // xmlns attributes that represent tag libraries (only in XML syntax)
    protected Attributes taglibAttrs;

    /*
     * xmlns attributes that do not represent tag libraries
     * (only in XML syntax)
     */
    protected Attributes nonTaglibXmlnsAttrs;

    protected Nodes body;
    protected String text;
    protected Mark startMark;
    protected int beginJavaLine;
    protected int endJavaLine;
    protected Node parent;
    protected Nodes namedAttributeNodes; // cached for performance
    protected String qName;
    protected String localName;

    private boolean isDummy;

    /**
     * Zero-arg Constructor.
     */
    Node() {
	this.isDummy = true;
    }

    /**
     * Constructor.
     *
     * @param start The location of the jsp page
     * @param parent The enclosing node
     */
    Node(Mark start, Node parent) {
	this.startMark = start;
	this.isDummy = (start == null);
	addToParent(parent);
    }

    /**
     * Constructor.
     *
     * @param qName The action's qualified name
     * @param localName The action's local name
     * @param start The location of the jsp page
     * @param parent The enclosing node
     */
    Node(String qName, String localName, Mark start, Node parent) {
	this.qName = qName;
	this.localName = localName;
	this.startMark = start;
	this.isDummy = (start == null);
	addToParent(parent);
    }

    /**
     * Constructor for Nodes parsed from standard syntax.
     *
     * @param qName The action's qualified name
     * @param localName The action's local name
     * @param attrs The attributes for this node
     * @param start The location of the jsp page
     * @param parent The enclosing node
     */
    Node(String qName, String localName, Attributes attrs, Mark start, Node parent) {
	this.qName = qName;
	this.localName = localName;
	this.attrs = attrs;
	this.startMark = start;
	this.isDummy = (start == null);
	addToParent(parent);
    }

    /**
     * Constructor for Nodes parsed from XML syntax.
     *
     * @param qName The action's qualified name
     * @param localName The action's local name
     * @param attrs The action's attributes whose name does not start with
     * xmlns
     * @param nonTaglibXmlnsAttrs The action's xmlns attributes that do not
     * represent tag libraries
     * @param taglibAttrs The action's xmlns attributes that represent tag
     * libraries
     * @param start The location of the jsp page
     * @param parent The enclosing node
     */
    Node(String qName, String localName, Attributes attrs, Attributes nonTaglibXmlnsAttrs, Attributes taglibAttrs, Mark start, Node parent) {
	this.qName = qName;
	this.localName = localName;
	this.attrs = attrs;
	this.nonTaglibXmlnsAttrs = nonTaglibXmlnsAttrs;
	this.taglibAttrs = taglibAttrs;
	this.startMark = start;
	this.isDummy = (start == null);
	addToParent(parent);
    }

    /*
     * Constructor.
     *
     * @param qName The action's qualified name
     * @param localName The action's local name
     * @param text The text associated with this node
     * @param start The location of the jsp page
     * @param parent The enclosing node
     */
    Node(String qName, String localName, String text, Mark start, Node parent) {
	this.qName = qName;
	this.localName = localName;
	this.text = text;
	this.startMark = start;
	this.isDummy = (start == null);
	addToParent(parent);
    }

    public String getQName() {
	return this.qName;
    }

    public String getLocalName() {
	return this.localName;
    }

    /*
     * Gets this Node's attributes.
     *
     * In the case of a Node parsed from standard syntax, this method returns
     * all the Node's attributes.
     *
     * In the case of a Node parsed from XML syntax, this method returns only
     * those attributes whose name does not start with xmlns.
     */
    public Attributes getAttributes() {
	return this.attrs;
    }

    /*
     * Gets this Node's xmlns attributes that represent tag libraries
     * (only meaningful for Nodes parsed from XML syntax)
     */
    public Attributes getTaglibAttributes() {
	return this.taglibAttrs;
    }

    /*
     * Gets this Node's xmlns attributes that do not represent tag libraries
     * (only meaningful for Nodes parsed from XML syntax)
     */
    public Attributes getNonTaglibXmlnsAttributes() {
	return this.nonTaglibXmlnsAttrs;
    }

    public void setAttributes(Attributes attrs) {
	this.attrs = attrs;
    }

    public String getAttributeValue(String name) {
	return (attrs == null) ? null : attrs.getValue(name);
    }

    /**
     * Get the attribute that is non request time expression, either
     * from the attribute of the node, or from a jsp:attrbute 
     */
    public String getTextAttribute(String name) {

	String attr = getAttributeValue(name);
	if (attr != null) {
	    return attr;
	}

	NamedAttribute namedAttribute = getNamedAttributeNode(name);
	if (namedAttribute == null) {
	    return null;
	}

	return namedAttribute.getText();
    }

    /**
     * Searches all subnodes of this node for jsp:attribute standard
     * actions with the given name, and returns the NamedAttribute node
     * of the matching named attribute, nor null if no such node is found.
     * <p>
     * This should always be called and only be called for nodes that
     * accept dynamic runtime attribute expressions.
     */
    public NamedAttribute getNamedAttributeNode( String name ) {
        NamedAttribute result = null;
        
        // Look for the attribute in NamedAttribute children
        Nodes nodes = getNamedAttributeNodes();
        int numChildNodes = nodes.size();
        for( int i = 0; i < numChildNodes; i++ ) {
            NamedAttribute na = (NamedAttribute)nodes.getNode( i );
	    boolean found = false;
	    int index = name.indexOf(':');
	    if (index != -1) {
		// qualified name
		found = na.getName().equals(name);
	    } else {
		found = na.getLocalName().equals(name);
	    }
	    if (found) {
                result = na;
                break;
            }
        }
        
        return result;
    }

    /**
     * Searches all subnodes of this node for jsp:attribute standard
     * actions, and returns that set of nodes as a Node.Nodes object.
     *
     * @return Possibly empty Node.Nodes object containing any jsp:attribute
     * subnodes of this Node
     */
    public Node.Nodes getNamedAttributeNodes() {

	if (namedAttributeNodes != null) {
	    return namedAttributeNodes;
	}

        Node.Nodes result = new Node.Nodes();
        
        // Look for the attribute in NamedAttribute children
        Nodes nodes = getBody();
        if( nodes != null ) {
            int numChildNodes = nodes.size();
            for( int i = 0; i < numChildNodes; i++ ) {
                Node n = nodes.getNode( i );
                if( n instanceof NamedAttribute ) {
                    result.add( n );
                }
                else {
                    // Nothing can come before jsp:attribute, and only
                    // jsp:body can come after it.
                    break;
                }
            }
        }

	namedAttributeNodes = result;
        return result;
    }
    
    public Nodes getBody() {
	return body;
    }

    public void setBody(Nodes body) {
	this.body = body;
    }

    public String getText() {
	return text;
    }

    public Mark getStart() {
	return startMark;
    }

    public Node getParent() {
	return parent;
    }

    public int getBeginJavaLine() {
	return beginJavaLine;
    }

    public void setBeginJavaLine(int begin) {
	beginJavaLine = begin;
    }

    public int getEndJavaLine() {
	return endJavaLine;
    }

    public void setEndJavaLine(int end) {
	endJavaLine = end;
    }

    public boolean isDummy() {
	return isDummy;
    }

    public Node.Root getRoot() {
	Node n = this;
	while (!(n instanceof Node.Root)) {
	    n = n.getParent();
	}
	return (Node.Root) n;
    }

    /**
     * Selects and invokes a method in the visitor class based on the node
     * type.  This is abstract and should be overrode by the extending classes.
     * @param v The visitor class
     */
    abstract void accept(Visitor v) throws JspException;


    //*********************************************************************
    // Private utility methods

    /*
     * Adds this Node to the body of the given parent.
     */
    private void addToParent(Node parent) {
	if (parent != null) {
	    this.parent = parent;
	    Nodes parentBody = parent.getBody();
	    if (parentBody == null) {
		parentBody = new Nodes();
		parent.setBody(parentBody);
	    }
	    parentBody.add(this);
	}
    }


    /*********************************************************************
     * Child classes
     */
    
    /**
     * Represents the root of a Jsp page or Jsp document
     */
    public static class Root extends Node {

	private Root parentRoot;
	private boolean isXmlSyntax;

	// Source encoding of the page containing this Root
	private String pageEnc;
	
	// Page encoding specified in JSP config element
	private String jspConfigPageEnc;

	/*
	 * Flag indicating if the default page encoding is being used (only
	 * applicable with standard syntax).
	 *
	 * True if the page does not provide a page directive with a
	 * 'contentType' attribute (or the 'contentType' attribute doesn't
	 * have a CHARSET value), the page does not provide a page directive
	 * with a 'pageEncoding' attribute, and there is no JSP configuration
	 * element page-encoding whose URL pattern matches the page.
	 */
	private boolean isDefaultPageEncoding;

	/*
	 * Indicates whether an encoding has been explicitly specified in the
	 * page's XML prolog (only used for pages in XML syntax).
	 * This information is used to decide whether a translation error must
	 * be reported for encoding conflicts.
	 */
	private boolean isEncodingSpecifiedInProlog;

	/*
	 * Constructor.
	 */
	Root(Mark start, Node parent, boolean isXmlSyntax) {
	    super(start, parent);
	    this.isXmlSyntax = isXmlSyntax;
	    this.qName = JSP_ROOT_ACTION;
	    this.localName = ROOT_ACTION;

	    // Figure out and set the parent root
	    Node r = parent;
	    while ((r != null) && !(r instanceof Node.Root))
		r = r.getParent();
	    parentRoot = (Node.Root) r;
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}

	public boolean isXmlSyntax() {
	    return isXmlSyntax;
	}

	/*
	 * Sets the encoding specified in the JSP config element whose URL
	 * pattern matches the page containing this Root.
	 */
	public void setJspConfigPageEncoding(String enc) {
	    jspConfigPageEnc = enc;
	}

	/*
	 * Gets the encoding specified in the JSP config element whose URL
	 * pattern matches the page containing this Root.
	 */
	public String getJspConfigPageEncoding() {
	    return jspConfigPageEnc;
	}

	public void setPageEncoding(String enc) {
	    pageEnc = enc;
	}

	public String getPageEncoding() {
	    return pageEnc;
	}

	public void setIsDefaultPageEncoding(boolean isDefault) {
	    isDefaultPageEncoding = isDefault;
	}

	public boolean isDefaultPageEncoding() {
	    return isDefaultPageEncoding;
	}
	
	public void setIsEncodingSpecifiedInProlog(boolean isSpecified) {
	    isEncodingSpecifiedInProlog = isSpecified;
	}

	public boolean isEncodingSpecifiedInProlog() {
	    return isEncodingSpecifiedInProlog;
	}

	/**
	 * @return The enclosing root to this Root. Usually represents the
	 * page that includes this one.
	 */
	public Root getParentRoot() {
	    return parentRoot;
	}
    }
    
    /**
     * Represents the root of a Jsp document (XML syntax)
     */
    public static class JspRoot extends Node {

	public JspRoot(String qName, Attributes attrs,
		       Attributes nonTaglibXmlnsAttrs, Attributes taglibAttrs,
		       Mark start, Node parent) {
	    super(qName, ROOT_ACTION, attrs, nonTaglibXmlnsAttrs, taglibAttrs,
		  start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}
    }

    /**
     * Represents a page directive
     */
    public static class PageDirective extends Node {

	private Vector<String> imports;

	public PageDirective(Attributes attrs, Mark start, Node parent) {
	    this(JSP_PAGE_DIRECTIVE_ACTION, attrs, null, null, start, parent);
	}

	public PageDirective(String qName, Attributes attrs,
			     Attributes nonTaglibXmlnsAttrs,
			     Attributes taglibAttrs, Mark start, Node parent) {
	    super(qName, PAGE_DIRECTIVE_ACTION, attrs, nonTaglibXmlnsAttrs,
		  taglibAttrs, start, parent);
	    imports = new Vector<String>();
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}

	/**
	 * Parses the comma-separated list of class or package names in the
	 * given attribute value and adds each component to this
	 * PageDirective's vector of imported classes and packages.
	 * @param value A comma-separated string of imports.
	 */
	public void addImport(String value) {
	    int start = 0;
	    int index;
	    while ((index = value.indexOf(',', start)) != -1) {
		imports.add(value.substring(start, index).trim());
		start = index + 1;
	    }
	    if (start == 0) {
		// No comma found
		imports.add(value.trim());
	    } else {
		imports.add(value.substring(start).trim());
	    }
	}

	public List<String> getImports() {
	    return imports;
	}
    }

    /**
     * Represents an include directive
     */
    public static class IncludeDirective extends Node {

	public IncludeDirective(Attributes attrs, Mark start, Node parent) {
	    this(JSP_INCLUDE_DIRECTIVE_ACTION, attrs, null, null, start,
		 parent);
	}

	public IncludeDirective(String qName, Attributes attrs,
				Attributes nonTaglibXmlnsAttrs,
				Attributes taglibAttrs, Mark start,
				Node parent) {
	    super(qName, INCLUDE_DIRECTIVE_ACTION, attrs, nonTaglibXmlnsAttrs,
		  taglibAttrs, start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}
    }

    /**
     * Represents a custom taglib directive
     */
    public static class TaglibDirective extends Node {

	public TaglibDirective(Attributes attrs, Mark start, Node parent) {
	    super(JSP_TAGLIB_DIRECTIVE_ACTION, TAGLIB_DIRECTIVE_ACTION, attrs,
		  start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}
    }

    /**
     * Represents a tag directive
     */
    public static class TagDirective extends Node {
        private Vector<String> imports;

	public TagDirective(Attributes attrs, Mark start, Node parent) {
	    this(JSP_TAG_DIRECTIVE_ACTION, attrs, null, null, start, parent);
	}

	public TagDirective(String qName, Attributes attrs,
			    Attributes nonTaglibXmlnsAttrs,
			    Attributes taglibAttrs, Mark start, Node parent) {
	    super(qName, TAG_DIRECTIVE_ACTION, attrs, nonTaglibXmlnsAttrs,
		  taglibAttrs, start, parent);
            imports = new Vector<String>();
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
        }
 
        /**
         * Parses the comma-separated list of class or package names in the
         * given attribute value and adds each component to this
         * PageDirective's vector of imported classes and packages.
         * @param value A comma-separated string of imports.
         */
        public void addImport(String value) {
            int start = 0;
            int index;
            while ((index = value.indexOf(',', start)) != -1) {
                imports.add(value.substring(start, index).trim());
                start = index + 1;
            }
            if (start == 0) {
                // No comma found
                imports.add(value.trim());
            } else {
                imports.add(value.substring(start).trim());
            }
        }
 
        public List<String> getImports() {
            return imports;
	}
    }

    /**
     * Represents an attribute directive
     */
    public static class AttributeDirective extends Node {

	public AttributeDirective(Attributes attrs, Mark start, Node parent) {
	    this(JSP_ATTRIBUTE_DIRECTIVE_ACTION, attrs, null, null, start,
		 parent);
	}

	public AttributeDirective(String qName, Attributes attrs,
				  Attributes nonTaglibXmlnsAttrs,
				  Attributes taglibAttrs, Mark start,
				  Node parent) {
	    super(qName, ATTRIBUTE_DIRECTIVE_ACTION, attrs,
		  nonTaglibXmlnsAttrs, taglibAttrs, start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}
    }

    /**
     * Represents a variable directive
     */
    public static class VariableDirective extends Node {

	public VariableDirective(Attributes attrs, Mark start, Node parent) {
	    this(JSP_VARIABLE_DIRECTIVE_ACTION, attrs, null, null, start,
		 parent);
	}

	public VariableDirective(String qName, Attributes attrs,
				 Attributes nonTaglibXmlnsAttrs,
				 Attributes taglibAttrs,
				 Mark start, Node parent) {
	    super(qName, VARIABLE_DIRECTIVE_ACTION, attrs, nonTaglibXmlnsAttrs,
		  taglibAttrs, start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}
    }

    /**
     * Represents a <jsp:invoke> tag file action
     */
    public static class InvokeAction extends Node {

	public InvokeAction(Attributes attrs, Mark start, Node parent) {
	    this(JSP_INVOKE_ACTION, attrs, null, null, start, parent);
	}

	public InvokeAction(String qName, Attributes attrs,
			    Attributes nonTaglibXmlnsAttrs,
			    Attributes taglibAttrs, Mark start, Node parent) {
	    super(qName, INVOKE_ACTION, attrs, nonTaglibXmlnsAttrs,
		  taglibAttrs, start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}
    }

    /**
     * Represents a <jsp:doBody> tag file action
     */
    public static class DoBodyAction extends Node {

	public DoBodyAction(Attributes attrs, Mark start, Node parent) {
	    this(JSP_DOBODY_ACTION, attrs, null, null, start, parent);
	}

	public DoBodyAction(String qName, Attributes attrs,
			    Attributes nonTaglibXmlnsAttrs,
			    Attributes taglibAttrs, Mark start, Node parent) {
	    super(qName, DOBODY_ACTION, attrs, nonTaglibXmlnsAttrs,
		  taglibAttrs, start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}
    }

    /**
     * Represents a Jsp comment
     * Comments are kept for completeness.
     */
    public static class Comment extends Node {

	public Comment(String text, Mark start, Node parent) {
	    super(null, null, text, start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}
    }

    /**
     * Represents an expression, declaration, or scriptlet
     */
    public abstract static class ScriptingElement extends Node {

	public ScriptingElement(String qName, String localName, String text,
				Mark start, Node parent) {
	    super(qName, localName, text, start, parent);
	}

	public ScriptingElement(String qName, String localName,
				Attributes nonTaglibXmlnsAttrs,
				Attributes taglibAttrs, Mark start,
				Node parent) {
	    super(qName, localName, null, nonTaglibXmlnsAttrs, taglibAttrs,
		  start, parent);
	}

	/**
	 * When this node was created from a JSP page in JSP syntax, its text
	 * was stored as a String in the "text" field, whereas when this node
	 * was created from a JSP document, its text was stored as one or more
	 * TemplateText nodes in its body. This method handles either case.
	 * @return The text string
	 */
	public String getText() {
	    String ret = text;
	    if ((ret == null) && (body != null)) {
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<body.size(); i++) {
		    buf.append(body.getNode(i).getText());
		}
		ret = buf.toString();
	    }
	    return ret;
	}
    }

    /**
     * Represents a declaration
     */
    public static class Declaration extends ScriptingElement {

	public Declaration(String text, Mark start, Node parent) {
	    super(JSP_DECLARATION_ACTION, DECLARATION_ACTION, text, start,
		  parent);
	}

	public Declaration(String qName, Attributes nonTaglibXmlnsAttrs,
			   Attributes taglibAttrs, Mark start,
			   Node parent) {
	    super(qName, DECLARATION_ACTION, nonTaglibXmlnsAttrs,
		  taglibAttrs, start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}
    }

    /**
     * Represents an expression.  Expressions in attributes are embedded
     * in the attribute string and not here.
     */
    public static class Expression extends ScriptingElement {

	public Expression(String text, Mark start, Node parent) {
	    super(JSP_EXPRESSION_ACTION, EXPRESSION_ACTION, text, start,
		  parent);
	}

	public Expression(String qName, Attributes nonTaglibXmlnsAttrs,
			  Attributes taglibAttrs, Mark start,
			  Node parent) {
	    super(qName, EXPRESSION_ACTION, nonTaglibXmlnsAttrs, taglibAttrs,
		  start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}
    }

    /**
     * Represents a scriptlet
     */
    public static class Scriptlet extends ScriptingElement {

	public Scriptlet(String text, Mark start, Node parent) {
	    super(JSP_SCRIPTLET_ACTION, SCRIPTLET_ACTION, text, start, parent);
	}

	public Scriptlet(String qName, Attributes nonTaglibXmlnsAttrs,
			 Attributes taglibAttrs, Mark start,
			 Node parent) {
	    super(qName, SCRIPTLET_ACTION, nonTaglibXmlnsAttrs, taglibAttrs,
		  start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}
    }

    /**
     * Represents an EL expression.  Expressions in attributes are embedded
     * in the attribute string and not here.
     */
    public static class ELExpression extends Node {

	private ELNode.Nodes el;

        public ELExpression(String text, Mark start, Node parent) {
            super(null, null, text, start, parent);
        }

        public void accept(Visitor v) throws JspException {
            v.visit(this);
        }

	public void setEL(ELNode.Nodes el) {
	    this.el = el;
	}

	public ELNode.Nodes getEL() {
	    return el;
	}
    }

    /**
     * Represents a param action
     */
    public static class ParamAction extends Node {

	JspAttribute value;

	public ParamAction(Attributes attrs, Mark start, Node parent) {
	    this(JSP_PARAM_ACTION, attrs, null, null, start, parent);
	}

	public ParamAction(String qName, Attributes attrs,
			   Attributes nonTaglibXmlnsAttrs,
			   Attributes taglibAttrs, Mark start, Node parent) {
	    super(qName, PARAM_ACTION, attrs, nonTaglibXmlnsAttrs,
		  taglibAttrs, start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}

	public void setValue(JspAttribute value) {
	    this.value = value;
	}

	public JspAttribute getValue() {
	    return value;
	}
    }

    /**
     * Represents a params action
     */
    public static class ParamsAction extends Node {

	public ParamsAction(Mark start, Node parent) {
	    this(JSP_PARAMS_ACTION, null, null, start, parent);
	}

	public ParamsAction(String qName,
			    Attributes nonTaglibXmlnsAttrs,
			    Attributes taglibAttrs,
			    Mark start, Node parent) {
	    super(qName, PARAMS_ACTION, null, nonTaglibXmlnsAttrs, taglibAttrs,
		  start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}
    }

    /**
     * Represents a fallback action
     */
    public static class FallBackAction extends Node {

	public FallBackAction(Mark start, Node parent) {
	    this(JSP_FALLBACK_ACTION, null, null, start, parent);
	}

	public FallBackAction(String qName,
			      Attributes nonTaglibXmlnsAttrs,
			      Attributes taglibAttrs, Mark start,
			      Node parent) {
	    super(qName, FALLBACK_ACTION, null, nonTaglibXmlnsAttrs,
		  taglibAttrs, start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}
    }

    /**
     * Represents an include action
     */
    public static class IncludeAction extends Node {

	private JspAttribute page;

	public IncludeAction(Attributes attrs, Mark start, Node parent) {
	    this(JSP_INCLUDE_ACTION, attrs, null, null, start, parent);
	}

	public IncludeAction(String qName, Attributes attrs,
			     Attributes nonTaglibXmlnsAttrs,
			     Attributes taglibAttrs, Mark start, Node parent) {
	    super(qName, INCLUDE_ACTION, attrs, nonTaglibXmlnsAttrs,
		  taglibAttrs, start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}

	public void setPage(JspAttribute page) {
	    this.page = page;
	}

	public JspAttribute getPage() {
	    return page;
	}
    }

    /**
     * Represents a forward action
     */
    public static class ForwardAction extends Node {

	private JspAttribute page;

	public ForwardAction(Attributes attrs, Mark start, Node parent) {
	    this(JSP_FORWARD_ACTION, attrs, null, null, start, parent);
	}

	public ForwardAction(String qName, Attributes attrs,
			     Attributes nonTaglibXmlnsAttrs,
			     Attributes taglibAttrs, Mark start, Node parent) {
	    super(qName, FORWARD_ACTION, attrs, nonTaglibXmlnsAttrs,
		  taglibAttrs, start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}

	public void setPage(JspAttribute page) {
	    this.page = page;
	}

	public JspAttribute getPage() {
	    return page;
	}
    }

    /**
     * Represents a getProperty action
     */
    public static class GetProperty extends Node {

	public GetProperty(Attributes attrs, Mark start, Node parent) {
	    this(JSP_GET_PROPERTY_ACTION, attrs, null, null, start, parent);
	}

	public GetProperty(String qName, Attributes attrs,
			   Attributes nonTaglibXmlnsAttrs,
			   Attributes taglibAttrs, Mark start, Node parent) {
	    super(qName, GET_PROPERTY_ACTION, attrs, nonTaglibXmlnsAttrs,
		  taglibAttrs, start,  parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}
    }

    /**
     * Represents a setProperty action
     */
    public static class SetProperty extends Node {

	private JspAttribute value;

	public SetProperty(Attributes attrs, Mark start, Node parent) {
	    this(JSP_SET_PROPERTY_ACTION, attrs, null, null, start, parent);
	}

	public SetProperty(String qName, Attributes attrs,
			   Attributes nonTaglibXmlnsAttrs,
			   Attributes taglibAttrs, Mark start, Node parent) {
	    super(qName, SET_PROPERTY_ACTION, attrs, nonTaglibXmlnsAttrs,
		  taglibAttrs, start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}

	public void setValue(JspAttribute value) {
	    this.value = value;
	}

	public JspAttribute getValue() {
	    return value;
	}
    }

    /**
     * Represents a useBean action
     */
    public static class UseBean extends Node {

	JspAttribute beanName;

	public UseBean(Attributes attrs, Mark start, Node parent) {
	    this(JSP_USE_BEAN_ACTION, attrs, null, null, start, parent);
	}

	public UseBean(String qName, Attributes attrs,
		       Attributes nonTaglibXmlnsAttrs, Attributes taglibAttrs,
		       Mark start, Node parent) {
	    super(qName, USE_BEAN_ACTION, attrs, nonTaglibXmlnsAttrs,
		  taglibAttrs, start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}

	public void setBeanName(JspAttribute beanName) {
	    this.beanName = beanName;
	}

	public JspAttribute getBeanName() {
	    return beanName;
	}
    }

    /**
     * Represents a plugin action
     */
    public static class PlugIn extends Node {

        private JspAttribute width;
        private JspAttribute height;
        
	public PlugIn(Attributes attrs, Mark start, Node parent) {
	    this(JSP_PLUGIN_ACTION, attrs, null, null, start, parent);
	}

	public PlugIn(String qName, Attributes attrs,
		      Attributes nonTaglibXmlnsAttrs, Attributes taglibAttrs,
		      Mark start, Node parent) {
	    super(qName, PLUGIN_ACTION, attrs, nonTaglibXmlnsAttrs,
		  taglibAttrs, start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}

	public void setHeight(JspAttribute height) {
	    this.height = height;
	}

	public void setWidth(JspAttribute width) {
	    this.width = width;
	}

	public JspAttribute getHeight() {
	    return height;
	}

	public JspAttribute getWidth() {
	    return width;
	}
    }

    /**
     * Represents an uninterpreted tag, from a Jsp document
     */
    public static class UninterpretedTag extends Node {

	private JspAttribute[] jspAttrs;

	public UninterpretedTag(String qName, String localName,
				Attributes attrs,
				Attributes nonTaglibXmlnsAttrs,
				Attributes taglibAttrs,
				Mark start, Node parent) {
	    super(qName, localName, attrs, nonTaglibXmlnsAttrs, taglibAttrs,
		  start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}

	public void setJspAttributes(JspAttribute[] jspAttrs) {
	    this.jspAttrs = jspAttrs;
	}

	public JspAttribute[] getJspAttributes() {
	    return jspAttrs;
	}
    }
    
    /**
     * Represents a <jsp:element>.
     */
    public static class JspElement extends Node {

	private JspAttribute[] jspAttrs;
	private JspAttribute nameAttr;

	public JspElement(Attributes attrs, Mark start, Node parent) {
	    this(JSP_ELEMENT_ACTION, attrs, null, null, start, parent);
	}

	public JspElement(String qName, Attributes attrs,
			  Attributes nonTaglibXmlnsAttrs,
			  Attributes taglibAttrs, Mark start, Node parent) {
	    super(qName, ELEMENT_ACTION, attrs, nonTaglibXmlnsAttrs,
		  taglibAttrs, start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}

	public void setJspAttributes(JspAttribute[] jspAttrs) {
	    this.jspAttrs = jspAttrs;
	}

	public JspAttribute[] getJspAttributes() {
	    return jspAttrs;
	}

	/*
	 * Sets the XML-style 'name' attribute
	 */
	public void setNameAttribute(JspAttribute nameAttr) {
	    this.nameAttr = nameAttr;
	}

	/*
	 * Gets the XML-style 'name' attribute
	 */
	public JspAttribute getNameAttribute() {
	    return this.nameAttr;
	}
    }

    /**
     * Represents a <jsp:output>.
     */
    public static class JspOutput extends Node {

	public JspOutput(String qName, Attributes attrs,
			 Attributes nonTaglibXmlnsAttrs,
			 Attributes taglibAttrs,
			 Mark start, Node parent) {
	    super(qName, OUTPUT_ACTION, attrs, nonTaglibXmlnsAttrs,
		  taglibAttrs, start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}
    }

    /**
     * Collected information about child elements.  Used by nodes like
     * CustomTag, JspBody, and NamedAttribute.  The information is 
     * set in the Collector.
     */
    public static class ChildInfo {
	private boolean scriptless;	// true if the tag and its body
					// contain no scripting elements.
	private boolean hasUseBean;
	private boolean hasIncludeAction;
	private boolean hasParamAction;
	private boolean hasSetProperty;
	private boolean hasScriptingVars;

	public void setScriptless(boolean s) {
	    scriptless = s;
	}

	public boolean isScriptless() {
	    return scriptless;
	}

	public void setHasUseBean(boolean u) {
	    hasUseBean = u;
	}

	public boolean hasUseBean() {
	    return hasUseBean;
	}

	public void setHasIncludeAction(boolean i) {
	    hasIncludeAction = i;
	}

	public boolean hasIncludeAction() {
	    return hasIncludeAction;
	}

	public void setHasParamAction(boolean i) {
	    hasParamAction = i;
	}

	public boolean hasParamAction() {
	    return hasParamAction;
	}

	public void setHasSetProperty(boolean s) {
	    hasSetProperty = s;
	}

	public boolean hasSetProperty() {
	    return hasSetProperty;
	}
        
	public void setHasScriptingVars(boolean s) {
	    hasScriptingVars = s;
	}

	public boolean hasScriptingVars() {
	    return hasScriptingVars;
	}
    }

    /**
     * Represents a custom tag
     */
    public static class CustomTag extends Node {

	private String uri;
	private String prefix;
	private JspAttribute[] jspAttrs;
	private TagData tagData;
	private String tagHandlerPoolName;
	private TagInfo tagInfo;
	private TagFileInfo tagFileInfo;
	private Class tagHandlerClass;
	private VariableInfo[] varInfos;
	private int customNestingLevel;
        private ChildInfo childInfo;
	private boolean implementsIterationTag;
	private boolean implementsBodyTag;
	private boolean implementsTryCatchFinally;
	private boolean implementsSimpleTag;
	private boolean implementsDynamicAttributes;
	private Vector atBeginScriptingVars;
	private Vector atEndScriptingVars;
	private Vector nestedScriptingVars;
	private Node.CustomTag customTagParent;
	private Integer numCount;
	//private boolean useTagPlugin;
	//private TagPluginContext tagPluginContext;

	/**
	 * The following two fields are used for holding the Java
	 * scriptlets that the tag plugins may generate.  Meaningful
	 * only if useTagPlugin is true;
	 * Could move them into TagPluginContextImpl, but we'll need
	 * to cast tagPluginContext to TagPluginContextImpl all the time...
	 */
	//private Nodes atSTag;
	//private Nodes atETag;

	/*
	 * Constructor for custom action implemented by tag handler.
	 */
	public CustomTag(String qName, String prefix, String localName,
			 String uri, Attributes attrs, Mark start, Node parent,
			 TagInfo tagInfo, Class tagHandlerClass) {
	    this(qName, prefix, localName, uri, attrs, null, null, start,
		 parent, tagInfo, tagHandlerClass);
	}

	/*
	 * Constructor for custom action implemented by tag handler.
	 */
	public CustomTag(String qName, String prefix, String localName,
			 String uri, Attributes attrs,
			 Attributes nonTaglibXmlnsAttrs,
			 Attributes taglibAttrs,
			 Mark start, Node parent, TagInfo tagInfo,
			 Class tagHandlerClass) {
	    super(qName, localName, attrs, nonTaglibXmlnsAttrs, taglibAttrs,
		  start, parent);

	    this.uri = uri;
	    this.prefix = prefix;
	    this.tagInfo = tagInfo;
	    this.tagHandlerClass = tagHandlerClass;
	    this.customNestingLevel = makeCustomNestingLevel();
            this.childInfo = new ChildInfo();

	    this.implementsIterationTag = 
		IterationTag.class.isAssignableFrom(tagHandlerClass);
	    this.implementsBodyTag =
		BodyTag.class.isAssignableFrom(tagHandlerClass);
	    this.implementsTryCatchFinally = 
		TryCatchFinally.class.isAssignableFrom(tagHandlerClass);
	    this.implementsSimpleTag = 
		SimpleTag.class.isAssignableFrom(tagHandlerClass);
	    this.implementsDynamicAttributes = 
		DynamicAttributes.class.isAssignableFrom(tagHandlerClass);
	}

	/*
	 * Constructor for custom action implemented by tag file.
	 */
	public CustomTag(String qName, String prefix, String localName,
			 String uri, Attributes attrs, Mark start, Node parent,
			 TagFileInfo tagFileInfo) {
	    this(qName, prefix, localName, uri, attrs, null, null, start,
		 parent, tagFileInfo);
	}

	/*
	 * Constructor for custom action implemented by tag file.
	 */
	public CustomTag(String qName, String prefix, String localName,
			 String uri, Attributes attrs,
			 Attributes nonTaglibXmlnsAttrs,
			 Attributes taglibAttrs,
			 Mark start, Node parent, TagFileInfo tagFileInfo) {

	    super(qName, localName, attrs, nonTaglibXmlnsAttrs, taglibAttrs,
		  start, parent);

	    this.uri = uri;
	    this.prefix = prefix;
	    this.tagFileInfo = tagFileInfo;
	    this.tagInfo = tagFileInfo.getTagInfo();
	    this.customNestingLevel = makeCustomNestingLevel();
            this.childInfo = new ChildInfo();

	    this.implementsIterationTag = false;
	    this.implementsBodyTag = false;
	    this.implementsTryCatchFinally = false;
	    this.implementsSimpleTag = true;
	    this.implementsDynamicAttributes = tagInfo.hasDynamicAttributes();
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}

	/**
	 * @return The URI namespace that this custom action belongs to
	 */
	public String getURI() {
	    return this.uri;
	}

	/**
	 * @return The tag prefix
	 */
	public String getPrefix() {
	    return prefix;
	}

	public void setJspAttributes(JspAttribute[] jspAttrs) {
	    this.jspAttrs = jspAttrs;
	}

	public JspAttribute[] getJspAttributes() {
	    return jspAttrs;
	}
        
        public ChildInfo getChildInfo() {
            return childInfo;
        }
	
	public void setTagData(TagData tagData) {
	    this.tagData = tagData;
	    this.varInfos = tagInfo.getVariableInfo(tagData);
	    if (this.varInfos == null) {
		this.varInfos = ZERO_VARIABLE_INFO;
	    }
	}

	public TagData getTagData() {
	    return tagData;
	}

	public void setTagHandlerPoolName(String s) {
	    tagHandlerPoolName = s;
	}

	public String getTagHandlerPoolName() {
	    return tagHandlerPoolName;
	}

	public TagInfo getTagInfo() {
	    return tagInfo;
	}

	public TagFileInfo getTagFileInfo() {
	    return tagFileInfo;
	}

	/*
	 * @return true if this custom action is supported by a tag file,
	 * false otherwise
	 */
	public boolean isTagFile() {
	    return tagFileInfo != null;
	}

	public Class getTagHandlerClass() {
	    return tagHandlerClass;
	}

	public void setTagHandlerClass(Class hc) {
	    tagHandlerClass = hc;
	}

	public boolean implementsIterationTag() {
	    return implementsIterationTag;
	}

	public boolean implementsBodyTag() {
	    return implementsBodyTag;
	}

	public boolean implementsTryCatchFinally() {
	    return implementsTryCatchFinally;
	}

	public boolean implementsSimpleTag() {
	    return implementsSimpleTag;
	}

	public boolean implementsDynamicAttributes() {
	    return implementsDynamicAttributes;
	}

	public TagVariableInfo[] getTagVariableInfos() {
	    return tagInfo.getTagVariableInfos();
 	}
 
	public VariableInfo[] getVariableInfos() {
	    return varInfos;
	}

	public void setCustomTagParent(Node.CustomTag n) {
	    this.customTagParent = n;
	}

	public Node.CustomTag getCustomTagParent() {
	    return this.customTagParent;
	}

	public void setNumCount(Integer count) {
	    this.numCount = count;
	}

	public Integer getNumCount() {
	    return this.numCount;
	}

	public void setScriptingVars(Vector vec, int scope) {
	    switch (scope) {
	    case VariableInfo.AT_BEGIN:
		this.atBeginScriptingVars = vec;
		break;
	    case VariableInfo.AT_END:
		this.atEndScriptingVars = vec;
		break;
	    case VariableInfo.NESTED:
		this.nestedScriptingVars = vec;
		break;
	    }
	}

	/*
	 * Gets the scripting variables for the given scope that need to be
	 * declared.
	 */
	public Vector getScriptingVars(int scope) {
	    Vector vec = null;

	    switch (scope) {
	    case VariableInfo.AT_BEGIN:
		vec = this.atBeginScriptingVars;
		break;
	    case VariableInfo.AT_END:
		vec = this.atEndScriptingVars;
		break;
	    case VariableInfo.NESTED:
		vec = this.nestedScriptingVars;
		break;
	    }

	    return vec;
	}

	/*
	 * Gets this custom tag's custom nesting level, which is given as
	 * the number of times this custom tag is nested inside itself.
	 */
	public int getCustomNestingLevel() {
	    return customNestingLevel;
	}

        /**
         * Checks to see if the attribute of the given name is of type
	 * JspFragment.
         */
        public boolean checkIfAttributeIsJspFragment( String name ) {
            boolean result = false;

	    TagAttributeInfo[] attributes = tagInfo.getAttributes();
	    for (int i = 0; i < attributes.length; i++) {
		if (attributes[i].getName().equals(name) &&
		            attributes[i].isFragment()) {
		    result = true;
		    break;
		}
	    }
            
            return result;
        }

	/*public void setUseTagPlugin(boolean use) {
	    useTagPlugin = use;
	}

	public boolean useTagPlugin() {
	    return useTagPlugin;
	}

	public void setTagPluginContext(TagPluginContext tagPluginContext) {
	    this.tagPluginContext = tagPluginContext;
	}

	public TagPluginContext getTagPluginContext() {
	    return tagPluginContext;
	}

	public void setAtSTag(Nodes sTag) {
	    atSTag = sTag;
	}

	public Nodes getAtSTag() {
	    return atSTag;
	}
        
	public void setAtETag(Nodes eTag) {
	    atETag = eTag;
	}

	public Nodes getAtETag() {
	    return atETag;
	}*/
        
	/*
	 * Computes this custom tag's custom nesting level, which corresponds
	 * to the number of times this custom tag is nested inside itself.
	 *
	 * Example:
	 * 
	 *  <g:h>
	 *    <a:b> -- nesting level 0
	 *      <c:d>
	 *        <e:f>
	 *          <a:b> -- nesting level 1
	 *            <a:b> -- nesting level 2
	 *            </a:b>
	 *          </a:b>
	 *          <a:b> -- nesting level 1
	 *          </a:b>
	 *        </e:f>
	 *      </c:d>
	 *    </a:b>
	 *  </g:h>
	 * 
	 * @return Custom tag's nesting level
	 */
	private int makeCustomNestingLevel() {
	    int n = 0;
	    Node p = parent;
	    while (p != null) {
		if ((p instanceof Node.CustomTag)
		        && qName.equals(((Node.CustomTag) p).qName)) {
		    n++;
		}
		p = p.parent;
	    }
	    return n;
	}

	/**
	 * Returns true if this custom action has an empty body, and false
	 * otherwise.
	 *
	 * A custom action is considered to have an empty body if the 
	 * following holds true:
	 * - getBody() returns null, or
	 * - all immediate children are jsp:attribute actions, or
	 * - the action's jsp:body is empty.
	 */
	 public boolean hasEmptyBody() {
	     boolean hasEmptyBody = true;
	     Nodes nodes = getBody();
	     if (nodes != null) {
		 int numChildNodes = nodes.size();
		 for (int i=0; i<numChildNodes; i++) {
		     Node n = nodes.getNode(i);
		     if (!(n instanceof NamedAttribute)) {
			 if (n instanceof JspBody) {
			     hasEmptyBody = (n.getBody() == null);
			 } else {
			     hasEmptyBody = false;
			 }
			 break;
		     }
		 }
	     }

	     return hasEmptyBody;
	 }
    }

    /**
     * Used as a placeholder for the evaluation code of a custom action
     * attribute (used by the tag plugin machinery only).
     */
    public static class AttributeGenerator extends Node {
	String name;	// name of the attribute
	CustomTag tag;	// The tag this attribute belongs to

	public AttributeGenerator(Mark start, String name, CustomTag tag) {
	    super(start, null);
	    this.name = name;
	    this.tag = tag;
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}

	public String getName() {
	    return name;
	}

	public CustomTag getTag() {
	    return tag;
	}
    }

    /**
     * Represents the body of a &lt;jsp:text&gt; element
     */
    public static class JspText extends Node {

	public JspText(String qName, Attributes nonTaglibXmlnsAttrs,
		       Attributes taglibAttrs, Mark start, Node parent) {
	    super(qName, TEXT_ACTION, null, nonTaglibXmlnsAttrs, taglibAttrs,
		  start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}
    }

    /**
     * Represents a Named Attribute (&lt;jsp:attribute&gt;)
     */
    public static class NamedAttribute extends Node {

        // A unique temporary variable name suitable for code generation
        //private String temporaryVariableName;

        // True if this node is to be trimmed, or false otherwise
        private boolean trim = true;
        
        private ChildInfo childInfo;
	private String name;
	private String localName;
	private String prefix;

        public NamedAttribute(Attributes attrs, Mark start, Node parent) {
	    this(JSP_ATTRIBUTE_ACTION, attrs, null, null, start, parent);
	}

        public NamedAttribute(String qName, Attributes attrs,
			      Attributes nonTaglibXmlnsAttrs,
			      Attributes taglibAttrs,
			      Mark start, Node parent) {

            super(qName, ATTRIBUTE_ACTION, attrs, nonTaglibXmlnsAttrs,
		  taglibAttrs, start, parent);
            //temporaryVariableName = JspUtil.nextTemporaryVariableName();
            if( "false".equals( this.getAttributeValue( "trim" ) ) ) {
                // (if null or true, leave default of true)
                trim = false;
            }
            childInfo = new ChildInfo();
	    name = this.getAttributeValue("name");
            if (name != null) {
                // Mandatary attribute "name" will be checked in Validator
	        localName = name;
	        int index = name.indexOf(':');
	        if (index != -1) {
		    prefix = name.substring(0, index);
		    localName = name.substring(index+1);
                }
	    }
        }

        public void accept(Visitor v) throws JspException {
            v.visit(this);
        }

        public String getName() {
            return this.name;
        }

        public String getLocalName() {
            return this.localName;
        }

        public String getPrefix() {
            return this.prefix;
        }
        
        public ChildInfo getChildInfo() {
            return this.childInfo;
        }

        public boolean isTrim() {
            return trim;
        }

        /**
         * @return A unique temporary variable name to store the result in.
         *      (this probably could go elsewhere, but it's convenient here)
         */
        /*public String getTemporaryVariableName() {
            return temporaryVariableName;
        }*/

	/*
	 * Get the attribute value from this named attribute (<jsp:attribute>).
	 * Since this method is only for attributes that are not rtexpr,
	 * we can assume the body of the jsp:attribute is a template text.
	 */
	public String getText() {

	    class AttributeVisitor extends Visitor {
		String attrValue = null;
		public void visit(TemplateText txt) {
		    attrValue = new String(txt.getText());
		}
		
		public String getAttrValue() {
		    return attrValue;
		}
	    }

	    // According to JSP 2.0, if the body of the <jsp:attribute>
	    // action is empty, it is equivalent of specifying "" as the value
	    // of the attribute.
	    String text = "";
	    if (getBody() != null) {
		AttributeVisitor attributeVisitor = new AttributeVisitor();
		try {
		    getBody().visit(attributeVisitor);
		} catch (JspException e) {
		}
		text = attributeVisitor.getAttrValue();
	    }
	    
	    return text;
	}
    }

    /**
     * Represents a JspBody node (&lt;jsp:body&gt;)
     */
    public static class JspBody extends Node {

        private ChildInfo childInfo;

        public JspBody(Mark start, Node parent) {
            this(JSP_BODY_ACTION, null, null, start, parent);
        }

        public JspBody(String qName, Attributes nonTaglibXmlnsAttrs,
		       Attributes taglibAttrs, Mark start, Node parent) {
            super(qName, BODY_ACTION, null, nonTaglibXmlnsAttrs, taglibAttrs,
		  start, parent);
            this.childInfo = new ChildInfo();
        }

        public void accept(Visitor v) throws JspException {
            v.visit(this);
        }

        public ChildInfo getChildInfo() {
            return childInfo;
        }
    }

    /**
     * Represents a template text string
     */
    public static class TemplateText extends Node {

	public TemplateText(String text, Mark start, Node parent) {
	    super(null, null, text, start, parent);
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}

        /**
         * Trim all whitespace from the left of the template text
         */
        public void ltrim() {
	    int index = 0;
            while ((index < text.length()) && (text.charAt(index) <= ' ')) {
		index++;
            }
            text = text.substring(index);
        }

        /**
         * Trim all whitespace from the right of the template text
         */
        public void rtrim() {
            int index = text.length();
            while( (index > 0) && (text.charAt(index-1) <= ' ') ) {
                index--;
            }
            text = text.substring(0, index);
        }

	/**
	 * Returns true if this template text contains whitespace only.
	 */
	public boolean isAllSpace() {
	    boolean isAllSpace = true;
	    for (int i=0; i<text.length(); i++) {
		if (!Character.isWhitespace(text.charAt(i))) {
		    isAllSpace = false;
		    break;
		}
	    }
	    return isAllSpace;
	}
    }

    /*********************************************************************
     * Auxillary classes used in Node
     */

    /**
     * Represents attributes that can be request time expressions.
     *
     * Can either be a plain attribute, an attribute that represents a
     * request time expression value, or a named attribute (specified using
     * the jsp:attribute standard action).
     */

    public static class JspAttribute {

	private String qName;
	private String uri;
	private String localName;
	private String value;
	private boolean expression;
	private boolean dynamic;
        private ELNode.Nodes el;

        // If true, this JspAttribute represents a <jsp:attribute>
        private boolean namedAttribute;
        // The node in the parse tree for the NamedAttribute
        private NamedAttribute namedAttributeNode;

        JspAttribute(String qName, String uri, String localName, String value,
		     boolean expr, ELNode.Nodes el, boolean dyn ) {
	    this.qName = qName;
	    this.uri = uri;
	    this.localName = localName;
	    this.value = value;
            this.namedAttributeNode = null;
	    this.expression = expr;
            this.el = el;
	    this.dynamic = dyn;
            this.namedAttribute = false;
	}

        /**
         * Use this constructor if the JspAttribute represents a
         * named attribute.  In this case, we have to store the nodes of
         * the body of the attribute.
         */
        JspAttribute(NamedAttribute na, boolean dyn) {
            this.qName = na.getName();
	    this.localName = na.getLocalName();
            this.value = null;
            this.namedAttributeNode = na;
            this.expression = false;
            this.el = null;
	    this.dynamic = dyn;
            this.namedAttribute = true;
        }

	/**
 	 * @return The name of the attribute
	 */
	public String getName() {
	    return qName;
	}

	/**
 	 * @return The local name of the attribute
	 */
	public String getLocalName() {
	    return localName;
	}

	/**
 	 * @return The namespace of the attribute, or null if in the default
	 * namespace
	 */
	public String getURI() {
	    return uri;
	}

	/**
         * Only makes sense if namedAttribute is false.
         *
         * @return the value for the attribute, or the expression string
         *         (stripped of "<%=", "%>", "%=", or "%"
         *          but containing "${" and "}" for EL expressions)
	 */
	public String getValue() {
	    return value;
	}

        /**
         * Only makes sense if namedAttribute is true.
         *
         * @return the nodes that evaluate to the body of this attribute.
         */
        public NamedAttribute getNamedAttributeNode() {
            return namedAttributeNode;
        }

	/**
         * @return true if the value represents a traditional rtexprvalue
	 */
	public boolean isExpression() {
	    return expression;
	}

        /**
         * @return true if the value represents a NamedAttribute value.
         */
        public boolean isNamedAttribute() {
            return namedAttribute;
        }

        /**
         * @return true if the value represents an expression that should
         * be fed to the expression interpreter
         * @return false for string literals or rtexprvalues that should
         * not be interpreted or reevaluated
         */
        public boolean isELInterpreterInput() {
            return el != null;
        }

	/**
	 * @return true if the value is a string literal known at translation
	 * time.
	 */
	public boolean isLiteral() {
	    return !expression && (el != null) && !namedAttribute;
	}

	/**
	 * XXX
	 */
	public boolean isDynamic() {
	    return dynamic;
	}

	public ELNode.Nodes getEL() {
	    return el;
	}
    }

    /**
     * An ordered list of Node, used to represent the body of an element, or
     * a jsp page of jsp document.
     */
    public static class Nodes {

	private List<Node> list;
	private Node.Root root;		// null if this is not a page

	public Nodes() {
	    list = new Vector<Node>();
	}

	public Nodes(List<Node> l) {
	    list = l;
	}

	public Nodes(Node.Root root) {
	    this.root = root;
	    list = new Vector<Node>();
	    list.add(root);
	}

	/**
	 * Appends a node to the list
	 * @param n The node to add
	 */
	public void add(Node n) {
	    list.add(n);
	    root = null;
	}

	/**
	 * Removes the given node from the list.
	 * @param n The node to be removed
	 */
	public void remove(Node n) {
	    list.remove(n);
	}

	/**
	 * Visit the nodes in the list with the supplied visitor
	 * @param v The visitor used
	 */
	public void visit(Visitor v) throws JspException {
            for (Node n : list) {
		n.accept(v);
	    }
	}

	public int size() {
	    return list.size();
	}

	public Node getNode(int index) {
	    Node n = null;
	    try {
		n = list.get(index);
	    } catch (ArrayIndexOutOfBoundsException e) {
	    }
	    return n;
	}
	
	public Node.Root getRoot() {
	    return root;
	}
        
        public String toString() {
            return DumpVisitor.dump(this);
        }
    }

    /**
     * A visitor class for visiting the node.  This class also provides the
     * default action (i.e. nop) for each of the child class of the Node.
     * An actual visitor should extend this class and supply the visit
     * method for the nodes that it cares.
     */
    public static class Visitor {

	/**
	 * This method provides a place to put actions that are common to
	 * all nodes. Override this in the child visitor class if need to.
	 */
	protected void doVisit(Node n) throws JspException {
	}

	/**
	 * Visit the body of a node, using the current visitor
	 */
	protected void visitBody(Node n) throws JspException {
	    if (n.getBody() != null) {
		n.getBody().visit(this);
	    }
	}

	public void visit(Root n) throws JspException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(JspRoot n) throws JspException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(PageDirective n) throws JspException {
	    doVisit(n);
	}

	public void visit(TagDirective n) throws JspException {
	    doVisit(n);
	}

	public void visit(IncludeDirective n) throws JspException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(TaglibDirective n) throws JspException {
	    doVisit(n);
	}

	public void visit(AttributeDirective n) throws JspException {
	    doVisit(n);
	}

	public void visit(VariableDirective n) throws JspException {
	    doVisit(n);
	}

	public void visit(Comment n) throws JspException {
	    doVisit(n);
	}

	public void visit(Declaration n) throws JspException {
	    doVisit(n);
	}

	public void visit(Expression n) throws JspException {
	    doVisit(n);
	}

	public void visit(Scriptlet n) throws JspException {
	    doVisit(n);
	}

        public void visit(ELExpression n) throws JspException {
            doVisit(n);
        }

	public void visit(IncludeAction n) throws JspException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(ForwardAction n) throws JspException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(GetProperty n) throws JspException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(SetProperty n) throws JspException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(ParamAction n) throws JspException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(ParamsAction n) throws JspException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(FallBackAction n) throws JspException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(UseBean n) throws JspException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(PlugIn n) throws JspException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(CustomTag n) throws JspException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(UninterpretedTag n) throws JspException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(JspElement n) throws JspException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(JspText n) throws JspException {
	    doVisit(n);
	    visitBody(n);
	}

        public void visit(NamedAttribute n) throws JspException {
            doVisit(n);
            visitBody(n);
        }

        public void visit(JspBody n) throws JspException {
            doVisit(n);
            visitBody(n);
        }

        public void visit(InvokeAction n) throws JspException {
            doVisit(n);
            visitBody(n);
        }

        public void visit(DoBodyAction n) throws JspException {
            doVisit(n);
            visitBody(n);
        }

	public void visit(TemplateText n) throws JspException {
	    doVisit(n);
	}

	public void visit(JspOutput n) throws JspException {
	    doVisit(n);
	}

	public void visit(AttributeGenerator n) throws JspException {
	    doVisit(n);
	}
    }
}
