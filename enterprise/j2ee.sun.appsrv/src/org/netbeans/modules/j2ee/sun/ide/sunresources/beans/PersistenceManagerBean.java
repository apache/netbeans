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
/*
 * PersistenceManagerBean.java
 *
 * Created on September 17, 2003, 1:29 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.util.Vector;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.netbeans.modules.j2ee.sun.share.serverresources.PersistenceManagerResource;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.*;

/**
 *
 * @author  nityad
 */
public class PersistenceManagerBean extends PersistenceManagerResource implements java.io.Serializable {
    
    /** Creates a new instance of PersistenceManagerBean */
    public PersistenceManagerBean() {
    }
    
    public String getName() {
        return super.getName();
    }
    
    public String getJndiName(){
        return super.getJndiName();
    }
    
    public static PersistenceManagerBean createBean(PersistenceManagerFactoryResource pmfresource) {
        PersistenceManagerBean bean = new PersistenceManagerBean();
        //name attribute in bean is for studio display purpose. 
        //It is not part of the persistence-manager-factory-resource dtd.
        bean.setName(pmfresource.getJndiName());
        bean.setDescription(pmfresource.getDescription());
        bean.setJndiName(pmfresource.getJndiName());
        bean.setFactoryClass(pmfresource.getFactoryClass());
        bean.setDatasourceJndiName(pmfresource.getJdbcResourceJndiName());
        bean.setIsEnabled(pmfresource.getEnabled());
        
        PropertyElement[] extraProperties = pmfresource.getPropertyElement();
        Vector vec = new Vector();       
        for (int i = 0; i < extraProperties.length; i++) {
            NameValuePair pair = new NameValuePair();
            pair.setParamName(extraProperties[i].getName());
            pair.setParamValue(extraProperties[i].getValue());
            vec.add(pair);
        }
        
        if (vec != null && vec.size() > 0) {
            NameValuePair[] props = new NameValuePair[vec.size()];
            bean.setExtraParams((NameValuePair[])vec.toArray(props));
        }
        
        return bean;
    }
    
    public Resources getGraph(){
        Resources res = getResourceGraph();
        return getBeanInGraph(res);
    }    
    
    public Resources getBeanInGraph(Resources res){
        PersistenceManagerFactoryResource pmfresource = res.newPersistenceManagerFactoryResource();
        pmfresource.setDescription(getDescription());
        pmfresource.setJndiName(getJndiName());
        pmfresource.setFactoryClass(getFactoryClass());
        pmfresource.setJdbcResourceJndiName(getDatasourceJndiName());
        pmfresource.setEnabled(getIsEnabled());
        
        // set properties
        NameValuePair[] params = getExtraParams();
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                NameValuePair pair = params[i];
                PropertyElement prop = pmfresource.newPropertyElement();
                prop = populatePropertyElement(prop, pair);
                pmfresource.addPropertyElement(prop);
            }
        }
        
        res.addPersistenceManagerFactoryResource(pmfresource);
        return res;
    }
    
}
