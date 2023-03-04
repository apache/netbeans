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
 *	This generated bean class SchemaTypeMappingType
 *	matches the schema element 'schemaTypeMappingType'.
 *  The root bean class is BeanGraph
 *
 *	===============================================================
 *	
 *			Map between schema types and java types.
 *		  
 *	===============================================================
 * @Generated
 */

package org.netbeans.modules.schema2beansdev.beangraph;

public class SchemaTypeMappingType implements org.netbeans.modules.schema2beansdev.beangraph.CommonBean {
	public static final String SCHEMA_TYPE_NAMESPACE = "SchemaTypeNamespace";	// NOI18N
	public static final String SCHEMA_TYPE_NAME = "SchemaTypeName";	// NOI18N
	public static final String JAVA_TYPE = "JavaType";	// NOI18N
	public static final String ROOT = "Root";	// NOI18N
	public static final String BEAN = "Bean";	// NOI18N
	public static final String CAN_BE_EMPTY = "CanBeEmpty";	// NOI18N

	private java.lang.String _SchemaTypeNamespace;
	private java.lang.String _SchemaTypeName;
	private java.lang.String _JavaType;
	private boolean _Root;
	private boolean _isSet_Root = false;
	private boolean _Bean;
	private boolean _isSet_Bean = false;
	private boolean _CanBeEmpty;
	private boolean _isSet_CanBeEmpty = false;
	private static final java.util.logging.Logger _logger = java.util.logging.Logger.getLogger("org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType");

	/**
	 * Normal starting point constructor.
	 */
	public SchemaTypeMappingType() {
		_SchemaTypeName = "";
		_JavaType = "";
	}

	/**
	 * Required parameters constructor
	 */
	public SchemaTypeMappingType(java.lang.String schemaTypeName, java.lang.String javaType) {
		_SchemaTypeName = schemaTypeName;
		_JavaType = javaType;
	}

