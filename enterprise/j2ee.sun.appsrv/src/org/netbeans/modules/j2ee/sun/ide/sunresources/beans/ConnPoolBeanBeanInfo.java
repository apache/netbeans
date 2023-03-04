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
package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.beans.*;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.sun.ide.editors.BooleanEditor;
import org.netbeans.modules.j2ee.sun.ide.editors.Int0Editor;
import org.netbeans.modules.j2ee.sun.ide.editors.LongEditor;
import org.netbeans.modules.j2ee.sun.ide.editors.IsolationLevelEditor;
import org.netbeans.modules.j2ee.sun.ide.editors.DataSourceTypeEditor;
import org.netbeans.modules.j2ee.sun.ide.editors.ValidationMethodEditor;
import org.openide.util.Exceptions;

public class ConnPoolBeanBeanInfo extends SimpleBeanInfo {
    
    private static String getLabel(String key){
        return NbBundle.getMessage(ConnPoolBean.class, key);
    }

    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     *
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean.class , null );
        return beanDescriptor;     
    }
    
    /**
     * Gets the bean's <code>PropertyDescriptor</code>s.
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
        int PROPERTY_allowNonComponentCallers = 0;
        int PROPERTY_connValidMethod = 1;
        int PROPERTY_description = 2;
        int PROPERTY_dsClass = 3;
        int PROPERTY_failAllConns = 4;
        int PROPERTY_idleIimeoutSecond = 5;
        int PROPERTY_isConnValidReq = 6;
        int PROPERTY_isIsoLevGuaranteed = 7;
        int PROPERTY_maxPoolSize = 8;
        int PROPERTY_maxWaitTimeMilli = 9;
        int PROPERTY_name = 10;
        int PROPERTY_nontranxconns = 11;
        int PROPERTY_poolResizeQty = 12;
        int PROPERTY_resType = 13;
        int PROPERTY_steadyPoolSize = 14;
        int PROPERTY_tranxIsoLevel = 15;
        int PROPERTY_validationTableName = 16;
        PropertyDescriptor[] properties = new PropertyDescriptor[17];
    
        try {
            properties[PROPERTY_allowNonComponentCallers] = new PropertyDescriptor ( "allowNonComponentCallers", org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean.class, "getAllowNonComponentCallers", "setAllowNonComponentCallers" ); // NOI18N
            properties[PROPERTY_allowNonComponentCallers].setDisplayName ( getLabel("LBL_allow_non_comp_callers") );
            properties[PROPERTY_allowNonComponentCallers].setShortDescription ( getLabel("LBL_allow_non_comp_callers") );
            properties[PROPERTY_allowNonComponentCallers].setPropertyEditorClass ( BooleanEditor.class  );
            properties[PROPERTY_connValidMethod] = new PropertyDescriptor ( "connValidMethod", org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean.class, "getConnValidMethod", "setConnValidMethod" ); // NOI18N
            properties[PROPERTY_connValidMethod].setDisplayName ( getLabel("LBL_conn_valid_method") );
            properties[PROPERTY_connValidMethod].setShortDescription ( getLabel("DSC_conn_valid_method") );
            properties[PROPERTY_connValidMethod].setPropertyEditorClass ( ValidationMethodEditor.class );
            properties[PROPERTY_description] = new PropertyDescriptor ( "description", org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean.class, "getDescription", "setDescription" ); // NOI18N
            properties[PROPERTY_description].setDisplayName ( getLabel("LBL_Description") );
            properties[PROPERTY_description].setShortDescription ( getLabel("DSC_Description") );
            properties[PROPERTY_dsClass] = new PropertyDescriptor ( "dsClass", org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean.class, "getDsClass", "setDsClass" ); // NOI18N
            properties[PROPERTY_dsClass].setDisplayName ( getLabel("LBL_DSClassName") );
            properties[PROPERTY_dsClass].setShortDescription ( getLabel("DSC_DSClassName") );
            properties[PROPERTY_failAllConns] = new PropertyDescriptor ( "failAllConns", org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean.class, "getFailAllConns", "setFailAllConns" ); // NOI18N
            properties[PROPERTY_failAllConns].setDisplayName ( getLabel("LBL_fail_all_connections") );
            properties[PROPERTY_failAllConns].setShortDescription ( getLabel("DSC_fail_all_connections") );
            properties[PROPERTY_failAllConns].setPropertyEditorClass ( BooleanEditor.class );
            properties[PROPERTY_idleIimeoutSecond] = new PropertyDescriptor ( "idleIimeoutSecond", org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean.class, "getIdleIimeoutSecond", "setIdleIimeoutSecond" ); // NOI18N
            properties[PROPERTY_idleIimeoutSecond].setDisplayName ( getLabel("LBL_connection_idle_timeout_in_seconds") );
            properties[PROPERTY_idleIimeoutSecond].setShortDescription ( getLabel("DSC_connection_idle_timeout_in_seconds") );
            properties[PROPERTY_idleIimeoutSecond].setPropertyEditorClass ( LongEditor.class );
            properties[PROPERTY_isConnValidReq] = new PropertyDescriptor ( "isConnValidReq", org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean.class, "getIsConnValidReq", "setIsConnValidReq" ); // NOI18N
            properties[PROPERTY_isConnValidReq].setDisplayName ( getLabel("LBL_is_connection_validation_required") );
            properties[PROPERTY_isConnValidReq].setShortDescription ( getLabel("DSC_is_connection_validation_required") );
            properties[PROPERTY_isConnValidReq].setPropertyEditorClass ( BooleanEditor.class );
            properties[PROPERTY_isIsoLevGuaranteed] = new PropertyDescriptor ( "isIsoLevGuaranteed", org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean.class, "getIsIsoLevGuaranteed", "setIsIsoLevGuaranteed" ); // NOI18N
            properties[PROPERTY_isIsoLevGuaranteed].setDisplayName ( getLabel("LBL_is_isolation_level_guaranteed") );
            properties[PROPERTY_isIsoLevGuaranteed].setShortDescription ( getLabel("DSC_is_isolation_level_guaranteed") );
            properties[PROPERTY_isIsoLevGuaranteed].setPropertyEditorClass ( BooleanEditor.class );
            properties[PROPERTY_maxPoolSize] = new PropertyDescriptor ( "maxPoolSize", org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean.class, "getMaxPoolSize", "setMaxPoolSize" ); // NOI18N
            properties[PROPERTY_maxPoolSize].setDisplayName ( getLabel("LBL_max_pool_size") );
            properties[PROPERTY_maxPoolSize].setShortDescription ( getLabel("DSC_max_pool_size") );
            properties[PROPERTY_maxPoolSize].setPropertyEditorClass ( Int0Editor.class );
            properties[PROPERTY_maxWaitTimeMilli] = new PropertyDescriptor ( "maxWaitTimeMilli", org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean.class, "getMaxWaitTimeMilli", "setMaxWaitTimeMilli" ); // NOI18N
            properties[PROPERTY_maxWaitTimeMilli].setDisplayName ( getLabel("LBL_max_connection_wait_time_in_millis") );
            properties[PROPERTY_maxWaitTimeMilli].setShortDescription ( getLabel("DSC_max_connection_wait_time_in_millis") );
            properties[PROPERTY_maxWaitTimeMilli].setPropertyEditorClass ( LongEditor.class );
            properties[PROPERTY_name] = new PropertyDescriptor ( "name", org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean.class, "getName", "setName" ); // NOI18N
            properties[PROPERTY_name].setDisplayName ( getLabel("LBL_pool_name") );
            properties[PROPERTY_name].setShortDescription ( getLabel("DSC_pool_name") );
            properties[PROPERTY_nontranxconns] = new PropertyDescriptor ( "nontranxconns", org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean.class, "getNontranxconns", "setNontranxconns" ); // NOI18N
            properties[PROPERTY_nontranxconns].setDisplayName ( getLabel("LBL_non_tranx_conns")  );
            properties[PROPERTY_nontranxconns].setShortDescription ( getLabel("DSC_non_tranx_conns")  );
            properties[PROPERTY_nontranxconns].setPropertyEditorClass ( BooleanEditor.class  );
            properties[PROPERTY_poolResizeQty] = new PropertyDescriptor ( "poolResizeQty", org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean.class, "getPoolResizeQty", "setPoolResizeQty" ); // NOI18N
            properties[PROPERTY_poolResizeQty].setDisplayName ( getLabel("LBL_pool_resize_qty") );
            properties[PROPERTY_poolResizeQty].setShortDescription ( getLabel("DSC_pool_resize_qty") );
            properties[PROPERTY_poolResizeQty].setPropertyEditorClass ( Int0Editor.class );
            properties[PROPERTY_resType] = new PropertyDescriptor ( "resType", org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean.class, "getResType", "setResType" ); // NOI18N
            properties[PROPERTY_resType].setDisplayName ( getLabel("LBL_res_type") );
            properties[PROPERTY_resType].setShortDescription ( getLabel("DSC_res_type") );
            properties[PROPERTY_resType].setPropertyEditorClass ( DataSourceTypeEditor.class );
            properties[PROPERTY_steadyPoolSize] = new PropertyDescriptor ( "steadyPoolSize", org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean.class, "getSteadyPoolSize", "setSteadyPoolSize" ); // NOI18N
            properties[PROPERTY_steadyPoolSize].setDisplayName ( getLabel("LBL_steady_pool_size") );
            properties[PROPERTY_steadyPoolSize].setShortDescription ( getLabel("DSC_steady_pool_size") );
            properties[PROPERTY_steadyPoolSize].setPropertyEditorClass ( Int0Editor.class );
            properties[PROPERTY_tranxIsoLevel] = new PropertyDescriptor ( "tranxIsoLevel", org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean.class, "getTranxIsoLevel", "setTranxIsoLevel" ); // NOI18N
            properties[PROPERTY_tranxIsoLevel].setDisplayName ( getLabel("LBL_transaction_isolation_level") );
            properties[PROPERTY_tranxIsoLevel].setShortDescription ( getLabel("DSC_transaction_isolation_level") );
            properties[PROPERTY_tranxIsoLevel].setPropertyEditorClass ( IsolationLevelEditor.class );
            properties[PROPERTY_validationTableName] = new PropertyDescriptor ( "validationTableName", org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean.class, "getValidationTableName", "setValidationTableName" ); // NOI18N
            properties[PROPERTY_validationTableName].setDisplayName ( getLabel("LBL_validation_table_name") );
            properties[PROPERTY_validationTableName].setShortDescription ( getLabel("DSC_validation_table_name") );
        }
        catch(IntrospectionException e) {
            Exceptions.printStackTrace(e);
        }
        
        return properties;
    }
    
    /**
     * Gets the bean's <code>EventSetDescriptor</code>s.
     *
     * @return  An array of EventSetDescriptors describing the kinds of
     * events fired by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        int EVENT_propertyChangeListener = 0;
        EventSetDescriptor[] eventSets = new EventSetDescriptor[1];
    
        try {
            eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[] {"propertyChange"}, "addPropertyChangeListener", "removePropertyChangeListener" ); // NOI18N
        }
        catch(IntrospectionException e) {
            Exceptions.printStackTrace(e);
        }
        return eventSets;
    }
    
    /**
     * Gets the bean's <code>MethodDescriptor</code>s.
     *
     * @return  An array of MethodDescriptors describing the methods
     * implemented by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public MethodDescriptor[] getMethodDescriptors() {
        return new MethodDescriptor[0];
    }
    
}

