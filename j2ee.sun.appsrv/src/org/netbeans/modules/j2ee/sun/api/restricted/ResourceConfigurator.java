/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
/*
 * ResourceConfiguration.java
 *
 * Created on August 22, 2005, 12:43 PM
 */

package org.netbeans.modules.j2ee.sun.api.restricted;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;

import org.netbeans.modules.j2ee.sun.api.ResourceConfiguratorInterface;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool;
import org.netbeans.modules.j2ee.sun.sunresources.beans.DatabaseUtils;

import org.netbeans.modules.j2ee.sun.sunresources.beans.Field;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroupHelper;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldHelper;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.modules.glassfish.eecommon.api.UrlData;
import org.netbeans.modules.glassfish.eecommon.api.Utils;

/**
 *
 * @author Nitya Doraisamy
 */
public class ResourceConfigurator implements ResourceConfiguratorInterface {

    public static final String __SunResourceExt = "sun-resource"; // NOI18N

    private final static char BLANK = ' ';
    private final static char DOT   = '.';
    private final static char[]	ILLEGAL_FILENAME_CHARS	= {'/', '\\', ':', '*', '?', '"', '<', '>', '|', ',', '=', ';' };
    // private final static char[]	ILLEGAL_RESOURCE_NAME_CHARS	= {':', '*', '?', '"', '<', '>', '|', ',' };
    private final static char REPLACEMENT_CHAR = '_';
    private final static char DASH = '-';

    private static final String DATAFILE = "org/netbeans/modules/j2ee/sun/sunresources/beans/CPWizard.xml";  // NOI18N

    private DeploymentManager currentDM = null;

    public static final String JDBC_RESOURCE = "jdbc"; // NOI18N
    public static final String JMS_RESOURCE = "jms"; // NOI18N
    public static final String JMS_PREFIX = "jms/"; // NOI18N

    ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.sunresources.beans.Bundle");// NOI18N

    private static final Logger LOG = Logger.getLogger(ResourceConfigurator.class.getName());

    /**
     * Creates a new instance of ResourceConfigurator
     */
    public ResourceConfigurator() {
    }

    public void setDeploymentManager(DeploymentManager dm) {
        this.currentDM = dm;
    }

    /** Returns whether or not the specific JMS resource alread exists.
     *
     * @param jndiName Jndi name identifying the JMS resource.
     * @param dir Folder where the resources should be stored.  Can be null,
     *   in which case this method will return false.
     *
     * @returns true if the resource exists in the resource, or false for all
     *  other possibilities.
     */
    @Override
    public boolean isJMSResourceDefined(String jndiName, File dir) {
        return requiredResourceExists(jndiName, dir, JMS_RESOURCE);
    }

    @Override
    public MessageDestination createJMSResource(String jndiName, MessageDestination.Type type, String ejbName, File dir, String baseName) {
        SunMessageDestination msgDest = null;
        if(! jndiName.startsWith(JMS_PREFIX)){
            jndiName = JMS_PREFIX + jndiName;
        }
        Resources resources = ResourceUtils.getServerResourcesGraph(dir);
        AdminObjectResource aoresource = resources.newAdminObjectResource();
        aoresource.setJndiName(jndiName);
        PropertyElement prop = aoresource.newPropertyElement();
        prop.setName("Name"); // NOI18N
        if (MessageDestination.Type.QUEUE.equals(type)) {
            aoresource.setResType(WizardConstants.__QUEUE);
            prop.setValue(WizardConstants.QUEUE_PROP);
        } else if (MessageDestination.Type.TOPIC.equals(type)) {
            aoresource.setResType(WizardConstants.__TOPIC);
            prop.setValue(WizardConstants.TOPIC_PROP);
        }
        aoresource.setResAdapter(WizardConstants.__JmsResAdapter);
        aoresource.setEnabled("true"); // NOI18N
        aoresource.setDescription(""); // NOI18N
        aoresource.addPropertyElement(prop);
        resources.addAdminObjectResource(aoresource);

        ConnectorResource connresource = resources.newConnectorResource();
        ConnectorConnectionPool connpoolresource = resources.newConnectorConnectionPool();

        String connectionFactoryJndiName= jndiName + "Factory"; // NOI18N
        String connectionFactoryPoolName = jndiName + "FactoryPool"; // NOI18N
        connresource.setJndiName(connectionFactoryJndiName);
        connresource.setDescription("");
        connresource.setEnabled("true");
        connresource.setPoolName(connectionFactoryPoolName);

        connpoolresource.setName(connectionFactoryPoolName);
        connpoolresource.setResourceAdapterName(WizardConstants.__JmsResAdapter);

        if(type.equals(MessageDestination.Type.QUEUE)) {
            connpoolresource.setConnectionDefinitionName(WizardConstants.__QUEUE_CNTN_FACTORY);
        } else {
            if(type.equals(MessageDestination.Type.TOPIC)) {
                connpoolresource.setConnectionDefinitionName(WizardConstants.__TOPIC_CNTN_FACTORY);
            } else {
                assert false; //control should never reach here
            }
        }
        resources.addConnectorResource(connresource);
        resources.addConnectorConnectionPool(connpoolresource);
        ResourceUtils.createFile(dir, resources, baseName);
        msgDest = new SunMessageDestination(jndiName, type);
        return msgDest;
    }
    /** Creates a new JMS resource with the specified values.
     *
     * @param jndiName jndi-name that identifies this JMS resource.
     * @param msgDstnType type of message destination.
     * @param msgDstnName name of the message destination.
     * @param ejbName name of ejb.
     * @param dir Folder where the resource should be stored.  Should not be null.
     */
    @Override
    public void createJMSResource(String jndiName, String msgDstnType, String msgDstnName, String ejbName, File dir, String baseName) {
        FileObject location = FileUtil.toFileObject(dir);
        Resources resources = ResourceUtils.getServerResourcesGraph(location,
                    baseName.contains("glassfish-resources") ? Resources.VERSION_1_5 : Resources.VERSION_1_3);
        AdminObjectResource aoresource = resources.newAdminObjectResource();
        aoresource.setJndiName(jndiName);
        aoresource.setResType(msgDstnType);
        aoresource.setResAdapter(WizardConstants.__JmsResAdapter);
        aoresource.setEnabled("true"); // NOI18N
        aoresource.setDescription(""); // NOI18N
        PropertyElement prop = aoresource.newPropertyElement();
        prop.setName("Name"); // NOI18N
        prop.setValue(ejbName);
        aoresource.addPropertyElement(prop);
        resources.addAdminObjectResource(aoresource);

        ConnectorResource connresource = resources.newConnectorResource();
        ConnectorConnectionPool connpoolresource = resources.newConnectorConnectionPool();

        String connectionFactoryJndiName= "jms/" + msgDstnName + "Factory"; // NOI18N
        connresource.setJndiName(connectionFactoryJndiName);
        connresource.setDescription("");
        connresource.setEnabled("true");
        connresource.setPoolName(connectionFactoryJndiName);

        connpoolresource.setName(connectionFactoryJndiName);
        connpoolresource.setResourceAdapterName(WizardConstants.__JmsResAdapter);

        if(msgDstnType.equals(WizardConstants.__QUEUE)) {
            connpoolresource.setConnectionDefinitionName(WizardConstants.__QUEUE_CNTN_FACTORY);
        } else {
            if(msgDstnType.equals(WizardConstants.__TOPIC)) {
                connpoolresource.setConnectionDefinitionName(WizardConstants.__TOPIC_CNTN_FACTORY);
            } else {
                assert false; //control should never reach here
            }
        }
        resources.addConnectorResource(connresource);
        resources.addConnectorConnectionPool(connpoolresource);

        ResourceUtils.createFile(location, resources, baseName);
    }

