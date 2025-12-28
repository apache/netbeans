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
 * Methods for accessing schema2beans objects in a bean-independent way.
 *
 * @author  Milan Kuchtiak
 */
package org.netbeans.modules.j2ee.dd.impl.common;

import java.lang.reflect.*;
import java.util.Set;
import java.util.HashSet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.openide.util.NbBundle;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;

/**
 * Methods for accessing schema2beans objects in bean-independent and version-independent way.
 *
 * @author Milan Kuchtiak
 */

public class CommonDDAccess {

    public static final String DOT = "."; //NOI18N
    
    public static final String COMMON_API = "org.netbeans.modules.j2ee.dd.api.common."; //NOI18N
    
    public static final String SERVLET_2_4 = "2_4"; //NOI18N
    public static final String WEB_API = "org.netbeans.modules.j2ee.dd.api.web."; //NOI18N
    public static final String WEB_PACKAGE_PREFIX = "org.netbeans.modules.j2ee.dd.impl.web.model_"; //NOI18N

    public static final String APP_1_4 = "1_4"; //NOI18N
    public static final String APP_API = "org.netbeans.modules.j2ee.dd.api.application."; //NOI18N
    public static final String APP_PACKAGE_PREFIX = "org.netbeans.modules.j2ee.dd.impl.application.model_"; //NOI18N
    
    public static final String EJB_2_1 = "2_1"; //NOI18N
    public static final String EJB_API = "org.netbeans.modules.j2ee.dd.api.ejb."; //NOI18N
    public static final String EJB_PACKAGE_PREFIX = "org.netbeans.modules.j2ee.dd.impl.ejb.model_"; //NOI18N
    
    private static Set COMMON_BEANS = new HashSet ();
    static {
        COMMON_BEANS.add("Icon"); //NOI18N
        COMMON_BEANS.add("InitParam"); //NOI18N
        COMMON_BEANS.add("EnvEntry"); //NOI18N
        COMMON_BEANS.add("EjbRef"); //NOI18N
        COMMON_BEANS.add("EjbLocalRef"); //NOI18N
        COMMON_BEANS.add("ResourceRef"); //NOI18N
        COMMON_BEANS.add("ResourceEnvRef"); //NOI18N
        COMMON_BEANS.add("ServiceRef"); //NOI18N
        COMMON_BEANS.add("Handler"); //NOI18N
        COMMON_BEANS.add("PortComponentRef"); //NOI18N
        COMMON_BEANS.add("MessageDestination"); //NOI18N
        COMMON_BEANS.add("MessageDestinationRef"); //NOI18N
        COMMON_BEANS.add("SecurityRole"); //NOI18N
        COMMON_BEANS.add("SecurityRoleRef"); //NOI18N
    }
    
    /**
     * Return a new instance of the specified type
     *
     * @param parent 	parent bean
     * @param beanName 	which bean to create
     * @param pkgName   implementation package name
     * @return BaseBean object 
     */

   public static BaseBean newBean(CommonDDBean parent, String beanName, String pkgName)  throws ClassNotFoundException {
        beanName = getImplementationBeanName(parent, beanName, pkgName);
        try {
	    Class beanClass = Class.forName(
				pkgName
                                + DOT
				+ beanName);
	    return (BaseBean) beanClass.getDeclaredConstructor().newInstance();

	} catch (Exception e) {
            if (e instanceof ClassNotFoundException) 
                throw (ClassNotFoundException)e;
            else {
                // This is a programming error.
                e.printStackTrace();
                throw new RuntimeException(
                    NbBundle.getMessage(CommonDDAccess.class,
                        "MSG_COMMONDDACCESS_ERROR", "newBean",	
                        ", package = " + pkgName + ", beanName = " + beanName, e+ ": " +e.getMessage()));
            }
	}
   }
	
       
   public static void addBean(CommonDDBean parent, CommonDDBean child, String beanName, String pkgName) {
	beanName = getImplementationBeanName(parent, beanName, pkgName);
        String apiPrefix = getAPIPrefix(beanName, pkgName);
	try {
            Class p = parent.getClass();
            Class ch = Class.forName(apiPrefix + beanName); //NOI18N
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
                    ", package = " + pkgName + ", beanName = " + beanName, e+ ": " +e.getMessage()));
	}
    }
	
   
   /**
     * Handle special cases of version differences
     */
    private static String getImplementationBeanName (CommonDDBean parent, String beanName, String pkgName) {
        if (beanName.equals("Session") || beanName.equals("Entity") || beanName.equals("MessageDriven")) { //NOI18N
            return beanName + "Bean"; //NOI18N
        } else
            return beanName;
    }
    
    private static String getAPIPrefix(String beanName, String pkgName){
        if (COMMON_BEANS.contains(beanName))
            return COMMON_API;
        if (pkgName.startsWith(EJB_PACKAGE_PREFIX))
            return EJB_API;
        else if (pkgName.startsWith(WEB_PACKAGE_PREFIX))
            return WEB_API;
        else if (pkgName.startsWith(APP_PACKAGE_PREFIX))
            return APP_API;
        assert false : "Invalid package prefix:" + pkgName;
        return "";  //NOI18N
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
	    getter = c.getMethod("get" + getNameForMethod((CommonDDBean)parent,beanProperty)); //NOI18N
	    result = getter.invoke(parent);
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
                    getter1 = c1.getMethod("get" + nameProperty); //NOI18N
                    result1 = getter1.invoke(beans[i]);
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
    private static String getNameForMethod (CommonDDBean parent, String beanName) {

        if ("InitParam".equals(beanName) && parent instanceof WebApp) return "ContextParam"; //NOI18N
        else if ("ServiceRefHandler".equals(beanName)) return "Handler"; //NOI18N
	else {
            return beanName;
	}
    }
}
