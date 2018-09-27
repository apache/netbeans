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
 *	This generated bean class ContextCaseType matches the schema element 'context-caseType'.
 *  The root bean class is WeblogicWebApp
 *
 *	Generated on Tue Jul 25 03:27:01 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.web1030;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ContextCaseType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String USER_NAME = "UserName";	// NOI18N
	static public final String USERNAMEJ2EEID = "UserNameJ2eeId";	// NOI18N
	static public final String GROUP_NAME = "GroupName";	// NOI18N
	static public final String GROUPNAMEID = "GroupNameId";	// NOI18N
	static public final String REQUEST_CLASS_NAME = "RequestClassName";	// NOI18N
	static public final String REQUESTCLASSNAMEJ2EEID = "RequestClassNameJ2eeId";	// NOI18N

	public ContextCaseType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ContextCaseType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("user-name", 	// NOI18N
			USER_NAME, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(USER_NAME, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("group-name", 	// NOI18N
			GROUP_NAME, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(GROUP_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("request-class-name", 	// NOI18N
			REQUEST_CLASS_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(REQUEST_CLASS_NAME, "j2ee:id", "J2eeId", 
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
	public void setUserName(java.lang.String value) {
		this.setValue(USER_NAME, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setGroupName(null);
		}
	}

	//
	public java.lang.String getUserName() {
		return (java.lang.String)this.getValue(USER_NAME);
	}

	// This attribute is optional
	public void setUserNameJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(USER_NAME) == 0) {
			setValue(USER_NAME, "");
		}
		setAttributeValue(USER_NAME, "J2eeId", value);
	}

	//
	public java.lang.String getUserNameJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(USER_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(USER_NAME, "J2eeId");
		}
	}

	// This attribute is mandatory
	public void setGroupName(java.lang.String value) {
		this.setValue(GROUP_NAME, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setUserName(null);
		}
	}

	//
	public java.lang.String getGroupName() {
		return (java.lang.String)this.getValue(GROUP_NAME);
	}

	// This attribute is optional
	public void setGroupNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(GROUP_NAME) == 0) {
			setValue(GROUP_NAME, "");
		}
		setAttributeValue(GROUP_NAME, "Id", value);
	}

	//
	public java.lang.String getGroupNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(GROUP_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(GROUP_NAME, "Id");
		}
	}

	// This attribute is mandatory
	public void setRequestClassName(java.lang.String value) {
		this.setValue(REQUEST_CLASS_NAME, value);
	}

	//
	public java.lang.String getRequestClassName() {
		return (java.lang.String)this.getValue(REQUEST_CLASS_NAME);
	}

	// This attribute is optional
	public void setRequestClassNameJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(USER_NAME) == 0) {
			setValue(USER_NAME, "");
		}
		setAttributeValue(USER_NAME, "J2eeId", value);
	}

	//
	public java.lang.String getRequestClassNameJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(USER_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(USER_NAME, "J2eeId");
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
		// Validating property userName
		if (getUserName() != null) {
			if (getGroupName() != null) {
				throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: UserName and GroupName", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "GroupName", this);	// NOI18N
			}
		}
		// Validating property userNameJ2eeId
		if (getUserNameJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getUserNameJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "userNameJ2eeId", this);	// NOI18N
			}
		}
		// Validating property groupName
		if (getGroupName() != null) {
			if (getUserName() != null) {
				throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: GroupName and UserName", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "UserName", this);	// NOI18N
			}
		}
		// Validating property groupNameId
		if (getGroupNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getGroupNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "groupNameId", this);	// NOI18N
			}
		}
		// Validating property requestClassName
		if (getRequestClassName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getRequestClassName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "requestClassName", this);	// NOI18N
		}
		// Validating property requestClassNameJ2eeId
		if (getRequestClassNameJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRequestClassNameJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "requestClassNameJ2eeId", this);	// NOI18N
			}
		}
		if (getGroupName() == null && getUserName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("required properties: getGroupName() == null && getUserName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "GroupName", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("UserName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getUserName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(USER_NAME, 0, str, indent);

		str.append(indent);
		str.append("GroupName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getGroupName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(GROUP_NAME, 0, str, indent);

		str.append(indent);
		str.append("RequestClassName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getRequestClassName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(REQUEST_CLASS_NAME, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ContextCaseType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

