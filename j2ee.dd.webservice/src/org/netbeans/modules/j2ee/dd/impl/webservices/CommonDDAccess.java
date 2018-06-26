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

/**
 * Methods for accessing schema2beans objects in a bean-independent way.
 *
 * @author  Milan Kuchtiak
 */
package org.netbeans.modules.j2ee.dd.impl.webservices;

import java.lang.reflect.*;
import org.openide.util.NbBundle;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;

/**
 * Methods for accessing schema2beans objects in bean-independent and version-independent way.
 *
 * @author Milan Kuchtiak
 */

public class CommonDDAccess {

    public static final String WEBSERVICES_1_1 = "1_1"; //NOI18N
    
    public static final String PACKAGE_PREFIX = "org.netbeans.modules.j2ee.dd.impl.webservices.model_"; //NOI18N
    public static final String DOT = "."; //NOI18N

    /**
     * Return a new instance of the specified type
     *
     * @param parent 	parent bean
     * @param beanName 	which bean to create
     * @param version	"2_3" or "2_4"
     * @return BaseBean object e.g. Servlet
     */

    public static BaseBean newBean(CommonDDBean parent, String beanName, String version)  throws ClassNotFoundException {
	beanName = getImplementationBeanName(parent, beanName, version);
	try {
	    Class beanClass = Class.forName(
				PACKAGE_PREFIX
				+ version + DOT
				+ beanName);
	    return (BaseBean) beanClass.newInstance();

	} catch (Exception e) {
            if (e instanceof ClassNotFoundException) 
                throw (ClassNotFoundException)e;
            else {
                // This is a programming error.
                e.printStackTrace();
                throw new RuntimeException(
                    NbBundle.getMessage(CommonDDAccess.class,
                        "MSG_COMMONDDACCESS_ERROR", "newBean",	
                        ", version = " + version + ", beanName = " + beanName, e+ ": " +e.getMessage()));
            }
	}
    }
    
    public static void addBean(CommonDDBean parent, CommonDDBean child, String beanName, String version) {
	beanName = getImplementationBeanName(parent, beanName, version);
	try {
            Class p = parent.getClass();
            Class ch = Class.forName("org.netbeans.modules.j2ee.dd.api.webservices."+beanName); //NOI18N
            Method setter=null;
            try {
                setter = p.getMethod("set" + beanName, new Class[]{ch}); //NOI18N
                setter.invoke(parent, new Object[]{child});
            } catch (NoSuchMethodException ex) {
            }
            if (setter==null) {
                setter = p.getMethod("add" + getNameForMethod(parent, beanName), new Class[]{ch}); //NOI18N
                setter.invoke(parent, new Object[]{child});
            }
	} catch (Exception e) {
            // This is a programming error.
            e.printStackTrace();
            throw new RuntimeException(
                NbBundle.getMessage(CommonDDAccess.class,
                    "MSG_COMMONDDACCESS_ERROR", "addBean",	
                    ", version = " + version + ", beanName = " + beanName, e+ ": " +e.getMessage()));
	}
    }

    /**
     * Get a BaseBean object from parent BaseBean
     *
     * @param parent            parent BaseBean
     * @param beanProperty 	name of child's BaseBean object e.g. "Servlet"
     * @param nameProperty      name of property e.g. ServletName
     * @param value             e.g. "ControllerServlet"
     */
    public static BaseBean findBeanByName(BaseBean parent, String beanProperty, String nameProperty, String value) {
	Class c = parent.getClass();
	Method getter;
	Object result;
	try {
	    getter = c.getMethod("get" + getNameForMethod((CommonDDBean)parent,beanProperty), null); //NOI18N
	    result = getter.invoke(parent, null);
	    if (result == null) {
		return null;
	    } else if (result instanceof BaseBean) {
		return null;
	    } else {
		BaseBean[] beans = (BaseBean[]) result;
                for (int i=0;i<beans.length;i++) {
                    Class c1 = beans[i].getClass();
                    Method getter1;
                    Object result1;
                    getter1 = c1.getMethod("get" + nameProperty, null); //NOI18N
                    result1 = getter1.invoke(beans[i], null);
                    if (result1 instanceof String) {
                        if (value.equals((String)result1)) {
                            return beans[i];
                        }
                    }
                }
                return null;
	    }
	} catch (Exception e) {
	    // This is a programming error
	    e.printStackTrace();
	    throw new RuntimeException(
		NbBundle.getMessage(CommonDDAccess.class,
		    "MSG_COMMONDDACCESS_ERROR", "getBeanByName",	
		    "parent = " + parent + ", beanProperty = " + beanProperty
                    + ", nameProperty = " + nameProperty
                    + ", value = " + value, 
		    e+ ": " +e.getMessage()));	
	}
    }
    
    /**
     * Handle special cases of version differences
     */
    private static String getImplementationBeanName (CommonDDBean parent, String beanName, String version) {
        if ("Webservices".equals(beanName)) {
            return beanName;
        } else {
            return beanName + "Type"; //NOI18N
        }
    }
    
    /**
     * Handle special cases of version differences
     */
    private static String getNameForMethod (CommonDDBean parent, String beanName) {
        if ("Webservices".equals(beanName) || 
            "WebserviceDescription".equals(beanName) ||
            "PortComponent".equals(beanName)) {
            return beanName;
        } else {
            return beanName + "Type"; //NOI18N
        }
    }
}
