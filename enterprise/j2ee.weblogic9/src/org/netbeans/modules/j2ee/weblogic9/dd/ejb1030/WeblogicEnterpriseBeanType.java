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
 *	This generated bean class WeblogicEnterpriseBeanType matches the schema element 'weblogic-enterprise-beanType'.
 *  The root bean class is WeblogicEjbJar
 *
 *	Generated on Tue Jul 25 03:26:50 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ejb1030;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class WeblogicEnterpriseBeanType extends org.netbeans.modules.schema2beans.BaseBean
	 implements org.netbeans.modules.j2ee.weblogic9.dd.model.WeblogicEnterpriseBeanType
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String EJB_NAME = "EjbName";	// NOI18N
	static public final String ENTITY_DESCRIPTOR = "EntityDescriptor";	// NOI18N
	static public final String STATELESS_SESSION_DESCRIPTOR = "StatelessSessionDescriptor";	// NOI18N
	static public final String STATEFUL_SESSION_DESCRIPTOR = "StatefulSessionDescriptor";	// NOI18N
	static public final String MESSAGE_DRIVEN_DESCRIPTOR = "MessageDrivenDescriptor";	// NOI18N
	static public final String TRANSACTION_DESCRIPTOR = "TransactionDescriptor";	// NOI18N
	static public final String IIOP_SECURITY_DESCRIPTOR = "IiopSecurityDescriptor";	// NOI18N
	static public final String RESOURCE_DESCRIPTION = "ResourceDescription";	// NOI18N
	static public final String RESOURCE_ENV_DESCRIPTION = "ResourceEnvDescription";	// NOI18N
	static public final String EJB_REFERENCE_DESCRIPTION = "EjbReferenceDescription";	// NOI18N
	static public final String SERVICE_REFERENCE_DESCRIPTION = "ServiceReferenceDescription";	// NOI18N
	static public final String ENABLE_CALL_BY_REFERENCE = "EnableCallByReference";	// NOI18N
	static public final String NETWORK_ACCESS_POINT = "NetworkAccessPoint";	// NOI18N
	static public final String NETWORKACCESSPOINTJ2EEID = "NetworkAccessPointJ2eeId";	// NOI18N
	static public final String CLIENTS_ON_SAME_SERVER = "ClientsOnSameServer";	// NOI18N
	static public final String RUN_AS_PRINCIPAL_NAME = "RunAsPrincipalName";	// NOI18N
	static public final String RUNASPRINCIPALNAMEID = "RunAsPrincipalNameId";	// NOI18N
	static public final String CREATE_AS_PRINCIPAL_NAME = "CreateAsPrincipalName";	// NOI18N
	static public final String CREATEASPRINCIPALNAMEID = "CreateAsPrincipalNameId";	// NOI18N
	static public final String REMOVE_AS_PRINCIPAL_NAME = "RemoveAsPrincipalName";	// NOI18N
	static public final String REMOVEASPRINCIPALNAMEID = "RemoveAsPrincipalNameId";	// NOI18N
	static public final String PASSIVATE_AS_PRINCIPAL_NAME = "PassivateAsPrincipalName";	// NOI18N
	static public final String PASSIVATEASPRINCIPALNAMEID = "PassivateAsPrincipalNameId";	// NOI18N
	static public final String JNDI_NAME = "JndiName";	// NOI18N
	static public final String LOCAL_JNDI_NAME = "LocalJndiName";	// NOI18N
	static public final String DISPATCH_POLICY = "DispatchPolicy";	// NOI18N
	static public final String DISPATCHPOLICYID = "DispatchPolicyId";	// NOI18N
	static public final String REMOTE_CLIENT_TIMEOUT = "RemoteClientTimeout";	// NOI18N
	static public final String REMOTECLIENTTIMEOUTJ2EEID = "RemoteClientTimeoutJ2eeId";	// NOI18N
	static public final String STICK_TO_FIRST_SERVER = "StickToFirstServer";	// NOI18N

	public WeblogicEnterpriseBeanType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public WeblogicEnterpriseBeanType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(23);
		this.createProperty("ejb-name", 	// NOI18N
			EJB_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("entity-descriptor", 	// NOI18N
			ENTITY_DESCRIPTOR, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			EntityDescriptorType.class);
		this.createAttribute(ENTITY_DESCRIPTOR, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("stateless-session-descriptor", 	// NOI18N
			STATELESS_SESSION_DESCRIPTOR, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			StatelessSessionDescriptorType.class);
		this.createAttribute(STATELESS_SESSION_DESCRIPTOR, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("stateful-session-descriptor", 	// NOI18N
			STATEFUL_SESSION_DESCRIPTOR, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			StatefulSessionDescriptorType.class);
		this.createAttribute(STATEFUL_SESSION_DESCRIPTOR, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("message-driven-descriptor", 	// NOI18N
			MESSAGE_DRIVEN_DESCRIPTOR, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MessageDrivenDescriptorType.class);
		this.createAttribute(MESSAGE_DRIVEN_DESCRIPTOR, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("transaction-descriptor", 	// NOI18N
			TRANSACTION_DESCRIPTOR, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			TransactionDescriptorType.class);
		this.createAttribute(TRANSACTION_DESCRIPTOR, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("iiop-security-descriptor", 	// NOI18N
			IIOP_SECURITY_DESCRIPTOR, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			IiopSecurityDescriptorType.class);
		this.createAttribute(IIOP_SECURITY_DESCRIPTOR, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("resource-description", 	// NOI18N
			RESOURCE_DESCRIPTION, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ResourceDescriptionType.class);
		this.createAttribute(RESOURCE_DESCRIPTION, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("resource-env-description", 	// NOI18N
			RESOURCE_ENV_DESCRIPTION, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ResourceEnvDescriptionType.class);
		this.createAttribute(RESOURCE_ENV_DESCRIPTION, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("ejb-reference-description", 	// NOI18N
			EJB_REFERENCE_DESCRIPTION, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			EjbReferenceDescriptionType.class);
		this.createAttribute(EJB_REFERENCE_DESCRIPTION, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("service-reference-description", 	// NOI18N
			SERVICE_REFERENCE_DESCRIPTION, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ServiceReferenceDescriptionType.class);
		this.createAttribute(SERVICE_REFERENCE_DESCRIPTION, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("enable-call-by-reference", 	// NOI18N
			ENABLE_CALL_BY_REFERENCE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("network-access-point", 	// NOI18N
			NETWORK_ACCESS_POINT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(NETWORK_ACCESS_POINT, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("clients-on-same-server", 	// NOI18N
			CLIENTS_ON_SAME_SERVER, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("run-as-principal-name", 	// NOI18N
			RUN_AS_PRINCIPAL_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(RUN_AS_PRINCIPAL_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("create-as-principal-name", 	// NOI18N
			CREATE_AS_PRINCIPAL_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(CREATE_AS_PRINCIPAL_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("remove-as-principal-name", 	// NOI18N
			REMOVE_AS_PRINCIPAL_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(REMOVE_AS_PRINCIPAL_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("passivate-as-principal-name", 	// NOI18N
			PASSIVATE_AS_PRINCIPAL_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(PASSIVATE_AS_PRINCIPAL_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("jndi-name", 	// NOI18N
			JNDI_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("local-jndi-name", 	// NOI18N
			LOCAL_JNDI_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("dispatch-policy", 	// NOI18N
			DISPATCH_POLICY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(DISPATCH_POLICY, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("remote-client-timeout", 	// NOI18N
			REMOTE_CLIENT_TIMEOUT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(REMOTE_CLIENT_TIMEOUT, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("stick-to-first-server", 	// NOI18N
			STICK_TO_FIRST_SERVER, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
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
	public void setEjbName(java.lang.String value) {
		this.setValue(EJB_NAME, value);
	}

	//
	public java.lang.String getEjbName() {
		return (java.lang.String)this.getValue(EJB_NAME);
	}

	// This attribute is mandatory
	public void setEntityDescriptor(EntityDescriptorType value) {
		this.setValue(ENTITY_DESCRIPTOR, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setStatelessSessionDescriptor(null);
			setStatefulSessionDescriptor(null);
			setMessageDrivenDescriptor(null);
		}
	}

	//
	public EntityDescriptorType getEntityDescriptor() {
		return (EntityDescriptorType)this.getValue(ENTITY_DESCRIPTOR);
	}

	// This attribute is mandatory
	public void setStatelessSessionDescriptor(StatelessSessionDescriptorType value) {
		this.setValue(STATELESS_SESSION_DESCRIPTOR, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setEntityDescriptor(null);
			setStatefulSessionDescriptor(null);
			setMessageDrivenDescriptor(null);
		}
	}

	//
	public StatelessSessionDescriptorType getStatelessSessionDescriptor() {
		return (StatelessSessionDescriptorType)this.getValue(STATELESS_SESSION_DESCRIPTOR);
	}

	// This attribute is mandatory
	public void setStatefulSessionDescriptor(StatefulSessionDescriptorType value) {
		this.setValue(STATEFUL_SESSION_DESCRIPTOR, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setEntityDescriptor(null);
			setStatelessSessionDescriptor(null);
			setMessageDrivenDescriptor(null);
		}
	}

	//
	public StatefulSessionDescriptorType getStatefulSessionDescriptor() {
		return (StatefulSessionDescriptorType)this.getValue(STATEFUL_SESSION_DESCRIPTOR);
	}

	// This attribute is mandatory
	public void setMessageDrivenDescriptor(MessageDrivenDescriptorType value) {
		this.setValue(MESSAGE_DRIVEN_DESCRIPTOR, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setEntityDescriptor(null);
			setStatelessSessionDescriptor(null);
			setStatefulSessionDescriptor(null);
		}
	}

	//
	public MessageDrivenDescriptorType getMessageDrivenDescriptor() {
		return (MessageDrivenDescriptorType)this.getValue(MESSAGE_DRIVEN_DESCRIPTOR);
	}

	// This attribute is optional
	public void setTransactionDescriptor(TransactionDescriptorType value) {
		this.setValue(TRANSACTION_DESCRIPTOR, value);
	}

	//
	public TransactionDescriptorType getTransactionDescriptor() {
		return (TransactionDescriptorType)this.getValue(TRANSACTION_DESCRIPTOR);
	}

	// This attribute is optional
	public void setIiopSecurityDescriptor(IiopSecurityDescriptorType value) {
		this.setValue(IIOP_SECURITY_DESCRIPTOR, value);
	}

	//
	public IiopSecurityDescriptorType getIiopSecurityDescriptor() {
		return (IiopSecurityDescriptorType)this.getValue(IIOP_SECURITY_DESCRIPTOR);
	}

	// This attribute is an array, possibly empty
	public void setResourceDescription(int index, ResourceDescriptionType value) {
		this.setValue(RESOURCE_DESCRIPTION, index, value);
	}

	//
	public ResourceDescriptionType getResourceDescription(int index) {
		return (ResourceDescriptionType)this.getValue(RESOURCE_DESCRIPTION, index);
	}

	// Return the number of properties
	public int sizeResourceDescription() {
		return this.size(RESOURCE_DESCRIPTION);
	}

	// This attribute is an array, possibly empty
	public void setResourceDescription(ResourceDescriptionType[] value) {
		this.setValue(RESOURCE_DESCRIPTION, value);
	}

	//
	public ResourceDescriptionType[] getResourceDescription() {
		return (ResourceDescriptionType[])this.getValues(RESOURCE_DESCRIPTION);
	}

	// Add a new element returning its index in the list
	public int addResourceDescription(org.netbeans.modules.j2ee.weblogic9.dd.ejb1030.ResourceDescriptionType value) {
		int positionOfNewItem = this.addValue(RESOURCE_DESCRIPTION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeResourceDescription(org.netbeans.modules.j2ee.weblogic9.dd.ejb1030.ResourceDescriptionType value) {
		return this.removeValue(RESOURCE_DESCRIPTION, value);
	}

	// This attribute is an array, possibly empty
	public void setResourceEnvDescription(int index, ResourceEnvDescriptionType value) {
		this.setValue(RESOURCE_ENV_DESCRIPTION, index, value);
	}

	//
	public ResourceEnvDescriptionType getResourceEnvDescription(int index) {
		return (ResourceEnvDescriptionType)this.getValue(RESOURCE_ENV_DESCRIPTION, index);
	}

	// Return the number of properties
	public int sizeResourceEnvDescription() {
		return this.size(RESOURCE_ENV_DESCRIPTION);
	}

	// This attribute is an array, possibly empty
	public void setResourceEnvDescription(ResourceEnvDescriptionType[] value) {
		this.setValue(RESOURCE_ENV_DESCRIPTION, value);
	}

	//
	public ResourceEnvDescriptionType[] getResourceEnvDescription() {
		return (ResourceEnvDescriptionType[])this.getValues(RESOURCE_ENV_DESCRIPTION);
	}

	// Add a new element returning its index in the list
	public int addResourceEnvDescription(org.netbeans.modules.j2ee.weblogic9.dd.ejb1030.ResourceEnvDescriptionType value) {
		int positionOfNewItem = this.addValue(RESOURCE_ENV_DESCRIPTION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeResourceEnvDescription(org.netbeans.modules.j2ee.weblogic9.dd.ejb1030.ResourceEnvDescriptionType value) {
		return this.removeValue(RESOURCE_ENV_DESCRIPTION, value);
	}

	// This attribute is an array, possibly empty
	public void setEjbReferenceDescription(int index, EjbReferenceDescriptionType value) {
		this.setValue(EJB_REFERENCE_DESCRIPTION, index, value);
	}

	//
	public EjbReferenceDescriptionType getEjbReferenceDescription(int index) {
		return (EjbReferenceDescriptionType)this.getValue(EJB_REFERENCE_DESCRIPTION, index);
	}

	// Return the number of properties
	public int sizeEjbReferenceDescription() {
		return this.size(EJB_REFERENCE_DESCRIPTION);
	}

	// This attribute is an array, possibly empty
	public void setEjbReferenceDescription(EjbReferenceDescriptionType[] value) {
		this.setValue(EJB_REFERENCE_DESCRIPTION, value);
	}

	//
	public EjbReferenceDescriptionType[] getEjbReferenceDescription() {
		return (EjbReferenceDescriptionType[])this.getValues(EJB_REFERENCE_DESCRIPTION);
	}

	// Add a new element returning its index in the list
	public int addEjbReferenceDescription(org.netbeans.modules.j2ee.weblogic9.dd.ejb1030.EjbReferenceDescriptionType value) {
		int positionOfNewItem = this.addValue(EJB_REFERENCE_DESCRIPTION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeEjbReferenceDescription(org.netbeans.modules.j2ee.weblogic9.dd.ejb1030.EjbReferenceDescriptionType value) {
		return this.removeValue(EJB_REFERENCE_DESCRIPTION, value);
	}

	// This attribute is an array, possibly empty
	public void setServiceReferenceDescription(int index, ServiceReferenceDescriptionType value) {
		this.setValue(SERVICE_REFERENCE_DESCRIPTION, index, value);
	}

	//
	public ServiceReferenceDescriptionType getServiceReferenceDescription(int index) {
		return (ServiceReferenceDescriptionType)this.getValue(SERVICE_REFERENCE_DESCRIPTION, index);
	}

	// Return the number of properties
	public int sizeServiceReferenceDescription() {
		return this.size(SERVICE_REFERENCE_DESCRIPTION);
	}

	// This attribute is an array, possibly empty
	public void setServiceReferenceDescription(ServiceReferenceDescriptionType[] value) {
		this.setValue(SERVICE_REFERENCE_DESCRIPTION, value);
	}

	//
	public ServiceReferenceDescriptionType[] getServiceReferenceDescription() {
		return (ServiceReferenceDescriptionType[])this.getValues(SERVICE_REFERENCE_DESCRIPTION);
	}

	// Add a new element returning its index in the list
	public int addServiceReferenceDescription(org.netbeans.modules.j2ee.weblogic9.dd.ejb1030.ServiceReferenceDescriptionType value) {
		int positionOfNewItem = this.addValue(SERVICE_REFERENCE_DESCRIPTION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeServiceReferenceDescription(org.netbeans.modules.j2ee.weblogic9.dd.ejb1030.ServiceReferenceDescriptionType value) {
		return this.removeValue(SERVICE_REFERENCE_DESCRIPTION, value);
	}

	// This attribute is optional
	public void setEnableCallByReference(boolean value) {
		this.setValue(ENABLE_CALL_BY_REFERENCE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isEnableCallByReference() {
		Boolean ret = (Boolean)this.getValue(ENABLE_CALL_BY_REFERENCE);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setNetworkAccessPoint(java.lang.String value) {
		this.setValue(NETWORK_ACCESS_POINT, value);
	}

	//
	public java.lang.String getNetworkAccessPoint() {
		return (java.lang.String)this.getValue(NETWORK_ACCESS_POINT);
	}

	// This attribute is optional
	public void setNetworkAccessPointJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(NETWORK_ACCESS_POINT) == 0) {
			setValue(NETWORK_ACCESS_POINT, "");
		}
		setAttributeValue(NETWORK_ACCESS_POINT, "J2eeId", value);
	}

	//
	public java.lang.String getNetworkAccessPointJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(NETWORK_ACCESS_POINT) == 0) {
			return null;
		} else {
			return getAttributeValue(NETWORK_ACCESS_POINT, "J2eeId");
		}
	}

	// This attribute is optional
	public void setClientsOnSameServer(boolean value) {
		this.setValue(CLIENTS_ON_SAME_SERVER, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isClientsOnSameServer() {
		Boolean ret = (Boolean)this.getValue(CLIENTS_ON_SAME_SERVER);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
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
	public void setCreateAsPrincipalName(java.lang.String value) {
		this.setValue(CREATE_AS_PRINCIPAL_NAME, value);
	}

	//
	public java.lang.String getCreateAsPrincipalName() {
		return (java.lang.String)this.getValue(CREATE_AS_PRINCIPAL_NAME);
	}

	// This attribute is optional
	public void setCreateAsPrincipalNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(CREATE_AS_PRINCIPAL_NAME) == 0) {
			setValue(CREATE_AS_PRINCIPAL_NAME, "");
		}
		setAttributeValue(CREATE_AS_PRINCIPAL_NAME, "Id", value);
	}

	//
	public java.lang.String getCreateAsPrincipalNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(CREATE_AS_PRINCIPAL_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(CREATE_AS_PRINCIPAL_NAME, "Id");
		}
	}

	// This attribute is optional
	public void setRemoveAsPrincipalName(java.lang.String value) {
		this.setValue(REMOVE_AS_PRINCIPAL_NAME, value);
	}

	//
	public java.lang.String getRemoveAsPrincipalName() {
		return (java.lang.String)this.getValue(REMOVE_AS_PRINCIPAL_NAME);
	}

	// This attribute is optional
	public void setRemoveAsPrincipalNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REMOVE_AS_PRINCIPAL_NAME) == 0) {
			setValue(REMOVE_AS_PRINCIPAL_NAME, "");
		}
		setAttributeValue(REMOVE_AS_PRINCIPAL_NAME, "Id", value);
	}

	//
	public java.lang.String getRemoveAsPrincipalNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REMOVE_AS_PRINCIPAL_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(REMOVE_AS_PRINCIPAL_NAME, "Id");
		}
	}

	// This attribute is optional
	public void setPassivateAsPrincipalName(java.lang.String value) {
		this.setValue(PASSIVATE_AS_PRINCIPAL_NAME, value);
	}

	//
	public java.lang.String getPassivateAsPrincipalName() {
		return (java.lang.String)this.getValue(PASSIVATE_AS_PRINCIPAL_NAME);
	}

	// This attribute is optional
	public void setPassivateAsPrincipalNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(PASSIVATE_AS_PRINCIPAL_NAME) == 0) {
			setValue(PASSIVATE_AS_PRINCIPAL_NAME, "");
		}
		setAttributeValue(PASSIVATE_AS_PRINCIPAL_NAME, "Id", value);
	}

	//
	public java.lang.String getPassivateAsPrincipalNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(PASSIVATE_AS_PRINCIPAL_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(PASSIVATE_AS_PRINCIPAL_NAME, "Id");
		}
	}

	// This attribute is optional
	public void setJndiName(java.lang.String value) {
		this.setValue(JNDI_NAME, value);
	}

	//
	public java.lang.String getJndiName() {
		return (java.lang.String)this.getValue(JNDI_NAME);
	}

	// This attribute is optional
	public void setLocalJndiName(java.lang.String value) {
		this.setValue(LOCAL_JNDI_NAME, value);
	}

	//
	public java.lang.String getLocalJndiName() {
		return (java.lang.String)this.getValue(LOCAL_JNDI_NAME);
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

	// This attribute is optional
	public void setRemoteClientTimeout(long value) {
		this.setValue(REMOTE_CLIENT_TIMEOUT, java.lang.Long.valueOf(value));
	}

	//
	public long getRemoteClientTimeout() {
		Long ret = (Long)this.getValue(REMOTE_CLIENT_TIMEOUT);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"REMOTE_CLIENT_TIMEOUT", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setRemoteClientTimeoutJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REMOTE_CLIENT_TIMEOUT) == 0) {
			setValue(REMOTE_CLIENT_TIMEOUT, "");
		}
		setAttributeValue(REMOTE_CLIENT_TIMEOUT, "J2eeId", value);
	}

	//
	public java.lang.String getRemoteClientTimeoutJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REMOTE_CLIENT_TIMEOUT) == 0) {
			return null;
		} else {
			return getAttributeValue(REMOTE_CLIENT_TIMEOUT, "J2eeId");
		}
	}

	// This attribute is optional
	public void setStickToFirstServer(boolean value) {
		this.setValue(STICK_TO_FIRST_SERVER, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isStickToFirstServer() {
		Boolean ret = (Boolean)this.getValue(STICK_TO_FIRST_SERVER);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public EntityDescriptorType newEntityDescriptorType() {
		return new EntityDescriptorType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public StatelessSessionDescriptorType newStatelessSessionDescriptorType() {
		return new StatelessSessionDescriptorType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public StatefulSessionDescriptorType newStatefulSessionDescriptorType() {
		return new StatefulSessionDescriptorType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public MessageDrivenDescriptorType newMessageDrivenDescriptorType() {
		return new MessageDrivenDescriptorType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public TransactionDescriptorType newTransactionDescriptorType() {
		return new TransactionDescriptorType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public IiopSecurityDescriptorType newIiopSecurityDescriptorType() {
		return new IiopSecurityDescriptorType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ResourceDescriptionType newResourceDescriptionType() {
		return new ResourceDescriptionType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ResourceEnvDescriptionType newResourceEnvDescriptionType() {
		return new ResourceEnvDescriptionType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public EjbReferenceDescriptionType newEjbReferenceDescriptionType() {
		return new EjbReferenceDescriptionType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ServiceReferenceDescriptionType newServiceReferenceDescriptionType() {
		return new ServiceReferenceDescriptionType();
	}

	//
	public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.add(c);
	}

	//
	public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.remove(c);
	}

	
                    public ResourceDescriptionType addResourceDescription() {
                        ResourceDescriptionType description = new ResourceDescriptionType();
                        addResourceDescription(description);
                        return description;
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
		// Validating property ejbName
		if (getEjbName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getEjbName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "ejbName", this);	// NOI18N
		}
		// Validating property entityDescriptor
		if (getEntityDescriptor() != null) {
			getEntityDescriptor().validate();
		}
		// Validating property statelessSessionDescriptor
		if (getStatelessSessionDescriptor() != null) {
			getStatelessSessionDescriptor().validate();
		}
		// Validating property statefulSessionDescriptor
		if (getStatefulSessionDescriptor() != null) {
			getStatefulSessionDescriptor().validate();
		}
		// Validating property messageDrivenDescriptor
		if (getMessageDrivenDescriptor() != null) {
			getMessageDrivenDescriptor().validate();
		}
		// Validating property transactionDescriptor
		if (getTransactionDescriptor() != null) {
			getTransactionDescriptor().validate();
		}
		// Validating property iiopSecurityDescriptor
		if (getIiopSecurityDescriptor() != null) {
			getIiopSecurityDescriptor().validate();
		}
		// Validating property resourceDescription
		for (int _index = 0; _index < sizeResourceDescription(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ejb1030.ResourceDescriptionType element = getResourceDescription(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property resourceEnvDescription
		for (int _index = 0; _index < sizeResourceEnvDescription(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ejb1030.ResourceEnvDescriptionType element = getResourceEnvDescription(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property ejbReferenceDescription
		for (int _index = 0; _index < sizeEjbReferenceDescription(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ejb1030.EjbReferenceDescriptionType element = getEjbReferenceDescription(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property serviceReferenceDescription
		for (int _index = 0; _index < sizeServiceReferenceDescription(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ejb1030.ServiceReferenceDescriptionType element = getServiceReferenceDescription(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property enableCallByReference
		{
			boolean patternPassed = false;
			if ((isEnableCallByReference() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isEnableCallByReference()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "enableCallByReference", this);	// NOI18N
		}
		// Validating property networkAccessPoint
		// Validating property networkAccessPointJ2eeId
		if (getNetworkAccessPointJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getNetworkAccessPointJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "networkAccessPointJ2eeId", this);	// NOI18N
			}
		}
		// Validating property clientsOnSameServer
		{
			boolean patternPassed = false;
			if ((isClientsOnSameServer() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isClientsOnSameServer()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "clientsOnSameServer", this);	// NOI18N
		}
		// Validating property runAsPrincipalName
		// Validating property runAsPrincipalNameId
		if (getRunAsPrincipalNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRunAsPrincipalNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "runAsPrincipalNameId", this);	// NOI18N
			}
		}
		// Validating property createAsPrincipalName
		// Validating property createAsPrincipalNameId
		if (getCreateAsPrincipalNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getCreateAsPrincipalNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "createAsPrincipalNameId", this);	// NOI18N
			}
		}
		// Validating property removeAsPrincipalName
		// Validating property removeAsPrincipalNameId
		if (getRemoveAsPrincipalNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRemoveAsPrincipalNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "removeAsPrincipalNameId", this);	// NOI18N
			}
		}
		// Validating property passivateAsPrincipalName
		// Validating property passivateAsPrincipalNameId
		if (getPassivateAsPrincipalNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getPassivateAsPrincipalNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "passivateAsPrincipalNameId", this);	// NOI18N
			}
		}
		// Validating property jndiName
		// Validating property localJndiName
		// Validating property dispatchPolicy
		// Validating property dispatchPolicyId
		if (getDispatchPolicyId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getDispatchPolicyId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "dispatchPolicyId", this);	// NOI18N
			}
		}
		// Validating property remoteClientTimeout
		if (getRemoteClientTimeout() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getRemoteClientTimeout() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "remoteClientTimeout", this);	// NOI18N
		}
		// Validating property remoteClientTimeoutJ2eeId
		if (getRemoteClientTimeoutJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRemoteClientTimeoutJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "remoteClientTimeoutJ2eeId", this);	// NOI18N
			}
		}
		// Validating property stickToFirstServer
		{
			boolean patternPassed = false;
			if ((isStickToFirstServer() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isStickToFirstServer()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "stickToFirstServer", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("EjbName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getEjbName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(EJB_NAME, 0, str, indent);

		str.append(indent);
		str.append("EntityDescriptor");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getEntityDescriptor();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(ENTITY_DESCRIPTOR, 0, str, indent);

		str.append(indent);
		str.append("StatelessSessionDescriptor");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getStatelessSessionDescriptor();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(STATELESS_SESSION_DESCRIPTOR, 0, str, indent);

		str.append(indent);
		str.append("StatefulSessionDescriptor");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getStatefulSessionDescriptor();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(STATEFUL_SESSION_DESCRIPTOR, 0, str, indent);

		str.append(indent);
		str.append("MessageDrivenDescriptor");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getMessageDrivenDescriptor();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(MESSAGE_DRIVEN_DESCRIPTOR, 0, str, indent);

		str.append(indent);
		str.append("TransactionDescriptor");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getTransactionDescriptor();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(TRANSACTION_DESCRIPTOR, 0, str, indent);

		str.append(indent);
		str.append("IiopSecurityDescriptor");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getIiopSecurityDescriptor();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(IIOP_SECURITY_DESCRIPTOR, 0, str, indent);

		str.append(indent);
		str.append("ResourceDescription["+this.sizeResourceDescription()+"]");	// NOI18N
		for(int i=0; i<this.sizeResourceDescription(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getResourceDescription(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(RESOURCE_DESCRIPTION, i, str, indent);
		}

		str.append(indent);
		str.append("ResourceEnvDescription["+this.sizeResourceEnvDescription()+"]");	// NOI18N
		for(int i=0; i<this.sizeResourceEnvDescription(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getResourceEnvDescription(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(RESOURCE_ENV_DESCRIPTION, i, str, indent);
		}

		str.append(indent);
		str.append("EjbReferenceDescription["+this.sizeEjbReferenceDescription()+"]");	// NOI18N
		for(int i=0; i<this.sizeEjbReferenceDescription(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getEjbReferenceDescription(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(EJB_REFERENCE_DESCRIPTION, i, str, indent);
		}

		str.append(indent);
		str.append("ServiceReferenceDescription["+this.sizeServiceReferenceDescription()+"]");	// NOI18N
		for(int i=0; i<this.sizeServiceReferenceDescription(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getServiceReferenceDescription(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SERVICE_REFERENCE_DESCRIPTION, i, str, indent);
		}

		str.append(indent);
		str.append("EnableCallByReference");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isEnableCallByReference()?"true":"false"));
		this.dumpAttributes(ENABLE_CALL_BY_REFERENCE, 0, str, indent);

		str.append(indent);
		str.append("NetworkAccessPoint");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getNetworkAccessPoint();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NETWORK_ACCESS_POINT, 0, str, indent);

		str.append(indent);
		str.append("ClientsOnSameServer");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isClientsOnSameServer()?"true":"false"));
		this.dumpAttributes(CLIENTS_ON_SAME_SERVER, 0, str, indent);

		str.append(indent);
		str.append("RunAsPrincipalName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getRunAsPrincipalName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(RUN_AS_PRINCIPAL_NAME, 0, str, indent);

		str.append(indent);
		str.append("CreateAsPrincipalName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getCreateAsPrincipalName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CREATE_AS_PRINCIPAL_NAME, 0, str, indent);

		str.append(indent);
		str.append("RemoveAsPrincipalName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getRemoveAsPrincipalName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(REMOVE_AS_PRINCIPAL_NAME, 0, str, indent);

		str.append(indent);
		str.append("PassivateAsPrincipalName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPassivateAsPrincipalName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PASSIVATE_AS_PRINCIPAL_NAME, 0, str, indent);

		str.append(indent);
		str.append("JndiName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getJndiName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(JNDI_NAME, 0, str, indent);

		str.append(indent);
		str.append("LocalJndiName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getLocalJndiName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(LOCAL_JNDI_NAME, 0, str, indent);

		str.append(indent);
		str.append("DispatchPolicy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDispatchPolicy();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DISPATCH_POLICY, 0, str, indent);

		if (this.getValue(REMOTE_CLIENT_TIMEOUT) != null) {
			str.append(indent);
			str.append("RemoteClientTimeout");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getRemoteClientTimeout());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(REMOTE_CLIENT_TIMEOUT, 0, str, indent);
		}

		str.append(indent);
		str.append("StickToFirstServer");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isStickToFirstServer()?"true":"false"));
		this.dumpAttributes(STICK_TO_FIRST_SERVER, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("WeblogicEnterpriseBeanType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