    @Override
    public void createJDBCDataSourceFromRef(String refName, String databaseInfo, File dir) {
        /*try {
            String name = refName;
            if(databaseInfo != null) {
                String vendorName = convertToValidName(databaseInfo);
                if(vendorName != null) {
                    name = vendorName;
                }

                if(vendorName.equals("derby_embedded")){ //NOI18N
                    NotifyDescriptor d = new NotifyDescriptor.Message(bundle.getString("Err_UnSupportedDerby"), NotifyDescriptor.WARNING_MESSAGE); // NOI18N
                    DialogDisplayer.getDefault().notify(d);
                    return;
                }

                // Is connection pool already defined
                String poolName = generatePoolName(name, dir, databaseInfo);
                if(poolName == null) {
                    if(resourceAlreadyDefined(refName, dir, __JdbcResource)) {
                        return;
                    } else {
                        createJDBCResource(name, refName, databaseInfo, dir);
                    }
                } else {
                    name = poolName;
                    createCPPoolResource(name, refName, databaseInfo, dir);
                    createJDBCResource(name, refName, databaseInfo, dir);
                }
            }
        } catch(IOException ex) {
            // XXX Report I/O Exception to the user.  We should do a nicely formatted
            // message identifying the problem.
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }*/
    }

    @Override
    public String createJDBCDataSourceForCmp(String beanName, String databaseInfo, File dir) {
        return null;
    }

    private boolean isLegalFilename(String filename) {
        for(int i = 0; i < ILLEGAL_FILENAME_CHARS.length; i++) {
            if(filename.indexOf(ILLEGAL_FILENAME_CHARS[i]) >= 0) {
                return false;
            }
        }

        return true;
    }

    private boolean isFriendlyFilename(String filename) {
        if(filename.indexOf(BLANK) >= 0 || filename.indexOf(DOT) >= 0) {
            return false;
        }

        return isLegalFilename(filename);
    }

    private String makeLegalFilename(String filename) {
        for(int i = 0; i < ILLEGAL_FILENAME_CHARS.length; i++) {
            filename = filename.replace(ILLEGAL_FILENAME_CHARS[i], REPLACEMENT_CHAR);
        }

        return filename;
    }

    private String makeShorterLegalFilename(String filename) {
        //To clean up the default generation a little
        if(filename.indexOf("://") != -1) { // NOI18N
            filename = filename.substring(0, filename.indexOf("://")) + "_" +  // NOI18N
                    filename.substring(filename.indexOf("://")+3, filename.length()); // NOI18N
        }
        if(filename.indexOf("//") != -1) { // NOI18N
            filename = filename.substring(0, filename.indexOf("//")) + "_" +  // NOI18N
                    filename.substring(filename.indexOf("//")+2, filename.length()); // NOI18N
        }
        filename = makeLegalFilename(filename);

        return filename;
    }

    private void ensureFolderExists(File folder) throws IOException {
        if(!folder.exists()) {
            FileUtil.createFolder(folder);
        }
    }

    private String getFileName(String beanName, String resourceType) {

        assert (beanName != null);
        assert (beanName.length() != 0);

        assert (resourceType != null);
        assert (resourceType.length() != 0);

        String fileName = resourceType;

        if(!isFriendlyFilename(beanName)) {
            beanName = makeLegalFilename(beanName);
        }

        if(!isFriendlyFilename(fileName)) {
            fileName = makeLegalFilename(fileName);
        }

        fileName = fileName + DASH + beanName + DOT + __SunResourceExt;
        return fileName;
    }

