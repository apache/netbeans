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

public class ContextCaseType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String USER_NAME = "UserName";	// NOI18N
	static public final String USERNAMEJ2EEID = "UserNameJ2eeId";	// NOI18N
	static public final String USERNAMECOMPONENTFACTORYCLASSNAMEJ2EEID2 = "UserNameComponentFactoryClassNameJ2eeId2";	// NOI18N
	static public final String GROUP_NAME = "GroupName";	// NOI18N
	static public final String GROUPNAMEID = "GroupNameId";	// NOI18N
	static public final String RESPONSE_TIME_REQUEST_CLASS = "ResponseTimeRequestClass";	// NOI18N
	static public final String FAIR_SHARE_REQUEST_CLASS = "FairShareRequestClass";	// NOI18N
	static public final String REQUEST_CLASS_NAME = "RequestClassName";	// NOI18N
	static public final String REQUESTCLASSNAMEJ2EEID = "RequestClassNameJ2eeId";	// NOI18N
	static public final String REQUESTCLASSNAMECOMPONENTFACTORYCLASSNAMEJ2EEID2 = "RequestClassNameComponentFactoryClassNameJ2eeId2";	// NOI18N

	public ContextCaseType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ContextCaseType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(5);
		this.createProperty("user-name", 	// NOI18N
			USER_NAME, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(USER_NAME, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(USER_NAME, "j2ee:id", "ComponentFactoryClassNameJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("group-name", 	// NOI18N
			GROUP_NAME, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(GROUP_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("response-time-request-class", 	// NOI18N
			RESPONSE_TIME_REQUEST_CLASS, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ResponseTimeRequestClassType.class);
		this.createAttribute(RESPONSE_TIME_REQUEST_CLASS, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("fair-share-request-class", 	// NOI18N
			FAIR_SHARE_REQUEST_CLASS, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			FairShareRequestClassType.class);
		this.createAttribute(FAIR_SHARE_REQUEST_CLASS, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("request-class-name", 	// NOI18N
			REQUEST_CLASS_NAME, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(REQUEST_CLASS_NAME, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(REQUEST_CLASS_NAME, "j2ee:id", "ComponentFactoryClassNameJ2eeId2", 
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

	// This attribute is optional
	public void setUserNameComponentFactoryClassNameJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(USER_NAME) == 0) {
			setValue(USER_NAME, "");
		}
		setAttributeValue(USER_NAME, "ComponentFactoryClassNameJ2eeId2", value);
	}

	//
	public java.lang.String getUserNameComponentFactoryClassNameJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(USER_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(USER_NAME, "ComponentFactoryClassNameJ2eeId2");
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
	public void setResponseTimeRequestClass(ResponseTimeRequestClassType value) {
		this.setValue(RESPONSE_TIME_REQUEST_CLASS, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setFairShareRequestClass(null);
			setRequestClassName(null);
		}
	}

	//
	public ResponseTimeRequestClassType getResponseTimeRequestClass() {
		return (ResponseTimeRequestClassType)this.getValue(RESPONSE_TIME_REQUEST_CLASS);
	}

	// This attribute is mandatory
	public void setFairShareRequestClass(FairShareRequestClassType value) {
		this.setValue(FAIR_SHARE_REQUEST_CLASS, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setResponseTimeRequestClass(null);
			setRequestClassName(null);
		}
	}

	//
	public FairShareRequestClassType getFairShareRequestClass() {
		return (FairShareRequestClassType)this.getValue(FAIR_SHARE_REQUEST_CLASS);
	}

	// This attribute is mandatory
	public void setRequestClassName(java.lang.String value) {
		this.setValue(REQUEST_CLASS_NAME, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setResponseTimeRequestClass(null);
			setFairShareRequestClass(null);
		}
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

	// This attribute is optional
	public void setRequestClassNameComponentFactoryClassNameJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(USER_NAME) == 0) {
			setValue(USER_NAME, "");
		}
		setAttributeValue(USER_NAME, "ComponentFactoryClassNameJ2eeId2", value);
	}

	//
	public java.lang.String getRequestClassNameComponentFactoryClassNameJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(USER_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(USER_NAME, "ComponentFactoryClassNameJ2eeId2");
		}
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ResponseTimeRequestClassType newResponseTimeRequestClassType() {
		return new ResponseTimeRequestClassType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public FairShareRequestClassType newFairShareRequestClassType() {
		return new FairShareRequestClassType();
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
		// Validating property userNameComponentFactoryClassNameJ2eeId2
		if (getUserNameComponentFactoryClassNameJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getUserNameComponentFactoryClassNameJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "userNameComponentFactoryClassNameJ2eeId2", this);	// NOI18N
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
		// Validating property responseTimeRequestClass
		if (getResponseTimeRequestClass() != null) {
			getResponseTimeRequestClass().validate();
		}
		if (getResponseTimeRequestClass() != null) {
			if (getFairShareRequestClass() != null) {
				throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: ResponseTimeRequestClass and FairShareRequestClass", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "FairShareRequestClass", this);	// NOI18N
			}
			if (getRequestClassName() != null) {
				throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: ResponseTimeRequestClass and RequestClassName", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "RequestClassName", this);	// NOI18N
			}
		}
		// Validating property fairShareRequestClass
		if (getFairShareRequestClass() != null) {
			getFairShareRequestClass().validate();
		}
		if (getFairShareRequestClass() != null) {
			if (getResponseTimeRequestClass() != null) {
				throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: FairShareRequestClass and ResponseTimeRequestClass", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "ResponseTimeRequestClass", this);	// NOI18N
			}
			if (getRequestClassName() != null) {
				throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: FairShareRequestClass and RequestClassName", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "RequestClassName", this);	// NOI18N
			}
		}
		// Validating property requestClassName
		if (getRequestClassName() != null) {
			if (getResponseTimeRequestClass() != null) {
				throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: RequestClassName and ResponseTimeRequestClass", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "ResponseTimeRequestClass", this);	// NOI18N
			}
			if (getFairShareRequestClass() != null) {
				throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: RequestClassName and FairShareRequestClass", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "FairShareRequestClass", this);	// NOI18N
			}
		}
		// Validating property requestClassNameJ2eeId
		if (getRequestClassNameJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRequestClassNameJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "requestClassNameJ2eeId", this);	// NOI18N
			}
		}
		// Validating property requestClassNameComponentFactoryClassNameJ2eeId2
		if (getRequestClassNameComponentFactoryClassNameJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRequestClassNameComponentFactoryClassNameJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "requestClassNameComponentFactoryClassNameJ2eeId2", this);	// NOI18N
			}
		}
		if (getGroupName() == null && getUserName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("required properties: getGroupName() == null && getUserName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "GroupName", this);	// NOI18N
		}
		if (getFairShareRequestClass() == null && getRequestClassName() == null && getResponseTimeRequestClass() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("required properties: getFairShareRequestClass() == null && getRequestClassName() == null && getResponseTimeRequestClass() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "RequestClassName", this);	// NOI18N
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
		str.append("ResponseTimeRequestClass");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getResponseTimeRequestClass();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(RESPONSE_TIME_REQUEST_CLASS, 0, str, indent);

		str.append(indent);
		str.append("FairShareRequestClass");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getFairShareRequestClass();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(FAIR_SHARE_REQUEST_CLASS, 0, str, indent);

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

