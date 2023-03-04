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

package org.netbeans.modules.j2ee.jboss4.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.jboss4.config.gen.EnterpriseBeans;
import org.netbeans.modules.j2ee.jboss4.config.gen.Entity;
import org.netbeans.modules.j2ee.jboss4.config.gen.Jboss;
import org.netbeans.modules.j2ee.jboss4.config.gen.MessageDriven;
import org.netbeans.modules.j2ee.jboss4.config.gen.ResourceRef;
import org.netbeans.modules.j2ee.jboss4.config.gen.Session;
import org.netbeans.modules.j2ee.jboss4.config.mdb.MessageDestinationSupport;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;


/**
 * EJB module deployment configuration handles jboss.xml configuration file creation.
 *
 * @author Stepan Herold, Libor Kotouc
 */
public class EjbDeploymentConfiguration extends JBDeploymentConfiguration 
implements ModuleConfiguration, DatasourceConfiguration, DeploymentPlanConfiguration, PropertyChangeListener {
    
    private static final String SESSION_RESOURCE_REF = "/ejb-jar/enterprise-beans/session/resource-ref"; // NOI18N
    private static final String ENTITY_RESOURCE_REF = "/ejb-jar/enterprise-beans/entity/resource-ref"; //NOI1*N
    private static final String MSGDRV_RESOURCE_REF = "/ejb-jar/enterprise-beans/message-driven/resource-ref"; // NOI18N
    private static final String SESSION_EJB_REF = "/ejb-jar/enterprise-beans/session/ejb-ref"; // NOI18N
    private static final String ENTITY_EJB_REF = "/ejb-jar/enterprise-beans/entity/ejb-ref"; // NOI18N
    private static final String MSGDRV_EJB_REF = "/ejb-jar/enterprise-beans/message-driven/ejb-ref"; // NOI18N
    private static final String MSGDRV = "/ejb-jar/enterprise-beans/message-driven"; // NOI18N
    private static final String MSGDRV_MSG_DEST = MSGDRV + "/message-destination-link"; // NOI18N
    private static final String SESSION_MSG_DEST_REF = "/ejb-jar/enterprise-beans/session/message-destination-ref"; // NOI18N
    private static final String ENTITY_MSG_DEST_REF = "/ejb-jar/enterprise-beans/entity/message-destination-ref"; // NOI18N
    private static final String MSGDRV_MSG_DEST_REF = "/ejb-jar/enterprise-beans/message-driven/message-destination-ref"; // NOI18N
    
    static class BEAN_TYPE {
        private String type;
        
        private BEAN_TYPE(String type) { this.type = type; }
        String getType() { return type; }
        
        static final BEAN_TYPE SESSION = new BEAN_TYPE("session"); // NOI18N
        static final BEAN_TYPE ENTITY = new BEAN_TYPE("entity"); // NOI18N
        static final BEAN_TYPE MSGDRV = new BEAN_TYPE("message-driven"); // NOI18N
    }
    
    private File jbossFile;
    private Jboss jboss;
    
    // stores ejb-name between MSGDRV Xpath event and MSGDRV_MSG_DEST are fired
    private String tempEjbName;

    public EjbDeploymentConfiguration(J2eeModule j2eeModule) {
        this(j2eeModule, null);
    }

    /**
     * Creates a new instance of EjbDeploymentConfiguration 
     */
    public EjbDeploymentConfiguration(J2eeModule j2eeModule, JBPluginUtils.Version version) {
        super(j2eeModule, version);
        this.jbossFile = j2eeModule.getDeploymentConfigurationFile("META-INF/jboss.xml"); // NOI18N;
        getJboss();
        if (deploymentDescriptorDO == null) {
            try {
                deploymentDescriptorDO = deploymentDescriptorDO.find(FileUtil.toFileObject(jbossFile));
                deploymentDescriptorDO.addPropertyChangeListener(this);
            } catch(DataObjectNotFoundException donfe) {
                Exceptions.printStackTrace(donfe);
            }
        }
        // TODO: rewrite
//        EjbJar ejbJar = (EjbJar) j2eeModule.getMetadataModel(J2eeModule.EJBJAR_XML);
//        if (ejbJar != null) {
//            ejbJar.addPropertyChangeListener(this);
//        }
    }
    
    public Lookup getLookup() {
        return Lookups.fixed(this);
    }
    
    public void dispose() {
//        EjbJar ejbJar = (EjbJar) j2eeModule.getMetadataModel(J2eeModule.EJBJAR_XML);
//        if (ejbJar != null) {
//            ejbJar.removePropertyChangeListener(this);
//        }
    }

    public boolean supportsCreateDatasource() {
        return !isAs7();
    }
    
    public boolean supportsCreateMessageDestination() {
        return !isAs7();
    }
    
//        //listen on the resource-ref element
//        deplObj.getDDBeanRoot().addXpathListener(SESSION_RESOURCE_REF, this);
//        deplObj.getDDBeanRoot().addXpathListener(ENTITY_RESOURCE_REF, this);
//        deplObj.getDDBeanRoot().addXpathListener(SESSION_EJB_REF, this);
//        deplObj.getDDBeanRoot().addXpathListener(ENTITY_EJB_REF, this);
//        deplObj.getDDBeanRoot().addXpathListener(MSGDRV_RESOURCE_REF, this);
//        deplObj.getDDBeanRoot().addXpathListener(MSGDRV_EJB_REF, this);
//        deplObj.getDDBeanRoot().addXpathListener(MSGDRV, this);
//        deplObj.getDDBeanRoot().addXpathListener(MSGDRV_MSG_DEST, this);
//        deplObj.getDDBeanRoot().addXpathListener(SESSION_MSG_DEST_REF, this);
//        deplObj.getDDBeanRoot().addXpathListener(ENTITY_MSG_DEST_REF, this);
//        deplObj.getDDBeanRoot().addXpathListener(MSGDRV_MSG_DEST_REF, this);
       
    /**
     * Return jboss graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     * @return jboss graph or null if the jboss.xml file is not parseable.
     */
    public synchronized Jboss getJboss() {
        if (jboss == null) {
            try {
                if (jbossFile.exists()) {
                    // load configuration if already exists
                    try {
                        jboss = jboss.createGraph(jbossFile);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    } catch (RuntimeException re) {
                        // jboss.xml is not parseable, do nothing
                    }
                } else {
                    // create jboss.xml if it does not exist yet
                    jboss = generateJboss();
                    ResourceConfigurationHelper.writeFile(jbossFile, jboss);
                }
            } catch (ConfigurationException ce) {
                Exceptions.printStackTrace(ce);
            }
        }
        return jboss;
    }
    
    /**
     * Listen to jboss.xml document changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == DataObject.PROP_MODIFIED &&
            evt.getNewValue() == Boolean.FALSE) {
            if (evt.getSource() == deploymentDescriptorDO) { // dataobject has been modified, jboss graph is out of sync
                synchronized (this) {
                    jboss = null;
                }
            } else {
//                super.propertyChange(evt);
            }

//        } else if (evt.getOldValue() == null) {
//            if (newValue instanceof org.netbeans.modules.j2ee.dd.api.common.ResourceRef) {
//                //a new resource reference added
//                org.netbeans.modules.j2ee.dd.api.common.ResourceRef resourceRef = (org.netbeans.modules.j2ee.dd.api.common.ResourceRef) newValue;
//                try {
//                    String resType = resourceRef.getResType();
//                    if ("javax.sql.DataSource".equals(resType)) { // NOI18N
//                        addResReference(resourceRef.getResRefName());
//                    } else if ("javax.mail.Session".equals(resType)) { // NOI18N
//                        addMailReference(resourceRef.getResRefName());
//                    } else if ("javax.jms.ConnectionFactory".equals(resType)) { // NOI18N
//                        addConnectionFactoryReference(resourceRef.getResRefName());
//                    }
//                } catch (ConfigurationException ce) {
//                    ErrorManager.getDefault().notify(ce);
//                }
//            }
        }
    }
   
// TODO: rewrite this when resouces support api is ready
    
//    public void fireXpathEvent(XpathEvent xpe) {
//        
//        if (!xpe.isAddEvent())
//            return;
//        
//        DDBean eventDDBean = xpe.getBean();
//        if (SESSION_RESOURCE_REF.equals(eventDDBean.getXpath()) ||
//            ENTITY_RESOURCE_REF.equals(eventDDBean.getXpath()) ||
//            MSGDRV_RESOURCE_REF.equals(eventDDBean.getXpath())) 
//        {
//            String[] desc = eventDDBean.getText("description"); // NOI18N
//            String[] name = eventDDBean.getText("res-ref-name"); // NOI18N
//            String[] type = eventDDBean.getText("res-type");     // NOI18N
//            if (name.length > 0 && type.length > 0) {
//                try {
//                    //we don't know which bean contains the created resource-ref,
//                    //thus we must find all beans containing the same resource-ref
//                    //as the one just created
//                    BEAN_TYPE beanType = (SESSION_RESOURCE_REF.equals(eventDDBean.getXpath()) ? 
//                                          BEAN_TYPE.SESSION : (ENTITY_RESOURCE_REF.equals(eventDDBean.getXpath()) ? 
//                                          BEAN_TYPE.ENTITY : BEAN_TYPE.MSGDRV));
//
//                    if (beanType != BEAN_TYPE.MSGDRV) { //sessions and entities
//                        Set beanNames = null;
//                        if (desc.length > 0  && "javax.sql.DataSource".equals(type[0])) { // NOI18N
//                            beanNames = getRelevantBeansDataRef(desc[0], name[0], eventDDBean.getRoot(), beanType);
//                            String jndiName = JBDeploymentConfiguration.JBOSS4_DATASOURCE_JNDI_PREFIX + resRefName;
//                            addResReference(jndiName, name[0], beanNames, beanType);
//                        }
//                        else
//                        if ("javax.mail.Session".equals(type[0])) { // NOI18N
//                            beanNames = getRelevantBeansMailRef(name[0], eventDDBean.getRoot(), beanType);
//                            addMailReference(name[0], beanNames, beanType);
//                        }
//                        else
//                        if ("javax.jms.ConnectionFactory".equals(type[0])) { // NOI18N
//                            beanNames = getRelevantBeansConnectionFactoryRef(name[0], eventDDBean.getRoot(), beanType);
//                            addConnectionFactoryReference(name[0], beanNames, beanType);
//                        }
//                    }
//                    else { // message-driven beans
//                        Map beans = null;
//                        if (desc.length > 0  && "javax.sql.DataSource".equals(type[0])) { // NOI18N
//                            beans = getRelevantMsgDrvBeansDataRef(desc[0], name[0], eventDDBean.getRoot());
//                            addMsgDrvResReference(desc[0], name[0], beans);
//                        }
//                        if ("javax.mail.Session".equals(type[0])) { // NOI18N
//                            beans = getRelevantMsgDrvBeansMailRef(name[0], eventDDBean.getRoot());
//                            addMsgDrvMailReference(name[0], beans);
//                        }
//                        else
//                        if ("javax.jms.ConnectionFactory".equals(type[0])) { // NOI18N
//                            beans = getRelevantMsgDrvBeansConnectionFactoryRef(name[0], eventDDBean.getRoot());
//                            addMsgDrvConnectionFactoryReference(name[0], beans);
//                        }
//                    }
//
//                } catch (ConfigurationException ce) {
//                    ErrorManager.getDefault().notify(ce);
//                }
//            }
//        }
//        else if (SESSION_EJB_REF.equals(eventDDBean.getXpath()) ||
//                 ENTITY_EJB_REF.equals(eventDDBean.getXpath()) ||
//                 MSGDRV_EJB_REF.equals(eventDDBean.getXpath())) 
//        {
//            String[] name = eventDDBean.getText("ejb-ref-name"); // NOI18N
//            String[] type = eventDDBean.getText("ejb-ref-type"); // NOI18N
//            if (name.length > 0 && type.length > 0 
//                    && ("Session".equals(type[0]) || "Entity".equals(type[0]))) { // NOI18N
//                try {
//                    //we don't know which bean contains the created ejb-ref,
//                    //thus we must find all beans containing the same ejb-ref
//                    //as the one just created
//                    BEAN_TYPE beanType = (SESSION_EJB_REF.equals(eventDDBean.getXpath()) ? 
//                                          BEAN_TYPE.SESSION : (ENTITY_EJB_REF.equals(eventDDBean.getXpath()) ? 
//                                          BEAN_TYPE.ENTITY : BEAN_TYPE.MSGDRV));
//
//                    if (beanType != BEAN_TYPE.MSGDRV) { //sessions and entities
//                        Set beanNames = getRelevantBeansEjbRef(name[0], eventDDBean.getRoot(), beanType);
//                        addEjbReference(name[0], beanNames, beanType);
//                    }
//                    else { // message-driven beans
//                        Map beans = getRelevantMsgDrvBeansEjbRef(name[0], eventDDBean.getRoot());
//                        addMsgDrvEjbReference(name[0], beans);
//                    }
//
//                } catch (ConfigurationException ce) {
//                    ErrorManager.getDefault().notify(ce);
//                }
//            }
//        }
//        else if (MSGDRV.equals(eventDDBean.getXpath())) {
//            if (isEJB3) { // do not generate MDB destination reference for EJB3 modules (issue #82452)
//                return;
//            }
//            
//            String[] name = eventDDBean.getText("ejb-name"); // NOI18N
//            String[] type = eventDDBean.getText("message-destination-type"); // NOI18N
//            String[] dest = eventDDBean.getText("message-destination-link"); // NOI18N
//            
//            if (name.length == 0)
//                return;
//            
//            if (dest.length == 0) {
//                tempEjbName = name[0];
//            } else {
//                try {
//                    addMDB(name[0], dest[0]);
//                } catch (ConfigurationException ce) {
//                    ErrorManager.getDefault().notify(ce);
//                }
//            }
//        }
//        else if (MSGDRV_MSG_DEST.equals(eventDDBean.getXpath())) { //is fired right after the MSGDRV Xpath event, too
//            if (isEJB3) { // do not generate MDB destination reference for EJB3 modules (issue #82452)
//                return;
//            }
//
//            if (tempEjbName == null) // MSGDRV was not fired before
//                return;
//            
//            try {
//                String dest = xpe.getBean().getText();
//                addMDB(tempEjbName, dest);
//            } catch (ConfigurationException ce) {
//                ErrorManager.getDefault().notify(ce);
//            } finally {
//                tempEjbName = null;
//            }
//        }
//        
//        else if (SESSION_MSG_DEST_REF.equals(eventDDBean.getXpath()) ||
//                 ENTITY_MSG_DEST_REF.equals(eventDDBean.getXpath()) ||
//                 MSGDRV_MSG_DEST_REF.equals(eventDDBean.getXpath()))
//        {
//            String[] name = eventDDBean.getText("message-destination-ref-name"); // NOI18N
//            String[] type = eventDDBean.getText("message-destination-type"); // NOI18N
//            
//            if (name.length > 0) {
//                
//                String destPrefix = "";
//                if (type.length > 0) {
//                    if (type[0].equals("javax.jms.Queue")) // NOI18N
//                        destPrefix = JBOSS4_MSG_QUEUE_JNDI_PREFIX;
//                    else
//                    if (type[0].equals("javax.jms.Topic")) // NOI18N
//                        destPrefix = JBOSS4_MSG_TOPIC_JNDI_PREFIX;
//                }
//                
//                try {
//                    //we don't know which bean contains the created resource-ref,
//                    //thus we must find all beans containing the same resource-ref
//                    //as the one just created
//                    BEAN_TYPE beanType = (SESSION_MSG_DEST_REF.equals(eventDDBean.getXpath()) ? 
//                                          BEAN_TYPE.SESSION : (ENTITY_MSG_DEST_REF.equals(eventDDBean.getXpath()) ? 
//                                          BEAN_TYPE.ENTITY : BEAN_TYPE.MSGDRV));
//
//                    if (beanType != BEAN_TYPE.MSGDRV) { //sessions and entities
//                        Set beanNames = getRelevantBeansMsgDestRef(name[0], eventDDBean.getRoot(), beanType);
//                        addMsgDestReference(name[0], destPrefix, beanNames, beanType);
//                    }
//                    else { // message-driven beans
//                        Map beans = getRelevantMsgDrvBeansMsgDestRef(name[0], eventDDBean.getRoot());
//                        addMsgDrvMsgDestReference(name[0], destPrefix, beans);
//                    }
//                
//                } catch (ConfigurationException ce) {
//                    ErrorManager.getDefault().notify(ce);
//                }
//            }
//        }
//    }
    
    /**
     * Searches for the beans of the give type referring to the given data source. 
     * It returns the names for the beans found.
     *
     * @param desc searched data source description tag value
     * @param resRefName searched data source (res-ref-name tag value)
     * @param root root bean to search from
     * @param beanType type of bean to search for
     *
     * @return set of the names (ejb-name) of the beans refering to the resource-ref 
     * with the same description and res-ref-name
     */
    private Set getRelevantBeansDataRef(String desc, String resRefName, DDBeanRoot root, BEAN_TYPE beanType) {
        
        Set beanNames = new HashSet();
        DDBean[] beans = root.getChildBean("/ejb-jar/enterprise-beans/" + beanType.getType()); // NOI18N
        for (int i = 0; i < beans.length; i++) {
            DDBean bean = beans[i];
            DDBean[] resRefs = bean.getChildBean("resource-ref");
            for (int j = 0; resRefs != null && j < resRefs.length; j++) {
                String[] descs = resRefs[j].getText("description"); // NOI18N
                String[] names = resRefs[j].getText("res-ref-name"); // NOI18N
                String[] types = resRefs[j].getText("res-type");     // NOI18N
                if (descs.length > 0  && names.length > 0 && types.length > 0 &&
                    descs[0].equals(desc) && names[0].equals(resRefName) && "javax.sql.DataSource".equals(types[0])) { // NOI18N
                    //store bean's ejb-name tag value
                    beanNames.add(bean.getChildBean("ejb-name")[0].getText()); // NOI18N
                    break;
                }
            }
        }
        
        return beanNames;
    }
    
    /**
     * Searches for the message-driven beans referring to the given resource. 
     * It returns the names and message destination links for the beans found.
     *
     * @param desc searched data source description tag value
     * @param resRefName searched res-ref-name tag value
     * @param root root bean to search from
     *
     * @return map where the keys are the bean names (ejb-name) and 
     * the values are the message destinations (message-destination-link)
     */
    private Map getRelevantMsgDrvBeansDataRef(String desc, String resRefName, DDBeanRoot root) {
        
        HashMap/*<String, String>*/ beanMap = new HashMap(); // maps ejb-name to message-destination-link
        DDBean[] beans = root.getChildBean("/ejb-jar/enterprise-beans/message-driven"); // NOI18N
        for (int i = 0; i < beans.length; i++) {
            DDBean bean = beans[i];
            DDBean[] resRefs = bean.getChildBean("resource-ref");
            for (int j = 0; resRefs != null && j < resRefs.length; j++) {
                String[] descs = resRefs[j].getText("description"); // NOI18N
                String[] names = resRefs[j].getText("res-ref-name"); // NOI18N
                String[] types = resRefs[j].getText("res-type");     // NOI18N
                if (descs.length > 0  && names.length > 0 && types.length > 0 &&
                    descs[0].equals(desc) && names[0].equals(resRefName) && "javax.sql.DataSource".equals(types[0])) { // NOI18N
                    //store bean's ejb-name and message-destination-link tag values
                    String key = bean.getChildBean("ejb-name")[0].getText();
                    String value = "";
                    if (bean.getChildBean("message-destination-link").length > 0) // NOI18N
                        value = bean.getChildBean("message-destination-link")[0].getText();
                    beanMap.put(key, value); // NOI18N
                    break;
                }
            }
        }
        
        return beanMap;
    }
    
    /**
     * @param ejbRefName searched ejb-ref-name tag value
     * @param root root bean to search from
     * @param beanType type of bean to search for
     *
     * @return set of the names (ejb-name) of the beans refering to the ejb-ref 
     * with the same ejb-ref-name
     */
    private Set getRelevantBeansEjbRef(String ejbRefName, DDBeanRoot root, BEAN_TYPE beanType) {
        
        Set beanNames = new HashSet();
        DDBean[] beans = root.getChildBean("/ejb-jar/enterprise-beans/" + beanType.getType()); // NOI18N
        for (int i = 0; i < beans.length; i++) {
            DDBean bean = beans[i];
            DDBean[] ejbRefs = bean.getChildBean("ejb-ref");
            for (int j = 0; ejbRefs != null && j < ejbRefs.length; j++) {
                String[] names = ejbRefs[j].getText("ejb-ref-name"); // NOI18N
                String[] types = ejbRefs[j].getText("ejb-ref-type"); // NOI18N
                if (names.length > 0 && types.length > 0 && names[0].equals(ejbRefName) 
                    && ("Session".equals(types[0]) || "Entity".equals(types[0]))) { // NOI18N
                    //store bean's ejb-name tag value
                    beanNames.add(bean.getChildBean("ejb-name")[0].getText()); // NOI18N
                    break;
                }
            }
        }
        
        return beanNames;
    }
    
    interface JbossModifier {
        public void modify(Jboss modifiedJboss);
    }
    
    /**
     * Searches for the message-driven beans referring to the given ejb. 
     * It returns the names and message destination links for the beans found.
     *
     * @param ejbRefName searched ejb-ref-name tag value
     * @param root root bean to search from
     *
     * @return map where the keys are the bean names (ejb-name) and 
     * the values are the message destinations (message-destination-link)
     */
    private Map getRelevantMsgDrvBeansEjbRef(String ejbRefName, DDBeanRoot root) {
        
        HashMap/*<String, String>*/ beanMap = new HashMap(); // maps ejb-name to message-destination-link
        DDBean[] beans = root.getChildBean("/ejb-jar/enterprise-beans/message-driven"); // NOI18N
        for (int i = 0; i < beans.length; i++) {
            DDBean bean = beans[i];
            DDBean[] ejbRefs = bean.getChildBean("ejb-ref");
            for (int j = 0; ejbRefs != null && j < ejbRefs.length; j++) {
                String[] names = ejbRefs[j].getText("ejb-ref-name"); // NOI18N
                String[] types = ejbRefs[j].getText("ejb-ref-type"); // NOI18N
                if (names.length > 0 && types.length > 0 && names[0].equals(ejbRefName) 
                    && ("Session".equals(types[0]) || "Entity".equals(types[0]))) { // NOI18N
                    //store bean's ejb-name and message-destination-link tag values
                    String key = bean.getChildBean("ejb-name")[0].getText();
                    String value = "";
                    if (bean.getChildBean("message-destination-link").length > 0) // NOI18N
                        value = bean.getChildBean("message-destination-link")[0].getText();
                    beanMap.put(key, value); // NOI18N
                    break;
                }
            }
        }
        
        return beanMap;
    }
    
    /**
     * Searches for the beans of the give type referring to the given mail service. 
     * It returns the names for the beans found.
     *
     * @param resRefName searched mail service (res-ref-name tag value)
     * @param root root bean to search from
     * @param beanType type of bean to search for
     *
     * @return set of the names (ejb-name) of the beans refering to the resource-ref 
     * with the same description and res-ref-name
     */
    private Set getRelevantBeansMailRef(String resRefName, DDBeanRoot root, BEAN_TYPE beanType) {
        
        Set beanNames = new HashSet();
        DDBean[] beans = root.getChildBean("/ejb-jar/enterprise-beans/" + beanType.getType()); // NOI18N
        for (int i = 0; i < beans.length; i++) {
            DDBean bean = beans[i];
            DDBean[] resRefs = bean.getChildBean("resource-ref");
            for (int j = 0; resRefs != null && j < resRefs.length; j++) {
                String[] names = resRefs[j].getText("res-ref-name"); // NOI18N
                String[] types = resRefs[j].getText("res-type");     // NOI18N
                if (names.length > 0 && types.length > 0 &&
                    names[0].equals(resRefName) && "javax.mail.Session".equals(types[0])) { // NOI18N
                    //store bean's ejb-name tag value
                    beanNames.add(bean.getChildBean("ejb-name")[0].getText()); // NOI18N
                    break;
                }
            }
        }
        
        return beanNames;
    }
    
    /**
     * Searches for the message-driven beans referring to the given mail service. 
     * It returns the names and message destination links for the beans found.
     *
     * @param resRefName searched res-ref-name tag value
     * @param root root bean to search from
     *
     * @return map where the keys are the bean names (ejb-name) and 
     * the values are the message destinations (message-destination-link)
     */
    private Map getRelevantMsgDrvBeansMailRef(String resRefName, DDBeanRoot root) {
        
        HashMap/*<String, String>*/ beanMap = new HashMap(); // maps ejb-name to message-destination-link
        DDBean[] beans = root.getChildBean("/ejb-jar/enterprise-beans/message-driven"); // NOI18N
        for (int i = 0; i < beans.length; i++) {
            DDBean bean = beans[i];
            DDBean[] resRefs = bean.getChildBean("resource-ref");
            for (int j = 0; resRefs != null && j < resRefs.length; j++) {
                String[] names = resRefs[j].getText("res-ref-name"); // NOI18N
                String[] types = resRefs[j].getText("res-type");     // NOI18N
                if (names.length > 0 && types.length > 0 &&
                    names[0].equals(resRefName) && "javax.mail.Session".equals(types[0])) { // NOI18N
                    //store bean's ejb-name and message-destination-link tag values
                    String key = bean.getChildBean("ejb-name")[0].getText();
                    String value = "";
                    if (bean.getChildBean("message-destination-link").length > 0) // NOI18N
                        value = bean.getChildBean("message-destination-link")[0].getText();
                    beanMap.put(key, value); // NOI18N
                    break;
                }
            }
        }
        
        return beanMap;
    }
    
    /**
     * Searches for the beans of the given type referring to the given connection factory. 
     * It returns the names for the beans found.
     *
     * @param resRefName searched connection factory (res-ref-name tag value)
     * @param root root bean to search from
     * @param beanType type of bean to search for
     *
     * @return set of the names (ejb-name) of the beans refering to the resource-ref 
     * with the same res-ref-name
     */
    private Set getRelevantBeansConnectionFactoryRef(String resRefName, DDBeanRoot root, BEAN_TYPE beanType) {
        
        Set beanNames = new HashSet();
        DDBean[] beans = root.getChildBean("/ejb-jar/enterprise-beans/" + beanType.getType()); // NOI18N
        for (int i = 0; i < beans.length; i++) {
            DDBean bean = beans[i];
            DDBean[] resRefs = bean.getChildBean("resource-ref");
            for (int j = 0; resRefs != null && j < resRefs.length; j++) {
                String[] names = resRefs[j].getText("res-ref-name"); // NOI18N
                String[] types = resRefs[j].getText("res-type");     // NOI18N
                if (names.length > 0 && types.length > 0 &&
                    names[0].equals(resRefName) && "javax.jms.ConnectionFactory".equals(types[0])) { // NOI18N
                    //store bean's ejb-name tag value
                    beanNames.add(bean.getChildBean("ejb-name")[0].getText()); // NOI18N
                    break;
                }
            }
        }
        
        return beanNames;
    }

    /**
     * Searches for the message-driven beans referring to the given connection factory. 
     * It returns the names and message destination links for the beans found.
     *
     * @param resRefName searched res-ref-name tag value
     * @param root root bean to search from
     *
     * @return map where the keys are the bean names (ejb-name) and 
     * the values are the message destinations (message-destination-link)
     */
    private Map getRelevantMsgDrvBeansConnectionFactoryRef(String resRefName, DDBeanRoot root) {
        
        HashMap/*<String, String>*/ beanMap = new HashMap(); // maps ejb-name to message-destination-link
        DDBean[] beans = root.getChildBean("/ejb-jar/enterprise-beans/message-driven"); // NOI18N
        for (int i = 0; i < beans.length; i++) {
            DDBean bean = beans[i];
            DDBean[] resRefs = bean.getChildBean("resource-ref");
            for (int j = 0; resRefs != null && j < resRefs.length; j++) {
                String[] names = resRefs[j].getText("res-ref-name"); // NOI18N
                String[] types = resRefs[j].getText("res-type");     // NOI18N
                if (names.length > 0 && types.length > 0 &&
                    names[0].equals(resRefName) && "javax.jms.ConnectionFactory".equals(types[0])) { // NOI18N
                    //store bean's ejb-name and message-destination-link tag values
                    String key = bean.getChildBean("ejb-name")[0].getText();
                    String value = "";
                    if (bean.getChildBean("message-destination-link").length > 0) // NOI18N
                        value = bean.getChildBean("message-destination-link")[0].getText();
                    beanMap.put(key, value); // NOI18N
                    break;
                }
            }
        }
        
        return beanMap;
    }
    
    /**
     * @param msgDestRefName searched message-destination-ref tag value
     * @param root root bean to search from
     * @param beanType type of bean to search for
     *
     * @return set of the names (ejb-name) of the beans refering to the message-destination-ref 
     * with the same message-destination-ref-name
     */
    private Set getRelevantBeansMsgDestRef(String msgDestRefName, DDBeanRoot root, BEAN_TYPE beanType) {
        
        Set beanNames = new HashSet();
        DDBean[] beans = root.getChildBean("/ejb-jar/enterprise-beans/" + beanType.getType()); // NOI18N
        for (int i = 0; i < beans.length; i++) {
            DDBean bean = beans[i];
            DDBean[] msgDestRefs = bean.getChildBean("message-destination-ref"); // NOI18N
            for (int j = 0; msgDestRefs != null && j < msgDestRefs.length; j++) {
                String[] names = msgDestRefs[j].getText("message-destination-ref-name"); // NOI18N
                if (names.length > 0 && names[0].equals(msgDestRefName)) {
                    //store bean's ejb-name tag value
                    beanNames.add(bean.getChildBean("ejb-name")[0].getText()); // NOI18N
                    break;
                }
            }
        }
        
        return beanNames;
    }
    
    /**
     * @param msgDestRefName searched message-destination-ref tag value
     * @param root root bean to search from
     * @param beanType type of bean to search for
     *
     * @return set of the names (ejb-name) of the beans refering to the message-destination-ref 
     * with the same message-destination-ref-name
     */
    private Map getRelevantMsgDrvBeansMsgDestRef(String msgDestRefName, DDBeanRoot root) {
        
        HashMap/*<String, String>*/ beanMap = new HashMap(); // maps ejb-name to message-destination-link
        DDBean[] beans = root.getChildBean("/ejb-jar/enterprise-beans/message-driven"); // NOI18N
        for (int i = 0; i < beans.length; i++) {
            DDBean bean = beans[i];
            DDBean[] msgDestRefs = bean.getChildBean("message-destination-ref"); // NOI18N
            for (int j = 0; msgDestRefs != null && j < msgDestRefs.length; j++) {
                String[] names = msgDestRefs[j].getText("message-destination-ref-name"); // NOI18N
                if (names.length > 0 && names[0].equals(msgDestRefName)) {
                    //store bean's ejb-name tag value
                    String key = bean.getChildBean("ejb-name")[0].getText();
                    String value = "";
                    if (bean.getChildBean("message-destination-link").length > 0) // NOI18N
                        value = bean.getChildBean("message-destination-link")[0].getText();
                    beanMap.put(key, value); // NOI18N
                    break;
                }
            }
        }
        
        return beanMap;
    }

    public void bindDatasourceReferenceForEjb(String ejbName, String ejbType, 
            String referenceName, String jndiName) throws ConfigurationException {
        
        Set beanNames = new HashSet();
        beanNames.add(ejbName);
        if (org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.SESSION.equals(ejbType)) {
            addResReference(jndiName, referenceName, beanNames, BEAN_TYPE.SESSION);
        }
        else
        if (org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.ENTITY.equals(ejbType)) {
            addResReference(jndiName, referenceName, beanNames, BEAN_TYPE.ENTITY);
        }
        else
        if (org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.MESSAGE_DRIVEN.equals(ejbType)) {
            addMsgDrvResReference(jndiName, referenceName, ejbName);
        }

    }
    
    public String findDatasourceJndiNameForEjb(String ejbName, String referenceName) throws ConfigurationException {

        EnterpriseBeans beans = getJboss().getEnterpriseBeans();
        if (beans == null) {
            return null;
        }
        
        Session[] sessions = beans.getSession();
        for (Session session : sessions) {
            if (ejbName.equals(session.getEjbName())) {
                ResourceRef[] resourceRefs = session.getResourceRef();
                for (ResourceRef resourceRef : resourceRefs) {
                    String rrn = resourceRef.getResRefName();
                    if (referenceName.equals(rrn)) {
                        String jndiName = resourceRef.getJndiName();
                        if (jndiName != null) {
                            return JBossDatasource.getJndiName(jndiName);
                        }
                    }
                }
                return null;
            }
        }

        Entity[] entities = beans.getEntity();
        for (Entity entity : entities) {
            if (ejbName.equals(entity.getEjbName())) {
                ResourceRef[] resourceRefs = entity.getResourceRef();
                for (ResourceRef resourceRef : resourceRefs) {
                    String rrn = resourceRef.getResRefName();
                    if (referenceName.equals(rrn)) {
                        return resourceRef.getJndiName();
                    }
                }
                return null;
            }
        }

        MessageDriven[] mdbs = beans.getMessageDriven();
        for (MessageDriven mdb : mdbs) {
            if (ejbName.equals(mdb.getEjbName())) {
                ResourceRef[] resourceRefs = mdb.getResourceRef();
                for (ResourceRef resourceRef : resourceRefs) {
                    String rrn = resourceRef.getResRefName();
                    if (referenceName.equals(rrn)) {
                        return resourceRef.getJndiName();
                    }
                }
                return null;
            }
        }
        
        return null;
    }    
    
    
    /**
     * Add a new data source reference to the beans of the given type without it.
     * 
     * @param jndiName JNDI name of the resource
     * @param resRefName data source reference name
     * @param beanNames the beans (ejb-name value) which might need to add data source reference specified by resRefName
     * @param beanType type of bean to add data source reference to
     */
    private void addResReference(final String jndiName, final String resRefName, final Set beanNames, final BEAN_TYPE beanType) 
    throws ConfigurationException 
    {
        modifyJboss(new JbossModifier() {
           public void modify(Jboss modifiedJboss) {
               JBossDataSourceRefModifier.modify(modifiedJboss, resRefName, beanNames, beanType, jndiName);
           }
        });
    }

    /**
     * Add a new resource reference to the message-driven bean.
     * 
     * @param jndiName JNDI name of the resource
     * @param resRefName resource reference name
     * @param mdbName the MDB (ejb-name) which might need to add resource reference specified by resRefName
     * which might need to add resource reference specified by resRefName
     */
    private void addMsgDrvResReference(final String jndiName, final String resRefName, final String mdbName) 
    throws ConfigurationException 
    {
        modifyJboss(new JbossModifier() {
           public void modify(Jboss modifiedJboss) {
               JBossDataSourceRefModifier.modifyMsgDrv(modifiedJboss, resRefName, mdbName, jndiName);
           }
        });
    }

    public void bindEjbReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String referencedEjbName) throws ConfigurationException {
    
        if (Double.parseDouble(j2eeModule.getModuleVersion()) > 2.1) {
            return;
        }
        
        Set beanNames = new HashSet();
        beanNames.add(ejbName);
        if (org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.SESSION.equals(ejbType)) {
            addEjbReference(referenceName, referencedEjbName, beanNames, BEAN_TYPE.SESSION);
        }
        else
        if (org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.ENTITY.equals(ejbType)) {
            addEjbReference(referenceName, referencedEjbName, beanNames, BEAN_TYPE.ENTITY);
        }
        else
        if (org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.MESSAGE_DRIVEN.equals(ejbType)) {
            addMsgDrvEjbReference(referenceName, referencedEjbName, ejbName);
        }
        
    }
    
    /**
     * Add a new ejb reference to the beans of the given type without it.
     * 
     * @param ejbRefName ejb reference name
     * @param referencedEjbName name of the referenced EJB
     * @param beanNames the beans (ejb-name value) which might need to add ejb reference specified by ejbRefName
     * @param beanType type of bean to add ejb reference to
     */
    private void addEjbReference(final String ejbRefName, final String referencedEjbName,
            final Set beanNames, final BEAN_TYPE beanType) throws ConfigurationException {

        modifyJboss(new JbossModifier() {
           public void modify(Jboss modifiedJboss) {
               JBossEjbRefModifier.modify(modifiedJboss, ejbRefName, referencedEjbName, beanNames, beanType);
           }
        });
    }

    /**
     * Add a new ejb reference to the message-driven beans without it.
     * 
     * @param ejbRefName ejb reference name
     * @param referencedEjbName name of the referenced EJB
     * @param mdbName the MDB (ejb-name value) which might need to add EJB
     *        reference specified by ejbRefName
     */
    private void addMsgDrvEjbReference(final String ejbRefName, final String referencedEjbName, final String mdbName) 
    throws ConfigurationException 
    {
        modifyJboss(new JbossModifier() {
           public void modify(Jboss modifiedJboss) {
               JBossEjbRefModifier.modifyMsgDrv(modifiedJboss, mdbName, ejbRefName, referencedEjbName);
           }
        });
    }

    /**
     * Add a new mail service reference to the beans of the given type without it.
     * 
     * @param resRefName mail service reference name
     * @param beanNames the beans (ejb-name value) which might need to add mail service reference specified by resRefName
     * @param beanType type of bean to add mail service reference to
     */
    private void addMailReference(final String resRefName, final Set beanNames, final BEAN_TYPE beanType) 
    throws ConfigurationException 
    {
        modifyJboss(new JbossModifier() {
           public void modify(Jboss modifiedJboss) {
               String jndiName = MAIL_SERVICE_JNDI_NAME_JB4;
               JBossDataSourceRefModifier.modify(modifiedJboss, resRefName, beanNames, beanType, jndiName);
           }
        });
    }

    /**
     * Add a new mail service reference to the message-driven beans without it.
     * 
     * @param resRefName mail service reference name
     * @param beans the bean names (ejb-name) mapped to the message destinations (message-destination-link)
     * which might need to add mail service reference specified by resRefName
     */
    private void addMsgDrvMailReference(final String resRefName, final Map beans) 
    throws ConfigurationException 
    {
        modifyJboss(new JbossModifier() {
           public void modify(Jboss modifiedJboss) {
               String jndiName = MAIL_SERVICE_JNDI_NAME_JB4;
               JBossDataSourceRefModifier.modifyMsgDrv(modifiedJboss, resRefName, beans, jndiName);
           }
        });
    }

    public void bindMdbToMessageDestination(String mdbName, String name, MessageDestination.Type type) throws ConfigurationException {
    
        if (Double.parseDouble(j2eeModule.getModuleVersion()) > 2.1) {
            return;
        }
        
        addMDB(mdbName, name, type);
    }
    
    public String findMessageDestinationName(String mdbName) throws ConfigurationException {

        EnterpriseBeans beans = getJboss().getEnterpriseBeans();
        if (beans == null) {
            return null;
        }
        
        MessageDriven[] mdbs = beans.getMessageDriven();
        for (MessageDriven mdb : mdbs) {
            if (mdbName.equals(mdb.getEjbName())) {
                String destJndiName = mdb.getDestinationJndiName();
                if (destJndiName != null) {
                    if (destJndiName.startsWith(JBossMessageDestination.QUEUE_PREFIX) || destJndiName.startsWith(JBossMessageDestination.TOPIC_PREFIX)) {
                        return destJndiName.substring(6); // JBossMessageDestination.QUEUE_PREFIX.length() == JBossMessageDestination.TOPIC_PREFIX.length() == 6
                    }
                    else {
                        Logger.getLogger("global").log(Level.INFO, NbBundle.getMessage(EjbDeploymentConfiguration.class, "MSG_NoPrefix", destJndiName));
                    }
                }
                return null;
            }
        }
        
        return null;
    }

    /**
     * Add MDB record.
     * 
     * @param name MDB name (ejb-name)
     * @param dest MDB destination (message-destination-link)
     */
    private void addMDB(final String name, final String destName, final MessageDestination.Type destType) 
    throws ConfigurationException {
        
        modifyJboss(new JbossModifier() {
            public void modify(Jboss modifiedJboss) {

                EnterpriseBeans eb = modifiedJboss.getEnterpriseBeans();
                if (eb == null) {
                    eb = new EnterpriseBeans();
                    modifiedJboss.setEnterpriseBeans(eb);
                }
                
                // check whether mdb not already defined
                MessageDriven[] mdbs = eb.getMessageDriven();
                for (int i = 0; i < mdbs.length; i++) {
                    String en = mdbs[i].getEjbName();
                    if (name.equals(en)) {
                        // already exists
                        return;
                    }
                }

                //if it doesn't exist yet, create a new one
                MessageDriven mdb = new MessageDriven();
                mdb.setEjbName(name);
                if (MessageDestination.Type.QUEUE.equals(destType)) {
                    mdb.setDestinationJndiName(JBossMessageDestination.QUEUE_PREFIX + destName); // NOI18N
                }
                else
                if (MessageDestination.Type.TOPIC.equals(destType)) {
                    mdb.setDestinationJndiName(JBossMessageDestination.TOPIC_PREFIX + destName); // NOI18N
                }
                eb.addMessageDriven(mdb);
            }
        });
    }

    /**
     * Add a new connection factory reference to the beans of the given type without it.
     * 
     * @param resRefName connection factory reference name
     * @param beanNames the beans (ejb-name value) which might need to add connection factory reference specified by resRefName
     * @param beanType type of bean to add connection factory reference to
     */
    private void addConnectionFactoryReference(final String resRefName, final Set beanNames, final BEAN_TYPE beanType) 
    throws ConfigurationException 
    {
        modifyJboss(new JbossModifier() {
           public void modify(Jboss modifiedJboss) {
               String jndiName = MessageDestinationSupport.CONN_FACTORY_JNDI_NAME_JB4;
               JBossDataSourceRefModifier.modify(modifiedJboss, resRefName, beanNames, beanType, jndiName);
           }
        });
    }

    /**
     * Add a new connection factory reference to the message-driven beans without it.
     * 
     * @param connectionFactoryName connection factory reference name
     * @param mdbName the MDB (ejb-name) which might need to add connection factory reference specified by resRefName
     */
    private void addMsgDrvConnectionFactoryReference(final String connectionFactoryName, final String mdbName) 
    throws ConfigurationException 
    {
        modifyJboss(new JbossModifier() {
           public void modify(Jboss modifiedJboss) {
               String jndiName = MessageDestinationSupport.CONN_FACTORY_JNDI_NAME_JB4;
               JBossDataSourceRefModifier.modifyMsgDrv(modifiedJboss, connectionFactoryName, mdbName, jndiName);
           }
        });
    }
    
    public void bindMessageDestinationReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String connectionFactoryName,
            String destName, MessageDestination.Type type) throws ConfigurationException {
    
        Set beanNames = new HashSet();
        beanNames.add(ejbName);
        
        String destPrefix = null;
        if (MessageDestination.Type.QUEUE.equals(type)) {
            destPrefix = JBossMessageDestination.QUEUE_PREFIX;
        }
        else
        if (MessageDestination.Type.TOPIC.equals(type)) {
            destPrefix = JBossMessageDestination.TOPIC_PREFIX;
        }
        
        if (org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.SESSION.equals(ejbType)) {
            addConnectionFactoryReference(connectionFactoryName, beanNames, BEAN_TYPE.SESSION);
            addMsgDestReference(referenceName, destPrefix, destName, beanNames, BEAN_TYPE.SESSION);
        }
        else
        if (org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.ENTITY.equals(ejbType)) {
            addConnectionFactoryReference(connectionFactoryName, beanNames, BEAN_TYPE.ENTITY);
            addMsgDestReference(referenceName, destPrefix, destName, beanNames, BEAN_TYPE.ENTITY);
        }
        else
        if (org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.MESSAGE_DRIVEN.equals(ejbType)) {
            addMsgDrvConnectionFactoryReference(connectionFactoryName, ejbName);
            addMsgDrvMsgDestReference(referenceName, destPrefix, destName, ejbName);
        }
        
    }
    
    
    /**
     * Add a new message destination reference to the beans of the given type without it.
     * 
     * @param msgDestRefName message destination reference name
     * @param destName message destination name
     * @param detPrefix message destination prefix (queue/ ot topic/)
     * @param beanNames the beans (ejb-name value) which might need to add message destination reference specified by msgDestRefName
     * @param beanType type of bean to add message destination reference to
     */
    private void addMsgDestReference(final String msgDestRefName, final String destPrefix, final String destName,
                                     final Set beanNames, final BEAN_TYPE beanType) throws ConfigurationException 
    {
        modifyJboss(new JbossModifier() {
           public void modify(Jboss modifiedJboss) {
               JBossMsgDestRefModifier.modify(modifiedJboss, msgDestRefName, beanNames, beanType, destPrefix, destName);
           }
        });
    }

    /**
     * Add a new message destination reference to the message driven beans without it.
     * 
     * @param msgDestRefName message destination reference name
     * @param destName message destination name
     * @param destPrefix message destination prefix (queue/ ot topic/)
     * @param mdbName the MDB (ejb-name value) which might need to add 
     *        message destination reference specified by msgDestRefName
     */
    private void addMsgDrvMsgDestReference(final String msgDestRefName, final String destPrefix,
                                     final String destName, final String mdbName) throws ConfigurationException 
    {
        modifyJboss(new JbossModifier() {
           public void modify(Jboss modifiedJboss) {
               JBossMsgDestRefModifier.modifyMsgDrv(modifiedJboss, msgDestRefName, mdbName, destPrefix, destName);
           }
        });
    }

    /**
     * Perform jboss changes defined by the jboss modifier. Update editor
     * content and save changes, if appropriate.
     *
     * @param modifier
     */
    private void modifyJboss(JbossModifier modifier)
    throws ConfigurationException 
    {
        assert deploymentDescriptorDO != null : "DataObject has not been initialized yet"; // NIO18N
        try {
            // get the document
            EditorCookie editor = (EditorCookie)deploymentDescriptorDO.getCookie(EditorCookie.class);
            StyledDocument doc = editor.getDocument();
            if (doc == null) {
                doc = editor.openDocument();
            }
            
            // get the up-to-date model
            Jboss newJboss = null;
            try {
                // try to create a graph from the editor content
                byte[] docString = doc.getText(0, doc.getLength()).getBytes();
                newJboss = Jboss.createGraph(new ByteArrayInputStream(docString));
            } catch (RuntimeException e) {
                Jboss oldJboss = getJboss();
                if (oldJboss == null) {
                    // neither the old graph is parseable, there is not much we can do here
                    // TODO: should we notify the user?
                    String msg = NbBundle.getMessage(JBDeploymentConfiguration.class, "MSG_jbossXmlCannotParse", jbossFile.getAbsolutePath());
                    throw new ConfigurationException(msg);
                }
                // current editor content is not parseable, ask whether to override or not
                NotifyDescriptor notDesc = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_jbossXmlNotValid", "jboss.xml"),
                        NotifyDescriptor.OK_CANCEL_OPTION);
                Object result = DialogDisplayer.getDefault().notify(notDesc);
                if (result == NotifyDescriptor.CANCEL_OPTION) {
                    // keep the old content
                    return;
                }
                // use the old graph
                newJboss = oldJboss;
            }
            
            // perform changes
            modifier.modify(newJboss);
            
            // save, if appropriate
            boolean modified = deploymentDescriptorDO.isModified();
            ResourceConfigurationHelper.replaceDocument(doc, newJboss);
            if (!modified) {
                SaveCookie cookie = (SaveCookie)deploymentDescriptorDO.getCookie(SaveCookie.class);
                if (cookie != null) {
                    cookie.save();
                }
            }
            synchronized (this) {
                jboss = newJboss;
            }
        } catch (BadLocationException ble) {
            // this should not occur, just log it if it happens
            Exceptions.printStackTrace(ble);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(EjbDeploymentConfiguration.class, "MSG_CannotUpdateFile", jbossFile.getAbsolutePath());
            throw new ConfigurationException(msg, ioe);
        }
    }
    
    public void save(OutputStream os) throws ConfigurationException {
        Jboss jboss = getJboss();
        if (jboss == null) {
            String msg = NbBundle.getMessage(EjbDeploymentConfiguration.class, "MSG_cannotSaveNotParseableConfFile", jbossFile.getAbsolutePath());
            throw new ConfigurationException(msg);
        }
        try {
            jboss.write(os);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(EjbDeploymentConfiguration.class, "MSG_CannotUpdateFile", jbossFile.getAbsolutePath());
            throw new ConfigurationException(msg, ioe);
        }
    }
    
    // private helper methods -------------------------------------------------
    
    /**
     * Generate Jboss graph.
     */
    private Jboss generateJboss() {
        return new Jboss();
    }
}
