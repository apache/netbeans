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
 * ResourceUtils.java
 *
 * Created on September 17, 2003, 11:54 AM
 */

package org.netbeans.modules.j2ee.sun.api.restricted;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;
import org.netbeans.modules.glassfish.tooling.utils.OsUtils;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.sun.api.ServerInterface;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.JmsResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.DataSourceBean;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.JMSBean;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.JavaMailSessionBean;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.PersistenceManagerBean;
import org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData;
import org.netbeans.modules.j2ee.sun.sunresources.beans.DatabaseUtils;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import static org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants.__ConnPoolSuffixJMS;
import static org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants.__JndiName;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;

/*
 *
 * @author  nityad
 */

public class ResourceUtils implements WizardConstants{

    static final ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.sunresources.beans.Bundle");// NOI18N
    static final String[] sysDatasources = {"jdbc/__TimerPool", "jdbc/__CallFlowPool"}; //NOI18N
    static final String[] sysConnpools = {"__CallFlowPool", "__TimerPool"}; //NOI18N
    static final String SAMPLE_DATASOURCE = "jdbc/sample";
    static final String SAMPLE_CONNPOOL = "SamplePool";

    /** Logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(ResourceUtils.class.getName());

    /** Name of web application configuration directory.
      * Duplicates {@code org.netbeans.modules.glassfish.eecommon.api.config.JavaEEModule} field.*/
    public static final String WEB_INF = "WEB-INF";

    /** Name of java archive manifest directory.
      * Duplicates {@code org.netbeans.modules.glassfish.eecommon.api.config.JavaEEModule} field.*/
    public static final String META_INF = "META-INF";

    /** GlassFish resource file suffix is {@code .xml}.
      * Duplicates {@code org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration} field.*/
    private static final String RESOURCE_FILES_SUFFIX = ".xml";

    /** List of base file names containing server resources:<ul>
      * <li><i>[0]</i> points to current name used since GlassFich v3.</li>
      * <li><i>[1]</i> points to old name used before GlassFich v3.</li>
      * <ul>
      * Duplicates {@code org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration} field.*/
    static final String[] RESOURCE_FILES = {
        "glassfish-resources" + RESOURCE_FILES_SUFFIX,
        "sun-resources" + RESOURCE_FILES_SUFFIX
    };

    //FIXME: should not the constructor be private? (all methods are static)
    /** Creates a new instance of ResourceUtils */
    public ResourceUtils() {
    }

    public static void saveNodeToXml(FileObject resFile, Resources res){
        try {
            res.write(FileUtil.toFile(resFile));
        }catch(Exception ex){
            LOGGER.log(Level.SEVERE, "saveNodeToXml failed", ex);
        }
    }

    public static void register(Resources resource, SunDeploymentManagerInterface sunDm, boolean update, String resType) throws Exception {
        if(sunDm.isRunning()){
            ServerInterface mejb = sunDm.getManagement();
            if(resType.equals(__JdbcConnectionPool)){
                register(resource.getJdbcConnectionPool(0), mejb, update);
            }else if(resType.equals(__JdbcResource)){
                register(resource.getJdbcResource(0), mejb, update);
            }else if(resType.equals(__PersistenceManagerFactoryResource)){
                register(resource.getPersistenceManagerFactoryResource(0), mejb, update);
            }else if(resType.equals(__MailResource)){
                register(resource.getMailResource(0), mejb, update);
            }else if(resType.equals(__JmsResource)){
                if(resource.getAdminObjectResource().length != 0){
                    register(resource.getAdminObjectResource(0), mejb, update);
                }else{
                    if(resource.getConnectorResource().length != 0 && resource.getConnectorConnectionPool().length != 0) {
                        register(resource.getConnectorConnectionPool(0), mejb, update);
                        register(resource.getConnectorResource(0), mejb, update);
                    }
                }
            }
        }else{
            throw new Exception(bundle.getString("Err_RegResServerStopped")); //NOI18N
        }
    }

    public static void register(JdbcConnectionPool resource, ServerInterface mejb, boolean update) throws Exception{
        AttributeList attrList = ResourceUtils.getResourceAttributes(resource, mejb);
        PropertyElement[] props = resource.getPropertyElement();
        Properties propsList = getProperties(props);
        Object[] params = new Object[]{attrList, propsList, null};
        String resourceName = resource.getName();
        if(!isResourceUpdated(resourceName, mejb, attrList, propsList, __GetJdbcConnectionPool)){
            createResource(__CreateCP, params, mejb);
        }
    }

    public static void register(JdbcResource resource, ServerInterface mejb, boolean update) throws Exception{
        AttributeList attrList = ResourceUtils.getResourceAttributes(resource);
        PropertyElement[] props = resource.getPropertyElement();
        Properties propsList = getProperties(props);
        Object[] params = new Object[]{attrList, propsList, null};
        String resourceName = resource.getJndiName();
        if(!isResourceUpdated(resourceName, mejb, attrList, propsList, __GetJdbcResource)){
            createResource(__CreateDS, params, mejb);
        }
    }

     public static void register(PersistenceManagerFactoryResource resource, ServerInterface mejb, boolean update) throws Exception{
         AttributeList attrList = ResourceUtils.getResourceAttributes(resource);
         PropertyElement[] props = resource.getPropertyElement();
         Properties propsList = getProperties(props);
         Object[] params = new Object[]{attrList, propsList, null};
         String resourceName = resource.getJndiName();
         if(!isResourceUpdated(resourceName, mejb, attrList, propsList, __GetPMFResource)){
             createResource(__CreatePMF, params, mejb);
         }
     }

     public static void register(AdminObjectResource resource, ServerInterface mejb, boolean update) throws Exception{
         AttributeList attrList = ResourceUtils.getResourceAttributes(resource);
         PropertyElement[] props = resource.getPropertyElement();
         Properties propsList = getProperties(props);
         Object[] params = new Object[]{attrList, propsList, null};
         String resourceName = resource.getJndiName();
         if(!isResourceUpdated(resourceName, mejb, attrList, propsList, __GetAdmObjResource)){
             createResource(__CreateAdmObj, params, mejb);
         }
     }

     public static void register(ConnectorResource resource, ServerInterface mejb, boolean update) throws Exception{
         AttributeList attrList = ResourceUtils.getResourceAttributes(resource);
         Properties propsList = new Properties();
         Object[] params = new Object[]{attrList, propsList, null};
         String resourceName = resource.getJndiName();
         if(!isResourceUpdated(resourceName, mejb, attrList, propsList, __GetConnectorResource)){
             createResource(__CreateConnector, params, mejb);
         }
     }

     public static void register(ConnectorConnectionPool resource, ServerInterface mejb, boolean update) throws Exception{
         AttributeList attrList = ResourceUtils.getResourceAttributes(resource);
         PropertyElement[] props = resource.getPropertyElement();
         Properties propsList = getProperties(props);
         Object[] params = new Object[]{attrList, propsList, null};
         String resourceName = resource.getName();
         if(!isResourceUpdated(resourceName, mejb, attrList, propsList, __GetConnPoolResource)){
             createResource(__CreateConnPool, params, mejb);
         }
     }

     public static void register(MailResource resource, ServerInterface mejb, boolean update) throws Exception{
         AttributeList attrList = ResourceUtils.getResourceAttributes(resource);
         PropertyElement[] props = resource.getPropertyElement();
         Properties propsList = getProperties(props);
         Object[] params = new Object[]{attrList, propsList, null};
         String resourceName = resource.getJndiName();
         if(!isResourceUpdated(resourceName, mejb, attrList, propsList, __GetMailResource)){
             createResource(__CreateMail, params, mejb);
         }
     }

     public static void register(JmsResource resource, ServerInterface mejb, boolean update) throws Exception{
         AttributeList attrList = ResourceUtils.getResourceAttributes(resource);
         PropertyElement[] props = resource.getPropertyElement();
         Properties propsList = getProperties(props);
         Object[] params = new Object[]{attrList, propsList, null};
         String operName = NbBundle.getMessage(ResourceUtils.class, "CreateJMS"); //NOI18N
         String resourceName = resource.getJndiName();
         if(!isResourceUpdated(resourceName, mejb, attrList, propsList, __GetJmsResource)){
             createResource(operName, params, mejb);
         }
     }

     private static boolean isResourceUpdated(String resourceName, ServerInterface mejb, AttributeList attrList, Properties props, String operName ){
        boolean isResUpdated = false;
        try{
            ObjectName objName = new ObjectName(MAP_RESOURCES);
            ObjectName[] resourceObjects = null;
            if(operName.equals(__GetPMFResource) || operName.equals(__GetJmsResource)){
                String[] signature = new String[]{"java.lang.String"};  //NOI18N
                Object[] params = new Object[]{null};
                resourceObjects = (ObjectName[])  mejb.invoke(objName, operName, params, signature);
            }else{
                resourceObjects = (ObjectName[])  mejb.invoke(objName, operName, null, null);
            }
            if(resourceObjects != null){
                ObjectName resOnServer = null;
                if(operName.equals(__GetJdbcConnectionPool) || operName.equals(__GetConnPoolResource))
                    resOnServer = getResourceDeployed(resourceObjects, resourceName, false);
                else
                    resOnServer = getResourceDeployed(resourceObjects, resourceName, true);
                if(resOnServer != null){
                    isResUpdated = true;
                    updateResourceAttributes(resOnServer, attrList, mejb);
                    updateResourceProperties(resOnServer, props, mejb);
                }
            }//Returned value is null for JMS.
        }catch(Exception ex){
            String errorMsg = MessageFormat.format(bundle.getString("Err_ResourceUpdate"), new Object[]{resourceName}); //NOI18N
            LOGGER.log(Level.SEVERE, errorMsg, ex);
        }
        return isResUpdated;
    }

    private static ObjectName getResourceDeployed(ObjectName[] resourceObjects, String resourceName, boolean useJndi){
        for(int i=0; i<resourceObjects.length; i++){
            ObjectName resObj = resourceObjects[i];
            String jndiName = null;
            if(useJndi)
                jndiName = resObj.getKeyProperty(__JndiName);
            else
                jndiName = resObj.getKeyProperty(__Name);

            if(jndiName.equals(resourceName)){
                return resObj;
            }
        }
        return null;
    }

