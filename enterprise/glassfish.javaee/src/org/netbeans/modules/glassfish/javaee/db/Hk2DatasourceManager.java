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

package org.netbeans.modules.glassfish.javaee.db;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.glassfish.common.GlassFishState;
import org.netbeans.modules.glassfish.common.parser.TreeParser;
import org.netbeans.modules.glassfish.eecommon.api.UrlData;
import org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration;
import org.netbeans.modules.glassfish.javaee.ApplicationScopedResourcesUtils;
import org.netbeans.modules.glassfish.javaee.ApplicationScopedResourcesUtils.JndiNameResolver;
import org.netbeans.modules.glassfish.javaee.ApplicationScopedResourcesUtils.ResourceFileDescription;
import static org.netbeans.modules.glassfish.javaee.ApplicationScopedResourcesUtils.checkNamespaces;
import static org.netbeans.modules.glassfish.javaee.ApplicationScopedResourcesUtils.getJndiName;
import org.netbeans.modules.glassfish.javaee.Hk2DeploymentManager;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;
import org.netbeans.modules.glassfish.tooling.utils.OsUtils;
import org.netbeans.modules.glassfish.tooling.utils.ServerUtils;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.javaee.specs.support.api.util.JndiNamespacesDefinition;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * GlassFish server data source manager.
 * <p/>
 * @author Peter Williams, Tomas Kraus
 */
