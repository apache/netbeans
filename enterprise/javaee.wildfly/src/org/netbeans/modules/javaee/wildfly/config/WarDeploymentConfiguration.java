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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.javaee.wildfly.config.gen.EjbRefType;
import org.netbeans.modules.javaee.wildfly.config.gen.JbossWeb;
import org.netbeans.modules.javaee.wildfly.config.gen.MessageDestinationRefType;
import org.netbeans.modules.javaee.wildfly.config.gen.ResourceRefType;
import org.netbeans.modules.javaee.wildfly.config.mdb.MessageDestinationSupportImpl;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils;
import org.netbeans.modules.schema2beans.AttrProp;
import org.netbeans.modules.schema2beans.Common;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Web module deployment configuration handles creation and updating of the
 * jboss-web.xml configuration file.
 *
 * @author Stepan Herold, Libor Kotouc
 */
public class WarDeploymentConfiguration extends WildflyDeploymentConfiguration
        implements ModuleConfiguration, ContextRootConfiguration, DatasourceConfiguration,
        DeploymentPlanConfiguration, PropertyChangeListener {
    private static final Logger LOGGER = Logger.getLogger(WarDeploymentConfiguration.class.getName());
    private File jbossWebFile;
    private JbossWeb jbossWeb;

    public WarDeploymentConfiguration(J2eeModule j2eeModule) {
        this(j2eeModule, null, true);
    }

    /**
     * Creates a new instance of WarDeploymentConfiguration
     */
    public WarDeploymentConfiguration(J2eeModule j2eeModule, WildflyPluginUtils.Version version, boolean isWildFly) {
        super(j2eeModule, version, isWildFly);
        jbossWebFile = j2eeModule.getDeploymentConfigurationFile("WEB-INF/jboss-web.xml"); // NOI18N
        getJbossWeb();
        if (deploymentDescriptorDO == null) {
            try {
                if (FileUtil.toFileObject(jbossWebFile) != null) {
                    deploymentDescriptorDO = DataObject.find(FileUtil.toFileObject(jbossWebFile));
                    deploymentDescriptorDO.addPropertyChangeListener(this);
                }
            } catch (DataObjectNotFoundException donfe) {
                LOGGER.log(Level.WARNING, null, donfe);
            }
        }
    }

    @Override
    public Lookup getLookup() {
        return Lookups.fixed(this);
    }

    @Override
    public void dispose() {
    }

    /**
     * Return context path.
     *
     * @return context path or null, if the file is not parseable.
     */
    @Override
    public String getContextRoot() throws ConfigurationException {
        JbossWeb jbossWeb = getJbossWeb();
        if (jbossWeb == null) { // graph not parseable
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_CannotReadContextRoot", jbossWebFile.getAbsolutePath());
            throw new ConfigurationException(msg);
        }
        if (jbossWeb.getContextRoot() == null || jbossWeb.getContextRoot().isEmpty()) {
            try {
                if (j2eeModule.getArchive() != null) {
                    return formatContextPath(j2eeModule.getArchive().getName());
                }
                if (j2eeModule.getUrl() != null) {
                    if (j2eeModule.getUrl().endsWith(".war")) {
                        return formatContextPath(j2eeModule.getUrl().substring(0, j2eeModule.getUrl().length() - 4));
                    } else {
                        return formatContextPath(j2eeModule.getUrl());
                    }
                }
            } catch (IOException ex) {
                return formatContextPath(jbossWeb.getContextRoot());
            }
        }
        return formatContextPath(jbossWeb.getContextRoot());
    }

    private String formatContextPath(final String contextRoot) {
        if (!contextRoot.startsWith("/")) {
            return '/' + contextRoot;
        }
        return contextRoot;
    }

    /**
     * Set context path.
     */
    @Override
    public void setContextRoot(String contextPath) throws ConfigurationException {
        // TODO: this contextPath fix code will be removed, as soon as it will
        // be moved to the web project

        String currentCP = "";
        if (contextPath != null) {
            currentCP = contextPath;
        }
        if (!isCorrectCP(currentCP)) {
            String ctxRoot = currentCP;
            java.util.StringTokenizer tok = new java.util.StringTokenizer(currentCP, "/"); //NOI18N
            StringBuilder buf = new StringBuilder();
            while (tok.hasMoreTokens()) {
                buf.append('/').append(tok.nextToken()); //NOI18N
            }
            ctxRoot = buf.toString();
            NotifyDescriptor desc = new NotifyDescriptor.Message(
                    NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_invalidCP", currentCP),
                    NotifyDescriptor.Message.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            currentCP = ctxRoot;
        }
        final String newContextPath = currentCP;
        if (!getContextRoot().equals(newContextPath)) {
            modifyJbossWeb(new JbossWebModifier() {
                @Override
                public void modify(JbossWeb jbossWeb) {
                    jbossWeb.setContextRoot(newContextPath);
                }
            });
        }
    }

    /**
     * Listen to jboss-web.xml document changes.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Object newValue = evt.getNewValue();
        if (evt.getPropertyName() == DataObject.PROP_MODIFIED && newValue == Boolean.FALSE) {
            if (evt.getSource() == deploymentDescriptorDO) { // dataobject has been modified, jbossWeb graph is out of sync
                synchronized (this) {
                    jbossWeb = null;
                }
            }
        } else if (evt.getOldValue() == null) {
            if (newValue instanceof org.netbeans.modules.j2ee.dd.api.common.ResourceRef) {
                //a new resource reference added
                org.netbeans.modules.j2ee.dd.api.common.ResourceRef resourceRef = (org.netbeans.modules.j2ee.dd.api.common.ResourceRef) newValue;
                try {
                    String resType = resourceRef.getResType();
                    if ("javax.sql.DataSource".equals(resType)) { // NOI18N
                        addResReference(resourceRef.getResRefName(), WildflyDatasource.PREFIX + resourceRef.getResRefName());
                    } else if ("javax.mail.Session".equals(resType)) { // NOI18N
                        addMailReference(resourceRef.getResRefName());
                    } else if ("javax.jms.ConnectionFactory".equals(resType)) { // NOI18N
                        addConnectionFactoryReference(resourceRef.getResRefName());
                    }
                } catch (ConfigurationException ce) {
                    LOGGER.log(Level.WARNING, null, ce);
                }
            } else if (newValue instanceof org.netbeans.modules.j2ee.dd.api.common.EjbRef) {
                // a new ejb reference added
                org.netbeans.modules.j2ee.dd.api.common.EjbRef ejbRef = (org.netbeans.modules.j2ee.dd.api.common.EjbRef) newValue;
                try {
                    String ejbRefType = ejbRef.getEjbRefType();
                    if ("Session".equals(ejbRefType) || "Entity".equals(ejbRefType)) { // NOI18N
                        addEjbReference(ejbRef.getEjbRefName(), ejbRef.getEjbRefName());
                    }
                } catch (ConfigurationException ce) {
                    LOGGER.log(Level.WARNING, null, ce);
                }
            } else if (newValue instanceof org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef) {
                //a new message destination reference added
                org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef messageDestinationRef = (org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef) newValue;
                try {
                    String messageDestinationType = messageDestinationRef.getMessageDestinationType();
                    String destPrefix = "javax.jms.Queue".equals(messageDestinationType) // NOI18N
                            ? WildflyMessageDestination.QUEUE_PREFIX : WildflyMessageDestination.TOPIC_PREFIX;
                    addMsgDestReference(messageDestinationRef.getMessageDestinationRefName(), destPrefix);
                } catch (ConfigurationException ce) {
                    LOGGER.log(Level.WARNING, null, ce);
                }
            }
        }
    }

    @Override
    public void bindDatasourceReference(String referenceName, String jndiName) throws ConfigurationException {
        addResReference(referenceName, jndiName);
    }

    @Override
    public String findDatasourceJndiName(String referenceName) throws ConfigurationException {

        ResourceRefType[] resourceRefs = getJbossWeb().getResourceRef();
        for (ResourceRefType resourceRef : resourceRefs) {
            String rrn = resourceRef.getResRefName();
            if (referenceName.equals(rrn)) {
                String jndiName = resourceRef.getJndiName();
                if (jndiName != null) {
                    return WildflyDatasource.getJndiName(jndiName);
                }
            }
        }

        return null;
    }

    /**
     * Add a new resource reference.
     *
     * @param name resource reference name
     */
    private void addResReference(final String name, final String jndiName) throws ConfigurationException {
        modifyJbossWeb(new JbossWebModifier() {
            public void modify(JbossWeb modifiedJbossWeb) {

                // check whether resource not already defined
                ResourceRefType resourceRefs[] = modifiedJbossWeb.getResourceRef();
                for (int i = 0; i < resourceRefs.length; i++) {
                    String rrn = resourceRefs[i].getResRefName();
                    if (name.equals(rrn)) {
                        // already exists
                        return;
                    }
                }

                //if it doesn't exist yet, create a new one
                ResourceRefType newRR = new ResourceRefType();
                newRR.setResRefName(name);
                newRR.setJndiName(jndiName);
                modifiedJbossWeb.addResourceRef(newRR);
            }
        });
    }

    /**
     * Add a new mail service reference.
     *
     * @param name mail service name
     */
    private void addMailReference(final String name) throws ConfigurationException {
        modifyJbossWeb(new JbossWebModifier() {
            public void modify(JbossWeb modifiedJbossWeb) {

                // check whether mail service not already defined
                ResourceRefType resourceRefs[] = modifiedJbossWeb.getResourceRef();
                for (int i = 0; i < resourceRefs.length; i++) {
                    String rrn = resourceRefs[i].getResRefName();
                    if (name.equals(rrn)) {
                        // already exists
                        return;
                    }
                }

                //if it doesn't exist yet, create a new one
                ResourceRefType newRR = new ResourceRefType();
                newRR.setResRefName(name);
                newRR.setJndiName(MAIL_SERVICE_JNDI_NAME_JB4);
                modifiedJbossWeb.addResourceRef(newRR);
            }
        });
    }

    @Override
    public void bindMessageDestinationReference(String referenceName, String connectionFactoryName,
            String destName, MessageDestination.Type type) throws ConfigurationException {

        addConnectionFactoryReference(connectionFactoryName);

        String jndiName = null;
        if (MessageDestination.Type.QUEUE.equals(type)) {
            jndiName = WildflyMessageDestination.QUEUE_PREFIX + destName;
        } else if (MessageDestination.Type.TOPIC.equals(type)) {
            jndiName = WildflyMessageDestination.TOPIC_PREFIX + destName;
        }

        addMsgDestReference(referenceName, jndiName);
    }

    /**
     * Add a new connection factory reference.
     *
     * @param name connection factory name
     */
    private void addConnectionFactoryReference(final String name) throws ConfigurationException {
        modifyJbossWeb(new JbossWebModifier() {
            public void modify(JbossWeb modifiedJbossWeb) {

                // check whether connection factory not already defined
                ResourceRefType resourceRefs[] = modifiedJbossWeb.getResourceRef();
                for (int i = 0; i < resourceRefs.length; i++) {
                    String rrn = resourceRefs[i].getResRefName();
                    if (name.equals(rrn)) {
                        // already exists
                        return;
                    }
                }

                //if it doesn't exist yet, create a new one
                ResourceRefType newRR = new ResourceRefType();
                newRR.setResRefName(name);
                newRR.setJndiName(MessageDestinationSupportImpl.CONN_FACTORY_JNDI_NAME_JB4);
                modifiedJbossWeb.addResourceRef(newRR);
            }
        });
    }

    /**
     * Add a new message destination reference.
     *
     * @param name message destination name
     * @param destPrefix MDB destination prefix
     */
    private void addMsgDestReference(final String name, final String jndiName) throws ConfigurationException {
        modifyJbossWeb(new JbossWebModifier() {
            @Override
            public void modify(JbossWeb modifiedJbossWeb) {

                // check whether message destination not already defined
                MessageDestinationRefType mdRefs[] = modifiedJbossWeb.getMessageDestinationRef();
                for (MessageDestinationRefType mdRef : mdRefs) {
                    String mdrn = mdRef.getMessageDestinationRefName();
                    if (name.equals(mdrn)) {
                        // already exists
                        return;
                    }
                }

                //if it doesn't exist yet, create a new one
                MessageDestinationRefType mdr = new MessageDestinationRefType();
                mdr.setMessageDestinationRefName(name);
                mdr.setJndiName(jndiName);
                modifiedJbossWeb.addMessageDestinationRef(mdr);
            }
        });
    }

    @Override
    public void bindEjbReference(String referenceName, String ejbName) throws ConfigurationException {
        if (Double.parseDouble(j2eeModule.getModuleVersion()) > 2.4) {
            return;
        }
        addEjbReference(referenceName, ejbName);
    }

    /**
     * Add a new ejb reference.
     *
     * @param name ejb reference name
     */
    private void addEjbReference(final String referenceName, final String ejbName) throws ConfigurationException {
        modifyJbossWeb(new JbossWebModifier() {
            @Override
            public void modify(JbossWeb modifiedJbossWeb) {

                // check whether resource not already defined
                EjbRefType ejbRefs[] = modifiedJbossWeb.getEjbRef();
                for (EjbRefType ejbRef : ejbRefs) {
                    String ern = ejbRef.getEjbRefName();
                    if (referenceName.equals(ern)) {
                        // already exists
                        return;
                    }
                }

                //if it doesn't exist yet, create a new one
                EjbRefType newER = new EjbRefType();
                newER.setEjbRefName(referenceName);
                newER.setJndiName(ejbName);
                modifiedJbossWeb.addEjbRef(newER);
            }
        });
    }

    /**
     * Return JbossWeb graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     * @return JbossWeb graph or null if the jboss-web.xml file is not
     * parseable.
     */
    public synchronized JbossWeb getJbossWeb() {
        if (jbossWeb == null) {
            if (jbossWebFile.exists()) {
                // load configuration if already exists
                try {
                    jbossWeb = JbossWeb.createGraph(jbossWebFile);
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, null, ioe);
                } catch (RuntimeException re) {
                    // jboss-web.xml is not parseable, do nothing
                }
            } else {
                jbossWeb = generateJbossWeb();
            }
        }
        return jbossWeb;
    }

    @Override
    public void save(OutputStream os) throws ConfigurationException {
        JbossWeb jbossWeb = getJbossWeb();
        if (jbossWeb == null) {
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_cannotSaveNotParseableConfFile", jbossWebFile.getAbsolutePath());
            throw new ConfigurationException(msg);
        }
        try {
            jbossWeb.write(os);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_CannotUpdateFile", jbossWebFile.getAbsolutePath());
            throw new ConfigurationException(msg, ioe);
        }
    }

    // private helper methods -------------------------------------------------
    /**
     * Perform jbossWeb changes defined by the jbossWeb modifier. Update editor
     * content and save changes, if appropriate.
     *
     * @param modifier
     */
    private void modifyJbossWeb(JbossWebModifier modifier) throws ConfigurationException {
        if (deploymentDescriptorDO == null) {
            if (FileUtil.toFileObject(jbossWebFile) == null) {
                ResourceConfigurationHelper.writeFile(jbossWebFile, jbossWeb);
                try {
                    deploymentDescriptorDO = DataObject.find(FileUtil.toFileObject(jbossWebFile));
                } catch (DataObjectNotFoundException ex) {
                    throw new ConfigurationException("Couldn't save jboss-web.xml", ex);
                }
                deploymentDescriptorDO.addPropertyChangeListener(this);
            }
        }
        if (deploymentDescriptorDO == null) {
            return;
        }
        assert deploymentDescriptorDO != null : "DataObject has not been initialized yet"; // NIO18N
        try {
            // get the document
            EditorCookie editor = (EditorCookie) deploymentDescriptorDO.getCookie(EditorCookie.class);
            StyledDocument doc = editor.getDocument();
            if (doc == null) {
                doc = editor.openDocument();
            }

            // get the up-to-date model
            JbossWeb newJbossWeb = null;
            try {
                // try to create a graph from the editor content
                byte[] docString = doc.getText(0, doc.getLength()).getBytes();
                newJbossWeb = JbossWeb.createGraph(new ByteArrayInputStream(docString));
            } catch (RuntimeException e) {
                JbossWeb oldJbossWeb = getJbossWeb();
                if (oldJbossWeb == null) {
                    // neither the old graph is parseable, there is not much we can do here
                    // TODO: should we notify the user?
                    String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_jbossXmlCannotParse", jbossWebFile.getAbsolutePath());
                    throw new ConfigurationException(msg);
                }
                // current editor content is not parseable, ask whether to override or not
                NotifyDescriptor notDesc = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_jbossWebXmlNotValid"),
                        NotifyDescriptor.OK_CANCEL_OPTION);
                Object result = DialogDisplayer.getDefault().notify(notDesc);
                if (result == NotifyDescriptor.CANCEL_OPTION) {
                    // keep the old content
                    return;
                }
                // use the old graph
                newJbossWeb = oldJbossWeb;
            }

            // perform changes
            modifier.modify(newJbossWeb);

            // save, if appropriate
            boolean modified = deploymentDescriptorDO.isModified();
            ResourceConfigurationHelper.replaceDocument(doc, newJbossWeb);
            if (!modified) {
                SaveCookie cookie = (SaveCookie) deploymentDescriptorDO.getCookie(SaveCookie.class);
                if (cookie != null) {
                    cookie.save();
                }
            } else {
                ResourceConfigurationHelper.writeFile(jbossWebFile, jbossWeb);
            }
            synchronized (this) {
                jbossWeb = newJbossWeb;
            }
        } catch (BadLocationException ble) {
            // this should not occur, just log it if it happens
                LOGGER.log(Level.WARNING, null, ble);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_CannotUpdateFile", jbossWebFile.getAbsolutePath());
            throw new ConfigurationException(msg, ioe);
        }
    }

    /**
     * Generate JbossWeb graph.
     */
    private JbossWeb generateJbossWeb() {
        JbossWeb jbossWeb = new JbossWeb(null, Common.NO_DEFAULT_VALUES);
        if (jbossWeb._getSchemaLocation() == null) {
            jbossWeb.createAttribute("xmlns:xsi", "xmlns:xsi", AttrProp.CDATA | AttrProp.IMPLIED, null, "http://www.w3.org/2001/XMLSchema-instance");
            jbossWeb.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            jbossWeb.createAttribute("xsi:schemaLocation", "xsi:schemaLocation", AttrProp.CDATA | AttrProp.IMPLIED, null, getJbossWebSchemaLocation());
            jbossWeb._setSchemaLocation(getJbossWebSchemaLocation());
            jbossWeb.setVersion(getJbossWebVersion());
        }
        jbossWeb.setContextRoot(""); // NOI18N
        return jbossWeb;
    }

    private String getJbossWebVersion() {
        if (version == null || WildflyPluginUtils.WILDFLY_8_0_0.compareTo(version) <= 0) {
            return "8.0";
        } else if (WildflyPluginUtils.EAP_6_3_0.compareTo(version) <= 0) {
            return "7.2";
        } else {
            return "7.1";
        }
    }

    private String getJbossWebSchemaLocation() {
        if (version == null || WildflyPluginUtils.WILDFLY_8_0_0.compareTo(version) <= 0) {
            return "http://www.jboss.com/xml/ns/javaee http://www.jboss.org/schema/jbossas/jboss-web_8_0.xsd";
        } else if (WildflyPluginUtils.EAP_6_3_0.compareTo(version) <= 0) {
            return "http://www.jboss.com/xml/ns/javaee http://www.jboss.org/schema/jbossas/jboss-web_7_2.xsd";
        } else {
            return "http://www.jboss.com/xml/ns/javaee http://www.jboss.org/schema/jbossas/jboss-web_7_1.xsd";
        }
    }

    // TODO: this contextPath fix code will be removed, as soon as it will
    // be moved to the web project
    private boolean isCorrectCP(String contextPath) {
        boolean correct = true;
        if (!"".equals(contextPath) && !contextPath.startsWith("/")) {
            correct = false; //NOI18N
        } else if (contextPath.endsWith("/")) {
            correct = false; //NOI18N
        } else if (contextPath.indexOf("//") >= 0) {
            correct = false; //NOI18N
        }
        return correct;
    }

    // private helper interface -----------------------------------------------
    private interface JbossWebModifier {

        void modify(JbossWeb modifiedJbossWeb);
    }
}
