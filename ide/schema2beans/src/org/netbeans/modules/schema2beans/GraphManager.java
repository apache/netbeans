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

package org.netbeans.modules.schema2beans;

import java.util.*;
import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 *
 *
 */
public class GraphManager extends Object {

    public static interface Writer {
	public void write(OutputStream out, Document doc);
    }

    public static interface Factory {
        public org.w3c.dom.Document createDocument(InputStream in,
                                                   boolean validate);
    }
    
    Document	document = null;
    NodeFactory	factory = null;
    HashMap 	bindingsMap  = new HashMap();
    BaseBean	root;
    private boolean writeCData = false;
    
    //	When set to null (default), use XMLDocument instead
    private Factory		docFactory;
    private Writer		docWriter;

    private String		docTypePublic;
    private String		docTypeSystem;
    
    //
    //	The key is the input stream. This is how we can get the
    //	factory/writer when we are asked to build a Dom graph.
    //
    static Map	factoryMap = Collections.synchronizedMap(new HashMap(2));
    static Map	writerMap = Collections.synchronizedMap(new HashMap(2));
    

    public GraphManager(BaseBean root) {
        this.root = root;
    }
    
    /**
     *	Associate a factory to a stream
     */
    public static void setFactory(InputStream in,
				  GraphManager.Factory factory)  throws Schema2BeansException {
	setFactory(in, factory, null);
    }
    
    /**
     *  Set an external factory to use instead of the default one
     */
    public static void setFactory(InputStream in, GraphManager.Factory factory,
				  GraphManager.Writer writer) throws Schema2BeansException {
	if (in == null)
	    throw new Schema2BeansException(Common.getMessage(
		"InputStreamCantBeNull_msg"));

	if (factory != null)
	    GraphManager.factoryMap.put(in, factory);
	else
	    GraphManager.factoryMap.remove(in);

	if (writer != null)
	    GraphManager.writerMap.put(in, writer);
	else
	    GraphManager.writerMap.remove(in);
    }

    /**
     *  Set an external writer to use instead of the default one
     */
    public void setWriter(GraphManager.Writer writer) {
        this.docWriter = writer;
    }

    public void setWriteCData(boolean value) {
        writeCData = value;
    }

