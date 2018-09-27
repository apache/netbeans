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
 *	This generated bean class IiopSecurityDescriptorType matches the schema element 'iiop-security-descriptorType'.
 *  The root bean class is WeblogicEjbJar
 *
 *	Generated on Tue Jul 25 03:26:54 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ejb1211;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class IiopSecurityDescriptorType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String TRANSPORT_REQUIREMENTS = "TransportRequirements";	// NOI18N
	static public final String CLIENT_AUTHENTICATION = "ClientAuthentication";	// NOI18N
	static public final String CLIENTAUTHENTICATIONID = "ClientAuthenticationId";	// NOI18N
	static public final String IDENTITY_ASSERTION = "IdentityAssertion";	// NOI18N
	static public final String IDENTITYASSERTIONID = "IdentityAssertionId";	// NOI18N

	public IiopSecurityDescriptorType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public IiopSecurityDescriptorType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("transport-requirements", 	// NOI18N
			TRANSPORT_REQUIREMENTS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			TransportRequirementsType.class);
		this.createAttribute(TRANSPORT_REQUIREMENTS, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("client-authentication", 	// NOI18N
			CLIENT_AUTHENTICATION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(CLIENT_AUTHENTICATION, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("identity-assertion", 	// NOI18N
			IDENTITY_ASSERTION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(IDENTITY_ASSERTION, "id", "Id", 
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
	public void setTransportRequirements(TransportRequirementsType value) {
		this.setValue(TRANSPORT_REQUIREMENTS, value);
	}

	//
	public TransportRequirementsType getTransportRequirements() {
		return (TransportRequirementsType)this.getValue(TRANSPORT_REQUIREMENTS);
	}

	// This attribute is optional
	public void setClientAuthentication(java.lang.String value) {
		this.setValue(CLIENT_AUTHENTICATION, value);
	}

	//
	public java.lang.String getClientAuthentication() {
		return (java.lang.String)this.getValue(CLIENT_AUTHENTICATION);
	}

	// This attribute is optional
	public void setClientAuthenticationId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(CLIENT_AUTHENTICATION) == 0) {
			setValue(CLIENT_AUTHENTICATION, "");
		}
		setAttributeValue(CLIENT_AUTHENTICATION, "Id", value);
	}

	//
	public java.lang.String getClientAuthenticationId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(CLIENT_AUTHENTICATION) == 0) {
			return null;
		} else {
			return getAttributeValue(CLIENT_AUTHENTICATION, "Id");
		}
	}

	// This attribute is optional
	public void setIdentityAssertion(java.lang.String value) {
		this.setValue(IDENTITY_ASSERTION, value);
	}

	//
	public java.lang.String getIdentityAssertion() {
		return (java.lang.String)this.getValue(IDENTITY_ASSERTION);
	}

	// This attribute is optional
	public void setIdentityAssertionId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(IDENTITY_ASSERTION) == 0) {
			setValue(IDENTITY_ASSERTION, "");
		}
		setAttributeValue(IDENTITY_ASSERTION, "Id", value);
	}

	//
	public java.lang.String getIdentityAssertionId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(IDENTITY_ASSERTION) == 0) {
			return null;
		} else {
			return getAttributeValue(IDENTITY_ASSERTION, "Id");
		}
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public TransportRequirementsType newTransportRequirementsType() {
		return new TransportRequirementsType();
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
		// Validating property transportRequirements
		if (getTransportRequirements() != null) {
			getTransportRequirements().validate();
		}
		// Validating property clientAuthentication
		// Validating property clientAuthenticationId
		if (getClientAuthenticationId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getClientAuthenticationId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "clientAuthenticationId", this);	// NOI18N
			}
		}
		// Validating property identityAssertion
		// Validating property identityAssertionId
		if (getIdentityAssertionId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getIdentityAssertionId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "identityAssertionId", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("TransportRequirements");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getTransportRequirements();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(TRANSPORT_REQUIREMENTS, 0, str, indent);

		str.append(indent);
		str.append("ClientAuthentication");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getClientAuthentication();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CLIENT_AUTHENTICATION, 0, str, indent);

		str.append(indent);
		str.append("IdentityAssertion");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getIdentityAssertion();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(IDENTITY_ASSERTION, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("IiopSecurityDescriptorType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

