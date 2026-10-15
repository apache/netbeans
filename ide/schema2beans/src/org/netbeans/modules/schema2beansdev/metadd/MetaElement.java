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
 *	This generated bean class MetaElement
 *	matches the schema element 'meta-element'.
 *  The root bean class is MetaDD
 *
 * @Generated
 */

package org.netbeans.modules.schema2beansdev.metadd;

public class MetaElement implements org.netbeans.modules.schema2beansdev.metadd.CommonBean {
	public static final String DTD_NAME = "DtdName";	// NOI18N
	public static final String NAMESPACE = "Namespace";	// NOI18N
	public static final String BEAN_NAME = "BeanName";	// NOI18N
	public static final String BEAN_CLASS = "BeanClass";	// NOI18N
	public static final String WRAPPER_CLASS = "WrapperClass";	// NOI18N
	public static final String DEFAULT_VALUE = "DefaultValue";	// NOI18N
	public static final String KNOWN_VALUE = "KnownValue";	// NOI18N
	public static final String META_PROPERTY = "MetaProperty";	// NOI18N
	public static final String COMPARATOR_CLASS = "ComparatorClass";	// NOI18N
	public static final String IMPLEMENTS = "Implements";	// NOI18N
	public static final String EXTENDS = "Extends";	// NOI18N
	public static final String IMPORT = "Import";	// NOI18N
	public static final String USER_CODE = "UserCode";	// NOI18N
	public static final String VETOABLE = "Vetoable";	// NOI18N
	public static final String SKIP_GENERATION = "SkipGeneration";	// NOI18N
	public static final String DELEGATOR_NAME = "DelegatorName";	// NOI18N
	public static final String DELEGATOR_EXTENDS = "DelegatorExtends";	// NOI18N
	public static final String BEAN_INTERFACE_EXTENDS = "BeanInterfaceExtends";	// NOI18N
	public static final String CAN_BE_EMPTY = "CanBeEmpty";	// NOI18N

	private String _DtdName;
	private String _Namespace;
	private String _BeanName;
	private String _BeanClass;
	private String _WrapperClass;
	private java.util.List _DefaultValue = new java.util.ArrayList();	// List<String>
	private java.util.List _KnownValue = new java.util.ArrayList();	// List<String>
	private java.util.List _MetaProperty = new java.util.ArrayList();	// List<MetaProperty>
	private java.util.List _ComparatorClass = new java.util.ArrayList();	// List<String>
	private String _Implements;
	private String _Extends;
	private java.util.List _Import = new java.util.ArrayList();	// List<String>
	private String _UserCode;
	private boolean _Vetoable;
	private boolean _isSet_Vetoable = false;
	private boolean _SkipGeneration;
	private boolean _isSet_SkipGeneration = false;
	private String _DelegatorName;
	private String _DelegatorExtends;
	private String _BeanInterfaceExtends;
	private boolean _CanBeEmpty;
	private boolean _isSet_CanBeEmpty = false;
	private static final java.util.logging.Logger _logger = java.util.logging.Logger.getLogger("org.netbeans.modules.schema2beansdev.metadd.MetaElement");

	/**
	 * Normal starting point constructor.
	 */
	public MetaElement() {
		_DtdName = "";
	}

	/**
	 * Required parameters constructor
	 */
	public MetaElement(String dtdName) {
		_DtdName = dtdName;
	}

