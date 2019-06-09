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

package org.netbeans.modules.payara.jakartaee;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.payara.common.parser.TreeParser;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author Nitya Doraisamy
 */
public class Hk2MessageDestinationManager implements  MessageDestinationDeployment {

    private Hk2DeploymentManager dm;

    private static final String DOMAIN_XML_PATH = "config/domain.xml";
    public static final String JMS_PREFIX = "jms/"; // NOI18N
    public static final String QUEUE = "javax.jms.Queue"; // NOI18N
    public static final String TOPIC = "javax.jms.Topic"; // NOI18N
    public static final String QUEUE_PROP = "PhysicalQueue"; // NOI18N
    public static final String TOPIC_PROP = "PhysicalTopic"; // NOI18N

    public static final String QUEUE_CNTN_FACTORY = "javax.jms.QueueConnectionFactory"; // NOI18N
    public static final String TOPIC_CNTN_FACTORY = "javax.jms.TopicConnectionFactory"; // NOI18N

    
    public Hk2MessageDestinationManager(Hk2DeploymentManager dm) {
        this.dm = dm;
    }

    @Override
    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        PayaraModule commonSupport = dm.getCommonServerSupport();
        String domainsDir = commonSupport.getInstanceProperties().get(PayaraModule.DOMAINS_FOLDER_ATTR);
        String domainName = commonSupport.getInstanceProperties().get(PayaraModule.DOMAIN_NAME_ATTR);
        if (null != domainsDir) {
            // XXX Fix to work with current server domain, not just default domain.
            File domainXml = new File(domainsDir, domainName + File.separatorChar + DOMAIN_XML_PATH);

            // TODO : need to account for a remote domain here?
            return readMessageDestinations(domainXml, "/domain/", null);
        } else {
            return Collections.EMPTY_SET;
        }
    }

    @Override
    public void deployMessageDestinations(Set<MessageDestination> destinations) throws ConfigurationException {
        // since a connection pool is not a Datasource, all resource deployment has to
        // happen in a different part of the deploy processing...  so this should remain
        // empty to prevent redundant processing.
    }

    // ------------------------------------------------------------------------
    //  Used by ModuleConfigurationImpl since
    // ------------------------------------------------------------------------
    public static Set<MessageDestination> getMessageDestinations(File resourceDir, String baseName) {
        File resourcesXml = new File(resourceDir, baseName+".xml");
        return readMessageDestinations(resourcesXml, "/", resourceDir);
    }
    
    private static Set<MessageDestination> readMessageDestinations(File xmlFile, String xPathPrefix, File resourcesDir) {
        Set<MessageDestination> msgDestinations = new HashSet<MessageDestination>();
        if(xmlFile.exists()) {
            Map<String, AdminObjectResource> aoResourceMap = new HashMap<String, AdminObjectResource>();
            List<TreeParser.Path> pathList = new ArrayList<TreeParser.Path>();
            pathList.add(new TreeParser.Path(xPathPrefix + "resources/admin-object-resource", new AdminObjectReader(aoResourceMap)));

            try {
                TreeParser.readXml(xmlFile, pathList);
            } catch(IllegalStateException ex) {
                Logger.getLogger("payara-jakartaee").log(Level.INFO, ex.getLocalizedMessage(), ex);
            }

            for(AdminObjectResource adminObj: aoResourceMap.values()) {
                String type = adminObj.getResType();
                if (type.equals(QUEUE)) {
                    msgDestinations.add(new SunMessageDestination(adminObj.getJndiName(), MessageDestination.Type.QUEUE, resourcesDir));
                } else {
                    msgDestinations.add(new SunMessageDestination(adminObj.getJndiName(), MessageDestination.Type.TOPIC, resourcesDir));
                }
            }
        }
        return msgDestinations;
    }

    public static MessageDestination createMessageDestination(String name, MessageDestination.Type type, File resourceDir, String baseName) throws ConfigurationException {
        SunMessageDestination msgDest;
        if(! name.startsWith(JMS_PREFIX)){
            name = JMS_PREFIX + name;
        }

        DuplicateAOFinder aoFinder = new DuplicateAOFinder(name);
        DuplicateConnectorFinder connFinder = new DuplicateConnectorFinder(name);
        ConnectorPoolFinder cpFinder = new ConnectorPoolFinder();

        File xmlFile = new File(resourceDir, baseName+".xml");
        if(xmlFile.exists()) {
            List<TreeParser.Path> pathList = new ArrayList<TreeParser.Path>();
            pathList.add(new TreeParser.Path("/resources/admin-object-resource", aoFinder));
            pathList.add(new TreeParser.Path("/resources/connector-resource", connFinder));
            pathList.add(new TreeParser.Path("/resources/connector-connection-pool", cpFinder));
            TreeParser.readXml(xmlFile, pathList);
            if(connFinder.isDuplicate()) {
               throw new ConfigurationException("Resource already exists");
            }
        }

        String connectionFactoryJndiName= name + "Factory"; // NOI18N
        String connectionFactoryPoolName = name + "FactoryPool"; // NOI18N
        try {
            createAdminObject(xmlFile, name, type);
            createConnectorConnectionPool(xmlFile, connectionFactoryPoolName, type);
            createConnector(xmlFile, connectionFactoryJndiName, connectionFactoryPoolName);
        } catch (IOException ex) {
            Logger.getLogger("payara-jakartaee").log(Level.WARNING, ex.getLocalizedMessage(), ex); // NOI18N
            throw new ConfigurationException(ex.getLocalizedMessage(), ex);
        }


        msgDest = new SunMessageDestination(name, type, resourceDir);
        return msgDest;

    }

    private static final String ATTR_JNDINAME = "jndi-name";
    private static final String ATTR_POOLNAME = "pool-name";
    private static final String ATTR_POOL_NAME = "name";
    
    private static final String AO_TAG_1 =
            "    <admin-object-resource enabled=\"true\" ";
    private static final String ATTR_RESTYPE =
            " res-type";
    private static final String AO_TAG_2 =
            " res-adapter=\"jmsra\">\n";
    private static final String PROP_NAME =
            "Name";
    private static final String AO_TAG_3 =
            "    </admin-object-resource>\n";
    public static void createAdminObject(File sunResourcesXml, String jndiName, Type type) throws IOException {
        // <admin-object-resource res-adapter="jmsra" res-type="javax.jms.Queue" jndi-name="testao"></admin-object-resource>
        StringBuilder xmlBuilder = new StringBuilder(500);
        xmlBuilder.append(AO_TAG_1);
        ResourceModifier.appendAttr(xmlBuilder, ATTR_JNDINAME, jndiName, false);
        if (MessageDestination.Type.QUEUE.equals(type)) {
            ResourceModifier.appendAttr(xmlBuilder, ATTR_RESTYPE, QUEUE, true);
            xmlBuilder.append(AO_TAG_2);
            ResourceModifier.appendProperty(xmlBuilder, PROP_NAME, QUEUE_PROP, true);
        } else if (MessageDestination.Type.TOPIC.equals(type)) {
            ResourceModifier.appendAttr(xmlBuilder, ATTR_RESTYPE, TOPIC, true);
            xmlBuilder.append(AO_TAG_2);
            ResourceModifier.appendProperty(xmlBuilder, PROP_NAME, TOPIC_PROP, true);
        }
        xmlBuilder.append(AO_TAG_3);
        String xmlFragment = xmlBuilder.toString();
        Logger.getLogger("payara-jakartaee").log(Level.FINER, "New Connector resource:\n" + xmlFragment);
        ResourceModifier.appendResource(sunResourcesXml, xmlFragment);
    }

    private static final String CONNECTOR_POOL_TAG_1 =
            "    <connector-connection-pool ";
    private static final String ATTR_CONN_DEFINITION =
            " connection-definition-name";
    private static final String CONNECTOR_POOL_TAG_2 =
            " resource-adapter-name=\"jmsra\"/>\n";
    public static void createConnectorConnectionPool(File sunResourcesXml, String poolName, Type type) throws IOException {
        //<connector-connection-pool name="testconnectorpool" resource-adapter-name="jmsra" connectiondefinition="javax.jms.ConnectionFactory"/>
        StringBuilder xmlBuilder = new StringBuilder(500);
        xmlBuilder.append(CONNECTOR_POOL_TAG_1);
        ResourceModifier.appendAttr(xmlBuilder, ATTR_POOL_NAME, poolName, true);
        if(type.equals(MessageDestination.Type.QUEUE)) {
            ResourceModifier.appendAttr(xmlBuilder, ATTR_CONN_DEFINITION, QUEUE_CNTN_FACTORY, true);
        } else if (type.equals(MessageDestination.Type.TOPIC)) {
            ResourceModifier.appendAttr(xmlBuilder, ATTR_CONN_DEFINITION, TOPIC_CNTN_FACTORY, true);
        }
        xmlBuilder.append(CONNECTOR_POOL_TAG_2);

        String xmlFragment = xmlBuilder.toString();
        Logger.getLogger("payara-jakartaee").log(Level.FINER, "New Connector Connection Pool resource:\n" + xmlFragment);
        ResourceModifier.appendResource(sunResourcesXml, xmlFragment);
    }

    private static final String CONNECTOR_TAG_1 =
            "    <connector-resource enabled=\"true\" ";
    private static final String CONNECTOR_TAG_2 =
            " />\n";
    public static void createConnector(File sunResourcesXml, String jndiName, String poolName) throws IOException {
        // <connector-resource pool-name="testconnectorpool" jndi-name="testconnector" />
        StringBuilder xmlBuilder = new StringBuilder(500);
        xmlBuilder.append(CONNECTOR_TAG_1);
        ResourceModifier.appendAttr(xmlBuilder, ATTR_JNDINAME, jndiName, true);
        ResourceModifier.appendAttr(xmlBuilder, ATTR_POOLNAME, poolName, true);
        xmlBuilder.append(CONNECTOR_TAG_2);

        String xmlFragment = xmlBuilder.toString();
        Logger.getLogger("payara-jakartaee").log(Level.FINER, "New Connector resource:\n" + xmlFragment);
        ResourceModifier.appendResource(sunResourcesXml, xmlFragment);
    }
    
    private static class AdminObjectReader extends TreeParser.NodeReader {

        private final Map<String, AdminObjectResource> resourceMap;

        public AdminObjectReader(Map<String, AdminObjectResource> resourceMap) {
            this.resourceMap = resourceMap;
        }

        //<admin-object-resource
            //enabled="true"
            //jndi-name="jms/testQ"
            //res-type="javax.jms.Queue"
            //res-adapter="jmsra"
           //</admin-object-resource>
        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            String type = attributes.getValue("object-type");

            // Ignore system resources
            if(type != null && type.startsWith("system-")) {
                return;
            }
            String jndiName = attributes.getValue("jndi-name");
            String resType = attributes.getValue("res-type");
            String resadapter = attributes.getValue("res-adapter");
            if(jndiName != null && jndiName.length() > 0 &&
                    resType != null && resType.length() > 0) {
                // add to admin object resource list
                resourceMap.put(jndiName,
                        new AdminObjectResource(jndiName, resType, resadapter));
            }
        }
    }
    
    private static class AdminObjectResource {

        private final String jndiName;
        private final String resType;
        private final String resAdapter;

        public AdminObjectResource(String jndiName, String resType, String resAdapter) {
            this.jndiName = jndiName;
            this.resType = resType;
            this.resAdapter = resAdapter;
        }

        public String getJndiName() {
            return jndiName;
        }

        public String getResType() {
            return resType;
        }

        public String getResAdapter() {
            return resAdapter;
        }
    }

    private static class DuplicateAOFinder extends TreeParser.NodeReader {

        private final String targetJndiName;
        private boolean duplicate;
        private String resType;

        public DuplicateAOFinder(String jndiName) {
            targetJndiName = jndiName;
            duplicate = false;
            resType = null;
        }

        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            String jndiName = attributes.getValue("jndi-name");
            if(targetJndiName.equals(jndiName)) {
                if(duplicate) {
                    Logger.getLogger("payara-jakartaee").log(Level.WARNING,
                            "Duplicate jndi-names defined for Admin Object resources.");
                }
                duplicate = true;
                resType = attributes.getValue("res-type");
            }
        }

        public boolean isDuplicate() {
            return duplicate;
        }

        public String getResType() {
            return resType;
        }
    }

    private static class DuplicateConnectorFinder extends TreeParser.NodeReader {

        private final String targetJndiName;
        private boolean duplicate;
        private String poolName;

        public DuplicateConnectorFinder(String jndiName) {
            targetJndiName = jndiName;
            duplicate = false;
            poolName = null;
        }

        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            String jndiName = attributes.getValue("jndi-name");
            if(targetJndiName.equals(jndiName)) {
                if(duplicate) {
                    Logger.getLogger("payara-jakartaee").log(Level.WARNING,
                            "Duplicate jndi-names defined for Connector resources.");
                }
                duplicate = true;
                poolName = attributes.getValue("pool-name");
            }
        }

        public boolean isDuplicate() {
            return duplicate;
        }

        public String getPoolName() {
            return poolName;
        }
    }

    private static class ConnectorPoolFinder extends TreeParser.NodeReader {

        private Map<String, String> properties = null;
        private Map<String, ConnectorPool> pools = new HashMap<String, ConnectorPool>();

        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            properties = new HashMap<String, String>();
            String poolName = attributes.getValue("name"); // NOI18N
            if(poolName != null && poolName.length() > 0) {
                if(!pools.containsKey(poolName)) {
                    properties.put("name", poolName); // NOI18N
                    properties.put("raname", attributes.getValue("resource-adapter-name")); // NOI18N
                    properties.put("conndefname", attributes.getValue("connection-definition-name")); // NOI18N
                } else {
                    Logger.getLogger("payara-jakartaee").log(Level.WARNING, // NOI18N
                            "Duplicate pool-names defined for Resource Adapter Pools: "+poolName); // NOI18N
                }
            }
        }

        @Override
        public void readChildren(String qname, Attributes attributes) throws SAXException {
            if (null != attributes && null != properties) {
                String key = attributes.getValue("name"); // NOI18N
                if(key != null && key.length() > 0) {
                    properties.put(key.toLowerCase(Locale.ENGLISH), attributes.getValue("value")); // NOI18N
                }
            }
        }

        @Override
        public void endNode(String qname) throws SAXException {
            String poolName = properties.get("name");
            ConnectorPool pool = new ConnectorPool(
                    poolName,
                    properties.get("raname"),
                    properties.get("conndefname")
                    );
            pools.put(poolName, pool);
        }

        public List<String> getPoolNames() {
            return new ArrayList<String>(pools.keySet());
        }

        public Map<String, ConnectorPool> getPoolData() {
            return Collections.unmodifiableMap(pools);
        }

    }

    private static class ConnectorPool {

        private final String poolName;
        private final String raName;
        private final String conndefName;

        public ConnectorPool(String poolName, String raname, String conndefname) {
            this.poolName = poolName;
            this.raName = raname;
            this.conndefName = conndefname;
        }

        public String getPoolName() {
            return poolName;
        }

        public String getRaName() {
            return raName;
        }

        public String getConndefName() {
            return conndefName;
        }
    }
}
