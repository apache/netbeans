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

package org.netbeans.modules.javaee.wildfly.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.javaee.wildfly.config.gen.EjbRef;
import org.netbeans.modules.javaee.wildfly.config.gen.JbossClient;
import org.netbeans.modules.javaee.wildfly.config.gen.ResourceRef;
import org.netbeans.modules.javaee.wildfly.config.mdb.MessageDestinationSupportImpl;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils;
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
 *
 * @author jungi
 */
public class CarDeploymentConfiguration extends WildflyDeploymentConfiguration
implements ModuleConfiguration, DatasourceConfiguration, DeploymentPlanConfiguration, PropertyChangeListener {

    private File jbossClientFile;
    private JbossClient jbossClient;

    public CarDeploymentConfiguration(J2eeModule j2eeModule) {
        this(j2eeModule, null, true);
    }

    /** Creates a new instance of CarDeploymentConfiguration */
    public CarDeploymentConfiguration(J2eeModule j2eeModule, WildflyPluginUtils.Version version, boolean isWildFly) {
        super(j2eeModule, version, isWildFly);
        jbossClientFile = j2eeModule.getDeploymentConfigurationFile("META-INF/jboss-client.xml"); // NOI18N
        getJbossClient();
        if (deploymentDescriptorDO == null) {
            try {
                deploymentDescriptorDO = deploymentDescriptorDO.find(FileUtil.toFileObject(jbossClientFile));
                deploymentDescriptorDO.addPropertyChangeListener(this);
            } catch(DataObjectNotFoundException donfe) {
                Exceptions.printStackTrace(donfe);
            }
        }
        // TODO: rewrite
//        AppClient appClient = (AppClient) j2eeModule.getMetadataModel(J2eeModule.CLIENT_XML);
//        if (appClient != null) {
//            appClient.addPropertyChangeListener(this);
//        }
    }


    public void dispose() {
        // TODO: rewrite
//        AppClient appClient = (AppClient) j2eeModule.getMetadataModel(J2eeModule.CLIENT_XML);
//        if (appClient != null) {
//            appClient.removePropertyChangeListener(this);
//        }
    }

    public Lookup getLookup() {
        return Lookups.fixed(this);
    }

    /**
     * Return JbossClient graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     * @return JbossWeb graph or null if the jboss-web.xml file is not parseable.
     */
    public synchronized JbossClient getJbossClient() {
        if (jbossClient == null) {
            try {
                if (jbossClientFile.exists()) {
                    // load configuration if already exists
                    try {
                        jbossClient = JbossClient.createGraph(jbossClientFile);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    } catch (RuntimeException re) {
                        // jboss-web.xml is not parseable, do nothing
                    }
                } else {
                    // create jboss-web.xml if it does not exist yet
                    jbossClient = generateJbossClient();
                    ResourceConfigurationHelper.writeFile(jbossClientFile, jbossClient);
                }
            } catch (ConfigurationException ce) {
                Exceptions.printStackTrace(ce);
            }
        }
        return jbossClient;
    }

    /**
     * Listen to jboss-web.xml document changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        Object newValue = evt.getNewValue();
        if (evt.getPropertyName() == DataObject.PROP_MODIFIED && evt.getNewValue() == Boolean.FALSE) {
            if (evt.getSource() == deploymentDescriptorDO) { // dataobject has been modified, jbossWeb graph is out of sync
                synchronized (this) {
                    jbossClient = null;
                }
            } else {
//                super.propertyChange(evt);
            }
        } else if (evt.getOldValue() == null) {
            // TODO do we also want to check changes in the application client display name?
            if (newValue instanceof org.netbeans.modules.j2ee.dd.api.common.ResourceRef) {
                //a new resource reference added
                org.netbeans.modules.j2ee.dd.api.common.ResourceRef resourceRef = (org.netbeans.modules.j2ee.dd.api.common.ResourceRef) newValue;
                try {
                    String resType = resourceRef.getResType();
                    if ("javax.sql.DataSource".equals(resType)) { // NOI18N
                        addResReference(resourceRef.getResRefName());
                    } else if ("javax.mail.Session".equals(resType)) { // NOI18N
                        addMailReference(resourceRef.getResRefName());
                    } else if ("javax.jms.ConnectionFactory".equals(resType)) { // NOI18N
                        addConnectionFactoryReference(resourceRef.getResRefName());
                    }
                } catch (ConfigurationException ce) {
                    Exceptions.printStackTrace(ce);
                }
            } else if (newValue instanceof org.netbeans.modules.j2ee.dd.api.common.EjbRef) {
                // a new ejb reference added
                org.netbeans.modules.j2ee.dd.api.common.EjbRef ejbRef = (org.netbeans.modules.j2ee.dd.api.common.EjbRef) newValue;
                try {
                    String ejbRefType = ejbRef.getEjbRefType();
                    if ("Session".equals(ejbRefType) || "Entity".equals(ejbRefType)) { // NOI18N
                        addEjbReference(ejbRef.getEjbRefName());
                    }
                } catch (ConfigurationException ce) {
                    Exceptions.printStackTrace(ce);
                }
            } else if (newValue instanceof org.netbeans.modules.j2ee.dd.api.common.ServiceRef) {
                // a new message destination reference added
                org.netbeans.modules.j2ee.dd.api.common.ServiceRef serviceRef = (org.netbeans.modules.j2ee.dd.api.common.ServiceRef) newValue;
                try {
                    addServiceReference(serviceRef.getServiceRefName());
                } catch (ConfigurationException ce) {
                    Exceptions.printStackTrace(ce);
                }
            }
        }
    }

    public void save(OutputStream os) throws ConfigurationException {
        JbossClient jbossClientDD = getJbossClient();
        if (jbossClientDD == null) {
            String msg = NbBundle.getMessage(CarDeploymentConfiguration.class, "MSG_cannotSaveNotParseableConfFile", jbossClientFile.getAbsolutePath());
            throw new ConfigurationException(msg);
        }
        try {
            jbossClientDD.write(os);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(CarDeploymentConfiguration.class, "MSG_CannotUpdateFile", jbossClientFile.getAbsolutePath());
            throw new ConfigurationException(msg, ioe);
        }
    }

    // private helper methods -------------------------------------------------

    /**
     * Generate JbossWeb graph.
     */
    private JbossClient generateJbossClient() {
        JbossClient jbossClientDD = new JbossClient();
        //jbossClientDD.setContextRoot(""); // NOI18N
        return jbossClientDD;
    }

    /**
     * Add a new resource reference.
     *
     * @param name resource reference name
     */
    private void addResReference(final String name) throws ConfigurationException {
        modifyJbossClient(new JbossClientModifier() {
            public void modify(JbossClient modifiedJbossClient) {

                // check whether resource not already defined
                ResourceRef resourceRefs[] = modifiedJbossClient.getResourceRef();
                for (int i = 0; i < resourceRefs.length; i++) {
                    String rrn = resourceRefs[i].getResRefName();
                    if (name.equals(rrn)) {
                        // already exists
                        return;
                    }
                }

                //if it doesn't exist yet, create a new one
                ResourceRef newRR = new ResourceRef();
                newRR.setResRefName(name);
                newRR.setJndiName(WildflyDatasource.PREFIX + name);
                modifiedJbossClient.addResourceRef(newRR);
            }
        });
    }

    /**
     * Add a new mail service reference.
     *
     * @param name mail service name
     */
    private void addMailReference(final String name) throws ConfigurationException {
        modifyJbossClient(new JbossClientModifier() {
            public void modify(JbossClient modifiedJbossClient) {

                // check whether mail service not already defined
                ResourceRef resourceRefs[] = modifiedJbossClient.getResourceRef();
                for (int i = 0; i < resourceRefs.length; i++) {
                    String rrn = resourceRefs[i].getResRefName();
                    if (name.equals(rrn)) {
                        // already exists
                        return;
                    }
                }

                //if it doesn't exist yet, create a new one
                ResourceRef newRR = new ResourceRef();
                newRR.setResRefName(name);
                newRR.setJndiName(MAIL_SERVICE_JNDI_NAME_JB4);
                modifiedJbossClient.addResourceRef(newRR);
            }
        });
    }

    /**
     * Add a new connection factory reference.
     *
     * @param name connection factory name
     */
    private void addConnectionFactoryReference(final String name) throws ConfigurationException {
        modifyJbossClient(new JbossClientModifier() {
            public void modify(JbossClient modifiedJbossClient) {

                // check whether connection factory not already defined
                ResourceRef resourceRefs[] = modifiedJbossClient.getResourceRef();
                for (int i = 0; i < resourceRefs.length; i++) {
                    String rrn = resourceRefs[i].getResRefName();
                    if (name.equals(rrn)) {
                        // already exists
                        return;
                    }
                }

                //if it doesn't exist yet, create a new one
                ResourceRef newRR = new ResourceRef();
                newRR.setResRefName(name);
                newRR.setJndiName(MessageDestinationSupportImpl.CONN_FACTORY_JNDI_NAME_JB4);
                modifiedJbossClient.addResourceRef(newRR);
            }
        });
    }

    /**
     * Add a new ejb reference.
     *
     * @param name ejb reference name
     */
    private void addEjbReference(final String name) throws ConfigurationException {
        modifyJbossClient(new JbossClientModifier() {
            public void modify(JbossClient modifiedJbossClient) {

                // check whether resource not already defined
                EjbRef ejbRefs[] = modifiedJbossClient.getEjbRef();
                for (int i = 0; i < ejbRefs.length; i++) {
                    String ern = ejbRefs[i].getEjbRefName();
                    if (name.equals(ern)) {
                        // already exists
                        return;
                    }
                }

                //if it doesn't exist yet, create a new one
                EjbRef newER = new EjbRef();
                newER.setEjbRefName(name);
                newER.setJndiName(/*JBOSS4_EJB_JNDI_PREFIX + */name);
                modifiedJbossClient.addEjbRef(newER);
            }
        });
    }

    /**
     * Add a new jndi-name.
     *
     * @param name jndi-name  name
     */
    private void setJndiName(final String jndiName) throws ConfigurationException {
        modifyJbossClient(new JbossClientModifier() {
            public void modify(JbossClient modifiedJbossClient) {
                modifiedJbossClient.setJndiName(jndiName);
            }
        });
    }

    /**
     * Add a new service reference.
     *
     * @param name service reference name
     */
    private void addServiceReference(final String name) throws ConfigurationException {
        modifyJbossClient(new JbossClientModifier() {
            public void modify(JbossClient modifiedJbossClient) {

                // check whether resource not already defined
                String serviceRefs[] = modifiedJbossClient.getServiceRef();
                for (int i = 0; i < serviceRefs.length; i++) {
                    String srn = serviceRefs[i];
                    if (name.equals(srn)) {
                        // already exists
                        return;
                    }
                }

                //if it doesn't exist yet, create a new one
                modifiedJbossClient.addServiceRef(name);
            }
        });
    }

    /**
     * Perform jbossWeb changes defined by the jbossWeb modifier. Update editor
     * content and save changes, if appropriate.
     *
     * @param modifier
     */
    private void modifyJbossClient(JbossClientModifier modifier) throws ConfigurationException {
        assert deploymentDescriptorDO != null : "DataObject has not been initialized yet"; // NIO18N
        try {
            // get the document
            EditorCookie editor = (EditorCookie)deploymentDescriptorDO.getCookie(EditorCookie.class);
            StyledDocument doc = editor.getDocument();
            if (doc == null) {
                doc = editor.openDocument();
            }

            // get the up-to-date model
            JbossClient newJbossClient = null;
            try {
                // try to create a graph from the editor content
                byte[] docString = doc.getText(0, doc.getLength()).getBytes();
                newJbossClient = JbossClient.createGraph(new ByteArrayInputStream(docString));
            } catch (RuntimeException e) {
                JbossClient oldJbossClient = getJbossClient();
                if (oldJbossClient == null) {
                    // neither the old graph is parseable, there is not much we can do here
                    // TODO: should we notify the user?
                    String msg = NbBundle.getMessage(CarDeploymentConfiguration.class, "MSG_jbossXmlCannotParse", jbossClientFile.getAbsolutePath());
                    throw new ConfigurationException(msg);
                }
                // current editor content is not parseable, ask whether to override or not
                NotifyDescriptor notDesc = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(CarDeploymentConfiguration.class, "MSG_jbossClientXmlNotValid"),
                        NotifyDescriptor.OK_CANCEL_OPTION);
                Object result = DialogDisplayer.getDefault().notify(notDesc);
                if (result == NotifyDescriptor.CANCEL_OPTION) {
                    // keep the old content
                    return;
                }
                // use the old graph
                newJbossClient = oldJbossClient;
            }

            // perform changes
            modifier.modify(newJbossClient);

            // save, if appropriate
            boolean modified = deploymentDescriptorDO.isModified();
            ResourceConfigurationHelper.replaceDocument(doc, newJbossClient);
            if (!modified) {
                SaveCookie cookie = (SaveCookie)deploymentDescriptorDO.getCookie(SaveCookie.class);
                if (cookie != null) {
                    cookie.save();
                }
            }
            synchronized (this) {
                jbossClient = newJbossClient;
            }
        } catch (BadLocationException ble) {
            // this should not occur, just log it if it happens
            Exceptions.printStackTrace(ble);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(CarDeploymentConfiguration.class, "MSG_CannotUpdateFile", jbossClientFile.getAbsolutePath());
            throw new ConfigurationException(msg, ioe);
        }
    }

    // private helper interface -----------------------------------------------

    private interface JbossClientModifier {
        void modify(JbossClient modifiedJbossClient);
    }
}