public class Hk2DatasourceManager implements DatasourceManager {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish server domain configuration file. */
    private static final String DOMAIN_XML_PATH = OsUtils.joinPaths(
            ServerUtils.GF_DOMAIN_CONFIG_DIR_NAME,
            ServerUtils.GF_DOMAIN_CONFIG_FILE_NAME);
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish server deployment manager. */
    private Hk2DeploymentManager dm;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of GlassFish server data source manager.
     * <p/>
     * @param dm GlassFish server deployment manager.
     */
    public Hk2DatasourceManager(Hk2DeploymentManager dm) {
        this.dm = dm;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieves the data sources deployed on the server.
     * <p/>
     * @return The set of data sources deployed on the server.
     * @throws ConfigurationException reports problems in retrieving data source
     *         definitions.
     */
    @Override
    public Set<Datasource> getDatasources() throws ConfigurationException {
        GlassFishServer server = dm.getCommonServerSupport().getInstance();
        String domainsDir = server.getDomainsFolder();
        String domainName = server.getDomainName();
        // Try to retrieve data sources from asadmin interface if possible
        if (GlassFishState.isOnline(server) || server.isRemote()) {
            DataSourcesReader dataSourcesReader
                    = new DataSourcesReader(server);
            Set<Datasource> dataSources = dataSourcesReader.getDataSourcesFromServer();
            // Return when data sources were retrieved successfully.
            if (dataSources != null) {
                // #250444 we have to convert JDBCResource back to SunDatasource
                // to have it all the same type in collections
                return translate(dataSources);
            }
        }
        // Fallback option to retrieve data sources from domain.xml
        // for local server
        if (!server.isRemote() && null != domainsDir) {
            // XXX This won't read app scoped DS. This does not seem to be a problem.
            File domainXml = new File(domainsDir, domainName + File.separatorChar + DOMAIN_XML_PATH);
            return readDatasources(
                    domainXml, "/domain/", null, server.getVersion(), false);
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * Deploys the given set of data sources.
     * <p/>
     * @param Set of datasources to deploy.
     * @throws ConfigurationException if there is some problem with data source
     *         configuration.
     * @throws DatasourceAlreadyExistsException if module data source(s) are
     *         conflicting with data source(s) already deployed on the server.
     */
    @Override
    public void deployDatasources(Set<Datasource> datasources) 
            throws ConfigurationException, DatasourceAlreadyExistsException {
        // Since a connection pool is not a Datasource, the deploy has to
        // happen in a different part of the deploy processing...
    }
 
    /**
     * Get {@link Datasource} objects from first available resources file.
     * <p/>
     * @param version Resources file names depend on GlassFish server version.
     * @param module  Java EE module (project).
     * @return {@link Datasource} objects found in first available file.
     */
    public static Set<Datasource> getDatasources(
            final J2eeModule module, final GlassFishVersion version) {
        Pair<File, Boolean> result = GlassfishConfiguration.getExistingResourceFile(module, version);
        if (result != null) {
            return readDatasources(result.first(), "/", module, version, result.second());
        } else {
            return new HashSet<>();
        }
    }

    private static Set<Datasource> translate(Collection<Datasource> sources) {
        Set<Datasource> ret = new HashSet<>();
        for (Datasource ds : sources) {
            ret.add(new SunDatasource(ds.getJndiName(), ds.getUrl(), ds.getUsername(), ds.getPassword(), ds.getDriverClassName()));
        }
        return ret;
    }

    // ------------------------------------------------------------------------
    //  Internal logic
    // ------------------------------------------------------------------------

    /** 
     * Get resource file for new data source creation and verify it.
     * Verifies existing resource file syntax and data sources or provides
     * a new file when no resource file exists.
     * <br/>
     * <i>Internal {@link #createDataSource(String, String, String,
     * String, String, J2eeModule, GlassFishVersion)} helper method.</i>
     * <p/>
     * @param jndiName Data source JNDI name.
     * @param url      Database URL.
     * @param username Database user name.
     * @param password Database user password.
     * @param driver   Database JDBC driver.
     * @param module   Java EE module (project).
     * @param version  GlassFish server version.
     * @param cpFinder Connection pool finder.
     * @return Resource file for new data source creation.
     * @throws ConfigurationException if there is a problem with resource
     *         file parsing.
     * @throws DatasourceAlreadyExistsException if the required data source
     *         already exists in resource file.
     */
    private static ApplicationScopedResourcesUtils.ResourceFileDescription resourceFileForDSCreation(
            final String jndiName, final String url, final String username,
            final String password, final String driver, final J2eeModule module,
            final GlassFishVersion version,final ConnectionPoolFinder cpFinder
    ) throws ConfigurationException, DatasourceAlreadyExistsException {
        Pair<File, Boolean> pair = GlassfishConfiguration.getExistingResourceFile(module, version);
        File file = pair == null ? null : pair.first();
        if (file != null && file.exists()) {
            final DuplicateJdbcResourceFinder jdbcFinder
                    = new DuplicateJdbcResourceFinder(jndiName);
            final JndiNamespaceFinder namespaceFinder = new JndiNamespaceFinder();
            List<TreeParser.Path> pathList = new ArrayList<>();
            pathList.add(new TreeParser.Path("/resources/jdbc-resource",
                    new ProxyNodeReader(jdbcFinder, namespaceFinder))); // NOI18N
            pathList.add(new TreeParser.Path("/resources/jdbc-connection-pool",
                    new ProxyNodeReader(cpFinder, namespaceFinder))); // NOI18N

            pathList.add(new TreeParser.Path("/resources/custom-resource", namespaceFinder)); // NOI18N
            pathList.add(new TreeParser.Path("/resources/mail-resource", namespaceFinder)); // NOI18N
            pathList.add(new TreeParser.Path("/resources/admin-object-resource", namespaceFinder)); // NOI18N
            pathList.add(new TreeParser.Path("/resources/connector-resource", namespaceFinder)); // NOI18N
            pathList.add(new TreeParser.Path("/resources/connector-connection-pool", namespaceFinder)); // NOI18N
            try {
                TreeParser.readXml(file, pathList);
                if(jdbcFinder.isDuplicate()) {
                    throw new DatasourceAlreadyExistsException(new SunDatasource(
                            jndiName, url, username, password, driver));
                }
            } catch(IllegalStateException ex) {
                Logger.getLogger("glassfish-javaee").log(
                        Level.INFO, ex.getLocalizedMessage(), ex);
                throw new ConfigurationException(ex.getLocalizedMessage(), ex);
            }
            return new ResourceFileDescription(file, pair.second(), namespaceFinder.getNamespaces());
        }
        pair = GlassfishConfiguration.getNewResourceFile(module, version);
        return new ResourceFileDescription(pair.first(), pair.second(), Collections.<String>emptySet());
    }
    
    /**
     * Create a data source (jdbc-resource and jdbc-connection-pool) and add it
     * to sun-resources.xml in the specified resource folder.
     * 
     * @param jndiName Data source JNDI name.
     * @param url      Database URL.
     * @param username Database user name.
     * @param password Database user password.
     * @param driver   Database JDBC driver.
     * @param module   Java EE module (project).
     * @param version  GlassFish server version.
     * @return New {@link Datasource} object.
     * @throws ConfigurationException if there is a problem with resource
     *         file parsing.
     * @throws DatasourceAlreadyExistsException if the required data source
     *         already exists in resource file.
     */
    public static Datasource createDataSource(
            final String jndiName, final String url, final String username,
            final String password, final String driver,
            final J2eeModule module, final GlassFishVersion version
    ) throws ConfigurationException, DatasourceAlreadyExistsException {
        SunDatasource ds;
        ConnectionPoolFinder cpFinder = new ConnectionPoolFinder();
        
        ResourceFileDescription fileDesc = resourceFileForDSCreation(
                jndiName, url, username, password, driver, module, version, cpFinder);

        File xmlFile = fileDesc.getFile();
        try {
            String vendorName = VendorNameMgr.vendorNameFromDbUrl(url);
            if(vendorName == null) {
                vendorName = jndiName;
            } else {
                if("derby_embedded".equals(vendorName)) {
                    // !PW FIXME display as dialog warning?
                    Logger.getLogger("glassfish-javaee").log(Level.WARNING, 
                            "Embedded derby not supported as a datasource");
                    return null;
                }
            }

            // Is there a connection pool we can reuse, or do we need to create one?
            String defaultPoolName = computePoolName(url, vendorName, username);
            Map<String, CPool> pools = cpFinder.getPoolData();
            CPool defaultPool = pools.get(defaultPoolName);
            
            String poolName = null;
            if(defaultPool != null && isSameDatabaseConnection(defaultPool, url, username, password)) {
                poolName = defaultPoolName;
            } else {
                for(CPool pool: pools.values()) {
                    if(isSameDatabaseConnection(pool, url, username, password)) {
                        poolName = pool.getPoolName();
                        break;
                    }
                }
            }
            
            if (poolName == null) {
                poolName = defaultPool == null ? defaultPoolName : generateUniqueName(defaultPoolName, pools.keySet());
                createConnectionPool(xmlFile, poolName, url, username, password, driver);
            }

            fileDesc = checkNamespaces(module, fileDesc, JndiNamespacesDefinition.getNamespace(jndiName));
            String realJndiName = getJndiName(jndiName, fileDesc);

            // create jdbc resource
            createJdbcResource(xmlFile, realJndiName, poolName);
            
            ds = new SunDatasource(realJndiName, url, username, password, driver, fileDesc.isIsApplicationScoped(), null);
        } catch(IOException ex) {
            Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getLocalizedMessage(), ex);
            throw new ConfigurationException(ex.getLocalizedMessage(), ex);
        }
        
        return ds;
    }

    /**
     * Parse resource file and build <code>Datasource</code> objects from it.
     * <p/>
     * For GlassFish server version 4 and higher new Java EE 7
     * <<code>comp/DefaultDataSource</code> data source is added if default
     * <code>jdbc/__default</code> data source exists. This does not apply
     * to calls with <code>null</code> value of <code>version</code> attribute.
     * <p/>
     * @param xmlFile      Resource file to be read. This file must exists, and must
     *                     be readable regular file containing XML.
     * @param xPathPrefix  XML path prefix,
     * @param resourcesDir Directory containing resource files.
     * @param version      GlassFish server version to determine if Java EE 7
     *                     new data source shall be added.
     * @return Data sources found in resource file.
     */
    private static Set<Datasource> readDatasources(
            final File xmlFile, final String xPathPrefix,
            final J2eeModule module, final GlassFishVersion version, boolean applicationScoped) {
        final Set<Datasource> dataSources = new HashSet<>();

        if (xmlFile.canRead()) {
            final List<JdbcResource> jdbcResources = new LinkedList<>();
            final Map<String, ConnectionPool> connectionPoolMap = new HashMap<>();

            final JndiNamespaceFinder namespaceFinder = new JndiNamespaceFinder();
            final List<TreeParser.Path> pathList = new ArrayList<>();
            pathList.add(new TreeParser.Path(xPathPrefix + "resources/jdbc-resource",
                    new ProxyNodeReader(new JdbcReader(jdbcResources), namespaceFinder)));
            pathList.add(new TreeParser.Path(xPathPrefix + "resources/jdbc-connection-pool",
                    new ProxyNodeReader(new ConnectionPoolReader(connectionPoolMap), namespaceFinder)));

            pathList.add(new TreeParser.Path("/resources/custom-resource", namespaceFinder)); // NOI18N
            pathList.add(new TreeParser.Path("/resources/mail-resource", namespaceFinder)); // NOI18N
            pathList.add(new TreeParser.Path("/resources/admin-object-resource", namespaceFinder)); // NOI18N
            pathList.add(new TreeParser.Path("/resources/connector-resource", namespaceFinder)); // NOI18N
            pathList.add(new TreeParser.Path("/resources/connector-connection-pool", namespaceFinder)); // NOI18N
            try {
                TreeParser.readXml(xmlFile, pathList);
            } catch(IllegalStateException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getLocalizedMessage(), ex);
            }

            ApplicationScopedResourcesUtils.ResourceFileDescription fileDesc = new ApplicationScopedResourcesUtils.ResourceFileDescription(
                    xmlFile, applicationScoped, namespaceFinder.getNamespaces());
            JndiNameResolver resolver = applicationScoped && module != null ? new UserResolver(module, fileDesc) : null;
            for (JdbcResource jdbc : jdbcResources) {
                final ConnectionPool pool = connectionPoolMap.get(jdbc.getPoolName());
                if (pool != null) {
                    try {
                        pool.normalize();

                        // add to sun datasource list
                        final String url = pool.getProperty("URL"); //NOI18N
                        final String username = pool.getProperty("User"); //NOI18N
                        final String password = pool.getProperty("Password"); //NOI18N
                        final String driverClassName = pool.getProperty("driverClass"); //NOI18N

                        final SunDatasource dataSource = new SunDatasource(
                                jdbc.getJndiName(), url, username, password, driverClassName, applicationScoped, resolver);
                        dataSources.add(dataSource);
                        // Add Java EE 7 comp/DefaultDataSource data source
                        // as jdbc/__default clone (since GF 4).
                        if (version != null && version.ordinal()
                                >= GlassFishVersion.GF_4.ordinal()
                                && DataSourcesReader.DEFAULT_DATA_SOURCE
                                .equals(jdbc.getJndiName()) ) {
                            dataSources.add(dataSource.copy(
                                    DataSourcesReader.DEFAULT_DATA_SOURCE_EE7));
                        }
                    } catch (NullPointerException npe) {
                        Logger.getLogger("glassfish-javaee").log(Level.INFO, pool.toString(), npe);
                    }
                }
            }
        }        
        return dataSources;
    }

    private static class JdbcResource {

        private final String jndiName;
        private final String poolName;
        
        public JdbcResource(String jndiName) {
            this(jndiName, "");
        }
        
        public JdbcResource(String jndiName, String poolName) {
            this.jndiName = jndiName;
            this.poolName = poolName;
        }
        
        public String getJndiName() {
            return jndiName;
        }

        public String getPoolName() {
            return poolName;
        }
    }

    private static class JdbcReader extends TreeParser.NodeReader {

        private final List<JdbcResource> resources;
        
        public JdbcReader(List<JdbcResource> resources) {
            this.resources = resources;
        }
        
        // <jdbc-resource 
        //      enabled="true" 
        //      pool-name="DerbyPool" 
        //      jndi-name="jdbc/__default" 
        //      object-type="user" />
        
        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            String type = attributes.getValue("object-type");
            
            String jndiName = attributes.getValue("jndi-name");
            String poolName = attributes.getValue("pool-name");
            if(jndiName != null && jndiName.length() > 0 && 
                    poolName != null && poolName.length() > 0) {
                // add to jdbc resource list
                resources.add(
                        new JdbcResource(jndiName, poolName));
            }
        }
    }
    