	/**
	 * Deep copy
	 */
	public MetaElement(org.netbeans.modules.schema2beansdev.metadd.MetaElement source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public MetaElement(org.netbeans.modules.schema2beansdev.metadd.MetaElement source, boolean justData) {
		_DtdName = source._DtdName;
		_Namespace = source._Namespace;
		_BeanName = source._BeanName;
		_BeanClass = source._BeanClass;
		_WrapperClass = source._WrapperClass;
		for (java.util.Iterator it = source._DefaultValue.iterator(); 
			it.hasNext(); ) {
			String srcElement = (String)it.next();
			_DefaultValue.add(srcElement);
		}
		for (java.util.Iterator it = source._KnownValue.iterator(); 
			it.hasNext(); ) {
			String srcElement = (String)it.next();
			_KnownValue.add(srcElement);
		}
		for (java.util.Iterator it = source._MetaProperty.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.schema2beansdev.metadd.MetaProperty srcElement = (org.netbeans.modules.schema2beansdev.metadd.MetaProperty)it.next();
			_MetaProperty.add((srcElement == null) ? null : newMetaProperty(srcElement, justData));
		}
		for (java.util.Iterator it = source._ComparatorClass.iterator(); 
			it.hasNext(); ) {
			String srcElement = (String)it.next();
			_ComparatorClass.add(srcElement);
		}
		_Implements = source._Implements;
		_Extends = source._Extends;
		for (java.util.Iterator it = source._Import.iterator(); 
			it.hasNext(); ) {
			String srcElement = (String)it.next();
			_Import.add(srcElement);
		}
		_UserCode = source._UserCode;
		_Vetoable = source._Vetoable;
		_isSet_Vetoable = source._isSet_Vetoable;
		_SkipGeneration = source._SkipGeneration;
		_isSet_SkipGeneration = source._isSet_SkipGeneration;
		_DelegatorName = source._DelegatorName;
		_DelegatorExtends = source._DelegatorExtends;
		_BeanInterfaceExtends = source._BeanInterfaceExtends;
		_CanBeEmpty = source._CanBeEmpty;
		_isSet_CanBeEmpty = source._isSet_CanBeEmpty;
	}

	// This attribute is mandatory
	public void setDtdName(String value) {
		_DtdName = value;
	}

	public String getDtdName() {
		return _DtdName;
	}

	// This attribute is optional
	public void setNamespace(String value) {
		_Namespace = value;
	}

	public String getNamespace() {
		return _Namespace;
	}

	// This attribute is optional
	public void setBeanName(String value) {
		_BeanName = value;
	}

	public String getBeanName() {
		return _BeanName;
	}

	// This attribute is optional
	public void setBeanClass(String value) {
		_BeanClass = value;
	}

	public String getBeanClass() {
		return _BeanClass;
	}

	// This attribute is optional
	public void setWrapperClass(String value) {
		_WrapperClass = value;
	}

	public String getWrapperClass() {
		return _WrapperClass;
	}

	// This attribute is an array, possibly empty
	public void setDefaultValue(String[] value) {
		if (value == null)
			value = new String[0];
		_DefaultValue.clear();
		((java.util.ArrayList) _DefaultValue).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_DefaultValue.add(value[i]);
		}
	}

	public void setDefaultValue(int index, String value) {
		_DefaultValue.set(index, value);
	}

	public String[] getDefaultValue() {
		String[] arr = new String[_DefaultValue.size()];
		return (String[]) _DefaultValue.toArray(arr);
	}

	public java.util.List fetchDefaultValueList() {
		return _DefaultValue;
	}

	public String getDefaultValue(int index) {
		return (String)_DefaultValue.get(index);
	}

	// Return the number of defaultValue
	public int sizeDefaultValue() {
		return _DefaultValue.size();
	}

	public int addDefaultValue(String value) {
		_DefaultValue.add(value);
		int positionOfNewItem = _DefaultValue.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeDefaultValue(String value) {
		int pos = _DefaultValue.indexOf(value);
		if (pos >= 0) {
			_DefaultValue.remove(pos);
		}
		return pos;
	}

	// This attribute is an array, possibly empty
	public void setKnownValue(String[] value) {
		if (value == null)
			value = new String[0];
		_KnownValue.clear();
		((java.util.ArrayList) _KnownValue).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_KnownValue.add(value[i]);
		}
	}

	public void setKnownValue(int index, String value) {
		_KnownValue.set(index, value);
	}

	public String[] getKnownValue() {
		String[] arr = new String[_KnownValue.size()];
		return (String[]) _KnownValue.toArray(arr);
	}

	public java.util.List fetchKnownValueList() {
		return _KnownValue;
	}

	public String getKnownValue(int index) {
		return (String)_KnownValue.get(index);
	}

	// Return the number of knownValue
	public int sizeKnownValue() {
		return _KnownValue.size();
	}

