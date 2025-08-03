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
/*
 * JvmOptions.java
 *
 * Created on February 24, 2004, 1:21 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping;

import javax.management.Attribute;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;

import java.rmi.RemoteException;
import java.util.Locale;



// ludo import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.Util;

/**
 *
 * @author  nityad
 */
public class JvmOptions extends ModuleMBean implements Constants{
    
    private ObjectName configObjName = null;
    private boolean isServerEightOne = true;
    /** Creates a new instance of JvmOptions */
    public JvmOptions(MBeanServerConnection in_conn) {
        super(in_conn);
        this.configObjName = createConfigObjectName();
        this.runtimeObjName = configObjName;
    }
    
    public JvmOptions(ObjectName objName, MBeanServerConnection in_conn) {
        super(objName, in_conn);
        this.configObjName = createConfigObjectName();
    }
    
    public JvmOptions(ObjectName objName, MBeanServerConnection in_conn, boolean in_isServerEightOne) {
        super(objName, in_conn);
        this.configObjName = createConfigObjectName();
        this.isServerEightOne = in_isServerEightOne;
    }
    
    private ObjectName createConfigObjectName(){
       ObjectName configName = null;
       try{
           configName = new ObjectName(this.MAP_JVMOptions);
       }catch(Exception ex){
           return null;
       } 
       return configName;
    }
    
    public ObjectName getConfigObjectName(){
        return configObjName;
    }
    public AttributeList getAttributes(String[] attributes){
        AttributeList attList = null;
        try{
            String addrValue = getAddressValue();
            Attribute jpdaAttr = null;
            if(isSharedMemory()){
                //Fix for bug# 4989322 - solaris does not support shmem
                if(isWindows())
                    jpdaAttr = new Attribute(SHARED_MEM, (Object)addrValue);
                else{
                //ludo    Util.setStatusBar(bundle.getString("Msg_SolarisShmem"));
                    Attribute newAttr = new Attribute(DEBUG_OPTIONS, DEF_DEUG_OPTIONS_SOCKET);
                    this.setAttribute(newAttr);
                    Integer value = 11000;
                    jpdaAttr = new Attribute(JPDA_PORT, value);
                }
            }else{ 
                Integer value = Integer.valueOf(addrValue);
                jpdaAttr = new Attribute(JPDA_PORT, (Object)value);
            }    
            attList = this.conn.getAttributes(this.configObjName, attributes);
            attList.add(jpdaAttr);
        }catch(Exception ex){
            return attList;
            //Attribute list is empty
        }
        return attList;
    }
    
    public MBeanInfo getMBeanInfo() {
        MBeanInfo updatedInfo = null;
        try{
            MBeanInfo newInfo = this.conn.getMBeanInfo(this.configObjName);
            
            //Adding custom property jpda-port number
            MBeanAttributeInfo[] newAttrInfo = newInfo.getAttributes();
            int size = newAttrInfo.length + 1;
            MBeanAttributeInfo[] updatedAttrInfo = new MBeanAttributeInfo[size];
            //get list of attributes from Config bean
            for ( int i=0; i<newAttrInfo.length; i++ ) {
                updatedAttrInfo[i] = newAttrInfo[i];
            }
            
            //Fix for bug# 4989322 - solaris does not support shmem
            if(isSharedMemory() && isWindows())
                updatedAttrInfo[size-1] = new MBeanAttributeInfo(SHARED_MEM, "java.lang.String", "Shared Memory", true, true, false); //NOI18N
            else
                updatedAttrInfo[size-1] = new MBeanAttributeInfo(JPDA_PORT, "int", "JPDA port number", true, true, false); //NOI18N
            
            updatedInfo = new MBeanInfo(newInfo.getClassName(), newInfo.getDescription(), updatedAttrInfo, newInfo.getConstructors(),
                  newInfo.getOperations(), newInfo.getNotifications());
            
        }catch(Exception ex){
            //System.out.println("Error in getMBeanInfo of JVMOptions " + ex.getMessage());
            return updatedInfo;
        }
        return updatedInfo;
    }
    
    public void setAttribute(Attribute attribute) throws RemoteException, InstanceNotFoundException, AttributeNotFoundException,
     InvalidAttributeValueException, MBeanException, ReflectionException, java.io.IOException{
        if(attribute.getName().equals(this.JPDA_PORT)){
            if(attribute.getValue() != null){
                setAddressValue(attribute.getValue().toString());
            }
        }else if(attribute.getName().equals(this.SHARED_MEM)){
            if(attribute.getValue() != null){
                    setAddressValue(attribute.getValue().toString());
            }
        }else if(attribute.getName().equals(this.DEBUG_OPTIONS)){
            //Fix for bug# 4989322 - solaris does not support shmem
            if((attribute.getValue() != null) && (attribute.getValue().toString().indexOf(ISMEM) == -1)){
                this.conn.setAttribute(this.configObjName, attribute);
            }else{
                if(isWindows()){
                    this.conn.setAttribute(this.configObjName, attribute);
                }
              //ludo  else
              //ludo      Util.setStatusBar(bundle.getString("Msg_SolarisShmem"));
            }
        }else{
            this.conn.setAttribute(this.configObjName, attribute);
        }
    }
    
    private String getDebugOptions(){
        Object debugOptionsAttr = getConfigAttributeValue(DEBUG_OPTIONS); 
        String debugOptionsVal = null;
        if(debugOptionsAttr != null){
            debugOptionsVal = debugOptionsAttr.toString().trim();
        }
        return debugOptionsVal;
    }
    
