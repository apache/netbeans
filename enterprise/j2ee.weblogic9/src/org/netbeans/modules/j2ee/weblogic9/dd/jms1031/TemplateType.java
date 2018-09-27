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
 *	This generated bean class TemplateType matches the schema element 'template-type'.
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

public class TemplateType extends org.netbeans.modules.j2ee.weblogic9.dd.jms1031.NamedEntityType
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String NAME = "Name";	// NOI18N
	static public final String NOTES = "Notes";	// NOI18N
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
	static public final String DEFAULT_UNIT_OF_ORDER = "DefaultUnitOfOrder";	// NOI18N
	static public final String SAF_EXPORT_POLICY = "SafExportPolicy";	// NOI18N
	static public final String MULTICAST = "Multicast";	// NOI18N
	static public final String GROUP_PARAMS = "GroupParams";	// NOI18N
	static public final String MESSAGING_PERFORMANCE_PREFERENCE = "MessagingPerformancePreference";	// NOI18N
	static public final String UNIT_OF_WORK_HANDLING_POLICY = "UnitOfWorkHandlingPolicy";	// NOI18N
	static public final String INCOMPLETE_WORK_EXPIRATION_TIME = "IncompleteWorkExpirationTime";	// NOI18N

	public TemplateType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public TemplateType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(19);
		this.createProperty("notes", 	// NOI18N
			NOTES, 
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
		this.createProperty("default-unit-of-order", 	// NOI18N
			DEFAULT_UNIT_OF_ORDER, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("saf-export-policy", 	// NOI18N
			SAF_EXPORT_POLICY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("multicast", 	// NOI18N
			MULTICAST, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MulticastParamsType.class);
		this.createProperty("group-params", 	// NOI18N
			GROUP_PARAMS, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			GroupParamsType.class);
		this.createAttribute(GROUP_PARAMS, "sub-deployment-name", "SubDeploymentName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
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
	public void setMulticast(MulticastParamsType value) {
		this.setValue(MULTICAST, value);
	}

	//
	public MulticastParamsType getMulticast() {
		return (MulticastParamsType)this.getValue(MULTICAST);
	}

	// This attribute is an array, possibly empty
	public void setGroupParams(int index, GroupParamsType value) {
		this.setValue(GROUP_PARAMS, index, value);
	}

	//
	public GroupParamsType getGroupParams(int index) {
		return (GroupParamsType)this.getValue(GROUP_PARAMS, index);
	}

	// Return the number of properties
	public int sizeGroupParams() {
		return this.size(GROUP_PARAMS);
	}

	// This attribute is an array, possibly empty
	public void setGroupParams(GroupParamsType[] value) {
		this.setValue(GROUP_PARAMS, value);
	}

	//
	public GroupParamsType[] getGroupParams() {
		return (GroupParamsType[])this.getValues(GROUP_PARAMS);
	}

	// Add a new element returning its index in the list
	public int addGroupParams(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.GroupParamsType value) {
		int positionOfNewItem = this.addValue(GROUP_PARAMS, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeGroupParams(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.GroupParamsType value) {
		return this.removeValue(GROUP_PARAMS, value);
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

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public MulticastParamsType newMulticastParamsType() {
		return new MulticastParamsType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public GroupParamsType newGroupParamsType() {
		return new GroupParamsType();
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
		// Validating property multicast
		if (getMulticast() != null) {
			getMulticast().validate();
		}
		// Validating property groupParams
		for (int _index = 0; _index < sizeGroupParams(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1031.GroupParamsType element = getGroupParams(_index);
			if (element != null) {
				element.validate();
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

		str.append(indent);
		str.append("Multicast");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getMulticast();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(MULTICAST, 0, str, indent);

		str.append(indent);
		str.append("GroupParams["+this.sizeGroupParams()+"]");	// NOI18N
		for(int i=0; i<this.sizeGroupParams(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getGroupParams(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(GROUP_PARAMS, i, str, indent);
		}

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

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("TemplateType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

