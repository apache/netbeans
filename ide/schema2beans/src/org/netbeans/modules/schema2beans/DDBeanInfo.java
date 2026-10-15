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

package org.netbeans.modules.schema2beans;

import java.beans.*;
import java.util.*;
import java.lang.reflect.*;

public class DDBeanInfo extends SimpleBeanInfo {

    private static final String BEANINFO = "BeanInfo";	// NOI18N

    private PropertyDescriptor[] properties = null;

    private boolean propertiesInited = false;
    
    private void initProperties() {
	ArrayList 	al = new ArrayList();
	String    	classname = null;
	BeanInfo	bi;

	try {
	    classname = this.getClass().getName();
	    if (!classname.endsWith(BEANINFO)) {
		return;
	    }
	    classname = classname.substring(0,(classname.length() - 
					       BEANINFO.length()));
	    bi = Introspector.getBeanInfo(Class.forName(classname));
	} catch (ClassNotFoundException e) {
	    System.err.println("Class name = " + classname);	// NOI18N
	    return;
	} catch (IntrospectionException e) {
	    Thread.dumpStack();
	    return;
	}

	PropertyDescriptor[] pd = bi.getPropertyDescriptors();
	Method m = null;
	for (int i=0;i<pd.length; i++) {
	    Class c = null;
	    if (pd[i] instanceof IndexedPropertyDescriptor) {
		IndexedPropertyDescriptor ipd = 
		    (IndexedPropertyDescriptor)pd[i];
		c = ipd.getIndexedPropertyType();
	    } else {
		c = pd[i].getPropertyType();
	    }
	    // Check for the following:
	    // 1: Does the metohd have a return type that implements 
	    //	the DDNode interface?
	    // 2: Is it a class in java.lang? but not getClass 
	    //	which is inherited from Object
	    // 3: Is it a primitive java type? This would have no "." 
	    //	chars in it.
	    if (c != null) {
		if (BaseBean.class.isAssignableFrom(c) ||
		    (c.getName().startsWith("java.lang.") 
		     && !c.getName().equals("java.lang.Class")) // NOI18N
		    || (c.getName().indexOf(".") < 0)) {	// NOI18N

		    al.add(pd[i]);
		}
	    }
	}
	properties = (PropertyDescriptor[])al.toArray(new PropertyDescriptor[0]);
	
    }
    
    
    /**
     * Gets the beans <code>PropertyDescriptor</code>s.
     *
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by this bean.  May return null if the
     * information should be obtained by automatic analysis.
     * <p>
     * If a property is indexed, then its entry in the result array will
     * belong to the IndexedPropertyDescriptor subclass of PropertyDescriptor.
     * A client of getPropertyDescriptors can use "instanceof" to check
     * if a given PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
	if (!propertiesInited) {
	    // Avoid recursion here
	    propertiesInited = true;
	    initProperties();
	}
	return properties;
    }
    
}
