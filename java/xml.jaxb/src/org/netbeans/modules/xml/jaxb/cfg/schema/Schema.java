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
 *	This generated bean class Schema
 *	matches the schema element 'schema'.
 *  The root bean class is Schemas
 *
 *	Generated on Thu Apr 05 11:55:09 PDT 2007
 * @Generated
 */

package org.netbeans.modules.xml.jaxb.cfg.schema;

public class Schema {
	public static final String TYPE = "Type";	// NOI18N
	public static final String PACKAGE = "Package";	// NOI18N
	public static final String NAME = "Name";	// NOI18N
	public static final String XJC_OPTIONS = "XjcOptions";	// NOI18N
	public static final String SCHEMA_SOURCES = "SchemaSources";	// NOI18N
	public static final String BINDINGS = "Bindings";	// NOI18N
	public static final String CATALOG = "Catalog";	// NOI18N

	private java.lang.String _Type;
	private java.lang.String _Package;
	private java.lang.String _Name;
	private XjcOptions _XjcOptions;
	private SchemaSources _SchemaSources;
	private Bindings _Bindings;
	private Catalog _Catalog;

	/**
	 * Normal starting point constructor.
	 */
	public Schema() {
		_Name = "";
	}

	/**
	 * Required parameters constructor
	 */
	public Schema(java.lang.String name) {
		_Name = name;
	}

