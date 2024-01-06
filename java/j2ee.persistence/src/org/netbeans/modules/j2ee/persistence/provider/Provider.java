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

package org.netbeans.modules.j2ee.persistence.provider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.Property;

/**
 * This class represents a persistence provider.
 *
 * @author Erno Mononen
 */
public abstract class Provider {
    
    // constants for properties
    public static final String TABLE_GENERATION_CREATE = "tableGenerationCreate";
    public static final String TABLE_GENERATION_DROPCREATE = "tableGenerationDropCreate";
    public static final String TABLE_GENERATTION_UNKOWN = "tableGenerationUnknown";

    /**
     * Fully qualified class name of the provider.
     */
    private final String providerClass;
    
    private final Set vendorSpecificProperties;
    private final String version;
    
    
    /**
     * Creates a new instance of Provider
     */
    protected Provider(String providerClass) {
        this(providerClass, null);
    }

    /**
     * Creates a new instance of Provider
     */
    protected Provider(String providerClass, String version) {
        assert !(null == providerClass || "".equals(providerClass.trim())) : "Provider class must be given!";
        this.providerClass = providerClass;
        this.version = version;
        this.vendorSpecificProperties = initPropertyNames();
    }
    
    public abstract String getDisplayName();
    
    /**
     * @see #providerClass
     */
    public final String getProviderClass(){
        return this.providerClass;
    }

    public boolean isOnClassPath(ClassPath cp)
    {
        String classRelativePath = getProviderClass().replace('.', '/') + ".class"; //NOI18N
        boolean ret = cp.findResource(classRelativePath) != null;
        if(ret && version != null)
        {
            if(Persistence.VERSION_3_2.equals(version)) {
                ret &= cp.findResource("jakarta/persistence/criteria/CriteriaSelect.class") != null;
            } else if(Persistence.VERSION_3_1.equals(version)){
                ret &= cp.findResource("jakarta/persistence/spi/TransformerException.class") != null;
            } else if(Persistence.VERSION_3_0.equals(version)){
                ret &= cp.findResource("jakarta/persistence/Entity.class") != null;
            } else if (Persistence.VERSION_2_2.equals(version)) {
                ret &= cp.findResource("javax/persistence/TableGenerators.class") != null;
            } else if (Persistence.VERSION_2_1.equals(version)) {
                ret &= cp.findResource("javax/persistence/criteria/CriteriaUpdate.class") != null;
            } else if (Persistence.VERSION_2_0.equals(version)) {
                ret &= cp.findResource("javax/persistence/criteria/JoinType.class") != null;
            } else if (Persistence.VERSION_1_0.equals(version)) {
                ret &= cp.findResource("javax/persistence/Entity.class") != null && cp.findResource("javax/persistence/criteria/JoinType.class") == null;
            }
        }
        return ret;
    }

    protected String getVersion()
    {
        return version;
    }

    protected boolean isJakartaNamespace() {
      return getVersion()!=null && Float.parseFloat(Persistence.VERSION_2_2) < Float.parseFloat(getVersion());
    }
    
    private Set initPropertyNames(){
        Set result = new HashSet();
        result.add(getJdbcDriver());
        result.add(getJdbcUsername());
        result.add(getJdbcUrl());
        result.add(getJdbcPassword());
        result.add(getTableGenerationPropertyName());
        for (Iterator it = getUnresolvedVendorSpecificProperties().keySet().iterator(); it.hasNext();) {
            String propertyName = (String) it.next();
            result.add(propertyName);
        }
        return result;
    }
    
    /**
     * Gets the names of all provider specific properties.
     * @return Set of Strings representing names of provider specific properties.
     */
    public Set getPropertyNames(){
        return this.vendorSpecificProperties;
    }
    
    /**
     * @return the property that represents table generation strategy.
     */
    public final Property getTableGenerationProperty(String strategy, String version){
        if ("".equals(getTableGenerationPropertyName())){
            // provider doesn't support table generation
            return null;
        }
        Property result;
        if (Persistence.VERSION_3_2.equals(version)) {
            result = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_2.Property();
        } else if (Persistence.VERSION_3_1.equals(version)) {
            result = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.Property();
        } else if (Persistence.VERSION_3_0.equals(version)) {
            result = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_0.Property();
        } else if  (Persistence.VERSION_2_2.equals(version)) {
                result = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_2.Property();
        } else if  (Persistence.VERSION_2_1.equals(version)) {
                result = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.Property();
        } else if  (Persistence.VERSION_2_0.equals(version)) {
                result = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.Property();
        } else {
            result = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Property();
        }
        result.setName(getTableGenerationPropertyName());
        if (TABLE_GENERATION_CREATE.equals(strategy)){
            result.setValue(getTableGenerationCreateValue());
        } else if (TABLE_GENERATION_DROPCREATE.equals(strategy)){
            result.setValue(getTableGenerationDropCreateValue());
        } else {
            return null;
        }
        return result;
    }
    
