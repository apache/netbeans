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
 *	This generated bean class TransportRequirementsType matches the schema element 'transport-requirementsType'.
 *  The root bean class is WeblogicEjbJar
 *
 *	Generated on Tue Jul 25 03:26:51 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ejb1030;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class TransportRequirementsType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String INTEGRITY = "Integrity";	// NOI18N
	static public final String INTEGRITYID = "IntegrityId";	// NOI18N
	static public final String CONFIDENTIALITY = "Confidentiality";	// NOI18N
	static public final String CONFIDENTIALITYID = "ConfidentialityId";	// NOI18N
	static public final String CLIENT_CERT_AUTHENTICATION = "ClientCertAuthentication";	// NOI18N
	static public final String CLIENTCERTAUTHENTICATIONID = "ClientCertAuthenticationId";	// NOI18N

	public TransportRequirementsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public TransportRequirementsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("integrity", 	// NOI18N
			INTEGRITY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(INTEGRITY, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("confidentiality", 	// NOI18N
			CONFIDENTIALITY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(CONFIDENTIALITY, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("client-cert-authentication", 	// NOI18N
			CLIENT_CERT_AUTHENTICATION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(CLIENT_CERT_AUTHENTICATION, "id", "Id", 
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
	public void setIntegrity(java.lang.String value) {
		this.setValue(INTEGRITY, value);
	}

	//
	public java.lang.String getIntegrity() {
		return (java.lang.String)this.getValue(INTEGRITY);
	}

	// This attribute is optional
	public void setIntegrityId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(INTEGRITY) == 0) {
			setValue(INTEGRITY, "");
		}
		setAttributeValue(INTEGRITY, "Id", value);
	}

	//
	public java.lang.String getIntegrityId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(INTEGRITY) == 0) {
			return null;
		} else {
			return getAttributeValue(INTEGRITY, "Id");
		}
	}

	// This attribute is optional
	public void setConfidentiality(java.lang.String value) {
		this.setValue(CONFIDENTIALITY, value);
	}

	//
	public java.lang.String getConfidentiality() {
		return (java.lang.String)this.getValue(CONFIDENTIALITY);
	}

	// This attribute is optional
	public void setConfidentialityId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(CONFIDENTIALITY) == 0) {
			setValue(CONFIDENTIALITY, "");
		}
		setAttributeValue(CONFIDENTIALITY, "Id", value);
	}

	//
	public java.lang.String getConfidentialityId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(CONFIDENTIALITY) == 0) {
			return null;
		} else {
			return getAttributeValue(CONFIDENTIALITY, "Id");
		}
	}

	// This attribute is optional
	public void setClientCertAuthentication(java.lang.String value) {
		this.setValue(CLIENT_CERT_AUTHENTICATION, value);
	}

	//
	public java.lang.String getClientCertAuthentication() {
		return (java.lang.String)this.getValue(CLIENT_CERT_AUTHENTICATION);
	}

	// This attribute is optional
	public void setClientCertAuthenticationId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(CLIENT_CERT_AUTHENTICATION) == 0) {
			setValue(CLIENT_CERT_AUTHENTICATION, "");
		}
		setAttributeValue(CLIENT_CERT_AUTHENTICATION, "Id", value);
	}

	//
	public java.lang.String getClientCertAuthenticationId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(CLIENT_CERT_AUTHENTICATION) == 0) {
			return null;
		} else {
			return getAttributeValue(CLIENT_CERT_AUTHENTICATION, "Id");
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
		// Validating property integrity
		// Validating property integrityId
		if (getIntegrityId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getIntegrityId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "integrityId", this);	// NOI18N
			}
		}
		// Validating property confidentiality
		// Validating property confidentialityId
		if (getConfidentialityId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getConfidentialityId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "confidentialityId", this);	// NOI18N
			}
		}
		// Validating property clientCertAuthentication
		// Validating property clientCertAuthenticationId
		if (getClientCertAuthenticationId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getClientCertAuthenticationId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "clientCertAuthenticationId", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Integrity");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getIntegrity();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(INTEGRITY, 0, str, indent);

		str.append(indent);
		str.append("Confidentiality");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getConfidentiality();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CONFIDENTIALITY, 0, str, indent);

		str.append(indent);
		str.append("ClientCertAuthentication");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getClientCertAuthentication();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CLIENT_CERT_AUTHENTICATION, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("TransportRequirementsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

