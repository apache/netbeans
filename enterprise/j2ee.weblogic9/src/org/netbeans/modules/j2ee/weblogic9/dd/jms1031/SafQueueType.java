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
 *	This generated bean class SafQueueType matches the schema element 'saf-queue-type'.
 *  The root bean class is WeblogicJms
 *
 *	Generated on Tue Jul 25 03:26:59 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.jms1031;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class SafQueueType extends org.netbeans.modules.j2ee.weblogic9.dd.jms1031.SafDestinationType
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String NAME = "Name";	// NOI18N
	static public final String NOTES = "Notes";	// NOI18N
	static public final String REMOTE_JNDI_NAME = "RemoteJndiName";	// NOI18N
	static public final String LOCAL_JNDI_NAME = "LocalJndiName";	// NOI18N
	static public final String NON_PERSISTENT_QOS = "NonPersistentQos";	// NOI18N
	static public final String SAF_ERROR_HANDLING = "SafErrorHandling";	// NOI18N
	static public final String TIME_TO_LIVE_DEFAULT = "TimeToLiveDefault";	// NOI18N
	static public final String USE_SAF_TIME_TO_LIVE_DEFAULT = "UseSafTimeToLiveDefault";	// NOI18N
	static public final String UNIT_OF_ORDER_ROUTING = "UnitOfOrderRouting";	// NOI18N
	static public final String MESSAGE_LOGGING_PARAMS = "MessageLoggingParams";	// NOI18N

	public SafQueueType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public SafQueueType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(9);
		this.createProperty("notes", 	// NOI18N
			NOTES, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("remote-jndi-name", 	// NOI18N
			REMOTE_JNDI_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("local-jndi-name", 	// NOI18N
			LOCAL_JNDI_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("non-persistent-qos", 	// NOI18N
			NON_PERSISTENT_QOS, 
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

	// This attribute is mandatory
	public void setRemoteJndiName(java.lang.String value) {
		this.setValue(REMOTE_JNDI_NAME, value);
	}

	//
	public java.lang.String getRemoteJndiName() {
		return (java.lang.String)this.getValue(REMOTE_JNDI_NAME);
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
	public void setNonPersistentQos(java.lang.String value) {
		this.setValue(NON_PERSISTENT_QOS, value);
	}

	//
	public java.lang.String getNonPersistentQos() {
		return (java.lang.String)this.getValue(NON_PERSISTENT_QOS);
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
		// Validating property remoteJndiName
		if (getRemoteJndiName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getRemoteJndiName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "remoteJndiName", this);	// NOI18N
		}
		// Validating property localJndiName
		// Validating property nonPersistentQos
		if (getNonPersistentQos() != null) {
			final java.lang.String[] enumRestrictionNonPersistentQos = {"At-Most-Once", "At-Least-Once", "Exactly-Once"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionNonPersistentQos.length; ++_index2) {
				if (enumRestrictionNonPersistentQos[_index2].equals(getNonPersistentQos())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getNonPersistentQos() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "nonPersistentQos", this);	// NOI18N
			}
		}
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

		str.append(indent);
		str.append("RemoteJndiName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getRemoteJndiName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(REMOTE_JNDI_NAME, 0, str, indent);

		str.append(indent);
		str.append("LocalJndiName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getLocalJndiName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(LOCAL_JNDI_NAME, 0, str, indent);

		str.append(indent);
		str.append("NonPersistentQos");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getNonPersistentQos();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NON_PERSISTENT_QOS, 0, str, indent);

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
		str.append("SafQueueType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

