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
import org.openide.util.Exceptions;

public class JavaMailSessionBeanBeanInfo extends SimpleBeanInfo {

    private static String getLabel(String key){
        return NbBundle.getMessage(JavaMailSessionBean.class,key);
    }

    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     *
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( JavaMailSessionBean.class , null );                              
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
        int PROPERTY_description = 0;
        int PROPERTY_fromAddr = 1;
        int PROPERTY_hostName = 2;
        int PROPERTY_isDebug = 3;
        int PROPERTY_isEnabled = 4;
        int PROPERTY_jndiName = 5;
        int PROPERTY_name = 6;
        int PROPERTY_storeProt = 7;
        int PROPERTY_storeProtClass = 8;
        int PROPERTY_transProt = 9;
        int PROPERTY_transProtClass = 10;
        int PROPERTY_userName = 11;
        PropertyDescriptor[] properties = new PropertyDescriptor[12];
    
        try {
            properties[PROPERTY_description] = new PropertyDescriptor ( "description", JavaMailSessionBean.class, "getDescription", "setDescription" );
            properties[PROPERTY_description].setDisplayName ( getLabel("LBL_Description") );
            properties[PROPERTY_description].setShortDescription ( getLabel("DSC_Description") );
            properties[PROPERTY_fromAddr] = new PropertyDescriptor ( "fromAddr", JavaMailSessionBean.class, "getFromAddr", "setFromAddr" );
            properties[PROPERTY_fromAddr].setDisplayName ( getLabel("LBL_from") );
            properties[PROPERTY_fromAddr].setShortDescription ( getLabel("DSC_from") );
            properties[PROPERTY_hostName] = new PropertyDescriptor ( "hostName", JavaMailSessionBean.class, "getHostName", "setHostName" );
            properties[PROPERTY_hostName].setDisplayName ( getLabel("LBL_host") );
            properties[PROPERTY_hostName].setShortDescription ( getLabel("DSC_host") );
            properties[PROPERTY_isDebug] = new PropertyDescriptor ( "isDebug", JavaMailSessionBean.class, "getIsDebug", "setIsDebug" );
            properties[PROPERTY_isDebug].setDisplayName ( getLabel("LBL_debug") );
            properties[PROPERTY_isDebug].setShortDescription ( getLabel("DSC_debug") );
            properties[PROPERTY_isDebug].setPropertyEditorClass ( BooleanEditor.class );
            properties[PROPERTY_isEnabled] = new PropertyDescriptor ( "isEnabled", JavaMailSessionBean.class, "getIsEnabled", "setIsEnabled" );
            properties[PROPERTY_isEnabled].setDisplayName ( getLabel("LBL_Enabled") );
            properties[PROPERTY_isEnabled].setShortDescription ( getLabel("DSC_Enabled") );
            properties[PROPERTY_isEnabled].setPropertyEditorClass ( BooleanEditor.class );
            properties[PROPERTY_jndiName] = new PropertyDescriptor ( "jndiName", JavaMailSessionBean.class, "getJndiName", "setJndiName" );
            properties[PROPERTY_jndiName].setDisplayName ( getLabel("LBL_JndiName") );
            properties[PROPERTY_jndiName].setShortDescription ( getLabel("DSC_MailJndiName") );
            properties[PROPERTY_name] = new PropertyDescriptor ( "name", JavaMailSessionBean.class, "getName", "setName" );
            properties[PROPERTY_name].setHidden ( true );
            properties[PROPERTY_storeProt] = new PropertyDescriptor ( "storeProt", JavaMailSessionBean.class, "getStoreProt", "setStoreProt" );
            properties[PROPERTY_storeProt].setDisplayName ( getLabel("LBL_StoreProtocol") );
            properties[PROPERTY_storeProt].setShortDescription ( getLabel("DSC_StoreProtocol") );
            properties[PROPERTY_storeProtClass] = new PropertyDescriptor ( "storeProtClass", JavaMailSessionBean.class, "getStoreProtClass", "setStoreProtClass" );
            properties[PROPERTY_storeProtClass].setDisplayName ( getLabel("LBL_StoreProtocolClass") );
            properties[PROPERTY_storeProtClass].setShortDescription ( getLabel("DSC_StoreProtocolClass") );
            properties[PROPERTY_transProt] = new PropertyDescriptor ( "transProt", JavaMailSessionBean.class, "getTransProt", "setTransProt" );
            properties[PROPERTY_transProt].setDisplayName ( getLabel("LBL_TransportProtocol") );
            properties[PROPERTY_transProt].setShortDescription ( getLabel("DSC_TransportProtocol") );
            properties[PROPERTY_transProtClass] = new PropertyDescriptor ( "transProtClass", JavaMailSessionBean.class, "getTransProtClass", "setTransProtClass" );
            properties[PROPERTY_transProtClass].setDisplayName ( getLabel("LBL_TransportProtocol") );
            properties[PROPERTY_transProtClass].setShortDescription ( getLabel("DSC_TransportProtocol") );
            properties[PROPERTY_userName] = new PropertyDescriptor ( "userName", JavaMailSessionBean.class, "getUserName", "setUserName" );
            properties[PROPERTY_userName].setDisplayName ( getLabel("LBL_user") );
            properties[PROPERTY_userName].setShortDescription ( getLabel("DSC_user") );
        }
        catch( IntrospectionException e) {
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
        eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( org.netbeans.modules.j2ee.sun.ide.sunresources.beans.JavaMailSessionBean.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[] {"propertyChange"}, "addPropertyChangeListener", "removePropertyChangeListener" );
        }
        catch( IntrospectionException e) {
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

