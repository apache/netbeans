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
package org.netbeans.modules.j2ee.sun.api.restricted;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;
import org.netbeans.modules.j2ee.sun.api.ServerInterface;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;

/**
 *
 * @author Nitya Doraisamy
 */
public class RegistrationUtils {

    private static String POOL_EXTENSION = "_Base";
    private static String DELETE_POOL = "deleteJdbcConnectionPool";
    private static String DELETE_JDBC = "deleteJdbcResource";
    private static String DELETE_MAIL = "deleteMailResource";
    private static String DELETE_CONNECTOR = "deleteConnectorResource";
    private static String DELETE_CONNECTORPOOL = "deleteConnectorConnectionPool";
    private static String DELETE_ADMINOBJECT = "deleteAdminObjectResource";
    private static String DAS_SERVER_NAME = "server";
    
    private static final Logger LOG = Logger.getLogger(RegistrationUtils.class.getName());
    
    public RegistrationUtils() {
    }
    
    public static void checkUpdateServerResources(ServerInterface mejb, java.io.File primaryFile) {
        Resources resources = ResourceUtils.getResourcesGraph(primaryFile);
        updateJDBCResourcesAndPools(resources, mejb);    
        updateMailResources(resources, mejb);    
        updateJMSResources(resources, mejb);    
    }

    private static void updateJDBCResourcesAndPools(Resources resources, ServerInterface mejb){
        HashMap serverPools = getServerConnectionPools(mejb, WizardConstants.__GetJdbcConnectionPool);
        HashMap serverDatasources = getServerResources(mejb, WizardConstants.__GetJdbcResource);
        
        JdbcConnectionPool[] pools = resources.getJdbcConnectionPool();
        JdbcResource[] dataSources = resources.getJdbcResource();
        
        //Delete datasources that are in this project
        HashMap dupJdbcResources = getProjectDatasources(serverDatasources, dataSources);
        deleteServerResources(dupJdbcResources, mejb, DELETE_JDBC);
        
        for(int i=0; i<pools.length; i++){           
            JdbcConnectionPool connectionPoolBean = pools[i];
            String newPoolName = connectionPoolBean.getName();
            
            //Is this pool registered on the server.
            if(serverPools.containsKey(newPoolName)){
                HashMap serverJdbcResources = getReferringResources(newPoolName, serverDatasources, mejb);               
                if(serverJdbcResources.size() > 0){
                    //Change this connectionPoolName
                    copyServerPool(serverPools, newPoolName, WizardConstants.__CreateCP, mejb);
                    updateExternalResource(serverJdbcResources, newPoolName, mejb);
                }
                deleteOldServerPool(newPoolName, DELETE_POOL, mejb);
            }else{
                //delete pool.
                deleteOldServerPool(newPoolName, DELETE_POOL, mejb);
            }
        }
    }
    
    private static void updateMailResources(Resources resources, ServerInterface mejb){
        HashMap serverMailResources = getServerResources(mejb, WizardConstants.__GetMailResource);
        MailResource[] mails = resources.getMailResource();
        //Delete mail resources that are in this project
        HashMap dupMailResources = getProjectMailResources(serverMailResources, mails);
        deleteServerResources(dupMailResources, mejb, DELETE_MAIL);
    }
    
    private static void updateJMSResources(Resources resources, ServerInterface mejb){
        updateAdminObjects(resources, mejb);
        updateConnectorsAndPools(resources, mejb);
    }
    
    private static void updateAdminObjects(Resources resources, ServerInterface mejb){
        HashMap serverAdminObjects = getServerResources(mejb, WizardConstants.__GetAdmObjResource);
        AdminObjectResource[] adminObjects = resources.getAdminObjectResource();
        //Delete adminObjects resources that are in this project
        HashMap dupAdminObjects = getProjectAdminObjects(serverAdminObjects, adminObjects);
        deleteServerResources(dupAdminObjects, mejb, DELETE_ADMINOBJECT);
    }
    
