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
/**
 *	This generated bean class MetaDD
 *	matches the schema element 'metaDD'.
 *
 *
 *	This class matches the root element of the DTD,
 *	and is the root of the bean graph.
 *
 * 	metaDD <metaDD> : MetaDD
 * 		metaElement <meta-element> : MetaElement[0,n]
 * 			dtdName <dtd-name> : String
 * 			namespace <namespace> : String[0,1]
 * 			beanName <bean-name> : String[0,1]
 * 			beanClass <bean-class> : String[0,1]
 * 			wrapperClass <wrapper-class> : String[0,1]
 * 			defaultValue <default-value> : String[0,n]
 * 			knownValue <known-value> : String[0,n]
 * 			metaProperty <meta-property> : MetaProperty[0,n]
 * 				beanName <bean-name> : String
 * 				defaultValue <default-value> : String[0,n]
 * 				knownValue <known-value> : String[0,n]
 * 				key <key> : boolean[0,1]
 * 					EMPTY : String
 * 				vetoable <vetoable> : boolean[0,1]
 * 					EMPTY : String
 * 			comparatorClass <comparator-class> : String[0,n]
 * 			implements <implements> : String[0,1]
 * 			extends <extends> : String[0,1]
 * 			import <import> : String[0,n]
 * 			userCode <user-code> : String[0,1]
 * 			vetoable <vetoable> : boolean[0,1]
 * 				EMPTY : String
 * 			skipGeneration <skip-generation> : boolean[0,1]
 * 				EMPTY : String
 * 			delegatorName <delegator-name> : String[0,1]
 * 			delegatorExtends <delegator-extends> : String[0,1]
 * 			beanInterfaceExtends <bean-interface-extends> : String[0,1]
 * 			canBeEmpty <can-be-empty> : boolean[0,1]
 * 				EMPTY : String
 * 		implements <implements> : String[0,1]
 * 		extends <extends> : String[0,1]
 * 		import <import> : String[0,n]
 * 		vetoable <vetoable> : boolean[0,1]
 * 			EMPTY : String
 * 		throwExceptions <throw-exceptions> : boolean[0,1]
 * 			EMPTY : String
 * 		schemaLocation <schemaLocation> : String[0,1]
 * 		finder <finder> : String[0,n]
 *
 * @Generated
 */

package org.netbeans.modules.schema2beansdev.metadd;

public class MetaDD implements org.netbeans.modules.schema2beansdev.metadd.CommonBean {
	public static final String META_ELEMENT = "MetaElement";	// NOI18N
	public static final String IMPLEMENTS = "Implements";	// NOI18N
	public static final String EXTENDS = "Extends";	// NOI18N
	public static final String IMPORT = "Import";	// NOI18N
	public static final String VETOABLE = "Vetoable";	// NOI18N
	public static final String THROW_EXCEPTIONS = "ThrowExceptions";	// NOI18N
	public static final String SCHEMALOCATION = "SchemaLocation";	// NOI18N
	public static final String FINDER = "Finder";	// NOI18N

	private java.util.List _MetaElement = new java.util.ArrayList();	// List<MetaElement>
	private String _Implements;
	private String _Extends;
	private java.util.List _Import = new java.util.ArrayList();	// List<String>
	private boolean _Vetoable;
	private boolean _isSet_Vetoable = false;
	private boolean _ThrowExceptions;
	private boolean _isSet_ThrowExceptions = false;
	private String _SchemaLocation;
	private java.util.List _Finder = new java.util.ArrayList();	// List<String>
	private java.lang.String schemaLocation;
	private static final java.util.logging.Logger _logger = java.util.logging.Logger.getLogger("org.netbeans.modules.schema2beansdev.metadd.MetaDD");

	/**
	 * Normal starting point constructor.
	 */
	public MetaDD() {
	}

