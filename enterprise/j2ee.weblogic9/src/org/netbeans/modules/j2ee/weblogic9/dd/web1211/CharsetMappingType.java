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
 *	This generated bean class CharsetMappingType matches the schema element 'charset-mappingType'.
 *  The root bean class is WeblogicWebApp
 *
 *	Generated on Tue Jul 25 03:27:04 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.web1211;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class CharsetMappingType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String IANA_CHARSET_NAME = "IanaCharsetName";	// NOI18N
	static public final String IANACHARSETNAMEID = "IanaCharsetNameId";	// NOI18N
	static public final String JAVA_CHARSET_NAME = "JavaCharsetName";	// NOI18N
	static public final String JAVACHARSETNAMEID = "JavaCharsetNameId";	// NOI18N

	public CharsetMappingType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public CharsetMappingType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("iana-charset-name", 	// NOI18N
			IANA_CHARSET_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(IANA_CHARSET_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("java-charset-name", 	// NOI18N
			JAVA_CHARSET_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(JAVA_CHARSET_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setId(java.lang.String value) {
		setAttributeValue(ID, value);
	}

	//
	public java.lang.String getId() {
		return getAttributeValue(ID);
	}

	// This attribute is mandatory
	public void setIanaCharsetName(java.lang.String value) {
		this.setValue(IANA_CHARSET_NAME, value);
	}

	//
	public java.lang.String getIanaCharsetName() {
		return (java.lang.String)this.getValue(IANA_CHARSET_NAME);
	}

	// This attribute is optional
	public void setIanaCharsetNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(IANA_CHARSET_NAME) == 0) {
			setValue(IANA_CHARSET_NAME, "");
		}
		setAttributeValue(IANA_CHARSET_NAME, "Id", value);
	}

	//
	public java.lang.String getIanaCharsetNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(IANA_CHARSET_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(IANA_CHARSET_NAME, "Id");
		}
	}

	// This attribute is mandatory
	public void setJavaCharsetName(java.lang.String value) {
		this.setValue(JAVA_CHARSET_NAME, value);
	}

	//
	public java.lang.String getJavaCharsetName() {
		return (java.lang.String)this.getValue(JAVA_CHARSET_NAME);
	}

	// This attribute is optional
	public void setJavaCharsetNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(JAVA_CHARSET_NAME) == 0) {
			setValue(JAVA_CHARSET_NAME, "");
		}
		setAttributeValue(JAVA_CHARSET_NAME, "Id", value);
	}

	//
	public java.lang.String getJavaCharsetNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(JAVA_CHARSET_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(JAVA_CHARSET_NAME, "Id");
		}
	}

	//
	public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.add(c);
	}

	//
	public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.remove(c);
	}
	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
		boolean restrictionFailure = false;
		boolean restrictionPassed = false;
		// Validating property id
		if (getId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "id", this);	// NOI18N
			}
		}
		// Validating property ianaCharsetName
		if (getIanaCharsetName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getIanaCharsetName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "ianaCharsetName", this);	// NOI18N
		}
		// Validating property ianaCharsetNameId
		if (getIanaCharsetNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getIanaCharsetNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "ianaCharsetNameId", this);	// NOI18N
			}
		}
		// Validating property javaCharsetName
		if (getJavaCharsetName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getJavaCharsetName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "javaCharsetName", this);	// NOI18N
		}
		// Validating property javaCharsetNameId
		if (getJavaCharsetNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getJavaCharsetNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "javaCharsetNameId", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("IanaCharsetName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getIanaCharsetName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(IANA_CHARSET_NAME, 0, str, indent);

		str.append(indent);
		str.append("JavaCharsetName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getJavaCharsetName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(JAVA_CHARSET_NAME, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("CharsetMappingType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

