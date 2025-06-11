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

package org.netbeans.modules.j2ee.dd.impl.commonws;

import org.netbeans.modules.schema2beans.Version;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.j2ee.dd.api.common.*;

import org.netbeans.modules.j2ee.dd.impl.webservices.CommonDDAccess;


/**
 * Superclass for most s2b beans that provides useful methods for creating and finding beans
 * in bean graph.
 *
 * @author  Milan Kuchtiak
 */
public abstract class EnclosingBean extends BaseBean implements CommonDDBean, CreateCapability, FindCapability {
    
    /** Creates a new instance of EnclosingBean */
    public EnclosingBean(java.util.Vector comps, Version version) {
	super(comps, version);
    }
    
    /**
    * Method is looking for the nested bean according to the specified property and value 
    *
    * @param beanName e.g. "Servlet" or "ResourceRef"
    * @param propertyName e.g. "ServletName" or ResourceRefName"
    * @param value specific propertyName value e.g. "ControllerServlet" or "jdbc/EmployeeAppDb"
    * @return Bean satisfying the parameter values or null if not found
    */ 
    public CommonDDBean findBeanByName(String beanName, String propertyName, String value) {
        return (CommonDDBean)CommonDDAccess.findBeanByName(this, beanName, propertyName, value);
    }
    
    /**
    * An empty (not bound to schema2beans graph) bean is created corresponding to beanName 
    * regardless the Servlet Spec. version 
    * @param beanName bean name e.g. Servlet
    * @return CommonDDBean corresponding to beanName value
    */
    public CommonDDBean createBean(String beanName) throws ClassNotFoundException {
        return (CommonDDBean)CommonDDAccess.newBean(this, beanName, getPackagePostfix ());
    }
    
    private String getPackagePostfix () {
        return CommonDDAccess.WEBSERVICES_1_1;
    }
    
    public void write (org.openide.filesystems.FileObject fo) throws java.io.IOException {
        // PENDING
        // need to be implemented with Dialog opened when the file object is locked
    }
    
    public CommonDDBean addBean(String beanName, String[] propertyNames, Object[] propertyValues, String keyProperty) throws ClassNotFoundException, NameAlreadyUsedException {
        if (keyProperty!=null) {
            Object keyValue = null;
            if (propertyNames!=null)
                for (int i=0;i<propertyNames.length;i++) {
                    if (keyProperty.equals(propertyNames[i])) {
                        keyValue=propertyValues[i];
                        break;
                    }
                }
            if (keyValue instanceof String) {
                if (findBeanByName(beanName, keyProperty,(String)keyValue)!=null) {
                    throw new NameAlreadyUsedException(beanName,  keyProperty, (String)keyValue);
                }   
            }
        }
        CommonDDBean newBean = createBean(beanName);
        if (propertyNames!=null)
            for (int i=0;i<propertyNames.length;i++) {
                try {
                    ((BaseBean)newBean).setValue(propertyNames[i],propertyValues[i]);
                } catch (IndexOutOfBoundsException ex) {
                    ((BaseBean)newBean).setValue(propertyNames[i],new Object[]{propertyValues[i]});
                }
            }
        CommonDDAccess.addBean(this, newBean, beanName, getPackagePostfix ());
        return newBean;
    }
    
    public CommonDDBean addBean(String beanName) throws ClassNotFoundException {
        try {
            return addBean(beanName,null,null,null);
        } catch (NameAlreadyUsedException ex){}
        return null;
    }
    
    public void merge(RootInterface root, int mode) {
        this.merge((BaseBean)root,mode);
    }
    
}