	/**
	 * Deep copy
	 */
	public MetaDD(org.netbeans.modules.schema2beansdev.metadd.MetaDD source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public MetaDD(org.netbeans.modules.schema2beansdev.metadd.MetaDD source, boolean justData) {
		for (java.util.Iterator it = source._MetaElement.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.schema2beansdev.metadd.MetaElement srcElement = (org.netbeans.modules.schema2beansdev.metadd.MetaElement)it.next();
			_MetaElement.add((srcElement == null) ? null : newMetaElement(srcElement, justData));
		}
		_Implements = source._Implements;
		_Extends = source._Extends;
		for (java.util.Iterator it = source._Import.iterator(); 
			it.hasNext(); ) {
			String srcElement = (String)it.next();
			_Import.add(srcElement);
		}
		_Vetoable = source._Vetoable;
		_isSet_Vetoable = source._isSet_Vetoable;
		_ThrowExceptions = source._ThrowExceptions;
		_isSet_ThrowExceptions = source._isSet_ThrowExceptions;
		_SchemaLocation = source._SchemaLocation;
		for (java.util.Iterator it = source._Finder.iterator(); 
			it.hasNext(); ) {
			String srcElement = (String)it.next();
			_Finder.add(srcElement);
		}
		schemaLocation = source.schemaLocation;
	}

	// This attribute is an array, possibly empty
	public void setMetaElement(org.netbeans.modules.schema2beansdev.metadd.MetaElement[] value) {
		if (value == null)
			value = new MetaElement[0];
		_MetaElement.clear();
		((java.util.ArrayList) _MetaElement).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_MetaElement.add(value[i]);
		}
	}

	public void setMetaElement(int index, org.netbeans.modules.schema2beansdev.metadd.MetaElement value) {
		_MetaElement.set(index, value);
	}

	public org.netbeans.modules.schema2beansdev.metadd.MetaElement[] getMetaElement() {
		MetaElement[] arr = new MetaElement[_MetaElement.size()];
		return (MetaElement[]) _MetaElement.toArray(arr);
	}

	public java.util.List fetchMetaElementList() {
		return _MetaElement;
	}

	public org.netbeans.modules.schema2beansdev.metadd.MetaElement getMetaElement(int index) {
		return (MetaElement)_MetaElement.get(index);
	}

	// Return the number of metaElement
	public int sizeMetaElement() {
		return _MetaElement.size();
	}

	public int addMetaElement(org.netbeans.modules.schema2beansdev.metadd.MetaElement value) {
		_MetaElement.add(value);
		int positionOfNewItem = _MetaElement.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeMetaElement(org.netbeans.modules.schema2beansdev.metadd.MetaElement value) {
		int pos = _MetaElement.indexOf(value);
		if (pos >= 0) {
			_MetaElement.remove(pos);
		}
		return pos;
	}

	// This attribute is optional
	public void setImplements(String value) {
		_Implements = value;
	}

	public String getImplements() {
		return _Implements;
	}

	// This attribute is optional
	public void setExtends(String value) {
		_Extends = value;
	}

	public String getExtends() {
		return _Extends;
	}

	// This attribute is an array, possibly empty
	public void setImport(String[] value) {
		if (value == null)
			value = new String[0];
		_Import.clear();
		((java.util.ArrayList) _Import).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_Import.add(value[i]);
		}
	}

	public void setImport(int index, String value) {
		_Import.set(index, value);
	}

	public String[] getImport() {
		String[] arr = new String[_Import.size()];
		return (String[]) _Import.toArray(arr);
	}

	public java.util.List fetchImportList() {
		return _Import;
	}

	public String getImport(int index) {
		return (String)_Import.get(index);
	}

	// Return the number of import
	public int sizeImport() {
		return _Import.size();
	}

	public int addImport(String value) {
		_Import.add(value);
		int positionOfNewItem = _Import.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeImport(String value) {
		int pos = _Import.indexOf(value);
		if (pos >= 0) {
			_Import.remove(pos);
		}
		return pos;
	}

	// This attribute is optional
	public void setVetoable(boolean value) {
		_Vetoable = value;
		_isSet_Vetoable = true;
	}

	public boolean isVetoable() {
		return _Vetoable;
	}

	// This attribute is optional
	public void setThrowExceptions(boolean value) {
		_ThrowExceptions = value;
		_isSet_ThrowExceptions = true;
	}

	public boolean isThrowExceptions() {
		return _ThrowExceptions;
	}

	// This attribute is optional
	public void setSchemaLocation(String value) {
		_SchemaLocation = value;
	}

	public String getSchemaLocation() {
		return _SchemaLocation;
	}

	// This attribute is an array, possibly empty
	public void setFinder(String[] value) {
		if (value == null)
			value = new String[0];
		_Finder.clear();
		((java.util.ArrayList) _Finder).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_Finder.add(value[i]);
		}
	}

