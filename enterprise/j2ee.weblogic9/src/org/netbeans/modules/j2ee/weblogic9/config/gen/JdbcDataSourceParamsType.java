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
 *	This generated bean class JdbcDataSourceParamsType matches the schema element 'jdbc-data-source-paramsType'.
 *  The root bean class is JdbcDataSource
 *
 *	Generated on Tue Jul 25 03:27:07 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.config.gen;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class JdbcDataSourceParamsType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String JNDI_NAME = "JndiName";	// NOI18N
	static public final String SCOPE = "Scope";	// NOI18N
	static public final String ROW_PREFETCH = "RowPrefetch";	// NOI18N
	static public final String ROW_PREFETCH_SIZE = "RowPrefetchSize";	// NOI18N
	static public final String ROWPREFETCHSIZEJ2EEID = "RowPrefetchSizeJ2eeId";	// NOI18N
	static public final String STREAM_CHUNK_SIZE = "StreamChunkSize";	// NOI18N
	static public final String STREAMCHUNKSIZEJ2EEID = "StreamChunkSizeJ2eeId";	// NOI18N
	static public final String ALGORITHM_TYPE = "AlgorithmType";	// NOI18N
	static public final String DATA_SOURCE_LIST = "DataSourceList";	// NOI18N
	static public final String CONNECTION_POOL_FAILOVER_CALLBACK_HANDLER = "ConnectionPoolFailoverCallbackHandler";	// NOI18N
	static public final String FAILOVER_REQUEST_IF_BUSY = "FailoverRequestIfBusy";	// NOI18N
	static public final String GLOBAL_TRANSACTIONS_PROTOCOL = "GlobalTransactionsProtocol";	// NOI18N

	public JdbcDataSourceParamsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public JdbcDataSourceParamsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(10);
		this.createProperty("jndi-name", 	// NOI18N
			JNDI_NAME, 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("scope", 	// NOI18N
			SCOPE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("row-prefetch", 	// NOI18N
			ROW_PREFETCH, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("row-prefetch-size", 	// NOI18N
			ROW_PREFETCH_SIZE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(ROW_PREFETCH_SIZE, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("stream-chunk-size", 	// NOI18N
			STREAM_CHUNK_SIZE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(STREAM_CHUNK_SIZE, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("algorithm-type", 	// NOI18N
			ALGORITHM_TYPE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("data-source-list", 	// NOI18N
			DATA_SOURCE_LIST, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("connection-pool-failover-callback-handler", 	// NOI18N
			CONNECTION_POOL_FAILOVER_CALLBACK_HANDLER, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("failover-request-if-busy", 	// NOI18N
			FAILOVER_REQUEST_IF_BUSY, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("global-transactions-protocol", 	// NOI18N
			GLOBAL_TRANSACTIONS_PROTOCOL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is an array, possibly empty
	public void setJndiName(int index, java.lang.String value) {
		this.setValue(JNDI_NAME, index, value);
	}

	//
	public java.lang.String getJndiName(int index) {
		return (java.lang.String)this.getValue(JNDI_NAME, index);
	}

	// Return the number of properties
	public int sizeJndiName() {
		return this.size(JNDI_NAME);
	}

	// This attribute is an array, possibly empty
	public void setJndiName(java.lang.String[] value) {
		this.setValue(JNDI_NAME, value);
	}

	//
	public java.lang.String[] getJndiName() {
		return (java.lang.String[])this.getValues(JNDI_NAME);
	}

	// Add a new element returning its index in the list
	public int addJndiName(java.lang.String value) {
		int positionOfNewItem = this.addValue(JNDI_NAME, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeJndiName(java.lang.String value) {
		return this.removeValue(JNDI_NAME, value);
	}

	// This attribute is optional
	public void setScope(java.lang.String value) {
		this.setValue(SCOPE, value);
	}

	//
	public java.lang.String getScope() {
		return (java.lang.String)this.getValue(SCOPE);
	}

	// This attribute is optional
	public void setRowPrefetch(boolean value) {
		this.setValue(ROW_PREFETCH, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isRowPrefetch() {
		Boolean ret = (Boolean)this.getValue(ROW_PREFETCH);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setRowPrefetchSize(long value) {
		this.setValue(ROW_PREFETCH_SIZE, java.lang.Long.valueOf(value));
	}

	//
	public long getRowPrefetchSize() {
		Long ret = (Long)this.getValue(ROW_PREFETCH_SIZE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"ROW_PREFETCH_SIZE", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setRowPrefetchSizeJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(ROW_PREFETCH_SIZE) == 0) {
			setValue(ROW_PREFETCH_SIZE, "");
		}
		setAttributeValue(ROW_PREFETCH_SIZE, "J2eeId", value);
	}

	//
	public java.lang.String getRowPrefetchSizeJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(ROW_PREFETCH_SIZE) == 0) {
			return null;
		} else {
			return getAttributeValue(ROW_PREFETCH_SIZE, "J2eeId");
		}
	}

	// This attribute is optional
	public void setStreamChunkSize(long value) {
		this.setValue(STREAM_CHUNK_SIZE, java.lang.Long.valueOf(value));
	}

	//
	public long getStreamChunkSize() {
		Long ret = (Long)this.getValue(STREAM_CHUNK_SIZE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"STREAM_CHUNK_SIZE", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setStreamChunkSizeJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(ROW_PREFETCH_SIZE) == 0) {
			setValue(ROW_PREFETCH_SIZE, "");
		}
		setAttributeValue(ROW_PREFETCH_SIZE, "J2eeId", value);
	}

	//
	public java.lang.String getStreamChunkSizeJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(ROW_PREFETCH_SIZE) == 0) {
			return null;
		} else {
			return getAttributeValue(ROW_PREFETCH_SIZE, "J2eeId");
		}
	}

	// This attribute is optional
	public void setAlgorithmType(java.lang.String value) {
		this.setValue(ALGORITHM_TYPE, value);
	}

	//
	public java.lang.String getAlgorithmType() {
		return (java.lang.String)this.getValue(ALGORITHM_TYPE);
	}

	// This attribute is optional
	public void setDataSourceList(java.lang.String value) {
		this.setValue(DATA_SOURCE_LIST, value);
	}

	//
	public java.lang.String getDataSourceList() {
		return (java.lang.String)this.getValue(DATA_SOURCE_LIST);
	}

	// This attribute is optional
	public void setConnectionPoolFailoverCallbackHandler(java.lang.String value) {
		this.setValue(CONNECTION_POOL_FAILOVER_CALLBACK_HANDLER, value);
	}

	//
	public java.lang.String getConnectionPoolFailoverCallbackHandler() {
		return (java.lang.String)this.getValue(CONNECTION_POOL_FAILOVER_CALLBACK_HANDLER);
	}

	// This attribute is optional
	public void setFailoverRequestIfBusy(boolean value) {
		this.setValue(FAILOVER_REQUEST_IF_BUSY, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isFailoverRequestIfBusy() {
		Boolean ret = (Boolean)this.getValue(FAILOVER_REQUEST_IF_BUSY);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setGlobalTransactionsProtocol(java.lang.String value) {
		this.setValue(GLOBAL_TRANSACTIONS_PROTOCOL, value);
	}

	//
	public java.lang.String getGlobalTransactionsProtocol() {
		return (java.lang.String)this.getValue(GLOBAL_TRANSACTIONS_PROTOCOL);
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
		// Validating property jndiName
		// Validating property scope
		if (getScope() != null) {
			final java.lang.String[] enumRestrictionScope = {"Global", "Application"};
			restrictionFailure = true;
			for (int _index2 = 0; _index2 < enumRestrictionScope.length; 
				++_index2) {
				if (enumRestrictionScope[_index2].equals(getScope())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getScope() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "scope", this);	// NOI18N
			}
		}
		// Validating property rowPrefetch
		{
			boolean patternPassed = false;
			if ((isRowPrefetch() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isRowPrefetch()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "rowPrefetch", this);	// NOI18N
		}
		// Validating property rowPrefetchSize
		if (getRowPrefetchSize() - 0L <= 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getRowPrefetchSize() minExclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "rowPrefetchSize", this);	// NOI18N
		}
		// Validating property rowPrefetchSizeJ2eeId
		if (getRowPrefetchSizeJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRowPrefetchSizeJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "rowPrefetchSizeJ2eeId", this);	// NOI18N
			}
		}
		// Validating property streamChunkSize
		if (getStreamChunkSize() - 0L <= 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getStreamChunkSize() minExclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "streamChunkSize", this);	// NOI18N
		}
		// Validating property streamChunkSizeJ2eeId
		if (getStreamChunkSizeJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getStreamChunkSizeJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "streamChunkSizeJ2eeId", this);	// NOI18N
			}
		}
		// Validating property algorithmType
		if (getAlgorithmType() != null) {
			final java.lang.String[] enumRestrictionAlgorithmType = {"Failover", "Load-Balancing"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionAlgorithmType.length; ++_index2) {
				if (enumRestrictionAlgorithmType[_index2].equals(getAlgorithmType())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getAlgorithmType() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "algorithmType", this);	// NOI18N
			}
		}
		// Validating property dataSourceList
		// Validating property connectionPoolFailoverCallbackHandler
		// Validating property failoverRequestIfBusy
		{
			boolean patternPassed = false;
			if ((isFailoverRequestIfBusy() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isFailoverRequestIfBusy()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "failoverRequestIfBusy", this);	// NOI18N
		}
		// Validating property globalTransactionsProtocol
		if (getGlobalTransactionsProtocol() != null) {
			final java.lang.String[] enumRestrictionGlobalTransactionsProtocol = {"TwoPhaseCommit", "LoggingLastResource", "EmulateTwoPhaseCommit", "OnePhaseCommit", "None"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionGlobalTransactionsProtocol.length; 
				++_index2) {
				if (enumRestrictionGlobalTransactionsProtocol[_index2].equals(getGlobalTransactionsProtocol())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getGlobalTransactionsProtocol() enumeration test", org.netbeans.modules.schema2beans.ValidateException.FailureType.ENUM_RESTRICTION, "globalTransactionsProtocol", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("JndiName["+this.sizeJndiName()+"]");	// NOI18N
		for(int i=0; i<this.sizeJndiName(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getJndiName(i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(JNDI_NAME, i, str, indent);
		}

		str.append(indent);
		str.append("Scope");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getScope();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SCOPE, 0, str, indent);

		str.append(indent);
		str.append("RowPrefetch");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isRowPrefetch()?"true":"false"));
		this.dumpAttributes(ROW_PREFETCH, 0, str, indent);

		if (this.getValue(ROW_PREFETCH_SIZE) != null) {
			str.append(indent);
			str.append("RowPrefetchSize");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getRowPrefetchSize());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(ROW_PREFETCH_SIZE, 0, str, indent);
		}

		if (this.getValue(STREAM_CHUNK_SIZE) != null) {
			str.append(indent);
			str.append("StreamChunkSize");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getStreamChunkSize());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(STREAM_CHUNK_SIZE, 0, str, indent);
		}

		str.append(indent);
		str.append("AlgorithmType");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getAlgorithmType();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ALGORITHM_TYPE, 0, str, indent);

		str.append(indent);
		str.append("DataSourceList");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDataSourceList();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DATA_SOURCE_LIST, 0, str, indent);

		str.append(indent);
		str.append("ConnectionPoolFailoverCallbackHandler");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getConnectionPoolFailoverCallbackHandler();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CONNECTION_POOL_FAILOVER_CALLBACK_HANDLER, 0, str, indent);

		str.append(indent);
		str.append("FailoverRequestIfBusy");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isFailoverRequestIfBusy()?"true":"false"));
		this.dumpAttributes(FAILOVER_REQUEST_IF_BUSY, 0, str, indent);

		str.append(indent);
		str.append("GlobalTransactionsProtocol");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getGlobalTransactionsProtocol();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(GLOBAL_TRANSACTIONS_PROTOCOL, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("JdbcDataSourceParamsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

