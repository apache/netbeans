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
 *	This generated bean class SecurityPermissionType matches the schema element 'security-permissionType'.
 *  The root bean class is WeblogicEjbJar
 *
 *	Generated on Tue Jul 25 03:26:55 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ejb1221;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class SecurityPermissionType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String DESCRIPTION = "Description";	// NOI18N
	static public final String DESCRIPTIONJ2EEID = "DescriptionJ2eeId";	// NOI18N
	static public final String DESCRIPTIONXMLLANG = "DescriptionXmlLang";	// NOI18N
	static public final String DESCRIPTIONDESCRIPTIONJ2EEID2 = "DescriptionDescriptionJ2eeId2";	// NOI18N
	static public final String DESCRIPTIONDESCRIPTIONXMLLANG2 = "DescriptionDescriptionXmlLang2";	// NOI18N
	static public final String SECURITY_PERMISSION_SPEC = "SecurityPermissionSpec";	// NOI18N
	static public final String SECURITYPERMISSIONSPECJ2EEID = "SecurityPermissionSpecJ2eeId";	// NOI18N
	static public final String SECURITYPERMISSIONSPECPRINCIPALNAMEJ2EEID2 = "SecurityPermissionSpecPrincipalNameJ2eeId2";	// NOI18N

	public SecurityPermissionType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public SecurityPermissionType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("description", 	// NOI18N
			DESCRIPTION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(DESCRIPTION, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(DESCRIPTION, "xml:lang", "XmlLang", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(DESCRIPTION, "j2ee:id", "DescriptionJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(DESCRIPTION, "xml:lang", "DescriptionXmlLang2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("security-permission-spec", 	// NOI18N
			SECURITY_PERMISSION_SPEC, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(SECURITY_PERMISSION_SPEC, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(SECURITY_PERMISSION_SPEC, "j2ee:id", "PrincipalNameJ2eeId2", 
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

	// This attribute is optional
	public void setDescription(java.lang.String value) {
		this.setValue(DESCRIPTION, value);
	}

	//
	public java.lang.String getDescription() {
		return (java.lang.String)this.getValue(DESCRIPTION);
	}

	// This attribute is optional
	public void setDescriptionJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(DESCRIPTION) == 0) {
			setValue(DESCRIPTION, "");
		}
		setAttributeValue(DESCRIPTION, "J2eeId", value);
	}

	//
	public java.lang.String getDescriptionJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(DESCRIPTION) == 0) {
			return null;
		} else {
			return getAttributeValue(DESCRIPTION, "J2eeId");
		}
	}

	// This attribute is optional
	public void setDescriptionXmlLang(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(DESCRIPTION) == 0) {
			setValue(DESCRIPTION, "");
		}
		setAttributeValue(DESCRIPTION, "XmlLang", value);
	}

	//
	public java.lang.String getDescriptionXmlLang() {
		// If our element does not exist, then the attribute does not exist.
		if (size(DESCRIPTION) == 0) {
			return null;
		} else {
			return getAttributeValue(DESCRIPTION, "XmlLang");
		}
	}

	// This attribute is optional
	public void setDescriptionDescriptionJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(DESCRIPTION) == 0) {
			setValue(DESCRIPTION, "");
		}
		setAttributeValue(DESCRIPTION, "DescriptionJ2eeId2", value);
	}

	//
	public java.lang.String getDescriptionDescriptionJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(DESCRIPTION) == 0) {
			return null;
		} else {
			return getAttributeValue(DESCRIPTION, "DescriptionJ2eeId2");
		}
	}

	// This attribute is optional
	public void setDescriptionDescriptionXmlLang2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(DESCRIPTION) == 0) {
			setValue(DESCRIPTION, "");
		}
		setAttributeValue(DESCRIPTION, "DescriptionXmlLang2", value);
	}

	//
	public java.lang.String getDescriptionDescriptionXmlLang2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(DESCRIPTION) == 0) {
			return null;
		} else {
			return getAttributeValue(DESCRIPTION, "DescriptionXmlLang2");
		}
	}

	// This attribute is mandatory
	public void setSecurityPermissionSpec(java.lang.String value) {
		this.setValue(SECURITY_PERMISSION_SPEC, value);
	}

	//
	public java.lang.String getSecurityPermissionSpec() {
		return (java.lang.String)this.getValue(SECURITY_PERMISSION_SPEC);
	}

	// This attribute is optional
	public void setSecurityPermissionSpecJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(SECURITY_PERMISSION_SPEC) == 0) {
			setValue(SECURITY_PERMISSION_SPEC, "");
		}
		setAttributeValue(SECURITY_PERMISSION_SPEC, "J2eeId", value);
	}

	//
	public java.lang.String getSecurityPermissionSpecJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(SECURITY_PERMISSION_SPEC) == 0) {
			return null;
		} else {
			return getAttributeValue(SECURITY_PERMISSION_SPEC, "J2eeId");
		}
	}

	// This attribute is optional
	public void setSecurityPermissionSpecPrincipalNameJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(SECURITY_PERMISSION_SPEC) == 0) {
			setValue(SECURITY_PERMISSION_SPEC, "");
		}
		setAttributeValue(SECURITY_PERMISSION_SPEC, "PrincipalNameJ2eeId2", value);
	}

	//
	public java.lang.String getSecurityPermissionSpecPrincipalNameJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(SECURITY_PERMISSION_SPEC) == 0) {
			return null;
		} else {
			return getAttributeValue(SECURITY_PERMISSION_SPEC, "PrincipalNameJ2eeId2");
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
		// Validating property description
		// Validating property descriptionJ2eeId
		if (getDescriptionJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getDescriptionJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "descriptionJ2eeId", this);	// NOI18N
			}
		}
		// Validating property descriptionXmlLang
		// Validating property descriptionDescriptionJ2eeId2
		if (getDescriptionDescriptionJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getDescriptionDescriptionJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "descriptionDescriptionJ2eeId2", this);	// NOI18N
			}
		}
		// Validating property descriptionDescriptionXmlLang2
		// Validating property securityPermissionSpec
		if (getSecurityPermissionSpec() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getSecurityPermissionSpec() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "securityPermissionSpec", this);	// NOI18N
		}
		// has whitespace restriction
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getSecurityPermissionSpec() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "securityPermissionSpec", this);	// NOI18N
		}
		// Validating property securityPermissionSpecJ2eeId
		if (getSecurityPermissionSpecJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getSecurityPermissionSpecJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "securityPermissionSpecJ2eeId", this);	// NOI18N
			}
		}
		// Validating property securityPermissionSpecPrincipalNameJ2eeId2
		if (getSecurityPermissionSpecPrincipalNameJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getSecurityPermissionSpecPrincipalNameJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "securityPermissionSpecPrincipalNameJ2eeId2", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Description");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDescription();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DESCRIPTION, 0, str, indent);

		str.append(indent);
		str.append("SecurityPermissionSpec");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getSecurityPermissionSpec();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SECURITY_PERMISSION_SPEC, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("SecurityPermissionType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

