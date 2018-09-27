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
 *	This generated bean class ServletDescriptorType matches the schema element 'servlet-descriptorType'.
 *  The root bean class is WeblogicWebApp
 *
 *	Generated on Tue Jul 25 03:27:06 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.web90;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ServletDescriptorType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String SERVLET_NAME = "ServletName";	// NOI18N
	static public final String RUN_AS_PRINCIPAL_NAME = "RunAsPrincipalName";	// NOI18N
	static public final String RUNASPRINCIPALNAMEID = "RunAsPrincipalNameId";	// NOI18N
	static public final String INIT_AS_PRINCIPAL_NAME = "InitAsPrincipalName";	// NOI18N
	static public final String INITASPRINCIPALNAMEID = "InitAsPrincipalNameId";	// NOI18N
	static public final String DESTROY_AS_PRINCIPAL_NAME = "DestroyAsPrincipalName";	// NOI18N
	static public final String DESTROYASPRINCIPALNAMEID = "DestroyAsPrincipalNameId";	// NOI18N
	static public final String DISPATCH_POLICY = "DispatchPolicy";	// NOI18N
	static public final String DISPATCHPOLICYID = "DispatchPolicyId";	// NOI18N

	public ServletDescriptorType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ServletDescriptorType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(5);
		this.createProperty("servlet-name", 	// NOI18N
			SERVLET_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("run-as-principal-name", 	// NOI18N
			RUN_AS_PRINCIPAL_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(RUN_AS_PRINCIPAL_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("init-as-principal-name", 	// NOI18N
			INIT_AS_PRINCIPAL_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(INIT_AS_PRINCIPAL_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("destroy-as-principal-name", 	// NOI18N
			DESTROY_AS_PRINCIPAL_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(DESTROY_AS_PRINCIPAL_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("dispatch-policy", 	// NOI18N
			DISPATCH_POLICY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(DISPATCH_POLICY, "id", "Id", 
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
	public void setServletName(java.lang.String value) {
		this.setValue(SERVLET_NAME, value);
	}

	//
	public java.lang.String getServletName() {
		return (java.lang.String)this.getValue(SERVLET_NAME);
	}

	// This attribute is optional
	public void setRunAsPrincipalName(java.lang.String value) {
		this.setValue(RUN_AS_PRINCIPAL_NAME, value);
	}

	//
	public java.lang.String getRunAsPrincipalName() {
		return (java.lang.String)this.getValue(RUN_AS_PRINCIPAL_NAME);
	}

	// This attribute is optional
	public void setRunAsPrincipalNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(RUN_AS_PRINCIPAL_NAME) == 0) {
			setValue(RUN_AS_PRINCIPAL_NAME, "");
		}
		setAttributeValue(RUN_AS_PRINCIPAL_NAME, "Id", value);
	}

	//
	public java.lang.String getRunAsPrincipalNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(RUN_AS_PRINCIPAL_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(RUN_AS_PRINCIPAL_NAME, "Id");
		}
	}

	// This attribute is optional
	public void setInitAsPrincipalName(java.lang.String value) {
		this.setValue(INIT_AS_PRINCIPAL_NAME, value);
	}

	//
	public java.lang.String getInitAsPrincipalName() {
		return (java.lang.String)this.getValue(INIT_AS_PRINCIPAL_NAME);
	}

	// This attribute is optional
	public void setInitAsPrincipalNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(INIT_AS_PRINCIPAL_NAME) == 0) {
			setValue(INIT_AS_PRINCIPAL_NAME, "");
		}
		setAttributeValue(INIT_AS_PRINCIPAL_NAME, "Id", value);
	}

	//
	public java.lang.String getInitAsPrincipalNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(INIT_AS_PRINCIPAL_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(INIT_AS_PRINCIPAL_NAME, "Id");
		}
	}

	// This attribute is optional
	public void setDestroyAsPrincipalName(java.lang.String value) {
		this.setValue(DESTROY_AS_PRINCIPAL_NAME, value);
	}

	//
	public java.lang.String getDestroyAsPrincipalName() {
		return (java.lang.String)this.getValue(DESTROY_AS_PRINCIPAL_NAME);
	}

	// This attribute is optional
	public void setDestroyAsPrincipalNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(DESTROY_AS_PRINCIPAL_NAME) == 0) {
			setValue(DESTROY_AS_PRINCIPAL_NAME, "");
		}
		setAttributeValue(DESTROY_AS_PRINCIPAL_NAME, "Id", value);
	}

	//
	public java.lang.String getDestroyAsPrincipalNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(DESTROY_AS_PRINCIPAL_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(DESTROY_AS_PRINCIPAL_NAME, "Id");
		}
	}

	// This attribute is optional
	public void setDispatchPolicy(java.lang.String value) {
		this.setValue(DISPATCH_POLICY, value);
	}

	//
	public java.lang.String getDispatchPolicy() {
		return (java.lang.String)this.getValue(DISPATCH_POLICY);
	}

	// This attribute is optional
	public void setDispatchPolicyId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(DISPATCH_POLICY) == 0) {
			setValue(DISPATCH_POLICY, "");
		}
		setAttributeValue(DISPATCH_POLICY, "Id", value);
	}

	//
	public java.lang.String getDispatchPolicyId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(DISPATCH_POLICY) == 0) {
			return null;
		} else {
			return getAttributeValue(DISPATCH_POLICY, "Id");
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
		// Validating property servletName
		if (getServletName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getServletName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "servletName", this);	// NOI18N
		}
		if ((getServletName()).length() < 1) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getServletName() minLength (1)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "servletName", this);	// NOI18N
		}
		// Validating property runAsPrincipalName
		// Validating property runAsPrincipalNameId
		if (getRunAsPrincipalNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRunAsPrincipalNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "runAsPrincipalNameId", this);	// NOI18N
			}
		}
		// Validating property initAsPrincipalName
		// Validating property initAsPrincipalNameId
		if (getInitAsPrincipalNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getInitAsPrincipalNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "initAsPrincipalNameId", this);	// NOI18N
			}
		}
		// Validating property destroyAsPrincipalName
		// Validating property destroyAsPrincipalNameId
		if (getDestroyAsPrincipalNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getDestroyAsPrincipalNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "destroyAsPrincipalNameId", this);	// NOI18N
			}
		}
		// Validating property dispatchPolicy
		// Validating property dispatchPolicyId
		if (getDispatchPolicyId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getDispatchPolicyId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "dispatchPolicyId", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("ServletName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getServletName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SERVLET_NAME, 0, str, indent);

		str.append(indent);
		str.append("RunAsPrincipalName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getRunAsPrincipalName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(RUN_AS_PRINCIPAL_NAME, 0, str, indent);

		str.append(indent);
		str.append("InitAsPrincipalName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getInitAsPrincipalName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(INIT_AS_PRINCIPAL_NAME, 0, str, indent);

		str.append(indent);
		str.append("DestroyAsPrincipalName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDestroyAsPrincipalName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DESTROY_AS_PRINCIPAL_NAME, 0, str, indent);

		str.append(indent);
		str.append("DispatchPolicy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDispatchPolicy();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DISPATCH_POLICY, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ServletDescriptorType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

