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
 * ConnPoolBean.java
 *
 * Created on September 12, 2003, 4:18 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.util.ResourceBundle;
import java.util.Vector;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.netbeans.modules.j2ee.sun.share.serverresources.JdbcCP;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.*;

/**
 *
 * @author  nityad
 */
public class ConnPoolBean extends JdbcCP implements java.io.Serializable{
    
    /** Creates new ConnPoolBean */
    public ConnPoolBean() {

    }
    
    public String getName() {
        return super.getName();
    }
    
    public static ConnPoolBean createBean(JdbcConnectionPool pool) {
        ConnPoolBean bean = new ConnPoolBean();
                
        bean.setName(pool.getName());
        bean.setDescription(pool.getDescription());
        bean.setDsClass(pool.getDatasourceClassname());
        bean.setResType(pool.getResType());
        bean.setSteadyPoolSize(pool.getSteadyPoolSize());
        bean.setMaxPoolSize(pool.getMaxPoolSize());
        bean.setMaxWaitTimeMilli(pool.getMaxWaitTimeInMillis());
        bean.setPoolResizeQty(pool.getPoolResizeQuantity());
        bean.setIdleIimeoutSecond(pool.getIdleTimeoutInSeconds());
        String tranxIsolation = pool.getTransactionIsolationLevel();
        if(tranxIsolation == null){
            tranxIsolation = ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/editors/Bundle").getString("LBL_driver_default");     //NOI18N
        }
        bean.setTranxIsoLevel(tranxIsolation);
        bean.setIsIsoLevGuaranteed(pool.getIsIsolationLevelGuaranteed());
        bean.setIsConnValidReq(pool.getIsConnectionValidationRequired());
        bean.setConnValidMethod(pool.getConnectionValidationMethod());
        bean.setValidationTableName(pool.getValidationTableName());
        bean.setFailAllConns(pool.getFailAllConnections());
        bean.setNontranxconns(pool.getNonTransactionalConnections());
        bean.setAllowNonComponentCallers(pool.getAllowNonComponentCallers());
                
        PropertyElement[] extraProperties = pool.getPropertyElement();
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
        JdbcConnectionPool connPool = res.newJdbcConnectionPool();
        connPool.setDescription(getDescription());
        connPool.setName(getName());
        connPool.setDatasourceClassname(getDsClass());
        connPool.setResType(getResType());
        connPool.setSteadyPoolSize(getSteadyPoolSize());
        connPool.setMaxPoolSize(getMaxPoolSize());
        connPool.setMaxWaitTimeInMillis(getMaxWaitTimeMilli());
        connPool.setPoolResizeQuantity(getPoolResizeQty());
        connPool.setIdleTimeoutInSeconds(getIdleIimeoutSecond());
        String isolation = getTranxIsoLevel();
        String defaultChoice = ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/editors/Bundle").getString("LBL_driver_default");     //NOI18N
        if (isolation != null && (isolation.length() == 0 || isolation.equals(defaultChoice))) {  
            isolation = null;
        }
        connPool.setTransactionIsolationLevel(isolation);
        connPool.setIsIsolationLevelGuaranteed(getIsIsoLevGuaranteed());
        connPool.setIsConnectionValidationRequired(getIsConnValidReq());
        connPool.setConnectionValidationMethod(getConnValidMethod());
        connPool.setValidationTableName(getValidationTableName());
        connPool.setFailAllConnections(getFailAllConns());
        connPool.setNonTransactionalConnections(getNontranxconns());
        connPool.setAllowNonComponentCallers(getAllowNonComponentCallers());
        NameValuePair[] params = getExtraParams();
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                NameValuePair pair = params[i];
                PropertyElement prop = connPool.newPropertyElement();
                prop = populatePropertyElement(prop, pair); 
                connPool.addPropertyElement(prop);
            }
        }
        res.addJdbcConnectionPool(connPool);
        return res;
    }    
}