    public static Node createRootElementNode(String name) throws Schema2BeansRuntimeException {
        String s = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +// NOI18N
            "<" + name + "/>";	// NOI18N

        ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes());
        Document doc = GraphManager.createXmlDocument(in, false);
        NodeList children = doc.getChildNodes();
        int length = children.getLength();
        for (int i = 0; i < length; ++i) {
            Node node = children.item(i);
            if (node instanceof DocumentType) {
                //System.out.println("Found DocumentType where there should be none.");
                doc.removeChild(node);
                --length;
            }
        }
        return doc;
    }

    //
    //	Document created for this GraphManager. Called by the generated bean.
    //
    public void setXmlDocument(Node doc) throws Schema2BeansRuntimeException {
	if (doc instanceof Document) {
	    this.document = (Document)doc;
	    this.setNodeFactory((Document)doc);

	    //
	    //	The factory/writer should know about the doc now
	    //	and no more about the original InputStream.
	    //	(if the user specified a factory/writer)
	    //
	    Object o = GraphManager.factoryMap.get(doc);
	    if (o != null) {
		this.docFactory = (GraphManager.Factory)o;
		GraphManager.factoryMap.remove(doc);
	    }

	    o = GraphManager.writerMap.get(doc);
	    if (o != null) {
		this.docWriter = (GraphManager.Writer)o;
		GraphManager.writerMap.remove(doc);
	    }
	}
	else
	    throw new Schema2BeansRuntimeException(Common.getMessage(
		"CantFindFactory_msg"));
    }


    /**
     * This returns the DOM Document object, root
     * of the current DOM graph.  Operations that cause structural
     * modifications to the DOM graph are not allowed.  Indeed,
     * modifying the DOM graph directly would cause the bean graph
     * and its internal representation to be out of sync.
     */
    public Document getXmlDocument() {
	return this.document;
    }

    public void setDoctype(String publicId, String systemId) {
        //System.out.println("GraphManager.setDoctype: publicId="+publicId+" systemId="+systemId);
	this.docTypePublic = publicId;
	this.docTypeSystem = systemId;
    }

    /**
     *	Parse the DOM tree until the element named 'name' is found.
     *	Return the node of the name or null if not found.
     *	This method is used by the root bean generated class to get
     *	the root element of the DOM tree and start building the
     *	bean graph from here.
     */
    public static Node getElementNode(String name, Node doc) {
	Node n;
	for (n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
	    if (n.getNodeType() == Node.ELEMENT_NODE
	    && n.getNodeName().equals(name)) {
		break;
	    }
	}
	return n;
    }

    /**
     *	This method is called by the createRoot() method of the root bean
     *	(part of the BaseBean class). The doc might not be available
     *	at the time of this call. In such a case, the method
     *	completeRootBinding is called afterwards with the doc value to complete
     *	the setup of the root.
     *
     *	This makes sure that the root element of the object bindings
     *	between the beans and the DOM Nodes is created, before that the
     *	recursing creation of the graph begins.
     */
    public void createRootBinding(BaseBean beanRoot, BeanProp prop, Node doc) throws Schema2BeansException {
	prop.registerDomNode(doc, null, beanRoot);
	if (doc != null)
	    this.bindingsMap.put(doc, beanRoot.binding);
    }

    public void completeRootBinding(BaseBean beanRoot, Node doc) {
	this.bindingsMap.put(doc, beanRoot.binding);
	beanRoot.binding.setNode(doc);
    }

    /**
     *	This method sets the DOM nodes factory.
     */
    public void setNodeFactory(Document doc) {
	this.factory = new NodeFactory(doc);
    }

    /**
     *  Return the DOM node factory
     */
    public NodeFactory getNodeFactory() {
	return this.factory;
    }

    /**
     *	Return the root of the bean graph
     */
    public BaseBean getBeanRoot() {
	return this.root;
    }

    /**
     *	OutputStream version of write()
     */
    void write(OutputStream out) throws IOException, Schema2BeansException {
        //
        //	Code specific to the DOM implementation:
        //
        if (this.document == null)
            throw new Schema2BeansException(Common.getMessage("CantGetDocument_msg"));
        if (this.docWriter != null)
            this.docWriter.write(out, this.document);
        else {
            XMLUtil.DOMWriter domWriter = getDOMWriter();
            domWriter.write(out, document);
        }
    }

    protected void write(OutputStream out, String encoding) throws java.io.IOException {
        XMLUtil.DOMWriter domWriter = getDOMWriter();
        domWriter.write(out, encoding, document);
    }

    protected void write(java.io.Writer out) throws java.io.IOException {
        XMLUtil.DOMWriter domWriter = getDOMWriter();
        domWriter.setWriter(out);
        domWriter.write(document);
    }

    protected void write(java.io.Writer out, String encoding) throws java.io.IOException {
        XMLUtil.DOMWriter domWriter = getDOMWriter();
        domWriter.setWriter(out);
        domWriter.write(document, encoding);
    }

    public void write(java.io.Writer out, Node node) throws java.io.IOException, Schema2BeansException {
        XMLUtil.DOMWriter domWriter = getDOMWriter();
        domWriter.setWriter(out);
        domWriter.write(node);
    }

    protected XMLUtil.DOMWriter getDOMWriter() {
        XMLUtil.DOMWriter domWriter = new XMLUtil.DOMWriter();
        domWriter.setDocTypePublic(docTypePublic);
        domWriter.setDocTypeSystem(docTypeSystem);
        domWriter.setWriteCData(writeCData);
        return domWriter;
    }

    /**
     * Take the current DOM tree and readjust whitespace so that it
     * looks pretty.
     */
    public void reindent(String indent) {
        XMLUtil.reindent(document, indent);
    }

    /**
     * Indent by 2 spaces for every @level.
     */
    protected static void printLevel(java.io.Writer out, int level, String indent) throws java.io.IOException {
        StringBuffer outBuf = new StringBuffer();
        printLevel(outBuf, level, indent);
        out.write(outBuf.toString());
    }

    protected static void printLevel(StringBuffer out, int level, String indent) {
        for (int i = 0; i < level; ++i) {
            out.append(indent);
        }
    }

    protected static void printLevel(java.io.Writer out, int level, String indent, String text) throws java.io.IOException {
        StringBuffer outBuf = new StringBuffer();
        printLevel(outBuf, level, indent, text);
        out.write(outBuf.toString());
    }

    protected static void printLevel(OutputStream out, int level, String indent, String text) throws java.io.IOException {
        OutputStreamWriter w = new OutputStreamWriter(out);
        printLevel(w, level, indent, text);
        w.flush();
    }

    protected static void printLevel(StringBuffer out, int level,
                                     String indent, String text) {
        printLevel(out, level, indent);
        out.append(text);
    }

    /**
     *	Creates a DOM document from the input stream.
     */
    public static Document createXmlDocument(InputStream in, boolean validate) throws Schema2BeansRuntimeException {
	return createXmlDocument(in, validate, null);
    }

    private static InputStream tee(InputStream in) throws IOException {
        byte[] buf = new byte[4096];
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        int totalLength = 0;
        int len;
        while ((len = in.read(buf, 0, 4096)) > 0) {
            ba.write(buf, 0, len);
            totalLength += len;
        }
        System.out.println("schema2beans: in (length="+totalLength+"):");
        System.out.println(ba.toString());
        ByteArrayInputStream bain = new ByteArrayInputStream(ba.toByteArray());
        return bain;
    }

    /**
     *	Creates a DOM document from the input stream.
     */
    public static Document createXmlDocument(InputStream in, boolean validate,
                                             EntityResolver er) throws Schema2BeansRuntimeException {
        if (in == null)
            throw new IllegalArgumentException("in == null");	// NOI18N
        try {
            if (DDLogFlags.debug) {
                // Dump the contents to stdout
                in = tee(in);
            }

            //
            //	Change the references to map the newly created doc
            //	The BaseBean instance is not created yet. The doc
            //	document will be used to get back the factories.
            //
            Object o = GraphManager.factoryMap.get(in);
            if (o != null) {
                GraphManager.Factory f = (GraphManager.Factory)o;

                Document doc = f.createDocument(in, validate);

                GraphManager.factoryMap.remove(in);
                GraphManager.factoryMap.put(doc, o);

                Object o2 = GraphManager.writerMap.get(in);
                if (o2 != null) {
                    GraphManager.writerMap.remove(in);
                    GraphManager.writerMap.put(doc, o2);
                }
                return doc;
            }
            else {
                return createXmlDocument(new InputSource(in), validate, er, null);
            }
        } catch (Schema2BeansException e) {
            throw new Schema2BeansRuntimeException(e);
        } catch (IOException e) {
            throw new Schema2BeansRuntimeException(e);
        }
    }


    public static Document createXmlDocument(InputSource in, boolean validate) throws Schema2BeansException {
	return createXmlDocument(in, validate, null, null);
    }


    public static Document createXmlDocument(InputSource in, boolean validate,
                                             EntityResolver er, ErrorHandler eh) throws Schema2BeansException {
        if (in == null)
            throw new IllegalArgumentException("in == null");	// NOI18N
        if (validate == false && er == null) {
            // The client is not interested in any validation, so make
            // see to it that any entity resolution doesn't hit the network
            er = NullEntityResolver.newInstance();
        }
        try {
            //	Build a Document using JAXP
            DocumentBuilderFactory dbf =
                DocumentBuilderFactory.newInstance();
            dbf.setValidating(validate);

            DocumentBuilder db = dbf.newDocumentBuilder();
            if (er != null)
                db.setEntityResolver(er);
            if (eh != null)
                db.setErrorHandler(eh);

            if (DDLogFlags.debug) {
                System.out.println("createXmlDocument: validate="+validate+" dbf="+dbf+" db="+db+" er="+er);
            }

            return db.parse(in);
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            throw new Schema2BeansNestedException(Common.getMessage("CantCreateXMLDOMDocument_msg"), e);
        } catch (org.xml.sax.SAXException e) {
            throw new Schema2BeansNestedException(Common.getMessage("CantCreateXMLDOMDocument_msg"), e);
        } catch (IOException e) {
            throw new Schema2BeansNestedException(Common.getMessage("CantCreateXMLDOMDocument_msg"), e);
        }
    }

    /**
     *	This method is called by the generated beans when they are
     *	building themselves from a DOM tree.
     *	Typically, the first root bean calls this method with the
     *	DOM root node and the list of the properties that are expected
     *	under this node.
     *	This method parses the DOM sub-node of the node and matches their names
     *	with the names of the properties. When a match is found, the
     *	bean property object is called with the node found. If the node
     *	has no match in the bean properties, the node is ignored but
     *	the event is logged as it might reveal a problem in the bean tree
     *	(DTD element missing in the bean class graph).
     *
     */
    public void fillProperties(BeanProp[] prop, Node node) throws Schema2BeansException {
        BaseBean 	bean;
        DOMBinding 	binding, newBinding;

        if (prop == null || node == null)
            return;

        if (this.bindingsMap.get(node) == null) {
            throw new Schema2BeansException(Common.getMessage(
                                                              "CurrentNodeHasNoBinding_msg", Integer.valueOf(node.hashCode())));
        }

        // Store the property's dtdName's into a map for fast lookup,
        // and be able to handle multiple properties with the same name.
        Map dtdName2Prop = new HashMap();	// Map<String, BeanProp>
        Map dupDtdNames = new HashMap();	// Map<String, List<BeanProp>>
        for(int i=0; i<prop.length; i++) {
            String dtdName = prop[i].dtdName;
            if (dtdName2Prop.containsKey(dtdName)) {
                //System.out.println("Found duplicate dtdName="+dtdName);
                List dupList = (List) dupDtdNames.get(dtdName);
                if (dupList == null) {
                    dupList = new ArrayList();
                    //dupList.add(dtdName2Prop.get(dtdName));
                    dupDtdNames.put(dtdName, dupList);
                }
                dupList.add(prop[i]);
            } else {
                dtdName2Prop.put(dtdName, prop[i]);
            }
        }

        //  Assume that the DOM parsing takes longer than prop parsing
        Map dupDtdNameIterators = new HashMap();	// Map<String, Iterator<BeanProp>>
        for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String eltName = n.getNodeName();

                //System.out.println("eltName="+eltName);
                BeanProp matchingProp = (BeanProp) dtdName2Prop.get(eltName);
                if (matchingProp != null) {
                    List dupList = (List) dupDtdNames.get(eltName);
                    if (dupList != null) {
                        // There are mutliple BeanProp's with the same dtd name,
                        // figure out which one we should pick.
                        if (!Common.isArray(matchingProp.type)) {
                            Iterator propIt = (Iterator) dupDtdNameIterators.get(eltName);
                            //System.out.println("propIt="+propIt);
                            if (propIt == null) {
                                // First time, let the matchingProp load it,
                                // but set it up for next time.
                                propIt = dupList.iterator();
                                dupDtdNameIterators.put(eltName, propIt);
                            } else if (propIt.hasNext()) {
                                matchingProp = (BeanProp) propIt.next();
                            }
                        }
                    }
                    binding = (DOMBinding)this.bindingsMap.get(n);

                    if (DDLogFlags.debug) {
                        String s = eltName + " N(" + n.hashCode()+") - " +
                            (binding==null?"new node":"already bound B(" +
                             binding.hashCode() +")");
                        TraceLogger.put(TraceLogger.DEBUG,
                                        TraceLogger.SVC_DD,
                                        DDLogFlags.DBG_BLD, 1,
                                        DDLogFlags.FOUNDNODE, s);
                    }

                    newBinding =
                        matchingProp.registerDomNode(n, binding, null);

                    if (newBinding != null) {

                        if (Common.isBean(matchingProp.type))
                            bean = (BaseBean)newBinding.getBean(matchingProp);
                        else
                            bean = null;

                        if (DDLogFlags.debug) {
                            String s = "B(" + newBinding.hashCode() +
                                ") - " + matchingProp.getPropClass().getName();
                            TraceLogger.put(TraceLogger.DEBUG,
                                            TraceLogger.SVC_DD,
                                            DDLogFlags.DBG_BLD, 1,
                                            DDLogFlags.BOUNDNODE, s);
                        }

                        if (bean != null) {
                            //
                            //  The property was a bean, fill up this bean.
                            //  This is were the recursing call in the
                            //  creation of the bean graph happens.
                            //
                            if (binding == null)
                                this.bindingsMap.put(n, newBinding);
                            bean.createBean(n, this);
                        }
                    }
                } else {
                    // log that there is no matching
                    if (DDLogFlags.debug) {
                        TraceLogger.put(TraceLogger.DEBUG,
                                        TraceLogger.SVC_DD,
                                        DDLogFlags.DBG_BLD, 1,
                                        DDLogFlags.NONODE, eltName);
                    }
                }
            } else {
                // Log that this is not an element
                short t = n.getNodeType();
                String v = n.getNodeValue();
                if (DDLogFlags.debug) {
                    TraceLogger.put(TraceLogger.DEBUG,
                                    TraceLogger.SVC_DD,
                                    DDLogFlags.DBG_BLD, 1,
                                    DDLogFlags.NOTELT,
                                    DDFactory.typeToString(t) +
                                    " = " + Common.dumpHex(v));
                }
            }
        }
    }

    //
    //	Event misc. methods, base on the name of the PropertyChanged event
    //
    //	BaseBean 	getPropertyParent(String name)
    //	String 		getPropertyParentName(String name)
    //	String 		getPropertyName(String name)
    //	int			getPropertyIndex(String name)
    //	String		getAttributeName(String name)
    //	boolean		isAttribute(String name)
    //

    /**
     *	Return the bean holding the property 'name' as a BaseBean object
     */
    public BaseBean getPropertyParent(String name) {
        return (BaseBean) getPropertyParent(root, name);
    }

    public static Bean getPropertyParent(Bean theRoot, String name) {
        String[] path = name.split("/", -1); // NOI18N
        int n = path.length;
        if (n < 2 || path[0].length() > 0) {
            throw new IllegalArgumentException(Common.getMessage("NameShouldStartWithSlash_msg", name));
        }
        if (n == 2) {
            return null;
        }
        Bean curBean = theRoot;
        for (int i = 2; i < n - 1; i++) {
            String[] element = path[i].split("[.]", 2); // NOI18N
            String beanName = element[0];
            int index;
            if (element.length == 1) {
                index = 0;
            } else {
                String indexName = element[1];
                if (indexName.indexOf('i') != -1) {
                    throw new IllegalStateException(
                            Common.getMessage("CantFindBeanBecausePartOfNameRemoved_msg", beanName, name));
                }
                index = Integer.parseInt(indexName, 16);
            }
            curBean = curBean.propertyById(beanName, index);
            if (curBean == null) {
                throw new IllegalStateException(
                        Common.getMessage("CantFindBeanMayHaveBeenRemoved_msg", beanName, name));
            }
        }
        return curBean;
    }
    
    public String getKeyPropertyName(String propName, String[] prop,
				     String[] key) {
	return this.getKeyPropertyName(propName, prop, key, false);
    }

    public String getKeyPropertyName(String propName) {
	return this.getKeyPropertyName(propName, null, null, true);
    }

    /**
     *	Return the bean holding the property 'name' as a BaseBean object
     */
    public String getKeyPropertyName(String propName, String[] prop,
				     String[] key, boolean keyName) {
        return getKeyPropertyName(root, propName, prop, key, keyName);
    }

    public static String getKeyPropertyName(Bean theRoot,
                                            String propName, String[] prop,
                                            String[] key, boolean keyName) {

	StringBuffer	keyPropName = new StringBuffer();
	Bean	curBean = theRoot;
	String 		beanName, indexName;
	String		name = propName;

	if (name.charAt(0) == '/')
	    name = name.substring(1);

	do {
	    int i = name.indexOf('/');
	    if (i != -1) {
		beanName = name.substring(0, i);
		name = name.substring(i+1);
	    }
	    else {
		beanName = name;
		name = null;
	    }

	    i = beanName.indexOf('.');

	    if (i != -1) {
		indexName = beanName.substring(i+1);
		beanName = beanName.substring(0, i);

		if (indexName.indexOf('i') != -1)
		    throw new IllegalStateException(
		    Common.getMessage(
			"CantFindBeanBecausePartOfNameRemoved_msg",
			beanName, propName));
	    }
	    else
		indexName = "0";	// NOI18N


	    if (theRoot.hasName(beanName)) {
            curBean = theRoot;
	    } else {
		if (curBean.getProperty(beanName).isBean()) {
		    curBean = curBean.propertyById(beanName,
						   Integer.parseInt(indexName,
								    16));
		} else
		    curBean = null;
	    }

	    keyPropName.append(beanName);

	    if (prop != null && curBean != null) {
		//	If a property name/key is defined, add it to the path
		for (i=0; i<prop.length; i++) {
		    if (prop[i].equals(beanName)) {
			keyPropName.append(".");	// NOI18N
			keyPropName.append(key[i]);
			keyPropName.append("=");	// NOI18N
			String v = (String)curBean.getValue(key[i], 0);
			if (v != null)
			    keyPropName.append(v);
			break;
		    }
		}
	    } else if (keyName && curBean != null) {
		//	If any property has 'name', use it
		BaseProperty[] l = curBean.listProperties();
		for (i=0; i<l.length; i++) {
		    String n = l[i].getName();
		    if (n.toLowerCase().indexOf("name") != -1) { // NOI18N
			keyPropName.append(".");	// NOI18N
			keyPropName.append(n);
			keyPropName.append("=");	// NOI18N
			String v = (String)curBean.getValue(n, 0);
			if (v != null)
			    keyPropName.append(v);
			break;
		    }
		}
	    }

	    if (name != null)
		keyPropName.append("/");			// NOI18N

	} while (name != null && curBean != null);

	return keyPropName.toString();
    }


    /**
     *	Return the bean holding the property 'name' as a BaseBean object
     */
    public static String trimPropertyName(String propName) {
	StringBuffer name = new StringBuffer();
	int i, j;
	i = 0;
	do {
	    j = propName.indexOf('.', i);
	    if (j==-1) {
		name.append(propName.substring(i));
	    } else {
		name.append(propName.substring(i, j));
		i = propName.indexOf('/', j);
	    }
	} while(j!=-1 && i!=-1);

	return name.toString();
    }

    /**
     *	Return the name of the bean holding the property 'name'
     */
    public static String getPropertyParentName(String name) {
	int i = name.lastIndexOf('/');
	if (i != -1)
	    name = name.substring(0, i);
	i = name.lastIndexOf('/');
	if (i != -1)
	    name = name.substring(i+1);
	i = name.lastIndexOf('.');
	if (i != -1)
	    name = name.substring(0, i);

	return name;
    }

    /**
     *	Return the name of the property of the PropertyChangeEvent named name.
     *	Any index or attribute is removed from the name of the event.
     *
     *	single property: 	/Book/Chapter.2/Comment	-> Comment
     *	indexed property:	/Book/Chapter.4  	-> Chapter
     *	attribute:		/Book/Chapter.2:title	-> Chapter
     *
     */
    public static String getPropertyName(String name) {
	int i = name.lastIndexOf('/');
	if (i != -1)
	    name = name.substring(i+1);
	//	Remove the index value
	i = name.lastIndexOf('.');
	if (i != -1)
	    name = name.substring(0, i);
	//	If there is a still an attribute, remove it
	i = name.lastIndexOf(':');
	if (i != -1)
	    name = name.substring(0, i);

	return name;
    }

    /**
     *	Return the name of the attribute if this is the name of an attribute,
     *	return null otherwise.
     *
     *	single property: 	/Book/Chapter.2/Comment	-> null
     *	indexed property:	/Book/Chapter.4  	-> null
     *	attribute:		/Book/Chapter.2:title	-> title
     *
     */
    public String getAttributeName(String name) {
	int i = name.lastIndexOf(':');
	if (i != -1)
	    name = name.substring(i+1);
	else
	    name = null;
	return name;
    }

    /**
     *	Return true if this is the name of an attribute
     */
    public boolean isAttribute(String name) {
	int i = name.lastIndexOf(':');
	return (i != -1);
    }

    /**
     *	Return the index value of the property, as a string
     */
    private static String extractPropertyIndex(String name) {
	int i = name.lastIndexOf('/');
	if (i != -1)
	    name = name.substring(i+1);
	i = name.lastIndexOf('.');
	if (i != -1) {
	    name = name.substring(i+1);
	    i = name.lastIndexOf(':');
	    if (i != -1)
		name = name.substring(0, i);
	}
	else
	    name = null;
	return name;
    }


    /**
     *	If the property is an indexed property, return the index of
     *	the property.
     */
    public int getPropertyIndex(String name) {
        return getPropertyIndex(root, name);
    }
    public static int getPropertyIndex(Bean theRoot, String name) {
        String index = extractPropertyIndex(name);
        if (index != null) {
            int i = index.lastIndexOf('i');
            if (i != -1) {
                //  This is a removed property - return the old value
                return Integer.parseInt(index.substring(i+1));
            }
            else {
                //  Get the current index value
                Bean bean = getPropertyParent(theRoot, name);
                if (bean != null) {
                    BeanProp bp = bean.beanProp(getPropertyName(name));

                    if (bp != null)
                        return bp.idToIndex(Integer.parseInt(index, 16));
                }
            }
        }
	
        return -1;
    }
    
    //
    //	Events misc. methods
    //
    public static void debug(boolean d) {
	DDLogFlags.debug = d;
    }
    
    //
    //	Default values for scalar types. The idea is to allow the user to
    //	change the following default values (TODO).
    //
    public Object defaultScalarValue(int type) {
	switch(type & Common.MASK_TYPE) {
	    case Common.TYPE_STRING:
		return "";	// NOI18N
	    case Common.TYPE_BOOLEAN:
		return Boolean.FALSE;
	    case Common.TYPE_BYTE:
		return (byte)0;
	    case Common.TYPE_CHAR:
		return '\0';
	    case Common.TYPE_SHORT:
		return (short)0;
	    case Common.TYPE_INT:
		return 0;
	    case Common.TYPE_LONG:
		return 0L;
	    case Common.TYPE_FLOAT:
		return 0.0F;
	    case Common.TYPE_DOUBLE:
		return 0.0D;
	    default:
                throw new IllegalArgumentException(Common.getMessage("UnknownType", type));
	}
    }
}

