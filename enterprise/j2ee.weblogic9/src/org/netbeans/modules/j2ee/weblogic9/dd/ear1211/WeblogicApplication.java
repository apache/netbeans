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
 *	This generated bean class WeblogicApplication matches the schema element 'weblogic-application'.
 *
 *	Generated on Tue Jul 25 03:26:46 PDT 2017
 *
 *	This class matches the root element of the XML Schema,
 *	and is the root of the following bean graph:
 *
 *	weblogicApplication <weblogic-application> : WeblogicApplication
 *		[attr: version CDATA #IMPLIED  : java.lang.String]
 *		ejb <ejb> : EjbType[0,1]
 *			entityCache <entity-cache> : ApplicationEntityCacheType[0,n]
 *				entityCacheName <entity-cache-name> : java.lang.String
 *				(
 *				  | maxBeansInCache <max-beans-in-cache> : int
 *				  | maxCacheSize <max-cache-size> : MaxCacheSizeType
 *				  | 	| bytes <bytes> : int
 *				  | 	| megabytes <megabytes> : int
 *				)[0,1]
 *				maxQueriesInCache <max-queries-in-cache> : int[0,1]
 *				cachingStrategy <caching-strategy> : java.lang.String[0,1]
 *			startMdbsWithApplication <start-mdbs-with-application> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		xml <xml> : XmlType[0,1]
 *			parserFactory <parser-factory> : ParserFactoryType[0,1]
 *				saxparserFactory <saxparser-factory> : java.lang.String[0,1]
 *				documentBuilderFactory <document-builder-factory> : java.lang.String[0,1]
 *				transformerFactory <transformer-factory> : java.lang.String[0,1]
 *			entityMapping <entity-mapping> : EntityMappingType[0,n]
 *				entityMappingName <entity-mapping-name> : java.lang.String
 *				publicId <public-id> : java.lang.String[0,1]
 *				systemId <system-id> : java.lang.String[0,1]
 *				entityUri <entity-uri> : java.lang.String[0,1]
 *				whenToCache <when-to-cache> : java.lang.String[0,1]
 *				cacheTimeoutInterval <cache-timeout-interval> : int[0,1]
 *		jdbcConnectionPool <jdbc-connection-pool> : JdbcConnectionPoolType[0,n]
 *			dataSourceJndiName <data-source-jndi-name> : java.lang.String
 *			connectionFactory <connection-factory> : ConnectionFactoryType
 *				factoryName <factory-name> : java.lang.String[0,1]
 *				connectionProperties <connection-properties> : ConnectionPropertiesType[0,1]
 *					userName <user-name> : java.lang.String[0,1]
 *					password <password> : java.lang.String[0,1]
 *					url <url> : java.lang.String[0,1]
 *					driverClassName <driver-class-name> : java.lang.String[0,1]
 *					connectionParams <connection-params> : ConnectionParamsType[0,n]
 *						parameter <parameter> : ParameterType[0,n]
 *							description <description> : java.lang.String[0,1]
 *							paramName <param-name> : java.lang.String
 *							paramValue <param-value> : java.lang.String
 *			poolParams <pool-params> : ApplicationPoolParamsType[0,1]
 *				sizeParams <size-params> : SizeParamsType[0,1]
 *					initialCapacity <initial-capacity> : int[0,1]
 *					maxCapacity <max-capacity> : int[0,1]
 *					capacityIncrement <capacity-increment> : int[0,1]
 *					shrinkingEnabled <shrinking-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *					shrinkPeriodMinutes <shrink-period-minutes> : int[0,1]
 *					shrinkFrequencySeconds <shrink-frequency-seconds> : int[0,1]
 *					highestNumWaiters <highest-num-waiters> : int[0,1]
 *					highestNumUnavailable <highest-num-unavailable> : int[0,1]
 *				xaParams <xa-params> : XaParamsType[0,1]
 *					debugLevel <debug-level> : int[0,1]
 *					keepConnUntilTxCompleteEnabled <keep-conn-until-tx-complete-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *					endOnlyOnceEnabled <end-only-once-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *					recoverOnlyOnceEnabled <recover-only-once-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *					txContextOnCloseNeeded <tx-context-on-close-needed> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *					newConnForCommitEnabled <new-conn-for-commit-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *					preparedStatementCacheSize <prepared-statement-cache-size> : int[0,1]
 *					keepLogicalConnOpenOnRelease <keep-logical-conn-open-on-release> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *					localTransactionSupported <local-transaction-supported> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *					resourceHealthMonitoringEnabled <resource-health-monitoring-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *					xaSetTransactionTimeout <xa-set-transaction-timeout> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *					xaTransactionTimeout <xa-transaction-timeout> : int[0,1]
 *					rollbackLocaltxUponConnclose <rollback-localtx-upon-connclose> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *				loginDelaySeconds <login-delay-seconds> : int[0,1]
 *				leakProfilingEnabled <leak-profiling-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *				connectionCheckParams <connection-check-params> : ConnectionCheckParamsType[0,1]
 *					tableName <table-name> : java.lang.String[0,1]
 *					checkOnReserveEnabled <check-on-reserve-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *					checkOnReleaseEnabled <check-on-release-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *					refreshMinutes <refresh-minutes> : int[0,1]
 *					checkOnCreateEnabled <check-on-create-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *					connectionReserveTimeoutSeconds <connection-reserve-timeout-seconds> : int[0,1]
 *					connectionCreationRetryFrequencySeconds <connection-creation-retry-frequency-seconds> : int[0,1]
 *					inactiveConnectionTimeoutSeconds <inactive-connection-timeout-seconds> : int[0,1]
 *					testFrequencySeconds <test-frequency-seconds> : int[0,1]
 *					initSql <init-sql> : java.lang.String[0,1]
 *				jdbcxaDebugLevel <jdbcxa-debug-level> : int[0,1]
 *				removeInfectedConnectionsEnabled <remove-infected-connections-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			driverParams <driver-params> : DriverParamsType[0,1]
 *				statement <statement> : StatementType[0,1]
 *					profilingEnabled <profiling-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *				preparedStatement <prepared-statement> : PreparedStatementType[0,1]
 *					profilingEnabled <profiling-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *					cacheProfilingThreshold <cache-profiling-threshold> : int[0,1]
 *					cacheSize <cache-size> : int[0,1]
 *					parameterLoggingEnabled <parameter-logging-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *					maxParameterLength <max-parameter-length> : int[0,1]
 *					cacheType <cache-type> : int[0,1]
 *				rowPrefetchEnabled <row-prefetch-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *				rowPrefetchSize <row-prefetch-size> : int[0,1]
 *				streamChunkSize <stream-chunk-size> : int[0,1]
 *			aclName <acl-name> : java.lang.String[0,1]
 *		security <security> : SecurityType[0,1]
 *			realmName <realm-name> : java.lang.String[0,1]
 *			securityRoleAssignment <security-role-assignment> : ApplicationSecurityRoleAssignmentType[0,n]
 *				roleName <role-name> : java.lang.String
 *				| principalName <principal-name> : java.lang.String[1,n]
 *				| externallyDefined <externally-defined> : EmptyType
 *				| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		applicationParam <application-param> : ApplicationParamType[0,n]
 *			description <description> : java.lang.String[0,1]
 *			paramName <param-name> : java.lang.String
 *			paramValue <param-value> : java.lang.String
 *		classloaderStructure <classloader-structure> : ClassloaderStructureType[0,1]
 *			moduleRef <module-ref> : ModuleRefType[0,n]
 *				moduleUri <module-uri> : java.lang.String
 *			classloaderStructure <classloader-structure> : ClassloaderStructureType[0,n]...
 *		listener <listener> : ListenerType[0,n]
 *			listenerClass <listener-class> : java.lang.String
 *			listenerUri <listener-uri> : java.lang.String[0,1]
 *			runAsPrincipalName <run-as-principal-name> : java.lang.String[0,1]
 *		singletonService <singleton-service> : SingletonServiceType[0,n]
 *			className <class-name> : java.lang.String
 *			name <name> : java.lang.String
 *			singletonUri <singleton-uri> : java.lang.String[0,1]
 *		startup <startup> : StartupType[0,n]
 *			startupClass <startup-class> : java.lang.String
 *			startupUri <startup-uri> : java.lang.String[0,1]
 *		shutdown <shutdown> : ShutdownType[0,n]
 *			shutdownClass <shutdown-class> : java.lang.String
 *			shutdownUri <shutdown-uri> : java.lang.String[0,1]
 *		module <module> : WeblogicModuleType[0,n]
 *			name <name> : java.lang.String
 *			type <type> : java.lang.String 	[enumeration (JMS), enumeration (JDBC), enumeration (Interception)]
 *			path <path> : java.lang.String
 *		libraryRef <library-ref> : LibraryRefType[0,n]
 *			libraryName <library-name> : java.lang.String
 *			specificationVersion <specification-version> : java.lang.String[0,1]
 *			implementationVersion <implementation-version> : java.lang.String[0,1]
 *			exactMatch <exact-match> : boolean[0,1]
 *			contextRoot <context-root> : java.lang.String[0,1]
 *		fairShareRequest <fair-share-request> : FairShareRequestClassType[0,n]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			name <name> : java.lang.String
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			fairShare <fair-share> : java.math.BigInteger
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		responseTimeRequest <response-time-request> : ResponseTimeRequestClassType[0,n]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			name <name> : java.lang.String
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			goalMs <goal-ms> : java.math.BigInteger
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		contextRequest <context-request> : ContextRequestClassType[0,n]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			name <name> : java.lang.String
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			contextCase <context-case> : ContextCaseType[1,n]
 *				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				| userName <user-name> : java.lang.String
 *				| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				| groupName <group-name> : java.lang.String
 *				| 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				requestClassName <request-class-name> : java.lang.String
 *					[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		maxThreadsConstraint <max-threads-constraint> : MaxThreadsConstraintType[0,n]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			name <name> : java.lang.String
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			| count <count> : java.math.BigInteger
 *			| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			| poolName <pool-name> : java.lang.String
 *			| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		minThreadsConstraint <min-threads-constraint> : MinThreadsConstraintType[0,n]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			name <name> : java.lang.String
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			count <count> : java.math.BigInteger
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		capacity <capacity> : CapacityType[0,n]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			name <name> : java.lang.String
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			count <count> : java.math.BigInteger
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		workManager <work-manager> : WorkManagerType[0,n]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			name <name> : java.lang.String
 *				[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			(
 *			  | responseTimeRequestClass <response-time-request-class> : ResponseTimeRequestClassType
 *			  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	name <name> : java.lang.String
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	goalMs <goal-ms> : java.math.BigInteger
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | fairShareRequestClass <fair-share-request-class> : FairShareRequestClassType
 *			  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	name <name> : java.lang.String
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	fairShare <fair-share> : java.math.BigInteger
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | contextRequestClass <context-request-class> : ContextRequestClassType
 *			  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	name <name> : java.lang.String
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	contextCase <context-case> : ContextCaseType[1,n]
 *			  | 		[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		| userName <user-name> : java.lang.String
 *			  | 		| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		| groupName <group-name> : java.lang.String
 *			  | 		| 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		requestClassName <request-class-name> : java.lang.String
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | requestClassName <request-class-name> : java.lang.String
 *			  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			)[0,1]
 *			(
 *			  | minThreadsConstraint <min-threads-constraint> : MinThreadsConstraintType
 *			  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	name <name> : java.lang.String
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	count <count> : java.math.BigInteger
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | minThreadsConstraintName <min-threads-constraint-name> : java.lang.String
 *			  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			)[0,1]
 *			(
 *			  | maxThreadsConstraint <max-threads-constraint> : MaxThreadsConstraintType
 *			  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	name <name> : java.lang.String
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	| count <count> : java.math.BigInteger
 *			  | 	| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	| poolName <pool-name> : java.lang.String
 *			  | 	| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | maxThreadsConstraintName <max-threads-constraint-name> : java.lang.String
 *			  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			)[0,1]
 *			(
 *			  | capacity <capacity> : CapacityType
 *			  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	name <name> : java.lang.String
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	count <count> : java.math.BigInteger
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | capacityName <capacity-name> : java.lang.String
 *			  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			)[0,1]
 *			(
 *			  | workManagerShutdownTrigger <work-manager-shutdown-trigger> : WorkManagerShutdownTriggerType
 *			  | 	[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	maxStuckThreadTime <max-stuck-thread-time> : java.math.BigInteger[0,1]
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	stuckThreadCount <stuck-thread-count> : java.math.BigInteger
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 		[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | ignoreStuckThreads <ignore-stuck-threads> : boolean
 *			  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			  | 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			)[0,1]
 *		componentFactoryClassName <component-factory-class-name> : java.lang.String[0,1]
 *			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		applicationAdminModeTrigger <application-admin-mode-trigger> : ApplicationAdminModeTriggerType[0,1]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			maxStuckThreadTime <max-stuck-thread-time> : java.math.BigInteger[0,1]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			stuckThreadCount <stuck-thread-count> : java.math.BigInteger
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		sessionDescriptor <session-descriptor> : SessionDescriptorType[0,1]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			timeoutSecs <timeout-secs> : java.math.BigInteger[0,1]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			invalidationIntervalSecs <invalidation-interval-secs> : java.math.BigInteger[0,1]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			debugEnabled <debug-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			idLength <id-length> : java.math.BigInteger[0,1]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			trackingEnabled <tracking-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			cacheSize <cache-size> : java.math.BigInteger[0,1]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			maxInMemorySessions <max-in-memory-sessions> : java.math.BigInteger[0,1]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			cookiesEnabled <cookies-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			cookieName <cookie-name> : java.lang.String[0,1]
 *			cookiePath <cookie-path> : java.lang.String[0,1]
 *			cookieDomain <cookie-domain> : java.lang.String[0,1]
 *			cookieComment <cookie-comment> : java.lang.String[0,1]
 *			cookieSecure <cookie-secure> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			cookieMaxAgeSecs <cookie-max-age-secs> : java.math.BigInteger[0,1]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			cookieHttpOnly <cookie-http-only> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			persistentStoreType <persistent-store-type> : java.lang.String[0,1]
 *			persistentStoreCookieName <persistent-store-cookie-name> : java.lang.String[0,1]
 *			persistentStoreDir <persistent-store-dir> : java.lang.String[0,1]
 *			persistentStorePool <persistent-store-pool> : java.lang.String[0,1]
 *			persistentDataSourceJndiName <persistent-data-source-jndi-name> : java.lang.String[0,1]
 *			persistentSessionFlushInterval <persistent-session-flush-interval> : java.math.BigInteger[0,1]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			persistentSessionFlushThreshold <persistent-session-flush-threshold> : long[0,1] 	[minExclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			persistentAsyncQueueTimeout <persistent-async-queue-timeout> : long[0,1] 	[minExclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			persistentStoreTable <persistent-store-table> : java.lang.String[0,1]
 *			jdbcColumnNameMaxInactiveInterval <jdbc-column-name-max-inactive-interval> : java.lang.String[0,1]
 *			jdbcConnectionTimeoutSecs <jdbc-connection-timeout-secs> : java.math.BigInteger[0,1]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			urlRewritingEnabled <url-rewriting-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			httpProxyCachingOfCookies <http-proxy-caching-of-cookies> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			encodeSessionIdInQueryParams <encode-session-id-in-query-params> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			monitoringAttributeName <monitoring-attribute-name> : java.lang.String[0,1]
 *			sharingEnabled <sharing-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			invalidateOnRelogin <invalidate-on-relogin> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		libraryContextRootOverride <library-context-root-override> : LibraryContextRootOverrideType[0,n]
 *			contextRoot <context-root> : java.lang.String
 *			overrideValue <override-value> : java.lang.String
 *		preferApplicationPackages <prefer-application-packages> : PreferApplicationPackagesType[0,1]
 *			packageName <package-name> : java.lang.String[0,n]
 *		preferApplicationResources <prefer-application-resources> : PreferApplicationResourcesType[0,1]
 *			resourceName <resource-name> : java.lang.String[0,n]
 *		fastSwap <fast-swap> : FastSwapType[0,1]
 *			enabled <enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			refreshInterval <refresh-interval> : java.math.BigInteger[0,1]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			redefinitionTaskLimit <redefinition-task-limit> : java.math.BigInteger[0,1]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		coherenceClusterRef <coherence-cluster-ref> : CoherenceClusterRefType[0,1]
 *			coherenceClusterName <coherence-cluster-name> : java.lang.String[0,1]
 *		resourceDescription <resource-description> : ResourceDescriptionType[0,n]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			resRefName <res-ref-name> : java.lang.String
 *			| jndiName <jndi-name> : java.lang.String 	[whiteSpace (collapse)]
 *			| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			| resourceLink <resource-link> : java.lang.String 	[whiteSpace (collapse)]
 *			| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			defaultResourcePrincipal <default-resource-principal> : DefaultResourcePrincipalType[0,1]
 *				name <name> : java.lang.String 	[whiteSpace (collapse)]
 *					[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				password <password> : java.lang.String 	[whiteSpace (collapse)]
 *					[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		resourceEnvDescription <resource-env-description> : ResourceEnvDescriptionType[0,n]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			resourceEnvRefName <resource-env-ref-name> : java.lang.String
 *			| jndiName <jndi-name> : java.lang.String 	[whiteSpace (collapse)]
 *			| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			| resourceLink <resource-link> : java.lang.String 	[whiteSpace (collapse)]
 *			| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *		ejbReferenceDescription <ejb-reference-description> : EjbReferenceDescriptionType[0,n]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			ejbRefName <ejb-ref-name> : java.lang.String
 *			jndiName <jndi-name> : java.lang.String
 *		serviceReferenceDescription <service-reference-description> : ServiceReferenceDescriptionType[0,n]
 *			[attr: id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			serviceRefName <service-ref-name> : java.lang.String
 *			wsdlUrl <wsdl-url> : java.lang.String[0,1] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			callProperty <call-property> : PropertyNamevalueType[0,n]
 *				name <name> : java.lang.String 	[whiteSpace (collapse)]
 *					[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				value <value> : java.lang.String 	[whiteSpace (collapse)]
 *					[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			portInfo <port-info> : PortInfoType[0,n]
 *				portName <port-name> : java.lang.String 	[whiteSpace (collapse)]
 *					[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				stubProperty <stub-property> : PropertyNamevalueType[0,n]
 *					name <name> : java.lang.String 	[whiteSpace (collapse)]
 *						[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *						[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					value <value> : java.lang.String 	[whiteSpace (collapse)]
 *						[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *						[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				callProperty <call-property> : PropertyNamevalueType[0,n]
 *					name <name> : java.lang.String 	[whiteSpace (collapse)]
 *						[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *						[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					value <value> : java.lang.String 	[whiteSpace (collapse)]
 *						[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *						[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				wsatConfig <wsat-config> : WsatConfigType[0,1]
 *					enabled <enabled> : boolean[0,1]
 *					flowType <flow-type> : java.lang.String[0,1] 	[enumeration (SUPPORTS), enumeration (MANDATORY), enumeration (NEVER)]
 *					version <version> : java.lang.String[0,1] 	[enumeration (DEFAULT), enumeration (WSAT10), enumeration (WSAT11), enumeration (WSAT12)]
 *				owsmPolicy <owsm-policy> : OwsmPolicyType[0,n]
 *					uri <uri> : java.lang.String 	[whiteSpace (collapse)]
 *						[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *						[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					status <status> : java.lang.String[0,1] 	[whiteSpace (collapse)]
 *						[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *						[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					category <category> : java.lang.String 	[whiteSpace (collapse)]
 *						[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *						[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					securityConfigurationProperty <security-configuration-property > : PropertyNamevalueType[0,n]
 *						name <name> : java.lang.String 	[whiteSpace (collapse)]
 *							[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *							[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *						value <value> : java.lang.String 	[whiteSpace (collapse)]
 *							[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *							[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *				operation <operation> : OperationInfoType[0,n]
 *					name <name> : java.lang.String 	[whiteSpace (collapse)]
 *						[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *						[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *					wsatConfig <wsat-config> : WsatConfigType[0,1]
 *						enabled <enabled> : boolean[0,1]
 *						flowType <flow-type> : java.lang.String[0,1] 	[enumeration (SUPPORTS), enumeration (MANDATORY), enumeration (NEVER)]
 *						version <version> : java.lang.String[0,1] 	[enumeration (DEFAULT), enumeration (WSAT10), enumeration (WSAT11), enumeration (WSAT12)]
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
 *			| 	[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear1211;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.*;

// BEGIN_NOI18N

public class WeblogicApplication extends org.netbeans.modules.schema2beans.BaseBean
	 implements org.netbeans.modules.j2ee.weblogic9.dd.model.WeblogicApplication
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	private static final String SERIALIZATION_HELPER_CHARSET = "UTF-8";	// NOI18N

	static public final String VERSION = "Version";	// NOI18N
	static public final String EJB = "Ejb";	// NOI18N
	static public final String XML = "Xml";	// NOI18N
	static public final String JDBC_CONNECTION_POOL = "JdbcConnectionPool";	// NOI18N
	static public final String SECURITY = "Security";	// NOI18N
	static public final String APPLICATION_PARAM = "ApplicationParam";	// NOI18N
	static public final String CLASSLOADER_STRUCTURE = "ClassloaderStructure";	// NOI18N
	static public final String LISTENER = "Listener";	// NOI18N
	static public final String SINGLETON_SERVICE = "SingletonService";	// NOI18N
	static public final String STARTUP = "Startup";	// NOI18N
	static public final String SHUTDOWN = "Shutdown";	// NOI18N
	static public final String MODULE = "Module";	// NOI18N
	static public final String LIBRARY_REF = "LibraryRef";	// NOI18N
	static public final String FAIR_SHARE_REQUEST = "FairShareRequest";	// NOI18N
	static public final String RESPONSE_TIME_REQUEST = "ResponseTimeRequest";	// NOI18N
	static public final String CONTEXT_REQUEST = "ContextRequest";	// NOI18N
	static public final String MAX_THREADS_CONSTRAINT = "MaxThreadsConstraint";	// NOI18N
	static public final String MIN_THREADS_CONSTRAINT = "MinThreadsConstraint";	// NOI18N
	static public final String CAPACITY = "Capacity";	// NOI18N
	static public final String WORK_MANAGER = "WorkManager";	// NOI18N
	static public final String COMPONENT_FACTORY_CLASS_NAME = "ComponentFactoryClassName";	// NOI18N
	static public final String COMPONENTFACTORYCLASSNAMEJ2EEID = "ComponentFactoryClassNameJ2eeId";	// NOI18N
	static public final String COMPONENTFACTORYCLASSNAMEJ2EEID2 = "ComponentFactoryClassNameJ2eeId2";	// NOI18N
	static public final String APPLICATION_ADMIN_MODE_TRIGGER = "ApplicationAdminModeTrigger";	// NOI18N
	static public final String SESSION_DESCRIPTOR = "SessionDescriptor";	// NOI18N
	static public final String LIBRARY_CONTEXT_ROOT_OVERRIDE = "LibraryContextRootOverride";	// NOI18N
	static public final String PREFER_APPLICATION_PACKAGES = "PreferApplicationPackages";	// NOI18N
	static public final String PREFER_APPLICATION_RESOURCES = "PreferApplicationResources";	// NOI18N
	static public final String FAST_SWAP = "FastSwap";	// NOI18N
	static public final String COHERENCE_CLUSTER_REF = "CoherenceClusterRef";	// NOI18N
	static public final String RESOURCE_DESCRIPTION = "ResourceDescription";	// NOI18N
	static public final String RESOURCE_ENV_DESCRIPTION = "ResourceEnvDescription";	// NOI18N
	static public final String EJB_REFERENCE_DESCRIPTION = "EjbReferenceDescription";	// NOI18N
	static public final String SERVICE_REFERENCE_DESCRIPTION = "ServiceReferenceDescription";	// NOI18N
	static public final String MESSAGE_DESTINATION_DESCRIPTOR = "MessageDestinationDescriptor";	// NOI18N

	public WeblogicApplication() {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public WeblogicApplication(org.w3c.dom.Node doc, int options) {
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
			doc = GraphManager.createRootElementNode("weblogic-application");	// NOI18N
			if (doc == null)
				throw new Schema2BeansException(Common.getMessage(
					"CantCreateDOMRoot_msg", "weblogic-application"));
		}
		Node n = GraphManager.getElementNode("weblogic-application", doc);	// NOI18N
		if (n == null)
			throw new Schema2BeansException(Common.getMessage(
				"DocRootNotInDOMGraph_msg", "weblogic-application", doc.getFirstChild().getNodeName()));

		this.graphManager.setXmlDocument(doc);

		// Entry point of the createBeans() recursive calls
		this.createBean(n, this.graphManager());
		this.initialize(options);
	}
	public WeblogicApplication(int options)
	{
		super(comparators, runtimeVersion);
		initOptions(options);
	}
	protected void initOptions(int options)
	{
		// The graph manager is allocated in the bean root
		this.graphManager = new GraphManager(this);
		this.createRoot("weblogic-application", "WeblogicApplication",	// NOI18N
			Common.TYPE_1 | Common.TYPE_BEAN, WeblogicApplication.class);

		// Properties (see root bean comments for the bean graph)
		initPropertyTables(32);
		this.createProperty("ejb", 	// NOI18N
			EJB, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			EjbType.class);
		this.createProperty("xml", 	// NOI18N
			XML, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			XmlType.class);
		this.createProperty("jdbc-connection-pool", 	// NOI18N
			JDBC_CONNECTION_POOL, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			JdbcConnectionPoolType.class);
		this.createProperty("security", 	// NOI18N
			SECURITY, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SecurityType.class);
		this.createProperty("application-param", 	// NOI18N
			APPLICATION_PARAM, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ApplicationParamType.class);
		this.createProperty("classloader-structure", 	// NOI18N
			CLASSLOADER_STRUCTURE, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ClassloaderStructureType.class);
		this.createProperty("listener", 	// NOI18N
			LISTENER, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ListenerType.class);
		this.createProperty("singleton-service", 	// NOI18N
			SINGLETON_SERVICE, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SingletonServiceType.class);
		this.createProperty("startup", 	// NOI18N
			STARTUP, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			StartupType.class);
		this.createProperty("shutdown", 	// NOI18N
			SHUTDOWN, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ShutdownType.class);
		this.createProperty("module", 	// NOI18N
			MODULE, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			WeblogicModuleType.class);
		this.createProperty("library-ref", 	// NOI18N
			LIBRARY_REF, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			LibraryRefType.class);
		this.createProperty("fair-share-request", 	// NOI18N
			FAIR_SHARE_REQUEST, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			FairShareRequestClassType.class);
		this.createAttribute(FAIR_SHARE_REQUEST, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("response-time-request", 	// NOI18N
			RESPONSE_TIME_REQUEST, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ResponseTimeRequestClassType.class);
		this.createAttribute(RESPONSE_TIME_REQUEST, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("context-request", 	// NOI18N
			CONTEXT_REQUEST, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ContextRequestClassType.class);
		this.createAttribute(CONTEXT_REQUEST, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("max-threads-constraint", 	// NOI18N
			MAX_THREADS_CONSTRAINT, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MaxThreadsConstraintType.class);
		this.createAttribute(MAX_THREADS_CONSTRAINT, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("min-threads-constraint", 	// NOI18N
			MIN_THREADS_CONSTRAINT, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MinThreadsConstraintType.class);
		this.createAttribute(MIN_THREADS_CONSTRAINT, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("capacity", 	// NOI18N
			CAPACITY, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			CapacityType.class);
		this.createAttribute(CAPACITY, "id", "Id", 
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
		this.createAttribute(COMPONENT_FACTORY_CLASS_NAME, "j2ee:id", "ComponentFactoryClassNameJ2eeId2", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("application-admin-mode-trigger", 	// NOI18N
			APPLICATION_ADMIN_MODE_TRIGGER, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ApplicationAdminModeTriggerType.class);
		this.createAttribute(APPLICATION_ADMIN_MODE_TRIGGER, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("session-descriptor", 	// NOI18N
			SESSION_DESCRIPTOR, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SessionDescriptorType.class);
		this.createAttribute(SESSION_DESCRIPTOR, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("library-context-root-override", 	// NOI18N
			LIBRARY_CONTEXT_ROOT_OVERRIDE, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			LibraryContextRootOverrideType.class);
		this.createProperty("prefer-application-packages", 	// NOI18N
			PREFER_APPLICATION_PACKAGES, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PreferApplicationPackagesType.class);
		this.createProperty("prefer-application-resources", 	// NOI18N
			PREFER_APPLICATION_RESOURCES, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PreferApplicationResourcesType.class);
		this.createProperty("fast-swap", 	// NOI18N
			FAST_SWAP, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			FastSwapType.class);
		this.createProperty("coherence-cluster-ref", 	// NOI18N
			COHERENCE_CLUSTER_REF, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			CoherenceClusterRefType.class);
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
		this.createProperty("message-destination-descriptor", 	// NOI18N
			MESSAGE_DESTINATION_DESCRIPTOR, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MessageDestinationDescriptorType.class);
		this.createAttribute(MESSAGE_DESTINATION_DESCRIPTOR, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute("version", "Version", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {
		setDefaultNamespace("http://xmlns.oracle.com/weblogic/weblogic-application");

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
	public void setEjb(EjbType value) {
		this.setValue(EJB, value);
	}

	//
	public EjbType getEjb() {
		return (EjbType)this.getValue(EJB);
	}

	// This attribute is optional
	public void setXml(XmlType value) {
		this.setValue(XML, value);
	}

	//
	public XmlType getXml() {
		return (XmlType)this.getValue(XML);
	}

	// This attribute is an array, possibly empty
	public void setJdbcConnectionPool(int index, JdbcConnectionPoolType value) {
		this.setValue(JDBC_CONNECTION_POOL, index, value);
	}

	//
	public JdbcConnectionPoolType getJdbcConnectionPool(int index) {
		return (JdbcConnectionPoolType)this.getValue(JDBC_CONNECTION_POOL, index);
	}

	// Return the number of properties
	public int sizeJdbcConnectionPool() {
		return this.size(JDBC_CONNECTION_POOL);
	}

	// This attribute is an array, possibly empty
	public void setJdbcConnectionPool(JdbcConnectionPoolType[] value) {
		this.setValue(JDBC_CONNECTION_POOL, value);
	}

	//
	public JdbcConnectionPoolType[] getJdbcConnectionPool() {
		return (JdbcConnectionPoolType[])this.getValues(JDBC_CONNECTION_POOL);
	}

	// Add a new element returning its index in the list
	public int addJdbcConnectionPool(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.JdbcConnectionPoolType value) {
		int positionOfNewItem = this.addValue(JDBC_CONNECTION_POOL, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeJdbcConnectionPool(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.JdbcConnectionPoolType value) {
		return this.removeValue(JDBC_CONNECTION_POOL, value);
	}

	// This attribute is optional
	public void setSecurity(SecurityType value) {
		this.setValue(SECURITY, value);
	}

	//
	public SecurityType getSecurity() {
		return (SecurityType)this.getValue(SECURITY);
	}

	// This attribute is an array, possibly empty
	public void setApplicationParam(int index, ApplicationParamType value) {
		this.setValue(APPLICATION_PARAM, index, value);
	}

	//
	public ApplicationParamType getApplicationParam(int index) {
		return (ApplicationParamType)this.getValue(APPLICATION_PARAM, index);
	}

	// Return the number of properties
	public int sizeApplicationParam() {
		return this.size(APPLICATION_PARAM);
	}

	// This attribute is an array, possibly empty
	public void setApplicationParam(ApplicationParamType[] value) {
		this.setValue(APPLICATION_PARAM, value);
	}

	//
	public ApplicationParamType[] getApplicationParam() {
		return (ApplicationParamType[])this.getValues(APPLICATION_PARAM);
	}

	// Add a new element returning its index in the list
	public int addApplicationParam(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ApplicationParamType value) {
		int positionOfNewItem = this.addValue(APPLICATION_PARAM, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeApplicationParam(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ApplicationParamType value) {
		return this.removeValue(APPLICATION_PARAM, value);
	}

	// This attribute is optional
	public void setClassloaderStructure(ClassloaderStructureType value) {
		this.setValue(CLASSLOADER_STRUCTURE, value);
	}

	//
	public ClassloaderStructureType getClassloaderStructure() {
		return (ClassloaderStructureType)this.getValue(CLASSLOADER_STRUCTURE);
	}

	// This attribute is an array, possibly empty
	public void setListener(int index, ListenerType value) {
		this.setValue(LISTENER, index, value);
	}

	//
	public ListenerType getListener(int index) {
		return (ListenerType)this.getValue(LISTENER, index);
	}

	// Return the number of properties
	public int sizeListener() {
		return this.size(LISTENER);
	}

	// This attribute is an array, possibly empty
	public void setListener(ListenerType[] value) {
		this.setValue(LISTENER, value);
	}

	//
	public ListenerType[] getListener() {
		return (ListenerType[])this.getValues(LISTENER);
	}

	// Add a new element returning its index in the list
	public int addListener(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ListenerType value) {
		int positionOfNewItem = this.addValue(LISTENER, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeListener(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ListenerType value) {
		return this.removeValue(LISTENER, value);
	}

	// This attribute is an array, possibly empty
	public void setSingletonService(int index, SingletonServiceType value) {
		this.setValue(SINGLETON_SERVICE, index, value);
	}

	//
	public SingletonServiceType getSingletonService(int index) {
		return (SingletonServiceType)this.getValue(SINGLETON_SERVICE, index);
	}

	// Return the number of properties
	public int sizeSingletonService() {
		return this.size(SINGLETON_SERVICE);
	}

	// This attribute is an array, possibly empty
	public void setSingletonService(SingletonServiceType[] value) {
		this.setValue(SINGLETON_SERVICE, value);
	}

	//
	public SingletonServiceType[] getSingletonService() {
		return (SingletonServiceType[])this.getValues(SINGLETON_SERVICE);
	}

	// Add a new element returning its index in the list
	public int addSingletonService(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.SingletonServiceType value) {
		int positionOfNewItem = this.addValue(SINGLETON_SERVICE, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSingletonService(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.SingletonServiceType value) {
		return this.removeValue(SINGLETON_SERVICE, value);
	}

	// This attribute is an array, possibly empty
	public void setStartup(int index, StartupType value) {
		this.setValue(STARTUP, index, value);
	}

	//
	public StartupType getStartup(int index) {
		return (StartupType)this.getValue(STARTUP, index);
	}

	// Return the number of properties
	public int sizeStartup() {
		return this.size(STARTUP);
	}

	// This attribute is an array, possibly empty
	public void setStartup(StartupType[] value) {
		this.setValue(STARTUP, value);
	}

	//
	public StartupType[] getStartup() {
		return (StartupType[])this.getValues(STARTUP);
	}

	// Add a new element returning its index in the list
	public int addStartup(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.StartupType value) {
		int positionOfNewItem = this.addValue(STARTUP, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeStartup(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.StartupType value) {
		return this.removeValue(STARTUP, value);
	}

	// This attribute is an array, possibly empty
	public void setShutdown(int index, ShutdownType value) {
		this.setValue(SHUTDOWN, index, value);
	}

	//
	public ShutdownType getShutdown(int index) {
		return (ShutdownType)this.getValue(SHUTDOWN, index);
	}

	// Return the number of properties
	public int sizeShutdown() {
		return this.size(SHUTDOWN);
	}

	// This attribute is an array, possibly empty
	public void setShutdown(ShutdownType[] value) {
		this.setValue(SHUTDOWN, value);
	}

	//
	public ShutdownType[] getShutdown() {
		return (ShutdownType[])this.getValues(SHUTDOWN);
	}

	// Add a new element returning its index in the list
	public int addShutdown(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ShutdownType value) {
		int positionOfNewItem = this.addValue(SHUTDOWN, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeShutdown(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ShutdownType value) {
		return this.removeValue(SHUTDOWN, value);
	}

	// This attribute is an array, possibly empty
	public void setModule(int index, WeblogicModuleType value) {
		this.setValue(MODULE, index, value);
	}

	//
	public WeblogicModuleType getModule(int index) {
		return (WeblogicModuleType)this.getValue(MODULE, index);
	}

	// Return the number of properties
	public int sizeModule() {
		return this.size(MODULE);
	}

	// This attribute is an array, possibly empty
	public void setModule(WeblogicModuleType[] value) {
		this.setValue(MODULE, value);
	}

	//
	public WeblogicModuleType[] getModule() {
		return (WeblogicModuleType[])this.getValues(MODULE);
	}

	// Add a new element returning its index in the list
	public int addModule(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.WeblogicModuleType value) {
		int positionOfNewItem = this.addValue(MODULE, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeModule(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.WeblogicModuleType value) {
		return this.removeValue(MODULE, value);
	}

	// This attribute is an array, possibly empty
	public void setLibraryRef(int index, LibraryRefType value) {
		this.setValue(LIBRARY_REF, index, value);
	}

	//
	public LibraryRefType getLibraryRef(int index) {
		return (LibraryRefType)this.getValue(LIBRARY_REF, index);
	}

	// Return the number of properties
	public int sizeLibraryRef() {
		return this.size(LIBRARY_REF);
	}

	// This attribute is an array, possibly empty
	public void setLibraryRef(LibraryRefType[] value) {
		this.setValue(LIBRARY_REF, value);
	}

	//
	public LibraryRefType[] getLibraryRef() {
		return (LibraryRefType[])this.getValues(LIBRARY_REF);
	}

	// Add a new element returning its index in the list
	public int addLibraryRef(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.LibraryRefType value) {
		int positionOfNewItem = this.addValue(LIBRARY_REF, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeLibraryRef(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.LibraryRefType value) {
		return this.removeValue(LIBRARY_REF, value);
	}

	// This attribute is an array, possibly empty
	public void setFairShareRequest(int index, FairShareRequestClassType value) {
		this.setValue(FAIR_SHARE_REQUEST, index, value);
	}

	//
	public FairShareRequestClassType getFairShareRequest(int index) {
		return (FairShareRequestClassType)this.getValue(FAIR_SHARE_REQUEST, index);
	}

	// Return the number of properties
	public int sizeFairShareRequest() {
		return this.size(FAIR_SHARE_REQUEST);
	}

	// This attribute is an array, possibly empty
	public void setFairShareRequest(FairShareRequestClassType[] value) {
		this.setValue(FAIR_SHARE_REQUEST, value);
	}

	//
	public FairShareRequestClassType[] getFairShareRequest() {
		return (FairShareRequestClassType[])this.getValues(FAIR_SHARE_REQUEST);
	}

	// Add a new element returning its index in the list
	public int addFairShareRequest(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.FairShareRequestClassType value) {
		int positionOfNewItem = this.addValue(FAIR_SHARE_REQUEST, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeFairShareRequest(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.FairShareRequestClassType value) {
		return this.removeValue(FAIR_SHARE_REQUEST, value);
	}

	// This attribute is an array, possibly empty
	public void setResponseTimeRequest(int index, ResponseTimeRequestClassType value) {
		this.setValue(RESPONSE_TIME_REQUEST, index, value);
	}

	//
	public ResponseTimeRequestClassType getResponseTimeRequest(int index) {
		return (ResponseTimeRequestClassType)this.getValue(RESPONSE_TIME_REQUEST, index);
	}

	// Return the number of properties
	public int sizeResponseTimeRequest() {
		return this.size(RESPONSE_TIME_REQUEST);
	}

	// This attribute is an array, possibly empty
	public void setResponseTimeRequest(ResponseTimeRequestClassType[] value) {
		this.setValue(RESPONSE_TIME_REQUEST, value);
	}

	//
	public ResponseTimeRequestClassType[] getResponseTimeRequest() {
		return (ResponseTimeRequestClassType[])this.getValues(RESPONSE_TIME_REQUEST);
	}

	// Add a new element returning its index in the list
	public int addResponseTimeRequest(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ResponseTimeRequestClassType value) {
		int positionOfNewItem = this.addValue(RESPONSE_TIME_REQUEST, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeResponseTimeRequest(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ResponseTimeRequestClassType value) {
		return this.removeValue(RESPONSE_TIME_REQUEST, value);
	}

	// This attribute is an array, possibly empty
	public void setContextRequest(int index, ContextRequestClassType value) {
		this.setValue(CONTEXT_REQUEST, index, value);
	}

	//
	public ContextRequestClassType getContextRequest(int index) {
		return (ContextRequestClassType)this.getValue(CONTEXT_REQUEST, index);
	}

	// Return the number of properties
	public int sizeContextRequest() {
		return this.size(CONTEXT_REQUEST);
	}

	// This attribute is an array, possibly empty
	public void setContextRequest(ContextRequestClassType[] value) {
		this.setValue(CONTEXT_REQUEST, value);
	}

	//
	public ContextRequestClassType[] getContextRequest() {
		return (ContextRequestClassType[])this.getValues(CONTEXT_REQUEST);
	}

	// Add a new element returning its index in the list
	public int addContextRequest(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ContextRequestClassType value) {
		int positionOfNewItem = this.addValue(CONTEXT_REQUEST, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeContextRequest(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ContextRequestClassType value) {
		return this.removeValue(CONTEXT_REQUEST, value);
	}

	// This attribute is an array, possibly empty
	public void setMaxThreadsConstraint(int index, MaxThreadsConstraintType value) {
		this.setValue(MAX_THREADS_CONSTRAINT, index, value);
	}

	//
	public MaxThreadsConstraintType getMaxThreadsConstraint(int index) {
		return (MaxThreadsConstraintType)this.getValue(MAX_THREADS_CONSTRAINT, index);
	}

	// Return the number of properties
	public int sizeMaxThreadsConstraint() {
		return this.size(MAX_THREADS_CONSTRAINT);
	}

	// This attribute is an array, possibly empty
	public void setMaxThreadsConstraint(MaxThreadsConstraintType[] value) {
		this.setValue(MAX_THREADS_CONSTRAINT, value);
	}

	//
	public MaxThreadsConstraintType[] getMaxThreadsConstraint() {
		return (MaxThreadsConstraintType[])this.getValues(MAX_THREADS_CONSTRAINT);
	}

	// Add a new element returning its index in the list
	public int addMaxThreadsConstraint(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.MaxThreadsConstraintType value) {
		int positionOfNewItem = this.addValue(MAX_THREADS_CONSTRAINT, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeMaxThreadsConstraint(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.MaxThreadsConstraintType value) {
		return this.removeValue(MAX_THREADS_CONSTRAINT, value);
	}

	// This attribute is an array, possibly empty
	public void setMinThreadsConstraint(int index, MinThreadsConstraintType value) {
		this.setValue(MIN_THREADS_CONSTRAINT, index, value);
	}

	//
	public MinThreadsConstraintType getMinThreadsConstraint(int index) {
		return (MinThreadsConstraintType)this.getValue(MIN_THREADS_CONSTRAINT, index);
	}

	// Return the number of properties
	public int sizeMinThreadsConstraint() {
		return this.size(MIN_THREADS_CONSTRAINT);
	}

	// This attribute is an array, possibly empty
	public void setMinThreadsConstraint(MinThreadsConstraintType[] value) {
		this.setValue(MIN_THREADS_CONSTRAINT, value);
	}

	//
	public MinThreadsConstraintType[] getMinThreadsConstraint() {
		return (MinThreadsConstraintType[])this.getValues(MIN_THREADS_CONSTRAINT);
	}

	// Add a new element returning its index in the list
	public int addMinThreadsConstraint(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.MinThreadsConstraintType value) {
		int positionOfNewItem = this.addValue(MIN_THREADS_CONSTRAINT, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeMinThreadsConstraint(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.MinThreadsConstraintType value) {
		return this.removeValue(MIN_THREADS_CONSTRAINT, value);
	}

	// This attribute is an array, possibly empty
	public void setCapacity(int index, CapacityType value) {
		this.setValue(CAPACITY, index, value);
	}

	//
	public CapacityType getCapacity(int index) {
		return (CapacityType)this.getValue(CAPACITY, index);
	}

	// Return the number of properties
	public int sizeCapacity() {
		return this.size(CAPACITY);
	}

	// This attribute is an array, possibly empty
	public void setCapacity(CapacityType[] value) {
		this.setValue(CAPACITY, value);
	}

	//
	public CapacityType[] getCapacity() {
		return (CapacityType[])this.getValues(CAPACITY);
	}

	// Add a new element returning its index in the list
	public int addCapacity(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.CapacityType value) {
		int positionOfNewItem = this.addValue(CAPACITY, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeCapacity(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.CapacityType value) {
		return this.removeValue(CAPACITY, value);
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
	public int addWorkManager(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.WorkManagerType value) {
		int positionOfNewItem = this.addValue(WORK_MANAGER, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeWorkManager(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.WorkManagerType value) {
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
	public void setComponentFactoryClassNameJ2eeId2(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(COMPONENT_FACTORY_CLASS_NAME) == 0) {
			setValue(COMPONENT_FACTORY_CLASS_NAME, "");
		}
		setAttributeValue(COMPONENT_FACTORY_CLASS_NAME, "ComponentFactoryClassNameJ2eeId2", value);
	}

	//
	public java.lang.String getComponentFactoryClassNameJ2eeId2() {
		// If our element does not exist, then the attribute does not exist.
		if (size(COMPONENT_FACTORY_CLASS_NAME) == 0) {
			return null;
		} else {
			return getAttributeValue(COMPONENT_FACTORY_CLASS_NAME, "ComponentFactoryClassNameJ2eeId2");
		}
	}

	// This attribute is optional
	public void setApplicationAdminModeTrigger(ApplicationAdminModeTriggerType value) {
		this.setValue(APPLICATION_ADMIN_MODE_TRIGGER, value);
	}

	//
	public ApplicationAdminModeTriggerType getApplicationAdminModeTrigger() {
		return (ApplicationAdminModeTriggerType)this.getValue(APPLICATION_ADMIN_MODE_TRIGGER);
	}

	// This attribute is optional
	public void setSessionDescriptor(SessionDescriptorType value) {
		this.setValue(SESSION_DESCRIPTOR, value);
	}

	//
	public SessionDescriptorType getSessionDescriptor() {
		return (SessionDescriptorType)this.getValue(SESSION_DESCRIPTOR);
	}

	// This attribute is an array, possibly empty
	public void setLibraryContextRootOverride(int index, LibraryContextRootOverrideType value) {
		this.setValue(LIBRARY_CONTEXT_ROOT_OVERRIDE, index, value);
	}

	//
	public LibraryContextRootOverrideType getLibraryContextRootOverride(int index) {
		return (LibraryContextRootOverrideType)this.getValue(LIBRARY_CONTEXT_ROOT_OVERRIDE, index);
	}

	// Return the number of properties
	public int sizeLibraryContextRootOverride() {
		return this.size(LIBRARY_CONTEXT_ROOT_OVERRIDE);
	}

	// This attribute is an array, possibly empty
	public void setLibraryContextRootOverride(LibraryContextRootOverrideType[] value) {
		this.setValue(LIBRARY_CONTEXT_ROOT_OVERRIDE, value);
	}

	//
	public LibraryContextRootOverrideType[] getLibraryContextRootOverride() {
		return (LibraryContextRootOverrideType[])this.getValues(LIBRARY_CONTEXT_ROOT_OVERRIDE);
	}

	// Add a new element returning its index in the list
	public int addLibraryContextRootOverride(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.LibraryContextRootOverrideType value) {
		int positionOfNewItem = this.addValue(LIBRARY_CONTEXT_ROOT_OVERRIDE, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeLibraryContextRootOverride(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.LibraryContextRootOverrideType value) {
		return this.removeValue(LIBRARY_CONTEXT_ROOT_OVERRIDE, value);
	}

	// This attribute is optional
	public void setPreferApplicationPackages(PreferApplicationPackagesType value) {
		this.setValue(PREFER_APPLICATION_PACKAGES, value);
	}

	//
	public PreferApplicationPackagesType getPreferApplicationPackages() {
		return (PreferApplicationPackagesType)this.getValue(PREFER_APPLICATION_PACKAGES);
	}

	// This attribute is optional
	public void setPreferApplicationResources(PreferApplicationResourcesType value) {
		this.setValue(PREFER_APPLICATION_RESOURCES, value);
	}

	//
	public PreferApplicationResourcesType getPreferApplicationResources() {
		return (PreferApplicationResourcesType)this.getValue(PREFER_APPLICATION_RESOURCES);
	}

	// This attribute is optional
	public void setFastSwap(FastSwapType value) {
		this.setValue(FAST_SWAP, value);
	}

	//
	public FastSwapType getFastSwap() {
		return (FastSwapType)this.getValue(FAST_SWAP);
	}

	// This attribute is optional
	public void setCoherenceClusterRef(CoherenceClusterRefType value) {
		this.setValue(COHERENCE_CLUSTER_REF, value);
	}

	//
	public CoherenceClusterRefType getCoherenceClusterRef() {
		return (CoherenceClusterRefType)this.getValue(COHERENCE_CLUSTER_REF);
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
	public int addResourceDescription(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ResourceDescriptionType value) {
		int positionOfNewItem = this.addValue(RESOURCE_DESCRIPTION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeResourceDescription(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ResourceDescriptionType value) {
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
	public int addResourceEnvDescription(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ResourceEnvDescriptionType value) {
		int positionOfNewItem = this.addValue(RESOURCE_ENV_DESCRIPTION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeResourceEnvDescription(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ResourceEnvDescriptionType value) {
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
	public int addEjbReferenceDescription(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.EjbReferenceDescriptionType value) {
		int positionOfNewItem = this.addValue(EJB_REFERENCE_DESCRIPTION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeEjbReferenceDescription(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.EjbReferenceDescriptionType value) {
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
	public int addServiceReferenceDescription(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ServiceReferenceDescriptionType value) {
		int positionOfNewItem = this.addValue(SERVICE_REFERENCE_DESCRIPTION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeServiceReferenceDescription(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ServiceReferenceDescriptionType value) {
		return this.removeValue(SERVICE_REFERENCE_DESCRIPTION, value);
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
	public int addMessageDestinationDescriptor(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.MessageDestinationDescriptorType value) {
		int positionOfNewItem = this.addValue(MESSAGE_DESTINATION_DESCRIPTOR, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeMessageDestinationDescriptor(org.netbeans.modules.j2ee.weblogic9.dd.ear1211.MessageDestinationDescriptorType value) {
		return this.removeValue(MESSAGE_DESTINATION_DESCRIPTOR, value);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public EjbType newEjbType() {
		return new EjbType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public XmlType newXmlType() {
		return new XmlType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public JdbcConnectionPoolType newJdbcConnectionPoolType() {
		return new JdbcConnectionPoolType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SecurityType newSecurityType() {
		return new SecurityType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ApplicationParamType newApplicationParamType() {
		return new ApplicationParamType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ClassloaderStructureType newClassloaderStructureType() {
		return new ClassloaderStructureType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ListenerType newListenerType() {
		return new ListenerType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SingletonServiceType newSingletonServiceType() {
		return new SingletonServiceType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public StartupType newStartupType() {
		return new StartupType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ShutdownType newShutdownType() {
		return new ShutdownType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public WeblogicModuleType newWeblogicModuleType() {
		return new WeblogicModuleType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public LibraryRefType newLibraryRefType() {
		return new LibraryRefType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public FairShareRequestClassType newFairShareRequestClassType() {
		return new FairShareRequestClassType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ResponseTimeRequestClassType newResponseTimeRequestClassType() {
		return new ResponseTimeRequestClassType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ContextRequestClassType newContextRequestClassType() {
		return new ContextRequestClassType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public MaxThreadsConstraintType newMaxThreadsConstraintType() {
		return new MaxThreadsConstraintType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public MinThreadsConstraintType newMinThreadsConstraintType() {
		return new MinThreadsConstraintType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public CapacityType newCapacityType() {
		return new CapacityType();
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
	public ApplicationAdminModeTriggerType newApplicationAdminModeTriggerType() {
		return new ApplicationAdminModeTriggerType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SessionDescriptorType newSessionDescriptorType() {
		return new SessionDescriptorType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public LibraryContextRootOverrideType newLibraryContextRootOverrideType() {
		return new LibraryContextRootOverrideType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public PreferApplicationPackagesType newPreferApplicationPackagesType() {
		return new PreferApplicationPackagesType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public PreferApplicationResourcesType newPreferApplicationResourcesType() {
		return new PreferApplicationResourcesType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public FastSwapType newFastSwapType() {
		return new FastSwapType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public CoherenceClusterRefType newCoherenceClusterRefType() {
		return new CoherenceClusterRefType();
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

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public MessageDestinationDescriptorType newMessageDestinationDescriptorType() {
		return new MessageDestinationDescriptorType();
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
	public static WeblogicApplication createGraph(org.w3c.dom.Node doc) {
		return new WeblogicApplication(doc, Common.NO_DEFAULT_VALUES);
	}

	public static WeblogicApplication createGraph(java.io.File f) throws java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return createGraph(in, false);
		} finally {
			in.close();
		}
	}

	public static WeblogicApplication createGraph(java.io.InputStream in) {
		return createGraph(in, false);
	}

	public static WeblogicApplication createGraph(java.io.InputStream in, boolean validate) {
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
	public static WeblogicApplication createGraph() {
		return new WeblogicApplication();
	}


	
                    public String getId() {
                        return null;
                    }

                    public void setId(String id) {
                       // noop
                    }
                
	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
		boolean restrictionFailure = false;
		boolean restrictionPassed = false;
		// Validating property version
		// Validating property ejb
		if (getEjb() != null) {
			getEjb().validate();
		}
		// Validating property xml
		if (getXml() != null) {
			getXml().validate();
		}
		// Validating property jdbcConnectionPool
		for (int _index = 0; _index < sizeJdbcConnectionPool(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.JdbcConnectionPoolType element = getJdbcConnectionPool(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property security
		if (getSecurity() != null) {
			getSecurity().validate();
		}
		// Validating property applicationParam
		for (int _index = 0; _index < sizeApplicationParam(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ApplicationParamType element = getApplicationParam(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property classloaderStructure
		if (getClassloaderStructure() != null) {
			getClassloaderStructure().validate();
		}
		// Validating property listener
		for (int _index = 0; _index < sizeListener(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ListenerType element = getListener(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property singletonService
		for (int _index = 0; _index < sizeSingletonService(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.SingletonServiceType element = getSingletonService(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property startup
		for (int _index = 0; _index < sizeStartup(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.StartupType element = getStartup(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property shutdown
		for (int _index = 0; _index < sizeShutdown(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ShutdownType element = getShutdown(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property module
		for (int _index = 0; _index < sizeModule(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.WeblogicModuleType element = getModule(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property libraryRef
		for (int _index = 0; _index < sizeLibraryRef(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.LibraryRefType element = getLibraryRef(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property fairShareRequest
		for (int _index = 0; _index < sizeFairShareRequest(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.FairShareRequestClassType element = getFairShareRequest(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property responseTimeRequest
		for (int _index = 0; _index < sizeResponseTimeRequest(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ResponseTimeRequestClassType element = getResponseTimeRequest(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property contextRequest
		for (int _index = 0; _index < sizeContextRequest(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ContextRequestClassType element = getContextRequest(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property maxThreadsConstraint
		for (int _index = 0; _index < sizeMaxThreadsConstraint(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.MaxThreadsConstraintType element = getMaxThreadsConstraint(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property minThreadsConstraint
		for (int _index = 0; _index < sizeMinThreadsConstraint(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.MinThreadsConstraintType element = getMinThreadsConstraint(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property capacity
		for (int _index = 0; _index < sizeCapacity(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.CapacityType element = getCapacity(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property workManager
		for (int _index = 0; _index < sizeWorkManager(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.WorkManagerType element = getWorkManager(_index);
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
		// Validating property componentFactoryClassNameJ2eeId2
		if (getComponentFactoryClassNameJ2eeId2() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getComponentFactoryClassNameJ2eeId2() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "componentFactoryClassNameJ2eeId2", this);	// NOI18N
			}
		}
		// Validating property applicationAdminModeTrigger
		if (getApplicationAdminModeTrigger() != null) {
			getApplicationAdminModeTrigger().validate();
		}
		// Validating property sessionDescriptor
		if (getSessionDescriptor() != null) {
			getSessionDescriptor().validate();
		}
		// Validating property libraryContextRootOverride
		for (int _index = 0; _index < sizeLibraryContextRootOverride(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.LibraryContextRootOverrideType element = getLibraryContextRootOverride(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property preferApplicationPackages
		if (getPreferApplicationPackages() != null) {
			getPreferApplicationPackages().validate();
		}
		// Validating property preferApplicationResources
		if (getPreferApplicationResources() != null) {
			getPreferApplicationResources().validate();
		}
		// Validating property fastSwap
		if (getFastSwap() != null) {
			getFastSwap().validate();
		}
		// Validating property coherenceClusterRef
		if (getCoherenceClusterRef() != null) {
			getCoherenceClusterRef().validate();
		}
		// Validating property resourceDescription
		for (int _index = 0; _index < sizeResourceDescription(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ResourceDescriptionType element = getResourceDescription(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property resourceEnvDescription
		for (int _index = 0; _index < sizeResourceEnvDescription(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ResourceEnvDescriptionType element = getResourceEnvDescription(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property ejbReferenceDescription
		for (int _index = 0; _index < sizeEjbReferenceDescription(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.EjbReferenceDescriptionType element = getEjbReferenceDescription(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property serviceReferenceDescription
		for (int _index = 0; _index < sizeServiceReferenceDescription(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.ServiceReferenceDescriptionType element = getServiceReferenceDescription(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property messageDestinationDescriptor
		for (int _index = 0; _index < sizeMessageDestinationDescriptor(); 
			++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1211.MessageDestinationDescriptorType element = getMessageDestinationDescriptor(_index);
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
		str.append(indent);
		str.append("Ejb");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getEjb();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(EJB, 0, str, indent);

		str.append(indent);
		str.append("Xml");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getXml();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(XML, 0, str, indent);

		str.append(indent);
		str.append("JdbcConnectionPool["+this.sizeJdbcConnectionPool()+"]");	// NOI18N
		for(int i=0; i<this.sizeJdbcConnectionPool(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getJdbcConnectionPool(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(JDBC_CONNECTION_POOL, i, str, indent);
		}

		str.append(indent);
		str.append("Security");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getSecurity();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(SECURITY, 0, str, indent);

		str.append(indent);
		str.append("ApplicationParam["+this.sizeApplicationParam()+"]");	// NOI18N
		for(int i=0; i<this.sizeApplicationParam(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getApplicationParam(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(APPLICATION_PARAM, i, str, indent);
		}

		str.append(indent);
		str.append("ClassloaderStructure");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getClassloaderStructure();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CLASSLOADER_STRUCTURE, 0, str, indent);

		str.append(indent);
		str.append("Listener["+this.sizeListener()+"]");	// NOI18N
		for(int i=0; i<this.sizeListener(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getListener(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(LISTENER, i, str, indent);
		}

		str.append(indent);
		str.append("SingletonService["+this.sizeSingletonService()+"]");	// NOI18N
		for(int i=0; i<this.sizeSingletonService(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getSingletonService(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SINGLETON_SERVICE, i, str, indent);
		}

		str.append(indent);
		str.append("Startup["+this.sizeStartup()+"]");	// NOI18N
		for(int i=0; i<this.sizeStartup(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getStartup(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(STARTUP, i, str, indent);
		}

		str.append(indent);
		str.append("Shutdown["+this.sizeShutdown()+"]");	// NOI18N
		for(int i=0; i<this.sizeShutdown(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getShutdown(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SHUTDOWN, i, str, indent);
		}

		str.append(indent);
		str.append("Module["+this.sizeModule()+"]");	// NOI18N
		for(int i=0; i<this.sizeModule(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getModule(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MODULE, i, str, indent);
		}

		str.append(indent);
		str.append("LibraryRef["+this.sizeLibraryRef()+"]");	// NOI18N
		for(int i=0; i<this.sizeLibraryRef(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getLibraryRef(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(LIBRARY_REF, i, str, indent);
		}

		str.append(indent);
		str.append("FairShareRequest["+this.sizeFairShareRequest()+"]");	// NOI18N
		for(int i=0; i<this.sizeFairShareRequest(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getFairShareRequest(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(FAIR_SHARE_REQUEST, i, str, indent);
		}

		str.append(indent);
		str.append("ResponseTimeRequest["+this.sizeResponseTimeRequest()+"]");	// NOI18N
		for(int i=0; i<this.sizeResponseTimeRequest(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getResponseTimeRequest(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(RESPONSE_TIME_REQUEST, i, str, indent);
		}

		str.append(indent);
		str.append("ContextRequest["+this.sizeContextRequest()+"]");	// NOI18N
		for(int i=0; i<this.sizeContextRequest(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getContextRequest(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CONTEXT_REQUEST, i, str, indent);
		}

		str.append(indent);
		str.append("MaxThreadsConstraint["+this.sizeMaxThreadsConstraint()+"]");	// NOI18N
		for(int i=0; i<this.sizeMaxThreadsConstraint(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getMaxThreadsConstraint(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MAX_THREADS_CONSTRAINT, i, str, indent);
		}

		str.append(indent);
		str.append("MinThreadsConstraint["+this.sizeMinThreadsConstraint()+"]");	// NOI18N
		for(int i=0; i<this.sizeMinThreadsConstraint(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getMinThreadsConstraint(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MIN_THREADS_CONSTRAINT, i, str, indent);
		}

		str.append(indent);
		str.append("Capacity["+this.sizeCapacity()+"]");	// NOI18N
		for(int i=0; i<this.sizeCapacity(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getCapacity(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CAPACITY, i, str, indent);
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
		str.append("ApplicationAdminModeTrigger");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getApplicationAdminModeTrigger();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(APPLICATION_ADMIN_MODE_TRIGGER, 0, str, indent);

		str.append(indent);
		str.append("SessionDescriptor");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getSessionDescriptor();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(SESSION_DESCRIPTOR, 0, str, indent);

		str.append(indent);
		str.append("LibraryContextRootOverride["+this.sizeLibraryContextRootOverride()+"]");	// NOI18N
		for(int i=0; i<this.sizeLibraryContextRootOverride(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getLibraryContextRootOverride(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(LIBRARY_CONTEXT_ROOT_OVERRIDE, i, str, indent);
		}

		str.append(indent);
		str.append("PreferApplicationPackages");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getPreferApplicationPackages();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(PREFER_APPLICATION_PACKAGES, 0, str, indent);

		str.append(indent);
		str.append("PreferApplicationResources");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getPreferApplicationResources();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(PREFER_APPLICATION_RESOURCES, 0, str, indent);

		str.append(indent);
		str.append("FastSwap");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getFastSwap();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(FAST_SWAP, 0, str, indent);

		str.append(indent);
		str.append("CoherenceClusterRef");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getCoherenceClusterRef();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(COHERENCE_CLUSTER_REF, 0, str, indent);

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

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("WeblogicApplication\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

