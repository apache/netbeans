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
 *	This generated bean class WeblogicEjbJar matches the schema element 'weblogic-ejb-jar'.
 *
 *	Generated on Tue Jul 25 03:26:51 PDT 2017
 *
 *	This class matches the root element of the XML Schema,
 *	and is the root of the following bean graph:
 *
 *	weblogicEjbJar <weblogic-ejb-jar> : WeblogicEjbJar
 *		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		[attr: version CDATA #IMPLIED  : java.lang.String]
 *		description <description> : java.lang.String[0,1]
 *			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			[attr: xml:lang CDATA #IMPLIED  : java.lang.String]
 *		weblogicEnterpriseBean <weblogic-enterprise-bean> : WeblogicEnterpriseBeanType[0,n]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			ejbName <ejb-name> : java.lang.String
 *			(
 *			  | entityDescriptor <entity-descriptor> : EntityDescriptorType
 *			  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	pool <pool> : PoolType[0,1]
 *			  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		maxBeansInFreePool <max-beans-in-free-pool> : long[0,1] 	[minInclusive (0)]
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		initialBeansInFreePool <initial-beans-in-free-pool> : long[0,1] 	[minInclusive (0)]
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		idleTimeoutSeconds <idle-timeout-seconds> : long[0,1] 	[minInclusive (0)]
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	timerDescriptor <timer-descriptor> : TimerDescriptorType[0,1]
 *			  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		persistentStoreLogicalName <persistent-store-logical-name> : java.lang.String[0,1]
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	(
 *			  | 	  | entityCache <entity-cache> : EntityCacheType
 *			  | 	  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	  | 	maxBeansInCache <max-beans-in-cache> : long[0,1] 	[minInclusive (0)]
 *			  | 	  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	  | 	maxQueriesInCache <max-queries-in-cache> : java.math.BigInteger[0,1]
 *			  | 	  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	  | 	idleTimeoutSeconds <idle-timeout-seconds> : long[0,1] 	[minInclusive (0)]
 *			  | 	  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	  | 	readTimeoutSeconds <read-timeout-seconds> : long[0,1] 	[minInclusive (0)]
 *			  | 	  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	  | 	concurrencyStrategy <concurrency-strategy> : java.lang.String[0,1]
 *			  | 	  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	  | 	cacheBetweenTransactions <cache-between-transactions> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | 	  | 	disableReadyInstances <disable-ready-instances> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | 	  | entityCacheRef <entity-cache-ref> : EntityCacheRefType
 *			  | 	  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	  | 	entityCacheName <entity-cache-name> : java.lang.String
 *			  | 	  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	  | 	idleTimeoutSeconds <idle-timeout-seconds> : long[0,1] 	[minInclusive (0)]
 *			  | 	  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	  | 	readTimeoutSeconds <read-timeout-seconds> : long[0,1] 	[minInclusive (0)]
 *			  | 	  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	  | 	concurrencyStrategy <concurrency-strategy> : java.lang.String[0,1]
 *			  | 	  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	  | 	cacheBetweenTransactions <cache-between-transactions> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | 	  | 	estimatedBeanSize <estimated-bean-size> : long[0,1] 	[minExclusive (0)]
 *			  | 	  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	)[0,1]
 *			  | 	persistence <persistence> : PersistenceType[0,1]
 *			  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		isModifiedMethodName <is-modified-method-name> : java.lang.String[0,1]
 *			  | 			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		delayUpdatesUntilEndOfTx <delay-updates-until-end-of-tx> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | 		findersLoadBean <finders-load-bean> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | 		persistenceUse <persistence-use> : PersistenceUseType[0,1]
 *			  | 			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 			typeIdentifier <type-identifier> : java.lang.String
 *			  | 				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 			typeVersion <type-version> : java.lang.String
 *			  | 				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 			typeStorage <type-storage> : java.lang.String
 *			  | 				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	entityClustering <entity-clustering> : EntityClusteringType[0,1]
 *			  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		homeIsClusterable <home-is-clusterable> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | 		homeLoadAlgorithm <home-load-algorithm> : java.lang.String[0,1]
 *			  | 			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		homeCallRouterClassName <home-call-router-class-name> : java.lang.String[0,1]
 *			  | 			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		useServersideStubs <use-serverside-stubs> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | 	invalidationTarget <invalidation-target> : InvalidationTargetType[0,1]
 *			  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		ejbName <ejb-name> : java.lang.String
 *			  | 	enableDynamicQueries <enable-dynamic-queries> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | statelessSessionDescriptor <stateless-session-descriptor> : StatelessSessionDescriptorType
 *			  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	pool <pool> : PoolType[0,1]
 *			  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		maxBeansInFreePool <max-beans-in-free-pool> : long[0,1] 	[minInclusive (0)]
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		initialBeansInFreePool <initial-beans-in-free-pool> : long[0,1] 	[minInclusive (0)]
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		idleTimeoutSeconds <idle-timeout-seconds> : long[0,1] 	[minInclusive (0)]
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	timerDescriptor <timer-descriptor> : TimerDescriptorType[0,1]
 *			  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		persistentStoreLogicalName <persistent-store-logical-name> : java.lang.String[0,1]
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	statelessClustering <stateless-clustering> : StatelessClusteringType[0,1]
 *			  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		homeIsClusterable <home-is-clusterable> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | 		homeLoadAlgorithm <home-load-algorithm> : java.lang.String[0,1]
 *			  | 			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		homeCallRouterClassName <home-call-router-class-name> : java.lang.String[0,1]
 *			  | 			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		useServersideStubs <use-serverside-stubs> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | 		statelessBeanIsClusterable <stateless-bean-is-clusterable> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | 		statelessBeanLoadAlgorithm <stateless-bean-load-algorithm> : java.lang.String[0,1]
 *			  | 			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		statelessBeanCallRouterClassName <stateless-bean-call-router-class-name> : java.lang.String[0,1]
 *			  | 			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	businessInterfaceJndiNameMap <business-interface-jndi-name-map> : BusinessInterfaceJndiNameMapType[0,n]
 *			  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		businessRemote <business-remote> : java.lang.String
 *			  | 		jndiName <jndi-name> : java.lang.String
 *			  | statefulSessionDescriptor <stateful-session-descriptor> : StatefulSessionDescriptorType
 *			  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	statefulSessionCache <stateful-session-cache> : StatefulSessionCacheType[0,1]
 *			  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		maxBeansInCache <max-beans-in-cache> : long[0,1] 	[minInclusive (0)]
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		idleTimeoutSeconds <idle-timeout-seconds> : long[0,1] 	[minInclusive (0)]
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		sessionTimeoutSeconds <session-timeout-seconds> : long[0,1] 	[minInclusive (0)]
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		cacheType <cache-type> : java.lang.String[0,1]
 *			  | 			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	persistentStoreDir <persistent-store-dir> : java.lang.String[0,1]
 *			  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	statefulSessionClustering <stateful-session-clustering> : StatefulSessionClusteringType[0,1]
 *			  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		homeIsClusterable <home-is-clusterable> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | 		homeLoadAlgorithm <home-load-algorithm> : java.lang.String[0,1]
 *			  | 			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		homeCallRouterClassName <home-call-router-class-name> : java.lang.String[0,1]
 *			  | 			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		useServersideStubs <use-serverside-stubs> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | 		replicationType <replication-type> : java.lang.String[0,1]
 *			  | 			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		passivateDuringReplication <passivate-during-replication> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | 		calculateDeltaUsingReflection <calculate-delta-using-reflection> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | 	allowConcurrentCalls <allow-concurrent-calls> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | 	allowRemoveDuringTransaction <allow-remove-during-transaction> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | 	businessInterfaceJndiNameMap <business-interface-jndi-name-map> : BusinessInterfaceJndiNameMapType[0,n]
 *			  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		businessRemote <business-remote> : java.lang.String
 *			  | 		jndiName <jndi-name> : java.lang.String
 *			  | messageDrivenDescriptor <message-driven-descriptor> : MessageDrivenDescriptorType
 *			  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	pool <pool> : PoolType[0,1]
 *			  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		maxBeansInFreePool <max-beans-in-free-pool> : long[0,1] 	[minInclusive (0)]
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		initialBeansInFreePool <initial-beans-in-free-pool> : long[0,1] 	[minInclusive (0)]
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		idleTimeoutSeconds <idle-timeout-seconds> : long[0,1] 	[minInclusive (0)]
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	timerDescriptor <timer-descriptor> : TimerDescriptorType[0,1]
 *			  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		persistentStoreLogicalName <persistent-store-logical-name> : java.lang.String[0,1]
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	| resourceAdapterJndiName <resource-adapter-jndi-name> : java.lang.String[0,1]
 *			  | 	| 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	| | destinationJndiName <destination-jndi-name> : java.lang.String[0,1]
 *			  | 	| | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	| | initialContextFactory <initial-context-factory> : java.lang.String[0,1]
 *			  | 	| | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	| | providerUrl <provider-url> : java.lang.String[0,1]
 *			  | 	| | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	| | connectionFactoryJndiName <connection-factory-jndi-name> : java.lang.String[0,1]
 *			  | 	| | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	| | destinationResourceLink <destination-resource-link> : java.lang.String[0,1] 	[whiteSpace (collapse)]
 *			  | 	| | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	| | connectionFactoryResourceLink <connection-factory-resource-link> : java.lang.String[0,1] 	[whiteSpace (collapse)]
 *			  | 	| | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	| jmsPollingIntervalSeconds <jms-polling-interval-seconds> : long[0,1] 	[minInclusive (0)]
 *			  | 	| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	| jmsClientId <jms-client-id> : java.lang.String[0,1]
 *			  | 	| 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	| generateUniqueJmsClientId <generate-unique-jms-client-id> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | 	| durableSubscriptionDeletion <durable-subscription-deletion> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | 	| maxMessagesInTransaction <max-messages-in-transaction> : long[0,1] 	[minInclusive (0)]
 *			  | 	| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	| distributedDestinationConnection <distributed-destination-connection> : java.lang.String[0,1]
 *			  | 	| 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	| use81StylePolling <use81-style-polling> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			  | 	initSuspendSeconds <init-suspend-seconds> : long[0,1] 	[minInclusive (0)]
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	maxSuspendSeconds <max-suspend-seconds> : long[0,1] 	[minInclusive (0)]
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	securityPlugin <security-plugin> : SecurityPluginType[0,1]
 *			  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		pluginClass <plugin-class> : java.lang.String
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		key <key> : java.lang.String
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			)[0,1]
 *			transactionDescriptor <transaction-descriptor> : TransactionDescriptorType[0,1]
 *				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				transTimeoutSeconds <trans-timeout-seconds> : long[0,1] 	[minInclusive (0)]
 *					[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			iiopSecurityDescriptor <iiop-security-descriptor> : IiopSecurityDescriptorType[0,1]
 *				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				transportRequirements <transport-requirements> : TransportRequirementsType[0,1]
 *					[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					integrity <integrity> : java.lang.String[0,1]
 *						[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					confidentiality <confidentiality> : java.lang.String[0,1]
 *						[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					clientCertAuthentication <client-cert-authentication> : java.lang.String[0,1]
 *						[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				clientAuthentication <client-authentication> : java.lang.String[0,1]
 *					[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				identityAssertion <identity-assertion> : java.lang.String[0,1]
 *					[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			resourceDescription <resource-description> : ResourceDescriptionType[0,n]
 *				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				resRefName <res-ref-name> : java.lang.String
 *				| jndiName <jndi-name> : java.lang.String 	[whiteSpace (collapse)]
 *				| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				| resourceLink <resource-link> : java.lang.String 	[whiteSpace (collapse)]
 *				| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			resourceEnvDescription <resource-env-description> : ResourceEnvDescriptionType[0,n]
 *				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				resourceEnvRefName <resource-env-ref-name> : java.lang.String
 *				| jndiName <jndi-name> : java.lang.String 	[whiteSpace (collapse)]
 *				| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				| resourceLink <resource-link> : java.lang.String 	[whiteSpace (collapse)]
 *				| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			ejbReferenceDescription <ejb-reference-description> : EjbReferenceDescriptionType[0,n]
 *				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				ejbRefName <ejb-ref-name> : java.lang.String
 *				jndiName <jndi-name> : java.lang.String
 *			serviceReferenceDescription <service-reference-description> : ServiceReferenceDescriptionType[0,n]
 *				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				serviceRefName <service-ref-name> : java.lang.String
 *				wsdlUrl <wsdl-url> : java.lang.String[0,1] 	[whiteSpace (collapse)]
 *					[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				callProperty <call-property> : PropertyNamevalueType[0,n]
 *					name <name> : java.lang.String 	[whiteSpace (collapse)]
 *						[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					value <value> : java.lang.String 	[whiteSpace (collapse)]
 *						[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				portInfo <port-info> : PortInfoType[0,n]
 *					portName <port-name> : java.lang.String 	[whiteSpace (collapse)]
 *						[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					stubProperty <stub-property> : PropertyNamevalueType[0,n]
 *						name <name> : java.lang.String 	[whiteSpace (collapse)]
 *							[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *						value <value> : java.lang.String 	[whiteSpace (collapse)]
 *							[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					callProperty <call-property> : PropertyNamevalueType[0,n]
 *						name <name> : java.lang.String 	[whiteSpace (collapse)]
 *							[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *						value <value> : java.lang.String 	[whiteSpace (collapse)]
 *							[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					wsatConfig <wsat-config> : WsatConfigType[0,1]
 *						enabled <enabled> : boolean[0,1]
 *						flowType <flow-type> : java.lang.String[0,1] 	[enumeration (SUPPORTS), enumeration (MANDATORY), enumeration (NEVER)]
 *						version <version> : java.lang.String[0,1] 	[enumeration (DEFAULT), enumeration (WSAT10), enumeration (WSAT11), enumeration (WSAT12)]
 *					owsmPolicy <owsm-policy> : OwsmPolicyType[0,n]
 *						uri <uri> : java.lang.String 	[whiteSpace (collapse)]
 *							[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *						status <status> : java.lang.String[0,1] 	[whiteSpace (collapse)]
 *							[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *						category <category> : java.lang.String 	[whiteSpace (collapse)]
 *							[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *						securityConfigurationProperty <security-configuration-property > : PropertyNamevalueType[0,n]
 *							name <name> : java.lang.String 	[whiteSpace (collapse)]
 *								[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *							value <value> : java.lang.String 	[whiteSpace (collapse)]
 *								[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					operation <operation> : OperationInfoType[0,n]
 *						name <name> : java.lang.String 	[whiteSpace (collapse)]
 *							[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *						wsatConfig <wsat-config> : WsatConfigType[0,1]
 *							enabled <enabled> : boolean[0,1]
 *							flowType <flow-type> : java.lang.String[0,1] 	[enumeration (SUPPORTS), enumeration (MANDATORY), enumeration (NEVER)]
 *							version <version> : java.lang.String[0,1] 	[enumeration (DEFAULT), enumeration (WSAT10), enumeration (WSAT11), enumeration (WSAT12)]
 *			enableCallByReference <enable-call-by-reference> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			networkAccessPoint <network-access-point> : java.lang.String[0,1]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			clientsOnSameServer <clients-on-same-server> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			runAsPrincipalName <run-as-principal-name> : java.lang.String[0,1]
 *				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			createAsPrincipalName <create-as-principal-name> : java.lang.String[0,1]
 *				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			removeAsPrincipalName <remove-as-principal-name> : java.lang.String[0,1]
 *				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			passivateAsPrincipalName <passivate-as-principal-name> : java.lang.String[0,1]
 *				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			jndiName <jndi-name> : java.lang.String[0,1]
 *			localJndiName <local-jndi-name> : java.lang.String[0,1]
 *			dispatchPolicy <dispatch-policy> : java.lang.String[0,1]
 *				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			remoteClientTimeout <remote-client-timeout> : long[0,1] 	[minInclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			stickToFirstServer <stick-to-first-server> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		securityRoleAssignment <security-role-assignment> : SecurityRoleAssignmentType[0,n]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			roleName <role-name> : java.lang.String
 *			| principalName <principal-name> : java.lang.String[1,n] 	[whiteSpace (collapse)]
 *			| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			| externallyDefined <externally-defined> : EmptyType
 *			| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		runAsRoleAssignment <run-as-role-assignment> : RunAsRoleAssignmentType[0,n]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			roleName <role-name> : java.lang.String
 *			runAsPrincipalName <run-as-principal-name> : java.lang.String 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		securityPermission <security-permission> : SecurityPermissionType[0,1]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			description <description> : java.lang.String[0,1]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: xml:lang CDATA #IMPLIED  : java.lang.String]
 *			securityPermissionSpec <security-permission-spec> : java.lang.String 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		transactionIsolation <transaction-isolation> : TransactionIsolationType[0,n]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			isolationLevel <isolation-level> : java.lang.String
 *				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			method <method> : MethodType[1,n]
 *				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				description <description> : java.lang.String[0,1]
 *					[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					[attr: xml:lang CDATA #IMPLIED  : java.lang.String]
 *				ejbName <ejb-name> : java.lang.String
 *				methodIntf <method-intf> : java.lang.String[0,1] 	[enumeration (Home), enumeration (Remote), enumeration (LocalHome), enumeration (Local), enumeration (ServiceEndpoint)]
 *				methodName <method-name> : java.lang.String
 *				methodParams <method-params> : MethodParamsType[0,1]
 *					[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					methodParam <method-param> : java.lang.String[0,n] 	[pattern ([^\p{Z}]*)]
 *		messageDestinationDescriptor <message-destination-descriptor> : MessageDestinationDescriptorType[0,n]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			messageDestinationName <message-destination-name> : java.lang.String
 *				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			| destinationJndiName <destination-jndi-name> : java.lang.String
 *			| 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			| initialContextFactory <initial-context-factory> : java.lang.String[0,1]
 *			| 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			| providerUrl <provider-url> : java.lang.String[0,1]
 *			| 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			| destinationResourceLink <destination-resource-link> : java.lang.String 	[whiteSpace (collapse)]
 *			| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		idempotentMethods <idempotent-methods> : IdempotentMethodsType[0,1]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			method <method> : MethodType[0,n]
 *				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				description <description> : java.lang.String[0,1]
 *					[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					[attr: xml:lang CDATA #IMPLIED  : java.lang.String]
 *				ejbName <ejb-name> : java.lang.String
 *				methodIntf <method-intf> : java.lang.String[0,1] 	[enumeration (Home), enumeration (Remote), enumeration (LocalHome), enumeration (Local), enumeration (ServiceEndpoint)]
 *				methodName <method-name> : java.lang.String
 *				methodParams <method-params> : MethodParamsType[0,1]
 *					[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					methodParam <method-param> : java.lang.String[0,n] 	[pattern ([^\p{Z}]*)]
 *		retryMethodsOnRollback <retry-methods-on-rollback> : RetryMethodsOnRollbackType[0,n]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			description <description> : java.lang.String[0,1]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: xml:lang CDATA #IMPLIED  : java.lang.String]
 *			retryCount <retry-count> : long 	[minInclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			method <method> : MethodType[1,n]
 *				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				description <description> : java.lang.String[0,1]
 *					[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					[attr: xml:lang CDATA #IMPLIED  : java.lang.String]
 *				ejbName <ejb-name> : java.lang.String
 *				methodIntf <method-intf> : java.lang.String[0,1] 	[enumeration (Home), enumeration (Remote), enumeration (LocalHome), enumeration (Local), enumeration (ServiceEndpoint)]
 *				methodName <method-name> : java.lang.String
 *				methodParams <method-params> : MethodParamsType[0,1]
 *					[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					methodParam <method-param> : java.lang.String[0,n] 	[pattern ([^\p{Z}]*)]
 *		enableBeanClassRedeploy <enable-bean-class-redeploy> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		timerImplementation <timer-implementation> : java.lang.String[0,1]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		disableWarning <disable-warning> : java.lang.String[0,n]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		workManager <work-manager> : WorkManagerType[0,n]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			name <name> : java.lang.String
 *				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			(
 *			  | responseTimeRequestClass <response-time-request-class> : ResponseTimeRequestClassType
 *			  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	name <name> : java.lang.String
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	goalMs <goal-ms> : java.math.BigInteger
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | fairShareRequestClass <fair-share-request-class> : FairShareRequestClassType
 *			  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	name <name> : java.lang.String
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	fairShare <fair-share> : java.math.BigInteger
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | contextRequestClass <context-request-class> : ContextRequestClassType
 *			  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	name <name> : java.lang.String
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	contextCase <context-case> : ContextCaseType[1,n]
 *			  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		| userName <user-name> : java.lang.String
 *			  | 		| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		| groupName <group-name> : java.lang.String
 *			  | 		| 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		requestClassName <request-class-name> : java.lang.String
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | requestClassName <request-class-name> : java.lang.String
 *			  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			)[0,1]
 *			(
 *			  | minThreadsConstraint <min-threads-constraint> : MinThreadsConstraintType
 *			  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	name <name> : java.lang.String
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	count <count> : java.math.BigInteger
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | minThreadsConstraintName <min-threads-constraint-name> : java.lang.String
 *			  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			)[0,1]
 *			(
 *			  | maxThreadsConstraint <max-threads-constraint> : MaxThreadsConstraintType
 *			  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	name <name> : java.lang.String
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	| count <count> : java.math.BigInteger
 *			  | 	| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	| poolName <pool-name> : java.lang.String
 *			  | 	| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | maxThreadsConstraintName <max-threads-constraint-name> : java.lang.String
 *			  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			)[0,1]
 *			(
 *			  | capacity <capacity> : CapacityType
 *			  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	name <name> : java.lang.String
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	count <count> : java.math.BigInteger
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | capacityName <capacity-name> : java.lang.String
 *			  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			)[0,1]
 *			(
 *			  | workManagerShutdownTrigger <work-manager-shutdown-trigger> : WorkManagerShutdownTriggerType
 *			  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	maxStuckThreadTime <max-stuck-thread-time> : java.math.BigInteger[0,1]
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	stuckThreadCount <stuck-thread-count> : java.math.BigInteger
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | ignoreStuckThreads <ignore-stuck-threads> : boolean
 *			  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			)[0,1]
 *		componentFactoryClassName <component-factory-class-name> : java.lang.String[0,1]
 *			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		weblogicCompatibility <weblogic-compatibility> : WeblogicCompatibilityType[0,1]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			entityAlwaysUsesTransaction <entity-always-uses-transaction> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		coherenceClusterRef <coherence-cluster-ref> : CoherenceClusterRefType[0,1]
 *			coherenceClusterName <coherence-cluster-name> : java.lang.String[0,1]
 *
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ejb1031;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.*;

// BEGIN_NOI18N

public class WeblogicEjbJar extends org.netbeans.modules.schema2beans.BaseBean
	 implements org.netbeans.modules.j2ee.weblogic9.dd.model.WeblogicEjbJar
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	private static final String SERIALIZATION_HELPER_CHARSET = "UTF-8";	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String VERSION = "Version";	// NOI18N
	static public final String DESCRIPTION = "Description";	// NOI18N
	static public final String DESCRIPTIONJ2EEID = "DescriptionJ2eeId";	// NOI18N
	static public final String DESCRIPTIONXMLLANG = "DescriptionXmlLang";	// NOI18N
	static public final String WEBLOGIC_ENTERPRISE_BEAN = "WeblogicEnterpriseBean";	// NOI18N
	static public final String SECURITY_ROLE_ASSIGNMENT = "SecurityRoleAssignment";	// NOI18N
	static public final String RUN_AS_ROLE_ASSIGNMENT = "RunAsRoleAssignment";	// NOI18N
	static public final String SECURITY_PERMISSION = "SecurityPermission";	// NOI18N
	static public final String TRANSACTION_ISOLATION = "TransactionIsolation";	// NOI18N
	static public final String MESSAGE_DESTINATION_DESCRIPTOR = "MessageDestinationDescriptor";	// NOI18N
	static public final String IDEMPOTENT_METHODS = "IdempotentMethods";	// NOI18N
	static public final String RETRY_METHODS_ON_ROLLBACK = "RetryMethodsOnRollback";	// NOI18N
	static public final String ENABLE_BEAN_CLASS_REDEPLOY = "EnableBeanClassRedeploy";	// NOI18N
	static public final String TIMER_IMPLEMENTATION = "TimerImplementation";	// NOI18N
	static public final String TIMERIMPLEMENTATIONID = "TimerImplementationId";	// NOI18N
	static public final String DISABLE_WARNING = "DisableWarning";	// NOI18N
	static public final String DISABLEWARNINGID = "DisableWarningId";	// NOI18N
	static public final String WORK_MANAGER = "WorkManager";	// NOI18N
	static public final String COMPONENT_FACTORY_CLASS_NAME = "ComponentFactoryClassName";	// NOI18N
	static public final String COMPONENTFACTORYCLASSNAMEJ2EEID = "ComponentFactoryClassNameJ2eeId";	// NOI18N
	static public final String WEBLOGIC_COMPATIBILITY = "WeblogicCompatibility";	// NOI18N
	static public final String COHERENCE_CLUSTER_REF = "CoherenceClusterRef";	// NOI18N

	public WeblogicEjbJar() {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public WeblogicEjbJar(org.w3c.dom.Node doc, int options) {
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
			doc = GraphManager.createRootElementNode("weblogic-ejb-jar");	// NOI18N
			if (doc == null)
				throw new Schema2BeansException(Common.getMessage(
					"CantCreateDOMRoot_msg", "weblogic-ejb-jar"));
		}
		Node n = GraphManager.getElementNode("weblogic-ejb-jar", doc);	// NOI18N
		if (n == null)
			throw new Schema2BeansException(Common.getMessage(
				"DocRootNotInDOMGraph_msg", "weblogic-ejb-jar", doc.getFirstChild().getNodeName()));

		this.graphManager.setXmlDocument(doc);

		// Entry point of the createBeans() recursive calls
		this.createBean(n, this.graphManager());
		this.initialize(options);
	}
	public WeblogicEjbJar(int options)
	{
		super(comparators, runtimeVersion);
		initOptions(options);
	}
	protected void initOptions(int options)
	{
		// The graph manager is allocated in the bean root
		this.graphManager = new GraphManager(this);
		this.createRoot("weblogic-ejb-jar", "WeblogicEjbJar",	// NOI18N
			Common.TYPE_1 | Common.TYPE_BEAN, WeblogicEjbJar.class);

		// Properties (see root bean comments for the bean graph)
		initPropertyTables(16);
		this.createProperty("description", 	// NOI18N
			DESCRIPTION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(DESCRIPTION, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(DESCRIPTION, "xml:lang", "XmlLang", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("weblogic-enterprise-bean", 	// NOI18N
			WEBLOGIC_ENTERPRISE_BEAN, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			WeblogicEnterpriseBeanType.class);
		this.createAttribute(WEBLOGIC_ENTERPRISE_BEAN, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("security-role-assignment", 	// NOI18N
			SECURITY_ROLE_ASSIGNMENT, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SecurityRoleAssignmentType.class);
		this.createAttribute(SECURITY_ROLE_ASSIGNMENT, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("run-as-role-assignment", 	// NOI18N
			RUN_AS_ROLE_ASSIGNMENT, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			RunAsRoleAssignmentType.class);
		this.createAttribute(RUN_AS_ROLE_ASSIGNMENT, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("security-permission", 	// NOI18N
			SECURITY_PERMISSION, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SecurityPermissionType.class);
		this.createAttribute(SECURITY_PERMISSION, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("transaction-isolation", 	// NOI18N
			TRANSACTION_ISOLATION, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			TransactionIsolationType.class);
		this.createAttribute(TRANSACTION_ISOLATION, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("message-destination-descriptor", 	// NOI18N
			MESSAGE_DESTINATION_DESCRIPTOR, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MessageDestinationDescriptorType.class);
		this.createAttribute(MESSAGE_DESTINATION_DESCRIPTOR, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("idempotent-methods", 	// NOI18N
			IDEMPOTENT_METHODS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			IdempotentMethodsType.class);
		this.createAttribute(IDEMPOTENT_METHODS, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("retry-methods-on-rollback", 	// NOI18N
			RETRY_METHODS_ON_ROLLBACK, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			RetryMethodsOnRollbackType.class);
		this.createAttribute(RETRY_METHODS_ON_ROLLBACK, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("enable-bean-class-redeploy", 	// NOI18N
			ENABLE_BEAN_CLASS_REDEPLOY, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("timer-implementation", 	// NOI18N
			TIMER_IMPLEMENTATION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(TIMER_IMPLEMENTATION, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("disable-warning", 	// NOI18N
			DISABLE_WARNING, 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(DISABLE_WARNING, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("work-manager", 	// NOI18N
			WORK_MANAGER, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			WorkManagerType.class);
		this.createAttribute(WORK_MANAGER, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("component-factory-class-name", 	// NOI18N
			COMPONENT_FACTORY_CLASS_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(COMPONENT_FACTORY_CLASS_NAME, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("weblogic-compatibility", 	// NOI18N
			WEBLOGIC_COMPATIBILITY, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			WeblogicCompatibilityType.class);
		this.createAttribute(WEBLOGIC_COMPATIBILITY, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("coherence-cluster-ref", 	// NOI18N
			COHERENCE_CLUSTER_REF, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			CoherenceClusterRefType.class);
		this.createAttribute("id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute("version", "Version", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {
		setDefaultNamespace("http://xmlns.oracle.com/weblogic/weblogic-ejb-jar");

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
	public void setVersion(java.lang.String value) {
		setAttributeValue(VERSION, value);
	}

	//
	public java.lang.String getVersion() {
		return getAttributeValue(VERSION);
	}

	// This attribute is optional
	public void setDescription(java.lang.String value) {
		this.setValue(DESCRIPTION, value);
	}

	//
	public java.lang.String getDescription() {
		return (java.lang.String)this.getValue(DESCRIPTION);
	}

	// This attribute is optional
	public void setDescriptionJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(DESCRIPTION) == 0) {
			setValue(DESCRIPTION, "");
		}
		setAttributeValue(DESCRIPTION, "J2eeId", value);
	}

	//
	public java.lang.String getDescriptionJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(DESCRIPTION) == 0) {
			return null;
		} else {
			return getAttributeValue(DESCRIPTION, "J2eeId");
		}
	}

	// This attribute is optional
	public void setDescriptionXmlLang(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(DESCRIPTION) == 0) {
			setValue(DESCRIPTION, "");
		}
		setAttributeValue(DESCRIPTION, "XmlLang", value);
	}

	//
	public java.lang.String getDescriptionXmlLang() {
		// If our element does not exist, then the attribute does not exist.
		if (size(DESCRIPTION) == 0) {
			return null;
		} else {
			return getAttributeValue(DESCRIPTION, "XmlLang");
		}
	}

	// This attribute is an array, possibly empty
	public void setWeblogicEnterpriseBean(int index, WeblogicEnterpriseBeanType value) {
		this.setValue(WEBLOGIC_ENTERPRISE_BEAN, index, value);
	}

	//
	public WeblogicEnterpriseBeanType getWeblogicEnterpriseBean(int index) {
		return (WeblogicEnterpriseBeanType)this.getValue(WEBLOGIC_ENTERPRISE_BEAN, index);
	}

	// Return the number of properties
	public int sizeWeblogicEnterpriseBean() {
		return this.size(WEBLOGIC_ENTERPRISE_BEAN);
	}

	// This attribute is an array, possibly empty
	public void setWeblogicEnterpriseBean(WeblogicEnterpriseBeanType[] value) {
		this.setValue(WEBLOGIC_ENTERPRISE_BEAN, value);
	}

	//
	public WeblogicEnterpriseBeanType[] getWeblogicEnterpriseBean() {
		return (WeblogicEnterpriseBeanType[])this.getValues(WEBLOGIC_ENTERPRISE_BEAN);
	}

	// Add a new element returning its index in the list
	public int addWeblogicEnterpriseBean(org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.WeblogicEnterpriseBeanType value) {
		int positionOfNewItem = this.addValue(WEBLOGIC_ENTERPRISE_BEAN, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeWeblogicEnterpriseBean(org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.WeblogicEnterpriseBeanType value) {
		return this.removeValue(WEBLOGIC_ENTERPRISE_BEAN, value);
	}

	// This attribute is an array, possibly empty
	public void setSecurityRoleAssignment(int index, SecurityRoleAssignmentType value) {
		this.setValue(SECURITY_ROLE_ASSIGNMENT, index, value);
	}

	//
	public SecurityRoleAssignmentType getSecurityRoleAssignment(int index) {
		return (SecurityRoleAssignmentType)this.getValue(SECURITY_ROLE_ASSIGNMENT, index);
	}

	// Return the number of properties
	public int sizeSecurityRoleAssignment() {
		return this.size(SECURITY_ROLE_ASSIGNMENT);
	}

	// This attribute is an array, possibly empty
	public void setSecurityRoleAssignment(SecurityRoleAssignmentType[] value) {
		this.setValue(SECURITY_ROLE_ASSIGNMENT, value);
	}

	//
	public SecurityRoleAssignmentType[] getSecurityRoleAssignment() {
		return (SecurityRoleAssignmentType[])this.getValues(SECURITY_ROLE_ASSIGNMENT);
	}

	// Add a new element returning its index in the list
	public int addSecurityRoleAssignment(org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.SecurityRoleAssignmentType value) {
		int positionOfNewItem = this.addValue(SECURITY_ROLE_ASSIGNMENT, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSecurityRoleAssignment(org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.SecurityRoleAssignmentType value) {
		return this.removeValue(SECURITY_ROLE_ASSIGNMENT, value);
	}

	// This attribute is an array, possibly empty
	public void setRunAsRoleAssignment(int index, RunAsRoleAssignmentType value) {
		this.setValue(RUN_AS_ROLE_ASSIGNMENT, index, value);
	}

	//
	public RunAsRoleAssignmentType getRunAsRoleAssignment(int index) {
		return (RunAsRoleAssignmentType)this.getValue(RUN_AS_ROLE_ASSIGNMENT, index);
	}

	// Return the number of properties
	public int sizeRunAsRoleAssignment() {
		return this.size(RUN_AS_ROLE_ASSIGNMENT);
	}

	// This attribute is an array, possibly empty
	public void setRunAsRoleAssignment(RunAsRoleAssignmentType[] value) {
		this.setValue(RUN_AS_ROLE_ASSIGNMENT, value);
	}

	//
	public RunAsRoleAssignmentType[] getRunAsRoleAssignment() {
		return (RunAsRoleAssignmentType[])this.getValues(RUN_AS_ROLE_ASSIGNMENT);
	}

	// Add a new element returning its index in the list
	public int addRunAsRoleAssignment(org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.RunAsRoleAssignmentType value) {
		int positionOfNewItem = this.addValue(RUN_AS_ROLE_ASSIGNMENT, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeRunAsRoleAssignment(org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.RunAsRoleAssignmentType value) {
		return this.removeValue(RUN_AS_ROLE_ASSIGNMENT, value);
	}

	// This attribute is optional
	public void setSecurityPermission(SecurityPermissionType value) {
		this.setValue(SECURITY_PERMISSION, value);
	}

	//
	public SecurityPermissionType getSecurityPermission() {
		return (SecurityPermissionType)this.getValue(SECURITY_PERMISSION);
	}

	// This attribute is an array, possibly empty
	public void setTransactionIsolation(int index, TransactionIsolationType value) {
		this.setValue(TRANSACTION_ISOLATION, index, value);
	}

	//
	public TransactionIsolationType getTransactionIsolation(int index) {
		return (TransactionIsolationType)this.getValue(TRANSACTION_ISOLATION, index);
	}

	// Return the number of properties
	public int sizeTransactionIsolation() {
		return this.size(TRANSACTION_ISOLATION);
	}

	// This attribute is an array, possibly empty
	public void setTransactionIsolation(TransactionIsolationType[] value) {
		this.setValue(TRANSACTION_ISOLATION, value);
	}

	//
	public TransactionIsolationType[] getTransactionIsolation() {
		return (TransactionIsolationType[])this.getValues(TRANSACTION_ISOLATION);
	}

	// Add a new element returning its index in the list
	public int addTransactionIsolation(org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.TransactionIsolationType value) {
		int positionOfNewItem = this.addValue(TRANSACTION_ISOLATION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeTransactionIsolation(org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.TransactionIsolationType value) {
		return this.removeValue(TRANSACTION_ISOLATION, value);
	}

	// This attribute is an array, possibly empty
	public void setMessageDestinationDescriptor(int index, MessageDestinationDescriptorType value) {
		this.setValue(MESSAGE_DESTINATION_DESCRIPTOR, index, value);
	}

	//
	public MessageDestinationDescriptorType getMessageDestinationDescriptor(int index) {
		return (MessageDestinationDescriptorType)this.getValue(MESSAGE_DESTINATION_DESCRIPTOR, index);
	}

	// Return the number of properties
	public int sizeMessageDestinationDescriptor() {
		return this.size(MESSAGE_DESTINATION_DESCRIPTOR);
	}

	// This attribute is an array, possibly empty
	public void setMessageDestinationDescriptor(MessageDestinationDescriptorType[] value) {
		this.setValue(MESSAGE_DESTINATION_DESCRIPTOR, value);
	}

	//
	public MessageDestinationDescriptorType[] getMessageDestinationDescriptor() {
		return (MessageDestinationDescriptorType[])this.getValues(MESSAGE_DESTINATION_DESCRIPTOR);
	}

	// Add a new element returning its index in the list
	public int addMessageDestinationDescriptor(org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.MessageDestinationDescriptorType value) {
		int positionOfNewItem = this.addValue(MESSAGE_DESTINATION_DESCRIPTOR, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeMessageDestinationDescriptor(org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.MessageDestinationDescriptorType value) {
		return this.removeValue(MESSAGE_DESTINATION_DESCRIPTOR, value);
	}

	// This attribute is optional
	public void setIdempotentMethods(IdempotentMethodsType value) {
		this.setValue(IDEMPOTENT_METHODS, value);
	}

	//
	public IdempotentMethodsType getIdempotentMethods() {
		return (IdempotentMethodsType)this.getValue(IDEMPOTENT_METHODS);
	}

	// This attribute is an array, possibly empty
	public void setRetryMethodsOnRollback(int index, RetryMethodsOnRollbackType value) {
		this.setValue(RETRY_METHODS_ON_ROLLBACK, index, value);
	}

	//
	public RetryMethodsOnRollbackType getRetryMethodsOnRollback(int index) {
		return (RetryMethodsOnRollbackType)this.getValue(RETRY_METHODS_ON_ROLLBACK, index);
	}

	// Return the number of properties
	public int sizeRetryMethodsOnRollback() {
		return this.size(RETRY_METHODS_ON_ROLLBACK);
	}

	// This attribute is an array, possibly empty
	public void setRetryMethodsOnRollback(RetryMethodsOnRollbackType[] value) {
		this.setValue(RETRY_METHODS_ON_ROLLBACK, value);
	}

	//
	public RetryMethodsOnRollbackType[] getRetryMethodsOnRollback() {
		return (RetryMethodsOnRollbackType[])this.getValues(RETRY_METHODS_ON_ROLLBACK);
	}

	// Add a new element returning its index in the list
	public int addRetryMethodsOnRollback(org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.RetryMethodsOnRollbackType value) {
		int positionOfNewItem = this.addValue(RETRY_METHODS_ON_ROLLBACK, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeRetryMethodsOnRollback(org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.RetryMethodsOnRollbackType value) {
		return this.removeValue(RETRY_METHODS_ON_ROLLBACK, value);
	}

	// This attribute is optional
	public void setEnableBeanClassRedeploy(boolean value) {
		this.setValue(ENABLE_BEAN_CLASS_REDEPLOY, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isEnableBeanClassRedeploy() {
		Boolean ret = (Boolean)this.getValue(ENABLE_BEAN_CLASS_REDEPLOY);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setTimerImplementation(java.lang.String value) {
		this.setValue(TIMER_IMPLEMENTATION, value);
	}

	//
	public java.lang.String getTimerImplementation() {
		return (java.lang.String)this.getValue(TIMER_IMPLEMENTATION);
	}

	// This attribute is optional
	public void setTimerImplementationId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(TIMER_IMPLEMENTATION) == 0) {
			setValue(TIMER_IMPLEMENTATION, "");
		}
		setAttributeValue(TIMER_IMPLEMENTATION, "Id", value);
	}

	//
	public java.lang.String getTimerImplementationId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(TIMER_IMPLEMENTATION) == 0) {
			return null;
		} else {
			return getAttributeValue(TIMER_IMPLEMENTATION, "Id");
		}
	}

	// This attribute is an array, possibly empty
	public void setDisableWarning(int index, java.lang.String value) {
		this.setValue(DISABLE_WARNING, index, value);
	}

	//
	public java.lang.String getDisableWarning(int index) {
		return (java.lang.String)this.getValue(DISABLE_WARNING, index);
	}

	// Return the number of properties
	public int sizeDisableWarning() {
		return this.size(DISABLE_WARNING);
	}

	// This attribute is an array, possibly empty
	public void setDisableWarning(java.lang.String[] value) {
		this.setValue(DISABLE_WARNING, value);
	}

	//
	public java.lang.String[] getDisableWarning() {
		return (java.lang.String[])this.getValues(DISABLE_WARNING);
	}

	// Add a new element returning its index in the list
	public int addDisableWarning(java.lang.String value) {
		int positionOfNewItem = this.addValue(DISABLE_WARNING, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeDisableWarning(java.lang.String value) {
		return this.removeValue(DISABLE_WARNING, value);
	}

	// This attribute is an array, possibly empty
	public void setDisableWarningId(int index, java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(DISABLE_WARNING) == 0) {
			addValue(DISABLE_WARNING, "");
		}
		setAttributeValue(DISABLE_WARNING, index, "Id", value);
	}

	//
	public java.lang.String getDisableWarningId(int index) {
		// If our element does not exist, then the attribute does not exist.
		if (size(DISABLE_WARNING) == 0) {
			return null;
		} else {
			return getAttributeValue(DISABLE_WARNING, index, "Id");
		}
	}

	// Return the number of properties
	public int sizeDisableWarningId() {
		return this.size(DISABLE_WARNING);
	}

	// This attribute is an array, possibly empty
	public void setWorkManager(int index, WorkManagerType value) {
		this.setValue(WORK_MANAGER, index, value);
	}

	//
	public WorkManagerType getWorkManager(int index) {
		return (WorkManagerType)this.getValue(WORK_MANAGER, index);
	}

	// Return the number of properties
	public int sizeWorkManager() {
		return this.size(WORK_MANAGER);
	}

	// This attribute is an array, possibly empty
	public void setWorkManager(WorkManagerType[] value) {
		this.setValue(WORK_MANAGER, value);
	}

	//
	public WorkManagerType[] getWorkManager() {
		return (WorkManagerType[])this.getValues(WORK_MANAGER);
	}

	// Add a new element returning its index in the list
	public int addWorkManager(org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.WorkManagerType value) {
		int positionOfNewItem = this.addValue(WORK_MANAGER, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeWorkManager(org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.WorkManagerType value) {
		return this.removeValue(WORK_MANAGER, value);
	}

	// This attribute is optional
	public void setComponentFactoryClassName(java.lang.String value) {
		this.setValue(COMPONENT_FACTORY_CLASS_NAME, value);
	}

	//
	public java.lang.String getComponentFactoryClassName() {
		return (java.lang.String)this.getValue(COMPONENT_FACTORY_CLASS_NAME);
	}

	// This attribute is optional
	public void setComponentFactoryClassNameJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(COMPONENT_FACTORY_CLASS_NAME) == 0) {
			setValue(COMPONENT_FACTORY_CLASS_NAME, "");
		}
		setAttributeValue(COMPONENT_FACTORY_CLASS_NAME, "J2eeId", value);
	}

	//
	public java.lang.String getComponentFactoryClassNameJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(COMPONENT_FACTORY_CLASS_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(COMPONENT_FACTORY_CLASS_NAME, "J2eeId");
		}
	}

	// This attribute is optional
	public void setWeblogicCompatibility(WeblogicCompatibilityType value) {
		this.setValue(WEBLOGIC_COMPATIBILITY, value);
	}

	//
	public WeblogicCompatibilityType getWeblogicCompatibility() {
		return (WeblogicCompatibilityType)this.getValue(WEBLOGIC_COMPATIBILITY);
	}

	// This attribute is optional
	public void setCoherenceClusterRef(CoherenceClusterRefType value) {
		this.setValue(COHERENCE_CLUSTER_REF, value);
	}

	//
	public CoherenceClusterRefType getCoherenceClusterRef() {
		return (CoherenceClusterRefType)this.getValue(COHERENCE_CLUSTER_REF);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public WeblogicEnterpriseBeanType newWeblogicEnterpriseBeanType() {
		return new WeblogicEnterpriseBeanType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SecurityRoleAssignmentType newSecurityRoleAssignmentType() {
		return new SecurityRoleAssignmentType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public RunAsRoleAssignmentType newRunAsRoleAssignmentType() {
		return new RunAsRoleAssignmentType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SecurityPermissionType newSecurityPermissionType() {
		return new SecurityPermissionType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public TransactionIsolationType newTransactionIsolationType() {
		return new TransactionIsolationType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public MessageDestinationDescriptorType newMessageDestinationDescriptorType() {
		return new MessageDestinationDescriptorType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public IdempotentMethodsType newIdempotentMethodsType() {
		return new IdempotentMethodsType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public RetryMethodsOnRollbackType newRetryMethodsOnRollbackType() {
		return new RetryMethodsOnRollbackType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public WorkManagerType newWorkManagerType() {
		return new WorkManagerType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public WeblogicCompatibilityType newWeblogicCompatibilityType() {
		return new WeblogicCompatibilityType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public CoherenceClusterRefType newCoherenceClusterRefType() {
		return new CoherenceClusterRefType();
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
	public static WeblogicEjbJar createGraph(org.w3c.dom.Node doc) {
		return new WeblogicEjbJar(doc, Common.NO_DEFAULT_VALUES);
	}

	public static WeblogicEjbJar createGraph(java.io.File f) throws java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return createGraph(in, false);
		} finally {
			in.close();
		}
	}

	public static WeblogicEjbJar createGraph(java.io.InputStream in) {
		return createGraph(in, false);
	}

	public static WeblogicEjbJar createGraph(java.io.InputStream in, boolean validate) {
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
	public static WeblogicEjbJar createGraph() {
		return new WeblogicEjbJar();
	}


	
                    public WeblogicEnterpriseBeanType addWeblogicEnterpriseBean() {
                        WeblogicEnterpriseBeanType bean = new WeblogicEnterpriseBeanType();
                        addWeblogicEnterpriseBean(bean);
                        return bean;
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
		// Validating property version
		// Validating property description
		// Validating property descriptionJ2eeId
		if (getDescriptionJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getDescriptionJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "descriptionJ2eeId", this);	// NOI18N
			}
		}
		// Validating property descriptionXmlLang
		// Validating property weblogicEnterpriseBean
		for (int _index = 0; _index < sizeWeblogicEnterpriseBean(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.WeblogicEnterpriseBeanType element = getWeblogicEnterpriseBean(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property securityRoleAssignment
		for (int _index = 0; _index < sizeSecurityRoleAssignment(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.SecurityRoleAssignmentType element = getSecurityRoleAssignment(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property runAsRoleAssignment
		for (int _index = 0; _index < sizeRunAsRoleAssignment(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.RunAsRoleAssignmentType element = getRunAsRoleAssignment(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property securityPermission
		if (getSecurityPermission() != null) {
			getSecurityPermission().validate();
		}
		// Validating property transactionIsolation
		for (int _index = 0; _index < sizeTransactionIsolation(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.TransactionIsolationType element = getTransactionIsolation(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property messageDestinationDescriptor
		for (int _index = 0; _index < sizeMessageDestinationDescriptor(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.MessageDestinationDescriptorType element = getMessageDestinationDescriptor(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property idempotentMethods
		if (getIdempotentMethods() != null) {
			getIdempotentMethods().validate();
		}
		// Validating property retryMethodsOnRollback
		for (int _index = 0; _index < sizeRetryMethodsOnRollback(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.RetryMethodsOnRollbackType element = getRetryMethodsOnRollback(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property enableBeanClassRedeploy
		{
			boolean patternPassed = false;
			if ((isEnableBeanClassRedeploy() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isEnableBeanClassRedeploy()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "enableBeanClassRedeploy", this);	// NOI18N
		}
		// Validating property timerImplementation
		// Validating property timerImplementationId
		if (getTimerImplementationId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getTimerImplementationId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "timerImplementationId", this);	// NOI18N
			}
		}
		// Validating property disableWarning
		// Validating property disableWarningId
		// Validating property workManager
		for (int _index = 0; _index < sizeWorkManager(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ejb1031.WorkManagerType element = getWorkManager(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property componentFactoryClassName
		// Validating property componentFactoryClassNameJ2eeId
		if (getComponentFactoryClassNameJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getComponentFactoryClassNameJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "componentFactoryClassNameJ2eeId", this);	// NOI18N
			}
		}
		// Validating property weblogicCompatibility
		if (getWeblogicCompatibility() != null) {
			getWeblogicCompatibility().validate();
		}
		// Validating property coherenceClusterRef
		if (getCoherenceClusterRef() != null) {
			getCoherenceClusterRef().validate();
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
		str.append(indent);
		str.append("Description");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDescription();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DESCRIPTION, 0, str, indent);

		str.append(indent);
		str.append("WeblogicEnterpriseBean["+this.sizeWeblogicEnterpriseBean()+"]");	// NOI18N
		for(int i=0; i<this.sizeWeblogicEnterpriseBean(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getWeblogicEnterpriseBean(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(WEBLOGIC_ENTERPRISE_BEAN, i, str, indent);
		}

		str.append(indent);
		str.append("SecurityRoleAssignment["+this.sizeSecurityRoleAssignment()+"]");	// NOI18N
		for(int i=0; i<this.sizeSecurityRoleAssignment(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getSecurityRoleAssignment(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SECURITY_ROLE_ASSIGNMENT, i, str, indent);
		}

		str.append(indent);
		str.append("RunAsRoleAssignment["+this.sizeRunAsRoleAssignment()+"]");	// NOI18N
		for(int i=0; i<this.sizeRunAsRoleAssignment(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getRunAsRoleAssignment(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(RUN_AS_ROLE_ASSIGNMENT, i, str, indent);
		}

		str.append(indent);
		str.append("SecurityPermission");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getSecurityPermission();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(SECURITY_PERMISSION, 0, str, indent);

		str.append(indent);
		str.append("TransactionIsolation["+this.sizeTransactionIsolation()+"]");	// NOI18N
		for(int i=0; i<this.sizeTransactionIsolation(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getTransactionIsolation(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(TRANSACTION_ISOLATION, i, str, indent);
		}

		str.append(indent);
		str.append("MessageDestinationDescriptor["+this.sizeMessageDestinationDescriptor()+"]");	// NOI18N
		for(int i=0; i<this.sizeMessageDestinationDescriptor(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getMessageDestinationDescriptor(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MESSAGE_DESTINATION_DESCRIPTOR, i, str, indent);
		}

		str.append(indent);
		str.append("IdempotentMethods");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getIdempotentMethods();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(IDEMPOTENT_METHODS, 0, str, indent);

		str.append(indent);
		str.append("RetryMethodsOnRollback["+this.sizeRetryMethodsOnRollback()+"]");	// NOI18N
		for(int i=0; i<this.sizeRetryMethodsOnRollback(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getRetryMethodsOnRollback(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(RETRY_METHODS_ON_ROLLBACK, i, str, indent);
		}

		str.append(indent);
		str.append("EnableBeanClassRedeploy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isEnableBeanClassRedeploy()?"true":"false"));
		this.dumpAttributes(ENABLE_BEAN_CLASS_REDEPLOY, 0, str, indent);

		str.append(indent);
		str.append("TimerImplementation");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getTimerImplementation();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(TIMER_IMPLEMENTATION, 0, str, indent);

		str.append(indent);
		str.append("DisableWarning["+this.sizeDisableWarning()+"]");	// NOI18N
		for(int i=0; i<this.sizeDisableWarning(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getDisableWarning(i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(DISABLE_WARNING, i, str, indent);
		}

		str.append(indent);
		str.append("WorkManager["+this.sizeWorkManager()+"]");	// NOI18N
		for(int i=0; i<this.sizeWorkManager(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getWorkManager(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(WORK_MANAGER, i, str, indent);
		}

		str.append(indent);
		str.append("ComponentFactoryClassName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getComponentFactoryClassName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(COMPONENT_FACTORY_CLASS_NAME, 0, str, indent);

		str.append(indent);
		str.append("WeblogicCompatibility");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getWeblogicCompatibility();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(WEBLOGIC_COMPATIBILITY, 0, str, indent);

		str.append(indent);
		str.append("CoherenceClusterRef");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getCoherenceClusterRef();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(COHERENCE_CLUSTER_REF, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("WeblogicEjbJar\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

