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
 *	This generated bean class ClientParamsType matches the schema element 'client-params-type'.
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

public class ClientParamsType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String CLIENT_ID = "ClientId";	// NOI18N
	static public final String CLIENT_ID_POLICY = "ClientIdPolicy";	// NOI18N
	static public final String SUBSCRIPTION_SHARING_POLICY = "SubscriptionSharingPolicy";	// NOI18N
	static public final String ACKNOWLEDGE_POLICY = "AcknowledgePolicy";	// NOI18N
	static public final String ALLOW_CLOSE_IN_ONMESSAGE = "AllowCloseInOnMessage";	// NOI18N
	static public final String MESSAGES_MAXIMUM = "MessagesMaximum";	// NOI18N
	static public final String MULTICAST_OVERRUN_POLICY = "MulticastOverrunPolicy";	// NOI18N
	static public final String SYNCHRONOUS_PREFETCH_MODE = "SynchronousPrefetchMode";	// NOI18N
	static public final String RECONNECT_POLICY = "ReconnectPolicy";	// NOI18N
	static public final String RECONNECT_BLOCKING_MILLIS = "ReconnectBlockingMillis";	// NOI18N
	static public final String TOTAL_RECONNECT_PERIOD_MILLIS = "TotalReconnectPeriodMillis";	// NOI18N

	public ClientParamsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ClientParamsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(11);
		this.createProperty("client-id", 	// NOI18N
			CLIENT_ID, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("client-id-policy", 	// NOI18N
			CLIENT_ID_POLICY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("subscription-sharing-policy", 	// NOI18N
			SUBSCRIPTION_SHARING_POLICY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("acknowledge-policy", 	// NOI18N
			ACKNOWLEDGE_POLICY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("allow-close-in-onMessage", 	// NOI18N
			ALLOW_CLOSE_IN_ONMESSAGE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("messages-maximum", 	// NOI18N
			MESSAGES_MAXIMUM, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("multicast-overrun-policy", 	// NOI18N
			MULTICAST_OVERRUN_POLICY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("synchronous-prefetch-mode", 	// NOI18N
			SYNCHRONOUS_PREFETCH_MODE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("reconnect-policy", 	// NOI18N
			RECONNECT_POLICY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("reconnect-blocking-millis", 	// NOI18N
			RECONNECT_BLOCKING_MILLIS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createProperty("total-reconnect-period-millis", 	// NOI18N
			TOTAL_RECONNECT_PERIOD_MILLIS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setClientId(java.lang.String value) {
		this.setValue(CLIENT_ID, value);
	}

	//
	public java.lang.String getClientId() {
		return (java.lang.String)this.getValue(CLIENT_ID);
	}

	// This attribute is optional
	public void setClientIdPolicy(java.lang.String value) {
		this.setValue(CLIENT_ID_POLICY, value);
	}

	//
	public java.lang.String getClientIdPolicy() {
		return (java.lang.String)this.getValue(CLIENT_ID_POLICY);
	}

	// This attribute is optional
	public void setSubscriptionSharingPolicy(java.lang.String value) {
		this.setValue(SUBSCRIPTION_SHARING_POLICY, value);
	}

	//
	public java.lang.String getSubscriptionSharingPolicy() {
		return (java.lang.String)this.getValue(SUBSCRIPTION_SHARING_POLICY);
	}

	// This attribute is optional
	public void setAcknowledgePolicy(java.lang.String value) {
		this.setValue(ACKNOWLEDGE_POLICY, value);
	}

	//
	public java.lang.String getAcknowledgePolicy() {
		return (java.lang.String)this.getValue(ACKNOWLEDGE_POLICY);
	}

	// This attribute is optional
	public void setAllowCloseInOnMessage(boolean value) {
		this.setValue(ALLOW_CLOSE_IN_ONMESSAGE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isAllowCloseInOnMessage() {
		Boolean ret = (Boolean)this.getValue(ALLOW_CLOSE_IN_ONMESSAGE);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setMessagesMaximum(int value) {
		this.setValue(MESSAGES_MAXIMUM, java.lang.Integer.valueOf(value));
	}

	//
	public int getMessagesMaximum() {
		Integer ret = (Integer)this.getValue(MESSAGES_MAXIMUM);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MESSAGES_MAXIMUM", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setMulticastOverrunPolicy(java.lang.String value) {
		this.setValue(MULTICAST_OVERRUN_POLICY, value);
	}

	//
	public java.lang.String getMulticastOverrunPolicy() {
		return (java.lang.String)this.getValue(MULTICAST_OVERRUN_POLICY);
	}

	// This attribute is optional
	public void setSynchronousPrefetchMode(java.lang.String value) {
		this.setValue(SYNCHRONOUS_PREFETCH_MODE, value);
	}

	//
	public java.lang.String getSynchronousPrefetchMode() {
		return (java.lang.String)this.getValue(SYNCHRONOUS_PREFETCH_MODE);
	}

	// This attribute is optional
	public void setReconnectPolicy(java.lang.String value) {
		this.setValue(RECONNECT_POLICY, value);
	}

	//
	public java.lang.String getReconnectPolicy() {
		return (java.lang.String)this.getValue(RECONNECT_POLICY);
	}

	// This attribute is optional
	public void setReconnectBlockingMillis(long value) {
		this.setValue(RECONNECT_BLOCKING_MILLIS, java.lang.Long.valueOf(value));
	}

	//
	public long getReconnectBlockingMillis() {
		Long ret = (Long)this.getValue(RECONNECT_BLOCKING_MILLIS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"RECONNECT_BLOCKING_MILLIS", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setTotalReconnectPeriodMillis(long value) {
		this.setValue(TOTAL_RECONNECT_PERIOD_MILLIS, java.lang.Long.valueOf(value));
	}

	//
	public long getTotalReconnectPeriodMillis() {
		Long ret = (Long)this.getValue(TOTAL_RECONNECT_PERIOD_MILLIS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"TOTAL_RECONNECT_PERIOD_MILLIS", "long"}));
		return ((java.lang.Long)ret).longValue();
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
		// Validating property clientId
		// Validating property clientIdPolicy
		if (getClientIdPolicy() != null) {
			final java.lang.String[] enumRestrictionClientIdPolicy = {"Restricted", "Unrestricted"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionClientIdPolicy.length; ++_index2) {
				if (enumRestrictionClientIdPolicy[_index2].equals(getClientIdPolicy())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getClientIdPolicy() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "clientIdPolicy", this);	// NOI18N
			}
		}
		// Validating property subscriptionSharingPolicy
		if (getSubscriptionSharingPolicy() != null) {
			final java.lang.String[] enumRestrictionSubscriptionSharingPolicy = {"Exclusive", "Sharable"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionSubscriptionSharingPolicy.length; 
				++_index2) {
				if (enumRestrictionSubscriptionSharingPolicy[_index2].equals(getSubscriptionSharingPolicy())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getSubscriptionSharingPolicy() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "subscriptionSharingPolicy", this);	// NOI18N
			}
		}
		// Validating property acknowledgePolicy
		if (getAcknowledgePolicy() != null) {
			final java.lang.String[] enumRestrictionAcknowledgePolicy = {"All", "Previous", "One"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionAcknowledgePolicy.length; ++_index2) {
				if (enumRestrictionAcknowledgePolicy[_index2].equals(getAcknowledgePolicy())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getAcknowledgePolicy() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "acknowledgePolicy", this);	// NOI18N
			}
		}
		// Validating property allowCloseInOnMessage
		// Validating property messagesMaximum
		// Validating property multicastOverrunPolicy
		if (getMulticastOverrunPolicy() != null) {
			final java.lang.String[] enumRestrictionMulticastOverrunPolicy = {"KeepOld", "KeepNew"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionMulticastOverrunPolicy.length; 
				++_index2) {
				if (enumRestrictionMulticastOverrunPolicy[_index2].equals(getMulticastOverrunPolicy())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getMulticastOverrunPolicy() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "multicastOverrunPolicy", this);	// NOI18N
			}
		}
		// Validating property synchronousPrefetchMode
		if (getSynchronousPrefetchMode() != null) {
			final java.lang.String[] enumRestrictionSynchronousPrefetchMode = {"enabled", "disabled", "topicSubscriberOnly"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionSynchronousPrefetchMode.length; 
				++_index2) {
				if (enumRestrictionSynchronousPrefetchMode[_index2].equals(getSynchronousPrefetchMode())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getSynchronousPrefetchMode() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "synchronousPrefetchMode", this);	// NOI18N
			}
		}
		// Validating property reconnectPolicy
		if (getReconnectPolicy() != null) {
			final java.lang.String[] enumRestrictionReconnectPolicy = {"none", "producer", "all"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionReconnectPolicy.length; ++_index2) {
				if (enumRestrictionReconnectPolicy[_index2].equals(getReconnectPolicy())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getReconnectPolicy() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "reconnectPolicy", this);	// NOI18N
			}
		}
		// Validating property reconnectBlockingMillis
		// Validating property totalReconnectPeriodMillis
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("ClientId");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getClientId();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CLIENT_ID, 0, str, indent);

		str.append(indent);
		str.append("ClientIdPolicy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getClientIdPolicy();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CLIENT_ID_POLICY, 0, str, indent);

		str.append(indent);
		str.append("SubscriptionSharingPolicy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getSubscriptionSharingPolicy();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SUBSCRIPTION_SHARING_POLICY, 0, str, indent);

		str.append(indent);
		str.append("AcknowledgePolicy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getAcknowledgePolicy();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ACKNOWLEDGE_POLICY, 0, str, indent);

		str.append(indent);
		str.append("AllowCloseInOnMessage");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isAllowCloseInOnMessage()?"true":"false"));
		this.dumpAttributes(ALLOW_CLOSE_IN_ONMESSAGE, 0, str, indent);

		if (this.getValue(MESSAGES_MAXIMUM) != null) {
			str.append(indent);
			str.append("MessagesMaximum");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getMessagesMaximum());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(MESSAGES_MAXIMUM, 0, str, indent);
		}

		str.append(indent);
		str.append("MulticastOverrunPolicy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getMulticastOverrunPolicy();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(MULTICAST_OVERRUN_POLICY, 0, str, indent);

		str.append(indent);
		str.append("SynchronousPrefetchMode");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getSynchronousPrefetchMode();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SYNCHRONOUS_PREFETCH_MODE, 0, str, indent);

		str.append(indent);
		str.append("ReconnectPolicy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getReconnectPolicy();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(RECONNECT_POLICY, 0, str, indent);

		if (this.getValue(RECONNECT_BLOCKING_MILLIS) != null) {
			str.append(indent);
			str.append("ReconnectBlockingMillis");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getReconnectBlockingMillis());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(RECONNECT_BLOCKING_MILLIS, 0, str, indent);
		}

		if (this.getValue(TOTAL_RECONNECT_PERIOD_MILLIS) != null) {
			str.append(indent);
			str.append("TotalReconnectPeriodMillis");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getTotalReconnectPeriodMillis());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(TOTAL_RECONNECT_PERIOD_MILLIS, 0, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ClientParamsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

