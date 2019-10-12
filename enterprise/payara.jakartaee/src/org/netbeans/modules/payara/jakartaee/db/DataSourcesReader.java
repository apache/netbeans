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
package org.netbeans.modules.payara.jakartaee.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.tooling.admin.CommandGetProperty;
import org.netbeans.modules.payara.tooling.admin.ResultMap;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.modules.payara.tooling.data.PayaraVersion;
import org.netbeans.modules.payara.tooling.utils.StringPrefixTree;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;

/**
 * Reads data sources from Payara server.
 * <p/>
 * @author Tomas Kraus
 */
public class DataSourcesReader {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Payara server <code>resource</code> property type.
     */
    private static enum ResourceType {
        /** Unknown resource type. */
        UNKNOWN,

        /** JDBC connection pool. */
        CONNECTION_POOL,

        /** JDBC data source. */
        DATA_SOURCE;

        /** A <code>String</code> representation of UNKNOWN value. */
        private static final String UNKNOWN_STR = "UNKNOWN";

        /** A <code>String</code> representation of CONNECTION_POOL value. */
        private static final String CONNECTION_POOL_STR
                = "jdbc-connection-pool";

        /** A <code>String</code> representation of DATA_SOURCE value. */
        private static final String DATA_SOURCE_STR = "jdbc-resource";

        /** Stored <code>String</code> values for backward <code>String</code>
         *  conversion. */
        private static final StringPrefixTree<ResourceType> stringValues
                = new StringPrefixTree<>(false);

        static {
            stringValues.add(CONNECTION_POOL.toString(), CONNECTION_POOL);
            stringValues.add(DATA_SOURCE.toString(), DATA_SOURCE);
        }

        /**
         * Returns a <code>ResourceType</code> with a value represented
         * by the specified <code>String</code>.
         * <p/>
         * The <code>ResourceType</code> returned represents existing value
         * only if specified <code>String</code> matches any <code>String</code>
         * returned by <code>toString</code> method. Otherwise <code>null</code>
         * value is returned.
         * <p>
         * @param name Value containing <code>ResourceType</code>
         *             <code>toString</code> representation.
         * @return <code>ResourceType</code> value represented
         *         by <code>String</code> or <code>null</code> if value
         *         was not recognized.
         */
        public static ResourceType toValue(final String name) {
            if (name != null) {
                ResourceType type = stringValues.match(name.toLowerCase());
                return type != null ? type : UNKNOWN;
            } else {
                return null;
            }
        }

        /**
         * Convert <code>ResourceType</code> value to <code>String</code>.
         * <p/>
         * @return A <code>String</code> representation of the value
         *         of this object.
         */
        @Override
        public String toString() {
            switch (this) {
                case UNKNOWN:         return UNKNOWN_STR;
                case CONNECTION_POOL: return CONNECTION_POOL_STR;
                case DATA_SOURCE:     return DATA_SOURCE_STR;
                // This is unrecheable. Being here means this class does not handle
                // all possible values correctly.
                default:
                    throw new IllegalStateException("Invalid ResourceType value");
            }
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara server JDBC data sources properties search pattern. */
    public static final String DATA_SOURCES_PATTERN = "resources.*";

    /** Payara server property key split pattern. */
    public static final String PROPERTY_SPLIT_PATTERN = "\\.";

    /** Payara server resource property identifier. */
    public static final String PROPERTY_IDENT = "property";

    /** Default JDBC data source registered in GF. */
    static final String DEFAULT_DATA_SOURCE = "jdbc/__default";

    /** JavaEE 7 new default data source linked to old default data source. */
    static final String DEFAULT_DATA_SOURCE_EE7
            = "java:comp/DefaultDataSource";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara server to read data sources from. */
    private final PayaraServer server;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Payara server data sources reader.
     * <p/>
     * @param server Payara server to read data sources from.
     */
    public DataSourcesReader(final PayaraServer server) {
        this.server = server;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieve data sources from Payara server.
     * <p/>
     * @return Data sources from Payara server or <code>null</code>
     *         when retrieving of data sources failed.
     */
    public Set<Datasource> getDataSourcesFromServer() {
        Map<String, String> properties = getPropertiesFromServer();
        if (properties != null) {
            Map<String, JDBCConnectionPool> pools = new HashMap<>();
            Map<String, JDBCResource> resources = new HashMap<>();
            buildJDBCContentObjects(properties, pools, resources);
            assignConnectionPoolsToResources(pools, resources);
            // Add Java EE 7 comp/DefaultDataSource data source (since PF 4).
            if (server.getVersion().ordinal()
                    >= PayaraVersion.PF_4_1_144.ordinal()) {
                addNewJavaEE7dataSource(resources);
            }
            return new HashSet<Datasource>(resources.values());
        } else {
            return null;
        }
    }

    /**
     * Build JDBC connection pools and resources content objects
     * from server properties.
     * <p/>
     * @param properties Server <code>resources.*</code> properties map.
     * @param pools      Existing JDBC connection pools map.
     * @param resources  Existing JDBC resources map.
     */
    private void buildJDBCContentObjects(final Map<String, String> properties,
            final Map<String, JDBCConnectionPool> pools,
            final Map<String, JDBCResource> resources) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            if (key != null && key.length() > 0) {
                String[] elements = key.split(PROPERTY_SPLIT_PATTERN);
                if (elements.length >= 4) {
                    JDBCConnectionPool.PropertyType propertyType;
                    ResourceType type = ResourceType.toValue(elements[1]);
                    switch (type) {
                        case CONNECTION_POOL:
                            buildJDBCConnectionPool(
                                    pools, elements, entry.getValue());
                            break;
                        case DATA_SOURCE:
                            buildJDBCResource(
                                    resources, elements, entry.getValue());
                            break;
                    }
                }
            }
        }
    }