    private static void updateConnectorsAndPools(Resources resources, ServerInterface mejb){
        HashMap serverConnectorPools = getServerConnectionPools(mejb, WizardConstants.__GetConnPoolResource);
        HashMap serverConnectors = getServerResources(mejb, WizardConstants.__GetConnectorResource);
        
        ConnectorConnectionPool[] connectorPools = resources.getConnectorConnectionPool();
        ConnectorResource[] connectors = resources.getConnectorResource();
        
        //Delete connectors that are in this project
        HashMap dupConnectors = getProjectConnectors(serverConnectors, connectors);
        deleteServerResources(dupConnectors, mejb, DELETE_CONNECTOR);
        
        for(int i=0; i<connectorPools.length; i++){           
            ConnectorConnectionPool connectorPoolBean = connectorPools[i];
            String newPoolName = connectorPoolBean.getName();
            
            //Is this pool registered on the server.
            if(serverConnectorPools.containsKey(newPoolName)){
                HashMap serverConnectorResources = getReferringResources(newPoolName, serverConnectors, mejb);               
                if(serverConnectorResources.size() > 0){
                    //Change this connectorPoolName
                    copyServerPool(serverConnectorPools, newPoolName, WizardConstants.__CreateConnPool, mejb);
                    updateExternalResource(serverConnectorResources, newPoolName, mejb);
                }
                deleteOldServerPool(newPoolName, DELETE_CONNECTORPOOL, mejb);
            }else{
                //delete pool.
                deleteOldServerPool(newPoolName, DELETE_CONNECTORPOOL, mejb);
            }
        }
    }
    
    public static HashMap getServerConnectionPools(ServerInterface mejb, String operationName){
        HashMap<String, ObjectName> pools = new HashMap<String, ObjectName>();
        try {
            ObjectName configObjName = new ObjectName(WizardConstants.MAP_RESOURCES);
            ObjectName[] resourceObjects = (ObjectName[])  mejb.invoke(configObjName, operationName, null, null);
            for(int i=0; i<resourceObjects.length; i++){
                ObjectName objName = resourceObjects[i];
                String poolName = (String)mejb.getAttribute(objName, "name"); //NOI18N
                pools.put(poolName, objName);
            } // for - each connection pool
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Unable to get server connection pools", ex);
        }
        return pools;
    }
    
    public static HashMap getReferringResources(String poolName, HashMap serverResources, ServerInterface mejb) {
        HashMap<String, ObjectName> referringResources = new HashMap<String, ObjectName>();
        try{
            for(Iterator itr=serverResources.keySet().iterator(); itr.hasNext();){
                String resourceName = (String)itr.next();
                ObjectName objName = (ObjectName)serverResources.get(resourceName);
                
                String connpoolName = (String)mejb.getAttribute(objName, "pool-name"); //NOI18N
                if(connpoolName.equals(poolName)){
                    referringResources.put(resourceName, objName);
                }
            }
        }catch(Exception ex){
            LOG.log(Level.SEVERE, "Unable to get referring resources", ex);
        }
        return referringResources;
    }

    public static HashMap getProjectDatasources(HashMap serverJdbcResources, JdbcResource[] dataSources){
        HashMap<String, ObjectName> datasources = new HashMap<String, ObjectName>();
        for(int i=0; i<dataSources.length; i++){
            JdbcResource dsResource = dataSources[i];
            String dsName = dsResource.getJndiName();
            if(serverJdbcResources.containsKey(dsName)){
                datasources.put(dsName, (ObjectName)serverJdbcResources.get(dsName));
            }
        }
        return datasources;
    }
    
    public static HashMap getProjectMailResources(HashMap serverMailResources, MailResource[] mails){
        HashMap<String, ObjectName> mailResources = new HashMap<String, ObjectName>();
        for(int i=0; i<mails.length; i++){
            MailResource mailResource = mails[i];
            String mailName = mailResource.getJndiName();
            if(serverMailResources.containsKey(mailName)){
                mailResources.put(mailName, (ObjectName)serverMailResources.get(mailName));
            }
        }
        return mailResources;
    }
    
    public static HashMap getProjectAdminObjects(HashMap serverAdminObjects, AdminObjectResource[] adminObjects){
        HashMap<String, ObjectName> adminObjectResources = new HashMap<String, ObjectName>();
        for(int i=0; i<adminObjects.length; i++){
            AdminObjectResource aoResource = adminObjects[i];
            String jndiName = aoResource.getJndiName();
            if(serverAdminObjects.containsKey(jndiName)){
                adminObjectResources.put(jndiName, (ObjectName)serverAdminObjects.get(jndiName));
            }
        }
        return adminObjectResources;
    }
    
