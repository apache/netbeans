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
 *	This generated bean class Schemas
 *	matches the schema element 'schemas'.
 *
 *	Generated on Thu Apr 05 11:55:09 PDT 2007
 *
 *	This class matches the root element of the XML Schema,
 *	and is the root of the bean graph.
 *
 * 	schemas <schemas> : Schemas
 * 		[attr: destdir CDATA #IMPLIED  : java.lang.String]
 * 		[attr: projectName CDATA #IMPLIED  : java.lang.String]
 * 		[attr: version CDATA #IMPLIED  : java.math.BigDecimal]
 * 		schema <schema> : Schema[0,n]
 * 			[attr: type CDATA #IMPLIED  : java.lang.String]
 * 			[attr: package CDATA #IMPLIED  : java.lang.String]
 * 			[attr: name CDATA #REQUIRED  : java.lang.String]
 * 			xjcOptions <xjc-options> : XjcOptions[0,1]
 * 				xjcOption <xjc-option> : XjcOption[0,n]
 * 					[attr: name CDATA #IMPLIED  : java.lang.String]
 * 					[attr: value CDATA #IMPLIED  : java.lang.String]
 * 			schemaSources <schema-sources> : SchemaSources[0,1]
 * 				schemaSource <schema-source> : SchemaSource[0,n]
 * 					[attr: location CDATA #IMPLIED  : java.lang.String]
 * 					[attr: origLocation CDATA #IMPLIED  : java.lang.String]
 * 					[attr: origLocationType CDATA #IMPLIED  : java.lang.String]
 * 			bindings <bindings> : Bindings[0,1]
 * 				binding <binding> : Binding[0,n]
 * 					[attr: origLocation CDATA #IMPLIED  : java.lang.String]
 * 					[attr: location CDATA #IMPLIED  : java.lang.String]
 * 			catalog <catalog> : Catalog[0,1]
 * 				[attr: location CDATA #IMPLIED  : java.lang.String]
 * 				[attr: origLocation CDATA #IMPLIED  : java.lang.String]
 *
 * @Generated
 */

package org.netbeans.modules.xml.jaxb.cfg.schema;

public class Schemas {
	public static final String DESTDIR = "Destdir";	// NOI18N
	public static final String PROJECTNAME = "ProjectName";	// NOI18N
	public static final String VERSION = "Version";	// NOI18N
	public static final String SCHEMA = "Schema";	// NOI18N

	private java.lang.String _Destdir;
	private java.lang.String _ProjectName;
	private java.math.BigDecimal _Version;
	private java.util.List _Schema = new java.util.ArrayList();	// List<Schema>
	private java.lang.String schemaLocation;

	/**
	 * Normal starting point constructor.
	 */
	public Schemas() {
	}

	/**
	 * Deep copy
	 */
	public Schemas(org.netbeans.modules.xml.jaxb.cfg.schema.Schemas source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public Schemas(org.netbeans.modules.xml.jaxb.cfg.schema.Schemas source, boolean justData) {
		_Destdir = source._Destdir;
		_ProjectName = source._ProjectName;
		_Version = source._Version;
		for (java.util.Iterator it = source._Schema.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.xml.jaxb.cfg.schema.Schema srcElement = (org.netbeans.modules.xml.jaxb.cfg.schema.Schema)it.next();
			_Schema.add((srcElement == null) ? null : newSchema(srcElement, justData));
		}
		schemaLocation = source.schemaLocation;
	}

	// This attribute is optional
	public void setDestdir(java.lang.String value) {
		_Destdir = value;
	}

	public java.lang.String getDestdir() {
		return _Destdir;
	}

	// This attribute is optional
	public void setProjectName(java.lang.String value) {
		_ProjectName = value;
	}

	public java.lang.String getProjectName() {
		return _ProjectName;
	}

	// This attribute is optional
	public void setVersion(java.math.BigDecimal value) {
		_Version = value;
	}

	public java.math.BigDecimal getVersion() {
		return _Version;
	}

	// This attribute is an array, possibly empty
	public void setSchema(org.netbeans.modules.xml.jaxb.cfg.schema.Schema[] value) {
		if (value == null)
			value = new Schema[0];
		_Schema.clear();
		((java.util.ArrayList) _Schema).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_Schema.add(value[i]);
		}
	}

	public void setSchema(int index,org.netbeans.modules.xml.jaxb.cfg.schema.Schema value) {
		_Schema.set(index, value);
	}

	public org.netbeans.modules.xml.jaxb.cfg.schema.Schema[] getSchema() {
		Schema[] arr = new Schema[_Schema.size()];
		return (Schema[]) _Schema.toArray(arr);
	}

	public java.util.List fetchSchemaList() {
		return _Schema;
	}

	public org.netbeans.modules.xml.jaxb.cfg.schema.Schema getSchema(int index) {
		return (Schema)_Schema.get(index);
	}

	// Return the number of schema
	public int sizeSchema() {
		return _Schema.size();
	}

	public int addSchema(org.netbeans.modules.xml.jaxb.cfg.schema.Schema value) {
		_Schema.add(value);
		int positionOfNewItem = _Schema.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeSchema(org.netbeans.modules.xml.jaxb.cfg.schema.Schema value) {
		int pos = _Schema.indexOf(value);
		if (pos >= 0) {
			_Schema.remove(pos);
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
	public org.netbeans.modules.xml.jaxb.cfg.schema.Schema newSchema() {
		return new org.netbeans.modules.xml.jaxb.cfg.schema.Schema();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.jaxb.cfg.schema.Schema newSchema(Schema source, boolean justData) {
		return new org.netbeans.modules.xml.jaxb.cfg.schema.Schema(source, justData);
	}

	public void write(org.openide.filesystems.FileObject fo) throws java.io.IOException {
		org.openide.filesystems.FileLock lock = fo.lock();
		try {
			java.io.OutputStream out = fo.getOutputStream(lock);
			write(out);
			out.close();
		} finally {
			lock.releaseLock();
		}
	}

	public void write(org.openide.filesystems.FileObject dir, String filename) throws java.io.IOException {
		org.openide.filesystems.FileObject file = dir.getFileObject(filename);
		if (file == null) {
			file = dir.createData(filename);
		}
		write(file);
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
		writeNode(out, "schemas", "");	// NOI18N
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "schemas";
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
		out.write(" xmlns='");	// NOI18N
		out.write("http://xml.netbeans.org/schema/JAXBWizConfig");	// NOI18N
		out.write("'");	// NOI18N
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
		// destdir is an attribute with namespace http://xml.netbeans.org/schema/JAXBWizConfig
		if (_Destdir != null) {
			out.write(" destdir='");
			org.netbeans.modules.xml.jaxb.cfg.schema.Schemas.writeXML(out, _Destdir, true);
			out.write("'");	// NOI18N
		}
		// projectName is an attribute with namespace http://xml.netbeans.org/schema/JAXBWizConfig
		if (_ProjectName != null) {
			out.write(" projectName='");
			org.netbeans.modules.xml.jaxb.cfg.schema.Schemas.writeXML(out, _ProjectName, true);
			out.write("'");	// NOI18N
		}
		// version is an attribute with namespace http://xml.netbeans.org/schema/JAXBWizConfig
		if (_Version != null) {
			out.write(" version='");
			out.write(_Version.toString());
			out.write("'");	// NOI18N
		}
	}

	protected void writeNodeChildren(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		String nextIndent = indent + "	";
		for (java.util.Iterator it = _Schema.iterator(); it.hasNext(); ) {
			org.netbeans.modules.xml.jaxb.cfg.schema.Schema element = (org.netbeans.modules.xml.jaxb.cfg.schema.Schema)it.next();
			if (element != null) {
				element.writeNode(out, "schema", null, nextIndent, namespaceMap);
			}
		}
	}

	public static Schemas read(org.openide.filesystems.FileObject fo) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		java.io.InputStream in = fo.getInputStream();
		try {
			return read(in);
		} finally {
			in.close();
		}
	}

	public static Schemas read(java.io.File f) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return read(in);
		} finally {
			in.close();
		}
	}

	public static Schemas read(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false, null, null);
	}

	/**
	 * Warning: in readNoEntityResolver character and entity references will
	 * not be read from any DTD in the XML source.
	 * However, this way is faster since no DTDs are looked up
	 * (possibly skipping network access) or parsed.
	 */
	public static Schemas readNoEntityResolver(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false,
			new org.xml.sax.EntityResolver() {
			public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) {
				java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(new byte[0]);
				return new org.xml.sax.InputSource(bin);
			}
		}
			, null);
	}

	public static Schemas read(org.xml.sax.InputSource in, boolean validate, org.xml.sax.EntityResolver er, org.xml.sax.ErrorHandler eh) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		dbf.setValidating(validate);
		dbf.setNamespaceAware(true);
		javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
		if (er != null)	db.setEntityResolver(er);
		if (eh != null)	db.setErrorHandler(eh);
		org.w3c.dom.Document doc = db.parse(in);
		return read(doc);
	}

	public static Schemas read(org.w3c.dom.Document document) {
		Schemas aSchemas = new Schemas();
		aSchemas.readFromDocument(document);
		return aSchemas;
	}

	protected void readFromDocument(org.w3c.dom.Document document) {
		readNode(document.getDocumentElement());
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
			for (java.util.Iterator it = namespacePrefixes.keySet().iterator(); 
				it.hasNext(); ) {
				String prefix = (String) it.next();
				String ns = (String) namespacePrefixes.get(prefix);
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
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("destdir");
		if (attr != null) {
			attrValue = attr.getValue();
			_Destdir = attrValue;
		}
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("projectName");
		if (attr != null) {
			attrValue = attr.getValue();
			_ProjectName = attrValue;
		}
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("version");
		if (attr != null) {
			attrValue = attr.getValue();
			_Version = new java.math.BigDecimal(attrValue);
		}
	}

	protected void readNodeChildren(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
		org.w3c.dom.NodeList children = node.getChildNodes();
		for (int i = 0, size = children.getLength(); i < size; ++i) {
			org.w3c.dom.Node childNode = children.item(i);
			String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern());
			String childNodeValue = "";
			if (childNode.getFirstChild() != null) {
				childNodeValue = childNode.getFirstChild().getNodeValue();
			}
			if (childNodeName == "schema") {
				Schema aSchema = newSchema();
				aSchema.readNode(childNode, namespacePrefixes);
				_Schema.add(aSchema);
			}
			else {
				// Found extra unrecognized childNode
			}
		}
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

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "destdir")
			setDestdir((java.lang.String)value);
		else if (name == "projectName")
			setProjectName((java.lang.String)value);
		else if (name == "version")
			setVersion((java.math.BigDecimal)value);
		else if (name == "schema")
			addSchema((Schema)value);
		else if (name == "schema[]")
			setSchema((Schema[]) value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for Schemas");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "destdir")
			return getDestdir();
		if (name == "projectName")
			return getProjectName();
		if (name == "version")
			return getVersion();
		if (name == "schema[]")
			return getSchema();
		throw new IllegalArgumentException(name+" is not a valid property name for Schemas");
	}

	public String nameSelf() {
		return "/Schemas";
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
		if (childObj instanceof Schema) {
			Schema child = (Schema) childObj;
			int index = 0;
			for (java.util.Iterator it = _Schema.iterator(); it.hasNext(); 
				) {
				org.netbeans.modules.xml.jaxb.cfg.schema.Schema element = (org.netbeans.modules.xml.jaxb.cfg.schema.Schema)it.next();
				if (child == element) {
					if (returnConstName) {
						return SCHEMA;
					} else if (returnSchemaName) {
						return "schema";
					} else if (returnXPathName) {
						return "schema[position()="+index+"]";
					} else {
						return "Schema."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		if (childObj instanceof java.lang.String) {
			java.lang.String child = (java.lang.String) childObj;
			if (child == _Destdir) {
				if (returnConstName) {
					return DESTDIR;
				} else if (returnSchemaName) {
					return "destdir";
				} else if (returnXPathName) {
					return "@destdir";
				} else {
					return "Destdir";
				}
			}
			if (child == _ProjectName) {
				if (returnConstName) {
					return PROJECTNAME;
				} else if (returnSchemaName) {
					return "projectName";
				} else if (returnXPathName) {
					return "@projectName";
				} else {
					return "ProjectName";
				}
			}
		}
		if (childObj instanceof java.math.BigDecimal) {
			java.math.BigDecimal child = (java.math.BigDecimal) childObj;
			if (child == _Version) {
				if (returnConstName) {
					return VERSION;
				} else if (returnSchemaName) {
					return "version";
				} else if (returnXPathName) {
					return "@version";
				} else {
					return "Version";
				}
			}
		}
		return null;
	}

	/**
	 * Return an array of all of the properties that are beans and are set.
	 */
	public java.lang.Object[] childBeans(boolean recursive) {
		java.util.List children = new java.util.LinkedList();
		childBeans(recursive, children);
		java.lang.Object[] result = new java.lang.Object[children.size()];
		return (java.lang.Object[]) children.toArray(result);
	}

	/**
	 * Put all child beans into the beans list.
	 */
	public void childBeans(boolean recursive, java.util.List beans) {
		for (java.util.Iterator it = _Schema.iterator(); it.hasNext(); ) {
			org.netbeans.modules.xml.jaxb.cfg.schema.Schema element = (org.netbeans.modules.xml.jaxb.cfg.schema.Schema)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.xml.jaxb.cfg.schema.Schemas && equals((org.netbeans.modules.xml.jaxb.cfg.schema.Schemas) o);
	}

	public boolean equals(org.netbeans.modules.xml.jaxb.cfg.schema.Schemas inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_Destdir == null ? inst._Destdir == null : _Destdir.equals(inst._Destdir))) {
			return false;
		}
		if (!(_ProjectName == null ? inst._ProjectName == null : _ProjectName.equals(inst._ProjectName))) {
			return false;
		}
		if (!(_Version == null ? inst._Version == null : _Version.equals(inst._Version))) {
			return false;
		}
		if (sizeSchema() != inst.sizeSchema())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _Schema.iterator(), it2 = inst._Schema.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.xml.jaxb.cfg.schema.Schema element = (org.netbeans.modules.xml.jaxb.cfg.schema.Schema)it.next();
			org.netbeans.modules.xml.jaxb.cfg.schema.Schema element2 = (org.netbeans.modules.xml.jaxb.cfg.schema.Schema)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_Destdir == null ? 0 : _Destdir.hashCode());
		result = 37*result + (_ProjectName == null ? 0 : _ProjectName.hashCode());
		result = 37*result + (_Version == null ? 0 : _Version.hashCode());
		result = 37*result + (_Schema == null ? 0 : _Schema.hashCode());
		return result;
	}

}


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://xml.netbeans.org/schema/JAXBWizConfig"
            xmlns:tns="http://xml.netbeans.org/schema/JAXBWizConfig"
            elementFormDefault="qualified">
                
    <xsd:element name="schemas">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element ref="tns:schema" maxOccurs="unbounded" minOccurs="0"/>
            </xsd:sequence>
            <xsd:attribute name="destdir" type="xsd:string"/>
            <xsd:attribute name="projectName" type="xsd:string"/>
            <xsd:attribute name="version" type="xsd:decimal"/>
        </xsd:complexType>
    </xsd:element>
    
    <xsd:element name="schema">
        <xsd:complexType xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <xsd:sequence>
                <xsd:element ref="tns:xjc-options" minOccurs="0"/>
                <xsd:element ref="tns:schema-sources" minOccurs="0"/>
                <xsd:element ref="tns:bindings" minOccurs="0"/>
                <xsd:element ref="tns:catalog" minOccurs="0"/>
            </xsd:sequence>
            <xsd:attribute name="type" type="xsd:string"/>
            <xsd:attribute name="package" type="xsd:string"/>
            <xsd:attribute name="name" type="xsd:string" use="required"/>
        </xsd:complexType>
    </xsd:element>
    
    <xsd:element name="xjc-options">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element ref="tns:xjc-option" maxOccurs="unbounded" minOccurs="0"/>
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>
    
    <xsd:element name="xjc-option">
        <xsd:complexType>
            <xsd:sequence/>
            <xsd:attribute name="name" type="xsd:string"/>
            <xsd:attribute name="value" type="xsd:string"/>
        </xsd:complexType>
    </xsd:element>
    
    <xsd:element name="schema-sources">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element ref="tns:schema-source" maxOccurs="unbounded" minOccurs="0"/>
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>
    
    <xsd:element name="schema-source">
        <xsd:complexType>
            <xsd:sequence/>
            <xsd:attribute name="location" type="xsd:string">
                <xsd:annotation>
                    <xsd:documentation>location is relative to Project Root. origLocation is either local file path or URL</xsd:documentation>
                </xsd:annotation>
            </xsd:attribute>
            <xsd:attribute name="origLocation" type="xsd:string"/>
            <xsd:attribute name="origLocationType" type="xsd:string"/>
        </xsd:complexType>
    </xsd:element>
    
    <xsd:element name="bindings">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element ref="tns:binding" maxOccurs="unbounded" minOccurs="0"/>
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>
    
    <xsd:element name="binding">
        <xsd:complexType>
            <xsd:sequence/>
            <xsd:attribute name="origLocation" type="xsd:string"/>
            <xsd:attribute name="location" type="xsd:string"/>
        </xsd:complexType>
    </xsd:element>
    
    <xsd:element name="catalog">
        <xsd:complexType>
            <xsd:sequence/>
            <xsd:attribute name="location" type="xsd:string"/>
            <xsd:attribute name="origLocation" type="xsd:string"/>
        </xsd:complexType>
    </xsd:element>
</xsd:schema>

*/
