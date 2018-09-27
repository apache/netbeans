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
 *	This generated bean class EntityMappingType matches the schema element 'entity-mappingType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:47 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear1211;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class EntityMappingType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ENTITY_MAPPING_NAME = "EntityMappingName";	// NOI18N
	static public final String PUBLIC_ID = "PublicId";	// NOI18N
	static public final String SYSTEM_ID = "SystemId";	// NOI18N
	static public final String ENTITY_URI = "EntityUri";	// NOI18N
	static public final String WHEN_TO_CACHE = "WhenToCache";	// NOI18N
	static public final String CACHE_TIMEOUT_INTERVAL = "CacheTimeoutInterval";	// NOI18N

	public EntityMappingType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public EntityMappingType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(6);
		this.createProperty("entity-mapping-name", 	// NOI18N
			ENTITY_MAPPING_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("public-id", 	// NOI18N
			PUBLIC_ID, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("system-id", 	// NOI18N
			SYSTEM_ID, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("entity-uri", 	// NOI18N
			ENTITY_URI, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("when-to-cache", 	// NOI18N
			WHEN_TO_CACHE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("cache-timeout-interval", 	// NOI18N
			CACHE_TIMEOUT_INTERVAL, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setEntityMappingName(java.lang.String value) {
		this.setValue(ENTITY_MAPPING_NAME, value);
	}

	//
	public java.lang.String getEntityMappingName() {
		return (java.lang.String)this.getValue(ENTITY_MAPPING_NAME);
	}

	// This attribute is optional
	public void setPublicId(java.lang.String value) {
		this.setValue(PUBLIC_ID, value);
	}

	//
	public java.lang.String getPublicId() {
		return (java.lang.String)this.getValue(PUBLIC_ID);
	}

	// This attribute is optional
	public void setSystemId(java.lang.String value) {
		this.setValue(SYSTEM_ID, value);
	}

	//
	public java.lang.String getSystemId() {
		return (java.lang.String)this.getValue(SYSTEM_ID);
	}

	// This attribute is optional
	public void setEntityUri(java.lang.String value) {
		this.setValue(ENTITY_URI, value);
	}

	//
	public java.lang.String getEntityUri() {
		return (java.lang.String)this.getValue(ENTITY_URI);
	}

	// This attribute is optional
	public void setWhenToCache(java.lang.String value) {
		this.setValue(WHEN_TO_CACHE, value);
	}

	//
	public java.lang.String getWhenToCache() {
		return (java.lang.String)this.getValue(WHEN_TO_CACHE);
	}

	// This attribute is optional
	public void setCacheTimeoutInterval(int value) {
		this.setValue(CACHE_TIMEOUT_INTERVAL, java.lang.Integer.valueOf(value));
	}

	//
	public int getCacheTimeoutInterval() {
		Integer ret = (Integer)this.getValue(CACHE_TIMEOUT_INTERVAL);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"CACHE_TIMEOUT_INTERVAL", "int"}));
		return ((java.lang.Integer)ret).intValue();
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
		// Validating property entityMappingName
		if (getEntityMappingName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getEntityMappingName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "entityMappingName", this);	// NOI18N
		}
		// Validating property publicId
		// Validating property systemId
		// Validating property entityUri
		// Validating property whenToCache
		// Validating property cacheTimeoutInterval
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("EntityMappingName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getEntityMappingName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ENTITY_MAPPING_NAME, 0, str, indent);

		str.append(indent);
		str.append("PublicId");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPublicId();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PUBLIC_ID, 0, str, indent);

		str.append(indent);
		str.append("SystemId");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getSystemId();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SYSTEM_ID, 0, str, indent);

		str.append(indent);
		str.append("EntityUri");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getEntityUri();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ENTITY_URI, 0, str, indent);

		str.append(indent);
		str.append("WhenToCache");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getWhenToCache();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(WHEN_TO_CACHE, 0, str, indent);

		if (this.getValue(CACHE_TIMEOUT_INTERVAL) != null) {
			str.append(indent);
			str.append("CacheTimeoutInterval");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getCacheTimeoutInterval());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(CACHE_TIMEOUT_INTERVAL, 0, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("EntityMappingType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

