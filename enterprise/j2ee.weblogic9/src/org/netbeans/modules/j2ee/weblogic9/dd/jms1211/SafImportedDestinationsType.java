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
 *	This generated bean class SafImportedDestinationsType matches the schema element 'saf-imported-destinations-type'.
 *  The root bean class is WeblogicJms
 *
 *	Generated on Tue Jul 25 03:27:00 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.jms1211;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class SafImportedDestinationsType extends org.netbeans.modules.j2ee.weblogic9.dd.jms1211.TargetableType
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String NAME = "Name";	// NOI18N
	static public final String NOTES = "Notes";	// NOI18N
	static public final String ID = "Id";	// NOI18N
	static public final String SUB_DEPLOYMENT_NAME = "SubDeploymentName";	// NOI18N
	static public final String DEFAULT_TARGETING_ENABLED = "DefaultTargetingEnabled";	// NOI18N
	static public final String SAF_QUEUE = "SafQueue";	// NOI18N
	static public final String SAF_TOPIC = "SafTopic";	// NOI18N
	static public final String JNDI_PREFIX = "JndiPrefix";	// NOI18N
	static public final String SAF_REMOTE_CONTEXT = "SafRemoteContext";	// NOI18N
	static public final String SAF_ERROR_HANDLING = "SafErrorHandling";	// NOI18N
	static public final String TIME_TO_LIVE_DEFAULT = "TimeToLiveDefault";	// NOI18N
	static public final String USE_SAF_TIME_TO_LIVE_DEFAULT = "UseSafTimeToLiveDefault";	// NOI18N
	static public final String UNIT_OF_ORDER_ROUTING = "UnitOfOrderRouting";	// NOI18N
	static public final String MESSAGE_LOGGING_PARAMS = "MessageLoggingParams";	// NOI18N

	public SafImportedDestinationsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public SafImportedDestinationsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(13);
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
		this.createProperty("saf-queue", 	// NOI18N
			SAF_QUEUE, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SafQueueType.class);
		this.createAttribute(SAF_QUEUE, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("saf-topic", 	// NOI18N
			SAF_TOPIC, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SafTopicType.class);
		this.createAttribute(SAF_TOPIC, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("jndi-prefix", 	// NOI18N
			JNDI_PREFIX, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("saf-remote-context", 	// NOI18N
			SAF_REMOTE_CONTEXT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("saf-error-handling", 	// NOI18N
			SAF_ERROR_HANDLING, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("time-to-live-default", 	// NOI18N
			TIME_TO_LIVE_DEFAULT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createProperty("use-saf-time-to-live-default", 	// NOI18N
			USE_SAF_TIME_TO_LIVE_DEFAULT, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("unit-of-order-routing", 	// NOI18N
			UNIT_OF_ORDER_ROUTING, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("message-logging-params", 	// NOI18N
			MESSAGE_LOGGING_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MessageLoggingParamsType.class);
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

	// This attribute is an array, possibly empty
	public void setSafQueue(int index, SafQueueType value) {
		this.setValue(SAF_QUEUE, index, value);
	}

	//
	public SafQueueType getSafQueue(int index) {
		return (SafQueueType)this.getValue(SAF_QUEUE, index);
	}

	// Return the number of properties
	public int sizeSafQueue() {
		return this.size(SAF_QUEUE);
	}

	// This attribute is an array, possibly empty
	public void setSafQueue(SafQueueType[] value) {
		this.setValue(SAF_QUEUE, value);
	}

	//
	public SafQueueType[] getSafQueue() {
		return (SafQueueType[])this.getValues(SAF_QUEUE);
	}

	// Add a new element returning its index in the list
	public int addSafQueue(org.netbeans.modules.j2ee.weblogic9.dd.jms1211.SafQueueType value) {
		int positionOfNewItem = this.addValue(SAF_QUEUE, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSafQueue(org.netbeans.modules.j2ee.weblogic9.dd.jms1211.SafQueueType value) {
		return this.removeValue(SAF_QUEUE, value);
	}

	// This attribute is an array, possibly empty
	public void setSafTopic(int index, SafTopicType value) {
		this.setValue(SAF_TOPIC, index, value);
	}

	//
	public SafTopicType getSafTopic(int index) {
		return (SafTopicType)this.getValue(SAF_TOPIC, index);
	}

	// Return the number of properties
	public int sizeSafTopic() {
		return this.size(SAF_TOPIC);
	}

	// This attribute is an array, possibly empty
	public void setSafTopic(SafTopicType[] value) {
		this.setValue(SAF_TOPIC, value);
	}

	//
	public SafTopicType[] getSafTopic() {
		return (SafTopicType[])this.getValues(SAF_TOPIC);
	}

	// Add a new element returning its index in the list
	public int addSafTopic(org.netbeans.modules.j2ee.weblogic9.dd.jms1211.SafTopicType value) {
		int positionOfNewItem = this.addValue(SAF_TOPIC, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSafTopic(org.netbeans.modules.j2ee.weblogic9.dd.jms1211.SafTopicType value) {
		return this.removeValue(SAF_TOPIC, value);
	}

	// This attribute is optional
	public void setJndiPrefix(java.lang.String value) {
		this.setValue(JNDI_PREFIX, value);
	}

	//
	public java.lang.String getJndiPrefix() {
		return (java.lang.String)this.getValue(JNDI_PREFIX);
	}

	// This attribute is optional
	public void setSafRemoteContext(java.lang.String value) {
		this.setValue(SAF_REMOTE_CONTEXT, value);
	}

	//
	public java.lang.String getSafRemoteContext() {
		return (java.lang.String)this.getValue(SAF_REMOTE_CONTEXT);
	}

	// This attribute is optional
	public void setSafErrorHandling(java.lang.String value) {
		this.setValue(SAF_ERROR_HANDLING, value);
	}

	//
	public java.lang.String getSafErrorHandling() {
		return (java.lang.String)this.getValue(SAF_ERROR_HANDLING);
	}

	// This attribute is optional
	public void setTimeToLiveDefault(long value) {
		this.setValue(TIME_TO_LIVE_DEFAULT, java.lang.Long.valueOf(value));
	}

	//
	public long getTimeToLiveDefault() {
		Long ret = (Long)this.getValue(TIME_TO_LIVE_DEFAULT);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"TIME_TO_LIVE_DEFAULT", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setUseSafTimeToLiveDefault(boolean value) {
		this.setValue(USE_SAF_TIME_TO_LIVE_DEFAULT, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isUseSafTimeToLiveDefault() {
		Boolean ret = (Boolean)this.getValue(USE_SAF_TIME_TO_LIVE_DEFAULT);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setUnitOfOrderRouting(java.lang.String value) {
		this.setValue(UNIT_OF_ORDER_ROUTING, value);
	}

	//
	public java.lang.String getUnitOfOrderRouting() {
		return (java.lang.String)this.getValue(UNIT_OF_ORDER_ROUTING);
	}

	// This attribute is optional
	public void setMessageLoggingParams(MessageLoggingParamsType value) {
		this.setValue(MESSAGE_LOGGING_PARAMS, value);
	}

	//
	public MessageLoggingParamsType getMessageLoggingParams() {
		return (MessageLoggingParamsType)this.getValue(MESSAGE_LOGGING_PARAMS);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SafQueueType newSafQueueType() {
		return new SafQueueType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SafTopicType newSafTopicType() {
		return new SafTopicType();
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
		// Validating property id
		// Validating property subDeploymentName
		// Validating property defaultTargetingEnabled
		// Validating property safQueue
		for (int _index = 0; _index < sizeSafQueue(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1211.SafQueueType element = getSafQueue(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property safTopic
		for (int _index = 0; _index < sizeSafTopic(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1211.SafTopicType element = getSafTopic(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property jndiPrefix
		// Validating property safRemoteContext
		// Validating property safErrorHandling
		// Validating property timeToLiveDefault
		// Validating property useSafTimeToLiveDefault
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
		// Validating property messageLoggingParams
		if (getMessageLoggingParams() != null) {
			getMessageLoggingParams().validate();
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
		str.append("SafQueue["+this.sizeSafQueue()+"]");	// NOI18N
		for(int i=0; i<this.sizeSafQueue(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getSafQueue(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SAF_QUEUE, i, str, indent);
		}

		str.append(indent);
		str.append("SafTopic["+this.sizeSafTopic()+"]");	// NOI18N
		for(int i=0; i<this.sizeSafTopic(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getSafTopic(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SAF_TOPIC, i, str, indent);
		}

		str.append(indent);
		str.append("JndiPrefix");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getJndiPrefix();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(JNDI_PREFIX, 0, str, indent);

		str.append(indent);
		str.append("SafRemoteContext");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getSafRemoteContext();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SAF_REMOTE_CONTEXT, 0, str, indent);

		str.append(indent);
		str.append("SafErrorHandling");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getSafErrorHandling();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SAF_ERROR_HANDLING, 0, str, indent);

		if (this.getValue(TIME_TO_LIVE_DEFAULT) != null) {
			str.append(indent);
			str.append("TimeToLiveDefault");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getTimeToLiveDefault());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(TIME_TO_LIVE_DEFAULT, 0, str, indent);
		}

		str.append(indent);
		str.append("UseSafTimeToLiveDefault");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isUseSafTimeToLiveDefault()?"true":"false"));
		this.dumpAttributes(USE_SAF_TIME_TO_LIVE_DEFAULT, 0, str, indent);

		str.append(indent);
		str.append("UnitOfOrderRouting");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getUnitOfOrderRouting();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(UNIT_OF_ORDER_ROUTING, 0, str, indent);

		str.append(indent);
		str.append("MessageLoggingParams");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getMessageLoggingParams();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(MESSAGE_LOGGING_PARAMS, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("SafImportedDestinationsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

