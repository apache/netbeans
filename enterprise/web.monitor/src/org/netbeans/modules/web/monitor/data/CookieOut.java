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

package org.netbeans.modules.web.monitor.data;

import java.beans.PropertyChangeListener;
import java.util.Vector;

import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.BeanComparator;
import org.netbeans.modules.schema2beans.BeanProp;
import org.netbeans.modules.schema2beans.Common;

public class CookieOut extends BaseBean
{

    static Vector<BeanComparator> comparators = new Vector<>();

    public CookieOut()
    {
	this(Common.USE_DEFAULT_VALUES);
    }

    public CookieOut(String name, String value, int maxAge, int version, String domain, String path, String comment,boolean secure) {
	super(CookieOut.comparators, new
	org.netbeans.modules.schema2beans.Version(1, 0, 5));

	// Note - the XML beans library does not treat NMTOKENS as
	// special - they have to be set as strings! 
	setAttributeValue("name", name); //NOI18N
	setAttributeValue("value", value); //NOI18N
	setAttributeValue("maxAge", String.valueOf(maxAge)); //NOI18N
	setAttributeValue("version", String.valueOf(version)); //NOI18N
	if(domain != null) {
	    if(domain.trim().equals("")) { //NOI18N
		setAttributeValue("domain", ""); //NOI18N
            } else {
		setAttributeValue("domain", domain);  //NOI18N
            }
	}
	if(path != null) {
	    if(path.trim().equals("")) { //NOI18N
		setAttributeValue("path", ""); //NOI18N
            } else {
		setAttributeValue("path", path); //NOI18N
            }
	}
	if(comment != null) {
	    if(comment.trim().equals("")) //NOI18N
		setAttributeValue("comment", ""); //NOI18N
	    else 
		setAttributeValue("comment", comment);  //NOI18N
	}
        if(secure) {
            setAttributeValue("secure", String.valueOf(secure)); //NOI18N
        }
    }


    public CookieOut(int options)
    {
	super(CookieOut.comparators, new org.netbeans.modules.schema2beans.Version(1, 0, 5));
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

    //
    public static void addComparator(BeanComparator c)
    {
	CookieOut.comparators.add(c);
    }

    //
    public static void removeComparator(BeanComparator c)
    {
	CookieOut.comparators.remove(c);
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
	str.append("CookieOut\n"); //NOI18N
	this.dump(str, "\n  "); //NOI18N
	return str.toString();
    }
}