	/**
	 * Deep copy
	 */
	public Schema(org.netbeans.modules.xml.jaxb.cfg.schema.Schema source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public Schema(org.netbeans.modules.xml.jaxb.cfg.schema.Schema source, boolean justData) {
		_Type = source._Type;
		_Package = source._Package;
		_Name = source._Name;
		_XjcOptions = (source._XjcOptions == null) ? null : newXjcOptions(source._XjcOptions, justData);
		_SchemaSources = (source._SchemaSources == null) ? null : newSchemaSources(source._SchemaSources, justData);
		_Bindings = (source._Bindings == null) ? null : newBindings(source._Bindings, justData);
		_Catalog = (source._Catalog == null) ? null : newCatalog(source._Catalog, justData);
	}

	// This attribute is optional
	public void setType(java.lang.String value) {
		_Type = value;
	}

	public java.lang.String getType() {
		return _Type;
	}

	// This attribute is optional
	public void setPackage(java.lang.String value) {
		_Package = value;
	}

	public java.lang.String getPackage() {
		return _Package;
	}

	// This attribute is mandatory
	public void setName(java.lang.String value) {
		_Name = value;
	}

	public java.lang.String getName() {
		return _Name;
	}

	// This attribute is optional
	public void setXjcOptions(org.netbeans.modules.xml.jaxb.cfg.schema.XjcOptions value) {
		_XjcOptions = value;
	}

	public org.netbeans.modules.xml.jaxb.cfg.schema.XjcOptions getXjcOptions() {
		return _XjcOptions;
	}

	// This attribute is optional
	public void setSchemaSources(org.netbeans.modules.xml.jaxb.cfg.schema.SchemaSources value) {
		_SchemaSources = value;
	}

	public org.netbeans.modules.xml.jaxb.cfg.schema.SchemaSources getSchemaSources() {
		return _SchemaSources;
	}

	// This attribute is optional
	public void setBindings(org.netbeans.modules.xml.jaxb.cfg.schema.Bindings value) {
		_Bindings = value;
	}

	public org.netbeans.modules.xml.jaxb.cfg.schema.Bindings getBindings() {
		return _Bindings;
	}

	// This attribute is optional
	public void setCatalog(org.netbeans.modules.xml.jaxb.cfg.schema.Catalog value) {
		_Catalog = value;
	}

	public org.netbeans.modules.xml.jaxb.cfg.schema.Catalog getCatalog() {
		return _Catalog;
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.jaxb.cfg.schema.XjcOptions newXjcOptions() {
		return new org.netbeans.modules.xml.jaxb.cfg.schema.XjcOptions();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.jaxb.cfg.schema.XjcOptions newXjcOptions(XjcOptions source, boolean justData) {
		return new org.netbeans.modules.xml.jaxb.cfg.schema.XjcOptions(source, justData);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.jaxb.cfg.schema.SchemaSources newSchemaSources() {
		return new org.netbeans.modules.xml.jaxb.cfg.schema.SchemaSources();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.jaxb.cfg.schema.SchemaSources newSchemaSources(SchemaSources source, boolean justData) {
		return new org.netbeans.modules.xml.jaxb.cfg.schema.SchemaSources(source, justData);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.jaxb.cfg.schema.Bindings newBindings() {
		return new org.netbeans.modules.xml.jaxb.cfg.schema.Bindings();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.jaxb.cfg.schema.Bindings newBindings(Bindings source, boolean justData) {
		return new org.netbeans.modules.xml.jaxb.cfg.schema.Bindings(source, justData);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.jaxb.cfg.schema.Catalog newCatalog() {
		return new org.netbeans.modules.xml.jaxb.cfg.schema.Catalog();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.jaxb.cfg.schema.Catalog newCatalog(Catalog source, boolean justData) {
		return new org.netbeans.modules.xml.jaxb.cfg.schema.Catalog(source, justData);
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "schema";
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
		// type is an attribute with namespace http://xml.netbeans.org/schema/JAXBWizConfig
		if (_Type != null) {
			out.write(" type='");
			org.netbeans.modules.xml.jaxb.cfg.schema.Schemas.writeXML(out, _Type, true);
			out.write("'");	// NOI18N
		}
		// package is an attribute with namespace http://xml.netbeans.org/schema/JAXBWizConfig
		if (_Package != null) {
			out.write(" package='");
			org.netbeans.modules.xml.jaxb.cfg.schema.Schemas.writeXML(out, _Package, true);
			out.write("'");	// NOI18N
		}
		// name is an attribute with namespace http://xml.netbeans.org/schema/JAXBWizConfig
		if (_Name != null) {
			out.write(" name='");
			org.netbeans.modules.xml.jaxb.cfg.schema.Schemas.writeXML(out, _Name, true);
			out.write("'");	// NOI18N
		}
	}

	protected void writeNodeChildren(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		String nextIndent = indent + "	";
		if (_XjcOptions != null) {
			_XjcOptions.writeNode(out, "xjc-options", null, nextIndent, namespaceMap);
		}
		if (_SchemaSources != null) {
			_SchemaSources.writeNode(out, "schema-sources", null, nextIndent, namespaceMap);
		}
		if (_Bindings != null) {
			_Bindings.writeNode(out, "bindings", null, nextIndent, namespaceMap);
		}
		if (_Catalog != null) {
			_Catalog.writeNode(out, "catalog", null, nextIndent, namespaceMap);
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
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("type");
		if (attr != null) {
			attrValue = attr.getValue();
			_Type = attrValue;
		}
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("package");
		if (attr != null) {
			attrValue = attr.getValue();
			_Package = attrValue;
		}
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("name");
		if (attr != null) {
			attrValue = attr.getValue();
			_Name = attrValue;
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
			if (childNodeName == "xjc-options") {
				_XjcOptions = newXjcOptions();
				_XjcOptions.readNode(childNode, namespacePrefixes);
			}
			else if (childNodeName == "schema-sources") {
				_SchemaSources = newSchemaSources();
				_SchemaSources.readNode(childNode, namespacePrefixes);
			}
			else if (childNodeName == "bindings") {
				_Bindings = newBindings();
				_Bindings.readNode(childNode, namespacePrefixes);
			}
			else if (childNodeName == "catalog") {
				_Catalog = newCatalog();
				_Catalog.readNode(childNode, namespacePrefixes);
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "type")
			setType((java.lang.String)value);
		else if (name == "package")
			setPackage((java.lang.String)value);
		else if (name == "name")
			setName((java.lang.String)value);
		else if (name == "xjcOptions")
			setXjcOptions((XjcOptions)value);
		else if (name == "schemaSources")
			setSchemaSources((SchemaSources)value);
		else if (name == "bindings")
			setBindings((Bindings)value);
		else if (name == "catalog")
			setCatalog((Catalog)value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for Schema");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "type")
			return getType();
		if (name == "package")
			return getPackage();
		if (name == "name")
			return getName();
		if (name == "xjcOptions")
			return getXjcOptions();
		if (name == "schemaSources")
			return getSchemaSources();
		if (name == "bindings")
			return getBindings();
		if (name == "catalog")
			return getCatalog();
		throw new IllegalArgumentException(name+" is not a valid property name for Schema");
	}

	public String nameSelf() {
		return "Schema";
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
		if (childObj instanceof XjcOptions) {
			XjcOptions child = (XjcOptions) childObj;
			if (child == _XjcOptions) {
				if (returnConstName) {
					return XJC_OPTIONS;
				} else if (returnSchemaName) {
					return "xjc-options";
				} else if (returnXPathName) {
					return "xjc-options";
				} else {
					return "XjcOptions";
				}
			}
		}
		if (childObj instanceof SchemaSources) {
			SchemaSources child = (SchemaSources) childObj;
			if (child == _SchemaSources) {
				if (returnConstName) {
					return SCHEMA_SOURCES;
				} else if (returnSchemaName) {
					return "schema-sources";
				} else if (returnXPathName) {
					return "schema-sources";
				} else {
					return "SchemaSources";
				}
			}
		}
		if (childObj instanceof java.lang.String) {
			java.lang.String child = (java.lang.String) childObj;
			if (child == _Type) {
				if (returnConstName) {
					return TYPE;
				} else if (returnSchemaName) {
					return "type";
				} else if (returnXPathName) {
					return "@type";
				} else {
					return "Type";
				}
			}
			if (child == _Package) {
				if (returnConstName) {
					return PACKAGE;
				} else if (returnSchemaName) {
					return "package";
				} else if (returnXPathName) {
					return "@package";
				} else {
					return "Package";
				}
			}
			if (child == _Name) {
				if (returnConstName) {
					return NAME;
				} else if (returnSchemaName) {
					return "name";
				} else if (returnXPathName) {
					return "@name";
				} else {
					return "Name";
				}
			}
		}
		if (childObj instanceof Catalog) {
			Catalog child = (Catalog) childObj;
			if (child == _Catalog) {
				if (returnConstName) {
					return CATALOG;
				} else if (returnSchemaName) {
					return "catalog";
				} else if (returnXPathName) {
					return "catalog";
				} else {
					return "Catalog";
				}
			}
		}
		if (childObj instanceof Bindings) {
			Bindings child = (Bindings) childObj;
			if (child == _Bindings) {
				if (returnConstName) {
					return BINDINGS;
				} else if (returnSchemaName) {
					return "bindings";
				} else if (returnXPathName) {
					return "bindings";
				} else {
					return "Bindings";
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
		if (_XjcOptions != null) {
			if (recursive) {
				_XjcOptions.childBeans(true, beans);
			}
			beans.add(_XjcOptions);
		}
		if (_SchemaSources != null) {
			if (recursive) {
				_SchemaSources.childBeans(true, beans);
			}
			beans.add(_SchemaSources);
		}
		if (_Bindings != null) {
			if (recursive) {
				_Bindings.childBeans(true, beans);
			}
			beans.add(_Bindings);
		}
		if (_Catalog != null) {
			if (recursive) {
				_Catalog.childBeans(true, beans);
			}
			beans.add(_Catalog);
		}
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.xml.jaxb.cfg.schema.Schema && equals((org.netbeans.modules.xml.jaxb.cfg.schema.Schema) o);
	}

	public boolean equals(org.netbeans.modules.xml.jaxb.cfg.schema.Schema inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_Type == null ? inst._Type == null : _Type.equals(inst._Type))) {
			return false;
		}
		if (!(_Package == null ? inst._Package == null : _Package.equals(inst._Package))) {
			return false;
		}
		if (!(_Name == null ? inst._Name == null : _Name.equals(inst._Name))) {
			return false;
		}
		if (!(_XjcOptions == null ? inst._XjcOptions == null : _XjcOptions.equals(inst._XjcOptions))) {
			return false;
		}
		if (!(_SchemaSources == null ? inst._SchemaSources == null : _SchemaSources.equals(inst._SchemaSources))) {
			return false;
		}
		if (!(_Bindings == null ? inst._Bindings == null : _Bindings.equals(inst._Bindings))) {
			return false;
		}
		if (!(_Catalog == null ? inst._Catalog == null : _Catalog.equals(inst._Catalog))) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_Type == null ? 0 : _Type.hashCode());
		result = 37*result + (_Package == null ? 0 : _Package.hashCode());
		result = 37*result + (_Name == null ? 0 : _Name.hashCode());
		result = 37*result + (_XjcOptions == null ? 0 : _XjcOptions.hashCode());
		result = 37*result + (_SchemaSources == null ? 0 : _SchemaSources.hashCode());
		result = 37*result + (_Bindings == null ? 0 : _Bindings.hashCode());
		result = 37*result + (_Catalog == null ? 0 : _Catalog.hashCode());
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
