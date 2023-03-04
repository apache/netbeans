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
 * Utils.java
 *
 * Created on February 24, 2004, 2:20 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping;

import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;

import javax.management.Attribute;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;

/**
 *
 * @author  nityad
 */
public class Utils {

    /** Creates a new instance of Utils */
    public Utils() {
    }
    
    public static boolean isUserResource(ObjectName currentComp, MBeanServerConnection in_conn){
        boolean userResource = false;
        try{
            Object resType = getAttribute(currentComp, "object_type", in_conn);
            if(resType != null){
                if(resType.toString().equals("user")){
                    userResource = true;
                }
            }else{
                userResource = true;
            }
        }catch(Exception ex){
            System.out.println("Error during isUserResource " + ex.getLocalizedMessage());
        }
        return userResource;
    }
    
    public static String getAttribute(ObjectName objName, String attributeName, MBeanServerConnection in_conn){
        String attrValue = null;
        try{
            Object attValue = in_conn.getAttribute(objName, attributeName);
            if(attValue != null){
                attrValue = attValue.toString();
            }
        }catch(Exception ex){
            return null;
            //suppress Exception. Callers to any of the getters should handle null condition
        }
        return attrValue;
    }
    
    /* Fix bug#5032958 - If deployed module's directory is renamed or deleted, the module is not available through
     * the runtime mbeans(above). They can be accessed only via the config mbeans
     * To identify type of modules:
     * ObjectName of runtime mbeans has key j2eeType
     * ObjectName of config mbeans has key type
     *
     *  In this scenario the incoming parameter for Runtime Object Name will be null. This needs to be handled.
     */
    
    public static ObjectName getRequiredObjectName(ObjectName origName, ObjectName confName, Attribute attribute, MBeanServerConnection in_conn){
        ObjectName oName = null;
        try{
            String name = attribute.getName();
            if(origName != null){
                MBeanInfo bnInfo = in_conn.getMBeanInfo(origName);
                MBeanAttributeInfo[] attInfo = bnInfo.getAttributes();
                String[] attNames = new String[attInfo.length];
                for(int i=0; i<attInfo.length; i++){
                    attNames[i] = attInfo[i].getName();
                }
                Set appList = new HashSet(Arrays.asList(attNames));
                if(appList.contains(name)){
                    oName = origName;
                    return oName;
                }
            }
            MBeanInfo beanInfo = in_conn.getMBeanInfo(confName);
            MBeanAttributeInfo[] attrInfo = beanInfo.getAttributes();
            String[] attrNames = new String[attrInfo.length];
            for(int i=0; i<attrInfo.length; i++){
                attrNames[i] = attrInfo[i].getName();
            }
            Set newList = new HashSet(Arrays.asList(attrNames));
            if(newList.contains(name)){
                oName = confName;
            }
            
        }catch(Exception ex){
            //System.out.println("Error in getRequiredObjectName " + ex.getLocalizedMessage());
            return oName;
        }
        return oName;
    }
    
    /* Fix bug#5032958 - If deployed module's directory is renamed or deleted, the module is not available through
     * the runtime mbeans(above). They can be accessed only via the config mbeans
     * To identify type of modules:
     * ObjectName of runtime mbeans has key j2eeType
     * ObjectName of config mbeans has key type
     *
     *  In this scenario the incoming parameter for Runtime Object Name will be null. This needs to be handled.
     */
    public static ObjectName getRequiredObjectName(ObjectName origName, ObjectName confName, String operationName, MBeanServerConnection in_conn){
        ObjectName oName = null;
        try{
            if(origName != null){
                MBeanInfo bnInfo = in_conn.getMBeanInfo(origName);
                MBeanOperationInfo[] operInfo = bnInfo.getOperations();
                String[] operNames = new String[operInfo.length];
                for(int i=0; i<operInfo.length; i++){
                    operNames[i] = operInfo[i].getName();
                }
                Set operList = new HashSet(Arrays.asList(operNames));
                if(operList.contains(operationName)){
                    oName = origName;
                    return oName;
                }
            }
            MBeanInfo beanInfo = in_conn.getMBeanInfo(confName);
            MBeanOperationInfo[] operationInfo = beanInfo.getOperations();
            String[] operationNames = new String[operationInfo.length];
            for(int i=0; i<operationInfo.length; i++){
                operationNames[i] = operationInfo[i].getName();
            }
            Set newOperList = new HashSet(Arrays.asList(operationNames));
            if(newOperList.contains(operationName)){
                oName = confName;
            }
        }catch(Exception ex){
            //System.out.println("Error in getRequiredObjectName " + ex.getLocalizedMessage());
            return oName;
        }
        return oName;
    }
    
    public static String[] getStringSignature(){
        String[] signature = new String[]{"java.lang.String"}; //NOI18N
        return signature;
    }
    
    public static Object[] getStringParam(String paramValue){
        String props = paramValue;
        Object[] params = new Object[]{props};
        return params;
    }
}