    private static class ConnectionPool {
        
        private final Map<String, String> properties;
        
        public ConnectionPool(String poolName) {
            this.properties = new HashMap<>();
        }
        
        public void setProperty(String key, String value) {
            properties.put(key, value);
        }
        
        public String getProperty(String key) {
            return properties.get(key);
        }
        
        public void normalize() {
           DbUtil.normalizePoolMap(properties);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Hk2DatasourceManager$ConnectionPool[");
            for (Entry<String,String> e : properties.entrySet()) {
                sb.append(e.getKey());
                sb.append("=");
                sb.append(e.getValue());
                sb.append("--%%--");
            }
            return sb.append("]").toString();
        }
        
    }
    
    private static class ConnectionPoolReader extends TreeParser.NodeReader {
        
        private Map<String, ConnectionPool> resourceMap;
        private ConnectionPool currentPool;
        
        public ConnectionPoolReader(Map<String, ConnectionPool> resourceMap) {
            this.resourceMap = resourceMap;
        }
        
        //<jdbc-connection-pool 
        //        datasource-classname="org.apache.derby.jdbc.ClientDataSource" 
        //        name="DerbyPool" 
        //        res-type="javax.sql.DataSource" 
        //    <property name="PortNumber" value="1527" />
        //    <property name="Password" value="APP" />
        //    <property name="User" value="APP" />
        //    <property name="serverName" value="localhost" />
        //    <property name="DatabaseName" value="sun-appserv-samples" />
        //    <property name="connectionAttributes" value=";create=true" />
        //</jdbc-connection-pool>
    
        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            String poolName = attributes.getValue("name");
            if(poolName != null && poolName.length() > 0) {
                currentPool = new ConnectionPool(poolName);
                currentPool.setProperty("dsClassName", attributes.getValue("datasource-classname"));
                currentPool.setProperty("resType", attributes.getValue("res-type"));
                resourceMap.put(poolName, currentPool);
            } else {
                currentPool = null;
            }
        }