	public int addKnownValue(String value) {
		_KnownValue.add(value);
		int positionOfNewItem = _KnownValue.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeKnownValue(String value) {
		int pos = _KnownValue.indexOf(value);
		if (pos >= 0) {
			_KnownValue.remove(pos);
		}
		return pos;
	}

	// This attribute is an array, possibly empty
	public void setMetaProperty(org.netbeans.modules.schema2beansdev.metadd.MetaProperty[] value) {
		if (value == null)
			value = new MetaProperty[0];
		_MetaProperty.clear();
		((java.util.ArrayList) _MetaProperty).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_MetaProperty.add(value[i]);
		}
	}

	public void setMetaProperty(int index, org.netbeans.modules.schema2beansdev.metadd.MetaProperty value) {
		_MetaProperty.set(index, value);
	}

	public org.netbeans.modules.schema2beansdev.metadd.MetaProperty[] getMetaProperty() {
		MetaProperty[] arr = new MetaProperty[_MetaProperty.size()];
		return (MetaProperty[]) _MetaProperty.toArray(arr);
	}

	public java.util.List fetchMetaPropertyList() {
		return _MetaProperty;
	}

	public org.netbeans.modules.schema2beansdev.metadd.MetaProperty getMetaProperty(int index) {
		return (MetaProperty)_MetaProperty.get(index);
	}

	// Return the number of metaProperty
	public int sizeMetaProperty() {
		return _MetaProperty.size();
	}

	public int addMetaProperty(org.netbeans.modules.schema2beansdev.metadd.MetaProperty value) {
		_MetaProperty.add(value);
		int positionOfNewItem = _MetaProperty.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeMetaProperty(org.netbeans.modules.schema2beansdev.metadd.MetaProperty value) {
		int pos = _MetaProperty.indexOf(value);
		if (pos >= 0) {
			_MetaProperty.remove(pos);
		}
		return pos;
	}

	// This attribute is an array, possibly empty
	public void setComparatorClass(String[] value) {
		if (value == null)
			value = new String[0];
		_ComparatorClass.clear();
		((java.util.ArrayList) _ComparatorClass).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_ComparatorClass.add(value[i]);
		}
	}

	public void setComparatorClass(int index, String value) {
		_ComparatorClass.set(index, value);
	}

	public String[] getComparatorClass() {
		String[] arr = new String[_ComparatorClass.size()];
		return (String[]) _ComparatorClass.toArray(arr);
	}

	public java.util.List fetchComparatorClassList() {
		return _ComparatorClass;
	}

	public String getComparatorClass(int index) {
		return (String)_ComparatorClass.get(index);
	}

	// Return the number of comparatorClass
	public int sizeComparatorClass() {
		return _ComparatorClass.size();
	}