    public String getAddressValue(){
        String debugOptionsVal = getDebugOptions();
        if(debugOptionsVal != null){
            debugOptionsVal = debugOptionsVal.substring(debugOptionsVal.indexOf(DEBUG_OPTIONS_ADDRESS)+DEBUG_OPTIONS_ADDRESS.length(), debugOptionsVal.length());
            int hasMore = debugOptionsVal.indexOf(","); //NOI18N
            if(hasMore != -1){ 
                debugOptionsVal = debugOptionsVal.substring(0, hasMore);
            }
        } 
        return debugOptionsVal;
    }
    
    public void setAddressValue(String portNumber){
        try{
            String debugOptionsVal = getDebugOptions();
            String debugOptionsStart = debugOptionsVal.substring(0, debugOptionsVal.indexOf(DEBUG_OPTIONS_ADDRESS) + DEBUG_OPTIONS_ADDRESS.length());
            debugOptionsStart = debugOptionsStart + portNumber;
            String inMiddle = debugOptionsVal.substring(debugOptionsVal.indexOf(DEBUG_OPTIONS_ADDRESS) + DEBUG_OPTIONS_ADDRESS.length(), debugOptionsVal.length());
            int hasMore = inMiddle.indexOf(","); //NOI18N
            if(hasMore != -1){
                String debugOptionsEnd = inMiddle.substring(hasMore);
                debugOptionsStart = debugOptionsStart + debugOptionsEnd;
            }
            Attribute newAttr = new Attribute(DEBUG_OPTIONS, debugOptionsStart);
            this.setAttribute(newAttr);
        }catch(Exception ex){
            //System.out.println("Cannot set  debug options : " + ex.getMessage());
        }
    }
    
    public String getClassPath(){
        String serverClassPath = getConfigAttributeValue("server_classpath"); //NOI18N
        return serverClassPath;
    } 
    
    public void setClassPath(String classpath){
        try{
            Attribute classPathAttr = new Attribute("server_classpath", classpath); //NOI18N
            this.setAttribute(classPathAttr);
        }catch(Exception ex){
            //System.out.println("Error in setClassPath " + ex.getMessage());
        }
    }
    
    public String getConfigAttributeValue(String attributeName){
        return super.getAttribute(this.configObjName, attributeName);
    }        
    
    //Fix for bug#5020943 - switch between dt_shmem & dt_socket in JVM Options
    public boolean isSharedMemory(){
        boolean isShared = false;
        String debugOpt = getDebugOptions();
        if((debugOpt.indexOf(ISMEM) == -1) && (debugOpt.indexOf(ISSOCKET) == -1)){
            try{
                Attribute attr = null;
                if(this.isServerEightOne)
                    attr = new Attribute(DEBUG_OPTIONS, DEF_DEUG_OPTIONS_81);
                else
                    attr = new Attribute(DEBUG_OPTIONS, DEF_DEUG_OPTIONS);
                setAttribute(attr);
            }catch(Exception ex){
                //System.out.println("Invalid value for debug options. Resetting to default value ");
            }    
        }else if(debugOpt.indexOf(ISSOCKET) != -1){
            String addrValue = getAddressValue();
            Integer value = null;
            try{
                value = Integer.valueOf(addrValue);
            }catch(Exception ex){
                if(this.isServerEightOne){
                    value = 9009;
                    setAddressValue("9009"); //NOI18N
                }else{
                    value = 1044;
                    setAddressValue("1044"); //NOI18N
                }    
            }
        }else if (debugOpt.indexOf(ISMEM) != -1){
            return true;
        }
        return isShared;
    }
    
    //Fix for bug# 4989322 - solaris does not support shmem
   public boolean isWindows(){
        Object javaHome = getConfigAttributeValue(JAVA_HOME);
        if (javaHome==null){//scary... assume not windows.
            return false;
        }
        try{
            //FIXME -- temporary workaround till plugin can map the env keys returned by appserver
            //to actual values
            String local_osName = System.getProperty("os.name"); //NOI18N
            if(local_osName.toLowerCase(Locale.ENGLISH).indexOf("windows") != -1) //NOI18N
                return true;
            else
                return false;
            /*String javaHomeVal = javaHome.toString().trim();            
            String semiColon = javaHomeVal.substring(1,2);
            if(semiColon.equals(":"))//NOI18N
                return true;
            else
                return false;*/
        }
        catch (IndexOutOfBoundsException ee){
            return false;
        }
    }
    
    //Fix for bug# 4989322 - solaris does not support shmem
    /** API to check if the server is on solaris or windows
     * @param: address value for dt_shmem.
     * If windows, sets the transport value to dt_shmem
     * If solaris, sets the transport value to dt_socket 
     * and if address is 1044, changes it to 11000 or leaves it as is.
     */ 
    public void setDefaultTransportForDebug(String addressVal) {
        Attribute newAttr = null;
        if(isWindows()){
           String value = DEF_DEUG_OPTIONS_SHMEM;
           if(addressVal != null)
               value = value + addressVal;
           else
               value = value + "defaultAddress"; //NOI18N
           newAttr = new Attribute(DEBUG_OPTIONS, value);
        }else{
            if((getAddressValue().equals("1044")) || (getAddressValue().equals("9009"))) {//NOI18N
                newAttr = new Attribute(DEBUG_OPTIONS, DEF_DEUG_OPTIONS_SOCKET);
            }
        }    
        try{
            if(newAttr != null){
                this.setAttribute(newAttr);
            }
        }catch(Exception ex){}    
    }

}