        @Override
        public void readChildren(String qname, Attributes attributes) throws SAXException {
            if(currentPool != null) {
                String key = attributes.getValue("name");
                if(key != null && key.length() > 0) {
                    currentPool.setProperty(key, attributes.getValue("value"));
                }
            }
        }
    }    
   
    private static String generateUniqueName(String prefix, Set<String> keys) {
        for(int i = 1; ; i++) {
            String candidate = prefix + "_" + i; // NOI18N
            if(!keys.contains(candidate)) {
                return candidate;
            }
        }
    }
    
    private static boolean isSameDatabaseConnection(final CPool pool, final String url, 
            final String username, final String password) {
        boolean result = false;
        boolean matchedSettings;
        
        UrlData urlData = new UrlData(url);
        if(DbUtil.strEmpty(pool.getUrl())) {
            matchedSettings = DbUtil.strEquivalent(urlData.getHostName(), pool.getHostname()) &&
                    DbUtil.strEquivalent(urlData.getPort(), pool.getPort()) &&
                    DbUtil.strEquivalent(urlData.getDatabaseName(), pool.getDatabaseName()) &&
                    DbUtil.strEquivalent(urlData.getSid(), pool.getSid());
        } else {
            matchedSettings = DbUtil.strEquivalent(url, pool.getUrl());
        }
        
        if(matchedSettings) {
            if(DbUtil.strEquivalent(username, pool.getUsername()) && 
                    DbUtil.strEquivalent(password, pool.getPassword())) {
                result = true;
            }
        }
        
        return result;
    }
    
