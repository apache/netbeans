/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
	properties = (PropertyDescriptor[])al.toArray(new PropertyDescriptor[al.size()]);
	
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