	public int addComparatorClass(String value) {
		_ComparatorClass.add(value);
		int positionOfNewItem = _ComparatorClass.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeComparatorClass(String value) {
		int pos = _ComparatorClass.indexOf(value);
		if (pos >= 0) {
			_ComparatorClass.remove(pos);
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
	public void setUserCode(String value) {
		_UserCode = value;
	}

	public String getUserCode() {
		return _UserCode;
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
	public void setSkipGeneration(boolean value) {
		_SkipGeneration = value;
		_isSet_SkipGeneration = true;
	}

	public boolean isSkipGeneration() {
		return _SkipGeneration;
	}

	// This attribute is optional
	public void setDelegatorName(String value) {
		_DelegatorName = value;
	}

	public String getDelegatorName() {
		return _DelegatorName;
	}

	// This attribute is optional
	public void setDelegatorExtends(String value) {
		_DelegatorExtends = value;
	}

	public String getDelegatorExtends() {
		return _DelegatorExtends;
	}

	// This attribute is optional
	public void setBeanInterfaceExtends(String value) {
		_BeanInterfaceExtends = value;
	}

	public String getBeanInterfaceExtends() {
		return _BeanInterfaceExtends;
	}

	// This attribute is optional
	public void setCanBeEmpty(boolean value) {
		_CanBeEmpty = value;
		_isSet_CanBeEmpty = true;
	}

	public boolean isCanBeEmpty() {
		return _CanBeEmpty;
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.schema2beansdev.metadd.MetaProperty newMetaProperty() {
		return new org.netbeans.modules.schema2beansdev.metadd.MetaProperty();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.schema2beansdev.metadd.MetaProperty newMetaProperty(MetaProperty source, boolean justData) {
		return new org.netbeans.modules.schema2beansdev.metadd.MetaProperty(source, justData);
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "meta-element";
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
		if (_DtdName != null) {
			out.write(nextIndent);
			out.write("<dtd-name");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, _DtdName, false);
			out.write("</dtd-name>\n");	// NOI18N
		}
		if (_Namespace != null) {
			out.write(nextIndent);
			out.write("<namespace");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, _Namespace, false);
			out.write("</namespace>\n");	// NOI18N
		}
		if (_BeanName != null) {
			out.write(nextIndent);
			out.write("<bean-name");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, _BeanName, false);
			out.write("</bean-name>\n");	// NOI18N
		}
		if (_BeanClass != null) {
			out.write(nextIndent);
			out.write("<bean-class");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, _BeanClass, false);
			out.write("</bean-class>\n");	// NOI18N
		}
		if (_WrapperClass != null) {
			out.write(nextIndent);
			out.write("<wrapper-class");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, _WrapperClass, false);
			out.write("</wrapper-class>\n");	// NOI18N
		}
		for (java.util.Iterator it = _DefaultValue.iterator(); 
			it.hasNext(); ) {
			String element = (String)it.next();
			if (element != null) {
				out.write(nextIndent);
				out.write("<default-value");	// NOI18N
				out.write(">");	// NOI18N
				org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, element, false);
				out.write("</default-value>\n");	// NOI18N
			}
		}
		for (java.util.Iterator it = _KnownValue.iterator(); it.hasNext(); 
			) {
			String element = (String)it.next();
			if (element != null) {
				out.write(nextIndent);
				out.write("<known-value");	// NOI18N
				out.write(">");	// NOI18N
				org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, element, false);
				out.write("</known-value>\n");	// NOI18N
			}
		}
		for (java.util.Iterator it = _MetaProperty.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.schema2beansdev.metadd.MetaProperty element = (org.netbeans.modules.schema2beansdev.metadd.MetaProperty)it.next();
			if (element != null) {
				element.writeNode(out, "meta-property", null, nextIndent, namespaceMap);
			}
		}
		for (java.util.Iterator it = _ComparatorClass.iterator(); 
			it.hasNext(); ) {
			String element = (String)it.next();
			if (element != null) {
				out.write(nextIndent);
				out.write("<comparator-class");	// NOI18N
				out.write(">");	// NOI18N
				org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, element, false);
				out.write("</comparator-class>\n");	// NOI18N
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
		if (_UserCode != null) {
			out.write(nextIndent);
			out.write("<user-code");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, _UserCode, false);
			out.write("</user-code>\n");	// NOI18N
		}
		if (_isSet_Vetoable) {
			if (_Vetoable) {
				out.write(nextIndent);
				out.write("<vetoable");	// NOI18N
				out.write("/>\n");	// NOI18N
			}
		}
		if (_isSet_SkipGeneration) {
			if (_SkipGeneration) {
				out.write(nextIndent);
				out.write("<skip-generation");	// NOI18N
				out.write("/>\n");	// NOI18N
			}
		}
		if (_DelegatorName != null) {
			out.write(nextIndent);
			out.write("<delegator-name");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, _DelegatorName, false);
			out.write("</delegator-name>\n");	// NOI18N
		}
		if (_DelegatorExtends != null) {
			out.write(nextIndent);
			out.write("<delegator-extends");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, _DelegatorExtends, false);
			out.write("</delegator-extends>\n");	// NOI18N
		}
		if (_BeanInterfaceExtends != null) {
			out.write(nextIndent);
			out.write("<bean-interface-extends");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, _BeanInterfaceExtends, false);
			out.write("</bean-interface-extends>\n");	// NOI18N
		}
		if (_isSet_CanBeEmpty) {
			if (_CanBeEmpty) {
				out.write(nextIndent);
				out.write("<can-be-empty");	// NOI18N
				out.write("/>\n");	// NOI18N
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
		if ("dtd-name".equals(childNodeName)) {
			_DtdName = childNodeValue;
		}
		else if ("namespace".equals(childNodeName)) {
			_Namespace = childNodeValue;
		}
		else if ("bean-name".equals(childNodeName)) {
			_BeanName = childNodeValue;
		}
		else if ("bean-class".equals(childNodeName)) {
			_BeanClass = childNodeValue;
		}
		else if ("wrapper-class".equals(childNodeName)) {
			_WrapperClass = childNodeValue;
		}
		else if ("default-value".equals(childNodeName)) {
			String aDefaultValue;
			aDefaultValue = childNodeValue;
			_DefaultValue.add(aDefaultValue);
		}
		else if ("known-value".equals(childNodeName)) {
			String aKnownValue;
			aKnownValue = childNodeValue;
			_KnownValue.add(aKnownValue);
		}
		else if ("meta-property".equals(childNodeName)) {
			MetaProperty aMetaProperty = newMetaProperty();
			aMetaProperty.readNode(childNode, namespacePrefixes);
			_MetaProperty.add(aMetaProperty);
		}
		else if ("comparator-class".equals(childNodeName)) {
			String aComparatorClass;
			aComparatorClass = childNodeValue;
			_ComparatorClass.add(aComparatorClass);
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
		else if ("user-code".equals(childNodeName)) {
			_UserCode = childNodeValue;
		}
		else if ("vetoable".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_Vetoable = true;
			else
				_Vetoable = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_Vetoable = true;
		}
		else if ("skip-generation".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_SkipGeneration = true;
			else
				_SkipGeneration = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_SkipGeneration = true;
		}
		else if ("delegator-name".equals(childNodeName)) {
			_DelegatorName = childNodeValue;
		}
		else if ("delegator-extends".equals(childNodeName)) {
			_DelegatorExtends = childNodeValue;
		}
		else if ("bean-interface-extends".equals(childNodeName)) {
			_BeanInterfaceExtends = childNodeValue;
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

	public void validate() throws org.netbeans.modules.schema2beansdev.metadd.MetaDD.ValidateException {
		boolean restrictionFailure = false;
		boolean restrictionPassed = false;
		// Validating property dtdName
		if (getDtdName() == null) {
			throw new org.netbeans.modules.schema2beansdev.metadd.MetaDD.ValidateException("getDtdName() == null", org.netbeans.modules.schema2beansdev.metadd.MetaDD.ValidateException.FailureType.NULL_VALUE, "dtdName", this);	// NOI18N
		}
		// Validating property namespace
		// Validating property beanName
		// Validating property beanClass
		// Validating property wrapperClass
		// Validating property defaultValue
		// Validating property knownValue
		// Validating property metaProperty
		for (int _index = 0; _index < sizeMetaProperty(); ++_index) {
			org.netbeans.modules.schema2beansdev.metadd.MetaProperty element = getMetaProperty(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property comparatorClass
		// Validating property implements
		// Validating property extends
		// Validating property import
		// Validating property userCode
		// Validating property vetoable
		// Validating property skipGeneration
		// Validating property delegatorName
		// Validating property delegatorExtends
		// Validating property beanInterfaceExtends
		// Validating property canBeEmpty
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if ("dtdName".equals(name))
			setDtdName((String)value);
		else if ("namespace".equals(name))
			setNamespace((String)value);
		else if ("beanName".equals(name))
			setBeanName((String)value);
		else if ("beanClass".equals(name))
			setBeanClass((String)value);
		else if ("wrapperClass".equals(name))
			setWrapperClass((String)value);
		else if ("defaultValue".equals(name))
			addDefaultValue((String)value);
		else if ("defaultValue[]".equals(name))
			setDefaultValue((String[]) value);
		else if ("knownValue".equals(name))
			addKnownValue((String)value);
		else if ("knownValue[]".equals(name))
			setKnownValue((String[]) value);
		else if ("metaProperty".equals(name))
			addMetaProperty((MetaProperty)value);
		else if ("metaProperty[]".equals(name))
			setMetaProperty((MetaProperty[]) value);
		else if ("comparatorClass".equals(name))
			addComparatorClass((String)value);
		else if ("comparatorClass[]".equals(name))
			setComparatorClass((String[]) value);
		else if ("implements".equals(name))
			setImplements((String)value);
		else if ("extends".equals(name))
			setExtends((String)value);
		else if ("import".equals(name))
			addImport((String)value);
		else if ("import[]".equals(name))
			setImport((String[]) value);
		else if ("userCode".equals(name))
			setUserCode((String)value);
		else if ("vetoable".equals(name))
			setVetoable(((java.lang.Boolean)value).booleanValue());
		else if ("skipGeneration".equals(name))
			setSkipGeneration(((java.lang.Boolean)value).booleanValue());
		else if ("delegatorName".equals(name))
			setDelegatorName((String)value);
		else if ("delegatorExtends".equals(name))
			setDelegatorExtends((String)value);
		else if ("beanInterfaceExtends".equals(name))
			setBeanInterfaceExtends((String)value);
		else if ("canBeEmpty".equals(name))
			setCanBeEmpty(((java.lang.Boolean)value).booleanValue());
		else
			throw new IllegalArgumentException(name+" is not a valid property name for MetaElement");
	}

	public Object fetchPropertyByName(String name) {
		if ("dtdName".equals(name))
			return getDtdName();
		if ("namespace".equals(name))
			return getNamespace();
		if ("beanName".equals(name))
			return getBeanName();
		if ("beanClass".equals(name))
			return getBeanClass();
		if ("wrapperClass".equals(name))
			return getWrapperClass();
		if ("defaultValue[]".equals(name))
			return getDefaultValue();
		if ("knownValue[]".equals(name))
			return getKnownValue();
		if ("metaProperty[]".equals(name))
			return getMetaProperty();
		if ("comparatorClass[]".equals(name))
			return getComparatorClass();
		if ("implements".equals(name))
			return getImplements();
		if ("extends".equals(name))
			return getExtends();
		if ("import[]".equals(name))
			return getImport();
		if ("userCode".equals(name))
			return getUserCode();
		if ("vetoable".equals(name))
			return (isVetoable() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("skipGeneration".equals(name))
			return (isSkipGeneration() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("delegatorName".equals(name))
			return getDelegatorName();
		if ("delegatorExtends".equals(name))
			return getDelegatorExtends();
		if ("beanInterfaceExtends".equals(name))
			return getBeanInterfaceExtends();
		if ("canBeEmpty".equals(name))
			return (isCanBeEmpty() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		throw new IllegalArgumentException(name+" is not a valid property name for MetaElement");
	}

	public String nameSelf() {
		return "MetaElement";
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
			if (((java.lang.Boolean)child).booleanValue() == _SkipGeneration) {
				if (returnConstName) {
					return SKIP_GENERATION;
				} else if (returnSchemaName) {
					return "skip-generation";
				} else if (returnXPathName) {
					return "skip-generation";
				} else {
					return "SkipGeneration";
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
		if (childObj instanceof MetaProperty) {
			MetaProperty child = (MetaProperty) childObj;
			int index = 0;
			for (java.util.Iterator it = _MetaProperty.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.schema2beansdev.metadd.MetaProperty element = (org.netbeans.modules.schema2beansdev.metadd.MetaProperty)it.next();
				if (child == element) {
					if (returnConstName) {
						return META_PROPERTY;
					} else if (returnSchemaName) {
						return "meta-property";
					} else if (returnXPathName) {
						return "meta-property[position()="+index+"]";
					} else {
						return "MetaProperty."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		if (childObj instanceof java.lang.String) {
			java.lang.String child = (java.lang.String) childObj;
			if (child.equals(_DtdName)) {
				if (returnConstName) {
					return DTD_NAME;
				} else if (returnSchemaName) {
					return "dtd-name";
				} else if (returnXPathName) {
					return "dtd-name";
				} else {
					return "DtdName";
				}
			}
			if (child.equals(_Namespace)) {
				if (returnConstName) {
					return NAMESPACE;
				} else if (returnSchemaName) {
					return "namespace";
				} else if (returnXPathName) {
					return "namespace";
				} else {
					return "Namespace";
				}
			}
			if (child.equals(_BeanName)) {
				if (returnConstName) {
					return BEAN_NAME;
				} else if (returnSchemaName) {
					return "bean-name";
				} else if (returnXPathName) {
					return "bean-name";
				} else {
					return "BeanName";
				}
			}
			if (child.equals(_BeanClass)) {
				if (returnConstName) {
					return BEAN_CLASS;
				} else if (returnSchemaName) {
					return "bean-class";
				} else if (returnXPathName) {
					return "bean-class";
				} else {
					return "BeanClass";
				}
			}
			if (child.equals(_WrapperClass)) {
				if (returnConstName) {
					return WRAPPER_CLASS;
				} else if (returnSchemaName) {
					return "wrapper-class";
				} else if (returnXPathName) {
					return "wrapper-class";
				} else {
					return "WrapperClass";
				}
			}
			int index = 0;
			for (java.util.Iterator it = _DefaultValue.iterator(); 
				it.hasNext(); ) {
				String element = (String)it.next();
				if (child.equals(element)) {
					if (returnConstName) {
						return DEFAULT_VALUE;
					} else if (returnSchemaName) {
						return "default-value";
					} else if (returnXPathName) {
						return "default-value[position()="+index+"]";
					} else {
						return "DefaultValue."+Integer.toHexString(index);
					}
				}
				++index;
			}
			index = 0;
			for (java.util.Iterator it = _KnownValue.iterator(); 
				it.hasNext(); ) {
				String element = (String)it.next();
				if (child.equals(element)) {
					if (returnConstName) {
						return KNOWN_VALUE;
					} else if (returnSchemaName) {
						return "known-value";
					} else if (returnXPathName) {
						return "known-value[position()="+index+"]";
					} else {
						return "KnownValue."+Integer.toHexString(index);
					}
				}
				++index;
			}
			index = 0;
			for (java.util.Iterator it = _ComparatorClass.iterator(); 
				it.hasNext(); ) {
				String element = (String)it.next();
				if (child.equals(element)) {
					if (returnConstName) {
						return COMPARATOR_CLASS;
					} else if (returnSchemaName) {
						return "comparator-class";
					} else if (returnXPathName) {
						return "comparator-class[position()="+index+"]";
					} else {
						return "ComparatorClass."+Integer.toHexString(index);
					}
				}
				++index;
			}
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
			index = 0;
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
			if (child.equals(_UserCode)) {
				if (returnConstName) {
					return USER_CODE;
				} else if (returnSchemaName) {
					return "user-code";
				} else if (returnXPathName) {
					return "user-code";
				} else {
					return "UserCode";
				}
			}
			if (child.equals(_DelegatorName)) {
				if (returnConstName) {
					return DELEGATOR_NAME;
				} else if (returnSchemaName) {
					return "delegator-name";
				} else if (returnXPathName) {
					return "delegator-name";
				} else {
					return "DelegatorName";
				}
			}
			if (child.equals(_DelegatorExtends)) {
				if (returnConstName) {
					return DELEGATOR_EXTENDS;
				} else if (returnSchemaName) {
					return "delegator-extends";
				} else if (returnXPathName) {
					return "delegator-extends";
				} else {
					return "DelegatorExtends";
				}
			}
			if (child.equals(_BeanInterfaceExtends)) {
				if (returnConstName) {
					return BEAN_INTERFACE_EXTENDS;
				} else if (returnSchemaName) {
					return "bean-interface-extends";
				} else if (returnXPathName) {
					return "bean-interface-extends";
				} else {
					return "BeanInterfaceExtends";
				}
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
		for (java.util.Iterator it = _MetaProperty.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.schema2beansdev.metadd.MetaProperty element = (org.netbeans.modules.schema2beansdev.metadd.MetaProperty)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.schema2beansdev.metadd.MetaElement && equals((org.netbeans.modules.schema2beansdev.metadd.MetaElement) o);
	}

	public boolean equals(org.netbeans.modules.schema2beansdev.metadd.MetaElement inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_DtdName == null ? inst._DtdName == null : _DtdName.equals(inst._DtdName))) {
			return false;
		}
		if (!(_Namespace == null ? inst._Namespace == null : _Namespace.equals(inst._Namespace))) {
			return false;
		}
		if (!(_BeanName == null ? inst._BeanName == null : _BeanName.equals(inst._BeanName))) {
			return false;
		}
		if (!(_BeanClass == null ? inst._BeanClass == null : _BeanClass.equals(inst._BeanClass))) {
			return false;
		}
		if (!(_WrapperClass == null ? inst._WrapperClass == null : _WrapperClass.equals(inst._WrapperClass))) {
			return false;
		}
		if (sizeDefaultValue() != inst.sizeDefaultValue())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _DefaultValue.iterator(), it2 = inst._DefaultValue.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			String element = (String)it.next();
			String element2 = (String)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (sizeKnownValue() != inst.sizeKnownValue())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _KnownValue.iterator(), it2 = inst._KnownValue.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			String element = (String)it.next();
			String element2 = (String)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (sizeMetaProperty() != inst.sizeMetaProperty())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _MetaProperty.iterator(), it2 = inst._MetaProperty.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.schema2beansdev.metadd.MetaProperty element = (org.netbeans.modules.schema2beansdev.metadd.MetaProperty)it.next();
			org.netbeans.modules.schema2beansdev.metadd.MetaProperty element2 = (org.netbeans.modules.schema2beansdev.metadd.MetaProperty)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (sizeComparatorClass() != inst.sizeComparatorClass())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _ComparatorClass.iterator(), it2 = inst._ComparatorClass.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			String element = (String)it.next();
			String element2 = (String)it2.next();
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
		if (!(_UserCode == null ? inst._UserCode == null : _UserCode.equals(inst._UserCode))) {
			return false;
		}
		if (_isSet_Vetoable != inst._isSet_Vetoable) {
			return false;
		}
		if (_isSet_Vetoable) {
			if (!(_Vetoable == inst._Vetoable)) {
				return false;
			}
		}
		if (_isSet_SkipGeneration != inst._isSet_SkipGeneration) {
			return false;
		}
		if (_isSet_SkipGeneration) {
			if (!(_SkipGeneration == inst._SkipGeneration)) {
				return false;
			}
		}
		if (!(_DelegatorName == null ? inst._DelegatorName == null : _DelegatorName.equals(inst._DelegatorName))) {
			return false;
		}
		if (!(_DelegatorExtends == null ? inst._DelegatorExtends == null : _DelegatorExtends.equals(inst._DelegatorExtends))) {
			return false;
		}
		if (!(_BeanInterfaceExtends == null ? inst._BeanInterfaceExtends == null : _BeanInterfaceExtends.equals(inst._BeanInterfaceExtends))) {
			return false;
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
		result = 37*result + (_DtdName == null ? 0 : _DtdName.hashCode());
		result = 37*result + (_Namespace == null ? 0 : _Namespace.hashCode());
		result = 37*result + (_BeanName == null ? 0 : _BeanName.hashCode());
		result = 37*result + (_BeanClass == null ? 0 : _BeanClass.hashCode());
		result = 37*result + (_WrapperClass == null ? 0 : _WrapperClass.hashCode());
		result = 37*result + (_DefaultValue == null ? 0 : _DefaultValue.hashCode());
		result = 37*result + (_KnownValue == null ? 0 : _KnownValue.hashCode());
		result = 37*result + (_MetaProperty == null ? 0 : _MetaProperty.hashCode());
		result = 37*result + (_ComparatorClass == null ? 0 : _ComparatorClass.hashCode());
		result = 37*result + (_Implements == null ? 0 : _Implements.hashCode());
		result = 37*result + (_Extends == null ? 0 : _Extends.hashCode());
		result = 37*result + (_Import == null ? 0 : _Import.hashCode());
		result = 37*result + (_UserCode == null ? 0 : _UserCode.hashCode());
		result = 37*result + (_isSet_Vetoable ? 0 : (_Vetoable ? 0 : 1));
		result = 37*result + (_isSet_SkipGeneration ? 0 : (_SkipGeneration ? 0 : 1));
		result = 37*result + (_DelegatorName == null ? 0 : _DelegatorName.hashCode());
		result = 37*result + (_DelegatorExtends == null ? 0 : _DelegatorExtends.hashCode());
		result = 37*result + (_BeanInterfaceExtends == null ? 0 : _BeanInterfaceExtends.hashCode());
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
