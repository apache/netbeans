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
 *	This generated bean class Bindings
 *	matches the schema element 'bindings'.
 *  The root bean class is Schemas
 *
 *	Generated on Thu Apr 05 11:55:09 PDT 2007
 * @Generated
 */

package org.netbeans.modules.xml.jaxb.cfg.schema;

public class Bindings {
	public static final String BINDING = "Binding";	// NOI18N

	private java.util.List _Binding = new java.util.ArrayList();	// List<Binding>

	/**
	 * Normal starting point constructor.
	 */
	public Bindings() {
	}

	/**
	 * Deep copy
	 */
	public Bindings(org.netbeans.modules.xml.jaxb.cfg.schema.Bindings source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public Bindings(org.netbeans.modules.xml.jaxb.cfg.schema.Bindings source, boolean justData) {
		for (java.util.Iterator it = source._Binding.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.xml.jaxb.cfg.schema.Binding srcElement = (org.netbeans.modules.xml.jaxb.cfg.schema.Binding)it.next();
			_Binding.add((srcElement == null) ? null : newBinding(srcElement, justData));
		}
	}

	// This attribute is an array, possibly empty
	public void setBinding(org.netbeans.modules.xml.jaxb.cfg.schema.Binding[] value) {
		if (value == null)
			value = new Binding[0];
		_Binding.clear();
		((java.util.ArrayList) _Binding).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_Binding.add(value[i]);
		}
	}

	public void setBinding(int index,org.netbeans.modules.xml.jaxb.cfg.schema.Binding value) {
		_Binding.set(index, value);
	}

	public org.netbeans.modules.xml.jaxb.cfg.schema.Binding[] getBinding() {
		Binding[] arr = new Binding[_Binding.size()];
		return (Binding[]) _Binding.toArray(arr);
	}

	public java.util.List fetchBindingList() {
		return _Binding;
	}

	public org.netbeans.modules.xml.jaxb.cfg.schema.Binding getBinding(int index) {
		return (Binding)_Binding.get(index);
	}

	// Return the number of binding
	public int sizeBinding() {
		return _Binding.size();
	}

	public int addBinding(org.netbeans.modules.xml.jaxb.cfg.schema.Binding value) {
		_Binding.add(value);
		int positionOfNewItem = _Binding.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeBinding(org.netbeans.modules.xml.jaxb.cfg.schema.Binding value) {
		int pos = _Binding.indexOf(value);
		if (pos >= 0) {
			_Binding.remove(pos);
		}
		return pos;
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.jaxb.cfg.schema.Binding newBinding() {
		return new org.netbeans.modules.xml.jaxb.cfg.schema.Binding();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.jaxb.cfg.schema.Binding newBinding(Binding source, boolean justData) {
		return new org.netbeans.modules.xml.jaxb.cfg.schema.Binding(source, justData);
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "bindings";
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
		for (java.util.Iterator it = _Binding.iterator(); it.hasNext(); ) {
			org.netbeans.modules.xml.jaxb.cfg.schema.Binding element = (org.netbeans.modules.xml.jaxb.cfg.schema.Binding)it.next();
			if (element != null) {
				element.writeNode(out, "binding", null, nextIndent, namespaceMap);
			}
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
			String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern());
			String childNodeValue = "";
			if (childNode.getFirstChild() != null) {
				childNodeValue = childNode.getFirstChild().getNodeValue();
			}
			if (childNodeName == "binding") {
				Binding aBinding = newBinding();
				aBinding.readNode(childNode, namespacePrefixes);
				_Binding.add(aBinding);
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "binding")
			addBinding((Binding)value);
		else if (name == "binding[]")
			setBinding((Binding[]) value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for Bindings");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "binding[]")
			return getBinding();
		throw new IllegalArgumentException(name+" is not a valid property name for Bindings");
	}

	public String nameSelf() {
		return "Bindings";
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
		if (childObj instanceof Binding) {
			Binding child = (Binding) childObj;
			int index = 0;
			for (java.util.Iterator it = _Binding.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.xml.jaxb.cfg.schema.Binding element = (org.netbeans.modules.xml.jaxb.cfg.schema.Binding)it.next();
				if (child == element) {
					if (returnConstName) {
						return BINDING;
					} else if (returnSchemaName) {
						return "binding";
					} else if (returnXPathName) {
						return "binding[position()="+index+"]";
					} else {
						return "Binding."+Integer.toHexString(index);
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
		for (java.util.Iterator it = _Binding.iterator(); it.hasNext(); ) {
			org.netbeans.modules.xml.jaxb.cfg.schema.Binding element = (org.netbeans.modules.xml.jaxb.cfg.schema.Binding)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.xml.jaxb.cfg.schema.Bindings && equals((org.netbeans.modules.xml.jaxb.cfg.schema.Bindings) o);
	}

	public boolean equals(org.netbeans.modules.xml.jaxb.cfg.schema.Bindings inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (sizeBinding() != inst.sizeBinding())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _Binding.iterator(), it2 = inst._Binding.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.xml.jaxb.cfg.schema.Binding element = (org.netbeans.modules.xml.jaxb.cfg.schema.Binding)it.next();
			org.netbeans.modules.xml.jaxb.cfg.schema.Binding element2 = (org.netbeans.modules.xml.jaxb.cfg.schema.Binding)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_Binding == null ? 0 : _Binding.hashCode());
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
