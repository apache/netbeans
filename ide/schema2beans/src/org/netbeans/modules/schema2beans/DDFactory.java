/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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

import java.lang.reflect.*;

import org.w3c.dom.*;


/**
 *  This class provides a dynamic mechanism to instantiate schema2beans graphs.
 *  The first and most common way to instantiate a schema2beans graph is to
 *  call the createGraph() method on the root class of the generated
 *  schema2beans classes.
 *
 *  It might happen that the code that whish to instantiate a schema2beans graph
 *  doesn't have or know about the class of the schema2beans root element.
 *  Such code should be able to say 'I want to instantiate a schema2beans graph',
 *  and I know that it is named 'graphName'. This is what the register and
 *  create methods are performing.
 *
 *  The code that knows about the schema2beans classes can register the class
 *  root node using some public name. Then, other part of the code can
 *  instantiate a schema2beans graph using simply the public name (@see create).
 *
 *  This class also provide some utility methods to dump the content of a
 *  DOM tree as a String (@see XmlToString).
 *
 */
public class DDFactory extends Object {
    static HashMap beanClassMap = new HashMap();
    
    static int 		idCount = 0;
    
    /**
     *	Create the proper DD bean object graph depending on the document
     *	type of the XML document specified as an input stream.
     *
     *	@param	in the XML document is passed as an input stream.
     *	@return	BaseBean is the base object for all DD beans.
     */
    public static BaseBean create(InputStream in, String rootName) throws Schema2BeansException {
	Document 	doc = null;
	String		docType;
	BaseBean	beanNode = null;
	Class		bean;
	
	if (DDLogFlags.debug) {
	    TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
	    DDLogFlags.DBG_BLD, 1,
	    DDLogFlags.DDCREATE);
	}
	
    doc = GraphManager.createXmlDocument(in, false);
	
