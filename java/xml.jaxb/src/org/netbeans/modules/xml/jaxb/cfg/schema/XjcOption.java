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
/**
 *	This generated bean class XjcOption
 *	matches the schema element 'xjc-option'.
 *  The root bean class is Schemas
 *
 *	Generated on Thu Apr 05 11:55:09 PDT 2007
 * @Generated
 */

package org.netbeans.modules.xml.jaxb.cfg.schema;

public class XjcOption {
	public static final String NAME = "Name";	// NOI18N
	public static final String VALUE = "Value";	// NOI18N

	private java.lang.String _Name;
	private java.lang.String _Value;

	/**
	 * Normal starting point constructor.
	 */
	public XjcOption() {
	}

	/**
	 * Deep copy
	 */
	public XjcOption(org.netbeans.modules.xml.jaxb.cfg.schema.XjcOption source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public XjcOption(org.netbeans.modules.xml.jaxb.cfg.schema.XjcOption source, boolean justData) {
		_Name = source._Name;
		_Value = source._Value;
	}

	// This attribute is optional
	public void setName(java.lang.String value) {
		_Name = value;
	}

	public java.lang.String getName() {
		return _Name;
	}

	// This attribute is optional
	public void setValue(java.lang.String value) {
		_Value = value;
	}

	public java.lang.String getValue() {
		return _Value;
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "xjc-option";
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
		writeNodeChildren(out, nodeName, namespace, indent, namespaceMap);
		out.write("/>\n");
	}

	protected void writeNodeAttributes(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		// name is an attribute with namespace http://xml.netbeans.org/schema/JAXBWizConfig
		if (_Name != null) {
			out.write(" name='");
			org.netbeans.modules.xml.jaxb.cfg.schema.Schemas.writeXML(out, _Name, true);
			out.write("'");	// NOI18N
		}
		// value is an attribute with namespace http://xml.netbeans.org/schema/JAXBWizConfig
		if (_Value != null) {
			out.write(" value='");
			org.netbeans.modules.xml.jaxb.cfg.schema.Schemas.writeXML(out, _Value, true);
			out.write("'");	// NOI18N
		}
	}

	protected void writeNodeChildren(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
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
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("name");
		if (attr != null) {
			attrValue = attr.getValue();
			_Name = attrValue;
		}
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("value");
		if (attr != null) {
			attrValue = attr.getValue();
			_Value = attrValue;
		}
	}

	protected void readNodeChildren(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "name")
			setName((java.lang.String)value);
		else if (name == "value")
			setValue((java.lang.String)value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for XjcOption");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "name")
			return getName();
		if (name == "value")
			return getValue();
		throw new IllegalArgumentException(name+" is not a valid property name for XjcOption");
	}

	public String nameSelf() {
		return "XjcOption";
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
		if (childObj instanceof java.lang.String) {
			java.lang.String child = (java.lang.String) childObj;
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
			if (child == _Value) {
				if (returnConstName) {
					return VALUE;
				} else if (returnSchemaName) {
					return "value";
				} else if (returnXPathName) {
					return "@value";
				} else {
					return "Value";
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
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.xml.jaxb.cfg.schema.XjcOption && equals((org.netbeans.modules.xml.jaxb.cfg.schema.XjcOption) o);
	}

	public boolean equals(org.netbeans.modules.xml.jaxb.cfg.schema.XjcOption inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_Name == null ? inst._Name == null : _Name.equals(inst._Name))) {
			return false;
		}
		if (!(_Value == null ? inst._Value == null : _Value.equals(inst._Value))) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_Name == null ? 0 : _Name.hashCode());
		result = 37*result + (_Value == null ? 0 : _Value.hashCode());
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
