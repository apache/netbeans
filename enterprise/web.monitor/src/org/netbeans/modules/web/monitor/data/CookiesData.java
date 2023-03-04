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

import java.beans.PropertyChangeListener;
import java.util.Vector;
import org.netbeans.modules.schema2beans.AttrProp;

import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.BeanComparator;
import org.netbeans.modules.schema2beans.BeanProp;
import org.netbeans.modules.schema2beans.Common;
import org.netbeans.modules.schema2beans.Version;

public class CookiesData extends BaseBean {

    static Vector<BeanComparator> comparators = new Vector<>();

    public static final String COOKIEIN = "CookieIn"; // NOI18N
    public static final String COOKIEOUT = "CookieOut"; // NOI18N
    //public final static String JSESSIONID = "JSESSIONID"; // NOI18N

    public CookiesData() {
	this(Common.USE_DEFAULT_VALUES);
    }

    public CookiesData(int options) {
	super(RequestData.comparators, new Version(1, 0, 6));
	// Properties (see root bean comments for the bean graph)
	this.createProperty("CookieIn", COOKIEIN, // NOI18N
			    Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    CookieIn.class);
	this.createAttribute(COOKIEIN, "name", "Name",  // NOI18N
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute(COOKIEIN, "value", "Value", // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createProperty("CookieOut", COOKIEOUT, // NOI18N
			    Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    CookieOut.class);
	this.createAttribute(COOKIEOUT, "name", "Name", // NOI18N
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute(COOKIEOUT, "value", "Value", // NOI18N
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute(COOKIEOUT, "path", "Path", // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(COOKIEOUT, "domain", "Domain", // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(COOKIEOUT, "maxAge", "MaxAge", // NOI18N
			     AttrProp.NMTOKEN | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(COOKIEOUT, "comment", "Comment", // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(COOKIEOUT, "version", "Version", // NOI18N
			     AttrProp.NMTOKEN | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(COOKIEOUT, "secure", "Secure", // NOI18N
			     AttrProp.ENUM,
			     new String[] { "false", "true" }, // NOI18N
			     "false"); // NOI18N
	this.initialize(options);
    }

    // Setting the default values of the properties
    void initialize(int options) {

    }

    // This attribute is an array, possibly empty
    public void setCookieIn(int index, CookieIn value) {
	this.setValue(COOKIEIN, index, value);
    }

    //
    public CookieIn getCookieIn(int index) {
	return (CookieIn)this.getValue(COOKIEIN, index);
    }

    // This attribute is an array, possibly empty
    public void setCookieIn(CookieIn[] value) {
	this.setValue(COOKIEIN, value);
    }

    //
    public CookieIn[] getCookieIn() {
	return (CookieIn[])this.getValues(COOKIEIN);
    }

    // Return the number of properties
    public int sizeCookieIn() {
	return this.size(COOKIEIN);
    }

    // Add a new element returning its index in the list
    public int addCookieIn(CookieIn value) {
	return this.addValue(COOKIEIN, value);
    }

    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeCookieIn(CookieIn value) {
	return this.removeValue(COOKIEIN, value);
    }

    // This attribute is an array, possibly empty
    public void setCookieOut(int index, CookieOut value) {
	this.setValue(COOKIEOUT, index, value);
    }

    //
    public CookieOut getCookieOut(int index) {
	return (CookieOut)this.getValue(COOKIEOUT, index);
    }

    // This attribute is an array, possibly empty
    public void setCookieOut(CookieOut[] value) {
	this.setValue(COOKIEOUT, value);
    }

    //
    public CookieOut[] getCookieOut() {
	return (CookieOut[])this.getValues(COOKIEOUT);
    }

    // Return the number of properties
    public int sizeCookieOut() {
	return this.size(COOKIEOUT);
    }

    // Add a new element returning its index in the list
    public int addCookieOut(CookieOut value) {
	return this.addValue(COOKIEOUT, value);
    }

    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeCookieOut(CookieOut value) {
	return this.removeValue(COOKIEOUT, value);
    }

    // This method verifies that the mandatory properties are set
    public boolean verify() {
	return true;
    }

    //
    public static void addComparator(BeanComparator c) {
	ClientData.comparators.add(c);
    }

    //
    public static void removeComparator(BeanComparator c) {
	ClientData.comparators.remove(c);
    }
    //
    public void addPropertyChangeListener(PropertyChangeListener l) {
	BeanProp p = this.beanProp();
	if (p != null)
	    p.addPCListener(l);
    }

    //
    public void removePropertyChangeListener(PropertyChangeListener l) {
	BeanProp p = this.beanProp();
	if (p != null)
	    p.removePCListener(l);
    }

    //
    public void addPropertyChangeListener(String n, PropertyChangeListener l) {
	BeanProp p = this.beanProp(n);
	if (p != null)
	    p.addPCListener(l);
    }

    //
    public void removePropertyChangeListener(String n,
					     PropertyChangeListener l) {
	BeanProp p = this.beanProp(n);
	if (p != null)
	    p.removePCListener(l);
    }

    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent) {
	String s;
	BaseBean n;

	str.append(indent);
	str.append("CookieIn["+this.sizeCookieIn()+"]"); // NOI18N
	for(int i=0; i<this.sizeCookieIn(); i++) 
	    {
		str.append(indent+"\t"); // NOI18N
		str.append("#"+i+":"); // NOI18N
		n = this.getCookieIn(i);
		if (n != null)
		    n.dump(str, indent + "\t"); // NOI18N
		else
		    str.append(indent+"\tnull"); // NOI18N
		this.dumpAttributes(COOKIEIN, i, str, indent);
	    }

	str.append(indent);
	str.append("CookieOut["+this.sizeCookieOut()+"]"); // NOI18N
	for(int i=0; i<this.sizeCookieOut(); i++) 
	    {
		str.append(indent+"\t"); // NOI18N
		str.append("#"+i+":"); // NOI18N
		n = this.getCookieOut(i);
		if (n != null)
		    n.dump(str, indent + "\t"); // NOI18N
		else
		    str.append(indent+"\tnull"); // NOI18N
		this.dumpAttributes(COOKIEOUT, i, str, indent);
	    }

    }

    public String dumpBeanNode() {
	StringBuffer str = new StringBuffer();
	str.append("CookiesData\n"); // NOI18N
	this.dump(str, "\n  "); // NOI18N
	return str.toString();
    }
}