	if (DDLogFlags.debug) {
	    String str = XmlToString(doc, 999);
	    TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
	    DDLogFlags.DBG_BLD, 20,
	    DDLogFlags.DDCREATED,
	    str );
	}
	
	synchronized (beanClassMap) {
	    bean = (Class)beanClassMap.get(rootName);
	}
	
	if (bean == null)
	    throw new Schema2BeansException(Common.getMessage(
		"CantCreateBeanForRootElement_msg", rootName));
	
	
	Constructor c = null;
	
	try {
	    Class[] cc = new Class[] {org.w3c.dom.Node.class,
	    int.class};
	    c = bean.getDeclaredConstructor(cc);
	}
	catch(NoSuchMethodException me) {
	    throw new Schema2BeansNestedException(Common.getMessage(
	    "CantGetConstructor_msg"), me);
	}
	
	Object[] p = new Object[] {doc, Integer.valueOf(Common.NO_DEFAULT_VALUES)};
	
	try {
	    beanNode = (BaseBean)c.newInstance(p);
	}
	catch(Exception e) {
	    TraceLogger.error(e);
	    throw new Schema2BeansNestedException(Common.getMessage(
                                          "CantInstanciateBeanClass_msg"), e);
	}
	
	if (DDLogFlags.debug) {
	    TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
	    DDLogFlags.DBG_BLD, 1,
	    DDLogFlags.DDBEANED,
	    "Created bean graph node for " + rootName );
	}
	
	return beanNode;
    }
    
    /**
     *	Register the root bean to use for a DTD document root name.
     *	Use the default class loader, then tries to use the String name
     *	object class loader. The name of the graph can be any string (it doesn't
     *	have to be the root element name)
     *
     *	@param name entry name of this graph registry
     *	@param className class name (package and class name) to use
     *	in order to instantiate the schema2beans graph. This should be the
     *	class name of the root of the schema2beans generated classes.
     */
    public static void register(String name, String className)
	throws ClassNotFoundException {
	Class c = null;
	try {
	    c = Class.forName(className);
	} catch(ClassNotFoundException e) {
	    //	Try with the String object class loader
	    c = name.getClass().forName(className);
	}
	DDFactory.register(name, c);
    }

    /**
     *	Register the root bean to use for a DTD document root name.
     *	Use the default class loader, then tries to use the String name
     *	object class loader. The name of the graph can be any string (it doesn't
     *	have to be the root element name)
     *
     *	@param name entry name of this graph registry
     *	@param clazz class to use in order to instantiate the schema2beans graph. 
     *	This should be the class of the root of the schema2beans generated classes.
     */
    public static void register(String name, Class clazz) {
	synchronized (beanClassMap) {
	    beanClassMap.put(name, clazz);
	}
    }

    /*
     *	This is where the schema2beans runtime can get a unique ID to tag the elts
     *	(any element is uniquely identified using this ID. This is how we
     *	can uniquely track the elements of an array even though the user
     *	mizes the array).
     */
    static synchronized int getUniqueId() {
	return DDFactory.idCount++;
    }
    
    
    static String typeToString(short type) {
	switch(type) {
	    case Node.ATTRIBUTE_NODE: return "attr";	// NOI18N
	    case Node.CDATA_SECTION_NODE: return "cdata";	// NOI18N
	    case Node.COMMENT_NODE : return "comment";	// NOI18N
	    case Node.DOCUMENT_FRAGMENT_NODE: return "doc_fragment";	// NOI18N
	    case Node.DOCUMENT_NODE: return "doc";		// NOI18N
	    case Node.DOCUMENT_TYPE_NODE: return "doc_type";	// NOI18N
	    case Node.ELEMENT_NODE: return "element";	// NOI18N
	    case Node.ENTITY_NODE: return "entity";		// NOI18N
	    case Node.ENTITY_REFERENCE_NODE: return "entity_ref";	// NOI18N
	    case Node.NOTATION_NODE: return "notation";	// NOI18N
	    case Node.PROCESSING_INSTRUCTION_NODE: return "processing_instr";// NOI18N
	    case Node.TEXT_NODE: return "text";		// NOI18N
	    default: return "type:" + type;			// NOI18N
	}
    }
    
    
    /**
     *	Dump the whole content of a DOM tree as a String
     */
    public static String XmlToString(Node n) {
	return XmlToString(n, 9999, null);
    }
    
    /**
     *	Dump the content of a DOM tree as a String. This methods only 
     *	recurses for the specified depth.
     */
    public static String XmlToString(Node n, int depth) {
	return XmlToString(n, depth, null);
    }
    
    
    /**
     *	Dump the content of a DOM tree as a String. This methods only 
     *	recurses for the specified depth and only prints elements that
     *	match the filter.
     */
    public static String XmlToString(Node n, int depth, String filter) {
	StringBuffer str = new StringBuffer();
	nodeToString("", str, n, depth, filter, true);	// NOI18N
	return str.toString();
    }
    
    //	Method related to XmlToString
    static void nodeToString(String indent, StringBuffer str, Node n,
			     int depth, String filter, boolean root) {
				 
	if (root)
	    //	Don't go for siblings on the root
	    nodeChildrenToString(indent, str, n, depth, filter);
	else {
	    for (;n != null; n = n.getNextSibling()) {
		nodeChildrenToString(indent, str, n, depth, filter);
	    }
	}
    }
    
    
    //	Method related to XmlToString
    static void nodeChildrenToString(String indent, StringBuffer str,
				     Node n, int depth, String filter) {
					 
	if ((filter == null) || n.getNodeName().equals(filter)) {
	    String 	tmp = indent + n.getNodeName();
	    String 	value = n.getNodeValue();
	    short 	type = n.getNodeType();
	    
	    if (value == null)
		value = "";			// NOI18N
	    //else
            //value = value.trim();
	    
	    if (!value.equals(""))			// NOI18N
		tmp += "=" + value;		// NOI18N
	    
	    tmp += " - " + typeToString(type);	// NOI18N
	    
	    if ((type != Node.TEXT_NODE && type != Node.CDATA_SECTION_NODE) || (!value.trim().equals("")))	// NOI18N
	    {
		str.append(tmp);
		str.append("\n");		// NOI18N
	    }
	    
	    NamedNodeMap map = n.getAttributes();
	    if (map != null && map.getLength() != 0) {
            List attrNames = new ArrayList(map.getLength());
            for (int i=0; i<map.getLength(); i++) {
                Attr a = (Attr)map.item(i);
                attrNames.add(a.getName());
            }
            Collections.sort(attrNames);
            for (Iterator it = attrNames.iterator(); it.hasNext(); ) {
                Attr a = (Attr) map.getNamedItem((String)it.next());
                str.append(indent);
                str.append("attribute: ");	// NOI18N
                str.append(a.getName());
                str.append("=");	// NOI18N
                str.append(a.getValue());
                str.append("\n");	// NOI18N
            }
	    }
	}
	
	if (n.getFirstChild() != null && (depth > 0) )
	    nodeToString(indent + "  ", str, n.getFirstChild(),	// NOI18N
	    depth-1, filter, false);
    }
}