	/**
	 * Deep copy
	 */
	public SchemaTypeMappingType(org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public SchemaTypeMappingType(org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType source, boolean justData) {
		_SchemaTypeNamespace = source._SchemaTypeNamespace;
		_SchemaTypeName = source._SchemaTypeName;
		_JavaType = source._JavaType;
		_Root = source._Root;
		_isSet_Root = source._isSet_Root;
		_Bean = source._Bean;
		_isSet_Bean = source._isSet_Bean;
		_CanBeEmpty = source._CanBeEmpty;
		_isSet_CanBeEmpty = source._isSet_CanBeEmpty;
	}

	// This attribute is optional
	public void setSchemaTypeNamespace(java.lang.String value) {
		_SchemaTypeNamespace = value;
	}

	public java.lang.String getSchemaTypeNamespace() {
		return _SchemaTypeNamespace;
	}

	// This attribute is mandatory
	public void setSchemaTypeName(java.lang.String value) {
		_SchemaTypeName = value;
	}

	public java.lang.String getSchemaTypeName() {
		return _SchemaTypeName;
	}

	// This attribute is mandatory
	public void setJavaType(java.lang.String value) {
		_JavaType = value;
	}

	public java.lang.String getJavaType() {
		return _JavaType;
	}

	// This attribute is optional
	public void setRoot(boolean value) {
		_Root = value;
		_isSet_Root = true;
	}

	public boolean isRoot() {
		return _Root;
	}

	// This attribute is optional
	public void setBean(boolean value) {
		_Bean = value;
		_isSet_Bean = true;
	}

	public boolean isBean() {
		return _Bean;
	}

	// This attribute is optional
	public void setCanBeEmpty(boolean value) {
		_CanBeEmpty = value;
		_isSet_CanBeEmpty = true;
	}

	public boolean isCanBeEmpty() {
		return _CanBeEmpty;
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "schemaTypeMappingType";
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
		if (_SchemaTypeNamespace != null) {
			out.write(nextIndent);
			out.write("<schema-type-namespace");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.beangraph.BeanGraph.writeXML(out, _SchemaTypeNamespace, false);
			out.write("</schema-type-namespace>\n");	// NOI18N
		}
		if (_SchemaTypeName != null) {
			out.write(nextIndent);
			out.write("<schema-type-name");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.beangraph.BeanGraph.writeXML(out, _SchemaTypeName, false);
			out.write("</schema-type-name>\n");	// NOI18N
		}
		if (_JavaType != null) {
			out.write(nextIndent);
			out.write("<java-type");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.beangraph.BeanGraph.writeXML(out, _JavaType, false);
			out.write("</java-type>\n");	// NOI18N
		}
		if (_isSet_Root) {
			out.write(nextIndent);
			out.write("<root");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_Root ? "true" : "false");
			out.write("</root>\n");	// NOI18N
		}
		if (_isSet_Bean) {
			out.write(nextIndent);
			out.write("<bean");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_Bean ? "true" : "false");
			out.write("</bean>\n");	// NOI18N
		}
		if (_isSet_CanBeEmpty) {
			out.write(nextIndent);
			out.write("<can-be-empty");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_CanBeEmpty ? "true" : "false");
			out.write("</can-be-empty>\n");	// NOI18N
		}
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
		if ("schema-type-namespace".equals(childNodeName)) {
			_SchemaTypeNamespace = childNodeValue;
		}
		else if ("schema-type-name".equals(childNodeName)) {
			_SchemaTypeName = childNodeValue;
		}
		else if ("java-type".equals(childNodeName)) {
			_JavaType = childNodeValue;
		}
		else if ("root".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_Root = true;
			else
				_Root = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_Root = true;
		}
		else if ("bean".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_Bean = true;
			else
				_Bean = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_Bean = true;
		}
		else if ("can-be-empty".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_CanBeEmpty = true;
			else
				_CanBeEmpty = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_CanBeEmpty = true;
		}
		else {
			return false;
		}
		return true;
	}

	public void validate() throws org.netbeans.modules.schema2beansdev.beangraph.BeanGraph.ValidateException {
		boolean restrictionFailure = false;
		boolean restrictionPassed = false;
		// Validating property schemaTypeNamespace
		// Validating property schemaTypeName
		if (getSchemaTypeName() == null) {
			throw new org.netbeans.modules.schema2beansdev.beangraph.BeanGraph.ValidateException("getSchemaTypeName() == null", org.netbeans.modules.schema2beansdev.beangraph.BeanGraph.ValidateException.FailureType.NULL_VALUE, "schemaTypeName", this);	// NOI18N
		}
		// Validating property javaType
		if (getJavaType() == null) {
			throw new org.netbeans.modules.schema2beansdev.beangraph.BeanGraph.ValidateException("getJavaType() == null", org.netbeans.modules.schema2beansdev.beangraph.BeanGraph.ValidateException.FailureType.NULL_VALUE, "javaType", this);	// NOI18N
		}
		// Validating property root
		// Validating property bean
		// Validating property canBeEmpty
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if ("schemaTypeNamespace".equals(name))
			setSchemaTypeNamespace((java.lang.String)value);
		else if ("schemaTypeName".equals(name))
			setSchemaTypeName((java.lang.String)value);
		else if ("javaType".equals(name))
			setJavaType((java.lang.String)value);
		else if ("root".equals(name))
			setRoot(((java.lang.Boolean)value).booleanValue());
		else if ("bean".equals(name))
			setBean(((java.lang.Boolean)value).booleanValue());
		else if ("canBeEmpty".equals(name))
			setCanBeEmpty(((java.lang.Boolean)value).booleanValue());
		else
			throw new IllegalArgumentException(name+" is not a valid property name for SchemaTypeMappingType");
	}

	public Object fetchPropertyByName(String name) {
		if ("schemaTypeNamespace".equals(name))
			return getSchemaTypeNamespace();
		if ("schemaTypeName".equals(name))
			return getSchemaTypeName();
		if ("javaType".equals(name))
			return getJavaType();
		if ("root".equals(name))
			return (isRoot() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("bean".equals(name))
			return (isBean() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("canBeEmpty".equals(name))
			return (isCanBeEmpty() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		throw new IllegalArgumentException(name+" is not a valid property name for SchemaTypeMappingType");
	}

	public String nameSelf() {
		return "SchemaTypeMappingType";
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
			if (((java.lang.Boolean)child).booleanValue() == _Root) {
				if (returnConstName) {
					return ROOT;
				} else if (returnSchemaName) {
					return "root";
				} else if (returnXPathName) {
					return "root";
				} else {
					return "Root";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _Bean) {
				if (returnConstName) {
					return BEAN;
				} else if (returnSchemaName) {
					return "bean";
				} else if (returnXPathName) {
					return "bean";
				} else {
					return "Bean";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _CanBeEmpty) {
				if (returnConstName) {
					return CAN_BE_EMPTY;
				} else if (returnSchemaName) {
					return "can-be-empty";
				} else if (returnXPathName) {
					return "can-be-empty";
				} else {
					return "CanBeEmpty";
				}
			}
		}
		if (childObj instanceof java.lang.String) {
			java.lang.String child = (java.lang.String) childObj;
			if (child.equals(_SchemaTypeNamespace)) {
				if (returnConstName) {
					return SCHEMA_TYPE_NAMESPACE;
				} else if (returnSchemaName) {
					return "schema-type-namespace";
				} else if (returnXPathName) {
					return "schema-type-namespace";
				} else {
					return "SchemaTypeNamespace";
				}
			}
			if (child.equals(_SchemaTypeName)) {
				if (returnConstName) {
					return SCHEMA_TYPE_NAME;
				} else if (returnSchemaName) {
					return "schema-type-name";
				} else if (returnXPathName) {
					return "schema-type-name";
				} else {
					return "SchemaTypeName";
				}
			}
			if (child.equals(_JavaType)) {
				if (returnConstName) {
					return JAVA_TYPE;
				} else if (returnSchemaName) {
					return "java-type";
				} else if (returnXPathName) {
					return "java-type";
				} else {
					return "JavaType";
				}
			}
		}
		return null;
	}

	/**
	 * Return an array of all of the properties that are beans and are set.
	 */
	public org.netbeans.modules.schema2beansdev.beangraph.CommonBean[] childBeans(boolean recursive) {
		java.util.List children = new java.util.LinkedList();
		childBeans(recursive, children);
		org.netbeans.modules.schema2beansdev.beangraph.CommonBean[] result = new org.netbeans.modules.schema2beansdev.beangraph.CommonBean[children.size()];
		return (org.netbeans.modules.schema2beansdev.beangraph.CommonBean[]) children.toArray(result);
	}

	/**
	 * Put all child beans into the beans list.
	 */
	public void childBeans(boolean recursive, java.util.List beans) {
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType && equals((org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType) o);
	}

	public boolean equals(org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_SchemaTypeNamespace == null ? inst._SchemaTypeNamespace == null : _SchemaTypeNamespace.equals(inst._SchemaTypeNamespace))) {
			return false;
		}
		if (!(_SchemaTypeName == null ? inst._SchemaTypeName == null : _SchemaTypeName.equals(inst._SchemaTypeName))) {
			return false;
		}
		if (!(_JavaType == null ? inst._JavaType == null : _JavaType.equals(inst._JavaType))) {
			return false;
		}
		if (_isSet_Root != inst._isSet_Root) {
			return false;
		}
		if (_isSet_Root) {
			if (!(_Root == inst._Root)) {
				return false;
			}
		}
		if (_isSet_Bean != inst._isSet_Bean) {
			return false;
		}
		if (_isSet_Bean) {
			if (!(_Bean == inst._Bean)) {
				return false;
			}
		}
		if (_isSet_CanBeEmpty != inst._isSet_CanBeEmpty) {
			return false;
		}
		if (_isSet_CanBeEmpty) {
			if (!(_CanBeEmpty == inst._CanBeEmpty)) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_SchemaTypeNamespace == null ? 0 : _SchemaTypeNamespace.hashCode());
		result = 37*result + (_SchemaTypeName == null ? 0 : _SchemaTypeName.hashCode());
		result = 37*result + (_JavaType == null ? 0 : _JavaType.hashCode());
		result = 37*result + (_isSet_Root ? 0 : (_Root ? 0 : 1));
		result = 37*result + (_isSet_Bean ? 0 : (_Bean ? 0 : 1));
		result = 37*result + (_isSet_CanBeEmpty ? 0 : (_CanBeEmpty ? 0 : 1));
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

<?xml version="1.0" ?>

<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema">
  <xsd:element name="bean-graph">
	<xsd:complexType>
	  <xsd:sequence>
		<xsd:element name="schema-type-mapping" type="schemaTypeMappingType"
		  minOccurs="0" maxOccurs="unbounded"/>
	  </xsd:sequence>
	</xsd:complexType>
  </xsd:element>

  <xsd:complexType name="schemaTypeMappingType">
	<xsd:annotation>
	  <xsd:documentation>
		Map between schema types and java types.
	  </xsd:documentation>
	</xsd:annotation>
	<xsd:sequence>
	  <xsd:element name="schema-type-namespace" type="xsd:string" minOccurs="0"/>
	  <xsd:element name="schema-type-name" type="xsd:string">
		<xsd:annotation>
		  <xsd:documentation>
			The schema type; for instance, "string"
		  </xsd:documentation>
		</xsd:annotation>
	  </xsd:element>
	  <xsd:element name="java-type" type="xsd:string">
		<xsd:annotation>
		  <xsd:documentation>
			The java type; for instance, "java.lang.String", or "int"
		  </xsd:documentation>
		</xsd:annotation>
	  </xsd:element>
	  <xsd:element name="root" type="xsd:boolean" minOccurs="0"/>
	  <xsd:element name="bean" type="xsd:boolean" minOccurs="0"/>
	  <xsd:element name="can-be-empty" type="xsd:boolean" minOccurs="0"/>
	</xsd:sequence>
  </xsd:complexType>
</xsd:schema>

*/