    public static void updateResourceAttributes(ObjectName objName, AttributeList attrList, ServerInterface mejb) throws Exception {
         try{
             Map attributeInfos = getResourceAttributeNames(objName, mejb);
             String[] attrNames = (String[]) attributeInfos.keySet().toArray(new String[attributeInfos.size()]);

             //Attributes from server
             AttributeList existAttrList = mejb.getAttributes(objName, attrNames);
             if (existAttrList != null) {
                 for (int i = 0; i < existAttrList.size(); i++) {
                     Attribute existAttr = (Attribute) existAttrList.get(i);
                     String existAttrName = existAttr.getName();
                     for (int j = 0; j < attrList.size(); j++) {
                         Attribute resAttr = (Attribute) attrList.get(j);
                         String resAttrName = resAttr.getName();
                         if (existAttrName.equals(resAttrName)) {
                             if (resAttr.getValue() == null && existAttr.getValue() != null) {
                                 mejb.setAttribute(objName, resAttr);
                             } else if (existAttr.getValue() == null) { //NOI18N
                                 if ((resAttr.getValue() != null) && (!resAttr.getValue().toString().equals(""))) {
                                     mejb.setAttribute(objName, resAttr);
                                 }
                             } else {
                                 if (!resAttr.getValue().toString().equals(existAttr.getValue().toString())) {
                                     mejb.setAttribute(objName, resAttr);
                                 }
                             }
                         }//if
                     }//loop through project's resource Attributes
                 }
             }
         }catch(Exception ex){
             throw new Exception(ex.getLocalizedMessage(), ex);
         }
     }
    public static void updateResourceProperties(ObjectName objName, Properties props, ServerInterface mejb) throws Exception {
         try{
             String[] signature = new String[]{"javax.management.Attribute"};  //NOI18N
             Object[] params = null;
             //Get Extra Properties From Server
             AttributeList attrList = (AttributeList)mejb.invoke(objName, __GetProperties, null, null);
             for(int i=0; i<attrList.size(); i++){
                 Attribute oldAttr = (Attribute)attrList.get(i);
                 String oldAttrName = oldAttr.getName();
                 if(props.containsKey(oldAttrName)){
                     if(oldAttr.getValue() != null){
                         String oldAttrValue = oldAttr.getValue().toString();
                         if(! props.getProperty(oldAttrName).equals(oldAttrValue)){
                             Attribute attr = new Attribute(oldAttrName, props.getProperty(oldAttrName));
                             params = new Object[]{attr};
                             mejb.invoke(objName, __SetProperty, params, signature);
                         }
                     }else{//Server extra property value not null
                         if(props.getProperty(oldAttrName) != null){
                             Attribute attr = new Attribute(oldAttrName, props.getProperty(oldAttrName));
                             params = new Object[]{attr};
                             mejb.invoke(objName, __SetProperty, params, signature);
                         }
                     }
                 }else{
                     //Modifies extra properties does not contain this property
                     //Remove from server resource
                     Attribute removeAttr = new Attribute(oldAttrName, null);
                     params = new Object[]{removeAttr};
                     mejb.invoke(objName, __SetProperty, params, signature);
                 }
             }//loop through server extra properties
             addNewExtraProperties(objName, props, attrList, mejb);
         }catch(Exception ex){
             throw new Exception(ex.getLocalizedMessage(), ex);
         }
     }

     public static Map getResourceAttributeNames(ObjectName objName, ServerInterface mejb) throws Exception {
         try{
             Map attributeInfos = new java.util.HashMap();
             javax.management.MBeanInfo info = mejb.getMBeanInfo(objName);
             if (info != null) {
                 javax.management.MBeanAttributeInfo[] attrs = info.getAttributes();
                 for (int i = 0; i < attrs.length; i++) {
                     if (attrs[i] != null) {
                         attributeInfos.put(attrs[i].getName(), attrs[i]);
                     }
                 }
             }
             return attributeInfos;
         }catch(Exception ex){
             throw new Exception(ex.getLocalizedMessage(), ex);
         }
     }

     private static void addNewExtraProperties(ObjectName objName, Properties props, AttributeList attrList, ServerInterface mejb) throws Exception {
         try{
             String[] signature = new String[]{"javax.management.Attribute"};  //NOI18N
             Object[] params = null;
             if(props.size() > attrList.size()){
                 java.util.Enumeration listProps = props.propertyNames();
                 while(listProps.hasMoreElements()){
                     String propName = listProps.nextElement().toString();
                     if(! attrList.contains(propName)){
                         Attribute attr = new Attribute(propName, props.getProperty(propName));
                         params = new Object[]{attr};
                         mejb.invoke(objName, __SetProperty, params, signature);
                     }
                 }//while
             }
         }catch(Exception ex){
             throw new Exception(ex.getLocalizedMessage(), ex);
         }
     }

     public static void createResource(String operName, Object[] params, ServerInterface mejb) throws Exception{
        try{
            ObjectName objName = new ObjectName(MAP_RESOURCES);
            String[] signature = new String[]{"javax.management.AttributeList", "java.util.Properties", "java.lang.String"};  //NOI18N
            mejb.invoke(objName, operName, params, signature);
        }catch(Exception ex){
            throw new Exception(ex.getLocalizedMessage(), ex);
        }
    }

    public static AttributeList getResourceAttributes(JdbcConnectionPool connPool, ServerInterface mejb){
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute(__Name, connPool.getName()));
        attrs.add(new Attribute(__DatasourceClassname, connPool.getDatasourceClassname()));
        attrs.add(new Attribute(__ResType, connPool.getResType()));
        attrs.add(new Attribute(__SteadyPoolSize, connPool.getSteadyPoolSize()));
        attrs.add(new Attribute(__MaxPoolSize, connPool.getMaxPoolSize()));
        attrs.add(new Attribute(__MaxWaitTimeInMillis, connPool.getMaxWaitTimeInMillis()));
        attrs.add(new Attribute(__PoolResizeQuantity, connPool.getPoolResizeQuantity()));
        attrs.add(new Attribute(__IdleTimeoutInSeconds, connPool.getIdleTimeoutInSeconds()));
        String isolation = connPool.getTransactionIsolationLevel();
        String defaultChoice = ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/editors/Bundle").getString("LBL_driver_default");     //NOI18N
        if (isolation != null && (isolation.length() == 0 || isolation.equals(defaultChoice)) ){
            isolation = null;
        }
        attrs.add(new Attribute(__TransactionIsolationLevel, isolation));
        attrs.add(new Attribute(__IsIsolationLevelGuaranteed, connPool.getIsIsolationLevelGuaranteed()));
        attrs.add(new Attribute(__IsConnectionValidationRequired, connPool.getIsConnectionValidationRequired()));
        attrs.add(new Attribute(__ConnectionValidationMethod, connPool.getConnectionValidationMethod()));
        attrs.add(new Attribute(__ValidationTableName, connPool.getValidationTableName()));
        attrs.add(new Attribute(__FailAllConnections, connPool.getFailAllConnections()));
        attrs.add(new Attribute(__Description, connPool.getDescription()));