    private static final String CP_TAG_1 = 
            "    <jdbc-connection-pool " +
            "allow-non-component-callers=\"false\" " +
            "associate-with-thread=\"false\" " +
            "connection-creation-retry-attempts=\"0\" " +
            "connection-creation-retry-interval-in-seconds=\"10\" " +
            "connection-leak-reclaim=\"false\" " +
            "connection-leak-timeout-in-seconds=\"0\" " +
            "connection-validation-method=\"auto-commit\" ";
    
//            "datasource-classname=\"org.postgresql.ds.PGSimpleDataSource\" " +
    private static final String ATTR_DATASOURCE_CLASSNAME = "datasource-classname";
    private static final String CP_TAG_2 = 
            "fail-all-connections=\"false\" " +
            "idle-timeout-in-seconds=\"300\" " +
            "is-connection-validation-required=\"false\" " +
            "is-isolation-level-guaranteed=\"true\" " +
            "lazy-connection-association=\"false\" " +
            "lazy-connection-enlistment=\"false\" " +
            "match-connections=\"false\" " +
            "max-connection-usage-count=\"0\" " +
            "max-pool-size=\"32\" " +
            "max-wait-time-in-millis=\"60000\" ";
//            "name=\"sawhorse-pool\" " +
    private static final String ATTR_POOL_NAME = "name";
    private static final String CP_TAG_3 = 
            "non-transactional-connections=\"false\" " +
            "pool-resize-quantity=\"2\" ";
//            "res-type=\"javax.sql.DataSource\" " +
    private static final String ATTR_RES_TYPE = "res-type";
    private static final String CP_TAG_4 = 
            "statement-timeout-in-seconds=\"-1\" " +
            "steady-pool-size=\"8\" " +
            "validate-atmost-once-period-in-seconds=\"0\" " +
            "wrap-jdbc-objects=\"false\">\n";
    private static final String PROP_SERVER_NAME = "serverName";
    private static final String PROP_PORT_NUMBER = "portNumber";
    private static final String PROP_DATABASE_NAME = "databaseName";
    private static final String PROP_USER = "User";
    private static final String PROP_PASSWORD = "Password";
    private static final String PROP_URL = "URL";
    private static final String PROP_DRIVER_CLASS = "driverClass";
    private static final String CP_TAG_5 = "    </jdbc-connection-pool>\n";
    
    private static final String RESTYPE_DATASOURCE = "javax.sql.DataSource";

    public static void createConnectionPool(File sunResourcesXml, String poolName, 
            String url, String username, String password, String driver) throws IOException {
            
//  <jdbc-connection-pool allow-non-component-callers="false" associate-with-thread="false" connection-creation-retry-attempts="0" connection-creation-retry-interval-in-seconds="10" connection-leak-reclaim="false" connection-leak-timeout-in-seconds="0" connection-validation-method="auto-commit" datasource-classname="org.postgresql.ds.PGSimpleDataSource" fail-all-connections="false" idle-timeout-in-seconds="300" is-connection-validation-required="false" is-isolation-level-guaranteed="true" lazy-connection-association="false" lazy-connection-enlistment="false" match-connections="false" max-connection-usage-count="0" max-pool-size="32" max-wait-time-in-millis="60000" name="sawhorse-pool" non-transactional-connections="false" pool-resize-quantity="2" res-type="javax.sql.DataSource" statement-timeout-in-seconds="-1" steady-pool-size="8" validate-atmost-once-period-in-seconds="0" wrap-jdbc-objects="false">
//    <property name="serverName" value="localhost"/>
//    <property name="portNumber" value="5432"/>
//    <property name="databaseName" value="cookbook2_development"/>
//    <property name="User" value="cookbook2"/>
//    <property name="Password" value="cookbook2"/>
//    <property name="URL" value="jdbc:postgresql://localhost:5432/cookbook2_development"/>
//    <property name="driverClass" value="org.postgresql.Driver"/>
//  </jdbc-connection-pool>
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        Document doc = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            docBuilder.setEntityResolver(DDResolver.getInstance());
            doc = readResourceFile(docBuilder, sunResourcesXml);
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }


        if (doc == null || docBuilder == null) return;
        NodeList resourcesNodes = doc.getElementsByTagName("resources");//NOI18N
        Node resourcesNode;
        if(resourcesNodes.getLength()<1){
            resourcesNode = doc.createElement("resources");//NOI18N
            doc.getDocumentElement().appendChild(resourcesNode);
        } else {
            resourcesNode = resourcesNodes.item(0);
        }
        Node newPool;
        try {
            newPool = resourcesNode.appendChild(doc.importNode(docBuilder.parse(new InputSource(new StringReader(CP_TAG_1+CP_TAG_2+CP_TAG_3+CP_TAG_4+CP_TAG_5))).getDocumentElement(), true));
            UrlData urlData = new UrlData(url);

            // Maybe move this logic into UrlData?
            String dsClassName = computeDataSourceClassName(url, driver);

            appendAttr(doc, newPool, ATTR_DATASOURCE_CLASSNAME, dsClassName, false);
            appendAttr(doc, newPool, ATTR_POOL_NAME, poolName, true);
            appendAttr(doc, newPool, ATTR_RES_TYPE, RESTYPE_DATASOURCE, true);
            appendProperty(doc, newPool, PROP_SERVER_NAME, urlData.getHostName(), true);
            appendProperty(doc, newPool, PROP_PORT_NUMBER, urlData.getPort(), false);
            appendProperty(doc, newPool, PROP_DATABASE_NAME, urlData.getDatabaseName(), false);
            appendProperty(doc, newPool, PROP_USER, username, true);
            // blank password is ok so check just null here and pass force=true.
            if(password != null) {
                appendProperty(doc, newPool, PROP_PASSWORD, password, true);
            }
            appendProperty(doc, newPool, PROP_URL, url, true);
            appendProperty(doc, newPool, PROP_DRIVER_CLASS, driver, true);

            Logger.getLogger("glassfish-javaee").log(Level.FINER,
                    "New connection pool resource:\n{0}", newPool.getTextContent());
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        }


        writeXmlResourceToFile(sunResourcesXml, doc);
    }
    
    private static String computeDataSourceClassName(String url, String driver) {
        String vendorName = VendorNameMgr.vendorNameFromDbUrl(url);
        String dsClassName = VendorNameMgr.dsClassNameFromVendorName(vendorName);
        
        if(dsClassName == null || dsClassName.length() == 0) {
            dsClassName = DriverMaps.getDSClassName(url);
            if(dsClassName == null || dsClassName.length() == 0) {
                dsClassName = driver;
            } 
        }
        
        return dsClassName;
    }
    
    private static String computePoolName(String url, String vendorName, String username){
        UrlData urlData = new UrlData(url);
        StringBuilder poolName = new StringBuilder(vendorName);
        String dbName = getDatabaseName(urlData);
        if (dbName != null) {
            poolName.append("_").append(dbName); //NOI18N
        }
        if (username != null) {
            poolName.append("_").append(username); //NOI18N
        }
        poolName.append("Pool"); //NOI18N
        return poolName.toString(); 
    }

    private static String getDatabaseName(UrlData urlData) {
        String databaseName = urlData.getDatabaseName();
        if (databaseName == null) {
            databaseName = urlData.getAlternateDBName();
        }

        return databaseName;
    }
    
    private static final String JDBC_TAG_1 = 
            "    <jdbc-resource " +
            "enabled=\"true\" ";
        //      pool-name="DerbyPool" 
    private static final String ATTR_POOLNAME = "pool-name";
        //      jndi-name="jdbc/__default" 
    private static final String ATTR_JNDINAME = "jndi-name";
    private static final String JDBC_TAG_2 = 
            " object-type=\"user\"/>\n";

    public static void createJdbcResource(File sunResourcesXml, String jndiName, String poolName) throws IOException {
        
        // <jdbc-resource 
        //      enabled="true" 
        //      pool-name="DerbyPool" 
        //      jndi-name="jdbc/__default" 
        //      object-type="user" />
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        Document doc = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            docBuilder.setEntityResolver(DDResolver.getInstance());
            doc = readResourceFile(docBuilder, sunResourcesXml);
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (doc == null || docBuilder == null) {
            return;
        }
        NodeList resourcesNodes = doc.getElementsByTagName("resources");//NOI18N
        Node resourcesNode = null;
        if(resourcesNodes.getLength()<1){
            throw new IOException("Malformed XML");
        } else {
            resourcesNode = resourcesNodes.item(0);
        }
        Node newJdbcRes;
        try {
            newJdbcRes = resourcesNode.appendChild(doc.importNode(
                    docBuilder.parse(new InputSource(new StringReader(JDBC_TAG_1+JDBC_TAG_2))).getDocumentElement(), true));
            appendAttr(doc, newJdbcRes, ATTR_POOLNAME, poolName, true);
            appendAttr(doc, newJdbcRes, ATTR_JNDINAME, jndiName, true);

            Logger.getLogger("glassfish-javaee").log(Level.FINER,
                    "New JDBC resource:\n{0}", newJdbcRes.getTextContent());
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        }

        writeXmlResourceToFile(sunResourcesXml, doc);
    }
    
    private static void appendAttr(Document doc, Node node, String name, String value, boolean force) {
        if(force || (name != null && name.length() > 0)) {
            Attr attr = doc.createAttribute(name);
            attr.setValue(value);
            NamedNodeMap attrs = node.getAttributes();
            attrs.setNamedItem(attr);
        }
    }
    
    private static void appendProperty(Document doc, Node node, String name, String value, boolean force) {
        if(force || (value != null && value.length() > 0)) {
            Node newProperty = doc.createElement("property");//NOI18N
            Attr nameAttr = doc.createAttribute("name");//NOI18N
            nameAttr.setValue(name);
            Attr valueAttr = doc.createAttribute("value");//NOI18N
            valueAttr.setValue(value);
            NamedNodeMap attrs = newProperty.getAttributes();
            attrs.setNamedItem(nameAttr);
            attrs.setNamedItem(valueAttr);
            node.appendChild(newProperty);
        }
    }
    
    private static final String SUN_RESOURCES_XML_HEADER =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<!DOCTYPE resources PUBLIC " +
            "\"-//Sun Microsystems, Inc.//DTD Application Server 9.0 Resource Definitions //EN\" " + 
            "\"http://www.sun.com/software/appserver/dtds/sun-resources_1_3.dtd\">\n" +
        "<resources/>";
    
    private static final String GF_RESOURCES_XML_HEADER =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<!DOCTYPE resources PUBLIC " +"\"-//GlassFish.org//DTD GlassFish Application Server 3.1 Resource Definitions//EN\" " +
            "\"http://glassfish.org/dtds/glassfish-resources_1_5.dtd\">\n" +
