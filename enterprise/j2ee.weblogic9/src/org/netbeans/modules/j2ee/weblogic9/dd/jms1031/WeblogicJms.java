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
 *	This generated bean class WeblogicJms matches the schema element 'weblogic-jms'.
 *
 *	Generated on Tue Jul 25 03:26:58 PDT 2017
 *
 *	This class matches the root element of the XML Schema,
 *	and is the root of the following bean graph:
 *
 *	weblogicJms <weblogic-jms> : WeblogicJms
 *		version <version> : int[0,1]
 *		notes <notes> : java.lang.String[0,1]
 *		quota <quota> : QuotaType[0,n]
 *			[attr: name CDATA #REQUIRED  : java.lang.String]
 *			notes <notes> : java.lang.String[0,1]
 *			bytesMaximum <bytes-maximum> : long[0,1]
 *			messagesMaximum <messages-maximum> : long[0,1]
 *			policy <policy> : java.lang.String[0,1] 	[enumeration (FIFO), enumeration (Preemptive)]
 *			shared <shared> : boolean[0,1]
 *		template <template> : TemplateType[0,n]
 *			[attr: name CDATA #REQUIRED  : java.lang.String]
 *			notes <notes> : java.lang.String[0,1]
 *			destinationKey <destination-key> : java.lang.String[0,n]
 *			thresholds <thresholds> : ThresholdParamsType[0,1]
 *				bytesHigh <bytes-high> : long[0,1]
 *				bytesLow <bytes-low> : long[0,1]
 *				messagesHigh <messages-high> : long[0,1]
 *				messagesLow <messages-low> : long[0,1]
 *			deliveryParamsOverrides <delivery-params-overrides> : DeliveryParamsOverridesType[0,1]
 *				deliveryMode <delivery-mode> : java.lang.String[0,1] 	[enumeration (Persistent), enumeration (Non-Persistent)]
 *				timeToDeliver <time-to-deliver> : java.lang.String[0,1]
 *				timeToLive <time-to-live> : long[0,1]
 *				priority <priority> : int[0,1]
 *				redeliveryDelay <redelivery-delay> : long[0,1]
 *			deliveryFailureParams <delivery-failure-params> : DeliveryFailureParamsType[0,1]
 *				errorDestination <error-destination> : java.lang.String[0,1]
 *				redeliveryLimit <redelivery-limit> : int[0,1]
 *				expirationPolicy <expiration-policy> : java.lang.String[0,1] 	[enumeration (Discard), enumeration (Log), enumeration (Redirect)]
 *				expirationLoggingPolicy <expiration-logging-policy> : java.lang.String[0,1]
 *			messageLoggingParams <message-logging-params> : MessageLoggingParamsType[0,1]
 *				messageLoggingEnabled <message-logging-enabled> : boolean[0,1]
 *				messageLoggingFormat <message-logging-format> : java.lang.String[0,1]
 *			attachSender <attach-sender> : java.lang.String[0,1] 	[enumeration (supports), enumeration (always), enumeration (never)]
 *			productionPausedAtStartup <production-paused-at-startup> : boolean[0,1]
 *			insertionPausedAtStartup <insertion-paused-at-startup> : boolean[0,1]
 *			consumptionPausedAtStartup <consumption-paused-at-startup> : boolean[0,1]
 *			maximumMessageSize <maximum-message-size> : int[0,1]
 *			quota <quota> : java.lang.String[0,1]
 *			defaultUnitOfOrder <default-unit-of-order> : boolean[0,1]
 *			safExportPolicy <saf-export-policy> : java.lang.String[0,1] 	[enumeration (All), enumeration (None)]
 *			multicast <multicast> : MulticastParamsType[0,1]
 *				multicastAddress <multicast-address> : java.lang.String[0,1]
 *				multicastPort <multicast-port> : int[0,1]
 *				multicastTimeToLive <multicast-time-to-live> : int[0,1]
 *			groupParams <group-params> : GroupParamsType[0,n]
 *				[attr: sub-deployment-name CDATA #REQUIRED  : java.lang.String]
 *				errorDestination <error-destination> : java.lang.String[0,1]
 *			messagingPerformancePreference <messaging-performance-preference> : int[0,1]
 *			unitOfWorkHandlingPolicy <unit-of-work-handling-policy> : java.lang.String[0,1] 	[enumeration (PassThrough), enumeration (SingleMessageDelivery)]
 *			incompleteWorkExpirationTime <incomplete-work-expiration-time> : int[0,1]
 *		destinationKey <destination-key> : DestinationKeyType[0,n]
 *			[attr: name CDATA #REQUIRED  : java.lang.String]
 *			notes <notes> : java.lang.String[0,1]
 *			property2 <property> : java.lang.String[0,1]
 *			keyType <key-type> : java.lang.String[0,1] 	[enumeration (Boolean), enumeration (Byte), enumeration (Short), enumeration (Int), enumeration (Long), enumeration (Float), enumeration (Double), enumeration (String)]
 *			sortOrder <sort-order> : java.lang.String[0,1] 	[enumeration (Ascending), enumeration (Descending)]
 *		connectionFactory <connection-factory> : JmsConnectionFactoryType[0,n]
 *			[attr: name CDATA #REQUIRED  : java.lang.String]
 *			notes <notes> : java.lang.String[0,1]
 *			subDeploymentName <sub-deployment-name> : java.lang.String[0,1]
 *			defaultTargetingEnabled <default-targeting-enabled> : boolean[0,1]
 *			jndiName <jndi-name> : java.lang.String[0,1]
 *			localJndiName <local-jndi-name> : java.lang.String[0,1]
 *			defaultDeliveryParams <default-delivery-params> : DefaultDeliveryParamsType[0,1]
 *				defaultDeliveryMode <default-delivery-mode> : java.lang.String[0,1] 	[enumeration (Persistent), enumeration (Non-Persistent)]
 *				defaultTimeToDeliver <default-time-to-deliver> : java.lang.String[0,1]
 *				defaultTimeToLive <default-time-to-live> : long[0,1]
 *				defaultPriority <default-priority> : int[0,1]
 *				defaultRedeliveryDelay <default-redelivery-delay> : long[0,1]
 *				sendTimeout <send-timeout> : long[0,1]
 *				defaultCompressionThreshold <default-compression-threshold> : int[0,1]
 *				defaultUnitOfOrder <default-unit-of-order> : java.lang.String[0,1]
 *			clientParams <client-params> : ClientParamsType[0,1]
 *				clientId <client-id> : java.lang.String[0,1]
 *				acknowledgePolicy <acknowledge-policy> : java.lang.String[0,1] 	[enumeration (All), enumeration (Previous), enumeration (One)]
 *				allowCloseInOnMessage <allow-close-in-onMessage> : boolean[0,1]
 *				messagesMaximum <messages-maximum> : int[0,1]
 *				multicastOverrunPolicy <multicast-overrun-policy> : java.lang.String[0,1] 	[enumeration (KeepOld), enumeration (KeepNew)]
 *				synchronousPrefetchMode <synchronous-prefetch-mode> : java.lang.String[0,1] 	[enumeration (enabled), enumeration (disabled), enumeration (topicSubscriberOnly)]
 *				reconnectPolicy <reconnect-policy> : java.lang.String[0,1] 	[enumeration (none), enumeration (producer), enumeration (all)]
 *				reconnectBlockingMillis <reconnect-blocking-millis> : long[0,1]
 *				totalReconnectPeriodMillis <total-reconnect-period-millis> : long[0,1]
 *			transactionParams <transaction-params> : TransactionParamsType[0,1]
 *				transactionTimeout <transaction-timeout> : long[0,1]
 *				xaConnectionFactoryEnabled <xa-connection-factory-enabled> : boolean[0,1]
 *			flowControlParams <flow-control-params> : FlowControlParamsType[0,1]
 *				flowMinimum <flow-minimum> : int[0,1]
 *				flowMaximum <flow-maximum> : int[0,1]
 *				flowInterval <flow-interval> : int[0,1]
 *				flowSteps <flow-steps> : int[0,1]
 *				flowControlEnabled <flow-control-enabled> : boolean[0,1]
 *				oneWaySendMode <one-way-send-mode> : java.lang.String[0,1] 	[enumeration (enabled), enumeration (disabled), enumeration (topicOnly)]
 *				oneWaySendWindowSize <one-way-send-window-size> : int[0,1]
 *			loadBalancingParams <load-balancing-params> : LoadBalancingParamsType[0,1]
 *				loadBalancingEnabled <load-balancing-enabled> : boolean[0,1]
 *				serverAffinityEnabled <server-affinity-enabled> : boolean[0,1]
 *			securityParams <security-params> : SecurityParamsType[0,1]
 *				attachJmsxUserId <attach-jmsx-user-id> : boolean[0,1]
 *		foreignServer <foreign-server> : ForeignServerType[0,n]
 *			[attr: name CDATA #REQUIRED  : java.lang.String]
 *			notes <notes> : java.lang.String[0,1]
 *			subDeploymentName <sub-deployment-name> : java.lang.String[0,1]
 *			defaultTargetingEnabled <default-targeting-enabled> : boolean[0,1]
 *			foreignDestination <foreign-destination> : ForeignDestinationType[0,n]
 *				[attr: name CDATA #REQUIRED  : java.lang.String]
 *				notes <notes> : java.lang.String[0,1]
 *				localJndiName <local-jndi-name> : java.lang.String
 *				remoteJndiName <remote-jndi-name> : java.lang.String
 *			foreignConnectionFactory <foreign-connection-factory> : ForeignConnectionFactoryType[0,n]
 *				[attr: name CDATA #REQUIRED  : java.lang.String]
 *				notes <notes> : java.lang.String[0,1]
 *				localJndiName <local-jndi-name> : java.lang.String
 *				remoteJndiName <remote-jndi-name> : java.lang.String
 *				username <username> : java.lang.String[0,1]
 *				passwordEncrypted <password-encrypted> : java.lang.String[0,1]
 *			initialContextFactory <initial-context-factory> : java.lang.String[0,1]
 *			connectionUrl <connection-url> : java.lang.String[0,1]
 *			jndiPropertiesCredentialEncrypted <jndi-properties-credential-encrypted> : java.lang.String[0,1]
 *			jndiProperty <jndi-property> : PropertyType[0,n]
 *				key <key> : java.lang.String
 *				value <value> : java.lang.String
 *		queue <queue> : QueueType[0,n]
 *			[attr: name CDATA #REQUIRED  : java.lang.String]
 *			notes <notes> : java.lang.String[0,1]
 *			subDeploymentName <sub-deployment-name> : java.lang.String[0,1]
 *			defaultTargetingEnabled <default-targeting-enabled> : boolean[0,1]
 *			template <template> : java.lang.String[0,1]
 *			destinationKey <destination-key> : java.lang.String[0,n]
 *			thresholds <thresholds> : ThresholdParamsType[0,1]
 *				bytesHigh <bytes-high> : long[0,1]
 *				bytesLow <bytes-low> : long[0,1]
 *				messagesHigh <messages-high> : long[0,1]
 *				messagesLow <messages-low> : long[0,1]
 *			deliveryParamsOverrides <delivery-params-overrides> : DeliveryParamsOverridesType[0,1]
 *				deliveryMode <delivery-mode> : java.lang.String[0,1] 	[enumeration (Persistent), enumeration (Non-Persistent)]
 *				timeToDeliver <time-to-deliver> : java.lang.String[0,1]
 *				timeToLive <time-to-live> : long[0,1]
 *				priority <priority> : int[0,1]
 *				redeliveryDelay <redelivery-delay> : long[0,1]
 *			deliveryFailureParams <delivery-failure-params> : DeliveryFailureParamsType[0,1]
 *				errorDestination <error-destination> : java.lang.String[0,1]
 *				redeliveryLimit <redelivery-limit> : int[0,1]
 *				expirationPolicy <expiration-policy> : java.lang.String[0,1] 	[enumeration (Discard), enumeration (Log), enumeration (Redirect)]
 *				expirationLoggingPolicy <expiration-logging-policy> : java.lang.String[0,1]
 *			messageLoggingParams <message-logging-params> : MessageLoggingParamsType[0,1]
 *				messageLoggingEnabled <message-logging-enabled> : boolean[0,1]
 *				messageLoggingFormat <message-logging-format> : java.lang.String[0,1]
 *			attachSender <attach-sender> : java.lang.String[0,1] 	[enumeration (supports), enumeration (always), enumeration (never)]
 *			productionPausedAtStartup <production-paused-at-startup> : boolean[0,1]
 *			insertionPausedAtStartup <insertion-paused-at-startup> : boolean[0,1]
 *			consumptionPausedAtStartup <consumption-paused-at-startup> : boolean[0,1]
 *			maximumMessageSize <maximum-message-size> : int[0,1]
 *			quota <quota> : java.lang.String[0,1]
 *			jndiName <jndi-name> : java.lang.String[0,1]
 *			localJndiName <local-jndi-name> : java.lang.String[0,1]
 *			jmsCreateDestinationIdentifier <jms-create-destination-identifier> : java.lang.String[0,1]
 *			defaultUnitOfOrder <default-unit-of-order> : boolean[0,1]
 *			safExportPolicy <saf-export-policy> : java.lang.String[0,1] 	[enumeration (All), enumeration (None)]
 *			messagingPerformancePreference <messaging-performance-preference> : int[0,1]
 *			unitOfWorkHandlingPolicy <unit-of-work-handling-policy> : java.lang.String[0,1] 	[enumeration (PassThrough), enumeration (SingleMessageDelivery)]
 *			incompleteWorkExpirationTime <incomplete-work-expiration-time> : int[0,1]
 *		topic <topic> : TopicType[0,n]
 *			[attr: name CDATA #REQUIRED  : java.lang.String]
 *			notes <notes> : java.lang.String[0,1]
 *			subDeploymentName <sub-deployment-name> : java.lang.String[0,1]
 *			defaultTargetingEnabled <default-targeting-enabled> : boolean[0,1]
 *			template <template> : java.lang.String[0,1]
 *			destinationKey <destination-key> : java.lang.String[0,n]
 *			thresholds <thresholds> : ThresholdParamsType[0,1]
 *				bytesHigh <bytes-high> : long[0,1]
 *				bytesLow <bytes-low> : long[0,1]
 *				messagesHigh <messages-high> : long[0,1]
 *				messagesLow <messages-low> : long[0,1]
 *			deliveryParamsOverrides <delivery-params-overrides> : DeliveryParamsOverridesType[0,1]
 *				deliveryMode <delivery-mode> : java.lang.String[0,1] 	[enumeration (Persistent), enumeration (Non-Persistent)]
 *				timeToDeliver <time-to-deliver> : java.lang.String[0,1]
 *				timeToLive <time-to-live> : long[0,1]
 *				priority <priority> : int[0,1]
 *				redeliveryDelay <redelivery-delay> : long[0,1]
 *			deliveryFailureParams <delivery-failure-params> : DeliveryFailureParamsType[0,1]
 *				errorDestination <error-destination> : java.lang.String[0,1]
 *				redeliveryLimit <redelivery-limit> : int[0,1]
 *				expirationPolicy <expiration-policy> : java.lang.String[0,1] 	[enumeration (Discard), enumeration (Log), enumeration (Redirect)]
 *				expirationLoggingPolicy <expiration-logging-policy> : java.lang.String[0,1]
 *			messageLoggingParams <message-logging-params> : MessageLoggingParamsType[0,1]
 *				messageLoggingEnabled <message-logging-enabled> : boolean[0,1]
 *				messageLoggingFormat <message-logging-format> : java.lang.String[0,1]
 *			attachSender <attach-sender> : java.lang.String[0,1] 	[enumeration (supports), enumeration (always), enumeration (never)]
 *			productionPausedAtStartup <production-paused-at-startup> : boolean[0,1]
 *			insertionPausedAtStartup <insertion-paused-at-startup> : boolean[0,1]
 *			consumptionPausedAtStartup <consumption-paused-at-startup> : boolean[0,1]
 *			maximumMessageSize <maximum-message-size> : int[0,1]
 *			quota <quota> : java.lang.String[0,1]
 *			jndiName <jndi-name> : java.lang.String[0,1]
 *			localJndiName <local-jndi-name> : java.lang.String[0,1]
 *			jmsCreateDestinationIdentifier <jms-create-destination-identifier> : java.lang.String[0,1]
 *			defaultUnitOfOrder <default-unit-of-order> : boolean[0,1]
 *			safExportPolicy <saf-export-policy> : java.lang.String[0,1] 	[enumeration (All), enumeration (None)]
 *			messagingPerformancePreference <messaging-performance-preference> : int[0,1]
 *			unitOfWorkHandlingPolicy <unit-of-work-handling-policy> : java.lang.String[0,1] 	[enumeration (PassThrough), enumeration (SingleMessageDelivery)]
 *			incompleteWorkExpirationTime <incomplete-work-expiration-time> : int[0,1]
 *			multicast <multicast> : MulticastParamsType[0,1]
 *				multicastAddress <multicast-address> : java.lang.String[0,1]
 *				multicastPort <multicast-port> : int[0,1]
 *				multicastTimeToLive <multicast-time-to-live> : int[0,1]
 *		distributedQueue <distributed-queue> : DistributedQueueType[0,n]
 *			[attr: name CDATA #REQUIRED  : java.lang.String]
 *			notes <notes> : java.lang.String[0,1]
 *			jndiName <jndi-name> : java.lang.String[0,1]
 *			localJndiName <local-jndi-name> : java.lang.String[0,1]
 *			loadBalancingPolicy <load-balancing-policy> : java.lang.String[0,1]
 *			unitOfOrderRouting <unit-of-order-routing> : java.lang.String[0,1] 	[enumeration (Hash), enumeration (PathService)]
 *			safExportPolicy <saf-export-policy> : java.lang.String[0,1] 	[enumeration (All), enumeration (None)]
 *			distributedQueueMember <distributed-queue-member> : DistributedDestinationMemberType[0,n]
 *				[attr: name CDATA #REQUIRED  : java.lang.String]
 *				notes <notes> : java.lang.String[0,1]
 *				weight <weight> : int[0,1]
 *				physicalDestinationName <physical-destination-name> : java.lang.String[0,1]
 *			forwardDelay <forward-delay> : int[0,1]
 *			resetDeliveryCountOnForward <reset-delivery-count-on-forward> : boolean[0,1]
 *		distributedTopic <distributed-topic> : DistributedTopicType[0,n]
 *			[attr: name CDATA #REQUIRED  : java.lang.String]
 *			notes <notes> : java.lang.String[0,1]
 *			jndiName <jndi-name> : java.lang.String[0,1]
 *			localJndiName <local-jndi-name> : java.lang.String[0,1]
 *			loadBalancingPolicy <load-balancing-policy> : java.lang.String[0,1]
 *			unitOfOrderRouting <unit-of-order-routing> : java.lang.String[0,1] 	[enumeration (Hash), enumeration (PathService)]
 *			safExportPolicy <saf-export-policy> : java.lang.String[0,1] 	[enumeration (All), enumeration (None)]
 *			distributedTopicMember <distributed-topic-member> : DistributedDestinationMemberType[0,n]
 *				[attr: name CDATA #REQUIRED  : java.lang.String]
 *				notes <notes> : java.lang.String[0,1]
 *				weight <weight> : int[0,1]
 *				physicalDestinationName <physical-destination-name> : java.lang.String[0,1]
 *		uniformDistributedQueue <uniform-distributed-queue> : UniformDistributedQueueType[0,n]
 *			[attr: name CDATA #REQUIRED  : java.lang.String]
 *			notes <notes> : java.lang.String[0,1]
 *			subDeploymentName <sub-deployment-name> : java.lang.String[0,1]
 *			defaultTargetingEnabled <default-targeting-enabled> : boolean[0,1]
 *			template <template> : java.lang.String[0,1]
 *			destinationKey <destination-key> : java.lang.String[0,n]
 *			thresholds <thresholds> : ThresholdParamsType[0,1]
 *				bytesHigh <bytes-high> : long[0,1]
 *				bytesLow <bytes-low> : long[0,1]
 *				messagesHigh <messages-high> : long[0,1]
 *				messagesLow <messages-low> : long[0,1]
 *			deliveryParamsOverrides <delivery-params-overrides> : DeliveryParamsOverridesType[0,1]
 *				deliveryMode <delivery-mode> : java.lang.String[0,1] 	[enumeration (Persistent), enumeration (Non-Persistent)]
 *				timeToDeliver <time-to-deliver> : java.lang.String[0,1]
 *				timeToLive <time-to-live> : long[0,1]
 *				priority <priority> : int[0,1]
 *				redeliveryDelay <redelivery-delay> : long[0,1]
 *			deliveryFailureParams <delivery-failure-params> : DeliveryFailureParamsType[0,1]
 *				errorDestination <error-destination> : java.lang.String[0,1]
 *				redeliveryLimit <redelivery-limit> : int[0,1]
 *				expirationPolicy <expiration-policy> : java.lang.String[0,1] 	[enumeration (Discard), enumeration (Log), enumeration (Redirect)]
 *				expirationLoggingPolicy <expiration-logging-policy> : java.lang.String[0,1]
 *			messageLoggingParams <message-logging-params> : MessageLoggingParamsType[0,1]
 *				messageLoggingEnabled <message-logging-enabled> : boolean[0,1]
 *				messageLoggingFormat <message-logging-format> : java.lang.String[0,1]
 *			attachSender <attach-sender> : java.lang.String[0,1] 	[enumeration (supports), enumeration (always), enumeration (never)]
 *			productionPausedAtStartup <production-paused-at-startup> : boolean[0,1]
 *			insertionPausedAtStartup <insertion-paused-at-startup> : boolean[0,1]
 *			consumptionPausedAtStartup <consumption-paused-at-startup> : boolean[0,1]
 *			maximumMessageSize <maximum-message-size> : int[0,1]
 *			quota <quota> : java.lang.String[0,1]
 *			jndiName <jndi-name> : java.lang.String[0,1]
 *			localJndiName <local-jndi-name> : java.lang.String[0,1]
 *			jmsCreateDestinationIdentifier <jms-create-destination-identifier> : java.lang.String[0,1]
 *			defaultUnitOfOrder <default-unit-of-order> : boolean[0,1]
 *			safExportPolicy <saf-export-policy> : java.lang.String[0,1] 	[enumeration (All), enumeration (None)]
 *			messagingPerformancePreference <messaging-performance-preference> : int[0,1]
 *			unitOfWorkHandlingPolicy <unit-of-work-handling-policy> : java.lang.String[0,1] 	[enumeration (PassThrough), enumeration (SingleMessageDelivery)]
 *			incompleteWorkExpirationTime <incomplete-work-expiration-time> : int[0,1]
 *			loadBalancingPolicy <load-balancing-policy> : java.lang.String[0,1]
 *			unitOfOrderRouting <unit-of-order-routing> : java.lang.String[0,1] 	[enumeration (Hash), enumeration (PathService)]
 *			forwardDelay <forward-delay> : int[0,1]
 *			resetDeliveryCountOnForward <reset-delivery-count-on-forward> : boolean[0,1]
 *		uniformDistributedTopic <uniform-distributed-topic> : UniformDistributedTopicType[0,n]
 *			[attr: name CDATA #REQUIRED  : java.lang.String]
 *			notes <notes> : java.lang.String[0,1]
 *			subDeploymentName <sub-deployment-name> : java.lang.String[0,1]
 *			defaultTargetingEnabled <default-targeting-enabled> : boolean[0,1]
 *			template <template> : java.lang.String[0,1]
 *			destinationKey <destination-key> : java.lang.String[0,n]
 *			thresholds <thresholds> : ThresholdParamsType[0,1]
 *				bytesHigh <bytes-high> : long[0,1]
 *				bytesLow <bytes-low> : long[0,1]
 *				messagesHigh <messages-high> : long[0,1]
 *				messagesLow <messages-low> : long[0,1]
 *			deliveryParamsOverrides <delivery-params-overrides> : DeliveryParamsOverridesType[0,1]
 *				deliveryMode <delivery-mode> : java.lang.String[0,1] 	[enumeration (Persistent), enumeration (Non-Persistent)]
 *				timeToDeliver <time-to-deliver> : java.lang.String[0,1]
 *				timeToLive <time-to-live> : long[0,1]
 *				priority <priority> : int[0,1]
 *				redeliveryDelay <redelivery-delay> : long[0,1]
 *			deliveryFailureParams <delivery-failure-params> : DeliveryFailureParamsType[0,1]
 *				errorDestination <error-destination> : java.lang.String[0,1]
 *				redeliveryLimit <redelivery-limit> : int[0,1]
 *				expirationPolicy <expiration-policy> : java.lang.String[0,1] 	[enumeration (Discard), enumeration (Log), enumeration (Redirect)]
 *				expirationLoggingPolicy <expiration-logging-policy> : java.lang.String[0,1]
 *			messageLoggingParams <message-logging-params> : MessageLoggingParamsType[0,1]
 *				messageLoggingEnabled <message-logging-enabled> : boolean[0,1]
 *				messageLoggingFormat <message-logging-format> : java.lang.String[0,1]
 *			attachSender <attach-sender> : java.lang.String[0,1] 	[enumeration (supports), enumeration (always), enumeration (never)]
 *			productionPausedAtStartup <production-paused-at-startup> : boolean[0,1]
 *			insertionPausedAtStartup <insertion-paused-at-startup> : boolean[0,1]
 *			consumptionPausedAtStartup <consumption-paused-at-startup> : boolean[0,1]
 *			maximumMessageSize <maximum-message-size> : int[0,1]
 *			quota <quota> : java.lang.String[0,1]
 *			jndiName <jndi-name> : java.lang.String[0,1]
 *			localJndiName <local-jndi-name> : java.lang.String[0,1]
 *			jmsCreateDestinationIdentifier <jms-create-destination-identifier> : java.lang.String[0,1]
 *			defaultUnitOfOrder <default-unit-of-order> : boolean[0,1]
 *			safExportPolicy <saf-export-policy> : java.lang.String[0,1] 	[enumeration (All), enumeration (None)]
 *			messagingPerformancePreference <messaging-performance-preference> : int[0,1]
 *			unitOfWorkHandlingPolicy <unit-of-work-handling-policy> : java.lang.String[0,1] 	[enumeration (PassThrough), enumeration (SingleMessageDelivery)]
 *			incompleteWorkExpirationTime <incomplete-work-expiration-time> : int[0,1]
 *			loadBalancingPolicy <load-balancing-policy> : java.lang.String[0,1]
 *			unitOfOrderRouting <unit-of-order-routing> : java.lang.String[0,1] 	[enumeration (Hash), enumeration (PathService)]
 *			multicast <multicast> : MulticastParamsType[0,1]
 *				multicastAddress <multicast-address> : java.lang.String[0,1]
 *				multicastPort <multicast-port> : int[0,1]
 *				multicastTimeToLive <multicast-time-to-live> : int[0,1]
 *		safImportedDestinations <saf-imported-destinations> : SafImportedDestinationsType[0,n]
 *			[attr: name CDATA #REQUIRED  : java.lang.String]
 *			notes <notes> : java.lang.String[0,1]
 *			subDeploymentName <sub-deployment-name> : java.lang.String[0,1]
 *			defaultTargetingEnabled <default-targeting-enabled> : boolean[0,1]
 *			safQueue <saf-queue> : SafQueueType[0,n]
 *				[attr: name CDATA #REQUIRED  : java.lang.String]
 *				notes <notes> : java.lang.String[0,1]
 *				remoteJndiName <remote-jndi-name> : java.lang.String
 *				localJndiName <local-jndi-name> : java.lang.String[0,1]
 *				nonPersistentQos <non-persistent-qos> : java.lang.String[0,1] 	[enumeration (At-Most-Once), enumeration (At-Least-Once), enumeration (Exactly-Once)]
 *				safErrorHandling <saf-error-handling> : java.lang.String[0,1]
 *				timeToLiveDefault <time-to-live-default> : long[0,1]
 *				useSafTimeToLiveDefault <use-saf-time-to-live-default> : boolean[0,1]
 *				unitOfOrderRouting <unit-of-order-routing> : java.lang.String[0,1] 	[enumeration (Hash), enumeration (PathService)]
 *				messageLoggingParams <message-logging-params> : MessageLoggingParamsType[0,1]
 *					messageLoggingEnabled <message-logging-enabled> : boolean[0,1]
 *					messageLoggingFormat <message-logging-format> : java.lang.String[0,1]
 *			safTopic <saf-topic> : SafTopicType[0,n]
 *				[attr: name CDATA #REQUIRED  : java.lang.String]
 *				notes <notes> : java.lang.String[0,1]
 *				remoteJndiName <remote-jndi-name> : java.lang.String
 *				localJndiName <local-jndi-name> : java.lang.String[0,1]
 *				nonPersistentQos <non-persistent-qos> : java.lang.String[0,1] 	[enumeration (At-Most-Once), enumeration (At-Least-Once), enumeration (Exactly-Once)]
 *				safErrorHandling <saf-error-handling> : java.lang.String[0,1]
 *				timeToLiveDefault <time-to-live-default> : long[0,1]
 *				useSafTimeToLiveDefault <use-saf-time-to-live-default> : boolean[0,1]
 *				unitOfOrderRouting <unit-of-order-routing> : java.lang.String[0,1] 	[enumeration (Hash), enumeration (PathService)]
 *				messageLoggingParams <message-logging-params> : MessageLoggingParamsType[0,1]
 *					messageLoggingEnabled <message-logging-enabled> : boolean[0,1]
 *					messageLoggingFormat <message-logging-format> : java.lang.String[0,1]
 *			jndiPrefix <jndi-prefix> : java.lang.String[0,1]
 *			safRemoteContext <saf-remote-context> : java.lang.String[0,1]
 *			safErrorHandling <saf-error-handling> : java.lang.String[0,1]
 *			timeToLiveDefault <time-to-live-default> : long[0,1]
 *			useSafTimeToLiveDefault <use-saf-time-to-live-default> : boolean[0,1]
 *			unitOfOrderRouting <unit-of-order-routing> : java.lang.String[0,1] 	[enumeration (Hash), enumeration (PathService)]
 *			messageLoggingParams <message-logging-params> : MessageLoggingParamsType[0,1]
 *				messageLoggingEnabled <message-logging-enabled> : boolean[0,1]
 *				messageLoggingFormat <message-logging-format> : java.lang.String[0,1]
 *		safRemoteContext <saf-remote-context> : SafRemoteContextType[0,n]
 *			[attr: name CDATA #REQUIRED  : java.lang.String]
 *			notes <notes> : java.lang.String[0,1]
 *			safLoginContext <saf-login-context> : SafLoginContextType[0,1]
 *				loginURL <loginURL> : java.lang.String
 *				username <username> : java.lang.String[0,1]
 *				passwordEncrypted <password-encrypted> : java.lang.String[0,1]
 *			compressionThreshold <compression-threshold> : int[0,1]
 *			replyToSafRemoteContextName <reply-to-saf-remote-context-name> : java.lang.String[0,1]
 *		safErrorHandling <saf-error-handling> : SafErrorHandlingType[0,n]
 *			[attr: name CDATA #REQUIRED  : java.lang.String]
 *			notes <notes> : java.lang.String[0,1]
 *			policy <policy> : java.lang.String[0,1]
 *			logFormat <log-format> : java.lang.String[0,1]
 *			safErrorDestination <saf-error-destination> : java.lang.String[0,1]
 *
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.jms1031;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.*;

// BEGIN_NOI18N

public class WeblogicJms extends org.netbeans.modules.schema2beans.BaseBean
	 implements org.netbeans.modules.j2ee.weblogic9.dd.model.WeblogicJms
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	private static final String SERIALIZATION_HELPER_CHARSET = "UTF-8";	// NOI18N

	static public final String VERSION = "Version";	// NOI18N
	static public final String NOTES = "Notes";	// NOI18N
	static public final String QUOTA = "Quota";	// NOI18N
	static public final String TEMPLATE = "Template";	// NOI18N
	static public final String DESTINATION_KEY = "DestinationKey";	// NOI18N
	static public final String CONNECTION_FACTORY = "ConnectionFactory";	// NOI18N
	static public final String FOREIGN_SERVER = "ForeignServer";	// NOI18N
	static public final String QUEUE = "Queue";	// NOI18N
	static public final String TOPIC = "Topic";	// NOI18N
	static public final String DISTRIBUTED_QUEUE = "DistributedQueue";	// NOI18N
	static public final String DISTRIBUTED_TOPIC = "DistributedTopic";	// NOI18N
	static public final String UNIFORM_DISTRIBUTED_QUEUE = "UniformDistributedQueue";	// NOI18N
	static public final String UNIFORM_DISTRIBUTED_TOPIC = "UniformDistributedTopic";	// NOI18N
	static public final String SAF_IMPORTED_DESTINATIONS = "SafImportedDestinations";	// NOI18N
	static public final String SAF_REMOTE_CONTEXT = "SafRemoteContext";	// NOI18N
	static public final String SAF_ERROR_HANDLING = "SafErrorHandling";	// NOI18N

	public WeblogicJms() {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public WeblogicJms(org.w3c.dom.Node doc, int options) {
		this(Common.NO_DEFAULT_VALUES);
		try {
			initFromNode(doc, options);
		}
		catch (Schema2BeansException e) {
			throw new RuntimeException(e);
		}
	}
	protected void initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException
	{
		if (doc == null)
		{
			doc = GraphManager.createRootElementNode("weblogic-jms");	// NOI18N
			if (doc == null)
				throw new Schema2BeansException(Common.getMessage(
					"CantCreateDOMRoot_msg", "weblogic-jms"));
		}
		Node n = GraphManager.getElementNode("weblogic-jms", doc);	// NOI18N
		if (n == null)
			throw new Schema2BeansException(Common.getMessage(
				"DocRootNotInDOMGraph_msg", "weblogic-jms", doc.getFirstChild().getNodeName()));

		this.graphManager.setXmlDocument(doc);

		// Entry point of the createBeans() recursive calls
		this.createBean(n, this.graphManager());
		this.initialize(options);
	}
	public WeblogicJms(int options)
	{
		super(comparators, runtimeVersion);
		initOptions(options);
	}
	protected void initOptions(int options)
	{
		// The graph manager is allocated in the bean root
		this.graphManager = new GraphManager(this);
		this.createRoot("weblogic-jms", "WeblogicJms",	// NOI18N
			Common.TYPE_1 | Common.TYPE_BEAN, WeblogicJms.class);

		// Properties (see root bean comments for the bean graph)
		initPropertyTables(16);
		this.createProperty("version", 	// NOI18N
			VERSION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("notes", 	// NOI18N
			NOTES, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("quota", 	// NOI18N
			QUOTA, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			QuotaType.class);
		this.createAttribute(QUOTA, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("template", 	// NOI18N
			TEMPLATE, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			TemplateType.class);
		this.createAttribute(TEMPLATE, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("destination-key", 	// NOI18N
			DESTINATION_KEY, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			DestinationKeyType.class);
		this.createAttribute(DESTINATION_KEY, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("connection-factory", 	// NOI18N
			CONNECTION_FACTORY, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			JmsConnectionFactoryType.class);
		this.createAttribute(CONNECTION_FACTORY, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("foreign-server", 	// NOI18N
			FOREIGN_SERVER, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ForeignServerType.class);
		this.createAttribute(FOREIGN_SERVER, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("queue", 	// NOI18N
			QUEUE, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			QueueType.class);
		this.createAttribute(QUEUE, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("topic", 	// NOI18N
			TOPIC, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			TopicType.class);
		this.createAttribute(TOPIC, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("distributed-queue", 	// NOI18N
			DISTRIBUTED_QUEUE, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			DistributedQueueType.class);
		this.createAttribute(DISTRIBUTED_QUEUE, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("distributed-topic", 	// NOI18N
			DISTRIBUTED_TOPIC, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			DistributedTopicType.class);
		this.createAttribute(DISTRIBUTED_TOPIC, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("uniform-distributed-queue", 	// NOI18N
			UNIFORM_DISTRIBUTED_QUEUE, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			UniformDistributedQueueType.class);
		this.createAttribute(UNIFORM_DISTRIBUTED_QUEUE, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("uniform-distributed-topic", 	// NOI18N
			UNIFORM_DISTRIBUTED_TOPIC, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			UniformDistributedTopicType.class);
		this.createAttribute(UNIFORM_DISTRIBUTED_TOPIC, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("saf-imported-destinations", 	// NOI18N
			SAF_IMPORTED_DESTINATIONS, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SafImportedDestinationsType.class);
		this.createAttribute(SAF_IMPORTED_DESTINATIONS, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("saf-remote-context", 	// NOI18N
			SAF_REMOTE_CONTEXT, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SafRemoteContextType.class);
		this.createAttribute(SAF_REMOTE_CONTEXT, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("saf-error-handling", 	// NOI18N
			SAF_ERROR_HANDLING, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SafErrorHandlingType.class);
		this.createAttribute(SAF_ERROR_HANDLING, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {
		setDefaultNamespace("http://xmlns.oracle.com/weblogic/weblogic-jms");

	}

	// This attribute is optional
	public void setVersion(int value) {
		this.setValue(VERSION, java.lang.Integer.valueOf(value));
	}

	//
	public int getVersion() {
		Integer ret = (Integer)this.getValue(VERSION);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"VERSION", "int"}));
		return ((java.lang.Integer)ret).intValue();
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
	public void setQuota(int index, QuotaType value) {
		this.setValue(QUOTA, index, value);
	}

	//
	public QuotaType getQuota(int index) {
		return (QuotaType)this.getValue(QUOTA, index);
	}

	// Return the number of properties
	public int sizeQuota() {
		return this.size(QUOTA);
	}

	// This attribute is an array, possibly empty
	public void setQuota(QuotaType[] value) {
		this.setValue(QUOTA, value);
	}

	//
	public QuotaType[] getQuota() {
		return (QuotaType[])this.getValues(QUOTA);
	}

	// Add a new element returning its index in the list
	public int addQuota(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.QuotaType value) {
		int positionOfNewItem = this.addValue(QUOTA, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeQuota(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.QuotaType value) {
		return this.removeValue(QUOTA, value);
	}

	// This attribute is an array, possibly empty
	public void setTemplate(int index, TemplateType value) {
		this.setValue(TEMPLATE, index, value);
	}

	//
	public TemplateType getTemplate(int index) {
		return (TemplateType)this.getValue(TEMPLATE, index);
	}

	// Return the number of properties
	public int sizeTemplate() {
		return this.size(TEMPLATE);
	}

	// This attribute is an array, possibly empty
	public void setTemplate(TemplateType[] value) {
		this.setValue(TEMPLATE, value);
	}

	//
	public TemplateType[] getTemplate() {
		return (TemplateType[])this.getValues(TEMPLATE);
	}

	// Add a new element returning its index in the list
	public int addTemplate(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.TemplateType value) {
		int positionOfNewItem = this.addValue(TEMPLATE, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeTemplate(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.TemplateType value) {
		return this.removeValue(TEMPLATE, value);
	}

	// This attribute is an array, possibly empty
	public void setDestinationKey(int index, DestinationKeyType value) {
		this.setValue(DESTINATION_KEY, index, value);
	}

	//
	public DestinationKeyType getDestinationKey(int index) {
		return (DestinationKeyType)this.getValue(DESTINATION_KEY, index);
	}

	// Return the number of properties
	public int sizeDestinationKey() {
		return this.size(DESTINATION_KEY);
	}

	// This attribute is an array, possibly empty
	public void setDestinationKey(DestinationKeyType[] value) {
		this.setValue(DESTINATION_KEY, value);
	}

	//
	public DestinationKeyType[] getDestinationKey() {
		return (DestinationKeyType[])this.getValues(DESTINATION_KEY);
	}

	// Add a new element returning its index in the list
	public int addDestinationKey(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.DestinationKeyType value) {
		int positionOfNewItem = this.addValue(DESTINATION_KEY, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeDestinationKey(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.DestinationKeyType value) {
		return this.removeValue(DESTINATION_KEY, value);
	}

	// This attribute is an array, possibly empty
	public void setConnectionFactory(int index, JmsConnectionFactoryType value) {
		this.setValue(CONNECTION_FACTORY, index, value);
	}

	//
	public JmsConnectionFactoryType getConnectionFactory(int index) {
		return (JmsConnectionFactoryType)this.getValue(CONNECTION_FACTORY, index);
	}

	// Return the number of properties
	public int sizeConnectionFactory() {
		return this.size(CONNECTION_FACTORY);
	}

	// This attribute is an array, possibly empty
	public void setConnectionFactory(JmsConnectionFactoryType[] value) {
		this.setValue(CONNECTION_FACTORY, value);
	}

	//
	public JmsConnectionFactoryType[] getConnectionFactory() {
		return (JmsConnectionFactoryType[])this.getValues(CONNECTION_FACTORY);
	}

	// Add a new element returning its index in the list
	public int addConnectionFactory(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.JmsConnectionFactoryType value) {
		int positionOfNewItem = this.addValue(CONNECTION_FACTORY, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeConnectionFactory(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.JmsConnectionFactoryType value) {
		return this.removeValue(CONNECTION_FACTORY, value);
	}

	// This attribute is an array, possibly empty
	public void setForeignServer(int index, ForeignServerType value) {
		this.setValue(FOREIGN_SERVER, index, value);
	}

	//
	public ForeignServerType getForeignServer(int index) {
		return (ForeignServerType)this.getValue(FOREIGN_SERVER, index);
	}

	// Return the number of properties
	public int sizeForeignServer() {
		return this.size(FOREIGN_SERVER);
	}

	// This attribute is an array, possibly empty
	public void setForeignServer(ForeignServerType[] value) {
		this.setValue(FOREIGN_SERVER, value);
	}

	//
	public ForeignServerType[] getForeignServer() {
		return (ForeignServerType[])this.getValues(FOREIGN_SERVER);
	}

	// Add a new element returning its index in the list
	public int addForeignServer(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.ForeignServerType value) {
		int positionOfNewItem = this.addValue(FOREIGN_SERVER, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeForeignServer(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.ForeignServerType value) {
		return this.removeValue(FOREIGN_SERVER, value);
	}

	// This attribute is an array, possibly empty
	public void setQueue(int index, QueueType value) {
		this.setValue(QUEUE, index, value);
	}

	//
	public QueueType getQueue(int index) {
		return (QueueType)this.getValue(QUEUE, index);
	}

	// Return the number of properties
	public int sizeQueue() {
		return this.size(QUEUE);
	}

	// This attribute is an array, possibly empty
	public void setQueue(QueueType[] value) {
		this.setValue(QUEUE, value);
	}

	//
	public QueueType[] getQueue() {
		return (QueueType[])this.getValues(QUEUE);
	}

	// Add a new element returning its index in the list
	public int addQueue(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.QueueType value) {
		int positionOfNewItem = this.addValue(QUEUE, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeQueue(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.QueueType value) {
		return this.removeValue(QUEUE, value);
	}

	// This attribute is an array, possibly empty
	public void setTopic(int index, TopicType value) {
		this.setValue(TOPIC, index, value);
	}

	//
	public TopicType getTopic(int index) {
		return (TopicType)this.getValue(TOPIC, index);
	}

	// Return the number of properties
	public int sizeTopic() {
		return this.size(TOPIC);
	}

	// This attribute is an array, possibly empty
	public void setTopic(TopicType[] value) {
		this.setValue(TOPIC, value);
	}

	//
	public TopicType[] getTopic() {
		return (TopicType[])this.getValues(TOPIC);
	}

	// Add a new element returning its index in the list
	public int addTopic(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.TopicType value) {
		int positionOfNewItem = this.addValue(TOPIC, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeTopic(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.TopicType value) {
		return this.removeValue(TOPIC, value);
	}

	// This attribute is an array, possibly empty
	public void setDistributedQueue(int index, DistributedQueueType value) {
		this.setValue(DISTRIBUTED_QUEUE, index, value);
	}

	//
	public DistributedQueueType getDistributedQueue(int index) {
		return (DistributedQueueType)this.getValue(DISTRIBUTED_QUEUE, index);
	}

	// Return the number of properties
	public int sizeDistributedQueue() {
		return this.size(DISTRIBUTED_QUEUE);
	}

	// This attribute is an array, possibly empty
	public void setDistributedQueue(DistributedQueueType[] value) {
		this.setValue(DISTRIBUTED_QUEUE, value);
	}

	//
	public DistributedQueueType[] getDistributedQueue() {
		return (DistributedQueueType[])this.getValues(DISTRIBUTED_QUEUE);
	}

	// Add a new element returning its index in the list
	public int addDistributedQueue(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.DistributedQueueType value) {
		int positionOfNewItem = this.addValue(DISTRIBUTED_QUEUE, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeDistributedQueue(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.DistributedQueueType value) {
		return this.removeValue(DISTRIBUTED_QUEUE, value);
	}

	// This attribute is an array, possibly empty
	public void setDistributedTopic(int index, DistributedTopicType value) {
		this.setValue(DISTRIBUTED_TOPIC, index, value);
	}

	//
	public DistributedTopicType getDistributedTopic(int index) {
		return (DistributedTopicType)this.getValue(DISTRIBUTED_TOPIC, index);
	}

	// Return the number of properties
	public int sizeDistributedTopic() {
		return this.size(DISTRIBUTED_TOPIC);
	}

	// This attribute is an array, possibly empty
	public void setDistributedTopic(DistributedTopicType[] value) {
		this.setValue(DISTRIBUTED_TOPIC, value);
	}

	//
	public DistributedTopicType[] getDistributedTopic() {
		return (DistributedTopicType[])this.getValues(DISTRIBUTED_TOPIC);
	}

	// Add a new element returning its index in the list
	public int addDistributedTopic(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.DistributedTopicType value) {
		int positionOfNewItem = this.addValue(DISTRIBUTED_TOPIC, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeDistributedTopic(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.DistributedTopicType value) {
		return this.removeValue(DISTRIBUTED_TOPIC, value);
	}

	// This attribute is an array, possibly empty
	public void setUniformDistributedQueue(int index, UniformDistributedQueueType value) {
		this.setValue(UNIFORM_DISTRIBUTED_QUEUE, index, value);
	}

	//
	public UniformDistributedQueueType getUniformDistributedQueue(int index) {
		return (UniformDistributedQueueType)this.getValue(UNIFORM_DISTRIBUTED_QUEUE, index);
	}

	// Return the number of properties
	public int sizeUniformDistributedQueue() {
		return this.size(UNIFORM_DISTRIBUTED_QUEUE);
	}

	// This attribute is an array, possibly empty
	public void setUniformDistributedQueue(UniformDistributedQueueType[] value) {
		this.setValue(UNIFORM_DISTRIBUTED_QUEUE, value);
	}

	//
	public UniformDistributedQueueType[] getUniformDistributedQueue() {
		return (UniformDistributedQueueType[])this.getValues(UNIFORM_DISTRIBUTED_QUEUE);
	}

	// Add a new element returning its index in the list
	public int addUniformDistributedQueue(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.UniformDistributedQueueType value) {
		int positionOfNewItem = this.addValue(UNIFORM_DISTRIBUTED_QUEUE, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeUniformDistributedQueue(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.UniformDistributedQueueType value) {
		return this.removeValue(UNIFORM_DISTRIBUTED_QUEUE, value);
	}

	// This attribute is an array, possibly empty
	public void setUniformDistributedTopic(int index, UniformDistributedTopicType value) {
		this.setValue(UNIFORM_DISTRIBUTED_TOPIC, index, value);
	}

	//
	public UniformDistributedTopicType getUniformDistributedTopic(int index) {
		return (UniformDistributedTopicType)this.getValue(UNIFORM_DISTRIBUTED_TOPIC, index);
	}

	// Return the number of properties
	public int sizeUniformDistributedTopic() {
		return this.size(UNIFORM_DISTRIBUTED_TOPIC);
	}

	// This attribute is an array, possibly empty
	public void setUniformDistributedTopic(UniformDistributedTopicType[] value) {
		this.setValue(UNIFORM_DISTRIBUTED_TOPIC, value);
	}

	//
	public UniformDistributedTopicType[] getUniformDistributedTopic() {
		return (UniformDistributedTopicType[])this.getValues(UNIFORM_DISTRIBUTED_TOPIC);
	}

	// Add a new element returning its index in the list
	public int addUniformDistributedTopic(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.UniformDistributedTopicType value) {
		int positionOfNewItem = this.addValue(UNIFORM_DISTRIBUTED_TOPIC, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeUniformDistributedTopic(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.UniformDistributedTopicType value) {
		return this.removeValue(UNIFORM_DISTRIBUTED_TOPIC, value);
	}

	// This attribute is an array, possibly empty
	public void setSafImportedDestinations(int index, SafImportedDestinationsType value) {
		this.setValue(SAF_IMPORTED_DESTINATIONS, index, value);
	}

	//
	public SafImportedDestinationsType getSafImportedDestinations(int index) {
		return (SafImportedDestinationsType)this.getValue(SAF_IMPORTED_DESTINATIONS, index);
	}

	// Return the number of properties
	public int sizeSafImportedDestinations() {
		return this.size(SAF_IMPORTED_DESTINATIONS);
	}

	// This attribute is an array, possibly empty
	public void setSafImportedDestinations(SafImportedDestinationsType[] value) {
		this.setValue(SAF_IMPORTED_DESTINATIONS, value);
	}

	//
	public SafImportedDestinationsType[] getSafImportedDestinations() {
		return (SafImportedDestinationsType[])this.getValues(SAF_IMPORTED_DESTINATIONS);
	}

	// Add a new element returning its index in the list
	public int addSafImportedDestinations(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.SafImportedDestinationsType value) {
		int positionOfNewItem = this.addValue(SAF_IMPORTED_DESTINATIONS, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSafImportedDestinations(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.SafImportedDestinationsType value) {
		return this.removeValue(SAF_IMPORTED_DESTINATIONS, value);
	}

	// This attribute is an array, possibly empty
	public void setSafRemoteContext(int index, SafRemoteContextType value) {
		this.setValue(SAF_REMOTE_CONTEXT, index, value);
	}

	//
	public SafRemoteContextType getSafRemoteContext(int index) {
		return (SafRemoteContextType)this.getValue(SAF_REMOTE_CONTEXT, index);
	}

	// Return the number of properties
	public int sizeSafRemoteContext() {
		return this.size(SAF_REMOTE_CONTEXT);
	}

	// This attribute is an array, possibly empty
	public void setSafRemoteContext(SafRemoteContextType[] value) {
		this.setValue(SAF_REMOTE_CONTEXT, value);
	}

	//
	public SafRemoteContextType[] getSafRemoteContext() {
		return (SafRemoteContextType[])this.getValues(SAF_REMOTE_CONTEXT);
	}

	// Add a new element returning its index in the list
	public int addSafRemoteContext(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.SafRemoteContextType value) {
		int positionOfNewItem = this.addValue(SAF_REMOTE_CONTEXT, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSafRemoteContext(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.SafRemoteContextType value) {
		return this.removeValue(SAF_REMOTE_CONTEXT, value);
	}

	// This attribute is an array, possibly empty
	public void setSafErrorHandling(int index, SafErrorHandlingType value) {
		this.setValue(SAF_ERROR_HANDLING, index, value);
	}

	//
	public SafErrorHandlingType getSafErrorHandling(int index) {
		return (SafErrorHandlingType)this.getValue(SAF_ERROR_HANDLING, index);
	}

	// Return the number of properties
	public int sizeSafErrorHandling() {
		return this.size(SAF_ERROR_HANDLING);
	}

	// This attribute is an array, possibly empty
	public void setSafErrorHandling(SafErrorHandlingType[] value) {
		this.setValue(SAF_ERROR_HANDLING, value);
	}

	//
	public SafErrorHandlingType[] getSafErrorHandling() {
		return (SafErrorHandlingType[])this.getValues(SAF_ERROR_HANDLING);
	}

	// Add a new element returning its index in the list
	public int addSafErrorHandling(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.SafErrorHandlingType value) {
		int positionOfNewItem = this.addValue(SAF_ERROR_HANDLING, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSafErrorHandling(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.SafErrorHandlingType value) {
		return this.removeValue(SAF_ERROR_HANDLING, value);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public QuotaType newQuotaType() {
		return new QuotaType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public TemplateType newTemplateType() {
		return new TemplateType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public DestinationKeyType newDestinationKeyType() {
		return new DestinationKeyType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public JmsConnectionFactoryType newJmsConnectionFactoryType() {
		return new JmsConnectionFactoryType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ForeignServerType newForeignServerType() {
		return new ForeignServerType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public QueueType newQueueType() {
		return new QueueType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public TopicType newTopicType() {
		return new TopicType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public DistributedQueueType newDistributedQueueType() {
		return new DistributedQueueType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public DistributedTopicType newDistributedTopicType() {
		return new DistributedTopicType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public UniformDistributedQueueType newUniformDistributedQueueType() {
		return new UniformDistributedQueueType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public UniformDistributedTopicType newUniformDistributedTopicType() {
		return new UniformDistributedTopicType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SafImportedDestinationsType newSafImportedDestinationsType() {
		return new SafImportedDestinationsType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SafRemoteContextType newSafRemoteContextType() {
		return new SafRemoteContextType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SafErrorHandlingType newSafErrorHandlingType() {
		return new SafErrorHandlingType();
	}

	//
	public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.add(c);
	}

	//
	public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.remove(c);
	}
	//
	// This method returns the root of the bean graph
	// Each call creates a new bean graph from the specified DOM graph
	//
	public static WeblogicJms createGraph(org.w3c.dom.Node doc) {
		return new WeblogicJms(doc, Common.NO_DEFAULT_VALUES);
	}

	public static WeblogicJms createGraph(java.io.File f) throws java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return createGraph(in, false);
		} finally {
			in.close();
		}
	}

	public static WeblogicJms createGraph(java.io.InputStream in) {
		return createGraph(in, false);
	}

	public static WeblogicJms createGraph(java.io.InputStream in, boolean validate) {
		try {
			Document doc = GraphManager.createXmlDocument(in, validate);
			return createGraph(doc);
		}
		catch (Exception t) {
			throw new RuntimeException(Common.getMessage(
				"DOMGraphCreateFailed_msg",
				t));
		}
	}

	//
	// This method returns the root for a new empty bean graph
	//
	public static WeblogicJms createGraph() {
		return new WeblogicJms();
	}


	
                    public String getId() {
                        return null;
                    }

                    public void setId(String id) {
                       // noop
                    }

                    public QueueType addQueue() {
                        QueueType bean = new QueueType();
                        addQueue(bean);
                        return bean;
                    }

                    public TopicType addTopic() {
                        TopicType bean = new TopicType();
                        addTopic(bean);
                        return bean;
                    }
                
	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
		boolean restrictionFailure = false;
		boolean restrictionPassed = false;
		// Validating property version
		// Validating property notes
		// Validating property quota
		for (int _index = 0; _index < sizeQuota(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1031.QuotaType element = getQuota(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property template
		for (int _index = 0; _index < sizeTemplate(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1031.TemplateType element = getTemplate(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property destinationKey
		for (int _index = 0; _index < sizeDestinationKey(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1031.DestinationKeyType element = getDestinationKey(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property connectionFactory
		for (int _index = 0; _index < sizeConnectionFactory(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1031.JmsConnectionFactoryType element = getConnectionFactory(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property foreignServer
		for (int _index = 0; _index < sizeForeignServer(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1031.ForeignServerType element = getForeignServer(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property queue
		for (int _index = 0; _index < sizeQueue(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1031.QueueType element = getQueue(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property topic
		for (int _index = 0; _index < sizeTopic(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1031.TopicType element = getTopic(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property distributedQueue
		for (int _index = 0; _index < sizeDistributedQueue(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1031.DistributedQueueType element = getDistributedQueue(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property distributedTopic
		for (int _index = 0; _index < sizeDistributedTopic(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1031.DistributedTopicType element = getDistributedTopic(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property uniformDistributedQueue
		for (int _index = 0; _index < sizeUniformDistributedQueue(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1031.UniformDistributedQueueType element = getUniformDistributedQueue(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property uniformDistributedTopic
		for (int _index = 0; _index < sizeUniformDistributedTopic(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1031.UniformDistributedTopicType element = getUniformDistributedTopic(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property safImportedDestinations
		for (int _index = 0; _index < sizeSafImportedDestinations(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1031.SafImportedDestinationsType element = getSafImportedDestinations(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property safRemoteContext
		for (int _index = 0; _index < sizeSafRemoteContext(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1031.SafRemoteContextType element = getSafRemoteContext(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property safErrorHandling
		for (int _index = 0; _index < sizeSafErrorHandling(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.jms1031.SafErrorHandlingType element = getSafErrorHandling(_index);
			if (element != null) {
				element.validate();
			}
		}
	}

	// Special serializer: output XML as serialization
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException{
		out.defaultWriteObject();
		final int MAX_SIZE = 0XFFFF;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try{
			write(baos, SERIALIZATION_HELPER_CHARSET);
			final byte [] array = baos.toByteArray();
			final int numStrings = array.length / MAX_SIZE;
			final int leftover = array.length % MAX_SIZE;
			out.writeInt(numStrings + (0 == leftover ? 0 : 1));
			out.writeInt(MAX_SIZE);
			int offset = 0;
			for (int i = 0; i < numStrings; i++){
				out.writeUTF(new String(array, offset, MAX_SIZE, SERIALIZATION_HELPER_CHARSET));
				offset += MAX_SIZE;
			}
			if (leftover > 0){
				final int count = array.length - offset;
				out.writeUTF(new String(array, offset, count, SERIALIZATION_HELPER_CHARSET));
			}
		}
		catch (Schema2BeansException ex){
			throw new Schema2BeansRuntimeException(ex);
		}
	}
	// Special deserializer: read XML as deserialization
	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		try{
			in.defaultReadObject();
			init(comparators, runtimeVersion);
			// init(comparators, new GenBeans.Version(1, 0, 8))
			final int numStrings = in.readInt();
			final int max_size = in.readInt();
			final StringBuilder sb = new StringBuilder(numStrings * max_size);
			for (int i = 0; i < numStrings; i++){
				sb.append(in.readUTF());
			}
			ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes(SERIALIZATION_HELPER_CHARSET));
			Document doc = GraphManager.createXmlDocument(bais, false);
			initOptions(Common.NO_DEFAULT_VALUES);
			initFromNode(doc, Common.NO_DEFAULT_VALUES);
		}
		catch (Schema2BeansException e){
			throw new RuntimeException(e);
		}
	}

	public void _setSchemaLocation(String location) {
		if (beanProp().getAttrProp("xsi:schemaLocation", true) == null) {
			createAttribute("xmlns:xsi", "xmlns:xsi", AttrProp.CDATA | AttrProp.IMPLIED, null, "http://www.w3.org/2001/XMLSchema-instance");
			setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			createAttribute("xsi:schemaLocation", "xsi:schemaLocation", AttrProp.CDATA | AttrProp.IMPLIED, null, location);
		}
		setAttributeValue("xsi:schemaLocation", location);
	}

	public String _getSchemaLocation() {
		if (beanProp().getAttrProp("xsi:schemaLocation", true) == null) {
			createAttribute("xmlns:xsi", "xmlns:xsi", AttrProp.CDATA | AttrProp.IMPLIED, null, "http://www.w3.org/2001/XMLSchema-instance");
			setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			createAttribute("xsi:schemaLocation", "xsi:schemaLocation", AttrProp.CDATA | AttrProp.IMPLIED, null, null);
		}
		return getAttributeValue("xsi:schemaLocation");
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		if (this.getValue(VERSION) != null) {
			str.append(indent);
			str.append("Version");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getVersion());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(VERSION, 0, str, indent);
		}

		str.append(indent);
		str.append("Notes");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getNotes();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NOTES, 0, str, indent);

		str.append(indent);
		str.append("Quota["+this.sizeQuota()+"]");	// NOI18N
		for(int i=0; i<this.sizeQuota(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getQuota(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(QUOTA, i, str, indent);
		}

		str.append(indent);
		str.append("Template["+this.sizeTemplate()+"]");	// NOI18N
		for(int i=0; i<this.sizeTemplate(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getTemplate(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(TEMPLATE, i, str, indent);
		}

		str.append(indent);
		str.append("DestinationKey["+this.sizeDestinationKey()+"]");	// NOI18N
		for(int i=0; i<this.sizeDestinationKey(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getDestinationKey(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(DESTINATION_KEY, i, str, indent);
		}

		str.append(indent);
		str.append("ConnectionFactory["+this.sizeConnectionFactory()+"]");	// NOI18N
		for(int i=0; i<this.sizeConnectionFactory(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getConnectionFactory(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CONNECTION_FACTORY, i, str, indent);
		}

		str.append(indent);
		str.append("ForeignServer["+this.sizeForeignServer()+"]");	// NOI18N
		for(int i=0; i<this.sizeForeignServer(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getForeignServer(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(FOREIGN_SERVER, i, str, indent);
		}

		str.append(indent);
		str.append("Queue["+this.sizeQueue()+"]");	// NOI18N
		for(int i=0; i<this.sizeQueue(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getQueue(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(QUEUE, i, str, indent);
		}

		str.append(indent);
		str.append("Topic["+this.sizeTopic()+"]");	// NOI18N
		for(int i=0; i<this.sizeTopic(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getTopic(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(TOPIC, i, str, indent);
		}

		str.append(indent);
		str.append("DistributedQueue["+this.sizeDistributedQueue()+"]");	// NOI18N
		for(int i=0; i<this.sizeDistributedQueue(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getDistributedQueue(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(DISTRIBUTED_QUEUE, i, str, indent);
		}

		str.append(indent);
		str.append("DistributedTopic["+this.sizeDistributedTopic()+"]");	// NOI18N
		for(int i=0; i<this.sizeDistributedTopic(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getDistributedTopic(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(DISTRIBUTED_TOPIC, i, str, indent);
		}

		str.append(indent);
		str.append("UniformDistributedQueue["+this.sizeUniformDistributedQueue()+"]");	// NOI18N
		for(int i=0; i<this.sizeUniformDistributedQueue(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getUniformDistributedQueue(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(UNIFORM_DISTRIBUTED_QUEUE, i, str, indent);
		}

		str.append(indent);
		str.append("UniformDistributedTopic["+this.sizeUniformDistributedTopic()+"]");	// NOI18N
		for(int i=0; i<this.sizeUniformDistributedTopic(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getUniformDistributedTopic(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(UNIFORM_DISTRIBUTED_TOPIC, i, str, indent);
		}

		str.append(indent);
		str.append("SafImportedDestinations["+this.sizeSafImportedDestinations()+"]");	// NOI18N
		for(int i=0; i<this.sizeSafImportedDestinations(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getSafImportedDestinations(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SAF_IMPORTED_DESTINATIONS, i, str, indent);
		}

		str.append(indent);
		str.append("SafRemoteContext["+this.sizeSafRemoteContext()+"]");	// NOI18N
		for(int i=0; i<this.sizeSafRemoteContext(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getSafRemoteContext(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SAF_REMOTE_CONTEXT, i, str, indent);
		}

		str.append(indent);
		str.append("SafErrorHandling["+this.sizeSafErrorHandling()+"]");	// NOI18N
		for(int i=0; i<this.sizeSafErrorHandling(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getSafErrorHandling(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SAF_ERROR_HANDLING, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("WeblogicJms\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