    private JdbcConnectionPool setDerbyProps(String url, JdbcConnectionPool jdbcConnectionPool){
        UrlData urlData = new UrlData(url);
        String hostName = urlData.getHostName();
        String portNumber = urlData.getPort();
        String databaseName = urlData.getDatabaseName();

        url = stripExtraDBInfo(url);
        String workingUrl = url.substring(url.indexOf("//") + 2, url.length()); //NOI18N
        PropertyElement servName = jdbcConnectionPool.newPropertyElement();
        servName.setName(WizardConstants.__ServerName);
        servName.setValue(hostName);

        PropertyElement portno = jdbcConnectionPool.newPropertyElement();
        portno.setName(WizardConstants.__DerbyPortNumber);
        portno.setValue(portNumber);

        PropertyElement dbName = jdbcConnectionPool.newPropertyElement();
        dbName.setName(WizardConstants.__DerbyDatabaseName);
        dbName.setValue(databaseName);

        String connectionAttr = getDerbyConnAttrs(workingUrl);
        if(! connectionAttr.equals("")) { //NOI18N
            PropertyElement connAttr = jdbcConnectionPool.newPropertyElement();
            connAttr.setName(WizardConstants.__DerbyConnAttr);
            connAttr.setValue(connectionAttr);
            jdbcConnectionPool.addPropertyElement(connAttr);
        }
        jdbcConnectionPool.addPropertyElement(servName);
        jdbcConnectionPool.addPropertyElement(portno);
        jdbcConnectionPool.addPropertyElement(dbName);

        return jdbcConnectionPool;
    }


    /**
     * Parses incoming url to create additional properties required by server
     * example of url : jdbc:sun:db2://serverName:portNumber;databaseName=databaseName
     * jdbc:sun:sqlserver://serverName[:portNumber]
     */
    private JdbcConnectionPool setAdditionalProps(String vendorName, String url, JdbcConnectionPool jdbcConnectionPool){
        UrlData urlData = new UrlData(url);
        String hostName = urlData.getHostName();
        String portNumber = urlData.getPort();
        String databaseName = urlData.getDatabaseName();

        url = stripExtraDBInfo(url);
        String workingUrl = url;
        if(vendorName.equals("sybase2")){ //NOI18N
            int index = url.indexOf("Tds:"); //NOI18N
            if(index != -1){
                workingUrl = url.substring(index + 4, url.length()); //NOI18N
            } else {
                return jdbcConnectionPool;
            }
        }else {
            workingUrl = url.substring(url.indexOf("//") + 2, url.length()); //NOI18N
        }
        PropertyElement servName = jdbcConnectionPool.newPropertyElement();
        servName.setName(WizardConstants.__ServerName);
        if(vendorName.contains("informix")){
            String informixSname = getInformixServerName(urlData);
            if (vendorName.equals("informix")) {
                servName.setValue(informixSname);
                PropertyElement informixhostName = jdbcConnectionPool.newPropertyElement();
                informixhostName.setName(WizardConstants.__InformixHostName);
                informixhostName.setValue(hostName);
                jdbcConnectionPool.addPropertyElement(informixhostName);
            }else{
                PropertyElement informixserver = jdbcConnectionPool.newPropertyElement();
                informixserver.setName(WizardConstants.__InformixServer);
                informixserver.setValue(informixSname);
                jdbcConnectionPool.addPropertyElement(informixserver);
                servName.setValue(hostName);
            }
        }else{
            servName.setValue(hostName);
        }
        jdbcConnectionPool.addPropertyElement(servName);
        if (Utils.notEmpty(portNumber)) {
            PropertyElement portno = jdbcConnectionPool.newPropertyElement();
            portno.setName(WizardConstants.__PortNumber);
            portno.setValue(portNumber);
            jdbcConnectionPool.addPropertyElement(portno);
        }
        if(Arrays.asList(WizardConstants.VendorsDBNameProp).contains(vendorName)) {  //NOI18N
            PropertyElement dbName = jdbcConnectionPool.newPropertyElement();
            if(vendorName.equals("sun_oracle") || vendorName.equals("datadirect_oracle")) {  //NOI18N
                dbName.setName(WizardConstants.__SID);
            }else{
                dbName.setName(WizardConstants.__DatabaseName);
            }
            if(databaseName != null) {
                dbName.setValue(databaseName);
                jdbcConnectionPool.addPropertyElement(dbName);
            }
        }

        return jdbcConnectionPool;
    }

    private String getDatasourceClassName(String vendorName, boolean isXA, Wizard wizard) {
        if(vendorName == null) {
            return null;
        }

        try {
            FieldGroup generalGroup = FieldGroupHelper.getFieldGroup(wizard, WizardConstants.__General);

            Field dsField = null;
            if (isXA) {
                dsField = FieldHelper.getField(generalGroup, WizardConstants.__XADatasourceClassname);
            } else {
                dsField = FieldHelper.getField(generalGroup, WizardConstants.__DatasourceClassname);
            }
            return FieldHelper.getConditionalFieldValue(dsField, vendorName);
        } catch(Exception ex) {
            // This should really a Schema2BeansException, but for classloader and dependency
            // purposes we're catching Exception instead.

            // XXX why do we suppress this?
            // Unable to create Wizard object -- supppress.
            LOG.log(Level.SEVERE, "getDatasourceClassName failed", ex);
        }

        return null;
    }


