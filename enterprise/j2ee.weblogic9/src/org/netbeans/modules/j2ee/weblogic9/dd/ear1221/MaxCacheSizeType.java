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
 *	This generated bean class MaxCacheSizeType matches the schema element 'max-cache-sizeType'.
 *  The root bean class is WeblogicApplication
 *
 *	Generated on Tue Jul 25 03:26:49 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.ear1221;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class MaxCacheSizeType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String BYTES = "Bytes";	// NOI18N
	static public final String MEGABYTES = "Megabytes";	// NOI18N

	public MaxCacheSizeType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public MaxCacheSizeType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("bytes", 	// NOI18N
			BYTES, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("megabytes", 	// NOI18N
			MEGABYTES, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setBytes(int value) {
		this.setValue(BYTES, java.lang.Integer.valueOf(value));
		if (value != 0) {
			// It's a mutually exclusive property.
			setMegabytes(0);
		}
	}

	//
	public int getBytes() {
		Integer ret = (Integer)this.getValue(BYTES);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"BYTES", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is mandatory
	public void setMegabytes(int value) {
		this.setValue(MEGABYTES, java.lang.Integer.valueOf(value));
		if (value != 0) {
			// It's a mutually exclusive property.
			setBytes(0);
		}
	}

	//
	public int getMegabytes() {
		Integer ret = (Integer)this.getValue(MEGABYTES);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"MEGABYTES", "int"}));
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
		// Validating property bytes
		if (getValue(BYTES) != null) {
			if (getValue(MEGABYTES) != null) {
				throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: Bytes and Megabytes", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "Megabytes", this);	// NOI18N
			}
		}
		// Validating property megabytes
		if (getValue(MEGABYTES) != null) {
			if (getValue(BYTES) != null) {
				throw new org.netbeans.modules.schema2beans.ValidateException("mutually exclusive properties: Megabytes and Bytes", org.netbeans.modules.schema2beans.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "Bytes", this);	// NOI18N
			}
		}
		if (getValue(BYTES) == null && getValue(MEGABYTES) == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("required properties: getValue(BYTES) == null && getValue(MEGABYTES) == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "Megabytes", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		if (this.getValue(BYTES) != null) {
			str.append(indent);
			str.append("Bytes");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getBytes());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(BYTES, 0, str, indent);
		}

		if (this.getValue(MEGABYTES) != null) {
			str.append(indent);
			str.append("Megabytes");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getMegabytes());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(MEGABYTES, 0, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("MaxCacheSizeType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