    /**
     * @return name of the default datasource.
     */
    public final String getDefaultJtaDatasource(){
        return "jdbc/__default";
    }
    
    /**
     * default implementation is valid for jpa 2.0+
     * @return names of the property representing JDBC URL, map version-property name
     */
    public String getJdbcUrl() {
        if(isJakartaNamespace()) {
            return "jakarta.persistence.jdbc.url";
        }
        return "javax.persistence.jdbc.url";
    }
    
    /**
     * default implementation is valid for jpa 2.0+
     * @return name of the property representing JDBC driver.
     */
    public String getJdbcDriver() {
        if(isJakartaNamespace()) {
            return "jakarta.persistence.jdbc.driver";
        }
        return "javax.persistence.jdbc.driver";
    }
    
    /**
     * default implementation is valid for jpa 2.0+
     * @return name of the property representing JDBC user name.
     */
    public String getJdbcUsername() {
        if(isJakartaNamespace()) {
            return "jakarta.persistence.jdbc.user";
        }
        return "javax.persistence.jdbc.user";
    }
    
    /**
     * default implementation is valid for jpa 2.0+
     * @return name of the property representing JDBC password.
     */
    public String getJdbcPassword() {
        if(isJakartaNamespace()) {
            return "jakarta.persistence.jdbc.password";
        }
        return "javax.persistence.jdbc.password";
    }

    /**
     * Return annotation processor for metamodel generation
     */
    public String getAnnotationProcessor() {
        return null;
    }

    /**
     * Return annotation processor subpackage property name for metamodel generation for differet pu in deifferent packages
     */
    public String getAnnotationSubPackageProperty() {
        return null;
    }
    
    /**
     * default implementation is valid for jpa 2.1+
     * @return name of the property representing table generation strategy in database
     */
    public String getTableGenerationPropertyName() {
        return PersistenceUnitProperties.SCHEMA_GENERATION_DATABASE_ACTION;
    }
    
    /**
     * default implementation is valid for jpa 2.1+
     * @return value of the property that represents <tt>create tables</tt> strategy.
     */
    public String getTableGenerationCreateValue(){
        return PersistenceUnitProperties.SCHEMA_GENERATION_CREATE_ACTION;
    }
    
    /**
     * default implementation is valid for jpa 2.1+
     * @return value of the property that represents <tt>create and drop tables</tt> strategy.
     */
    public String getTableGenerationDropCreateValue(){
        return PersistenceUnitProperties.SCHEMA_GENERATION_DROP_AND_CREATE_ACTION;
    }
    
    /**
     * @return Map{@code <String, String>} containing vendor specific properties.
     */
    public abstract Map getUnresolvedVendorSpecificProperties();
    
    /**
     * @return Map{@code <String, String>} containing vendor specific properties
     * which should be set on a new unit by default.
     */
    public abstract Map getDefaultVendorSpecificProperties();
    
    /**
     * Gets a map containing provider specific name / values pairs of given
     * database connection's properties. If given connection was null, will
     * return a map containing keys (names) of properties but empty Strings as values.
     * @param connection
     * @version isn't used yt, may need to be removed
     * @return Map (key String representing name of the property, value String
     *  representing value of the property).
     */
    public final Map<String, String> getConnectionPropertiesMap(DatabaseConnection connection, String version){
        Map<String, String> result = new HashMap<>();
        result.put(getJdbcDriver(), connection != null ? connection.getDriverClass() : "");
        result.put(getJdbcUrl(), connection != null ? connection.getDatabaseURL() : "");
        result.put(getJdbcUsername(), connection != null ? connection.getUser(): "");
        // must set an empty string for password if a null password
        // was returned from the connection, see #81729
        result.put(getJdbcPassword(), 
                connection != null && connection.getPassword() != null ? connection.getPassword() : "");
        return result;
    }
    
    /**
     * @return true if this provider support table generation, false otherwise.
     */
    public final boolean supportsTableGeneration(){
        return getTableGenerationPropertyName() != null && !"".equals(getTableGenerationPropertyName().trim());
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
    
    @Override
    public int hashCode() {
        return providerClass.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Provider)){
            return false;
        }
        Provider that = (Provider) obj;
        boolean sameVersion = (getVersion()!=null && getVersion().equals(that.getVersion())) || (getVersion()==null && that.getVersion()==null);
        return getClass().equals(that.getClass()) && providerClass.equals(that.providerClass) && sameVersion;
    }
    
    
}
