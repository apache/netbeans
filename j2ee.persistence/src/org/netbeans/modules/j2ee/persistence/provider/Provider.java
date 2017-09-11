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
            if(Persistence.VERSION_2_1.equals(version)){
                ret &= cp.findResource("javax/persistence/criteria/CriteriaUpdate.class") != null;
            } else if(Persistence.VERSION_2_0.equals(version)){
                ret &= cp.findResource("javax/persistence/criteria/JoinType.class") != null;
            } else if(Persistence.VERSION_1_0.equals(version)){
                ret &= cp.findResource("javax/persistence/Entity.class") != null && cp.findResource("javax/persistence/criteria/JoinType.class") == null;
            }
        }
        return ret;
    }

    protected String getVersion()
    {
        return version;
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
        if  (Persistence.VERSION_2_1.equals(version)) {
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
     * default inplementation is valid for jpa 2.0+
     * @return names of the property representing JDBC URL, map version-property name
     */
    public String getJdbcUrl() {
        return "javax.persistence.jdbc.url";
    }
    
    /**
     * default inplementation is valid for jpa 2.0+
     * @return name of the property representing JDBC driver.
     */
    public String getJdbcDriver() {
        return "javax.persistence.jdbc.driver";
    }
    
    /**
     * default inplementation is valid for jpa 2.0+
     * @return name of the property representing JDBC user name.
     */
    public String getJdbcUsername() {
        return "javax.persistence.jdbc.user";
    }
    
    /**
     * default inplementation is valid for jpa 2.0+
     * @return name of the property representing JDBC password.
     */
    public String getJdbcPassword() {
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
     * default inplementation is valid for jpa 2.1+
     * @return name of the property representing table generation strategy in database
     */
    public String getTableGenerationPropertyName() {
        return PersistenceUnitProperties.SCHEMA_GENERATION_DATABASE_ACTION;
    }
    
    /**
     * default inplementation is valid for jpa 2.1+
     * @return value of the property that represents <tt>create tables</tt> strategy.
     */
    public String getTableGenerationCreateValue(){
        return PersistenceUnitProperties.SCHEMA_GENERATION_CREATE_ACTION;
    }
    
    /**
     * default inplementation is valid for jpa 2.1+
     * @return value of the property that represents <tt>create and drop tables</tt> strategy.
     */
    public String getTableGenerationDropCreateValue(){
        return PersistenceUnitProperties.SCHEMA_GENERATION_DROP_AND_CREATE_ACTION;
    }
    
    /**
     * @return Map<String, String> containing vendor specific properties.
     */
    public abstract Map getUnresolvedVendorSpecificProperties();
    
    /**
     * @return Map<String, String> containing vendor specific properties
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
        Map<String, String> result = new HashMap<String, String>();
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
