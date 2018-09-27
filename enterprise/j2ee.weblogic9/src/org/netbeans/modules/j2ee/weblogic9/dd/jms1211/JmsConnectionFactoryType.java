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
 *	This generated bean class JmsConnectionFactoryType matches the schema element 'jms-connection-factory-type'.
 *  The root bean class is WeblogicJms
 *
 *	Generated on Tue Jul 25 03:26:59 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.jms1211;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class JmsConnectionFactoryType extends org.netbeans.modules.j2ee.weblogic9.dd.jms1211.TargetableType
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String NAME = "Name";	// NOI18N
	static public final String NOTES = "Notes";	// NOI18N
	static public final String ID = "Id";	// NOI18N
	static public final String SUB_DEPLOYMENT_NAME = "SubDeploymentName";	// NOI18N
	static public final String DEFAULT_TARGETING_ENABLED = "DefaultTargetingEnabled";	// NOI18N
	static public final String JNDI_NAME = "JndiName";	// NOI18N
	static public final String LOCAL_JNDI_NAME = "LocalJndiName";	// NOI18N
	static public final String DEFAULT_DELIVERY_PARAMS = "DefaultDeliveryParams";	// NOI18N
	static public final String CLIENT_PARAMS = "ClientParams";	// NOI18N
	static public final String TRANSACTION_PARAMS = "TransactionParams";	// NOI18N
	static public final String FLOW_CONTROL_PARAMS = "FlowControlParams";	// NOI18N
	static public final String LOAD_BALANCING_PARAMS = "LoadBalancingParams";	// NOI18N
	static public final String SECURITY_PARAMS = "SecurityParams";	// NOI18N

	public JmsConnectionFactoryType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public JmsConnectionFactoryType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(12);
		this.createProperty("notes", 	// NOI18N
			NOTES, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("id", 	// NOI18N
			ID, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createProperty("sub-deployment-name", 	// NOI18N
			SUB_DEPLOYMENT_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("default-targeting-enabled", 	// NOI18N
			DEFAULT_TARGETING_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("jndi-name", 	// NOI18N
			JNDI_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("local-jndi-name", 	// NOI18N
			LOCAL_JNDI_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("default-delivery-params", 	// NOI18N
			DEFAULT_DELIVERY_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			DefaultDeliveryParamsType.class);
		this.createProperty("client-params", 	// NOI18N
			CLIENT_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ClientParamsType.class);
		this.createProperty("transaction-params", 	// NOI18N
			TRANSACTION_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			TransactionParamsType.class);
		this.createProperty("flow-control-params", 	// NOI18N
			FLOW_CONTROL_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			FlowControlParamsType.class);
		this.createProperty("load-balancing-params", 	// NOI18N
			LOAD_BALANCING_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			LoadBalancingParamsType.class);
		this.createProperty("security-params", 	// NOI18N
			SECURITY_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SecurityParamsType.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setName(java.lang.String value) {
		setAttributeValue(NAME, value);
	}

	//
	public java.lang.String getName() {
		return getAttributeValue(NAME);
	}

	// This attribute is optional
	public void setNotes(java.lang.String value) {
		this.setValue(NOTES, value);
	}

	//
	public java.lang.String getNotes() {
		return (java.lang.String)this.getValue(NOTES);
	}

	// This attribute is optional
	public void setId(long value) {
		this.setValue(ID, java.lang.Long.valueOf(value));
	}

	//
	public long getId() {
		Long ret = (Long)this.getValue(ID);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"ID", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setSubDeploymentName(java.lang.String value) {
		this.setValue(SUB_DEPLOYMENT_NAME, value);
	}

	//
	public java.lang.String getSubDeploymentName() {
		return (java.lang.String)this.getValue(SUB_DEPLOYMENT_NAME);
	}

	// This attribute is optional
	public void setDefaultTargetingEnabled(boolean value) {
		this.setValue(DEFAULT_TARGETING_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isDefaultTargetingEnabled() {
		Boolean ret = (Boolean)this.getValue(DEFAULT_TARGETING_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
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
	public void setDefaultDeliveryParams(DefaultDeliveryParamsType value) {
		this.setValue(DEFAULT_DELIVERY_PARAMS, value);
	}

	//
	public DefaultDeliveryParamsType getDefaultDeliveryParams() {
		return (DefaultDeliveryParamsType)this.getValue(DEFAULT_DELIVERY_PARAMS);
	}

	// This attribute is optional
	public void setClientParams(ClientParamsType value) {
		this.setValue(CLIENT_PARAMS, value);
	}

	//
	public ClientParamsType getClientParams() {
		return (ClientParamsType)this.getValue(CLIENT_PARAMS);
	}

	// This attribute is optional
	public void setTransactionParams(TransactionParamsType value) {
		this.setValue(TRANSACTION_PARAMS, value);
	}

	//
	public TransactionParamsType getTransactionParams() {
		return (TransactionParamsType)this.getValue(TRANSACTION_PARAMS);
	}

	// This attribute is optional
	public void setFlowControlParams(FlowControlParamsType value) {
		this.setValue(FLOW_CONTROL_PARAMS, value);
	}

	//
	public FlowControlParamsType getFlowControlParams() {
		return (FlowControlParamsType)this.getValue(FLOW_CONTROL_PARAMS);
	}

	// This attribute is optional
	public void setLoadBalancingParams(LoadBalancingParamsType value) {
		this.setValue(LOAD_BALANCING_PARAMS, value);
	}

	//
	public LoadBalancingParamsType getLoadBalancingParams() {
		return (LoadBalancingParamsType)this.getValue(LOAD_BALANCING_PARAMS);
	}

	// This attribute is optional
	public void setSecurityParams(SecurityParamsType value) {
		this.setValue(SECURITY_PARAMS, value);
	}

	//
	public SecurityParamsType getSecurityParams() {
		return (SecurityParamsType)this.getValue(SECURITY_PARAMS);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public DefaultDeliveryParamsType newDefaultDeliveryParamsType() {
		return new DefaultDeliveryParamsType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ClientParamsType newClientParamsType() {
		return new ClientParamsType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public TransactionParamsType newTransactionParamsType() {
		return new TransactionParamsType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public FlowControlParamsType newFlowControlParamsType() {
		return new FlowControlParamsType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public LoadBalancingParamsType newLoadBalancingParamsType() {
		return new LoadBalancingParamsType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SecurityParamsType newSecurityParamsType() {
		return new SecurityParamsType();
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
		// Validating property name
		if (getName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "name", this);	// NOI18N
		}
		// Validating property notes
		// Validating property id
		// Validating property subDeploymentName
		// Validating property defaultTargetingEnabled
		// Validating property jndiName
		// Validating property localJndiName
		// Validating property defaultDeliveryParams
		if (getDefaultDeliveryParams() != null) {
			getDefaultDeliveryParams().validate();
		}
		// Validating property clientParams
		if (getClientParams() != null) {
			getClientParams().validate();
		}
		// Validating property transactionParams
		if (getTransactionParams() != null) {
			getTransactionParams().validate();
		}
		// Validating property flowControlParams
		if (getFlowControlParams() != null) {
			getFlowControlParams().validate();
		}
		// Validating property loadBalancingParams
		if (getLoadBalancingParams() != null) {
			getLoadBalancingParams().validate();
		}
		// Validating property securityParams
		if (getSecurityParams() != null) {
			getSecurityParams().validate();
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Notes");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getNotes();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NOTES, 0, str, indent);

		if (this.getValue(ID) != null) {
			str.append(indent);
			str.append("Id");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getId());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(ID, 0, str, indent);
		}

		str.append(indent);
		str.append("SubDeploymentName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getSubDeploymentName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SUB_DEPLOYMENT_NAME, 0, str, indent);

		str.append(indent);
		str.append("DefaultTargetingEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isDefaultTargetingEnabled()?"true":"false"));
		this.dumpAttributes(DEFAULT_TARGETING_ENABLED, 0, str, indent);

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
		str.append("DefaultDeliveryParams");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getDefaultDeliveryParams();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(DEFAULT_DELIVERY_PARAMS, 0, str, indent);

		str.append(indent);
		str.append("ClientParams");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getClientParams();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CLIENT_PARAMS, 0, str, indent);

		str.append(indent);
		str.append("TransactionParams");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getTransactionParams();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(TRANSACTION_PARAMS, 0, str, indent);

		str.append(indent);
		str.append("FlowControlParams");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getFlowControlParams();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(FLOW_CONTROL_PARAMS, 0, str, indent);

		str.append(indent);
		str.append("LoadBalancingParams");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getLoadBalancingParams();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(LOAD_BALANCING_PARAMS, 0, str, indent);

		str.append(indent);
		str.append("SecurityParams");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getSecurityParams();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(SECURITY_PARAMS, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("JmsConnectionFactoryType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