    public static String getDatabaseVendorName(String url, Wizard wizard) {
        String vendorName = "";
        try {
            if(wizard == null) {
               wizard = getWizardInfo();
            }
            FieldGroup propGroup = FieldGroupHelper.getFieldGroup(wizard, WizardConstants.__PropertiesURL);
            Field urlField = FieldHelper.getField(propGroup, "vendorUrls"); // NOI18N
            vendorName = FieldHelper.getOptionNameFromValue(urlField, url);
        } catch(Exception ex) {
            // This should really a Schema2BeansException, but for classloader and dependency
            // purposes we're catching Exception instead.

            // XXX why do we suppress this?
            // Unable to create Wizard object -- supppress.
            LOG.log(Level.SEVERE, "getDatabaseVendorName failed", ex);
        }

        return vendorName;
    }

    private String convertToValidName(String database) {
        database = stripExtraDBInfo(database);
        String vendorName = getDatabaseVendorName(database, null);
        if(vendorName != null) {
            if(!vendorName.equals("")) { // NOI18N
                if(!isFriendlyFilename(vendorName)) {
                    vendorName = makeLegalFilename(vendorName);
                }
            } else {
                vendorName = makeShorterLegalFilename(database);
            }
        }
        return vendorName;
    }

    private String isSameDatabaseConnection(JdbcConnectionPool connPool, String databaseUrl, String username, String password) {
        String poolJndiName = null;
        UrlData urlData = new UrlData(databaseUrl);
        String prefix = urlData.getPrefix();
        PropertyElement[] pl = connPool.getPropertyElement();
        if(prefix != null && prefix.equals("jdbc:derby:")){ //NOI18N
            String hostName = urlData.getHostName();
            String portNumber = urlData.getPort();
            String databaseName = urlData.getDatabaseName();
            String hostProp = null;
            String portProp = null;
            String dbProp = null;
            String dbUser = null;
            String dbPwd = null;
            for(int i=0; i<pl.length; i++) {
                String prop = pl[i].getName();
                if(prop.equalsIgnoreCase(WizardConstants.__ServerName)) {
                    hostProp = pl[i].getValue();
                }else if(prop.equals(WizardConstants.__DerbyPortNumber)){
                    portProp = pl[i].getValue();
                }else if(prop.equals(WizardConstants.__DerbyDatabaseName)){
                    dbProp = pl[i].getValue();
                }else if(prop.equals(WizardConstants.__User)){
                    dbUser = pl[i].getValue();
                }else if(prop.equals(WizardConstants.__Password)){
                    dbPwd = pl[i].getValue();
                }
            }

            if (Utils.strEquivalent(hostName, hostProp) &&
                    Utils.strEquivalent(portNumber, portProp) &&
                    Utils.strEquivalent(databaseName, dbProp)){
                if(dbUser != null && dbPwd != null && dbUser.equals(username) && dbPwd.equals(password)){
                    poolJndiName = connPool.getName();
                }
            }
        }else{
            String hostName = ""; //NOI18N
            String portNumber = ""; //NOI18N
            String databaseName = ""; //NOI18N
            String sid = ""; //NOI18N
            String user = ""; //NOI18N
            String pwd = ""; //NOI18N
            for(int i=0; i<pl.length; i++) {
                String prop = pl[i].getName();
                if(prop.equalsIgnoreCase(WizardConstants.__ServerName)) {
                    hostName = pl[i].getValue();
                }else if(prop.equals(WizardConstants.__PortNumber)){
                    portNumber = pl[i].getValue();
                }else if(prop.equalsIgnoreCase(WizardConstants.__DatabaseName)){
                    databaseName = pl[i].getValue();
                }else if(prop.equals(WizardConstants.__SID)){
                    sid = pl[i].getValue();
                }else if(prop.equals(WizardConstants.__User)){
                    user = pl[i].getValue();
                }else if(prop.equals(WizardConstants.__Password)){
                    pwd = pl[i].getValue();
                }
            }
            String serverPort = hostName;
            if (null != portNumber && portNumber.length() > 0) {
                serverPort += ":" + portNumber; //NOI18N
            }
            if (null != databaseUrl) {
                if ((databaseUrl.indexOf(serverPort) != -1)
                        && ((databaseUrl.indexOf(databaseName) != -1)
                        || (databaseUrl.indexOf(sid) != -1))) {
                    if ((username != null && user.equals(username))
                            && (password != null && pwd.equals(password))) {
                        for (int i = 0; i < pl.length; i++) {
                            String prop = pl[i].getName();
                            if (prop.equals("URL") || prop.equals("databaseName")) { // NOI18N
                                String urlValue = pl[i].getValue();
                                if (urlValue.equals(databaseUrl)) {
                                    poolJndiName = connPool.getName();
                                    break;
                                }
                            }
                        } //for
                    }
                }
            }
        }
        return poolJndiName;
    }

    private String stripExtraDBInfo(String dbConnectionString) {
        if(dbConnectionString.indexOf("[") != -1) { //NOI18N
            dbConnectionString = dbConnectionString.substring(0, dbConnectionString.indexOf("[")).trim(); // NOI18N
        }
        return dbConnectionString;
    }

