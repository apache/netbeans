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
import javax.servlet.http.Cookie;

import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.BeanComparator;
import org.netbeans.modules.schema2beans.BeanProp;
import org.netbeans.modules.schema2beans.Common;
import org.netbeans.modules.schema2beans.Version;

public class CookieIn extends BaseBean {

    static Vector<BeanComparator> comparators = new Vector<>();

    public CookieIn() {
	this(Common.USE_DEFAULT_VALUES);
    }

    public CookieIn(Cookie cookie) {
	super(comparators, new Version(1, 0, 5));
	this.setAttributeValue("name", cookie.getName());//NOI18N
	this.setAttributeValue("value", cookie.getValue());//NOI18N
    }

    public CookieIn(String name, String value) {
	super(comparators, new Version(1, 0, 5));
	this.setAttributeValue("name", name);//NOI18N
	this.setAttributeValue("value", value);//NOI18N
    }

    public CookieIn(int options) {
	super(comparators, new Version(1, 0, 6));
	// Properties (see root bean comments for the bean graph)
	this.initialize(options);
    }

    // Setting the default values of the properties
    void initialize(int options)
    {

    }

    // This method verifies that the mandatory properties are set
    public boolean verify()
    {
	return true;
    }

        
    /**
       * Get the value of name.
       * @return Value of name.
       */
    public String getName() {return this.getAttributeValue("name");} //NOI18N
    
    /**
       * Set the value of Name.
       * @param v  Value to assign to name.
       */
    public void setName(String  v) {this.setAttributeValue("name", v);} //NOI18N
    

    /**
       * Get the value of value.
       * @return Value of value.
       */
    public String getValue() {return this.getAttributeValue("value");} //NOI18N
    
    /**
       * Set the value of value.
       * @param v  Value to assign to value.
       */
    public void setValue(String  v)  {this.setAttributeValue("value", v);} //NOI18N
    
    //
    public static void addComparator(BeanComparator c)
    {
	CookieIn.comparators.add(c);
    }

    //
    public static void removeComparator(BeanComparator c)
    {
	CookieIn.comparators.remove(c);
    }
    //
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
	BeanProp p = this.beanProp();
	if (p != null)
	    p.addPCListener(l);
    }

    //
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
	BeanProp p = this.beanProp();
	if (p != null)
	    p.removePCListener(l);
    }

    //
    public void addPropertyChangeListener(String n, PropertyChangeListener l)
    {
	BeanProp p = this.beanProp(n);
	if (p != null)
	    p.addPCListener(l);
    }

    //
    public void removePropertyChangeListener(String n, PropertyChangeListener l)
    {
	BeanProp p = this.beanProp(n);
	if (p != null)
	    p.removePCListener(l);
    }

    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent)
    {
	String s;
	BaseBean n;
    }

    public String dumpBeanNode()
    {
	StringBuffer str = new StringBuffer();
	str.append("CookieIn\n");//NOI18N
	this.dump(str, "\n  ");//NOI18N
	return str.toString();
    }
}

