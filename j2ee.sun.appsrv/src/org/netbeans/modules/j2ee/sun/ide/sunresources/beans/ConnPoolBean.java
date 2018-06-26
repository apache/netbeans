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
