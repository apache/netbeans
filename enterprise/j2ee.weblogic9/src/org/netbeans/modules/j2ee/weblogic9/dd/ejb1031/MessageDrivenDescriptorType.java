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
 *	This generated bean class MessageDrivenDescriptorType matches the schema element 'message-driven-descriptorType'.
 *  The root bean class is WeblogicEjbJar
 *
 *	Generated on Tue Jul 25 03:26:52 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ejb1031;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class MessageDrivenDescriptorType extends org.netbeans.modules.schema2beans.BaseBean
	 implements org.netbeans.modules.j2ee.weblogic9.dd.model.MessageDrivenDescriptorType
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String POOL = "Pool";	// NOI18N
	static public final String TIMER_DESCRIPTOR = "TimerDescriptor";	// NOI18N
	static public final String RESOURCE_ADAPTER_JNDI_NAME = "ResourceAdapterJndiName";	// NOI18N
	static public final String RESOURCEADAPTERJNDINAMEID = "ResourceAdapterJndiNameId";	// NOI18N
	static public final String DESTINATION_JNDI_NAME = "DestinationJndiName";	// NOI18N
	static public final String DESTINATIONJNDINAMEID = "DestinationJndiNameId";	// NOI18N
	static public final String INITIAL_CONTEXT_FACTORY = "InitialContextFactory";	// NOI18N
	static public final String INITIALCONTEXTFACTORYID = "InitialContextFactoryId";	// NOI18N
	static public final String PROVIDER_URL = "ProviderUrl";	// NOI18N
	static public final String PROVIDERURLID = "ProviderUrlId";	// NOI18N
	static public final String CONNECTION_FACTORY_JNDI_NAME = "ConnectionFactoryJndiName";	// NOI18N
	static public final String CONNECTIONFACTORYJNDINAMEID = "ConnectionFactoryJndiNameId";	// NOI18N
	static public final String DESTINATION_RESOURCE_LINK = "DestinationResourceLink";	// NOI18N
	static public final String DESTINATIONRESOURCELINKJ2EEID = "DestinationResourceLinkJ2eeId";	// NOI18N
	static public final String CONNECTION_FACTORY_RESOURCE_LINK = "ConnectionFactoryResourceLink";	// NOI18N
	static public final String CONNECTIONFACTORYRESOURCELINKJ2EEID = "ConnectionFactoryResourceLinkJ2eeId";	// NOI18N
	static public final String JMS_POLLING_INTERVAL_SECONDS = "JmsPollingIntervalSeconds";	// NOI18N
	static public final String JMSPOLLINGINTERVALSECONDSJ2EEID = "JmsPollingIntervalSecondsJ2eeId";	// NOI18N
	static public final String JMS_CLIENT_ID = "JmsClientId";	// NOI18N
	static public final String JMSCLIENTIDID = "JmsClientIdId";	// NOI18N
	static public final String GENERATE_UNIQUE_JMS_CLIENT_ID = "GenerateUniqueJmsClientId";	// NOI18N
	static public final String DURABLE_SUBSCRIPTION_DELETION = "DurableSubscriptionDeletion";	// NOI18N
	static public final String MAX_MESSAGES_IN_TRANSACTION = "MaxMessagesInTransaction";	// NOI18N
	static public final String MAXMESSAGESINTRANSACTIONJ2EEID = "MaxMessagesInTransactionJ2eeId";	// NOI18N
	static public final String DISTRIBUTED_DESTINATION_CONNECTION = "DistributedDestinationConnection";	// NOI18N
	static public final String DISTRIBUTEDDESTINATIONCONNECTIONID = "DistributedDestinationConnectionId";	// NOI18N
	static public final String USE81_STYLE_POLLING = "Use81StylePolling";	// NOI18N
	static public final String INIT_SUSPEND_SECONDS = "InitSuspendSeconds";	// NOI18N
	static public final String INITSUSPENDSECONDSJ2EEID = "InitSuspendSecondsJ2eeId";	// NOI18N
	static public final String MAX_SUSPEND_SECONDS = "MaxSuspendSeconds";	// NOI18N
	static public final String MAXSUSPENDSECONDSJ2EEID = "MaxSuspendSecondsJ2eeId";	// NOI18N
	static public final String SECURITY_PLUGIN = "SecurityPlugin";	// NOI18N

	public MessageDrivenDescriptorType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public MessageDrivenDescriptorType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(19);
		this.createProperty("pool", 	// NOI18N
			POOL, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PoolType.class);
		this.createAttribute(POOL, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("timer-descriptor", 	// NOI18N
			TIMER_DESCRIPTOR, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			TimerDescriptorType.class);
		this.createAttribute(TIMER_DESCRIPTOR, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("resource-adapter-jndi-name", 	// NOI18N
			RESOURCE_ADAPTER_JNDI_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(RESOURCE_ADAPTER_JNDI_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("destination-jndi-name", 	// NOI18N
			DESTINATION_JNDI_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(DESTINATION_JNDI_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("initial-context-factory", 	// NOI18N
			INITIAL_CONTEXT_FACTORY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(INITIAL_CONTEXT_FACTORY, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("provider-url", 	// NOI18N
			PROVIDER_URL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(PROVIDER_URL, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("connection-factory-jndi-name", 	// NOI18N
			CONNECTION_FACTORY_JNDI_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(CONNECTION_FACTORY_JNDI_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("destination-resource-link", 	// NOI18N
			DESTINATION_RESOURCE_LINK, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(DESTINATION_RESOURCE_LINK, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("connection-factory-resource-link", 	// NOI18N
			CONNECTION_FACTORY_RESOURCE_LINK, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(CONNECTION_FACTORY_RESOURCE_LINK, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("jms-polling-interval-seconds", 	// NOI18N
			JMS_POLLING_INTERVAL_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(JMS_POLLING_INTERVAL_SECONDS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("jms-client-id", 	// NOI18N
			JMS_CLIENT_ID, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(JMS_CLIENT_ID, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("generate-unique-jms-client-id", 	// NOI18N
			GENERATE_UNIQUE_JMS_CLIENT_ID, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("durable-subscription-deletion", 	// NOI18N
			DURABLE_SUBSCRIPTION_DELETION, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("max-messages-in-transaction", 	// NOI18N
			MAX_MESSAGES_IN_TRANSACTION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(MAX_MESSAGES_IN_TRANSACTION, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("distributed-destination-connection", 	// NOI18N
			DISTRIBUTED_DESTINATION_CONNECTION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(DISTRIBUTED_DESTINATION_CONNECTION, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("use81-style-polling", 	// NOI18N
			USE81_STYLE_POLLING, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("init-suspend-seconds", 	// NOI18N
			INIT_SUSPEND_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(INIT_SUSPEND_SECONDS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("max-suspend-seconds", 	// NOI18N
			MAX_SUSPEND_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(MAX_SUSPEND_SECONDS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("security-plugin", 	// NOI18N
			SECURITY_PLUGIN, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SecurityPluginType.class);
		this.createAttribute(SECURITY_PLUGIN, "id", "Id", 
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
	public void setPool(PoolType value) {
		this.setValue(POOL, value);
	}

	//
	public PoolType getPool() {
		return (PoolType)this.getValue(POOL);
	}

	// This attribute is optional
	public void setTimerDescriptor(TimerDescriptorType value) {
		this.setValue(TIMER_DESCRIPTOR, value);
	}

	//
	public TimerDescriptorType getTimerDescriptor() {
		return (TimerDescriptorType)this.getValue(TIMER_DESCRIPTOR);
	}

	// This attribute is optional
	public void setResourceAdapterJndiName(java.lang.String value) {
		this.setValue(RESOURCE_ADAPTER_JNDI_NAME, value);
	}

	//
	public java.lang.String getResourceAdapterJndiName() {
		return (java.lang.String)this.getValue(RESOURCE_ADAPTER_JNDI_NAME);
	}

	// This attribute is optional
	public void setResourceAdapterJndiNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(RESOURCE_ADAPTER_JNDI_NAME) == 0) {
			setValue(RESOURCE_ADAPTER_JNDI_NAME, "");
		}
		setAttributeValue(RESOURCE_ADAPTER_JNDI_NAME, "Id", value);
	}

	//
	public java.lang.String getResourceAdapterJndiNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(RESOURCE_ADAPTER_JNDI_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(RESOURCE_ADAPTER_JNDI_NAME, "Id");
		}
	}

	// This attribute is optional
	public void setDestinationJndiName(java.lang.String value) {
		this.setValue(DESTINATION_JNDI_NAME, value);
	}

	//
	public java.lang.String getDestinationJndiName() {
		return (java.lang.String)this.getValue(DESTINATION_JNDI_NAME);
	}

	// This attribute is optional
	public void setDestinationJndiNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(DESTINATION_JNDI_NAME) == 0) {
			setValue(DESTINATION_JNDI_NAME, "");
		}
		setAttributeValue(DESTINATION_JNDI_NAME, "Id", value);
	}

	//
	public java.lang.String getDestinationJndiNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(DESTINATION_JNDI_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(DESTINATION_JNDI_NAME, "Id");
		}
	}

	// This attribute is optional
	public void setInitialContextFactory(java.lang.String value) {
		this.setValue(INITIAL_CONTEXT_FACTORY, value);
	}

	//
	public java.lang.String getInitialContextFactory() {
		return (java.lang.String)this.getValue(INITIAL_CONTEXT_FACTORY);
	}

	// This attribute is optional
	public void setInitialContextFactoryId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(INITIAL_CONTEXT_FACTORY) == 0) {
			setValue(INITIAL_CONTEXT_FACTORY, "");
		}
		setAttributeValue(INITIAL_CONTEXT_FACTORY, "Id", value);
	}

	//
	public java.lang.String getInitialContextFactoryId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(INITIAL_CONTEXT_FACTORY) == 0) {
			return null;
		} else {
			return getAttributeValue(INITIAL_CONTEXT_FACTORY, "Id");
		}
	}

	// This attribute is optional
	public void setProviderUrl(java.lang.String value) {
		this.setValue(PROVIDER_URL, value);
	}

	//
	public java.lang.String getProviderUrl() {
		return (java.lang.String)this.getValue(PROVIDER_URL);
	}

	// This attribute is optional
	public void setProviderUrlId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(PROVIDER_URL) == 0) {
			setValue(PROVIDER_URL, "");
		}
		setAttributeValue(PROVIDER_URL, "Id", value);
	}

	//
	public java.lang.String getProviderUrlId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(PROVIDER_URL) == 0) {
			return null;
		} else {
			return getAttributeValue(PROVIDER_URL, "Id");
		}
	}

	// This attribute is optional
	public void setConnectionFactoryJndiName(java.lang.String value) {
		this.setValue(CONNECTION_FACTORY_JNDI_NAME, value);
	}

	//
	public java.lang.String getConnectionFactoryJndiName() {
		return (java.lang.String)this.getValue(CONNECTION_FACTORY_JNDI_NAME);
	}

	// This attribute is optional
	public void setConnectionFactoryJndiNameId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(CONNECTION_FACTORY_JNDI_NAME) == 0) {
			setValue(CONNECTION_FACTORY_JNDI_NAME, "");
		}
		setAttributeValue(CONNECTION_FACTORY_JNDI_NAME, "Id", value);
	}

	//
	public java.lang.String getConnectionFactoryJndiNameId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(CONNECTION_FACTORY_JNDI_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(CONNECTION_FACTORY_JNDI_NAME, "Id");
		}
	}

	// This attribute is optional
	public void setDestinationResourceLink(java.lang.String value) {
		this.setValue(DESTINATION_RESOURCE_LINK, value);
	}

	//
	public java.lang.String getDestinationResourceLink() {
		return (java.lang.String)this.getValue(DESTINATION_RESOURCE_LINK);
	}

	// This attribute is optional
	public void setDestinationResourceLinkJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(DESTINATION_RESOURCE_LINK) == 0) {
			setValue(DESTINATION_RESOURCE_LINK, "");
		}
		setAttributeValue(DESTINATION_RESOURCE_LINK, "J2eeId", value);
	}

	//
	public java.lang.String getDestinationResourceLinkJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(DESTINATION_RESOURCE_LINK) == 0) {
			return null;
		} else {
			return getAttributeValue(DESTINATION_RESOURCE_LINK, "J2eeId");
		}
	}

	// This attribute is optional
	public void setConnectionFactoryResourceLink(java.lang.String value) {
		this.setValue(CONNECTION_FACTORY_RESOURCE_LINK, value);
	}

	//
	public java.lang.String getConnectionFactoryResourceLink() {
		return (java.lang.String)this.getValue(CONNECTION_FACTORY_RESOURCE_LINK);
	}

	// This attribute is optional
	public void setConnectionFactoryResourceLinkJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(DESTINATION_RESOURCE_LINK) == 0) {
			setValue(DESTINATION_RESOURCE_LINK, "");
		}
		setAttributeValue(DESTINATION_RESOURCE_LINK, "J2eeId", value);
	}

	//
	public java.lang.String getConnectionFactoryResourceLinkJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(DESTINATION_RESOURCE_LINK) == 0) {
			return null;
		} else {
			return getAttributeValue(DESTINATION_RESOURCE_LINK, "J2eeId");
		}
	}

	// This attribute is optional
	public void setJmsPollingIntervalSeconds(long value) {
		this.setValue(JMS_POLLING_INTERVAL_SECONDS, java.lang.Long.valueOf(value));
	}

	//
	public long getJmsPollingIntervalSeconds() {
		Long ret = (Long)this.getValue(JMS_POLLING_INTERVAL_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"JMS_POLLING_INTERVAL_SECONDS", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setJmsPollingIntervalSecondsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(JMS_POLLING_INTERVAL_SECONDS) == 0) {
			setValue(JMS_POLLING_INTERVAL_SECONDS, "");
		}
		setAttributeValue(JMS_POLLING_INTERVAL_SECONDS, "J2eeId", value);
	}

	//
	public java.lang.String getJmsPollingIntervalSecondsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(JMS_POLLING_INTERVAL_SECONDS) == 0) {
			return null;
		} else {
			return getAttributeValue(JMS_POLLING_INTERVAL_SECONDS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setJmsClientId(java.lang.String value) {
		this.setValue(JMS_CLIENT_ID, value);
	}

	//
	public java.lang.String getJmsClientId() {
		return (java.lang.String)this.getValue(JMS_CLIENT_ID);
	}

	// This attribute is optional
	public void setJmsClientIdId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(JMS_CLIENT_ID) == 0) {
			setValue(JMS_CLIENT_ID, "");
		}
		setAttributeValue(JMS_CLIENT_ID, "Id", value);
	}

	//
	public java.lang.String getJmsClientIdId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(JMS_CLIENT_ID) == 0) {
			return null;
		} else {
			return getAttributeValue(JMS_CLIENT_ID, "Id");
		}
	}

	// This attribute is optional
	public void setGenerateUniqueJmsClientId(boolean value) {
		this.setValue(GENERATE_UNIQUE_JMS_CLIENT_ID, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isGenerateUniqueJmsClientId() {
		Boolean ret = (Boolean)this.getValue(GENERATE_UNIQUE_JMS_CLIENT_ID);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setDurableSubscriptionDeletion(boolean value) {
		this.setValue(DURABLE_SUBSCRIPTION_DELETION, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isDurableSubscriptionDeletion() {
		Boolean ret = (Boolean)this.getValue(DURABLE_SUBSCRIPTION_DELETION);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setMaxMessagesInTransaction(long value) {
		this.setValue(MAX_MESSAGES_IN_TRANSACTION, java.lang.Long.valueOf(value));
	}

	//
	public long getMaxMessagesInTransaction() {
		Long ret = (Long)this.getValue(MAX_MESSAGES_IN_TRANSACTION);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MAX_MESSAGES_IN_TRANSACTION", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setMaxMessagesInTransactionJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(JMS_POLLING_INTERVAL_SECONDS) == 0) {
			setValue(JMS_POLLING_INTERVAL_SECONDS, "");
		}
		setAttributeValue(JMS_POLLING_INTERVAL_SECONDS, "J2eeId", value);
	}

	//
	public java.lang.String getMaxMessagesInTransactionJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(JMS_POLLING_INTERVAL_SECONDS) == 0) {
			return null;
		} else {
			return getAttributeValue(JMS_POLLING_INTERVAL_SECONDS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setDistributedDestinationConnection(java.lang.String value) {
		this.setValue(DISTRIBUTED_DESTINATION_CONNECTION, value);
	}

	//
	public java.lang.String getDistributedDestinationConnection() {
		return (java.lang.String)this.getValue(DISTRIBUTED_DESTINATION_CONNECTION);
	}

	// This attribute is optional
	public void setDistributedDestinationConnectionId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(DISTRIBUTED_DESTINATION_CONNECTION) == 0) {
			setValue(DISTRIBUTED_DESTINATION_CONNECTION, "");
		}
		setAttributeValue(DISTRIBUTED_DESTINATION_CONNECTION, "Id", value);
	}

	//
	public java.lang.String getDistributedDestinationConnectionId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(DISTRIBUTED_DESTINATION_CONNECTION) == 0) {
			return null;
		} else {
			return getAttributeValue(DISTRIBUTED_DESTINATION_CONNECTION, "Id");
		}
	}

	// This attribute is optional
	public void setUse81StylePolling(boolean value) {
		this.setValue(USE81_STYLE_POLLING, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isUse81StylePolling() {
		Boolean ret = (Boolean)this.getValue(USE81_STYLE_POLLING);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setInitSuspendSeconds(long value) {
		this.setValue(INIT_SUSPEND_SECONDS, java.lang.Long.valueOf(value));
	}

	//
	public long getInitSuspendSeconds() {
		Long ret = (Long)this.getValue(INIT_SUSPEND_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"INIT_SUSPEND_SECONDS", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setInitSuspendSecondsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(JMS_POLLING_INTERVAL_SECONDS) == 0) {
			setValue(JMS_POLLING_INTERVAL_SECONDS, "");
		}
		setAttributeValue(JMS_POLLING_INTERVAL_SECONDS, "J2eeId", value);
	}

	//
	public java.lang.String getInitSuspendSecondsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(JMS_POLLING_INTERVAL_SECONDS) == 0) {
			return null;
		} else {
			return getAttributeValue(JMS_POLLING_INTERVAL_SECONDS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setMaxSuspendSeconds(long value) {
		this.setValue(MAX_SUSPEND_SECONDS, java.lang.Long.valueOf(value));
	}

	//
	public long getMaxSuspendSeconds() {
		Long ret = (Long)this.getValue(MAX_SUSPEND_SECONDS);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MAX_SUSPEND_SECONDS", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setMaxSuspendSecondsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(JMS_POLLING_INTERVAL_SECONDS) == 0) {
			setValue(JMS_POLLING_INTERVAL_SECONDS, "");
		}
		setAttributeValue(JMS_POLLING_INTERVAL_SECONDS, "J2eeId", value);
	}

	//
	public java.lang.String getMaxSuspendSecondsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(JMS_POLLING_INTERVAL_SECONDS) == 0) {
			return null;
		} else {
			return getAttributeValue(JMS_POLLING_INTERVAL_SECONDS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setSecurityPlugin(SecurityPluginType value) {
		this.setValue(SECURITY_PLUGIN, value);
	}

	//
	public SecurityPluginType getSecurityPlugin() {
		return (SecurityPluginType)this.getValue(SECURITY_PLUGIN);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public PoolType newPoolType() {
		return new PoolType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public TimerDescriptorType newTimerDescriptorType() {
		return new TimerDescriptorType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SecurityPluginType newSecurityPluginType() {
		return new SecurityPluginType();
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
		// Validating property pool
		if (getPool() != null) {
			getPool().validate();
		}
		// Validating property timerDescriptor
		if (getTimerDescriptor() != null) {
			getTimerDescriptor().validate();
		}
		// Validating property resourceAdapterJndiName
		// Validating property resourceAdapterJndiNameId
		if (getResourceAdapterJndiNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getResourceAdapterJndiNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "resourceAdapterJndiNameId", this);	// NOI18N
			}
		}
		// Validating property destinationJndiName
		// Validating property destinationJndiNameId
		if (getDestinationJndiNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getDestinationJndiNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "destinationJndiNameId", this);	// NOI18N
			}
		}
		// Validating property initialContextFactory
		// Validating property initialContextFactoryId
		if (getInitialContextFactoryId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getInitialContextFactoryId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "initialContextFactoryId", this);	// NOI18N
			}
		}
		// Validating property providerUrl
		// Validating property providerUrlId
		if (getProviderUrlId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getProviderUrlId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "providerUrlId", this);	// NOI18N
			}
		}
		// Validating property connectionFactoryJndiName
		// Validating property connectionFactoryJndiNameId
		if (getConnectionFactoryJndiNameId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getConnectionFactoryJndiNameId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "connectionFactoryJndiNameId", this);	// NOI18N
			}
		}
		// Validating property destinationResourceLink
		if (getDestinationResourceLink() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getDestinationResourceLink() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "destinationResourceLink", this);	// NOI18N
			}
		}
		// Validating property destinationResourceLinkJ2eeId
		if (getDestinationResourceLinkJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getDestinationResourceLinkJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "destinationResourceLinkJ2eeId", this);	// NOI18N
			}
		}
		// Validating property connectionFactoryResourceLink
		if (getConnectionFactoryResourceLink() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getConnectionFactoryResourceLink() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "connectionFactoryResourceLink", this);	// NOI18N
			}
		}
		// Validating property connectionFactoryResourceLinkJ2eeId
		if (getConnectionFactoryResourceLinkJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getConnectionFactoryResourceLinkJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "connectionFactoryResourceLinkJ2eeId", this);	// NOI18N
			}
		}
		// Validating property jmsPollingIntervalSeconds
		if (getJmsPollingIntervalSeconds() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getJmsPollingIntervalSeconds() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "jmsPollingIntervalSeconds", this);	// NOI18N
		}
		// Validating property jmsPollingIntervalSecondsJ2eeId
		if (getJmsPollingIntervalSecondsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getJmsPollingIntervalSecondsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "jmsPollingIntervalSecondsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property jmsClientId
		// Validating property jmsClientIdId
		if (getJmsClientIdId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getJmsClientIdId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "jmsClientIdId", this);	// NOI18N
			}
		}
		// Validating property generateUniqueJmsClientId
		{
			boolean patternPassed = false;
			if ((isGenerateUniqueJmsClientId() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isGenerateUniqueJmsClientId()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "generateUniqueJmsClientId", this);	// NOI18N
		}
		// Validating property durableSubscriptionDeletion
		{
			boolean patternPassed = false;
			if ((isDurableSubscriptionDeletion() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isDurableSubscriptionDeletion()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "durableSubscriptionDeletion", this);	// NOI18N
		}
		// Validating property maxMessagesInTransaction
		if (getMaxMessagesInTransaction() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getMaxMessagesInTransaction() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "maxMessagesInTransaction", this);	// NOI18N
		}
		// Validating property maxMessagesInTransactionJ2eeId
		if (getMaxMessagesInTransactionJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getMaxMessagesInTransactionJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "maxMessagesInTransactionJ2eeId", this);	// NOI18N
			}
		}
		// Validating property distributedDestinationConnection
		// Validating property distributedDestinationConnectionId
		if (getDistributedDestinationConnectionId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getDistributedDestinationConnectionId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "distributedDestinationConnectionId", this);	// NOI18N
			}
		}
		// Validating property use81StylePolling
		{
			boolean patternPassed = false;
			if ((isUse81StylePolling() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isUse81StylePolling()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "use81StylePolling", this);	// NOI18N
		}
		// Validating property initSuspendSeconds
		if (getInitSuspendSeconds() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getInitSuspendSeconds() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "initSuspendSeconds", this);	// NOI18N
		}
		// Validating property initSuspendSecondsJ2eeId
		if (getInitSuspendSecondsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getInitSuspendSecondsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "initSuspendSecondsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property maxSuspendSeconds
		if (getMaxSuspendSeconds() - 0L < 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getMaxSuspendSeconds() minInclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "maxSuspendSeconds", this);	// NOI18N
		}
		// Validating property maxSuspendSecondsJ2eeId
		if (getMaxSuspendSecondsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getMaxSuspendSecondsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "maxSuspendSecondsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property securityPlugin
		if (getSecurityPlugin() != null) {
			getSecurityPlugin().validate();
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Pool");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getPool();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(POOL, 0, str, indent);

		str.append(indent);
		str.append("TimerDescriptor");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getTimerDescriptor();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(TIMER_DESCRIPTOR, 0, str, indent);

		str.append(indent);
		str.append("ResourceAdapterJndiName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getResourceAdapterJndiName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(RESOURCE_ADAPTER_JNDI_NAME, 0, str, indent);

		str.append(indent);
		str.append("DestinationJndiName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDestinationJndiName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DESTINATION_JNDI_NAME, 0, str, indent);

		str.append(indent);
		str.append("InitialContextFactory");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getInitialContextFactory();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(INITIAL_CONTEXT_FACTORY, 0, str, indent);

		str.append(indent);
		str.append("ProviderUrl");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getProviderUrl();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PROVIDER_URL, 0, str, indent);

		str.append(indent);
		str.append("ConnectionFactoryJndiName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getConnectionFactoryJndiName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CONNECTION_FACTORY_JNDI_NAME, 0, str, indent);

		str.append(indent);
		str.append("DestinationResourceLink");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDestinationResourceLink();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DESTINATION_RESOURCE_LINK, 0, str, indent);

		str.append(indent);
		str.append("ConnectionFactoryResourceLink");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getConnectionFactoryResourceLink();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CONNECTION_FACTORY_RESOURCE_LINK, 0, str, indent);

		if (this.getValue(JMS_POLLING_INTERVAL_SECONDS) != null) {
			str.append(indent);
			str.append("JmsPollingIntervalSeconds");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getJmsPollingIntervalSeconds());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(JMS_POLLING_INTERVAL_SECONDS, 0, str, indent);
		}

		str.append(indent);
		str.append("JmsClientId");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getJmsClientId();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(JMS_CLIENT_ID, 0, str, indent);

		str.append(indent);
		str.append("GenerateUniqueJmsClientId");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isGenerateUniqueJmsClientId()?"true":"false"));
		this.dumpAttributes(GENERATE_UNIQUE_JMS_CLIENT_ID, 0, str, indent);

		str.append(indent);
		str.append("DurableSubscriptionDeletion");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isDurableSubscriptionDeletion()?"true":"false"));
		this.dumpAttributes(DURABLE_SUBSCRIPTION_DELETION, 0, str, indent);

		if (this.getValue(MAX_MESSAGES_IN_TRANSACTION) != null) {
			str.append(indent);
			str.append("MaxMessagesInTransaction");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getMaxMessagesInTransaction());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(MAX_MESSAGES_IN_TRANSACTION, 0, str, indent);
		}

		str.append(indent);
		str.append("DistributedDestinationConnection");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDistributedDestinationConnection();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DISTRIBUTED_DESTINATION_CONNECTION, 0, str, indent);

		str.append(indent);
		str.append("Use81StylePolling");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isUse81StylePolling()?"true":"false"));
		this.dumpAttributes(USE81_STYLE_POLLING, 0, str, indent);

		if (this.getValue(INIT_SUSPEND_SECONDS) != null) {
			str.append(indent);
			str.append("InitSuspendSeconds");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getInitSuspendSeconds());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(INIT_SUSPEND_SECONDS, 0, str, indent);
		}

		if (this.getValue(MAX_SUSPEND_SECONDS) != null) {
			str.append(indent);
			str.append("MaxSuspendSeconds");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getMaxSuspendSeconds());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(MAX_SUSPEND_SECONDS, 0, str, indent);
		}

		str.append(indent);
		str.append("SecurityPlugin");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getSecurityPlugin();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(SECURITY_PLUGIN, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("MessageDrivenDescriptorType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