    /**
     * Assign JDBC connection pools to resources (data sources).
     * <p/>
     * @param pools          Existing JDBC connection pools map.
     * @param resources      Existing JDBC resources map.
     */
    private void assignConnectionPoolsToResources(
            final Map<String, JDBCConnectionPool> pools,
            final Map<String, JDBCResource> resources) {
        for (Map.Entry<String, JDBCResource> entry : resources.entrySet()) {
            JDBCResource resource = entry.getValue();
            if (resource != null && resource.getPoolName() != null) {
                resource.setPool(pools.get(resource.getPoolName()));
            }
        }
    }

    /**
     * Add new Java EE 7 <code>comp/DefaultDataSource</code> data source as old
     * default <code>jdbc/__default</code> data source clone.
     * <p/>
     * Data source will be added only if <code>jdbc/__default</code> data source
     * exists and Java EE 7 new data source is not registered yet. Old data
     * source must be already fully initialized and linked with connection pool.
     * <p/>
     * @param resources Existing JDBC resources map.
     */
    private void addNewJavaEE7dataSource(
            final Map<String, JDBCResource> resources) {
        JDBCResource defaultResource = resources.get(DEFAULT_DATA_SOURCE);
        if (defaultResource != null
                && !resources.containsKey(DEFAULT_DATA_SOURCE_EE7)) {
            resources.put(DEFAULT_DATA_SOURCE_EE7,
                    defaultResource.copy(DEFAULT_DATA_SOURCE_EE7));
        }
    }

    /**
     * Process one step of building <code>Map</code> of JDBC connection pool
     * content objects.
     * <p/>
     * @param pools          Existing JDBC connection pools map.
     * @param elements       Payara server JDBC connection pool property key
     *                       elements.
     * @param attributeValue Payara server JDBC connection pool property
     *                       value.
     */
    private void buildJDBCConnectionPool(
            final Map<String, JDBCConnectionPool> pools,
            final String[] elements, final String attributeValue) {
        JDBCConnectionPool.PropertyType propertyType = null;
        final String name = elements[2];
        switch(elements.length) {
            case 4:
                propertyType = null;
                break;
            case 5:
                if (PROPERTY_IDENT.equalsIgnoreCase(elements[3])) {
                    propertyType = JDBCConnectionPool
                            .PropertyType.toValue(elements[4]);
                } else {
                    propertyType = null;
                }
                break;
        }
        if (propertyType != null
                && propertyType != JDBCConnectionPool.PropertyType.UNKNOWN) {
            JDBCConnectionPool pool = getJDBCConnectionPool(pools, name);
            pool.setProperty(propertyType, attributeValue);
        }
        
    }

    /**
     * Process one step of building <code>Map</code> of JDBC resource
     * content objects.
     * <p/>
     * @param resources      Existing JDBC resources map.
     * @param elements       Payara server JDBC resource property key
     *                       elements.
     * @param attributeValue Payara server JDBC resource property value.
     */
     private void buildJDBCResource(final Map<String, JDBCResource> resources,
             final String[] elements, final String attributeValue) {
         JDBCResource.AttrType attrType = null;
         final String name = elements[2];
         if (elements.length == 4) {
             attrType = JDBCResource.AttrType.toValue(elements[3]);
         }
         if (attrType != null && attrType != JDBCResource.AttrType.UNKNOWN) {
             JDBCResource resource = getJDBCResource(resources, name);
             resource.setProperty(attrType, attributeValue);
         }
     }

     /**
     * Get existing JDBC connection pool content object from pools
     * <code>Map</code> or create new one and put it into pools <code>Map</code>
     * when no such object exists.
     * <p/>
     * @param pools Existing JDBC connection pools map.
     * @param name  JDBC connection pool name (<code>Map</code> key).
     * @return JDBC connection pool content object. Never returns
     *         <code>null</code>.
     */
    private JDBCConnectionPool getJDBCConnectionPool(
            final Map<String, JDBCConnectionPool> pools, final String name) {
        JDBCConnectionPool pool = pools.get(name);
        if (pool == null) {
            pool = new JDBCConnectionPool();
            pools.put(name, pool);
        }
        return pool;
    }

    /**
     * Get existing JDBC resources content object from resources
     * <code>Map</code> or create new one and put it into resources
     * <code>Map</code> when no such object exists.
     * <p/>
     * @param pools Existing JDBC resources map.
     * @param name  JDBC resource name (<code>Map</code> key).
     * @return JDBC resource content object. Never returns <code>null</code>.
     */
    private JDBCResource getJDBCResource(
            final Map<String, JDBCResource> resources, final String name) {
        JDBCResource resource = resources.get(name);
        if (resource == null) {
            resource = new JDBCResource();
            resources.put(name, resource);
        }
        return resource;
    }

    /**
     * Retrieve data sources properties from Payara server.
     * <p/>
     * @return Data sources properties from Payara server
     *         or <code>null</code> when retrieving using server
     *         <code>get</code> administration command failed.
     */
    private Map<String, String> getPropertiesFromServer() {
        ResultMap<String, String> result = CommandGetProperty
                .getProperties(server, DATA_SOURCES_PATTERN);
        if (result != null && result.getState() == TaskState.COMPLETED) {
            return result.getValue();
        } else {
            return null;
        }
    }

}
