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
package org.netbeans.modules.j2ee.sun.ide.sunresources.resourcesloader;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.InputSource;


import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;

import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.JmsResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBean;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ConnPoolBeanDataNode;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.DataSourceBean;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.DataSourceBeanDataNode;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.JMSBean;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.JMSBeanDataNode;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.JavaMailSessionBean;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.JavaMailSessionBeanDataNode;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.PersistenceManagerBean;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.PersistenceManagerBeanDataNode;

import org.openide.filesystems.*;
import org.openide.util.WeakListeners;

/** Represents a SunResource object in the Repository.
 *
 * @author nityad
 */
@NbBundle.Messages("GlassfishRecognizer=GlassFish Resources Files")
@MIMEResolver.ExtensionRegistration(
    displayName="#GlassfishRecognizer",
    extension="sun-resource",
    position=890,
    mimeType="text/x-sun-resource+xml"
)
public class SunResourceDataObject extends XMLDataObject implements FileChangeListener { // extends MultiDataObject{
    private static final Logger LOG = Logger.getLogger(SunResourceDataObject.class.getName());

    private static String JDBC_CP = "jdbc-connection-pool"; //NOI18N
    private static String JDBC_DS = "jdbc-resource"; //NOI18N
    private static String PMF = "persistence-manager-factory-resource"; //NOI18N
    private static String MAIL = "mail-resource"; //NOI18N
    private static String JMS = "jms-resource"; //NOI18N

    private transient ValidateXMLCookie validateCookie = null;
    private transient CheckXMLCookie checkCookie = null;
    
    ConnPoolBean cpBean = null;
    DataSourceBean dsBean = null;
    PersistenceManagerBean pmfBean = null;
    JavaMailSessionBean mailBean = null;
    JMSBean jmsBean = null;
    
    String resType;

    /**
     * Verify if array is not empty and contains at least one element.
     * <p/>
     * @param arr Array to be verified.
     * @return Value of <code>true</code> when <code>arr</code> contains
     *         at least one element or <code>false</code> when <code>arr</code>
     *         contains no elements or is <code>null</code>.
     */
    private static boolean notEmpty(Object[] arr) {
        return arr != null && arr.length > 0;
    }

    public SunResourceDataObject(FileObject pf, SunResourceDataLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        pf.addFileChangeListener((FileChangeListener) WeakListeners.create(FileChangeListener.class, this, pf));
        
        resType = getResource(pf);
    }
       
    public <T extends Node.Cookie> T getCookie(Class<T> c) {
        Node.Cookie retValue = null;
        if (ValidateXMLCookie.class.isAssignableFrom(c)) {
            if (validateCookie == null) {
                InputSource in = DataObjectAdapters.inputSource(this);
                validateCookie = new ValidateXMLSupport(in);
            }
            return (T) validateCookie;
        } else if (CheckXMLCookie.class.isAssignableFrom(c)) {
            if (checkCookie == null) {
                InputSource in = DataObjectAdapters.inputSource(this);
                checkCookie = new CheckXMLSupport(in);
            }
            return (T) checkCookie;
        }
        
        if (retValue == null) {
            retValue = super.getCookie(c);
        }
        return (T) retValue;
    }
    
    
    public HelpCtx getHelpCtx() {
        return null; // HelpCtx.DEFAULT_HELP;
        // If you add context help, change to:
        // return new HelpCtx(SunResourceDataObject.class);
    }
    
    protected Node createNodeDelegate() {
        if(resType != null){
            if(this.resType.equals(this.JDBC_CP)){
                Node node = new ConnPoolBeanDataNode(this, getPool());
                return node;
            }if(this.resType.equals(this.JDBC_DS)){
                Node node = new DataSourceBeanDataNode(this, getDataSource());
                return node;
            }if(this.resType.equals(this.PMF)){
                Node node = new PersistenceManagerBeanDataNode(this, getPersistenceManager());
                return node;
            }if(this.resType.equals(this.MAIL)){
                Node node = new JavaMailSessionBeanDataNode(this, getMailSession());
                return node;
            }if(this.resType.equals(this.JMS)){    
                Node node = new JMSBeanDataNode(this, getJMS());
                return node;
            }else{
                String mess = NbBundle.getMessage(SunResourceDataObject.class, "Info_notSunResource"); //NOI18N
                LOG.log(Level.INFO, mess);
                return new SunResourceDataNode(this);
            }    
        }else{
            return new SunResourceDataNode(this);
        }   
    }
    
