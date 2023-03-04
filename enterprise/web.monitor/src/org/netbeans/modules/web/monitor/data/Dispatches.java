/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.web.monitor.data;

import java.util.Vector;
import org.netbeans.modules.schema2beans.AttrProp;

import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.BeanComparator;
import org.netbeans.modules.schema2beans.Common;

public class Dispatches extends BaseBean {

    static Vector<BeanComparator> comparators = new Vector<>();

    public static final String DISPATCHDATA = "DispatchData"; // NOI18N

    public Dispatches() {
	this(Common.USE_DEFAULT_VALUES);
    }

    public Dispatches(int options) {
	super(comparators, new org.netbeans.modules.schema2beans.Version(1, 0, 8));
	// Properties (see root bean comments for the bean graph)
	this.createProperty("DispatchData", 	// NOI18N
			    DISPATCHDATA, 
			    Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    DispatchData.class);
	this.createAttribute(DISPATCHDATA, "resource", "Resource", //NOI18N
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.initialize(options);
    }

    // Setting the default values of the properties
    void initialize(int options) { }

    // This attribute is an array, possibly empty
    public void setDispatchData(int index, DispatchData value) {
	this.setValue(DISPATCHDATA, index, value);
    }

    //
    public DispatchData getDispatchData(int index) {
	return (DispatchData)this.getValue(DISPATCHDATA, index);
    }

    // This attribute is an array, possibly empty
    public void setDispatchData(DispatchData[] value) {
	this.setValue(DISPATCHDATA, value);
    }

    //
    public DispatchData[] getDispatchData() {
	return (DispatchData[])this.getValues(DISPATCHDATA);
    }

    // Return the number of properties
    public int sizeDispatchData() {
	return this.size(DISPATCHDATA);
    }

    // Add a new element returning its index in the list
    public int addDispatchData(DispatchData value) {
	return this.addValue(DISPATCHDATA, value);
    }

    // Remove an element using its reference
    // Returns the index the element had in the list
    public int removeDispatchData(DispatchData value) {
	return this.removeValue(DISPATCHDATA, value);
    }

    // This method verifies that the mandatory properties are set
    public boolean verify() {
	return true;
    }

    //
    public static void addComparator(BeanComparator c) {
	comparators.add(c);
    }

    //
    public static void removeComparator(BeanComparator c) {
	comparators.remove(c);
    }
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent) {
	String s;
	Object o;
	BaseBean n;
	str.append(indent);
	str.append("DispatchData["+this.sizeDispatchData()+"]");	// NOI18N
	for(int i=0; i<this.sizeDispatchData(); i++) {
	    str.append(indent+"\t"); // NOI18N
	    str.append("#"+i+":"); // NOI18N
	    n = this.getDispatchData(i);
	    if (n != null)
		n.dump(str, indent + "\t");	// NOI18N
	    else
		str.append(indent+"\tnull");	// NOI18N
	    this.dumpAttributes(DISPATCHDATA, i, str, indent);
	}

    }
    public String dumpBeanNode() {
	StringBuffer str = new StringBuffer();
	str.append("Dispatches\n");	// NOI18N
	this.dump(str, "\n  ");	// NOI18N
	return str.toString();
    }
}



