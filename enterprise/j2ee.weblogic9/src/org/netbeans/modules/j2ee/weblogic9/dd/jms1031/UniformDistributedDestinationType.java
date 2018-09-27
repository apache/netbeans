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
 *	This generated bean class UniformDistributedDestinationType matches the schema element 'uniform-distributed-destination-type'.
 *  The root bean class is WeblogicJms
 *
 *	Generated on Tue Jul 25 03:26:58 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.jms1031;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class UniformDistributedDestinationType extends org.netbeans.modules.j2ee.weblogic9.dd.jms1031.DestinationType
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String NAME = "Name";	// NOI18N
	static public final String NOTES = "Notes";	// NOI18N
	static public final String SUB_DEPLOYMENT_NAME = "SubDeploymentName";	// NOI18N
	static public final String DEFAULT_TARGETING_ENABLED = "DefaultTargetingEnabled";	// NOI18N
	static public final String TEMPLATE = "Template";	// NOI18N
	static public final String DESTINATION_KEY = "DestinationKey";	// NOI18N
	static public final String THRESHOLDS = "Thresholds";	// NOI18N
	static public final String DELIVERY_PARAMS_OVERRIDES = "DeliveryParamsOverrides";	// NOI18N
	static public final String DELIVERY_FAILURE_PARAMS = "DeliveryFailureParams";	// NOI18N
	static public final String MESSAGE_LOGGING_PARAMS = "MessageLoggingParams";	// NOI18N
	static public final String ATTACH_SENDER = "AttachSender";	// NOI18N
	static public final String PRODUCTION_PAUSED_AT_STARTUP = "ProductionPausedAtStartup";	// NOI18N
	static public final String INSERTION_PAUSED_AT_STARTUP = "InsertionPausedAtStartup";	// NOI18N
	static public final String CONSUMPTION_PAUSED_AT_STARTUP = "ConsumptionPausedAtStartup";	// NOI18N
	static public final String MAXIMUM_MESSAGE_SIZE = "MaximumMessageSize";	// NOI18N
	static public final String QUOTA = "Quota";	// NOI18N
	static public final String JNDI_NAME = "JndiName";	// NOI18N
	static public final String LOCAL_JNDI_NAME = "LocalJndiName";	// NOI18N
	static public final String JMS_CREATE_DESTINATION_IDENTIFIER = "JmsCreateDestinationIdentifier";	// NOI18N
	static public final String DEFAULT_UNIT_OF_ORDER = "DefaultUnitOfOrder";	// NOI18N
	static public final String SAF_EXPORT_POLICY = "SafExportPolicy";	// NOI18N
	static public final String MESSAGING_PERFORMANCE_PREFERENCE = "MessagingPerformancePreference";	// NOI18N
	static public final String UNIT_OF_WORK_HANDLING_POLICY = "UnitOfWorkHandlingPolicy";	// NOI18N
	static public final String INCOMPLETE_WORK_EXPIRATION_TIME = "IncompleteWorkExpirationTime";	// NOI18N
	static public final String LOAD_BALANCING_POLICY = "LoadBalancingPolicy";	// NOI18N
	static public final String UNIT_OF_ORDER_ROUTING = "UnitOfOrderRouting";	// NOI18N

	public UniformDistributedDestinationType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	protected UniformDistributedDestinationType(Vector comparators, Version runtimeVersion){

		super(comparators, runtimeVersion);
	}
	public UniformDistributedDestinationType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(25);
		this.createProperty("notes", 	// NOI18N
			NOTES, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("sub-deployment-name", 	// NOI18N
			SUB_DEPLOYMENT_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("default-targeting-enabled", 	// NOI18N
			DEFAULT_TARGETING_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("template", 	// NOI18N
			TEMPLATE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("destination-key", 	// NOI18N
			DESTINATION_KEY, 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("thresholds", 	// NOI18N
			THRESHOLDS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ThresholdParamsType.class);
		this.createProperty("delivery-params-overrides", 	// NOI18N
			DELIVERY_PARAMS_OVERRIDES, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			DeliveryParamsOverridesType.class);
		this.createProperty("delivery-failure-params", 	// NOI18N
			DELIVERY_FAILURE_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			DeliveryFailureParamsType.class);
		this.createProperty("message-logging-params", 	// NOI18N
			MESSAGE_LOGGING_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MessageLoggingParamsType.class);
		this.createProperty("attach-sender", 	// NOI18N
			ATTACH_SENDER, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("production-paused-at-startup", 	// NOI18N
			PRODUCTION_PAUSED_AT_STARTUP, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("insertion-paused-at-startup", 	// NOI18N
			INSERTION_PAUSED_AT_STARTUP, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("consumption-paused-at-startup", 	// NOI18N
			CONSUMPTION_PAUSED_AT_STARTUP, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("maximum-message-size", 	// NOI18N
			MAXIMUM_MESSAGE_SIZE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("quota", 	// NOI18N
			QUOTA, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("jndi-name", 	// NOI18N
			JNDI_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("local-jndi-name", 	// NOI18N
			LOCAL_JNDI_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("jms-create-destination-identifier", 	// NOI18N
			JMS_CREATE_DESTINATION_IDENTIFIER, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("default-unit-of-order", 	// NOI18N
			DEFAULT_UNIT_OF_ORDER, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("saf-export-policy", 	// NOI18N
			SAF_EXPORT_POLICY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("messaging-performance-preference", 	// NOI18N
			MESSAGING_PERFORMANCE_PREFERENCE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("unit-of-work-handling-policy", 	// NOI18N
			UNIT_OF_WORK_HANDLING_POLICY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("incomplete-work-expiration-time", 	// NOI18N
			INCOMPLETE_WORK_EXPIRATION_TIME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("load-balancing-policy", 	// NOI18N
			LOAD_BALANCING_POLICY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("unit-of-order-routing", 	// NOI18N
			UNIT_OF_ORDER_ROUTING, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
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
	public void setTemplate(java.lang.String value) {
		this.setValue(TEMPLATE, value);
	}

	//
	public java.lang.String getTemplate() {
		return (java.lang.String)this.getValue(TEMPLATE);
	}

	// This attribute is an array, possibly empty
	public void setDestinationKey(int index, java.lang.String value) {
		this.setValue(DESTINATION_KEY, index, value);
	}

	//
	public java.lang.String getDestinationKey(int index) {
		return (java.lang.String)this.getValue(DESTINATION_KEY, index);
	}

	// Return the number of properties
	public int sizeDestinationKey() {
		return this.size(DESTINATION_KEY);
	}

	// This attribute is an array, possibly empty
	public void setDestinationKey(java.lang.String[] value) {
		this.setValue(DESTINATION_KEY, value);
	}

	//
	public java.lang.String[] getDestinationKey() {
		return (java.lang.String[])this.getValues(DESTINATION_KEY);
	}

	// Add a new element returning its index in the list
	public int addDestinationKey(java.lang.String value) {
		int positionOfNewItem = this.addValue(DESTINATION_KEY, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeDestinationKey(java.lang.String value) {
		return this.removeValue(DESTINATION_KEY, value);
	}

	// This attribute is optional
	public void setThresholds(ThresholdParamsType value) {
		this.setValue(THRESHOLDS, value);
	}

	//
	public ThresholdParamsType getThresholds() {
		return (ThresholdParamsType)this.getValue(THRESHOLDS);
	}

	// This attribute is optional
	public void setDeliveryParamsOverrides(DeliveryParamsOverridesType value) {
		this.setValue(DELIVERY_PARAMS_OVERRIDES, value);
	}

	//
	public DeliveryParamsOverridesType getDeliveryParamsOverrides() {
		return (DeliveryParamsOverridesType)this.getValue(DELIVERY_PARAMS_OVERRIDES);
	}

	// This attribute is optional
	public void setDeliveryFailureParams(DeliveryFailureParamsType value) {
		this.setValue(DELIVERY_FAILURE_PARAMS, value);
	}

	//
	public DeliveryFailureParamsType getDeliveryFailureParams() {
		return (DeliveryFailureParamsType)this.getValue(DELIVERY_FAILURE_PARAMS);
	}

	// This attribute is optional
	public void setMessageLoggingParams(MessageLoggingParamsType value) {
		this.setValue(MESSAGE_LOGGING_PARAMS, value);
	}

	//
	public MessageLoggingParamsType getMessageLoggingParams() {
		return (MessageLoggingParamsType)this.getValue(MESSAGE_LOGGING_PARAMS);
	}

	// This attribute is optional
	public void setAttachSender(java.lang.String value) {
		this.setValue(ATTACH_SENDER, value);
	}

	//
	public java.lang.String getAttachSender() {
		return (java.lang.String)this.getValue(ATTACH_SENDER);
	}

	// This attribute is optional
	public void setProductionPausedAtStartup(boolean value) {
		this.setValue(PRODUCTION_PAUSED_AT_STARTUP, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isProductionPausedAtStartup() {
		Boolean ret = (Boolean)this.getValue(PRODUCTION_PAUSED_AT_STARTUP);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setInsertionPausedAtStartup(boolean value) {
		this.setValue(INSERTION_PAUSED_AT_STARTUP, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isInsertionPausedAtStartup() {
		Boolean ret = (Boolean)this.getValue(INSERTION_PAUSED_AT_STARTUP);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setConsumptionPausedAtStartup(boolean value) {
		this.setValue(CONSUMPTION_PAUSED_AT_STARTUP, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isConsumptionPausedAtStartup() {
		Boolean ret = (Boolean)this.getValue(CONSUMPTION_PAUSED_AT_STARTUP);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setMaximumMessageSize(int value) {
		this.setValue(MAXIMUM_MESSAGE_SIZE, java.lang.Integer.valueOf(value));
	}

	//
	public int getMaximumMessageSize() {
		Integer ret = (Integer)this.getValue(MAXIMUM_MESSAGE_SIZE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MAXIMUM_MESSAGE_SIZE", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setQuota(java.lang.String value) {
		this.setValue(QUOTA, value);
	}

	//
	public java.lang.String getQuota() {
		return (java.lang.String)this.getValue(QUOTA);
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
	public void setJmsCreateDestinationIdentifier(java.lang.String value) {
		this.setValue(JMS_CREATE_DESTINATION_IDENTIFIER, value);
	}

	//
	public java.lang.String getJmsCreateDestinationIdentifier() {
		return (java.lang.String)this.getValue(JMS_CREATE_DESTINATION_IDENTIFIER);
	}

	// This attribute is optional
	public void setDefaultUnitOfOrder(boolean value) {
		this.setValue(DEFAULT_UNIT_OF_ORDER, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isDefaultUnitOfOrder() {
		Boolean ret = (Boolean)this.getValue(DEFAULT_UNIT_OF_ORDER);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setSafExportPolicy(java.lang.String value) {
		this.setValue(SAF_EXPORT_POLICY, value);
	}

	//
	public java.lang.String getSafExportPolicy() {
		return (java.lang.String)this.getValue(SAF_EXPORT_POLICY);
	}

	// This attribute is optional
	public void setMessagingPerformancePreference(int value) {
		this.setValue(MESSAGING_PERFORMANCE_PREFERENCE, java.lang.Integer.valueOf(value));
	}

	//
	public int getMessagingPerformancePreference() {
		Integer ret = (Integer)this.getValue(MESSAGING_PERFORMANCE_PREFERENCE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MESSAGING_PERFORMANCE_PREFERENCE", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setUnitOfWorkHandlingPolicy(java.lang.String value) {
		this.setValue(UNIT_OF_WORK_HANDLING_POLICY, value);
	}

	//
	public java.lang.String getUnitOfWorkHandlingPolicy() {
		return (java.lang.String)this.getValue(UNIT_OF_WORK_HANDLING_POLICY);
	}

	// This attribute is optional
	public void setIncompleteWorkExpirationTime(int value) {
		this.setValue(INCOMPLETE_WORK_EXPIRATION_TIME, java.lang.Integer.valueOf(value));
	}

	//
	public int getIncompleteWorkExpirationTime() {
		Integer ret = (Integer)this.getValue(INCOMPLETE_WORK_EXPIRATION_TIME);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"INCOMPLETE_WORK_EXPIRATION_TIME", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setLoadBalancingPolicy(java.lang.String value) {
		this.setValue(LOAD_BALANCING_POLICY, value);
	}

	//
	public java.lang.String getLoadBalancingPolicy() {
		return (java.lang.String)this.getValue(LOAD_BALANCING_POLICY);
	}

	// This attribute is optional
	public void setUnitOfOrderRouting(java.lang.String value) {
		this.setValue(UNIT_OF_ORDER_ROUTING, value);
	}

	//
	public java.lang.String getUnitOfOrderRouting() {
		return (java.lang.String)this.getValue(UNIT_OF_ORDER_ROUTING);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ThresholdParamsType newThresholdParamsType() {
		return new ThresholdParamsType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public DeliveryParamsOverridesType newDeliveryParamsOverridesType() {
		return new DeliveryParamsOverridesType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public DeliveryFailureParamsType newDeliveryFailureParamsType() {
		return new DeliveryFailureParamsType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public MessageLoggingParamsType newMessageLoggingParamsType() {
		return new MessageLoggingParamsType();
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
		// Validating property subDeploymentName
		// Validating property defaultTargetingEnabled
		// Validating property template
		// Validating property destinationKey
		// Validating property thresholds
		if (getThresholds() != null) {
			getThresholds().validate();
		}
		// Validating property deliveryParamsOverrides
		if (getDeliveryParamsOverrides() != null) {
			getDeliveryParamsOverrides().validate();
		}
		// Validating property deliveryFailureParams
		if (getDeliveryFailureParams() != null) {
			getDeliveryFailureParams().validate();
		}
		// Validating property messageLoggingParams
		if (getMessageLoggingParams() != null) {
			getMessageLoggingParams().validate();
		}
		// Validating property attachSender
		if (getAttachSender() != null) {
			final java.lang.String[] enumRestrictionAttachSender = {"supports", "always", "never"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionAttachSender.length; ++_index2) {
				if (enumRestrictionAttachSender[_index2].equals(getAttachSender())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getAttachSender() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "attachSender", this);	// NOI18N
			}
		}
		// Validating property productionPausedAtStartup
		// Validating property insertionPausedAtStartup
		// Validating property consumptionPausedAtStartup
		// Validating property maximumMessageSize
		// Validating property quota
		// Validating property jndiName
		// Validating property localJndiName
		// Validating property jmsCreateDestinationIdentifier
		// Validating property defaultUnitOfOrder
		// Validating property safExportPolicy
		if (getSafExportPolicy() != null) {
			final java.lang.String[] enumRestrictionSafExportPolicy = {"All", "None"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionSafExportPolicy.length; ++_index2) {
				if (enumRestrictionSafExportPolicy[_index2].equals(getSafExportPolicy())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getSafExportPolicy() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "safExportPolicy", this);	// NOI18N
			}
		}
		// Validating property messagingPerformancePreference
		// Validating property unitOfWorkHandlingPolicy
		if (getUnitOfWorkHandlingPolicy() != null) {
			final java.lang.String[] enumRestrictionUnitOfWorkHandlingPolicy = {"PassThrough", "SingleMessageDelivery"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionUnitOfWorkHandlingPolicy.length; 
				++_index2) {
				if (enumRestrictionUnitOfWorkHandlingPolicy[_index2].equals(getUnitOfWorkHandlingPolicy())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getUnitOfWorkHandlingPolicy() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "unitOfWorkHandlingPolicy", this);	// NOI18N
			}
		}
		// Validating property incompleteWorkExpirationTime
		// Validating property loadBalancingPolicy
		// Validating property unitOfOrderRouting
		if (getUnitOfOrderRouting() != null) {
			final java.lang.String[] enumRestrictionUnitOfOrderRouting = {"Hash", "PathService"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionUnitOfOrderRouting.length; ++_index2) {
				if (enumRestrictionUnitOfOrderRouting[_index2].equals(getUnitOfOrderRouting())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getUnitOfOrderRouting() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "unitOfOrderRouting", this);	// NOI18N
			}
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
		str.append("Template");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getTemplate();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(TEMPLATE, 0, str, indent);

		str.append(indent);
		str.append("DestinationKey["+this.sizeDestinationKey()+"]");	// NOI18N
		for(int i=0; i<this.sizeDestinationKey(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getDestinationKey(i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(DESTINATION_KEY, i, str, indent);
		}

		str.append(indent);
		str.append("Thresholds");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getThresholds();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(THRESHOLDS, 0, str, indent);

		str.append(indent);
		str.append("DeliveryParamsOverrides");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getDeliveryParamsOverrides();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(DELIVERY_PARAMS_OVERRIDES, 0, str, indent);

		str.append(indent);
		str.append("DeliveryFailureParams");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getDeliveryFailureParams();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(DELIVERY_FAILURE_PARAMS, 0, str, indent);

		str.append(indent);
		str.append("MessageLoggingParams");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getMessageLoggingParams();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(MESSAGE_LOGGING_PARAMS, 0, str, indent);

		str.append(indent);
		str.append("AttachSender");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getAttachSender();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ATTACH_SENDER, 0, str, indent);

		str.append(indent);
		str.append("ProductionPausedAtStartup");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isProductionPausedAtStartup()?"true":"false"));
		this.dumpAttributes(PRODUCTION_PAUSED_AT_STARTUP, 0, str, indent);

		str.append(indent);
		str.append("InsertionPausedAtStartup");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isInsertionPausedAtStartup()?"true":"false"));
		this.dumpAttributes(INSERTION_PAUSED_AT_STARTUP, 0, str, indent);

		str.append(indent);
		str.append("ConsumptionPausedAtStartup");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isConsumptionPausedAtStartup()?"true":"false"));
		this.dumpAttributes(CONSUMPTION_PAUSED_AT_STARTUP, 0, str, indent);

		if (this.getValue(MAXIMUM_MESSAGE_SIZE) != null) {
			str.append(indent);
			str.append("MaximumMessageSize");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getMaximumMessageSize());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(MAXIMUM_MESSAGE_SIZE, 0, str, indent);
		}

		str.append(indent);
		str.append("Quota");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getQuota();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(QUOTA, 0, str, indent);

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
		str.append("JmsCreateDestinationIdentifier");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getJmsCreateDestinationIdentifier();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(JMS_CREATE_DESTINATION_IDENTIFIER, 0, str, indent);

		str.append(indent);
		str.append("DefaultUnitOfOrder");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isDefaultUnitOfOrder()?"true":"false"));
		this.dumpAttributes(DEFAULT_UNIT_OF_ORDER, 0, str, indent);

		str.append(indent);
		str.append("SafExportPolicy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getSafExportPolicy();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SAF_EXPORT_POLICY, 0, str, indent);

		if (this.getValue(MESSAGING_PERFORMANCE_PREFERENCE) != null) {
			str.append(indent);
			str.append("MessagingPerformancePreference");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getMessagingPerformancePreference());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(MESSAGING_PERFORMANCE_PREFERENCE, 0, str, indent);
		}

		str.append(indent);
		str.append("UnitOfWorkHandlingPolicy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getUnitOfWorkHandlingPolicy();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(UNIT_OF_WORK_HANDLING_POLICY, 0, str, indent);

		if (this.getValue(INCOMPLETE_WORK_EXPIRATION_TIME) != null) {
			str.append(indent);
			str.append("IncompleteWorkExpirationTime");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getIncompleteWorkExpirationTime());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(INCOMPLETE_WORK_EXPIRATION_TIME, 0, str, indent);
		}

		str.append(indent);
		str.append("LoadBalancingPolicy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getLoadBalancingPolicy();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(LOAD_BALANCING_POLICY, 0, str, indent);

		str.append(indent);
		str.append("UnitOfOrderRouting");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getUnitOfOrderRouting();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(UNIT_OF_ORDER_ROUTING, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("UniformDistributedDestinationType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