    private String getResource(FileObject primaryFile) {
       String type = null;
       try {
            if((! primaryFile.isFolder()) && primaryFile.isValid()){
                InputStream in = primaryFile.getInputStream();
                Resources resources = DDProvider.getDefault().getResourcesGraph(in);
                
                // identify JDBC Connection Pool xml
                JdbcConnectionPool[] pools = resources.getJdbcConnectionPool();
                if(notEmpty(pools)){
                    ConnPoolBean currCPBean = ConnPoolBean.createBean(pools[0]);
                    type = this.JDBC_CP;
                    setPool(currCPBean);
                }  
                
                // identify JDBC Resources xml
                JdbcResource[] dataSources = resources.getJdbcResource();
                if(notEmpty(dataSources)){
                    DataSourceBean currDSBean = DataSourceBean.createBean(dataSources[0]);
                    type = this.JDBC_DS;
                    setDataSource(currDSBean);
                }
                
                // import Persistence Manager Factory Resources
                PersistenceManagerFactoryResource[] pmfResources = resources.getPersistenceManagerFactoryResource();
                if(notEmpty(pmfResources)){
                    PersistenceManagerBean currPMFBean = PersistenceManagerBean.createBean(pmfResources[0]);
                    type = this.PMF;
                    setPersistenceManager(currPMFBean);
                }
                
                // import Mail Resources
                MailResource[] mailResources = resources.getMailResource();
                if(notEmpty(mailResources)){
                    JavaMailSessionBean currMailBean = JavaMailSessionBean.createBean(mailResources[0]);
                    type = this.MAIL;
                    setMailSession(currMailBean);
                }
                
                // import JMS Resources and convert to Admin Object
//                JmsResource[] jmsResources = resources.getJmsResource();
//                if (jmsResources.length != 0) {
//                    JMSBean currJmsBean = JMSBean.createBean(jmsResources[0]);
//                    type = this.JMS;
//                    setJMS(currJmsBean);
//                }
                
                // import Admin Object Resources
                AdminObjectResource[] aoResources = resources.getAdminObjectResource();
                if(notEmpty(aoResources)){
                    JMSBean currJmsBean = JMSBean.createBean(aoResources[0]);
                    type = this.JMS;
                    setJMS(currJmsBean);
                }
                
                ConnectorResource[] connResources = resources.getConnectorResource();
                ConnectorConnectionPool[] connPoolResources = resources.getConnectorConnectionPool();
                if(notEmpty(connResources) && notEmpty(connPoolResources)){
                    JMSBean currJmsBean = JMSBean.createBean(resources);
                    type = this.JMS;
                    setJMS(currJmsBean);
                }
            }
        }catch(NullPointerException npe){
            LOG.log(Level.SEVERE, "Unable to load *.sun-resource file", npe);
        }catch(Exception ex){
            LOG.log(Level.SEVERE, "Unable to load *.sun-resource file", ex);

        }
            return type;
        }
       
    private void setPool(ConnPoolBean in_cpBean){
        this.cpBean = in_cpBean;
    }
    
    private ConnPoolBean getPool(){
        return this.cpBean;
    }
    
    private void setDataSource(DataSourceBean in_dsBean){
        this.dsBean = in_dsBean;
    }
    
    private DataSourceBean getDataSource(){
        return this.dsBean;
    }
    
    private void setPersistenceManager(PersistenceManagerBean in_pmfBean){
        this.pmfBean = in_pmfBean;
    }
    
    private PersistenceManagerBean getPersistenceManager(){
        return this.pmfBean;
    }
    
    private void setMailSession(JavaMailSessionBean in_mailBean){
        this.mailBean = in_mailBean;
    }
    
    private JavaMailSessionBean getMailSession(){
        return this.mailBean;
    }
    
    private void setJMS(JMSBean in_jmsBean){
        this.jmsBean = in_jmsBean;
    }
    
    private JMSBean getJMS(){
        return this.jmsBean;
    }
    
    public void fileAttributeChanged (FileAttributeEvent fe) {
        updateDataObject();
    }
    
    public void fileChanged (FileEvent fe) {
        updateDataObject();
    }
    
    public void fileDataCreated (FileEvent fe) {
        updateDataObject ();
    }
    
    public void fileDeleted (FileEvent fe) {
        updateDataObject ();
    }
    
    public void fileFolderCreated (FileEvent fe) {
        updateDataObject ();
    }
    
    public void fileRenamed (FileRenameEvent fe) {
        updateDataObject ();
    }
    
    private void updateDataObject(){
        resType = getResource(this.getPrimaryFile());       
    }
    
    public String getResourceType(){
        return resType;
    }
  
}