    public static void showInformation(final String msg) {
        // Asynchronous message posting.  Placed on AWT thread automatically by DialogDescriptor.
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        });
    }

    private static Wizard getWizardInfo(){
        Wizard wizard = null;
        try {
            InputStream in = Wizard.class.getClassLoader().getResourceAsStream(DATAFILE);
            wizard = Wizard.createGraph(in);
            in.close();
        } catch(Exception ex) {
            // XXX Report I/O Exception to the user.  We should do a nicely formatted
            // message identifying the problem.
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
        return wizard;
    }

    public String getDerbyServerName(String url){
        String hostName = ""; //NOI18N
        int index = url.indexOf(":"); //NOI18N
        if(index != -1) {
            hostName = url.substring(0, index);
        }else{
            index = url.indexOf("/"); //NOI18N
            if(index != -1){
                hostName = url.substring(0, index);
            }
        }
        return hostName;
    }

    public String getDerbyPortNo(String url){
        String portNumber = "";  //NOI18N
        int index = url.indexOf(":"); //NOI18N
        if(index != -1) {
            portNumber = url.substring(index + 1, url.indexOf("/")); //NOI18N
        }
        return portNumber;
    }

    public String getDerbyDatabaseName(String url){
        String databaseName = ""; //NOI18N
        int index = url.indexOf("/"); //NOI18N
        if(index != -1){
            int colonIndex = url.indexOf(";"); //NOI18N
            if(colonIndex != -1) {
                databaseName = url.substring(index + 1, colonIndex);
            } else {
                databaseName = url.substring(index + 1, url.length());
            }
        }
        return databaseName;
    }

    private String getDerbyConnAttrs(String url){
        String connAttr = ""; //NOI18N
        int colonIndex = url.indexOf(";"); //NOI18N
        if(colonIndex != -1) {
            connAttr = url.substring(colonIndex,  url.length());
        }
        return connAttr;
    }

    /**
     * Get INFORMIXSERVER additional property
     * example of url : jdbc:informix-sqli://#HOST$:#PORT$/#DB$:INFORMIXSERVER=#SERVER_NAME$
     */
    private String getInformixServerName(UrlData urlData){
        String informixServer = "";
        Map<String, String> props = urlData.getProperties();
        for (Iterator it = props.keySet().iterator(); it.hasNext();) {
            String propName = (String) it.next();
            if(propName.toLowerCase(Locale.ENGLISH).equals("informixserver")){
                informixServer = props.get(propName);
            }
        }
        return informixServer;
    }
    /***************************************** DS Management API *****************************************************************************/

    /**
     * Returns Set of SunDataSource's(JDBC Resources) that are deployed on the server.
     * Called from SunDataSourceManager.
     * SunDataSource is a combination of JDBC & JDBC Connection Pool Resources.
     * @return Set containing SunDataSource
     */
    @Override
    public HashSet getServerDataSources() {
        return ResourceUtils.getServerDataSources(this.currentDM);
    }

    /**
     * Implementation of DS Management API in ConfigurationSupport
     * SunDataSource is a combination of JDBC & JDBC Connection Pool Resources.
     * Called through ConfigurationSupportImpl
     * @return Returns Set of SunDatasource's(JDBC Resources) present in this J2EE project
     * @param dir File providing location of the project's server resource directory
     */
    @Override
    public HashSet getResources(File resourceDir) {
        return getResourcesFromFile(resourceDir);
    }

    public static HashSet getResourcesFromFile(File resourceDir) {
        HashSet serverresources = new HashSet();
        File resourceFile = getServerResourceFiles(resourceDir);
        if (resourceFile == null) {
            return serverresources;
        }

        HashSet<SunDatasource> dsources = new HashSet<SunDatasource>();
        HashMap connPools = getConnectionPools(resourceFile);
        HashMap dataSources = getJdbcResources(resourceFile);
        for (Iterator it = dataSources.values().iterator(); it.hasNext();) {
            JdbcResource datasourceBean = (JdbcResource)it.next();
            String poolName = datasourceBean.getPoolName();
            try{
                JdbcConnectionPool connectionPoolBean =(JdbcConnectionPool)connPools.get(poolName);
                String url = "";
                String username = "";
                String password = "";
                String driverClass = "";
                String serverName = "";
                String portNo = "";
                String dbName = "";
                String sid = "";
                if(connectionPoolBean != null){
                    PropertyElement[] props = connectionPoolBean.getPropertyElement();
                    String dsClass = connectionPoolBean.getDatasourceClassname();
                    String resType = connectionPoolBean.getResType();
                    for (int j = 0; j < props.length; j++) {
                        Object val = props[j].getValue();
                        String propValue = "";
                        if(val != null) {
                            propValue = val.toString();
                        }
                        String propName = props[j].getName();
                        if(propName.equalsIgnoreCase(WizardConstants.__DatabaseName)){
                            if(dsClass.indexOf("pointbase") != -1) { //NOI18N
                                url = propValue;
                            } else if(dsClass.indexOf("derby") != -1) { //NOI18N
                                dbName = propValue;
                            } else {
                                dbName = propValue;
                            }
                        }else if(propName.equalsIgnoreCase(WizardConstants.__User)) {
                            username = propValue;
                        }else if(propName.equalsIgnoreCase(WizardConstants.__Password)){
                            password = propValue;
                        }else if(propName.equalsIgnoreCase(WizardConstants.__Url)){
                            url = propValue;
                        }else if(propName.equalsIgnoreCase(WizardConstants.__ServerName)){
                            serverName = propValue;
                        }else if(propName.equalsIgnoreCase(WizardConstants.__DerbyPortNumber)){
                            portNo = propValue;
                        }else if(propName.equalsIgnoreCase(WizardConstants.__SID)){
                            sid = propValue;
                        }else if(propName.equalsIgnoreCase(WizardConstants.__Url)){
                            url = propValue;
                        }else if(propName.equalsIgnoreCase(WizardConstants.__DriverClass)){
                            driverClass = propValue;
                        }
                    }

                    if(url == null || url.equals("")){ //NOI18N
                        if(dsClass.indexOf("derby") != -1){ //NOI18N
                            url = "jdbc:derby://";
                            if(serverName != null){
                                url = url + serverName;
                                if(portNo != null  && portNo.length() > 0) {
                                    url = url + ":" + portNo; //NOI18N
                                }
                                url = url + "/" + dbName ; //NOI18N
                            }
                        } else {
                            String urlPrefix = DatabaseUtils.getUrlPrefix(dsClass, resType);
                            String vName = getDatabaseVendorName(urlPrefix, null);
                            if(serverName != null){
                                if(vName.equals("sybase2")){ //NOI18N
                                    url = urlPrefix + serverName;
                                } else {
                                    url = urlPrefix + "//" + serverName; //NOI18N
                                }
                                if(portNo != null  && portNo.length() > 0) {
                                    url = url + ":" + portNo; //NOI18N
                                }
                            }
                            if(vName.equals("sun_oracle") || vName.equals("datadirect_oracle")) { //NOI18N
                                url = url + ";SID=" + sid; //NOI18N
                            }else if(Arrays.asList(WizardConstants.Reqd_DBName).contains(vName)) {
                                url = url + ";databaseName=" + dbName; //NOI18N
                            }else if(Arrays.asList(WizardConstants.VendorsDBNameProp).contains(vName)) {
                                url = url + "/" + dbName ; //NOI18N
                            }
                        }
                    }

                    if (!(url == null || url.equals(""))) { //NOI18N
                        if (driverClass == null || driverClass.equals("")) { //NOI18N
                            DatabaseConnection databaseConnection = ResourceUtils.getDatabaseConnection(url);
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

                        SunDatasource sunResource = new SunDatasource(datasourceBean.getJndiName(), url, username, password, driverClass);
                        sunResource.setResourceDir(resourceDir);
                        dsources.add(sunResource);
                    }
                }else{
                    //Get Pool From Server
                    HashMap poolValues = ResourceUtils.getConnPoolValues(resourceDir, poolName);
                    if(! poolValues.isEmpty()){
                        url = (String)poolValues.get(WizardConstants.__Url);
                        if((url != null) && (! url.equals (""))) { //NOI18N
                            username = (String)poolValues.get(WizardConstants.__User);
                            password = (String)poolValues.get(WizardConstants.__Password);
                            driverClass = (String)poolValues.get(WizardConstants.__DriverClass);
                            SunDatasource sunResource = new SunDatasource (datasourceBean.getJndiName (), url, username, password, driverClass);
                            sunResource.setResourceDir (resourceDir);
                            dsources.add (sunResource);
                        }
                    }
                }
            }catch(Exception ex){
                //Should never happen
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Cannot construct SunDatasource for jdbc resource : " + datasourceBean.getJndiName()
                    + "with pool " + poolName); // NOI18N
            }
        }
        return dsources;
    }

    /**
     * Create SunDataSource object's defined. Called from impl of
     * ConfigurationSupport API (ConfigurationSupportImpl).
     * SunDataSource is a combination of JDBC & JDBC Connection Pool
     * Resources.
     * @return Set containing SunDataSource
     * @param jndiName JNDI Name of JDBC Resource
     * @param url Url for database referred to by this JDBC Resource's Connection Pool
     * @param username UserName for database referred to by this JDBC Resource's Connection Pool
     * @param password Password for database referred to by this JDBC Resource's Connection Pool
     * @param driver Driver ClassName for database referred to by this JDBC Resource's Connection Pool
     * @param dir File providing location of the project's server resource directory
     */
    @Override
    public Datasource createDataSource(String jndiName, String url, String username, String password, String driver, File dir, String baseName) throws DatasourceAlreadyExistsException {
        SunDatasource ds = null;
        try {
            if(isDataSourcePresent(jndiName, dir)){
                throw new DatasourceAlreadyExistsException(new SunDatasource(jndiName, url, username, password, driver));
            }
            if(url != null){
                String vendorName = convertToValidName(url);
                if(vendorName == null) {
                    vendorName = jndiName;
                }else{
                    if(vendorName.equals("derby_embedded")){ //NOI18N
                        NotifyDescriptor d = new NotifyDescriptor.Message(bundle.getString("Err_UnSupportedDerby"), NotifyDescriptor.WARNING_MESSAGE); // NOI18N
                        DialogDisplayer.getDefault().notify(d);
                        return null;
                    }
                }
                ensureFolderExists(dir);
                // Is connection pool already defined, if not create
                String poolName = createCheckForConnectionPool(vendorName, url, username, password, driver, dir, baseName);
                boolean jdbcExists = requiredResourceExists(jndiName, dir, JDBC_RESOURCE);
                if (jdbcExists) {
                    ds = null;
                } else {
                    createJDBCResource(jndiName, poolName, dir, baseName);
                    ds = new SunDatasource(jndiName, url, username, password, driver);
                }
            }
        } catch(IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
        return ds;
    }

    private void createCPPoolResource(String name, String databaseUrl, String username, String password, String driver, File resourceDir, String baseName) throws IOException {
        FileObject location = FileUtil.toFileObject(resourceDir);
        Resources resources = ResourceUtils.getServerResourcesGraph(location,
                    baseName.contains("glassfish-resources") ? Resources.VERSION_1_5 : Resources.VERSION_1_3);
        JdbcConnectionPool jdbcConnectionPool = resources.newJdbcConnectionPool();
        jdbcConnectionPool.setName(name);
        jdbcConnectionPool.setResType(WizardConstants.__Type_Datasource);

        // XXX Refactor database abstractions into own object.  For example,
        // due to lack of member data, we're parsing CPWizard.xml twice here,
        // once in getDatabaseVendorName() and again in getDatasourceClassName()
        Wizard wizard = getWizardInfo();
        String vendorName = getDatabaseVendorName(databaseUrl, wizard);
        String datasourceClassName = ""; // NOI18N
        if(!vendorName.equals("")) { // NOI18N
            datasourceClassName = getDatasourceClassName(vendorName, false, wizard);
        }

        if(datasourceClassName.equals("")) { // NOI18N
            datasourceClassName = DatabaseUtils.getDSClassName(databaseUrl);
            if(datasourceClassName == null || datasourceClassName.equals("")) { //NOI18N
                //String mess = MessageFormat.format(bundle.getString("LBL_NoDSClassName"), new Object [] { name }); // NOI18N
                //showInformation(mess);
                datasourceClassName = driver;
            }
        }
        if(datasourceClassName != null) {
            jdbcConnectionPool.setDatasourceClassname(datasourceClassName);
        }

        PropertyElement user = jdbcConnectionPool.newPropertyElement();
        user.setName(WizardConstants.__User); // NOI18N
        user.setValue(username);
        PropertyElement passElement = jdbcConnectionPool.newPropertyElement();
        passElement.setName(WizardConstants.__Password); // NOI18N
        jdbcConnectionPool.addPropertyElement(user);
        if (username != null && (password == null || password.trim().length() == 0)) {
            password = "()"; //NOI18N
        }
        passElement.setValue(password);
        jdbcConnectionPool.addPropertyElement(passElement);

        if(vendorName.equals("derby_net")) {  //NOI18N)
            jdbcConnectionPool = setDerbyProps(databaseUrl, jdbcConnectionPool);
        }else {
            if(Arrays.asList(WizardConstants.VendorsExtraProps).contains(vendorName)) {
                jdbcConnectionPool = setAdditionalProps(vendorName, databaseUrl, jdbcConnectionPool);
            }else{
                if(vendorName.equals("pointbase")) { // NOI18N
                    PropertyElement databaseOrUrl = jdbcConnectionPool.newPropertyElement();
                    databaseOrUrl.setName(WizardConstants.__DatabaseName); // NOI18N
                    databaseOrUrl.setValue(databaseUrl);
                    jdbcConnectionPool.addPropertyElement(databaseOrUrl);
                }
            }
        }

        PropertyElement url = jdbcConnectionPool.newPropertyElement();
        url.setName(WizardConstants.__Url); // NOI18N
        url.setValue(databaseUrl);
        jdbcConnectionPool.addPropertyElement(url);
        PropertyElement driverClass = jdbcConnectionPool.newPropertyElement();
        driverClass.setName(WizardConstants.__DriverClass); // NOI18N
        driverClass.setValue(driver);
        jdbcConnectionPool.addPropertyElement(driverClass);
        resources.addJdbcConnectionPool(jdbcConnectionPool);

        ResourceUtils.createFile(location, resources, baseName);
        try{
            Thread.sleep(1000);
        }catch(Exception ex){
            LOG.log(Level.SEVERE, "createCPPoolResource failed", ex);
        }
    }

    private void createJDBCResource(String jndiName, String poolName, File resourceDir, String baseName) throws IOException {
        FileObject location = FileUtil.toFileObject(resourceDir);
        Resources resources = ResourceUtils.getServerResourcesGraph(location,
                    baseName.contains("glassfish-resources") ? Resources.VERSION_1_5 : Resources.VERSION_1_3);
        JdbcResource jdbcResource = resources.newJdbcResource();
        jdbcResource.setPoolName(poolName);
        jdbcResource.setJndiName(jndiName);
        resources.addJdbcResource(jdbcResource);
        ResourceUtils.createFile(location, resources, baseName);
    }

    private static File getServerResourceFiles(File resourceDir) {
        File resourceFile = null;
        if(resourceDir != null){
            resourceFile =  ResourceUtils.getServerResourcesFile(FileUtil.toFileObject(resourceDir),true);
        }
        return resourceFile;
    }

    private static HashMap getConnectionPools(File resourceFile) {
        HashMap<String, JdbcConnectionPool> connPools = new HashMap<String, JdbcConnectionPool>();
        Resources resources = getResourcesGraph(resourceFile);
        JdbcConnectionPool[] pools = resources.getJdbcConnectionPool();
        for(int i=0; i<pools.length; i++){
            JdbcConnectionPool pool = pools[i];
            connPools.put(pool.getName(), pool);
        }
        return connPools;
    }

    private static HashMap getJdbcResources(File resourceFile) {
        HashMap<String, JdbcResource> jdbcResources = new HashMap<String, JdbcResource>();
        Resources resources = getResourcesGraph(resourceFile);
        JdbcResource[] dsources = resources.getJdbcResource();
        for(int i=0; i<dsources.length; i++){
            JdbcResource datasource = dsources[i];
            jdbcResources.put(datasource.getJndiName(), datasource);
        }
        return jdbcResources;
    }

    private HashMap getAdminObjectResources(File resourceFile) {
        HashMap<String, AdminObjectResource> aoResources = new HashMap<String, AdminObjectResource>();
        Resources resources = getResourcesGraph(resourceFile);
        AdminObjectResource[] adminObjects = resources.getAdminObjectResource();
        for(int i=0; i<adminObjects.length; i++){
            AdminObjectResource aObject = adminObjects[i];
            aoResources.put(aObject.getJndiName(), aObject);
        }
        return aoResources;
    }

    private static Resources getResourcesGraph(File resourceFile){
        Resources resourceGraph = DDProvider.getDefault().getResourcesGraph(Resources.VERSION_1_3);
        try {
            if(! resourceFile.isDirectory()){
                FileInputStream in = new FileInputStream(resourceFile);
                try {
                    resourceGraph = DDProvider.getDefault().getResourcesGraph(in);
                } finally {
                    in.close();
                }
            }
        } catch (RuntimeException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return resourceGraph;
    }

    private boolean isDataSourcePresent(String jndiName, File dir){
        boolean exists = false;
        HashMap serverResources = getDataSourceMap(getResources(dir));
        if(serverResources.containsKey(jndiName)) {
            exists = true;
        }
        return exists;
    }

    private HashMap getDataSourceMap(HashSet resources){
        HashMap<String, SunDatasource> dSources = new HashMap<String, SunDatasource>();
        for (Iterator it = resources.iterator(); it.hasNext();) {
            SunDatasource ds = (SunDatasource)it.next();
            dSources.put(ds.getJndiName(), ds);
        }
        return dSources;
    }

    /**
     *
     * @param vendorName Database vendor name
     * @param url Database url
     * @param username Username for Databse
     * @param password Password for Databse
     * @param driver Driver Class Name of Database
     * @param dir File object of the setup folder where resources are placed
     * @return Returns null if Connection Pool already exists for this database else returns
     * unique Connection PoolName.
     *
     */
    private String createCheckForConnectionPool(String vendorName, String url, String username, String password, String driver, File dir, String baseName){
        boolean createResource = true;
        String poolName = createPoolName(url, vendorName, username);
        File resourceFile = getServerResourceFiles(dir);
        if(resourceFile != null){
            HashMap pools = getConnectionPools(resourceFile);
            if (pools.containsKey(poolName)) {
                JdbcConnectionPool pool = (JdbcConnectionPool) pools.get(poolName);
                String poolJndiName = isSameDatabaseConnection(pool, url, username, password);
                //poolJndiName will be null if the connection does not exist
                if (poolJndiName == null) {
                    poolName = ResourceUtils.getUniqueResourceName(poolName, pools);
                }else{
                    createResource = false;
                }
            } else {
                for (Iterator itr = pools.values().iterator(); itr.hasNext();) {
                    JdbcConnectionPool pool = (JdbcConnectionPool) itr.next();
                    String poolJndiName = isSameDatabaseConnection(pool, url, username, password);
                    if (poolJndiName != null) {
                        poolName = poolJndiName;
                        createResource = false;
                        break;
                    }
                }
            }
        }else{
            createResource = true;
        }
        if (createResource) {
            try {
                createCPPoolResource(poolName, url, username, password, driver, dir, baseName);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        }
        return poolName;
    }

    private String createPoolName(String url, String vendorName, String username){
        UrlData urlData = new UrlData(url);
        StringBuffer poolName = new StringBuffer(vendorName);
        String dbName = getDatabaseName(urlData);
        if (dbName != null) {
            poolName.append("_" + dbName); //NOI18N
        }
        if (username != null) {
            poolName.append("_" + username); //NOI18N
        }
        poolName.append(WizardConstants.__ConnPoolSuffix);
        return poolName.toString();
    }

    private static String getDatabaseName(UrlData urlData) {
        String databaseName = urlData.getDatabaseName();
        if (databaseName == null) {
            databaseName = urlData.getAlternateDBName();
        }

        return databaseName;
    }

    /**
     * Implementation of Message Destination API in ConfigurationSupport
     * @return returns Set of SunMessageDestination's(JMS Resources) present in this J2EE project
     * @param dir File providing location of the project's server resource directory
     */
    @Override
    public HashSet getMessageDestinations(File resourceDir) {
        HashSet serverresources = new HashSet();
        File resourceFile = getServerResourceFiles(resourceDir);
        if (resourceFile == null) {
            return serverresources;
        }

        HashSet<SunMessageDestination> destinations = new HashSet<SunMessageDestination>();
        HashMap jmsResources = getAdminObjectResources(resourceFile);
        for(Iterator itr=jmsResources.values().iterator(); itr.hasNext();){
            AdminObjectResource aoBean = (AdminObjectResource)itr.next();
            String jmsName = aoBean.getJndiName();
            String type = aoBean.getResType();
            SunMessageDestination sunMessage = null;
            if(type.equals(WizardConstants.__QUEUE)){
                sunMessage = new SunMessageDestination(jmsName, MessageDestination.Type.QUEUE);
            } else {
                sunMessage = new SunMessageDestination(jmsName, MessageDestination.Type.TOPIC);
            }
            sunMessage.setResourceDir(resourceDir);
            destinations.add(sunMessage);
        }
        return destinations;
    }

    @Override
    public HashSet getServerDestinations() {
        return ResourceUtils.getServerDestinations(this.currentDM);
    }

    private boolean requiredResourceExists(String jndiName, File dir, String resType) {
        boolean resourceExists = false;
        File resourceFile = getServerResourceFiles(dir);
        if(resourceFile != null){
            HashMap resources = new HashMap();
            if(resType.equals(JDBC_RESOURCE)){
                resources = getJdbcResources(resourceFile);
            }else if(resType.equals(JMS_RESOURCE)){
                resources = getAdminObjectResources(resourceFile);
            }
            for(Iterator itr=resources.keySet().iterator(); itr.hasNext();){
                String resJndiName = (String)itr.next();
                if(resJndiName.equals(jndiName)){
                    resourceExists = true;
                    break;
                }
            }
        }
        return resourceExists;
    }
}

