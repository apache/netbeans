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

import org.netbeans.modules.payara.tooling.utils.StringPrefixTree;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;

/**
 * Payara JDBC resource (data source) server property content.
 * <p/>
 * @author Tomas Kraus
 */
public class JDBCResource implements Datasource {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Recognized attributes in JDBC resource.
     */
    public enum AttrType {

        /** Unknown JDBC resource attribute. */
        UNKNOWN,

        /** Resource JNDI name. */
        JNDI_NAME,

        /** Resource object type. */
        OBJECT_TYPE,

        /** JDBC connection pool name. */
        POOL_NAME,

        /** Is this JDBC resource enabled? */
        ENABLED,

        /** Deployment order. */
        DEPLOYMENT_ORDER;

        /** A <code>String</code> representation of UNKNOWN value. */
        private static final String UNKNOWN_STR = "UNKNOWN";

        /** A <code>String</code> representation of JNDI_NAME value. */
        private static final String JNDI_NAME_STR = "jndi-name";

        /** A <code>String</code> representation of OBJECT_TYPE value. */
        private static final String OBJECT_TYPE_STR = "object-type";

        /** A <code>String</code> representation of UNKNOWN value. */
        private static final String POOL_NAME_STR = "pool-name";

        /** A <code>String</code> representation of ENABLED value. */
        private static final String ENABLED_STR = "enabled";

        /** A <code>String</code> representation of DEPLOYMENT_ORDER value. */
        private static final String DEPLOYMENT_ORDER_STR = "deployment-order";

        /** Stored <code>String</code> values for backward <code>String</code>
         *  conversion. */
        private static final StringPrefixTree<AttrType> stringValues
                = new StringPrefixTree<>(false);

        static {
            stringValues.add(JNDI_NAME.toString(), JNDI_NAME);
            stringValues.add(OBJECT_TYPE.toString(), OBJECT_TYPE);
            stringValues.add(POOL_NAME.toString(), POOL_NAME);
            stringValues.add(ENABLED.toString(), ENABLED);
            stringValues.add(DEPLOYMENT_ORDER.toString(), DEPLOYMENT_ORDER);
        }

        /**
         * Returns a <code>AttrType</code> with a value represented
         * by the specified <code>String</code>.
         * <p/>
         * The <code>AttrType</code> returned represents existing value
         * only if specified <code>String</code> matches any <code>String</code>
         * returned by <code>toString</code> method. Otherwise <code>null</code>
         * value is returned.
         * <p>
         * @param name Value containing <code>AttrType</code>
         *             <code>toString</code> representation.
         * @return <code>AttrType</code> value represented
         *         by <code>String</code> or <code>UNKNOWN</code> if value
         *         was not recognized.
         */
        public static AttrType toValue(final String name) {
            if (name != null) {
                AttrType type = stringValues.match(name.toLowerCase());
                return type != null ? type : UNKNOWN;
            } else {
                return null;
            }
        }