	public void setFinder(int index, String value) {
		_Finder.set(index, value);
	}

	public String[] getFinder() {
		String[] arr = new String[_Finder.size()];
		return (String[]) _Finder.toArray(arr);
	}

	public java.util.List fetchFinderList() {
		return _Finder;
	}

	public String getFinder(int index) {
		return (String)_Finder.get(index);
	}

	// Return the number of finder
	public int sizeFinder() {
		return _Finder.size();
	}

	public int addFinder(String value) {
		_Finder.add(value);
		int positionOfNewItem = _Finder.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeFinder(String value) {
		int pos = _Finder.indexOf(value);
		if (pos >= 0) {
			_Finder.remove(pos);
		}
		return pos;
	}

	public void _setSchemaLocation(String location) {
		schemaLocation = location;
	}

	public String _getSchemaLocation() {
		return schemaLocation;
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.schema2beansdev.metadd.MetaElement newMetaElement() {
		return new org.netbeans.modules.schema2beansdev.metadd.MetaElement();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.schema2beansdev.metadd.MetaElement newMetaElement(MetaElement source, boolean justData) {
		return new org.netbeans.modules.schema2beansdev.metadd.MetaElement(source, justData);
	}

	public void write(java.io.File f) throws java.io.IOException {
		java.io.OutputStream out = new java.io.FileOutputStream(f);
		try {
			write(out);
		} finally {
			out.close();
		}
	}

	public void write(java.io.OutputStream out) throws java.io.IOException {
		write(out, null);
	}

	public void write(java.io.OutputStream out, String encoding) throws java.io.IOException {
		java.io.Writer w;
		if (encoding == null) {
			encoding = "UTF-8";	// NOI18N
		}
		w = new java.io.BufferedWriter(new java.io.OutputStreamWriter(out, encoding));
		write(w, encoding);
		w.flush();
	}

	/**
	 * Print this Java Bean to @param out including an XML header.
	 * @param encoding is the encoding style that @param out was opened with.
	 */
	public void write(java.io.Writer out, String encoding) throws java.io.IOException {
		out.write("<?xml version='1.0'");	// NOI18N
		if (encoding != null)
			out.write(" encoding='"+encoding+"'");	// NOI18N
		out.write(" ?>\n");	// NOI18N
		writeNode(out, "metaDD", "");	// NOI18N
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "metaDD";
		writeNode(out, myName, "");	// NOI18N
	}

	public void writeNode(java.io.Writer out, String nodeName, String indent) throws java.io.IOException {
		writeNode(out, nodeName, null, indent, new java.util.HashMap());
	}

	/**
	 * It's not recommended to call this method directly.
	 */
	public void writeNode(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		out.write(indent);
		out.write("<");
		if (namespace != null) {
			out.write((String)namespaceMap.get(namespace));
			out.write(":");
		}
		out.write(nodeName);
		if (schemaLocation != null) {
			namespaceMap.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
			out.write(" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='");
			out.write(schemaLocation);
			out.write("'");	// NOI18N
		}
		writeNodeAttributes(out, nodeName, namespace, indent, namespaceMap);
		out.write(">\n");
		writeNodeChildren(out, nodeName, namespace, indent, namespaceMap);
		out.write(indent);
		out.write("</");
		if (namespace != null) {
			out.write((String)namespaceMap.get(namespace));
			out.write(":");
		}
		out.write(nodeName);
		out.write(">\n");
	}

	protected void writeNodeAttributes(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
	}

	protected void writeNodeChildren(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		String nextIndent = indent + "	";
		for (java.util.Iterator it = _MetaElement.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.schema2beansdev.metadd.MetaElement element = (org.netbeans.modules.schema2beansdev.metadd.MetaElement)it.next();
			if (element != null) {
				element.writeNode(out, "meta-element", null, nextIndent, namespaceMap);
			}
		}
		if (_Implements != null) {
			out.write(nextIndent);
			out.write("<implements");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, _Implements, false);
			out.write("</implements>\n");	// NOI18N
		}
		if (_Extends != null) {
			out.write(nextIndent);
			out.write("<extends");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, _Extends, false);
			out.write("</extends>\n");	// NOI18N
		}
		for (java.util.Iterator it = _Import.iterator(); it.hasNext(); ) {
			String element = (String)it.next();
			if (element != null) {
				out.write(nextIndent);
				out.write("<import");	// NOI18N
				out.write(">");	// NOI18N
				org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, element, false);
				out.write("</import>\n");	// NOI18N
			}
		}
		if (_isSet_Vetoable) {
			if (_Vetoable) {
				out.write(nextIndent);
				out.write("<vetoable");	// NOI18N
				out.write("/>\n");	// NOI18N
			}
		}
		if (_isSet_ThrowExceptions) {
			if (_ThrowExceptions) {
				out.write(nextIndent);
				out.write("<throw-exceptions");	// NOI18N
				out.write("/>\n");	// NOI18N
			}
		}
		if (_SchemaLocation != null) {
			out.write(nextIndent);
			out.write("<schemaLocation");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, _SchemaLocation, false);
			out.write("</schemaLocation>\n");	// NOI18N
		}
		for (java.util.Iterator it = _Finder.iterator(); it.hasNext(); ) {
			String element = (String)it.next();
			if (element != null) {
				out.write(nextIndent);
				out.write("<finder");	// NOI18N
				out.write(">");	// NOI18N
				org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, element, false);
				out.write("</finder>\n");	// NOI18N
			}
		}
	}

	public static MetaDD read(java.io.File f) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return read(in);
		} finally {
			in.close();
		}
	}

	public static MetaDD read(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false, null, null);
	}

	/**
	 * Warning: in readNoEntityResolver character and entity references will
	 * not be read from any DTD in the XML source.
	 * However, this way is faster since no DTDs are looked up
	 * (possibly skipping network access) or parsed.
	 */
	public static MetaDD readNoEntityResolver(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false,
			new org.xml.sax.EntityResolver() {
			public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) {
				java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(new byte[0]);
				return new org.xml.sax.InputSource(bin);
			}
		}
			, null);
	}

	public static MetaDD read(org.xml.sax.InputSource in, boolean validate, org.xml.sax.EntityResolver er, org.xml.sax.ErrorHandler eh) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		dbf.setValidating(validate);
		dbf.setNamespaceAware(true);
		javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
		if (er != null)	db.setEntityResolver(er);
		if (eh != null)	db.setErrorHandler(eh);
		org.w3c.dom.Document doc = db.parse(in);
		return read(doc);
	}

	public static MetaDD read(org.w3c.dom.Document document) {
		MetaDD aMetaDD = new MetaDD();
		aMetaDD.readFromDocument(document);
		return aMetaDD;
	}

	protected void readFromDocument(org.w3c.dom.Document document) {
		readNode(document.getDocumentElement());
	}

	protected static class ReadState {
		int lastElementType;
		int elementPosition;
	}

	public void readNode(org.w3c.dom.Node node) {
		readNode(node, new java.util.HashMap());
	}

	public void readNode(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
		if (node.hasAttributes()) {
			org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
			org.w3c.dom.Attr attr;
			java.lang.String attrValue;
			boolean firstNamespaceDef = true;
			for (int attrNum = 0; attrNum < attrs.getLength(); ++attrNum) {
				attr = (org.w3c.dom.Attr) attrs.item(attrNum);
				String attrName = attr.getName();
				if (attrName.startsWith("xmlns:")) {
					if (firstNamespaceDef) {
						firstNamespaceDef = false;
						// Dup prefix map, so as to not write over previous values, and to make it easy to clear out our entries.
						namespacePrefixes = new java.util.HashMap(namespacePrefixes);
					}
					String attrNSPrefix = attrName.substring(6);
					namespacePrefixes.put(attrNSPrefix, attr.getValue());
				}
			}
			String xsiPrefix = "xsi";
			for (java.util.Iterator it = namespacePrefixes.entrySet().iterator(); 
				it.hasNext(); ) {
				java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
				String prefix = (String) entry.getKey();
				String ns = (String) entry.getValue();
				if ("http://www.w3.org/2001/XMLSchema-instance".equals(ns)) {
					xsiPrefix = prefix;
					break;
				}
			}
			attr = (org.w3c.dom.Attr) attrs.getNamedItem(""+xsiPrefix+":schemaLocation");
			if (attr != null) {
				attrValue = attr.getValue();
				schemaLocation = attrValue;
			}
			readNodeAttributes(node, namespacePrefixes, attrs);
		}
		readNodeChildren(node, namespacePrefixes);
	}

	protected void readNodeAttributes(org.w3c.dom.Node node, java.util.Map namespacePrefixes, org.w3c.dom.NamedNodeMap attrs) {
		org.w3c.dom.Attr attr;
		java.lang.String attrValue;
	}

	protected void readNodeChildren(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
		org.w3c.dom.NodeList children = node.getChildNodes();
		for (int i = 0, size = children.getLength(); i < size; ++i) {
			org.w3c.dom.Node childNode = children.item(i);
			if (!(childNode instanceof org.w3c.dom.Element)) {
				continue;
			}
			String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern());
			String childNodeValue = "";
			if (childNode.getFirstChild() != null) {
				childNodeValue = childNode.getFirstChild().getNodeValue();
			}
			boolean recognized = readNodeChild(childNode, childNodeName, childNodeValue, namespacePrefixes);
			if (!recognized) {
				if (childNode instanceof org.w3c.dom.Element) {
					_logger.info("Found extra unrecognized childNode '"+childNodeName+"'");
				}
			}
		}
	}

	protected boolean readNodeChild(org.w3c.dom.Node childNode, String childNodeName, String childNodeValue, java.util.Map namespacePrefixes) {
		// assert childNodeName == childNodeName.intern()
		if ("meta-element".equals(childNodeName)) {
			MetaElement aMetaElement = newMetaElement();
			aMetaElement.readNode(childNode, namespacePrefixes);
			_MetaElement.add(aMetaElement);
		}
		else if ("implements".equals(childNodeName)) {
			_Implements = childNodeValue;
		}
		else if ("extends".equals(childNodeName)) {
			_Extends = childNodeValue;
		}
		else if ("import".equals(childNodeName)) {
			String aImport;
			aImport = childNodeValue;
			_Import.add(aImport);
		}
		else if ("vetoable".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_Vetoable = true;
			else
				_Vetoable = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_Vetoable = true;
		}
		else if ("throw-exceptions".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_ThrowExceptions = true;
			else
				_ThrowExceptions = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_ThrowExceptions = true;
		}
		else if ("schemaLocation".equals(childNodeName)) {
			_SchemaLocation = childNodeValue;
		}
		else if ("finder".equals(childNodeName)) {
			String aFinder;
			aFinder = childNodeValue;
			_Finder.add(aFinder);
		}
		else {
			return false;
		}
		return true;
	}

	/**
	 * Takes some text to be printed into an XML stream and escapes any
	 * characters that might make it invalid XML (like '<').
	 */
	public static void writeXML(java.io.Writer out, String msg) throws java.io.IOException {
		writeXML(out, msg, true);
	}

	public static void writeXML(java.io.Writer out, String msg, boolean attribute) throws java.io.IOException {
		if (msg == null)
			return;
		int msgLength = msg.length();
		for (int i = 0; i < msgLength; ++i) {
			char c = msg.charAt(i);
			writeXML(out, c, attribute);
		}
	}

	public static void writeXML(java.io.Writer out, char msg, boolean attribute) throws java.io.IOException {
		if (msg == '&')
			out.write("&amp;");
		else if (msg == '<')
			out.write("&lt;");
		else if (msg == '>')
			out.write("&gt;");
		else if (attribute) {
			if (msg == '"')
				out.write("&quot;");
			else if (msg == '\'')
				out.write("&apos;");
			else if (msg == '\n')
				out.write("&#xA;");
			else if (msg == '\t')
				out.write("&#x9;");
			else
				out.write(msg);
		}
		else
			out.write(msg);
	}

	public static class ValidateException extends Exception {
		private org.netbeans.modules.schema2beansdev.metadd.CommonBean failedBean;
		private String failedPropertyName;
		private FailureType failureType;
		public ValidateException(String msg, String failedPropertyName, org.netbeans.modules.schema2beansdev.metadd.CommonBean failedBean) {
			super(msg);
			this.failedBean = failedBean;
			this.failedPropertyName = failedPropertyName;
		}
		public ValidateException(String msg, FailureType ft, String failedPropertyName, org.netbeans.modules.schema2beansdev.metadd.CommonBean failedBean) {
			super(msg);
			this.failureType = ft;
			this.failedBean = failedBean;
			this.failedPropertyName = failedPropertyName;
		}
		public String getFailedPropertyName() {return failedPropertyName;}
		public FailureType getFailureType() {return failureType;}
		public org.netbeans.modules.schema2beansdev.metadd.CommonBean getFailedBean() {return failedBean;}
		public static class FailureType {
			private final String name;
			private FailureType(String name) {this.name = name;}
			public String toString() { return name;}
			public static final FailureType NULL_VALUE = new FailureType("NULL_VALUE");
			public static final FailureType DATA_RESTRICTION = new FailureType("DATA_RESTRICTION");
			public static final FailureType ENUM_RESTRICTION = new FailureType("ENUM_RESTRICTION");
			public static final FailureType ALL_RESTRICTIONS = new FailureType("ALL_RESTRICTIONS");
			public static final FailureType MUTUALLY_EXCLUSIVE = new FailureType("MUTUALLY_EXCLUSIVE");
		}
	}

	public void validate() throws org.netbeans.modules.schema2beansdev.metadd.MetaDD.ValidateException {
		boolean restrictionFailure = false;
		boolean restrictionPassed = false;
		// Validating property metaElement
		for (int _index = 0; _index < sizeMetaElement(); ++_index) {
			org.netbeans.modules.schema2beansdev.metadd.MetaElement element = getMetaElement(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property implements
		// Validating property extends
		// Validating property import
		// Validating property vetoable
		// Validating property throwExceptions
		// Validating property schemaLocation
		// Validating property finder
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if ("metaElement".equals(name))
			addMetaElement((MetaElement)value);
		else if ("metaElement[]".equals(name))
			setMetaElement((MetaElement[]) value);
		else if ("implements".equals(name))
			setImplements((String)value);
		else if ("extends".equals(name))
			setExtends((String)value);
		else if ("import".equals(name))
			addImport((String)value);
		else if ("import[]".equals(name))
			setImport((String[]) value);
		else if ("vetoable".equals(name))
			setVetoable(((java.lang.Boolean)value).booleanValue());
		else if ("throwExceptions".equals(name))
			setThrowExceptions(((java.lang.Boolean)value).booleanValue());
		else if ("schemaLocation".equals(name))
			setSchemaLocation((String)value);
		else if ("finder".equals(name))
			addFinder((String)value);
		else if ("finder[]".equals(name))
			setFinder((String[]) value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for MetaDD");
	}

	public Object fetchPropertyByName(String name) {
		if ("metaElement[]".equals(name))
			return getMetaElement();
		if ("implements".equals(name))
			return getImplements();
		if ("extends".equals(name))
			return getExtends();
		if ("import[]".equals(name))
			return getImport();
		if ("vetoable".equals(name))
			return (isVetoable() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("throwExceptions".equals(name))
			return (isThrowExceptions() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("schemaLocation".equals(name))
			return getSchemaLocation();
		if ("finder[]".equals(name))
			return getFinder();
		throw new IllegalArgumentException(name+" is not a valid property name for MetaDD");
	}

	public String nameSelf() {
		return "/MetaDD";
	}

	public String nameChild(Object childObj) {
		return nameChild(childObj, false, false);
	}

	/**
	 * @param childObj  The child object to search for
	 * @param returnSchemaName  Whether or not the schema name should be returned or the property name
	 * @return null if not found
	 */
	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName) {
		return nameChild(childObj, returnConstName, returnSchemaName, false);
	}

	/**
	 * @param childObj  The child object to search for
	 * @param returnSchemaName  Whether or not the schema name should be returned or the property name
	 * @return null if not found
	 */
	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName, boolean returnXPathName) {
		if (childObj instanceof java.lang.Boolean) {
			java.lang.Boolean child = (java.lang.Boolean) childObj;
			if (((java.lang.Boolean)child).booleanValue() == _Vetoable) {
				if (returnConstName) {
					return VETOABLE;
				} else if (returnSchemaName) {
					return "vetoable";
				} else if (returnXPathName) {
					return "vetoable";
				} else {
					return "Vetoable";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _ThrowExceptions) {
				if (returnConstName) {
					return THROW_EXCEPTIONS;
				} else if (returnSchemaName) {
					return "throw-exceptions";
				} else if (returnXPathName) {
					return "throw-exceptions";
				} else {
					return "ThrowExceptions";
				}
			}
		}
		if (childObj instanceof MetaElement) {
			MetaElement child = (MetaElement) childObj;
			int index = 0;
			for (java.util.Iterator it = _MetaElement.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.schema2beansdev.metadd.MetaElement element = (org.netbeans.modules.schema2beansdev.metadd.MetaElement)it.next();
				if (child == element) {
					if (returnConstName) {
						return META_ELEMENT;
					} else if (returnSchemaName) {
						return "meta-element";
					} else if (returnXPathName) {
						return "meta-element[position()="+index+"]";
					} else {
						return "MetaElement."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		if (childObj instanceof java.lang.String) {
			java.lang.String child = (java.lang.String) childObj;
			if (child.equals(_Implements)) {
				if (returnConstName) {
					return IMPLEMENTS;
				} else if (returnSchemaName) {
					return "implements";
				} else if (returnXPathName) {
					return "implements";
				} else {
					return "Implements";
				}
			}
			if (child.equals(_Extends)) {
				if (returnConstName) {
					return EXTENDS;
				} else if (returnSchemaName) {
					return "extends";
				} else if (returnXPathName) {
					return "extends";
				} else {
					return "Extends";
				}
			}
			int index = 0;
			for (java.util.Iterator it = _Import.iterator(); it.hasNext(); 
				) {
				String element = (String)it.next();
				if (child.equals(element)) {
					if (returnConstName) {
						return IMPORT;
					} else if (returnSchemaName) {
						return "import";
					} else if (returnXPathName) {
						return "import[position()="+index+"]";
					} else {
						return "Import."+Integer.toHexString(index);
					}
				}
				++index;
			}
			if (child.equals(_SchemaLocation)) {
				if (returnConstName) {
					return SCHEMALOCATION;
				} else if (returnSchemaName) {
					return "schemaLocation";
				} else if (returnXPathName) {
					return "schemaLocation";
				} else {
					return "SchemaLocation";
				}
			}
			index = 0;
			for (java.util.Iterator it = _Finder.iterator(); it.hasNext(); 
				) {
				String element = (String)it.next();
				if (child.equals(element)) {
					if (returnConstName) {
						return FINDER;
					} else if (returnSchemaName) {
						return "finder";
					} else if (returnXPathName) {
						return "finder[position()="+index+"]";
					} else {
						return "Finder."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		return null;
	}

	/**
	 * Return an array of all of the properties that are beans and are set.
	 */
	public org.netbeans.modules.schema2beansdev.metadd.CommonBean[] childBeans(boolean recursive) {
		java.util.List children = new java.util.LinkedList();
		childBeans(recursive, children);
		org.netbeans.modules.schema2beansdev.metadd.CommonBean[] result = new org.netbeans.modules.schema2beansdev.metadd.CommonBean[children.size()];
		return (org.netbeans.modules.schema2beansdev.metadd.CommonBean[]) children.toArray(result);
	}

	/**
	 * Put all child beans into the beans list.
	 */
	public void childBeans(boolean recursive, java.util.List beans) {
		for (java.util.Iterator it = _MetaElement.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.schema2beansdev.metadd.MetaElement element = (org.netbeans.modules.schema2beansdev.metadd.MetaElement)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.schema2beansdev.metadd.MetaDD && equals((org.netbeans.modules.schema2beansdev.metadd.MetaDD) o);
	}

	public boolean equals(org.netbeans.modules.schema2beansdev.metadd.MetaDD inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (sizeMetaElement() != inst.sizeMetaElement())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _MetaElement.iterator(), it2 = inst._MetaElement.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.schema2beansdev.metadd.MetaElement element = (org.netbeans.modules.schema2beansdev.metadd.MetaElement)it.next();
			org.netbeans.modules.schema2beansdev.metadd.MetaElement element2 = (org.netbeans.modules.schema2beansdev.metadd.MetaElement)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (!(_Implements == null ? inst._Implements == null : _Implements.equals(inst._Implements))) {
			return false;
		}
		if (!(_Extends == null ? inst._Extends == null : _Extends.equals(inst._Extends))) {
			return false;
		}
		if (sizeImport() != inst.sizeImport())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _Import.iterator(), it2 = inst._Import.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			String element = (String)it.next();
			String element2 = (String)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (_isSet_Vetoable != inst._isSet_Vetoable) {
			return false;
		}
		if (_isSet_Vetoable) {
			if (!(_Vetoable == inst._Vetoable)) {
				return false;
			}
		}
		if (_isSet_ThrowExceptions != inst._isSet_ThrowExceptions) {
			return false;
		}
		if (_isSet_ThrowExceptions) {
			if (!(_ThrowExceptions == inst._ThrowExceptions)) {
				return false;
			}
		}
		if (!(_SchemaLocation == null ? inst._SchemaLocation == null : _SchemaLocation.equals(inst._SchemaLocation))) {
			return false;
		}
		if (sizeFinder() != inst.sizeFinder())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _Finder.iterator(), it2 = inst._Finder.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			String element = (String)it.next();
			String element2 = (String)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_MetaElement == null ? 0 : _MetaElement.hashCode());
		result = 37*result + (_Implements == null ? 0 : _Implements.hashCode());
		result = 37*result + (_Extends == null ? 0 : _Extends.hashCode());
		result = 37*result + (_Import == null ? 0 : _Import.hashCode());
		result = 37*result + (_isSet_Vetoable ? 0 : (_Vetoable ? 0 : 1));
		result = 37*result + (_isSet_ThrowExceptions ? 0 : (_ThrowExceptions ? 0 : 1));
		result = 37*result + (_SchemaLocation == null ? 0 : _SchemaLocation.hashCode());
		result = 37*result + (_Finder == null ? 0 : _Finder.hashCode());
		return result;
	}

	public String toString() {
		java.io.StringWriter sw = new java.io.StringWriter();
		try {
			writeNode(sw);
		} catch (java.io.IOException e) {
			// How can we actually get an IOException on a StringWriter?
			throw new RuntimeException(e);
		}
		return sw.toString();
	}

}


/*
		The following schema file has been used for generation:

<!-- This holds data about the schema that is not part of DTDs and/or XML Schemas.
-->

<!--
-->
<!ELEMENT metaDD (meta-element*, implements?, extends?, import*, vetoable?, throw-exceptions?, schemaLocation?, finder*)>

<!--
-->
<!ELEMENT meta-element (dtd-name, namespace?, bean-name?, bean-class?, wrapper-class?, default-value*, known-value*, meta-property*, comparator-class*, implements?, extends?, import*, user-code?, vetoable?, skip-generation?, delegator-name?, delegator-extends?, bean-interface-extends?, can-be-empty?>

<!--
-->
<!ELEMENT meta-property (bean-name, default-value*, known-value*, key?, vetoable?)>

<!ELEMENT delegator-name (#PCDATA)>

<!--
-->
<!ELEMENT implements (#PCDATA)>

<!--
-->
<!ELEMENT extends (#PCDATA)>

<!--
-->
<!ELEMENT import (#PCDATA)>

<!--
-->
<!ELEMENT dtd-name (#PCDATA)>

<!ELEMENT namespace (#PCDATA)>

<!--
-->
<!ELEMENT default-value (#PCDATA)>

<!--
-->
<!ELEMENT skip-generation EMPTY>

<!--
-->
<!ELEMENT key EMPTY>

<!--
-->
<!ELEMENT vetoable EMPTY>

<!--
-->
<!ELEMENT known-value (#PCDATA)>

<!--
-->
<!ELEMENT bean-name (#PCDATA)>

<!--
-->
<!ELEMENT bean-class (#PCDATA)>

<!--
-->
<!ELEMENT wrapper-class (#PCDATA)>

<!--
-->
<!ELEMENT comparator-class (#PCDATA)>

<!--
-->
<!ELEMENT user-code (#PCDATA)>

<!ELEMENT throw-exceptions EMPTY>

<!-- Automatically set the schemaLocation -->
<!ELEMENT schemaLocation (#PCDATA)>

<!ELEMENT finder (#PCDATA)>

<!ELEMENT bean-interface-extends (#PCDATA)>

<!ELEMENT can-be-empty EMPTY>

*/