//            "\"-//Sun Microsystems, Inc.//DTD Application Server 9.0 Resource Definitions //EN\" " +
//            "\"http://www.sun.com/software/appserver/dtds/sun-resources_1_3.dtd\">\n" +
        "<resources/>";

    private static Document readResourceFile(DocumentBuilder docBuilder, File sunResourcesXml) throws IOException{
        boolean newOne = false;
        if(!sunResourcesXml.exists()){
            FileUtil.createData(sunResourcesXml);//ensure file exist
            newOne = true;
        }
        Document doc = null;
        try {
            if(newOne) {
                if (sunResourcesXml.getAbsolutePath().contains("sun-resources.xml"))
                    doc = docBuilder.parse(new InputSource(new StringReader(SUN_RESOURCES_XML_HEADER)));
                else
                    doc = docBuilder.parse(new InputSource(new StringReader(GF_RESOURCES_XML_HEADER)));
            }
            else   {
                doc = docBuilder.parse(sunResourcesXml);
            }
        } catch (SAXException ex) {
            throw new IOException("Malformed XML: " +ex.getMessage());
        }

        return doc;
    }


    private static void writeXmlResourceToFile(final File sunResourcesXml, final Document doc) throws IOException {

        FileObject parentFolder = FileUtil.createFolder(sunResourcesXml.getParentFile());
        FileSystem fs = parentFolder.getFileSystem();

         fs.runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                FileLock lock = null;
                OutputStream os = null;
                try {
                    FileObject sunResourcesFO = FileUtil.createData(sunResourcesXml);
                    lock = sunResourcesFO.lock();
                    os = sunResourcesFO.getOutputStream(lock);

                    XMLUtil.write(doc, os, doc.getXmlEncoding());
                } finally {
                    if(os !=null ){
                        os.close();
                    }
                    if(lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        });
    }

    private static class UserResolver implements JndiNameResolver {

        private final J2eeModule module;

        private ApplicationScopedResourcesUtils.ResourceFileDescription fileDesc;

        public UserResolver(J2eeModule module, ApplicationScopedResourcesUtils.ResourceFileDescription fileDesc) {
            this.module = module;
            this.fileDesc = fileDesc;
        }

        @Override
        public synchronized String resolveJndiName(String jndiName) {
            fileDesc = ApplicationScopedResourcesUtils.checkNamespaces(module, fileDesc,
                    JndiNamespacesDefinition.getNamespace(jndiName));
            String jndi = ApplicationScopedResourcesUtils.getJndiName(jndiName, fileDesc);
            return jndi;
        }
    }

    private static class DuplicateJdbcResourceFinder extends TreeParser.NodeReader {
        
        private final String targetJndiName;
        private boolean duplicate;
        private String poolName;
        
        public DuplicateJdbcResourceFinder(String jndiName) {
            targetJndiName = jndiName;
            duplicate = false;
            poolName = null;
        }
        
        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            String jndiName = attributes.getValue("jndi-name");
            if(targetJndiName.equals(jndiName)) {
                if(duplicate) {
                    Logger.getLogger("glassfish-javaee").log(Level.WARNING, 
                            "Duplicate jndi-names defined for JDBC resources.");
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
    
    private static class ConnectionPoolFinder extends TreeParser.NodeReader {
        
        private Map<String, String> properties = null;
        private Map<String, CPool> pools = new HashMap<>();
        
        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            properties = new HashMap<>();
            
            String poolName = attributes.getValue("name");
            if(poolName != null && poolName.length() > 0) {
                if(!pools.containsKey(poolName)) {
                    properties.put("name", poolName);
                } else {
                    Logger.getLogger("glassfish-javaee").log(Level.WARNING, 
                            "Duplicate pool-names defined for JDBC Connection Pools.");
                }
            }
        }

        @Override
        public void readChildren(String qname, Attributes attributes) throws SAXException {
            if (null != attributes && null != properties) {
                String key = attributes.getValue("name");  // NOI18N
                if(key != null && key.length() > 0) {
                    properties.put(key.toLowerCase(Locale.ENGLISH), attributes.getValue("value"));  // NOI18N
                }
            }
        }
        
        @Override
        public void endNode(String qname) throws SAXException {
            String poolName = properties.get("name");
            CPool pool = new CPool(
                    poolName,
                    properties.get("url"),
                    properties.get("servername"),
                    properties.get("portnumber"),
                    properties.get("databasename"),
                    properties.get("user"),
                    properties.get("password"),
                    properties.get("connectionattributes")
                    );
            pools.put(poolName, pool);
        }
        
        public List<String> getPoolNames() {
            return new ArrayList<>(pools.keySet());
        }
        
        public Map<String, CPool> getPoolData() {
            return Collections.unmodifiableMap(pools);
        }
        
    }

    private static class CPool {
        
        private final String poolName;
        private final String url;
        private final String hostname;
        private final String port;
        private final String databaseName;
        private final String username;
        private final String password;
        private final String sid;
        
        public CPool(String poolName, String url, String hostName, String port, 
                String databaseName, String username, String password, String sid) {
            this.poolName = poolName;
            this.url = url;
            this.hostname = hostName;
            this.port = port;
            this.databaseName = databaseName;
            this.username = username;
            this.password = password;
            this.sid = sid;
        }
        
        public String getPoolName() {
            return poolName;
        }
        
        public String getUrl() {
            return url;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public String getHostname() {
            return hostname;
        }

        public String getPassword() {
            return password;
        }

        public String getPort() {
            return port;
        }

        public String getSid() {
            return sid;
        }

        public String getUsername() {
            return username;
        }
        
    }

    private static class JndiNamespaceFinder extends TreeParser.NodeReader {

        private final Set<String> namespaces = new HashSet<>();

        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            String jndiName = attributes.getValue("jndi-name");
            if (jndiName == null) {
                jndiName = attributes.getValue("name");
            }
            if (jndiName != null) {
                String ns = JndiNamespacesDefinition.getNamespace(jndiName);
                if (ns != null) {
                    namespaces.add(ns);
                }
            }
        }

        public Set<String> getNamespaces() {
            return namespaces;
        }
    }

    private static class ProxyNodeReader extends TreeParser.NodeReader {

        private final TreeParser.NodeReader[] readers;

        public ProxyNodeReader(TreeParser.NodeReader... readers) {
            this.readers = readers;
        }

        @Override
        public void endNode(String qname) throws SAXException {
            for (TreeParser.NodeReader r : readers) {
                r.endNode(qname);
            }
        }

        @Override
        public void readCData(String qname, char[] ch, int start, int length) throws SAXException {
            for (TreeParser.NodeReader r : readers) {
                r.readCData(qname, ch, start, length);
            }
        }

        @Override
        public void readChildren(String qname, Attributes attributes) throws SAXException {
            for (TreeParser.NodeReader r : readers) {
                r.readChildren(qname, attributes);
            }
        }

        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            for (TreeParser.NodeReader r : readers) {
                r.readAttributes(qname, attributes);
            }
        }
    }

    private static class DDResolver implements EntityResolver {
        static DDResolver resolver;
        static synchronized DDResolver getInstance() {
            if (resolver==null) {
                resolver=new DDResolver();
            }
            return resolver;
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) {

            String resource=null;
            // return a proper input source
            if (systemId!=null && systemId.endsWith("sun-resources_1_3.dtd")) {
                resource="/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-resources_1_3.dtd"; //NOI18N
            }
            if (systemId!=null && systemId.endsWith("glassfish-resources_1_5.dtd")) {
                resource="/org/netbeans/modules/j2ee/sun/dd/impl/resources/glassfish-resources_1_5.dtd"; //NOI18N
            }

            if (resource==null) {
                return null;
            }
            java.net.URL url = RootInterface.class.getResource(resource);
            return new InputSource(url.toString());
        }
    }
}