        /**
         * Convert <code>AttrType</code> value to <code>String</code>.
         * <p/>
         * @return A <code>String</code> representation of the value
         *         of this object.
         */
        @Override
        public String toString() {
            switch (this) {
                case UNKNOWN:          return UNKNOWN_STR;
                case JNDI_NAME:        return JNDI_NAME_STR;
                case OBJECT_TYPE:      return OBJECT_TYPE_STR;
                case POOL_NAME:        return POOL_NAME_STR;
                case ENABLED:          return ENABLED_STR;
                case DEPLOYMENT_ORDER: return DEPLOYMENT_ORDER_STR;
                // This is unrecheable. Being here means this class does not handle
                // all possible values correctly.
                default:
                    throw new IllegalStateException(
                            "Invalid ResourceType value");
            }
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Resource JNDI name. */
    private String jndiName;

    /** Resource object type. */
    private String objectType;

    /** JDBC connection pool name. */
    private String poolName;

    /** Is this JDBC resource enabled? Default value is false. */
    private boolean enabled;

    /** Deployment order. Negative value (usually <code>-1</code>)
     *  represents <code>null</code>. */
    private int deploymentOrder;

    /** JDBC connection pool reference. */
    private JDBCConnectionPool pool;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of JDBC resource (data source) content with
     * no values set.
     */
    public JDBCResource() {
        this.jndiName        = null;
        this.objectType      = null;
        this.poolName        = null;
        this.enabled         = false;
        this.deploymentOrder = -1;
        this.pool            = null;
    }

    /**
     * Creates an instance of JDBC resource (data source) content with
     * all values set.
     * <p/>
     * @param jndiName        Resource JNDI name.
     * @param objectType      Resource object type.
     * @param poolName        JDBC connection pool name.
     * @param enabled         Is this JDBC resource enabled?
     * @param deploymentOrder Deployment order.
     * @param pool            JDBC connection pool reference.
     */
    public JDBCResource(final String jndiName, final String objectType,
            final String poolName, final boolean enabled,
            final int deploymentOrder, final JDBCConnectionPool pool) {
        this.jndiName        = jndiName;
        this.objectType      = objectType;
        this.poolName        = poolName;
        this.enabled         = enabled;
        this.deploymentOrder = deploymentOrder;
        this.pool            = pool;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Datasource interface getters                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get resource JNDI name.
     * <p/>
     * @return Resource JNDI name.
     */
    @Override
    public String getJndiName() {
        return jndiName;
    }

    /**
     * Get data source JNDI name.
     * <p/>
     * @return Data source JNDI name.
     */
    @Override
    public String getDisplayName() {
        return jndiName;
    }

    /**
     * Get data source URL.
     * <p/>
     * @return Data source URL.
     */
    @Override
    public String getUrl() {
        return pool != null ? pool.getUrl() : null;
    }

    /**
     * Get data source user name.
     * <p/>
     * @return Data source user name.
     */
    @Override
    public String getUsername() {
        return pool != null ? pool.getUser() : null;
    }

    /**
     * Get data source user password.
     * <p/>
     * @return Data source user password.
     */
    @Override
    public String getPassword() {
        return pool != null ? pool.getPassword() : null;
    }

    /**
     * Get data source driver class name.
     * <p/>
     * @return Data source driver class name.
     */
    @Override
    public String getDriverClassName() {
        return pool != null ? pool.getDriverClass() : null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get resource object type.
     * <p/>
     * @return Resource object type.
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * Get JDBC connection pool name.
     * <p/>
     * @return JDBC connection pool name.
     */
    public String getPoolName() {
        return poolName;
    }

    /**
     * Is this JDBC resource enabled? Default value is false.
     * <p/>
     * @return  Is this JDBC resource enabled?
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get deployment order.
     * <p/>
     * Negative value (usually <code>-1</code>) represents <code>null</code>.
     * <p/>
     * @return Deployment order.
     */
    public int getDeploymentOrder() {
        return deploymentOrder;
    }

    /**
     * Get JDBC connection pool reference.
     * <p/>
     * @return JDBC connection pool reference.
     */
    public JDBCConnectionPool getPool() {
        return pool;
    }

    /**
     * Set JDBC connection pool reference.
     * <p/>
     * @param pool JDBC connection pool reference.
     */
    public void setPool(JDBCConnectionPool pool) {
        this.pool = pool;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Set property value depending on {@link AttrType} type
     * <code>enum</code> value.
     * <p/>
     * @return Value of <code>true</code> if property was set
     *         or <code>false</code> otherwise (what means that
     *         {@link Properties} type value vas <code>UNKNOWN</code>.
     */
    public boolean setProperty(AttrType type, String value) {
        switch (type) {
            case JNDI_NAME:
                this.jndiName = value;
                return true;
            case OBJECT_TYPE:
                this.objectType = value;
                return true;
            case POOL_NAME:
                this.poolName = value;
                return true;
            case ENABLED:
                this.enabled = Boolean.parseBoolean(value);
                return true;
            case DEPLOYMENT_ORDER:
                try {
                    this.deploymentOrder = Integer.parseInt(value);
                    return true;
                } catch (NumberFormatException nfe) {
                    this.deploymentOrder = -1;
                    return false;
                }
            default:
                return false;
        }
    }

    /**
     * Create a copy of existing JDBC data source object with new JNDI name.
     * <p/>
     * @param jndiName     JNDI name to be assigned to new JDBC data source
     *                     copy.
     */
    public JDBCResource copy(final String jndiName) {
        return new JDBCResource(
                jndiName, this.objectType, this.poolName,
                this.enabled, this.deploymentOrder, this.pool);
    }

}
