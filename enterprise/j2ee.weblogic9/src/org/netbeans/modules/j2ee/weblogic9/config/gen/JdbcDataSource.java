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
 *	This generated bean class JdbcDataSource matches the schema element 'jdbc-data-source'.
 *
 *	Generated on Tue Jul 25 03:27:07 PDT 2017
 *
 *	This class matches the root element of the XML Schema,
 *	and is the root of the following bean graph:
 *
 *	jdbcDataSource <jdbc-data-source> : JdbcDataSource
 *		[attr: version CDATA #IMPLIED  : java.lang.String]
 *		name <name> : java.lang.String
 *		internalProperties <internal-properties> : JdbcPropertiesType[0,1]
 *			property2 <property> : JdbcPropertyType[0,n]
 *				name <name> : java.lang.String[0,1]
 *				value <value> : java.lang.String[0,1]
 *		jdbcDriverParams <jdbc-driver-params> : JdbcDriverParamsType[0,1]
 *			url <url> : java.lang.String[0,1]
 *			driverName <driver-name> : java.lang.String[0,1]
 *			properties <properties> : JdbcPropertiesType[0,1]
 *				property2 <property> : JdbcPropertyType[0,n]
 *					name <name> : java.lang.String[0,1]
 *					value <value> : java.lang.String[0,1]
 *			passwordEncrypted <password-encrypted> : java.lang.String[0,1]
 *			useXaDataSourceInterface <use-xa-data-source-interface> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			usePasswordIndirection <use-password-indirection> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		jdbcConnectionPoolParams <jdbc-connection-pool-params> : JdbcConnectionPoolParamsType[0,1]
 *			initialCapacity <initial-capacity> : long[0,1] 	[minInclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			maxCapacity <max-capacity> : long[0,1] 	[minExclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			capacityIncrement <capacity-increment> : long[0,1] 	[minExclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			shrinkFrequencySeconds <shrink-frequency-seconds> : long[0,1] 	[minInclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			highestNumWaiters <highest-num-waiters> : long[0,1] 	[minInclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			connectionCreationRetryFrequencySeconds <connection-creation-retry-frequency-seconds> : long[0,1] 	[minInclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			connectionReserveTimeoutSeconds <connection-reserve-timeout-seconds> : java.math.BigInteger[0,1]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			testFrequencySeconds <test-frequency-seconds> : long[0,1] 	[minInclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			testConnectionsOnReserve <test-connections-on-reserve> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			profileHarvestFrequencySeconds <profile-harvest-frequency-seconds> : long[0,1] 	[minInclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			ignoreInUseConnectionsEnabled <ignore-in-use-connections-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			inactiveConnectionTimeoutSeconds <inactive-connection-timeout-seconds> : long[0,1] 	[minInclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			testTableName <test-table-name> : java.lang.String[0,1]
 *			loginDelaySeconds <login-delay-seconds> : long[0,1] 	[minInclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			initSql <init-sql> : java.lang.String[0,1]
 *			statementCacheSize <statement-cache-size> : long[0,1] 	[minInclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			statementCacheType <statement-cache-type> : java.lang.String[0,1] 	[enumeration (LRU), enumeration (FIXED)]
 *			removeInfectedConnections <remove-infected-connections> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			secondsToTrustAnIdlePoolConnection <seconds-to-trust-an-idle-pool-connection> : long[0,1] 	[minInclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			statementTimeout <statement-timeout> : java.math.BigInteger[0,1]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			profileType <profile-type> : long[0,1] 	[minInclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			jdbcXaDebugLevel <jdbc-xa-debug-level> : java.math.BigInteger[0,1]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			credentialMappingEnabled <credential-mapping-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			driverInterceptor <driver-interceptor> : java.lang.String[0,1]
 *			pinnedToThread <pinned-to-thread> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			identityBasedConnectionPoolingEnabled <identity-based-connection-pooling-enabled> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *		jdbcDataSourceParams <jdbc-data-source-params> : JdbcDataSourceParamsType[0,1]
 *			jndiName <jndi-name> : java.lang.String[0,n]
 *			scope <scope> : java.lang.String[0,1] 	[enumeration (Global), enumeration (Application)]
 *			rowPrefetch <row-prefetch> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			rowPrefetchSize <row-prefetch-size> : long[0,1] 	[minExclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			streamChunkSize <stream-chunk-size> : long[0,1] 	[minExclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			algorithmType <algorithm-type> : java.lang.String[0,1] 	[enumeration (Failover), enumeration (Load-Balancing)]
 *			dataSourceList <data-source-list> : java.lang.String[0,1]
 *			connectionPoolFailoverCallbackHandler <connection-pool-failover-callback-handler> : java.lang.String[0,1]
 *			failoverRequestIfBusy <failover-request-if-busy> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			globalTransactionsProtocol <global-transactions-protocol> : java.lang.String[0,1] 	[enumeration (TwoPhaseCommit), enumeration (LoggingLastResource), enumeration (EmulateTwoPhaseCommit), enumeration (OnePhaseCommit), enumeration (None)]
 *		jdbcXaParams <jdbc-xa-params> : JdbcXaParamsType[0,1]
 *			keepXaConnTillTxComplete <keep-xa-conn-till-tx-complete> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			needTxCtxOnClose <need-tx-ctx-on-close> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			xaEndOnlyOnce <xa-end-only-once> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			newXaConnForCommit <new-xa-conn-for-commit> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			keepLogicalConnOpenOnRelease <keep-logical-conn-open-on-release> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			resourceHealthMonitoring <resource-health-monitoring> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			recoverOnlyOnce <recover-only-once> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			xaSetTransactionTimeout <xa-set-transaction-timeout> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			xaTransactionTimeout <xa-transaction-timeout> : long[0,1] 	[minInclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			rollbackLocalTxUponConnClose <rollback-local-tx-upon-conn-close> : boolean[0,1] 	[pattern ((true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0))]
 *			xaRetryDurationSeconds <xa-retry-duration-seconds> : long[0,1] 	[minInclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *			xaRetryIntervalSeconds <xa-retry-interval-seconds> : long[0,1] 	[minInclusive (0)]
 *				[attr: j2ee:id CDATA #IMPLIED  : java.lang.String] 	[whiteSpace (collapse)]
 *
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.config.gen;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.*;

// BEGIN_NOI18N

public class JdbcDataSource extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	private static final String SERIALIZATION_HELPER_CHARSET = "UTF-8";	// NOI18N

	static public final String VERSION = "Version";	// NOI18N
	static public final String NAME = "Name";	// NOI18N
	static public final String INTERNAL_PROPERTIES = "InternalProperties";	// NOI18N
	static public final String JDBC_DRIVER_PARAMS = "JdbcDriverParams";	// NOI18N
	static public final String JDBC_CONNECTION_POOL_PARAMS = "JdbcConnectionPoolParams";	// NOI18N
	static public final String JDBC_DATA_SOURCE_PARAMS = "JdbcDataSourceParams";	// NOI18N
	static public final String JDBC_XA_PARAMS = "JdbcXaParams";	// NOI18N

	public JdbcDataSource() {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public JdbcDataSource(org.w3c.dom.Node doc, int options) {
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
			doc = GraphManager.createRootElementNode("jdbc-data-source");	// NOI18N
			if (doc == null)
				throw new Schema2BeansException(Common.getMessage(
					"CantCreateDOMRoot_msg", "jdbc-data-source"));
		}
		Node n = GraphManager.getElementNode("jdbc-data-source", doc);	// NOI18N
		if (n == null)
			throw new Schema2BeansException(Common.getMessage(
				"DocRootNotInDOMGraph_msg", "jdbc-data-source", doc.getFirstChild().getNodeName()));

		this.graphManager.setXmlDocument(doc);

		// Entry point of the createBeans() recursive calls
		this.createBean(n, this.graphManager());
		this.initialize(options);
	}
	public JdbcDataSource(int options)
	{
		super(comparators, runtimeVersion);
		initOptions(options);
	}
	protected void initOptions(int options)
	{
		// The graph manager is allocated in the bean root
		this.graphManager = new GraphManager(this);
		this.createRoot("jdbc-data-source", "JdbcDataSource",	// NOI18N
			Common.TYPE_1 | Common.TYPE_BEAN, JdbcDataSource.class);

		// Properties (see root bean comments for the bean graph)
		initPropertyTables(6);
		this.createProperty("name", 	// NOI18N
			NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("internal-properties", 	// NOI18N
			INTERNAL_PROPERTIES, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			JdbcPropertiesType.class);
		this.createProperty("jdbc-driver-params", 	// NOI18N
			JDBC_DRIVER_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			JdbcDriverParamsType.class);
		this.createProperty("jdbc-connection-pool-params", 	// NOI18N
			JDBC_CONNECTION_POOL_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			JdbcConnectionPoolParamsType.class);
		this.createProperty("jdbc-data-source-params", 	// NOI18N
			JDBC_DATA_SOURCE_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			JdbcDataSourceParamsType.class);
		this.createProperty("jdbc-xa-params", 	// NOI18N
			JDBC_XA_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			JdbcXaParamsType.class);
		this.createAttribute("version", "Version", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {
		setDefaultNamespace("http://xmlns.oracle.com/weblogic/jdbc-data-source");

	}

	// This attribute is optional
	public void setVersion(java.lang.String value) {
		setAttributeValue(VERSION, value);
	}

	//
	public java.lang.String getVersion() {
		return getAttributeValue(VERSION);
	}

	// This attribute is mandatory
	public void setName(java.lang.String value) {
		this.setValue(NAME, value);
	}

	//
	public java.lang.String getName() {
		return (java.lang.String)this.getValue(NAME);
	}

	// This attribute is optional
	public void setInternalProperties(JdbcPropertiesType value) {
		this.setValue(INTERNAL_PROPERTIES, value);
	}

	//
	public JdbcPropertiesType getInternalProperties() {
		return (JdbcPropertiesType)this.getValue(INTERNAL_PROPERTIES);
	}

	// This attribute is optional
	public void setJdbcDriverParams(JdbcDriverParamsType value) {
		this.setValue(JDBC_DRIVER_PARAMS, value);
	}

	//
	public JdbcDriverParamsType getJdbcDriverParams() {
		return (JdbcDriverParamsType)this.getValue(JDBC_DRIVER_PARAMS);
	}

	// This attribute is optional
	public void setJdbcConnectionPoolParams(JdbcConnectionPoolParamsType value) {
		this.setValue(JDBC_CONNECTION_POOL_PARAMS, value);
	}

	//
	public JdbcConnectionPoolParamsType getJdbcConnectionPoolParams() {
		return (JdbcConnectionPoolParamsType)this.getValue(JDBC_CONNECTION_POOL_PARAMS);
	}

	// This attribute is optional
	public void setJdbcDataSourceParams(JdbcDataSourceParamsType value) {
		this.setValue(JDBC_DATA_SOURCE_PARAMS, value);
	}

	//
	public JdbcDataSourceParamsType getJdbcDataSourceParams() {
		return (JdbcDataSourceParamsType)this.getValue(JDBC_DATA_SOURCE_PARAMS);
	}

	// This attribute is optional
	public void setJdbcXaParams(JdbcXaParamsType value) {
		this.setValue(JDBC_XA_PARAMS, value);
	}

	//
	public JdbcXaParamsType getJdbcXaParams() {
		return (JdbcXaParamsType)this.getValue(JDBC_XA_PARAMS);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public JdbcPropertiesType newJdbcPropertiesType() {
		return new JdbcPropertiesType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public JdbcDriverParamsType newJdbcDriverParamsType() {
		return new JdbcDriverParamsType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public JdbcConnectionPoolParamsType newJdbcConnectionPoolParamsType() {
		return new JdbcConnectionPoolParamsType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public JdbcDataSourceParamsType newJdbcDataSourceParamsType() {
		return new JdbcDataSourceParamsType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public JdbcXaParamsType newJdbcXaParamsType() {
		return new JdbcXaParamsType();
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
	public static JdbcDataSource createGraph(org.w3c.dom.Node doc) {
		return new JdbcDataSource(doc, Common.NO_DEFAULT_VALUES);
	}

	public static JdbcDataSource createGraph(java.io.File f) throws java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return createGraph(in, false);
		} finally {
			in.close();
		}
	}

	public static JdbcDataSource createGraph(java.io.InputStream in) {
		return createGraph(in, false);
	}

	public static JdbcDataSource createGraph(java.io.InputStream in, boolean validate) {
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
	public static JdbcDataSource createGraph() {
		return new JdbcDataSource();
	}

	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
		boolean restrictionFailure = false;
		boolean restrictionPassed = false;
		// Validating property version
		// Validating property name
		if (getName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "name", this);	// NOI18N
		}
		// Validating property internalProperties
		if (getInternalProperties() != null) {
			getInternalProperties().validate();
		}
		// Validating property jdbcDriverParams
		if (getJdbcDriverParams() != null) {
			getJdbcDriverParams().validate();
		}
		// Validating property jdbcConnectionPoolParams
		if (getJdbcConnectionPoolParams() != null) {
			getJdbcConnectionPoolParams().validate();
		}
		// Validating property jdbcDataSourceParams
		if (getJdbcDataSourceParams() != null) {
			getJdbcDataSourceParams().validate();
		}
		// Validating property jdbcXaParams
		if (getJdbcXaParams() != null) {
			getJdbcXaParams().validate();
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
		str.append("Name");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NAME, 0, str, indent);

		str.append(indent);
		str.append("InternalProperties");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getInternalProperties();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(INTERNAL_PROPERTIES, 0, str, indent);

		str.append(indent);
		str.append("JdbcDriverParams");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getJdbcDriverParams();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(JDBC_DRIVER_PARAMS, 0, str, indent);

		str.append(indent);
		str.append("JdbcConnectionPoolParams");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getJdbcConnectionPoolParams();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(JDBC_CONNECTION_POOL_PARAMS, 0, str, indent);

		str.append(indent);
		str.append("JdbcDataSourceParams");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getJdbcDataSourceParams();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(JDBC_DATA_SOURCE_PARAMS, 0, str, indent);

		str.append(indent);
		str.append("JdbcXaParams");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getJdbcXaParams();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(JDBC_XA_PARAMS, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("JdbcDataSource\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

