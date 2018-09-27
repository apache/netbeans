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
 *	This generated bean class EntityDescriptorType matches the schema element 'entity-descriptorType'.
 *  The root bean class is WeblogicEjbJar
 *
 *	Generated on Tue Jul 25 03:26:56 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ejb1221;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class EntityDescriptorType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String POOL = "Pool";	// NOI18N
	static public final String TIMER_DESCRIPTOR = "TimerDescriptor";	// NOI18N
	static public final String ENTITY_CACHE = "EntityCache";	// NOI18N
	static public final String ENTITY_CACHE_REF = "EntityCacheRef";	// NOI18N
	static public final String PERSISTENCE = "Persistence";	// NOI18N
	static public final String ENTITY_CLUSTERING = "EntityClustering";	// NOI18N
	static public final String INVALIDATION_TARGET = "InvalidationTarget";	// NOI18N
	static public final String ENABLE_DYNAMIC_QUERIES = "EnableDynamicQueries";	// NOI18N

	public EntityDescriptorType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public EntityDescriptorType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(8);
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
		this.createProperty("entity-cache", 	// NOI18N
			ENTITY_CACHE, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			EntityCacheType.class);
		this.createAttribute(ENTITY_CACHE, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("entity-cache-ref", 	// NOI18N
			ENTITY_CACHE_REF, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			EntityCacheRefType.class);
		this.createAttribute(ENTITY_CACHE_REF, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("persistence", 	// NOI18N
			PERSISTENCE, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PersistenceType.class);
		this.createAttribute(PERSISTENCE, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("entity-clustering", 	// NOI18N
			ENTITY_CLUSTERING, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			EntityClusteringType.class);
		this.createAttribute(ENTITY_CLUSTERING, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("invalidation-target", 	// NOI18N
			INVALIDATION_TARGET, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			InvalidationTargetType.class);
		this.createAttribute(INVALIDATION_TARGET, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("enable-dynamic-queries", 	// NOI18N
			ENABLE_DYNAMIC_QUERIES, 
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

	// This attribute is mandatory
	public void setEntityCache(EntityCacheType value) {
		this.setValue(ENTITY_CACHE, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setEntityCacheRef(null);
		}
	}

	//
	public EntityCacheType getEntityCache() {
		return (EntityCacheType)this.getValue(ENTITY_CACHE);
	}

	// This attribute is mandatory
	public void setEntityCacheRef(EntityCacheRefType value) {
		this.setValue(ENTITY_CACHE_REF, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setEntityCache(null);
		}
	}

	//
	public EntityCacheRefType getEntityCacheRef() {
		return (EntityCacheRefType)this.getValue(ENTITY_CACHE_REF);
	}

	// This attribute is optional
	public void setPersistence(PersistenceType value) {
		this.setValue(PERSISTENCE, value);
	}

	//
	public PersistenceType getPersistence() {
		return (PersistenceType)this.getValue(PERSISTENCE);
	}

	// This attribute is optional
	public void setEntityClustering(EntityClusteringType value) {
		this.setValue(ENTITY_CLUSTERING, value);
	}

	//
	public EntityClusteringType getEntityClustering() {
		return (EntityClusteringType)this.getValue(ENTITY_CLUSTERING);
	}

	// This attribute is optional
	public void setInvalidationTarget(InvalidationTargetType value) {
		this.setValue(INVALIDATION_TARGET, value);
	}

	//
	public InvalidationTargetType getInvalidationTarget() {
		return (InvalidationTargetType)this.getValue(INVALIDATION_TARGET);
	}

	// This attribute is optional
	public void setEnableDynamicQueries(boolean value) {
		this.setValue(ENABLE_DYNAMIC_QUERIES, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isEnableDynamicQueries() {
		Boolean ret = (Boolean)this.getValue(ENABLE_DYNAMIC_QUERIES);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
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
	public EntityCacheType newEntityCacheType() {
		return new EntityCacheType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public EntityCacheRefType newEntityCacheRefType() {
		return new EntityCacheRefType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public PersistenceType newPersistenceType() {
		return new PersistenceType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public EntityClusteringType newEntityClusteringType() {
		return new EntityClusteringType();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public InvalidationTargetType newInvalidationTargetType() {
		return new InvalidationTargetType();
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
		// Validating property entityCache
		if (getEntityCache() != null) {
			getEntityCache().validate();
		}
		// Validating property entityCacheRef
		if (getEntityCacheRef() != null) {
			getEntityCacheRef().validate();
		}
		// Validating property persistence
		if (getPersistence() != null) {
			getPersistence().validate();
		}
		// Validating property entityClustering
		if (getEntityClustering() != null) {
			getEntityClustering().validate();
		}
		// Validating property invalidationTarget
		if (getInvalidationTarget() != null) {
			getInvalidationTarget().validate();
		}
		// Validating property enableDynamicQueries
		{
			boolean patternPassed = false;
			if ((isEnableDynamicQueries() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isEnableDynamicQueries()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "enableDynamicQueries", this);	// NOI18N
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
		str.append("EntityCache");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getEntityCache();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(ENTITY_CACHE, 0, str, indent);

		str.append(indent);
		str.append("EntityCacheRef");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getEntityCacheRef();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(ENTITY_CACHE_REF, 0, str, indent);

		str.append(indent);
		str.append("Persistence");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getPersistence();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(PERSISTENCE, 0, str, indent);

		str.append(indent);
		str.append("EntityClustering");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getEntityClustering();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(ENTITY_CLUSTERING, 0, str, indent);

		str.append(indent);
		str.append("InvalidationTarget");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getInvalidationTarget();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(INVALIDATION_TARGET, 0, str, indent);

		str.append(indent);
		str.append("EnableDynamicQueries");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isEnableDynamicQueries()?"true":"false"));
		this.dumpAttributes(ENABLE_DYNAMIC_QUERIES, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("EntityDescriptorType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

