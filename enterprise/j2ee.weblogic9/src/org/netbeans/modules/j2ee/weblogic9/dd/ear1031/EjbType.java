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
 *	This generated bean class EjbType matches the schema element 'ejbType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:45 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear1031;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class EjbType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ENTITY_CACHE = "EntityCache";	// NOI18N
	static public final String START_MDBS_WITH_APPLICATION = "StartMdbsWithApplication";	// NOI18N

	public EjbType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public EjbType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("entity-cache", 	// NOI18N
			ENTITY_CACHE, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ApplicationEntityCacheType.class);
		this.createProperty("start-mdbs-with-application", 	// NOI18N
			START_MDBS_WITH_APPLICATION, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is an array, possibly empty
	public void setEntityCache(int index, ApplicationEntityCacheType value) {
		this.setValue(ENTITY_CACHE, index, value);
	}

	//
	public ApplicationEntityCacheType getEntityCache(int index) {
		return (ApplicationEntityCacheType)this.getValue(ENTITY_CACHE, index);
	}

	// Return the number of properties
	public int sizeEntityCache() {
		return this.size(ENTITY_CACHE);
	}

	// This attribute is an array, possibly empty
	public void setEntityCache(ApplicationEntityCacheType[] value) {
		this.setValue(ENTITY_CACHE, value);
	}

	//
	public ApplicationEntityCacheType[] getEntityCache() {
		return (ApplicationEntityCacheType[])this.getValues(ENTITY_CACHE);
	}

	// Add a new element returning its index in the list
	public int addEntityCache(org.netbeans.modules.j2ee.weblogic9.dd.ear1031.ApplicationEntityCacheType value) {
		int positionOfNewItem = this.addValue(ENTITY_CACHE, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeEntityCache(org.netbeans.modules.j2ee.weblogic9.dd.ear1031.ApplicationEntityCacheType value) {
		return this.removeValue(ENTITY_CACHE, value);
	}

	// This attribute is optional
	public void setStartMdbsWithApplication(boolean value) {
		this.setValue(START_MDBS_WITH_APPLICATION, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isStartMdbsWithApplication() {
		Boolean ret = (Boolean)this.getValue(START_MDBS_WITH_APPLICATION);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ApplicationEntityCacheType newApplicationEntityCacheType() {
		return new ApplicationEntityCacheType();
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
		// Validating property entityCache
		for (int _index = 0; _index < sizeEntityCache(); ++_index) {
			org.netbeans.modules.j2ee.weblogic9.dd.ear1031.ApplicationEntityCacheType element = getEntityCache(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property startMdbsWithApplication
		{
			boolean patternPassed = false;
			if ((isStartMdbsWithApplication() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isStartMdbsWithApplication()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "startMdbsWithApplication", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("EntityCache["+this.sizeEntityCache()+"]");	// NOI18N
		for(int i=0; i<this.sizeEntityCache(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getEntityCache(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(ENTITY_CACHE, i, str, indent);
		}

		str.append(indent);
		str.append("StartMdbsWithApplication");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isStartMdbsWithApplication()?"true":"false"));
		this.dumpAttributes(START_MDBS_WITH_APPLICATION, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("EjbType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

