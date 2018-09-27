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
 *	This generated bean class DriverParamsType matches the schema element 'driver-paramsType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:50 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear90;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class DriverParamsType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String STATEMENT = "Statement";	// NOI18N
	static public final String PREPARED_STATEMENT = "PreparedStatement";	// NOI18N
	static public final String ROW_PREFETCH_ENABLED = "RowPrefetchEnabled";	// NOI18N
	static public final String ROW_PREFETCH_SIZE = "RowPrefetchSize";	// NOI18N
	static public final String STREAM_CHUNK_SIZE = "StreamChunkSize";	// NOI18N

	public DriverParamsType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public DriverParamsType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(5);
		this.createProperty("statement", 	// NOI18N
			STATEMENT, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			StatementType.class);
		this.createProperty("prepared-statement", 	// NOI18N
			PREPARED_STATEMENT, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PreparedStatementType.class);
		this.createProperty("row-prefetch-enabled", 	// NOI18N
			ROW_PREFETCH_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("row-prefetch-size", 	// NOI18N
			ROW_PREFETCH_SIZE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("stream-chunk-size", 	// NOI18N
			STREAM_CHUNK_SIZE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setStatement(StatementType value) {
		this.setValue(STATEMENT, value);
	}

	//
	public StatementType getStatement() {
		return (StatementType)this.getValue(STATEMENT);
	}

	// This attribute is optional
	public void setPreparedStatement(PreparedStatementType value) {
		this.setValue(PREPARED_STATEMENT, value);
	}

	//
	public PreparedStatementType getPreparedStatement() {
		return (PreparedStatementType)this.getValue(PREPARED_STATEMENT);
	}

	// This attribute is optional
	public void setRowPrefetchEnabled(boolean value) {
		this.setValue(ROW_PREFETCH_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isRowPrefetchEnabled() {
		Boolean ret = (Boolean)this.getValue(ROW_PREFETCH_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setRowPrefetchSize(int value) {
		this.setValue(ROW_PREFETCH_SIZE, java.lang.Integer.valueOf(value));
	}

	//
	public int getRowPrefetchSize() {
		Integer ret = (Integer)this.getValue(ROW_PREFETCH_SIZE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"ROW_PREFETCH_SIZE", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setStreamChunkSize(int value) {
		this.setValue(STREAM_CHUNK_SIZE, java.lang.Integer.valueOf(value));
	}

	//
	public int getStreamChunkSize() {
		Integer ret = (Integer)this.getValue(STREAM_CHUNK_SIZE);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"STREAM_CHUNK_SIZE", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public StatementType newStatementType() {
		return new StatementType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public PreparedStatementType newPreparedStatementType() {
		return new PreparedStatementType();
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
		// Validating property statement
		if (getStatement() != null) {
			getStatement().validate();
		}
		// Validating property preparedStatement
		if (getPreparedStatement() != null) {
			getPreparedStatement().validate();
		}
		// Validating property rowPrefetchEnabled
		{
			boolean patternPassed = false;
			if ((isRowPrefetchEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isRowPrefetchEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "rowPrefetchEnabled", this);	// NOI18N
		}
		// Validating property rowPrefetchSize
		// Validating property streamChunkSize
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Statement");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getStatement();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(STATEMENT, 0, str, indent);

		str.append(indent);
		str.append("PreparedStatement");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getPreparedStatement();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(PREPARED_STATEMENT, 0, str, indent);

		str.append(indent);
		str.append("RowPrefetchEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isRowPrefetchEnabled()?"true":"false"));
		this.dumpAttributes(ROW_PREFETCH_ENABLED, 0, str, indent);

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

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("DriverParamsType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