        if(is90Server(mejb)){
            attrs.add(new Attribute(__NonTransactionalConnections, connPool.getNonTransactionalConnections()));
            attrs.add(new Attribute(__AllowNonComponentCallers, connPool.getAllowNonComponentCallers()));
        }
        return attrs;
    }

    public static AttributeList getResourceAttributes(JdbcResource jdbcResource){
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute(__JndiName, jdbcResource.getJndiName()));
        attrs.add(new Attribute(__PoolName, jdbcResource.getPoolName()));
        attrs.add(new Attribute(__JdbcObjectType, jdbcResource.getObjectType()));
        attrs.add(new Attribute(__Enabled, jdbcResource.getEnabled()));
        attrs.add(new Attribute(__Description, jdbcResource.getDescription()));
        return attrs;
    }

    public static AttributeList getResourceAttributes(PersistenceManagerFactoryResource pmResource){
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute(__JndiName, pmResource.getJndiName()));
        attrs.add(new Attribute(__FactoryClass, pmResource.getFactoryClass()));
        attrs.add(new Attribute(__JdbcResourceJndiName, pmResource.getJdbcResourceJndiName()));
        attrs.add(new Attribute(__Enabled, pmResource.getEnabled()));
        attrs.add(new Attribute(__Description, pmResource.getDescription()));
        return attrs;
    }

    public static AttributeList getResourceAttributes(AdminObjectResource aoResource){
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute(__JndiName, aoResource.getJndiName()));
        attrs.add(new Attribute(__Description, aoResource.getDescription()));
        attrs.add(new Attribute(__Enabled, aoResource.getEnabled()));
        attrs.add(new Attribute(__JavaMessageResType, aoResource.getResType()));
        attrs.add(new Attribute(__AdminObjResAdapterName, aoResource.getResAdapter()));
        return attrs;
    }

    public static AttributeList getResourceAttributes(ConnectorResource connResource){
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute(__JndiName, connResource.getJndiName()));
        attrs.add(new Attribute(__PoolName, connResource.getPoolName()));
        attrs.add(new Attribute(__Description, connResource.getDescription()));
        attrs.add(new Attribute(__Enabled, connResource.getEnabled()));
        return attrs;
    }

    public static AttributeList getResourceAttributes(ConnectorConnectionPool connPoolResource){
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute(__Name, connPoolResource.getName()));
        attrs.add(new Attribute(__ConnectorPoolResAdName, connPoolResource.getResourceAdapterName()));
        attrs.add(new Attribute(__ConnectorPoolConnDefName, connPoolResource.getConnectionDefinitionName()));
        return attrs;
    }

    public static AttributeList getResourceAttributes(MailResource mailResource){
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute(__JndiName, mailResource.getJndiName()));
        attrs.add(new Attribute(__StoreProtocol, mailResource.getStoreProtocol()));
        attrs.add(new Attribute(__StoreProtocolClass, mailResource.getStoreProtocolClass()));
        attrs.add(new Attribute(__TransportProtocol, mailResource.getTransportProtocol()));
        attrs.add(new Attribute(__TransportProtocolClass, mailResource.getTransportProtocolClass()));
        attrs.add(new Attribute(__Host, mailResource.getHost()));
        attrs.add(new Attribute(__MailUser, mailResource.getUser()));
        attrs.add(new Attribute(__From, mailResource.getFrom()));
        attrs.add(new Attribute(__Debug, mailResource.getDebug()));
        attrs.add(new Attribute(__Enabled, mailResource.getEnabled()));
        attrs.add(new Attribute(__Description, mailResource.getDescription()));
        return attrs;
    }

    public static AttributeList getResourceAttributes(JmsResource jmsResource){
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute(__JavaMessageJndiName, jmsResource.getJndiName()));
        attrs.add(new Attribute(__JavaMessageResType, jmsResource.getResType()));
        attrs.add(new Attribute(__Enabled, jmsResource.getEnabled()));
        attrs.add(new Attribute(__Description, jmsResource.getDescription()));
        return attrs;
    }

    private static Properties getProperties(PropertyElement[] props) throws Exception {
        Properties propList = new Properties();
        for(int i=0; i<props.length; i++){
            String name = props[i].getName();
            String value = props[i].getValue();
            if(value != null && value.trim().length() != 0){
                propList.put(name, value);
            }
        }
        return propList;
    }

    public static void saveConnPoolDatatoXml(ResourceConfigData data,String baseName) {
        Resources res = getServerResourcesGraph(data.getTargetFileObject(),
                            baseName.contains("glassfish-resources") ? Resources.VERSION_1_5 : Resources.VERSION_1_3);
        saveConnPoolDatatoXml(data, res,baseName);
    }

    public static void saveConnPoolDatatoXml(ResourceConfigData data, Resources res,String baseName) {
        try{
            JdbcConnectionPool connPool = res.newJdbcConnectionPool();

            String[] keys = data.getFieldNames();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (key.equals(__Properties)){
                    Vector props = (Vector)data.getProperties();
                    for (int j = 0; j < props.size(); j++) {
                        NameValuePair pair = (NameValuePair)props.elementAt(j);
                        PropertyElement prop = connPool.newPropertyElement();
                        prop = populatePropertyElement(prop, pair);
                        connPool.addPropertyElement(prop);
                    }
                }else{
                    String value = data.getString(key);
                    if (key.equals(__Name)){
                        connPool.setName(value);
                        data.setTargetFile(value);
                    }else if (key.equals(__DatasourceClassname))
                        connPool.setDatasourceClassname(value);
                    else if (key.equals(__ResType))
                        connPool.setResType(value);
                    else if (key.equals(__SteadyPoolSize))
                        connPool.setSteadyPoolSize(value);
                    else if (key.equals(__MaxPoolSize))
                        connPool.setMaxPoolSize(value);
                    else if (key.equals(__MaxWaitTimeInMillis))
                        connPool.setMaxWaitTimeInMillis(value);
                    else if (key.equals(__PoolResizeQuantity))
                        connPool.setPoolResizeQuantity(value);
                    else if (key.equals(__IdleTimeoutInSeconds))
                        connPool.setIdleTimeoutInSeconds(value);
                    else if (key.equals(__TransactionIsolationLevel)){
                        String defaultChoice = ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/editors/Bundle").getString("LBL_driver_default");     //NOI18N
                        if (value.equals(defaultChoice)){
                            value = null;
                        }
                        connPool.setTransactionIsolationLevel(value);
                    }else if (key.equals(__IsIsolationLevelGuaranteed))
                        connPool.setIsIsolationLevelGuaranteed(value);
                    else if (key.equals(__IsConnectionValidationRequired))
                        connPool.setIsConnectionValidationRequired(value);
                    else if (key.equals(__ConnectionValidationMethod))
                        connPool.setConnectionValidationMethod(value);
                    else if (key.equals(__ValidationTableName))
                        connPool.setValidationTableName(value);
                    else if (key.equals(__FailAllConnections))
                        connPool.setFailAllConnections(value);
                    else if (key.equals(__Description))
                        connPool.setDescription(value);
                    else if (key.equals(__NonTransactionalConnections))
                        connPool.setNonTransactionalConnections(value);
                    else if (key.equals(__AllowNonComponentCallers))
                        connPool.setAllowNonComponentCallers(value);
                }

            } //for
            res.addJdbcConnectionPool(connPool);
            createFile(data, res,baseName);
        }catch(Exception ex){
            LOGGER.log(Level.SEVERE, "Unable to saveConnPoolDatatoXml", ex);
        }
    }

    /**
     * Create directory structure for given file if not exists and return it as
     * {@link File} instance
     * <p/>.
     * @param resourceFile File to check for existence of full path.
     * @return Parent directory (including full path) of given file or {@code null}
     *         if such a path could not be found.
     */
    public static File createPathForFile(final File resourceFile) {
        final File resourceFileDir = resourceFile.getParentFile();
        // Create directory structure if missing.
        if (resourceFileDir != null) {
            if (!resourceFileDir.exists()) {
                resourceFileDir.mkdirs();
            }
        }
        return resourceFileDir;
    }

    /**
     * Get Java EE module provider for provided server resources file target folder.
     * <p/>
     * @param targetFolder Server resources file target folder.
     * @return Java EE module provider found or {@code null} when target folder has
     *         no owning project or no Java EE module provider was found.
     */
    public static J2eeModuleProvider getJavaEEModuleProvider(final FileObject targetFolder) {
        final Project holdingProj = FileOwnerQuery.getOwner(targetFolder);
        return holdingProj != null ?
                (J2eeModuleProvider)holdingProj.getLookup().lookup(J2eeModuleProvider.class)
                : null;
    }

    /**
     * Retrieve {@link GlassFishServer} instance from Java EE platform stored in given
     * Java EE module provider.
     * <p/>
     * @param provider Java EE module provider.
     * @return {@link GlassFishServer} found or {@code null} when given {@code provider}
     *         is {@code null} or no {@link GlassFishServer} instance was stored in Java
     *         EE platform's lookup.
     */
    public static GlassFishServer getGlassFishServer(final J2eeModuleProvider provider) {
        final ServerInstance serverInstance = provider != null && provider.getServerInstanceID() != null
                ? Deployment.getDefault().getServerInstance(provider.getServerInstanceID())
                : null;
        J2eePlatform platform;
        try {
            platform = serverInstance != null
                    ? serverInstance.getJ2eePlatform()
                    : null;
        } catch (InstanceRemovedException ex) {
            platform = null;
            LOGGER.log(Level.INFO, "Could not get Java EE platform", ex);
        }
        return platform != null
                ? (GlassFishServer)platform.getLookup().lookup(GlassFishServer.class)
                : null;        
    }

   /**
     * Get Java EE module configuration directory (e.g. {@code "META-INF"}).
     * This duplicates more complex and better code from
     * {@code org.netbeans.modules.glassfish.eecommon.api.config.JavaEEModule}
     * but I had to implement it twice because of reverse module dependency.
     * <i>Internal helper method, do not call outside this module.</i>
     * <p/>
     * @param module Java EE module type.
     * @return Java EE module configuration directory.
     */
    public static final String getJavaEEModuleConfigDir(final J2eeModule module) {
        J2eeModule.Type type = module.getType();
        if (type.equals(J2eeModule.Type.WAR)) {
            return WEB_INF;
        } else {
            return META_INF;
        }
    }

    /**
     * Get resource file name depending on GlassFish server version.
     * This duplicates more complex and better code from
     * {@code org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration}
     * but I had to implement it twice because of reverse module dependency.
     * <i>Internal helper method, do not call outside this module.</i>
     * <p/>
     * @param version GlassFish server version.
     * @return Current {@code "glassfish-resources.xml"} file name since 3.1
     *         or {@code "sun-resources.xml"} otherwise.
     */
    public static final String getResourcesFileName(final GlassFishVersion version) {
        return GlassFishVersion.ge(version, GlassFishVersion.GF_3_1)
                ? RESOURCE_FILES[0] : RESOURCE_FILES[1];
    }

    /**
     * Create resource file path fragment for given Java EE module
     * (e.g. {@code "META-INF/glassfish-resources.xml"}).
     * This duplicates more complex and better code from
     * {@code org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration}
     * but I had to implement it twice because of reverse module dependency.
     * <i>Internal helper method, do not call outside this module.</i>
     * <p/>
     * @param module  Java EE module (project).
     * @param version GlassFish server version.
     * @return Resource file path fragment for given Java EE module.
     */
    public static final String getResourcesFileModulePath(final J2eeModule module, final GlassFishVersion version) {
        return OsUtils.joinPaths(getJavaEEModuleConfigDir(module), getResourcesFileName(version));
    }

    public static void saveJDBCResourceDatatoXml(ResourceConfigData dsData, ResourceConfigData cpData, String baseName) {
        try{
            Resources res = getServerResourcesGraph(dsData.getTargetFileObject(),
                    baseName.contains("glassfish-resources") ? Resources.VERSION_1_5 : Resources.VERSION_1_3);
            JdbcResource datasource = res.newJdbcResource();

            String[] keys = dsData.getFieldNames();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (key.equals(__Properties)){
                    Vector props = (Vector)dsData.getProperties();
                    for (int j = 0; j < props.size(); j++) {
                        NameValuePair pair = (NameValuePair)props.elementAt(j);
                        PropertyElement prop = datasource.newPropertyElement();
                        prop = populatePropertyElement(prop, pair);
                        datasource.addPropertyElement(prop);
                    }
                }else{
                    String value = dsData.getString(key);
                    if (key.equals(__JndiName)){
                        datasource.setJndiName(value);
                        dsData.setTargetFile(value);
                    }else if (key.equals(__PoolName))
                        datasource.setPoolName(value);
                    else if (key.equals(__JdbcObjectType))
                        datasource.setObjectType(value);
                    else if (key.equals(__Enabled))
                        datasource.setEnabled(value);
                    else if (key.equals(__Description))
                        datasource.setDescription(value);
                }

            } //for
            res.addJdbcResource(datasource);
            if(cpData != null){
                saveConnPoolDatatoXml(cpData, res,baseName);
            }
            createFile(dsData, res,baseName);
        }catch(Exception ex){
            LOGGER.log(Level.SEVERE, "Unable to saveJDBCResourceDatatoXml", ex);
        }
    }

    public static void savePMFResourceDatatoXml(ResourceConfigData pmfData, ResourceConfigData dsData, ResourceConfigData cpData,String baseName) {
        try{
            Resources res = getServerResourcesGraph(pmfData.getTargetFileObject(),
                    baseName.contains("glassfish-resources") ? Resources.VERSION_1_5 : Resources.VERSION_1_3);
            PersistenceManagerFactoryResource pmfresource = res.newPersistenceManagerFactoryResource();

            String[] keys = pmfData.getFieldNames();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (key.equals(__Properties)){
                    Vector props = (Vector)pmfData.getProperties();
                    for (int j = 0; j < props.size(); j++) {
                        NameValuePair pair = (NameValuePair)props.elementAt(j);
                        PropertyElement prop = pmfresource.newPropertyElement();
                        prop = populatePropertyElement(prop, pair);
                        pmfresource.addPropertyElement(prop);
                    }
                }else{
                    String value = pmfData.getString(key);
                    if (key.equals(__JndiName)){
                        pmfresource.setJndiName(value);
                        pmfData.setTargetFile(value);
                    }else if (key.equals(__FactoryClass))
                        pmfresource.setFactoryClass(value);
                    else if (key.equals(__JdbcResourceJndiName))
                        pmfresource.setJdbcResourceJndiName(value);
                    else if (key.equals(__Enabled))
                        pmfresource.setEnabled(value);
                    else if (key.equals(__Description))
                        pmfresource.setDescription(value);
                }

            } //for
            res.addPersistenceManagerFactoryResource(pmfresource);
            createFile(pmfData, res,baseName);

            if(dsData != null){
                saveJDBCResourceDatatoXml(dsData, cpData,baseName);
            }
        }catch(Exception ex){
            LOGGER.log(Level.SEVERE, "Unable to savePMFResourceDatatoXml", ex);
        }
    }

    public static void saveJMSResourceDatatoXml(ResourceConfigData jmsData,String baseName) {
        try{
            Resources res = getServerResourcesGraph(jmsData.getTargetFileObject(),
                    baseName.contains("glassfish-resources") ? Resources.VERSION_1_5 : Resources.VERSION_1_3);
            String type = jmsData.getString(__ResType);
            if(type.equals(__QUEUE) || type.equals(__TOPIC)){
                AdminObjectResource aoresource = res.newAdminObjectResource();
                aoresource.setDescription(jmsData.getString(__Description));
                aoresource.setEnabled(jmsData.getString(__Enabled));
                aoresource.setJndiName(jmsData.getString(__JndiName));
                aoresource.setResType(jmsData.getString(__ResType));
                aoresource.setResAdapter(__JmsResAdapter);
                Vector props = (Vector)jmsData.getProperties();
                for (int j = 0; j < props.size(); j++) {
                    NameValuePair pair = (NameValuePair)props.elementAt(j);
                    PropertyElement prop = aoresource.newPropertyElement();
                    prop = populatePropertyElement(prop, pair);
                    aoresource.addPropertyElement(prop);
                }

                res.addAdminObjectResource(aoresource);
            }else{
                ConnectorResource connresource = res.newConnectorResource();
                connresource.setDescription(jmsData.getString(__Description));
                connresource.setEnabled(jmsData.getString(__Enabled));
                connresource.setJndiName(jmsData.getString(__JndiName));
                connresource.setPoolName(jmsData.getString(__JndiName) + __ConnPoolSuffixJMS);

                ConnectorConnectionPool connpoolresource = res.newConnectorConnectionPool();
                connpoolresource.setName(jmsData.getString(__JndiName) + __ConnPoolSuffixJMS);
                connpoolresource.setConnectionDefinitionName(jmsData.getString(__ResType));
                connpoolresource.setResourceAdapterName(__JmsResAdapter);

                Vector props = (Vector)jmsData.getProperties();
                for (int j = 0; j < props.size(); j++) {
                    NameValuePair pair = (NameValuePair)props.elementAt(j);
                    PropertyElement prop = connpoolresource.newPropertyElement();
                    prop = populatePropertyElement(prop, pair);
                    connpoolresource.addPropertyElement(prop);
                }

                res.addConnectorResource(connresource);
                res.addConnectorConnectionPool(connpoolresource);
            }

            createFile(jmsData, res, baseName);
        }catch(Exception ex){
            LOGGER.log(Level.SEVERE, "Unable to saveJMSResourceDatatoXml", ex);
        }
    }

    public static void saveMailResourceDatatoXml(ResourceConfigData data, String baseName) {
        try{
            Vector vec = data.getProperties();
            Resources res = getServerResourcesGraph(data.getTargetFileObject(),
                    baseName.contains("glassfish-resources") ? Resources.VERSION_1_5 : Resources.VERSION_1_3);
            MailResource mlresource = res.newMailResource();

            String[] keys = data.getFieldNames();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (key.equals(__Properties)) {
                    Vector props = (Vector)data.getProperties();
                    for (int j = 0; j < props.size(); j++) {
                        NameValuePair pair = (NameValuePair)props.elementAt(j);
                        PropertyElement prop = mlresource.newPropertyElement();
                        prop = populatePropertyElement(prop, pair);
                        mlresource.addPropertyElement(prop);
                    }
                }else{
                    String value = data.getString(key);
                    if (key.equals(__JndiName)){
                        mlresource.setJndiName(value);
                        data.setTargetFile(value);
                    }else if (key.equals(__StoreProtocol))
                        mlresource.setStoreProtocol(value);
                    else if (key.equals(__StoreProtocolClass))
                        mlresource.setStoreProtocolClass(value);
                    else if (key.equals(__TransportProtocol))
                        mlresource.setTransportProtocol(value);
                    else if (key.equals(__TransportProtocolClass))
                        mlresource.setTransportProtocolClass(value);
                    else if (key.equals(__Host))
                        mlresource.setHost(value);
                    else if (key.equals(__MailUser))
                        mlresource.setUser(value);
                    else if (key.equals(__From))
                        mlresource.setFrom(value);
                    else if (key.equals(__Debug))
                        mlresource.setDebug(value);
                    else if (key.equals(__Description))
                        mlresource.setDescription(value);
                }
            } //for

            res.addMailResource(mlresource);
            createFile(data, res, baseName);
        }catch(Exception ex){
            LOGGER.log(Level.SEVERE, "Unable to saveMailResourceDatatoXml", ex);
        }
    }

    public static String createUniqueFileName(String in_targetName, FileObject fo, String defName){
        String targetName = in_targetName;
        if (targetName == null || targetName.length() == 0)
            targetName = defName;

        List resources = getProjectResources(fo, defName);
        if(resources.contains(targetName)){
            targetName = getUniqueResourceName(targetName, resources);
        }

        return targetName;
    }

    public static List getRegisteredConnectionPools(ResourceConfigData data){
        List connPools = new ArrayList();
        try {
            String keyProp = "name"; //NOI18N
            InstanceProperties instanceProperties = getTargetServer(data.getTargetFileObject());
            if(instanceProperties != null) {
                connPools = getResourceNames(instanceProperties, __GetJdbcConnectionPool, keyProp);
            }
            connPools.removeAll(Arrays.asList(sysConnpools));
            FileObject targetFolder = data.getTargetFileObject();
            List projectCP = getProjectResources(targetFolder, __ConnectionPoolResource);
            for(int i=0; i<projectCP.size(); i++){
                String localCP = projectCP.get(i).toString();
                if(! connPools.contains(localCP)) {
                    connPools.add(localCP);
                }
            }
        } catch (java.lang.NoClassDefFoundError ncdfe) {
            // this happens durring unit tests for the DataSourceWizard
            LOGGER.log(Level.SEVERE, "getRegisteredConnectionPools failed", ncdfe);
        }
        return connPools;
    }

    public static List getRegisteredJdbcResources(ResourceConfigData data){
        List dataSources = new ArrayList();
        try {
            String keyProp = "jndi-name"; //NOI18N
            InstanceProperties instanceProperties = getTargetServer(data.getTargetFileObject());
            if(instanceProperties != null)
                dataSources = getResourceNames(instanceProperties, __GetJdbcResource, keyProp);
            dataSources.removeAll(Arrays.asList(sysDatasources));
            FileObject targetFolder = data.getTargetFileObject();
            List projectDS = getProjectResources(targetFolder, __JDBCResource);
            for(int i=0; i<projectDS.size(); i++){
                String localDS = projectDS.get(i).toString();
                if(! dataSources.contains(localDS))
                    dataSources.add(localDS);
            }
        } catch (java.lang.NoClassDefFoundError ncdfe) {
            // this happens durring unit tests for the PMFWizard
            LOGGER.log(Level.SEVERE, "getRegisteredJdbcResources failed", ncdfe);
        }
        return dataSources;
    }

    private static List getResourceNames(InstanceProperties instProps, String query, String keyProperty){
        List retVal = new ArrayList();
        DeploymentManager dm = null;
        LOGGER.log(Level.INFO, "investigate", new Exception());
        if (dm instanceof SunDeploymentManagerInterface) {
            SunDeploymentManagerInterface eightDM = (SunDeploymentManagerInterface) dm;
            if (eightDM.isRunning()) {
                retVal = getResourceNames(eightDM, query, keyProperty);
            } else if (eightDM.isLocal()) {
                if (query.equals(__GetJdbcResource)) {
                    HashMap dsources = eightDM.getSunDatasourcesFromXml();
                    retVal = new ArrayList(dsources.keySet());
                } else if (query.equals(__GetJdbcConnectionPool)) {
                    HashMap pools = eightDM.getConnPoolsFromXml();
                    retVal = new ArrayList(pools.keySet());
                }
            }
        }
        return retVal;
    }

    private static List getResourceNames(SunDeploymentManagerInterface eightDM, String query, String keyProperty){
        List resList = new ArrayList();
        try{
            ServerInterface mejb = (ServerInterface)eightDM.getManagement();
            ObjectName objName = new ObjectName(MAP_RESOURCES);
            ObjectName[] beans = (ObjectName[])mejb.invoke(objName, query, null, null);
            for(int i=0; i<beans.length; i++){
                String resName = ((ObjectName)beans[i]).getKeyProperty(keyProperty);
                resList.add(resName);
            }
        }catch(Exception ex){
            //Suppress exception when unable to get resource names
            //Possibe errors: deafult server is not Sun Application Server (classcast exception)
            //Application server is not running.
            LOGGER.log(Level.WARNING, "getResourceNames failed", ex);
        }
        return resList;
    }

    private static List getProjectResources(FileObject targetFolder, String resourceType){
        List projectResources = new ArrayList();
        if(targetFolder != null){
            File resource = getServerResourcesFile(targetFolder,true);
            if((resource != null) && resource.exists()){
                if(resourceType.equals(__ConnectionPoolResource)) {
                    projectResources = getConnectionPools(resource, projectResources);
                } else if(resourceType.equals(__JDBCResource)) {
                    projectResources = getDataSources(resource, projectResources);
                } else if(resourceType.equals(__MAILResource)) {
                    projectResources = getMailResources(resource, projectResources);
                } else if(resourceType.equals(__JMSResource)) {
                    projectResources = getJMSResources(resource, projectResources);
                } else if(resourceType.equals(__PersistenceResource)) {
                    projectResources = getPersistenceResources(resource, projectResources);
                }else {
                    projectResources = getAllResourceNames(resource, projectResources);
                }
            }
        }
        return projectResources;
    }

    private static List getConnectionPools(File primaryFile, List projectCP){
        try{
            if(! primaryFile.isDirectory()){
                FileInputStream in = new FileInputStream(primaryFile);
                try {
                    Resources resources = DDProvider.getDefault().getResourcesGraph(in);

                    // identify JDBC Connection Pool xml
                    JdbcConnectionPool[] pools = resources.getJdbcConnectionPool();
                    for(int i=0; i<pools.length; i++){
                        projectCP.add(pools[i].getName());
                    }
                } finally {
                    in.close();
                }
            }
        }catch(Exception ex){
            //Could not get list of local Connection pools
            LOGGER.log(Level.SEVERE, "getConnectionPools failed", ex);
        }
        return projectCP;
    }

    private static List getDataSources(File primaryFile, List projectDS){
        try{
            if(! primaryFile.isDirectory()){
                FileInputStream in = new FileInputStream(primaryFile);
                try {
                    Resources resources = DDProvider.getDefault().getResourcesGraph(in);

                    // identify JDBC Resources xml
                    JdbcResource[] dataSources = resources.getJdbcResource();
                    for(int i=0; i<dataSources.length; i++){
                        projectDS.add(dataSources[i].getJndiName());
                    }
                } finally {
                    in.close();
                }
            }
        }catch(Exception ex){
            //Could not get list of local Connection pools
            LOGGER.log(Level.SEVERE, "filterDataSources failed", ex);
        }
        return projectDS;
    }

    private static List getMailResources(File primaryFile, List projectRes){
        try{
            if(! primaryFile.isDirectory()){
                FileInputStream in = new FileInputStream(primaryFile);
                try {
                    Resources resources = DDProvider.getDefault().getResourcesGraph(in);

                    // identify MailResource xml
                    MailResource[] res = resources.getMailResource();
                    for(int i=0; i<res.length; i++){
                        projectRes.add(res[i].getJndiName());
                    }
                } finally {
                    in.close();
                }
            }
        }catch(Exception ex){
            LOGGER.log(Level.SEVERE, "getMailResources failed", ex);
        }
        return projectRes;
    }

    private static List getJMSResources(File primaryFile, List projectRes){
        try{
            if(! primaryFile.isDirectory()){
                FileInputStream in = new FileInputStream(primaryFile);
                try {
                    Resources resources = DDProvider.getDefault().getResourcesGraph(in);

                    // identify AdminObjectResource xml
                    AdminObjectResource[] aoRes = resources.getAdminObjectResource();
                    for(int i=0; i<aoRes.length; i++){
                        projectRes.add(aoRes[i].getJndiName());
                    }
                    // identify ConnectorResource xml
                    ConnectorResource[] connRes = resources.getConnectorResource();
                    for(int i=0; i<connRes.length; i++){
                        projectRes.add(connRes[i].getJndiName());
                    }
                } finally {
                    in.close();
                }
            }
        }catch(Exception ex){
            LOGGER.log(Level.SEVERE, "getJMSResources failed", ex);
        }
        return projectRes;
    }

    private static List getPersistenceResources(File primaryFile, List projectRes){
        try{
            if(! primaryFile.isDirectory()){
                FileInputStream in = new FileInputStream(primaryFile);
                try {
                    Resources resources = DDProvider.getDefault().getResourcesGraph(in);

                    // identify AdminObjectResource xml
                    PersistenceManagerFactoryResource[] pmfRes = resources.getPersistenceManagerFactoryResource();
                    for(int i=0; i<pmfRes.length; i++){
                        projectRes.add(pmfRes[i].getJndiName());
                    }
                } finally {
                    in.close();
                }
            }
        }catch(Exception ex){
            LOGGER.log(Level.SEVERE, "getPersistenceManagerFactoryResource failed", ex);
        }
        return projectRes;
    }

    public static List getAllResourceNames(File primaryFile, List projectRes){
        try{
            if(! primaryFile.isDirectory()){
                FileInputStream in = new FileInputStream(primaryFile);
                try {
                    Resources resources = DDProvider.getDefault().getResourcesGraph(in);

                    // identify JDBC Connection Pool xml
                    JdbcConnectionPool[] pools = resources.getJdbcConnectionPool();
                    for(int i=0; i<pools.length; i++){
                        projectRes.add(pools[i].getName());
                    }

                    // identify JDBC Resources xml
                    JdbcResource[] dataSources = resources.getJdbcResource();
                    for(int i=0; i<dataSources.length; i++){
                        projectRes.add(dataSources[i].getJndiName());
                    }

                    // identify MailResource xml
                    MailResource[] mailRes = resources.getMailResource();
                    for(int i=0; i<mailRes.length; i++){
                        projectRes.add(mailRes[i].getJndiName());
                    }

                    // identify AdminObjectResource xml
                    AdminObjectResource[] aoRes = resources.getAdminObjectResource();
                    for(int i=0; i<aoRes.length; i++){
                        projectRes.add(aoRes[i].getJndiName());
                    }
                    // identify ConnectorResource xml
                    ConnectorResource[] connRes = resources.getConnectorResource();
                    for(int i=0; i<connRes.length; i++){
                        projectRes.add(connRes[i].getJndiName());
                    }
                } finally {
                    in.close();
                }
            }
        }catch(Exception ex){
            LOGGER.log(Level.SEVERE, "getAllResourceNames failed", ex);
        }
        return projectRes;
    }

    public static FileObject setUpExists(FileObject targetFolder){
        FileObject pkgLocation = getResourceDirectory(targetFolder);
        if(pkgLocation == null){
            //resource will be created under existing structure
            return targetFolder;
        }else{
            return pkgLocation;
        }
    }

    private static Resources getResourceGraph(String version){
        return DDProvider.getDefault().getResourcesGraph(version);
    }

    private static PropertyElement populatePropertyElement(PropertyElement prop, NameValuePair pair){
        prop.setName(pair.getParamName());
        prop.setValue(pair.getParamValue());
        if ("()".equals(prop.getValue()))  prop.setValue(""); //NOI18N
        return prop;
    }

    //Obtained from com.iplanet.ias.util.io.FileUtils - Byron's
    public static boolean isLegalFilename(String filename) {
        for(int i = 0; i < ILLEGAL_FILENAME_CHARS.length; i++)
            if(filename.indexOf(ILLEGAL_FILENAME_CHARS[i]) >= 0)
                return false;

        return true;
    }

    public static boolean isFriendlyFilename(String filename) {
        if(filename.indexOf(BLANK) >= 0 || filename.indexOf(DOT) >= 0)
            return false;

        return isLegalFilename(filename);
    }

    public static String makeLegalFilename(String filename) {
        for(int i = 0; i < ILLEGAL_FILENAME_CHARS.length; i++)
            filename = filename.replace(ILLEGAL_FILENAME_CHARS[i], REPLACEMENT_CHAR);

        return filename;
    }

    public static boolean isLegalResourceName(String filename) {
        for(int i = 0; i < ILLEGAL_RESOURCE_NAME_CHARS.length; i++)
            if(filename.indexOf(ILLEGAL_RESOURCE_NAME_CHARS[i]) >= 0)
                return false;

        return true;
    }

    
    public static FileObject getResourceDirectory(FileObject fo){
        Project holdingProj = FileOwnerQuery.getOwner(fo);
        FileObject resourceDir = null;
        if (holdingProj != null){
            J2eeModuleProvider provider = (J2eeModuleProvider) holdingProj.getLookup().lookup(J2eeModuleProvider.class);
            if(provider != null){
                File resourceLoc = provider.getJ2eeModule().getResourceDirectory();
                if(resourceLoc != null){
                    if(resourceLoc.exists ()){
                        resourceDir = FileUtil.toFileObject (resourceLoc);
                    }else{
                        try {
                            resourceDir = FileUtil.createFolder(resourceLoc);
                        } catch (IOException ex) {
                            LOGGER.log(Level.SEVERE, "getResourceDirectory failed", ex);
                        }
                    }
                }
            }
        }
        return resourceDir;
    }

    private static DeploymentManager getDeploymentManager(J2eeModuleProvider provider) {
        DeploymentManager dm = null;
        InstanceProperties ip = provider.getInstanceProperties();
        if (ip != null) {
            LOGGER.log(Level.INFO, "investigate", new Exception());
        }
        return dm;
    }

    public static void createSampleDataSource(J2eeModuleProvider provider){
        DeploymentManager dm = getDeploymentManager(provider);
        if (dm instanceof SunDeploymentManagerInterface) {
            SunDeploymentManagerInterface eightDM = (SunDeploymentManagerInterface) dm;
            try {
                ObjectName configObjName = new ObjectName(MAP_RESOURCES);
                if (eightDM.isRunning()) {
                    updateSampleDatasource(eightDM, configObjName);
                } else {
                    eightDM.createSampleDataSourceinDomain();
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "createSampleDataSource failed", ex);
            }
        }
    }

    /***************************************** DS Management API *****************************************************************************/

    public static HashSet getServerDataSources(DeploymentManager dm){
        HashSet datasources = new HashSet();
        try {
            ObjectName configObjName = new ObjectName(MAP_RESOURCES);
            SunDeploymentManagerInterface eightDM = (SunDeploymentManagerInterface)dm;
            ServerInterface mejb = (ServerInterface)eightDM.getManagement();
            List systemDS = Arrays.asList(sysDatasources);
            if(eightDM.isRunning()){
                updateSampleDatasource(eightDM, configObjName);
                ObjectName[] resourceObjects = (ObjectName[])  mejb.invoke(configObjName, __GetJdbcResource, null, null);
                for(int i=0; i<resourceObjects.length; i++){
                    ObjectName objName = resourceObjects[i];
                    //Get Required values from JDBC Resource
                    String dsJndiName = (String)mejb.getAttribute(objName, "jndi-name"); //NOI18N
                    if (!systemDS.contains(dsJndiName)) {
                        String poolName = (String) mejb.getAttribute(objName, "pool-name"); //NOI18N
                        HashMap poolValues = fillInPoolValues(eightDM, configObjName, poolName);
                        if (!poolValues.isEmpty()) {
                            String url = (String) poolValues.get(__Url);
                            if ((url != null) && (!url.equals(""))) { //NOI18N
                                String username = (String) poolValues.get(__User);
                                String password = (String) poolValues.get(__Password);
                                String driverClassName = (String) poolValues.get(__DriverClass);

                                SunDatasource ds = new SunDatasource(dsJndiName, url, username, password, driverClassName);
                                datasources.add(ds);
                            }
                        }
                    }
                } // for - each JDBC Resource
            } else{
                if(eightDM.isLocal()) {
                    datasources = formatXmlSunDatasources(eightDM.getSunDatasourcesFromXml());
                }
            }// Server Running
        } catch (Exception ex) {
            //Unable to get server datasources
            LOGGER.log(Level.SEVERE, "getServerDataSources failed", ex);
        }
        return datasources;
    }

    private static void updateSampleDatasource(SunDeploymentManagerInterface eightDM, ObjectName configObjName){
        try{
            if(! eightDM.isLocal())
                return;
            List datasources = getResourceNames(eightDM, __GetJdbcResource, "jndi-name"); //NOI18N
            if(! datasources.contains(SAMPLE_DATASOURCE)){
                ServerInterface mejb = (ServerInterface)eightDM.getManagement();
                List pools = getResourceNames(eightDM, __GetJdbcConnectionPool, "name"); //NOI18N
                if(! pools.contains(SAMPLE_CONNPOOL)){
                    AttributeList poolAttrs = new AttributeList();
                    Attribute attr = new Attribute("name", SAMPLE_CONNPOOL); //NOI18N
                    poolAttrs.add(attr);
                    attr = new Attribute("datasource-classname", "org.apache.derby.jdbc.ClientDataSource"); //NOI18N
                    poolAttrs.add(attr);
                    attr = new Attribute("res-type", "javax.sql.DataSource"); //NOI18N
                    poolAttrs.add(attr);

                    Properties propsList = new Properties();
                    propsList.put(__User, "app"); //NOI18N
                    propsList.put(__Password, "app"); //NOI18N
                    propsList.put(__ServerName, "localhost"); //NOI18N
                    propsList.put(__DerbyPortNumber, "1527");
                    propsList.put(__DerbyDatabaseName, "sample"); //NOI18N
                    propsList.put(__Url, "jdbc:derby://localhost:1527/sample"); //NOI18N
                    Object[] poolParams = new Object[]{poolAttrs, propsList, null};
                    createResource(__CreateCP, poolParams, mejb);
                }

                AttributeList attrs = new AttributeList();
                attrs.add(new Attribute(__JndiName, SAMPLE_DATASOURCE));
                attrs.add(new Attribute(__PoolName, SAMPLE_CONNPOOL));
                attrs.add(new Attribute(__JdbcObjectType, "user")); //NOI18N
                attrs.add(new Attribute(__Enabled, "true")); //NOI18N
                Object[] params = new Object[]{attrs, new Properties(), null};
                createResource(__CreateDS, params, mejb);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "updateSampleDatasource failed", ex);
        }
    }

    public static HashMap fillInPoolValues(SunDeploymentManagerInterface eightDM, ObjectName configObjName, String poolName) throws Exception {
        HashMap connPoolAttrs = new HashMap();
        ServerInterface mejb = (ServerInterface)eightDM.getManagement();

        List connPools = getResourceNames(eightDM, __GetJdbcConnectionPool, "name"); //NOI18N
        if(connPools.contains(poolName)) {
            //Get Values from JDBC Connection Pool : driver
            ObjectName connPoolObj = getConnectionPoolByName(mejb, configObjName, poolName);
            String dsClassName = (String) mejb.getAttribute(connPoolObj, "datasource-classname"); //NOI18N
            String resType = (String) mejb.getAttribute(connPoolObj, "res-type"); //NOI18N
            String url = ""; //NOI18N
            String username = ""; //NOI18N
            String password = ""; //NOI18N
            String serverName = ""; //NOI18N
            String portNo = ""; //NOI18N
            String dbName = ""; //NOI18N
            String sid = ""; //NOI18N
            String driverClass = ""; //NOI18N
            String informixServerName = ""; //NOI18N
            String informixHostName = ""; //NOI18N
            String derbyConnAttr = ""; //NOI18N

            AttributeList attrList = (AttributeList) mejb.invoke(connPoolObj, __GetProperties, null, null);
            HashMap attrs = getObjMap(attrList);
            Object[] keys = attrs.keySet().toArray();
            for (int i = 0; i < keys.length; i++) {
                String keyName = (String) keys[i];
                if (keyName.equalsIgnoreCase(__DatabaseName)) {
                    if (dsClassName.indexOf("pointbase") != -1) { //NOI18N
                        url = getStringVal(attrs.get(keyName));
                    } else {
                        dbName = getStringVal(attrs.get(keyName));
                    }
                } else if (keyName.equalsIgnoreCase(__User)) {
                    username = getStringVal(attrs.get(keyName));
                } else if (keyName.equalsIgnoreCase(__Password)) {
                    password = getStringVal(attrs.get(keyName));
                } else if (keyName.equalsIgnoreCase(__Url)) {
                    url = getStringVal(attrs.get(keyName));
                } else if (keyName.equalsIgnoreCase(__ServerName)) {
                    serverName = getStringVal(attrs.get(keyName));
                } else if (keyName.equalsIgnoreCase(__DerbyPortNumber)) {
                    portNo = getStringVal(attrs.get(keyName));
                } else if (keyName.equalsIgnoreCase(__SID)) {
                    sid = getStringVal(attrs.get(keyName));
                } else if (keyName.equalsIgnoreCase(__DriverClass)) {
                    driverClass = getStringVal(attrs.get(keyName));
                } else if (keyName.equalsIgnoreCase(__InformixServer)) {
                    informixServerName = getStringVal(attrs.get(keyName));
                } else if (keyName.equalsIgnoreCase(__InformixHostName)) {
                    informixHostName = getStringVal(attrs.get(keyName));
                } else if (keyName.equalsIgnoreCase(__DerbyConnAttr)) {
                    derbyConnAttr = getStringVal(attrs.get(keyName));
                }

            }
            if (url == null || url.equals("")) {
                if (dsClassName.indexOf("derby") != -1) { //NOI18N
                    url = "jdbc:derby://"; //NOI18N
                    if (serverName != null) {
                        url = url + serverName;
                        if (portNo != null && portNo.length() > 0) {
                            url = url + ":" + portNo; //NOI18N
                        }
                        url = url + "/" + dbName; //NOI18N
                        if (derbyConnAttr != null && (!derbyConnAttr.equals(""))) { //NOI18N
                            url = url + derbyConnAttr;
                        }
                    }
                } else {
                    String urlPrefix = DatabaseUtils.getUrlPrefix(dsClassName, resType);
                    String vName = ResourceConfigurator.getDatabaseVendorName(urlPrefix, null);
                    if (serverName != null) {
                        if (vName.equals("sybase2")) { //NOI18N
                            url = urlPrefix + serverName;
                        }
                        if (vName.equals("informix")) { //NOI18N
                            url = urlPrefix + "//" + informixHostName;
                        } else {
                            url = urlPrefix + "//" + serverName; //NOI18N
                        }
                        if (portNo != null && portNo.length() > 0) {
                            url = url + ":" + portNo; //NOI18N
                        }
                    }
                    if (vName.equals("sun_oracle") || vName.equals("datadirect_oracle")) { //NOI18N
                        url = url + ";SID=" + sid; //NOI18N
                    } else if (Arrays.asList(Reqd_DBName).contains(vName)) {
                        url = url + ";databaseName=" + dbName; //NOI18N
                    } else if (Arrays.asList(VendorsDBNameProp).contains(vName)) {
                        url = url + "/" + dbName; //NOI18N
                    }
                    if (vName.equals("informix")) { //NOI18N
                        url = url + ":INFORMIXSERVER=" + serverName;
                    } else if (vName.equals("datadirect_informix")) {
                        url = url + ";InformixServer=" + informixServerName;
                    }
                }
            }

            if (url != null && (!url.equals(""))) { //NOI18N
                if ((!eightDM.isLocal()) && (url.indexOf("localhost") != -1)) { //NOI18N
                    String hostName = eightDM.getHost();
                    url = url.replaceFirst("localhost", hostName); //NOI18N
                }
                DatabaseConnection databaseConnection = getDatabaseConnection(url);
                if (driverClass == null || driverClass.equals("")) { //NOI18N
                    if (databaseConnection != null) {
                        driverClass = databaseConnection.getDriverClass();
                    } else {
                        //Fix Issue 78212 - NB required driver classname
                        String drivername = DatabaseUtils.getDriverName(url);
                        if (drivername != null) {
                            driverClass = drivername;
                        }
                    }
                }
            }

            connPoolAttrs.put(__User, username);
            connPoolAttrs.put(__Password, password);
            connPoolAttrs.put(__Url, url);
            connPoolAttrs.put(__DriverClass, driverClass);
        }
        return connPoolAttrs;
    }

    private static ObjectName getConnectionPoolByName(ServerInterface mejb, ObjectName configObjName, String poolName) throws Exception {
        String[] signature = new String[]{"java.lang.String"};  //NOI18N
        Object[] params = new Object[]{poolName};
        ObjectName connPoolObj = (ObjectName) mejb.invoke(configObjName, __GetJdbcConnectionPoolByName, params, signature);
        return connPoolObj;
    }

    private static String getStringVal(Object val){
        String value = null;
        if (val != null)
            value = val.toString();
        return value;
    }

    private static HashMap getObjMap(AttributeList attrList){
        HashMap attrs = new HashMap();
        for(int k=0; k<attrList.size(); k++){
            Attribute currAttr = (Attribute)attrList.get(k);
            String pname = currAttr.getName();
            Object pObjvalue = currAttr.getValue();
            attrs.put(pname, pObjvalue);
        }
        return attrs;
    }

    public static String revertToResName(String filename) {
        if(filename.indexOf("jdbc_") != -1)
            filename = filename.replaceFirst("jdbc_", "jdbc/");
        if(filename.indexOf("mail_") != -1)
            filename = filename.replaceFirst("mail_", "mail/");
        if(filename.indexOf("jms_") != -1)
            filename = filename.replaceFirst("jms_", "jms/");
        return filename;
    }

    public static boolean isUniqueFileName(String in_targetName, FileObject fo, String defName){
        boolean isUniq = true;
        String targetName = in_targetName;
        if (targetName != null && targetName.length() != 0) {
              targetName = makeLegalFilename(targetName);
              targetName = targetName + "." + __SunResourceExt; //NOI18N
              File targFile = new File(fo.getPath(), targetName);
              if(targFile.exists())
                  isUniq = false;
        }
        return isUniq;
    }

    public static DatabaseConnection getDatabaseConnection(String url) {
        DatabaseConnection[] dbConns = ConnectionManager.getDefault().getConnections();
        for(int i=0; i<dbConns.length; i++){
            String dbConnUrl = ((DatabaseConnection)dbConns[i]).getDatabaseURL();
            if(dbConnUrl.startsWith(url))
                return ((DatabaseConnection)dbConns[i]);
        }
        return null;
    }

    public static InstanceProperties getTargetServer(FileObject fo){
        InstanceProperties serverName = null;
        Project holdingProj = FileOwnerQuery.getOwner(fo);
        if (holdingProj != null){
            J2eeModuleProvider modProvider = (J2eeModuleProvider) holdingProj.getLookup().lookup(J2eeModuleProvider.class);
            if(modProvider != null)
                serverName = modProvider.getInstanceProperties();
        }
        return serverName;
    }

    public static HashMap getConnPoolValues(File resourceDir, String poolName){
        HashMap poolValues = new HashMap();
        try{
            ObjectName configObjName = new ObjectName(MAP_RESOURCES);
            InstanceProperties instanceProperties = getTargetServer(FileUtil.toFileObject(resourceDir));
            if(instanceProperties != null){
                SunDeploymentManagerInterface eightDM = null; // (SunDeploymentManagerInterface)instanceProperties.getDeploymentManager();
                LOGGER.log(Level.INFO, "investigate", new Exception());
                return poolValues;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "getConnPoolValues failed", ex);
        }
        return poolValues;
    }

    public static HashSet formatXmlSunDatasources(HashMap dsMap){
        HashSet datasources = new HashSet();
        String[] keys = (String[])dsMap.keySet().toArray(new String[dsMap.size()]);
        for(int i=0; i<keys.length; i++){
            String jndiName = keys[i];
            HashMap poolValues = (HashMap)dsMap.get(jndiName);
            poolValues = formatPoolMap(poolValues);

            String url = getStringVal(poolValues.get(__Url));
            String username = getStringVal(poolValues.get(__User));
            String password = getStringVal(poolValues.get(__Password));
            String driverClassName = getStringVal(poolValues.get(__DriverClass)); //NOI18N
            if((url != null) && (! url.equals (""))) { //NOI18N
                SunDatasource ds = new SunDatasource (jndiName, url, username, password, driverClassName);
                datasources.add (ds);
            }
        }

        return datasources;
    }

    private static HashMap formatPoolMap(HashMap poolValues){
        String driverClassName = getStringVal(poolValues.get("dsClassName")); //NOI18N
        String resType = getStringVal(poolValues.get("resType")); //NOI18N
        String url = ""; //NOI18N
        String serverName = getStringVal(poolValues.get(__ServerName));
        String portNo     = getStringVal(poolValues.get(__DerbyPortNumber));
        String dbName     = getStringVal(poolValues.get(__DerbyDatabaseName));
        String dbVal     = getStringVal(poolValues.get(__DatabaseName));
        String portVal     = getStringVal(poolValues.get(__PortNumber));
        String sid     = getStringVal(poolValues.get(__SID));
        String urlValue     = getStringVal(poolValues.get(__Url));
        String driverClass     = getStringVal(poolValues.get(__DriverClass));
        String derbyConnAttr   = getStringVal(poolValues.get(__DerbyConnAttr));

        if (driverClassName != null) {
            if (driverClassName.indexOf("pointbase") != -1) {
                url = getStringVal(poolValues.get(__DatabaseName));
            }
            if (urlValue == null || urlValue.equals("")) { //NOI18N
                if (driverClassName.indexOf("derby") != -1) {
                    if (serverName != null) {
                        url = "jdbc:derby://" + serverName;
                        if (portNo != null && portNo.length() > 0) {
                            url = url + ":" + portNo; //NOI18N
                        }
                        url = url + "/" + dbName; //NOI18N
                        if (derbyConnAttr != null && (!derbyConnAttr.equals(""))) { //NOI18N
                            url = url + derbyConnAttr;
                        }
                    }
                } else {
                    if (url == null || url.equals("")) {  //NOI18N
                        String urlPrefix = DatabaseUtils.getUrlPrefix(driverClassName, resType);
                        String vName = ResourceConfigurator.getDatabaseVendorName(urlPrefix, null);
                        if (serverName != null) {
                            if (vName.equals("sun_oracle")) {    //NOI18N
                                url = urlPrefix + serverName;
                            } else {
                                url = urlPrefix + "//" + serverName; //NOI18N
                            }
                            if (portVal != null && portVal.length() > 0) {
                                url = url + ":" + portVal; //NOI18N
                            }
                        }
                        if (vName.equals("sun_oracle") || vName.equals("datadirect_oracle")) {  //NOI18N
                            url = url + ";SID=" + sid; //NOI18N
                        } else if (Arrays.asList(Reqd_DBName).contains(vName)) {
                            url = url + ";databaseName=" + dbVal; //NOI18N
                        } else if (Arrays.asList(VendorsDBNameProp).contains(vName)) {
                            url = url + "/" + dbVal; //NOI18N
                        }
                    }
                }
            } else {
                url = urlValue;
            }
        }
        if (url != null && (!url.equals(""))) { //NOI18N
            if (driverClass == null || driverClass.equals("")) { //NOI18N
                DatabaseConnection databaseConnection = getDatabaseConnection(url);
                if (databaseConnection != null) {
                    driverClass = databaseConnection.getDriverClass();
                } else {
                    //Fix Issue 78212 - NB required driver classname
                    String drivername = DatabaseUtils.getDriverName(url);
                    if (drivername != null) {
                        driverClass = drivername;
                    }
                }
            }
        }
        poolValues.put(__Url, url);
        poolValues.put(__DriverClass, driverClass);

        return poolValues;
    }

    public static HashSet getServerDestinations(DeploymentManager dm){
        HashSet destinations = new HashSet();
        try {
            ObjectName configObjName = new ObjectName(MAP_RESOURCES);
            SunDeploymentManagerInterface eightDM = (SunDeploymentManagerInterface)dm;
            ServerInterface mejb = (ServerInterface)eightDM.getManagement();
            if(eightDM.isRunning()){
                ObjectName[] resourceObjects = (ObjectName[])  mejb.invoke(configObjName, __GetAdmObjResource, null, null);
                for(int i=0; i<resourceObjects.length; i++){
                    ObjectName objName = resourceObjects[i];
                    String jndiName = (String)mejb.getAttribute(objName, "jndi-name"); //NOI18N
                    String type = (String)mejb.getAttribute(objName, "res-type"); //NOI18N
                    SunMessageDestination sunMessage = null;
                    if(type.equals(__QUEUE)){
                        sunMessage = new SunMessageDestination(jndiName, MessageDestination.Type.QUEUE);
                    } else {
                        sunMessage = new SunMessageDestination(jndiName, MessageDestination.Type.TOPIC);
                    }
                    destinations.add(sunMessage);
                } //
            } else{
                if(eightDM.isLocal()) {
                    HashMap aoMap =  eightDM.getAdminObjectResourcesFromXml();
                    String[] keys = (String[])aoMap.keySet().toArray(new String[aoMap.size()]);
                    for(int i=0; i<keys.length; i++){
                        String jndiName = keys[i];
                        String type = (String)aoMap.get(jndiName);
                        SunMessageDestination sunMessage = null;
                        if(type.equals(__QUEUE)){
                            sunMessage = new SunMessageDestination(jndiName, MessageDestination.Type.QUEUE);
                        } else {
                            sunMessage = new SunMessageDestination(jndiName, MessageDestination.Type.TOPIC);
                        }
                        destinations.add(sunMessage);
                    }
                }
            }// Server Running
        } catch (Exception ex) {
            //Unable to get server datasources
            LOGGER.log(Level.SEVERE, "getServerDestinations failed", ex);
        }
        return destinations;
    }

    public static boolean is90Server(ServerInterface mejb){
        boolean is90Server = true;
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)mejb.getDeploymentManager();
        if(sunDm.isLocal()){
            is90Server = is90ServerLocal(sunDm);
        }else{
            try{
                ObjectName serverObj = new ObjectName("com.sun.appserv:j2eeType=J2EEServer,name=server,category=runtime"); //NOI18N
                String serverName = (String)mejb.getAttribute(serverObj, "serverVersion"); //NOI18N
                if((serverName != null) && (serverName.indexOf("8.") != -1)) //NOI18N
                    is90Server = false;
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "is90Server failed", ex);
            }
        }
        return is90Server;
    }

    private static boolean is90ServerLocal(SunDeploymentManagerInterface sunDm){
        boolean isGlassfish = true;
        try{
            isGlassfish = ServerLocationManager.isGlassFish(sunDm.getPlatformRoot());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "is90ServerLocal failed", ex);
        }
        return isGlassfish;
    }

    /*
     * Create a new sun-resources graph if none exists or obtain the existing
     * graph to add new resource.
     */
    public static Resources getServerResourcesGraph(File targetFolder){
        FileObject location = FileUtil.toFileObject(targetFolder.getParentFile());
        try{
            location = FileUtil.createFolder(targetFolder);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "getServerResourcesGraph failed", ex);
        }
        return getServerResourcesGraph(location,"");
    }

    /*
     * Create a new sun-resources graph if none exists or obtain the existing
     * graph to add new resource.
     */
    public static Resources getServerResourcesGraph(FileObject targetFolder, String version){
        Resources res = getResourceGraph(version);
        targetFolder = setUpExists(targetFolder);
        File sunResource = getServerResourcesFile(targetFolder,true);
        if(sunResource != null){
            res = getResourcesGraph(sunResource);
        }
        return res;
    }

    /*
     * Get the resources-graph for a sun-resource.xml
     *
     */
    public static Resources getResourcesGraph(File sunResource){
        Resources res = null;
        if(sunResource != null){
            java.io.FileInputStream in = null;
            try {
                in = new java.io.FileInputStream(sunResource);
                res = DDProvider.getDefault().getResourcesGraph(in);
            } catch (FileNotFoundException ex) {
                LOGGER.log(Level.SEVERE, "getResourcesGraph failed", ex);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "getResourcesGraph failed", ex);
            } catch (SAXException ex) {
                LOGGER.log(Level.SEVERE, "getResourcesGraph failed", ex);
            } finally {
                try {
                    if (null != in)  {
                        in.close();
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "getResourcesGraph failed", ex);
                }
            }
        }
        return res;
    }

    /**
     * Write resources to file.
     * <p/>
     * @param targetFolder Resources file target folder.
     * @param res          Resources to be written.
     * @param baseName     Resources file base name.
     * @deprecated Use {@link #createFile(ResourceConfigData, Resources, String)}
     */
    @Deprecated
    public static void createFile(
            final File targetFolder, final Resources res, final String baseName
    ) {
        createFile(FileUtil.toFileObject(targetFolder), res, baseName);
    }

    /**
     * Write resources to file.
     * <p/>
     * @param targetFolder Resources file target folder.
     * @param res          Resources to be written.
     * @param baseName     Resources file base name.
     * @deprecated Use {@link #createFile(ResourceConfigData, Resources, String)}
     */
    @Deprecated
    public static void createFile (
            final FileObject targetFolder, final Resources res, final String baseName
    ) {
        final FileObject folderToWrite = setUpExists(targetFolder);
        if (!overwriteExistingResourcesFile(folderToWrite, res)) {
            final File resourceFile = new File(FileUtil.toFile(folderToWrite), baseName);
            writeNewResourceFile(resourceFile, res);
        }
    }

    /**
     * Write resources to file.
     * <p/>
     * @param configData Resources configuration data.
     * @param res        Resources to be written.
     * @param baseName   Resources file base name.
     */
    public static void createFile (
            final ResourceConfigData configData, final Resources res, final String baseName
    ) {
        if (!overwriteExistingResourcesFile(configData.getTargetFileObject(), res)) {
            final File resourceFile = configData.targetFileForNewResourceFile(baseName);
            writeNewResourceFile(resourceFile, res);
        }
    }

    /**
     * Overwrite server resources file if this file already exists.
     * Will write resources file only if such a file already exists.
     * <p/>
     * @param targetFolder Resources file target folder. 
     * @param res          Resources to be written.
     * @returns Value of true if (@code targetFolder} is not {@code null}
     *          and resources file already exists (an attempt to overwrite
     *          resources file was done).
     */
    private static boolean overwriteExistingResourcesFile(
            final FileObject targetFolder, final Resources res
    ) {
        final File resourceFile = getServerResourcesFile(targetFolder, true);
        if ((resourceFile != null) && resourceFile.exists()) {
            try {
                res.write(resourceFile);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to overwrite server resources file", ex);
            }
            return true;
        }
        return false;
    }

    /**
     * Write new server resources file.
     * <p/>
     * @param resourceFile Resources file including full path.
     * @param res          Resources to be written.
     */
    private static void writeNewResourceFile(final File resourceFile, final Resources res) {
        try {
            final FileObject resourceFileDir = FileUtil.toFileObject(resourceFile.getParentFile());
            final FileSystem fs = resourceFileDir.getFileSystem();
            fs.runAtomicAction(new FileSystem.AtomicAction() {
                @Override
                public void run() throws java.io.IOException {
                    resourceFile.createNewFile();
                    final FileObject resourceFileFo = FileUtil.toFileObject(resourceFile);
                    final FileLock lock = resourceFileFo.lock();
                    Writer out = null;
                    try {
                        out = new OutputStreamWriter(resourceFileFo.getOutputStream(lock), "UTF8");
                        res.write(out);
                        out.flush();
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Failed to write new server resources file", ex);
                    } finally {
                        if (out != null) {
                            out.close();
                        }
                        lock.releaseLock();
                    }
                }
            });
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to write new server resources file", ex);
        }
    }

    /*
     *  Get sun-resources.xml file if it exists in a given folder.
     *  Returns null if no file exists.
     */
    public static File getServerResourcesFile(FileObject targetFolder, boolean search){
        File serverResource = null;
        if(targetFolder != null){
            FileObject setUpFolder = setUpExists(targetFolder);
            if (search) {
                // look in the 'web' project place
                FileObject metaInf = setUpFolder.getParent().getFileObject("web/WEB-INF"); // NOI18N
                if (null == metaInf) {
                    // look in the 'ant' project place
                    metaInf = setUpFolder.getParent().getFileObject("src/java/META-INF"); // NOI18N
                    if (null == metaInf) {
                        // look in the 'maven' project place
                        metaInf = targetFolder.getParent().getFileObject("java/META-INF"); // NOI18N
                    }
                }
                if (null != metaInf) {
                    FileObject resourcesFO = metaInf.getFileObject(RESOURCE_FILES[0]);
                    if (null == resourcesFO) {
                        resourcesFO = metaInf.getFileObject(RESOURCE_FILES[1]);
                    }
                    if (null != resourcesFO) {
                        return FileUtil.toFile(resourcesFO);
                    }
                }
            }

            java.util.Enumeration en = setUpFolder.getData(false);
            while(en.hasMoreElements()){
                FileObject resourceFile = (FileObject)en.nextElement();
                File resource = FileUtil.toFile(resourceFile);
                if(resource.getName().equals(RESOURCE_FILES[1])){
                    serverResource = resource;
                }
                if(resource.getName().equals(RESOURCE_FILES[0])){
                    serverResource = resource;
                    break;
                }
            }
        }
        return serverResource;
    }

    /*
     * Consolidates *.sun-resource into sun-resources.xml
     * Called by registerResources in Utils (appsrv81 module)
     * sun-resources.xml is created once.
     */
    public static void migrateResources(File resourceDir,String baseName) {
        migrateResources(FileUtil.toFileObject(resourceDir),baseName);
    }

    /*
     * Consolidates *.sun-resource into sun-resources.xml
     * Called by SunResourceDataObject by the .sun-resource
     * loader. sun-resources.xml is created once.
     */
    public static void migrateResources(FileObject targetFolder,String baseName){
        targetFolder = setUpExists(targetFolder);
        File sunResource = getServerResourcesFile(targetFolder,false);
        if((sunResource == null) || (! sunResource.exists())){
            File resourceDir = FileUtil.toFile(targetFolder);
            File[] resources = resourceDir.listFiles(new OldResourceFileFilter());
            if (resources.length > 0) {
                Resources newGraph = DDProvider.getDefault().getResourcesGraph(Resources.VERSION_1_3);
                try {
                    for (int i = 0; i < resources.length; i++) {
                        File oldResource = resources[i];
                        FileInputStream in = new java.io.FileInputStream(oldResource);
                        Resources existResource = DDProvider.getDefault().getResourcesGraph(in);
                        newGraph = getResourceGraphs(newGraph, existResource);
                        boolean success = oldResource.delete();
                        if(! success){
                            LOGGER.log(Level.INFO, "Unable to delete *.sun-resource file(s)");
                        }
                    }
                    createFile(targetFolder, newGraph,baseName);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "migrateResources failed", ex);
                }
            }
        }
    }

    private static Resources getResourceGraphs(Resources consolidatedGraph, Resources existResource){
        JdbcConnectionPool[] pools = existResource.getJdbcConnectionPool();
        if(pools.length != 0){
            ConnPoolBean currCPBean = ConnPoolBean.createBean(pools[0]);
            currCPBean.getBeanInGraph(consolidatedGraph);
        }

        JdbcResource[] dataSources = existResource.getJdbcResource();
        if(dataSources.length != 0){
            DataSourceBean currDSBean = DataSourceBean.createBean(dataSources[0]);
            currDSBean.getBeanInGraph(consolidatedGraph);
        }

        MailResource[] mailResources = existResource.getMailResource();
        if(mailResources.length != 0){
            JavaMailSessionBean currMailBean = JavaMailSessionBean.createBean(mailResources[0]);
            currMailBean.getBeanInGraph(consolidatedGraph);
        }

//        JmsResource[] jmsResources = existResource.getJmsResource();
//        if(jmsResources.length != 0){
//            JMSBean jmsBean = JMSBean.createBean(jmsResources[0]);
//            jmsBean.getAdminObjectBeanInGraph(consolidatedGraph);
//        }

        AdminObjectResource[] aoResources = existResource.getAdminObjectResource();
        if(aoResources.length != 0){
            JMSBean jmsBean = JMSBean.createBean(aoResources[0]);
            jmsBean.getAdminObjectBeanInGraph(consolidatedGraph);
        }

        ConnectorResource[] connResources = existResource.getConnectorResource();
        ConnectorConnectionPool[] connPoolResources = existResource.getConnectorConnectionPool();
        if(connResources.length != 0 && connPoolResources.length != 0){
            JMSBean jmsBean = JMSBean.createBean(existResource);
            jmsBean.getConnectorBeanInGraph(consolidatedGraph);
        }

        PersistenceManagerFactoryResource[] pmfResources = existResource.getPersistenceManagerFactoryResource();
        if(pmfResources.length != 0){
            PersistenceManagerBean currPMFBean = PersistenceManagerBean.createBean(pmfResources[0]);
            currPMFBean.getBeanInGraph(consolidatedGraph);
        }

        return consolidatedGraph;
    }

    private static class OldResourceFileFilter implements FileFilter {
        public boolean accept(File f) {
            return ((! f.isDirectory()) && f.getName().toLowerCase(Locale.ENGLISH).endsWith(".sun-resource")); //NOI18N
        }
    }

    /****************************************Utilities *********************************************/
    /**
     *
     * @param name Resource Name
     * @param resources Map of objects to check Resource Name for duplicate
     * @return Returns unique resource name
     *
     */
    public static String getUniqueResourceName(String name, HashMap resources){
        for (int i = 1;; i++) {
            String resourceName = name + "_" + i; // NOI18N
            if (! resources.containsKey(resourceName)) {
                return resourceName;
            }
        }
    }

    public static String getUniqueResourceName(String name, List resources){
        for (int i = 1;; i++) {
            String resourceName = name + "_" + i; // NOI18N
            if (! resources.contains(resourceName)) {
                return resourceName;
            }
        }
    }

    private static final char BLANK = ' ';
    private static final char DOT   = '.';
    private static final char REPLACEMENT_CHAR = '_';
    private static final char[]	ILLEGAL_FILENAME_CHARS	= {'/', '\\', ':', '*', '?', '"', '<', '>', '|', ',' };
    private static final char[]	ILLEGAL_RESOURCE_NAME_CHARS	= {':', '*', '?', '"', '<', '>', '|', ',' };
}
