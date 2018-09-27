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
 *	This generated bean class StatefulSessionDescriptorType matches the schema element 'stateful-session-descriptorType'.
 *  The root bean class is WeblogicEjbJar
 *
 *	Generated on Tue Jul 25 03:26:57 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ejb90;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class StatefulSessionDescriptorType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String STATEFUL_SESSION_CACHE = "StatefulSessionCache";	// NOI18N
	static public final String PERSISTENT_STORE_DIR = "PersistentStoreDir";	// NOI18N
	static public final String PERSISTENTSTOREDIRID = "PersistentStoreDirId";	// NOI18N
	static public final String STATEFUL_SESSION_CLUSTERING = "StatefulSessionClustering";	// NOI18N
	static public final String ALLOW_CONCURRENT_CALLS = "AllowConcurrentCalls";	// NOI18N
	static public final String ALLOW_REMOVE_DURING_TRANSACTION = "AllowRemoveDuringTransaction";	// NOI18N

	public StatefulSessionDescriptorType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public StatefulSessionDescriptorType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(5);
		this.createProperty("stateful-session-cache", 	// NOI18N
			STATEFUL_SESSION_CACHE, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			StatefulSessionCacheType.class);
		this.createAttribute(STATEFUL_SESSION_CACHE, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("persistent-store-dir", 	// NOI18N
			PERSISTENT_STORE_DIR, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(PERSISTENT_STORE_DIR, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("stateful-session-clustering", 	// NOI18N
			STATEFUL_SESSION_CLUSTERING, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			StatefulSessionClusteringType.class);
		this.createAttribute(STATEFUL_SESSION_CLUSTERING, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("allow-concurrent-calls", 	// NOI18N
			ALLOW_CONCURRENT_CALLS, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("allow-remove-during-transaction", 	// NOI18N
			ALLOW_REMOVE_DURING_TRANSACTION, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
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
	public void setStatefulSessionCache(StatefulSessionCacheType value) {
		this.setValue(STATEFUL_SESSION_CACHE, value);
	}

	//
	public StatefulSessionCacheType getStatefulSessionCache() {
		return (StatefulSessionCacheType)this.getValue(STATEFUL_SESSION_CACHE);
	}

	// This attribute is optional
	public void setPersistentStoreDir(java.lang.String value) {
		this.setValue(PERSISTENT_STORE_DIR, value);
	}

	//
	public java.lang.String getPersistentStoreDir() {
		return (java.lang.String)this.getValue(PERSISTENT_STORE_DIR);
	}

	// This attribute is optional
	public void setPersistentStoreDirId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(PERSISTENT_STORE_DIR) == 0) {
			setValue(PERSISTENT_STORE_DIR, "");
		}
		setAttributeValue(PERSISTENT_STORE_DIR, "Id", value);
	}

	//
	public java.lang.String getPersistentStoreDirId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(PERSISTENT_STORE_DIR) == 0) {
			return null;
		} else {
			return getAttributeValue(PERSISTENT_STORE_DIR, "Id");
		}
	}

	// This attribute is optional
	public void setStatefulSessionClustering(StatefulSessionClusteringType value) {
		this.setValue(STATEFUL_SESSION_CLUSTERING, value);
	}

	//
	public StatefulSessionClusteringType getStatefulSessionClustering() {
		return (StatefulSessionClusteringType)this.getValue(STATEFUL_SESSION_CLUSTERING);
	}

	// This attribute is optional
	public void setAllowConcurrentCalls(boolean value) {
		this.setValue(ALLOW_CONCURRENT_CALLS, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isAllowConcurrentCalls() {
		Boolean ret = (Boolean)this.getValue(ALLOW_CONCURRENT_CALLS);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setAllowRemoveDuringTransaction(boolean value) {
		this.setValue(ALLOW_REMOVE_DURING_TRANSACTION, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isAllowRemoveDuringTransaction() {
		Boolean ret = (Boolean)this.getValue(ALLOW_REMOVE_DURING_TRANSACTION);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public StatefulSessionCacheType newStatefulSessionCacheType() {
		return new StatefulSessionCacheType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public StatefulSessionClusteringType newStatefulSessionClusteringType() {
		return new StatefulSessionClusteringType();
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
		// Validating property statefulSessionCache
		if (getStatefulSessionCache() != null) {
			getStatefulSessionCache().validate();
		}
		// Validating property persistentStoreDir
		// Validating property persistentStoreDirId
		if (getPersistentStoreDirId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getPersistentStoreDirId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "persistentStoreDirId", this);	// NOI18N
			}
		}
		// Validating property statefulSessionClustering
		if (getStatefulSessionClustering() != null) {
			getStatefulSessionClustering().validate();
		}
		// Validating property allowConcurrentCalls
		{
			boolean patternPassed = false;
			if ((isAllowConcurrentCalls() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isAllowConcurrentCalls()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "allowConcurrentCalls", this);	// NOI18N
		}
		// Validating property allowRemoveDuringTransaction
		{
			boolean patternPassed = false;
			if ((isAllowRemoveDuringTransaction() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isAllowRemoveDuringTransaction()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "allowRemoveDuringTransaction", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("StatefulSessionCache");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getStatefulSessionCache();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(STATEFUL_SESSION_CACHE, 0, str, indent);

		str.append(indent);
		str.append("PersistentStoreDir");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPersistentStoreDir();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PERSISTENT_STORE_DIR, 0, str, indent);

		str.append(indent);
		str.append("StatefulSessionClustering");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getStatefulSessionClustering();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(STATEFUL_SESSION_CLUSTERING, 0, str, indent);

		str.append(indent);
		str.append("AllowConcurrentCalls");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isAllowConcurrentCalls()?"true":"false"));
		this.dumpAttributes(ALLOW_CONCURRENT_CALLS, 0, str, indent);

		str.append(indent);
		str.append("AllowRemoveDuringTransaction");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isAllowRemoveDuringTransaction()?"true":"false"));
		this.dumpAttributes(ALLOW_REMOVE_DURING_TRANSACTION, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("StatefulSessionDescriptorType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