    public static HashMap getProjectConnectors(HashMap serverConnectors, ConnectorResource[] connectors){
        HashMap<String, ObjectName> connectorResources = new HashMap<String, ObjectName>();
        for(int i=0; i<connectors.length; i++){
            ConnectorResource connResource = connectors[i];
            String jndiName = connResource.getJndiName();
            if(serverConnectors.containsKey(jndiName)){
                connectorResources.put(jndiName, (ObjectName)serverConnectors.get(jndiName));
            }
        }
        return connectorResources;
    }
    
    public static void updateExternalResource(HashMap serverResources, String newPoolName, ServerInterface mejb){
        try{
            String updatedPoolName = newPoolName + POOL_EXTENSION;
            for(Iterator itr=serverResources.values().iterator(); itr.hasNext();){
                ObjectName dsObjName = (ObjectName)itr.next();
                Attribute poolNameAttr = new Attribute("pool-name", updatedPoolName);
                mejb.setAttribute(dsObjName, poolNameAttr);
            }
        }catch(Exception ex){
            //Could not update resource. 
            System.out.println(ex.getLocalizedMessage());
        }
    }
    
    
    
    public static void copyServerPool(HashMap serverPools, String newPoolName, String operationName, ServerInterface mejb){
        try{
            String updatedPoolName = newPoolName + POOL_EXTENSION;
            if(! serverPools.containsKey(updatedPoolName)){
                ObjectName serverPoolObj = (ObjectName)serverPools.get(newPoolName);
                Map attributeInfos = ResourceUtils.getResourceAttributeNames(serverPoolObj, mejb);
                attributeInfos.remove("name");
                String[] attrNames = (String[]) attributeInfos.keySet().toArray(new String[attributeInfos.size()]);
                AttributeList attrList = mejb.getAttributes(serverPoolObj, attrNames);
                Attribute nameAttr = new Attribute("name", updatedPoolName);
                attrList.add(nameAttr);
                          
                Properties props = new Properties();
                AttributeList propsList = (AttributeList)mejb.invoke(serverPoolObj, WizardConstants.__GetProperties, null, null);             
                for(int i=0; i<propsList.size(); i++){
                    Attribute propAttr = (Attribute)propsList.get(i);
                    String propName = propAttr.getName();
                    Object propValue = propAttr.getValue();
                    if(propValue != null){
                        props.put(propName, propValue);
                    }    
                }
                
                Object[] params = new Object[]{attrList, props, null};
                ResourceUtils.createResource(operationName, params, mejb);
            }
        }catch(Exception ex){  
            //Unable to copy pool
            System.out.println(ex.getLocalizedMessage());
        }
    }
    
    public static void deleteOldServerPool(String newPoolName, String operationName, ServerInterface mejb){
        try{
            ObjectName objName = new ObjectName(WizardConstants.MAP_RESOURCES);
            mejb.invoke(objName, operationName, new Object[]{newPoolName, DAS_SERVER_NAME},
                    new String[]{"java.lang.String", "java.lang.String"} );
        }catch(Exception ex){
            LOG.log(Level.SEVERE, "Unable to clean up existing duplicate pools", ex);
         }
    }
    
    public static void deleteServerResources(HashMap serverResources, ServerInterface mejb, String operationName){
        try{
            ObjectName objName = new ObjectName(WizardConstants.MAP_RESOURCES);
            for(Iterator itr = serverResources.keySet().iterator(); itr.hasNext();){
                String jdbcName = (String)itr.next();
                mejb.invoke(objName, operationName, new Object[]{jdbcName, DAS_SERVER_NAME},
                        new String[]{"java.lang.String", "java.lang.String"} );
            }
        }catch(Exception ex){
            LOG.log(Level.SEVERE, "Unable to clean up existing duplicate datasources", ex);
        }
    }
    
    public static HashMap getServerResources(ServerInterface mejb, String operationName){
        HashMap<String, ObjectName> serverResources = new HashMap<String, ObjectName>();
        try {
            ObjectName configObjName = new ObjectName(WizardConstants.MAP_RESOURCES);
            ObjectName[] resourceObjects = (ObjectName[]) mejb.invoke(configObjName, operationName, null, null);
            for(int i=0; i<resourceObjects.length; i++){
                ObjectName objName = resourceObjects[i];
                String jndiName = (String)mejb.getAttribute(objName, "jndi-name"); //NOI18N
                serverResources.put(jndiName, objName);
            } // for 
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Unable to get server datasources", ex);
        }
        return serverResources;
    }
    
}
