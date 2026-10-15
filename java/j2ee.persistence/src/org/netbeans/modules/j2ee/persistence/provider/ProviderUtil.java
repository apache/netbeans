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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.persistence.api.PersistenceLocation;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.dd.common.Properties;
import org.netbeans.modules.j2ee.persistence.dd.common.Property;
import org.netbeans.modules.j2ee.persistence.editor.JPAEditorUtil;
import org.netbeans.modules.j2ee.persistence.spi.provider.PersistenceProviderSupplier;
import org.netbeans.modules.j2ee.persistence.spi.server.ServerStatusProvider;
import org.netbeans.modules.j2ee.persistence.spi.server.ServerStatusProvider2;
import org.netbeans.modules.j2ee.persistence.unit.*;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 * A utility class for handling persistence units and providers. Provides means
 * for constructing a persistence unit and for getting/setting/changing 
 * properties of persistence units.
 * 
 * @author Martin Adamek, Erno Mononen
 */
public class ProviderUtil {

    // known providers
    public static final Provider HIBERNATE_PROVIDER1_0 = new HibernateProvider(Persistence.VERSION_1_0);
    public static final Provider HIBERNATE_PROVIDER2_0 = new HibernateProvider(Persistence.VERSION_2_0);
    public static final Provider HIBERNATE_PROVIDER2_1 = new HibernateProvider(Persistence.VERSION_2_1);
    public static final Provider HIBERNATE_PROVIDER2_2 = new HibernateProvider(Persistence.VERSION_2_2);
    public static final Provider HIBERNATE_PROVIDER3_0 = new HibernateProvider(Persistence.VERSION_3_0);
    public static final Provider HIBERNATE_PROVIDER3_1 = new HibernateProvider(Persistence.VERSION_3_1);
    public static final Provider HIBERNATE_PROVIDER3_2 = new HibernateProvider(Persistence.VERSION_3_2);
    public static final Provider TOPLINK_PROVIDER1_0 = ToplinkProvider.create(Persistence.VERSION_1_0);
    public static final Provider ECLIPSELINK_PROVIDER1_0 = new EclipseLinkProvider(Persistence.VERSION_1_0);
    public static final Provider ECLIPSELINK_PROVIDER2_0 = new EclipseLinkProvider(Persistence.VERSION_2_0);
    public static final Provider ECLIPSELINK_PROVIDER2_1 = new EclipseLinkProvider(Persistence.VERSION_2_1);
    public static final Provider ECLIPSELINK_PROVIDER2_2 = new EclipseLinkProvider(Persistence.VERSION_2_2);
    public static final Provider ECLIPSELINK_PROVIDER3_0 = new EclipseLinkProvider(Persistence.VERSION_3_0);
    public static final Provider ECLIPSELINK_PROVIDER3_1 = new EclipseLinkProvider(Persistence.VERSION_3_1);
    public static final Provider ECLIPSELINK_PROVIDER3_2 = new EclipseLinkProvider(Persistence.VERSION_3_2);
    public static final Provider KODO_PROVIDER = new KodoProvider();
    public static final Provider DATANUCLEUS_PROVIDER1_0 = new DataNucleusProvider(Persistence.VERSION_1_0);
    public static final Provider DATANUCLEUS_PROVIDER2_0 = new DataNucleusProvider(Persistence.VERSION_2_0);
    public static final Provider DATANUCLEUS_PROVIDER2_1 = new DataNucleusProvider(Persistence.VERSION_2_1);
    public static final Provider DATANUCLEUS_PROVIDER2_2 = new DataNucleusProvider(Persistence.VERSION_2_2);
    public static final Provider DATANUCLEUS_PROVIDER3_0 = new DataNucleusProvider(Persistence.VERSION_3_0);
    public static final Provider DATANUCLEUS_PROVIDER3_1 = new DataNucleusProvider(Persistence.VERSION_3_1);
    public static final Provider DATANUCLEUS_PROVIDER3_2 = new DataNucleusProvider(Persistence.VERSION_3_2);
    public static final Provider OPENJPA_PROVIDER1_0 = new OpenJPAProvider(Persistence.VERSION_1_0);
    public static final Provider OPENJPA_PROVIDER2_0 = new OpenJPAProvider(Persistence.VERSION_2_0);
    public static final Provider OPENJPA_PROVIDER2_1 = new OpenJPAProvider(Persistence.VERSION_2_1);
    public static final Provider OPENJPA_PROVIDER2_2 = new OpenJPAProvider(Persistence.VERSION_2_2);
    public static final Provider DEFAULT_PROVIDER = new DefaultProvider();
    public static final Provider DEFAULT_PROVIDER2_0 = new DefaultProvider(Persistence.VERSION_2_0);
    public static final Provider DEFAULT_PROVIDER2_1 = new DefaultProvider(Persistence.VERSION_2_1);
    public static final Provider DEFAULT_PROVIDER2_2 = new DefaultProvider(Persistence.VERSION_2_2);
    public static final Provider DEFAULT_PROVIDER3_0 = new DefaultProvider(Persistence.VERSION_3_0);
    public static final Provider DEFAULT_PROVIDER3_1 = new DefaultProvider(Persistence.VERSION_3_1);
    public static final Provider DEFAULT_PROVIDER3_2 = new DefaultProvider(Persistence.VERSION_3_2);
    /**
     * TopLink provider using the provider class that was used in NetBeans 5.5. Needed
     * for maintaining backwards compatibility with persistence units created in 5.5.
     */
    private static final Provider TOPLINK_PROVIDER_55_COMPATIBLE = ToplinkProvider.create55Compatible();

    private ProviderUtil() {
    }

    /**
     * Gets the persistence provider identified by the given <code>providerClass</code>.
     * If the given class was empty or null, will return the default persistence provider
     * of the given project's target server, or null if a default provider is not supported
     * in the given project.
     *
     * @param providerClass the FQN of the class that specifies the persistence provider.
     * @param project
     *
     * @return the provider that the given providerClass represents or null if it was
     * an empty string and the project doesn't support a default (container managed)
     * persistence provider.
     */
    public static Provider getProvider(String providerClass, Project project) {

        if (null == providerClass || "".equals(providerClass.trim())) {
            return getContainerManagedProvider(project);
        }
        
        String ver = PersistenceUtils.getJPAVersion(project);
        ver = ver == null ? Persistence.VERSION_3_1 : ver;

        Provider ret = null;
        switch(ver) {
            case Persistence.VERSION_1_0:
            ret = DEFAULT_PROVIDER;
                break;
            case Persistence.VERSION_2_0:
                ret = DEFAULT_PROVIDER2_0;
                break;
            case Persistence.VERSION_2_1:
                ret = DEFAULT_PROVIDER2_1;
                break;
            case Persistence.VERSION_2_2:
                ret = DEFAULT_PROVIDER2_2;
                break;
            case Persistence.VERSION_3_0:
                ret = DEFAULT_PROVIDER3_0;
                break;
            case Persistence.VERSION_3_1:
                ret = DEFAULT_PROVIDER3_1;
                break;
            case Persistence.VERSION_3_2:
                ret = DEFAULT_PROVIDER3_2;
        }// some unknown provider
       
       for (Provider each : getAllProviders()) {
            if (each.getProviderClass().equals(providerClass.trim())) {
                ret = each;
                if(ver.equals(ProviderUtil.getVersion(each))) {
                    return each;
                }
            }
        }
        
        return ret;

    }

    /**
     * Gets the default persistence provider of the target server
     * of the given <code>project</code>.
     *
     * @return the default container managed provider for the given project or <code>null</code>
     * no default provider could be resolved.
     *
     * @throws NullPointerException if the given project was null.
     */
    private static Provider getContainerManagedProvider(Project project) {

        PersistenceProviderSupplier providerSupplier = project.getLookup().lookup(PersistenceProviderSupplier.class);

        if (providerSupplier == null
                || !providerSupplier.supportsDefaultProvider()
                || providerSupplier.getSupportedProviders().isEmpty()) {

            return null;
        }

        return providerSupplier.getSupportedProviders().get(0);
    }

    /**
     * Gets the database connection specified in the given persistence
     * unit.
     * 
     * @param pu the persistence unit whose database connection is to 
     * be retrieved; must not be null.
     * 
     * @return the connection specified in the given persistence unit or
     * <code>null</code> if it didn't specify a connection.
     * 
     */
    public static DatabaseConnection getConnection(PersistenceUnit pu) {

        Parameters.notNull("pu", pu); //NOI18N

        if (pu.getProperties() == null) {
            return null;
        }

        String url = null;
        String driver = null;
        String username = null;
        Property[] properties = pu.getProperties().getProperty2();
        Provider provider = getProvider(pu);

        for (int i = 0; i < properties.length; i++) {
            String key = properties[i].getName();
            if (key == null) {
                continue;
            }
            if (key.equals(provider.getJdbcUrl())) {
                url = properties[i].getValue();
            } else if (key.equals(provider.getJdbcDriver())) {
                driver = properties[i].getValue();
            } else if (key.equals(provider.getJdbcUsername())) {
                username = properties[i].getValue();
            }
        }
        DatabaseConnection[] connections = ConnectionManager.getDefault().getConnections();

        for (int i = 0; i < connections.length; i++) {
            DatabaseConnection c = connections[i];
            // password is problematic, when it is returned?
            if (c.getDatabaseURL().equals(url)
                    && c.getDriverClass().equals(driver)
                    && c.getUser().equals(username)) {
                return c;
            }
        }
        return null;
    }
    /**
     * Gets the database connection properties (url,name,password) specified in the given persistence
     * unit.
     * 
     * @param pu the persistence unit whose database connection is to 
     * be retrieved; must not be null.
     * 
     * @return the connection properties specified in the given persistence unit or
     * <code>null</code> if it didn't specify a connection.
     * 
     */
    public static HashMap<String, String> getConnectionProperties(PersistenceUnit pu) {

        Parameters.notNull("pu", pu); //NOI18N

        if (pu.getProperties() == null) {
            return null;
        }

        HashMap<String, String> ret = new HashMap<>();
        Property[] properties = pu.getProperties().getProperty2();
        Provider provider = getProvider(pu);

        for (int i = 0; i < properties.length; i++) {
            String key = properties[i].getName();
            if (key == null) {
                continue;
            }
            if (key.equals(provider.getJdbcUrl())) {
                ret.put(JPAEditorUtil.JDBCURLKEY, properties[i].getValue());//NOI18N
            } else if (key.equals(provider.getJdbcDriver())) {
                ret.put(JPAEditorUtil.JDBCDRIVERKEY, properties[i].getValue());
            } else if (key.equals(provider.getJdbcUsername())) {
                ret.put(JPAEditorUtil.JDBCUSERKEY, properties[i].getValue());
            }
        }
        return ret;
    }

    /**
     * Sets the given table generation strategy for given persistence unit.
     * @param persistenceUnit
     * @param tableGenerationStrategy the strategy to set, see constants in <code>Provider</code>
     * @project the project of the given persistence unit
     */
    public static void setTableGeneration(PersistenceUnit persistenceUnit, String tableGenerationStrategy, Project project) {
        String providerClass = persistenceUnit.getProvider();
        Provider provider = ProviderUtil.getProvider(providerClass, project);
        setTableGeneration(persistenceUnit, tableGenerationStrategy, provider);
    }

    /**
     * Sets the given table generation strategy for the given persistence unit.
     *
     * @param persistenceUnit the persistenceUnit to which the given strategy is to be set.
     * @param tableGenerationStrategy the strategy to set, see constants in <code>Provider</code> for
     * options.
     * @provider the provider whose table generation property will be used.
     */
    public static void setTableGeneration(PersistenceUnit persistenceUnit, String tableGenerationStrategy, Provider provider) {
        // issue 123224. The user can have a persistence.xml in J2SE project without provider specified
        if (provider == null) {
            return;
        }
        String version = Persistence.VERSION_1_0;
        if (persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_2.PersistenceUnit) {
            version = Persistence.VERSION_3_2;
        } else if (persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.PersistenceUnit) {
            version = Persistence.VERSION_3_1;
        } else if (persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_0.PersistenceUnit) {
            version = Persistence.VERSION_3_0;
        } else if (persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_2.PersistenceUnit) {
            version = Persistence.VERSION_2_2;
        } else if (persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.PersistenceUnit) {
            version = Persistence.VERSION_2_1;
        } else if (persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit) {
            version = Persistence.VERSION_2_0;
        }
        Property tableGenerationProperty = provider.getTableGenerationProperty(tableGenerationStrategy, version);
        Properties properties = persistenceUnit.getProperties();
        if (properties == null) {
            properties = persistenceUnit.newProperties();
            persistenceUnit.setProperties(properties);
        }

        Property existing = getProperty(properties.getProperty2(), provider.getTableGenerationPropertyName());

        if (existing != null && tableGenerationProperty == null) {
            properties.removeProperty2(existing);
        } else if (existing != null && tableGenerationProperty != null) {
            existing.setValue(tableGenerationProperty.getValue());
        } else if (tableGenerationProperty != null) {
            properties.addProperty2(tableGenerationProperty);
        }

    }

    /**
     * Sets the given provider, connection and table generation strategy to the given persistence unit. Note
     * that if the given persistence unit already had an existing provider, its existing  properties are not preserved
     * with the exception of the database connection properties. In other words, you have to explicitly set for
     * example a table generation strategy for the persistence unit after changing the provider.
     *
     * @param persistenceUnit the persistence unit to which the other params are to be set; must not be null.
     * @param provider the provider to set; must not be null.
     * @connection the connection to set; must not be null.
     * @tableGenerationStrategy the table generation strategy to set.
     */
    public static void setProvider(PersistenceUnit persistenceUnit, Provider provider,
            DatabaseConnection connection, String tableGenerationStrategy) {

        Parameters.notNull("persistenceUnit", persistenceUnit); //NOI18N
        // See issue 123224 desc 12 and desc 15 - connection can be null
        //Parameters.notNull("connection", connection); //NOI18N
        Parameters.notNull("provider", provider); //NOI18N

        removeProviderProperties(persistenceUnit);
        persistenceUnit.setProvider(provider.getProviderClass());
        setDatabaseConnection(persistenceUnit, provider, connection);
        setTableGeneration(persistenceUnit, tableGenerationStrategy, provider);
    }

    /**
     * Removes all provider specific properties from the given persistence unit.
     * Should be called before setting a new provider for persistence units.
     *
     * @param persistenceUnit the persistence unit whose provider specific
     * properties are to be removed; must not be null.
     */
    public static void removeProviderProperties(PersistenceUnit persistenceUnit) {
        Parameters.notNull("persistenceUnit", persistenceUnit); //NOI18N

        ArrayList<Provider> olds = getProviders(persistenceUnit);
        Set providersProperties = new HashSet();
        for (Provider old : olds) {
            providersProperties.addAll(old.getPropertyNames());
        }
        Property[] properties = getProperties(persistenceUnit);

        for (int i = 0; i < properties.length; i++) {
            Property each = properties[i];
            if (providersProperties.contains(each.getName())) {
                persistenceUnit.getProperties().removeProperty2(each);
            }
        }
        persistenceUnit.setProvider(null);

    }

    /**
     * Constructs a persistence unit based on the given paramaters. Takes care of
     * setting the default vendor specific properties (if any) to the created
     * persistence unit.
     *
     * @param name the name for the persistence unit; must not be null.
     * @param provider the provider for the persitence unit; must not be null.
     * @param connection the database connection for the persistence unit; must not be null.
     *
     * @return the created persistence unit.
     */
    public static PersistenceUnit buildPersistenceUnit(String name, Provider provider, DatabaseConnection connection, String version) {
        Parameters.notNull("name", name);
        Parameters.notNull("provider", provider);
        Parameters.notNull("connection", connection);
        PersistenceUnit persistenceUnit = null;
        if (Persistence.VERSION_3_2.equals(version)) {
            persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_2.PersistenceUnit();
        } else if (Persistence.VERSION_3_1.equals(version)) {
            persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.PersistenceUnit();
        } else if (Persistence.VERSION_3_0.equals(version)) {
            persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_0.PersistenceUnit();
        } else if (Persistence.VERSION_2_2.equals(version)) {
            persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_2.PersistenceUnit();
        } else if (Persistence.VERSION_2_1.equals(version)) {
            persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.PersistenceUnit();
        } else if (Persistence.VERSION_2_0.equals(version)) {
            persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit();
        } else {//currently default 1.0
            persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
        }
        persistenceUnit.setName(name);
        persistenceUnit.setProvider(provider.getProviderClass());
        Properties properties = persistenceUnit.newProperties();
        Map connectionProperties = provider.getConnectionPropertiesMap(connection, version);
        for (Iterator it = connectionProperties.keySet().iterator(); it.hasNext();) {
            String propertyName = (String) it.next();
            Property property = properties.newProperty();
            property.setName(propertyName);
            property.setValue((String) connectionProperties.get(propertyName));
            properties.addProperty2(property);
        }

        Map defaultProperties = provider.getDefaultVendorSpecificProperties();
        for (Iterator it = defaultProperties.keySet().iterator(); it.hasNext();) {
            String propertyName = (String) it.next();
            Property property = properties.newProperty();
            property.setName(propertyName);
            property.setValue((String) defaultProperties.get(propertyName));
            properties.addProperty2(property);
        }

        persistenceUnit.setProperties(properties);
        return persistenceUnit;
    }

    /**
     * Sets the properties of the given connection to the given persistence unit.
     * 
     * @param persistenceUnit the persistence unit to which the connection properties
     * are to be set. Must not be null.
     * @param connection the database connections whose properties are to be set. Must
     * not be null.
     */
    public static void setDatabaseConnection(PersistenceUnit persistenceUnit, DatabaseConnection connection) {
        setDatabaseConnection(persistenceUnit, null, connection);
    }

    /**
     * Sets the properties of the given connection to the given persistence unit.
     *
     * @param persistenceUnit the persistence unit to which the connection properties
     * are to be set. Must not be null.
     * @param connection the database connections whose properties are to be set. Must
     * not be null.
     * @param provider it's persistence provider,  most database connection properties are 
     * based on provider supported properties, if null provider is received from provider class from persistence unit.
     * it's better to pass provider as different providers may have the same provider class.
     */
    public static void setDatabaseConnection(PersistenceUnit persistenceUnit, Provider provider, DatabaseConnection connection) {

        Parameters.notNull("persistenceUnit", persistenceUnit); //NOI18N
        // See issue 123224 desc 12 and desc 15 - connection can be null
        //Parameters.notNull("connection", connection); //NOI18N


        if (provider == null) {
            provider = getProvider(persistenceUnit);
        }
        Property[] properties = getProperties(persistenceUnit);

        String version = Persistence.VERSION_1_0;
        if(persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_2.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version =  Persistence.VERSION_3_2;
        } else if(persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version =  Persistence.VERSION_3_1;
        } else if(persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_0.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version =  Persistence.VERSION_3_0;
        } else if(persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_2.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version =  Persistence.VERSION_2_2;
        } else if(persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version =  Persistence.VERSION_2_1;
        } else if(persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version =  Persistence.VERSION_2_0;
        }
        Map<String, String> propertiesMap = provider.getConnectionPropertiesMap(connection, version);

        for (String name : propertiesMap.keySet()) {
            Property property = getProperty(properties, name);
            if (property == null) {

                if (persistenceUnit.getProperties() == null) {
                    persistenceUnit.setProperties(persistenceUnit.newProperties());
                }

                property = persistenceUnit.getProperties().newProperty();
                property.setName(name);
                persistenceUnit.getProperties().addProperty2(property);
            }

            String value = propertiesMap.get(name);
            // value must be present (setting null would cause
            // value attribute to not be present)
            if (value == null) {
                value = "";
            }
            property.setValue(value);
        }
    }

    /**
     * Gets the properties of the given persistence unit. If the properties of
     * given unit were null, will return an empty array.
     * 
     * @return array of properties, empty if the given unit's properties were null.
     */
    static Property[] getProperties(PersistenceUnit persistenceUnit) {
        if (persistenceUnit.getProperties() != null) {
            return persistenceUnit.getProperties().getProperty2();
        }
        return new Property[0];
    }

    /**
     * @return the property from the given properties whose name matches 
     * the given propertyName
     * or null if the given properties didn't contain property with a matching name.
     */
    private static Property getProperty(Property[] properties, String propertyName) {

        if (null == properties) {
            return null;
        }

        for (int i = 0; i < properties.length; i++) {
            Property each = properties[i];
            if (each.getName() != null && each.getName().equals(propertyName)) {
                return each;
            }
        }

        return null;
    }

    /**
     * Gets the property that matches the given <code>propertyName</code> from the
     * given <code>persistenceUnit</code>.
     *
     * @return the matching property or null if the given persistence unit didn't
     * contain a property with a matching name.
     */
    public static Property getProperty(PersistenceUnit persistenceUnit, String propertyName) {
        if (persistenceUnit.getProperties() == null) {
            return null;
        }
        return getProperty(persistenceUnit.getProperties().getProperty2(), propertyName);
    }

    /**
     * Gets the persistence provider of the given persistence unit with latest version match.
     * As for now providers should be backward compartible but forward compartibility may be missed.
     * 
     * @param persistenceUnit the persistence unit whose provider is to 
     * be get. Must not be null.
     * 
     * @return the provider of the given persistence unit. In case that no specific
     * provider can be resolved <code>DEFAULT_PROVIDER</code> will be returned. prvider
     */
    public static Provider getProvider(PersistenceUnit persistenceUnit) {
        return getProvider(persistenceUnit, getAllProviders());
    }

    /**
     * Gets the persistence provider of the given persistence unit with latest version match if exact match isn't possible
     * As for now providers should be backward compartible but forward compartibility may be missed.
     *
     * @param persistenceUnit the persistence unit whose provider is to
     * be get. Must not be null.
     *
     * @return the provider of the given persistence unit. In case that no specific
     * provider can be resolved <code>DEFAULT_PROVIDER</code> will be returned. prvider
     */
    public static Provider getProvider(PersistenceUnit persistenceUnit, Provider[] providers) {
        Parameters.notNull("persistenceUnit", persistenceUnit); //NOI18N
        String version = Persistence.VERSION_1_0;
        if(persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_2.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version =  Persistence.VERSION_3_2;
        } else if(persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version =  Persistence.VERSION_3_1;
        } else if(persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_0.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version =  Persistence.VERSION_3_0;
        } else if(persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_2.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version =  Persistence.VERSION_2_2;
        } else if(persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version =  Persistence.VERSION_2_1;
        } else if(persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version =  Persistence.VERSION_2_0;
        }
        long top_version = Math.round(Double.parseDouble(version) * 100);
        long longVersion = top_version;
        Provider top_provider = null;
        if (providers == null) {
            providers = getAllProviders();
        }
        Set<Provider> candidates = new HashSet<>();
        for (Provider each : providers) {
            if (each.getProviderClass().equals(persistenceUnit.getProvider())) {
                candidates.add(each);
            }
        }
        candidates = filterProvidersByProperties(candidates, persistenceUnit.getProperties()!=null ? persistenceUnit.getProperties().getProperty2() : null);
        //
        if (candidates.size() == 1) {
            return candidates.iterator().next();
        } else {
            //unfortunatly there is no strict rule from class to provider version, but we are trying to guess best
            for (Provider each : candidates) {
                String provVersion = each.getVersion();
                if (provVersion == null) {
                    //this provider do not support versioning yet
                    return each;
                } else {
                    long cur_version = Math.round(Double.parseDouble(provVersion) * 100);
                    if (longVersion == cur_version) {
                        //exact match with jpa version, suppose provider to use based on persistence.xml version
                        top_provider = each;
                        top_version = cur_version;
                        break;
                    } else if (cur_version >= top_version) {
                        top_provider = each;
                        top_version = cur_version;
                    }
                }
            }
        }
        if(top_provider == null) {
            switch (version) {
                case Persistence.VERSION_1_0:
                    return DEFAULT_PROVIDER;
                case Persistence.VERSION_2_0:
                    return DEFAULT_PROVIDER2_0;
                case Persistence.VERSION_2_1:
                    return DEFAULT_PROVIDER2_1;
                case Persistence.VERSION_2_2:
                    return DEFAULT_PROVIDER2_2;
                case Persistence.VERSION_3_0:
                    return DEFAULT_PROVIDER3_0;
                case Persistence.VERSION_3_2:
                    return DEFAULT_PROVIDER3_2;
                default:
                    return DEFAULT_PROVIDER3_1;
            }// some unknown provider
        }
        return top_provider;
    }

    //analize properties for best match provider version
    private static Set<Provider> filterProvidersByProperties(Set<Provider> providers, Property[] properties){
        Set<Provider> ret = null;
        if(providers == null){}
        else if(providers.size()<= 1 || properties==null || properties.length==0) {
            ret = new HashSet(providers);
        } else {
            ret = new HashSet(providers);
            HashMap <Integer, ArrayList<Provider>> rates = new HashMap<>();
            int lowrate = Integer.MAX_VALUE;
            for(Provider each : providers){
                int rate = 0;
                for(Property prop: properties){
                    if(each.getPropertyNames().contains(prop.getName())) {
                        rate++;
                    }
                }
                if(rates.get(rate) == null) {
                    rates.put(rate, new ArrayList<Provider>());
                }
                rates.get(rate).add(each);
                if(rate<lowrate) {
                    lowrate=rate;
                }
            }
            if(rates.size()>1){
                for(Provider prov:rates.get(lowrate)) {
                    ret.remove(prov);
                }
            }
        }
        return ret;
    }

    /**
     * 
     * @param provider shouldn't be null
     * @return jpa version for the provider
     */
    public static String getVersion(Provider provider) {
        return provider.getVersion();
    }

    /**
     * Gets the all versions of persistence providers of the given persistence unit
     *
     * @param persistenceUnit the persistence unit whose provider is to
     * be get. Must not be null.
     *
     * @return the providers of the given persistence unit. In case that no specific
     * provider can be resolved <code>DEFAULT_PROVIDER</code> will be returned. prvider
     */
    public static ArrayList<Provider> getProviders(PersistenceUnit persistenceUnit) {
        Parameters.notNull("persistenceUnit", persistenceUnit); //NOI18N
        ArrayList<Provider> providers = new ArrayList<>();
        for (Provider each : getAllProviders()) {
            if (each.getProviderClass().equals(persistenceUnit.getProvider())) {
                providers.add(each);
            }
        }
        if (providers.size() == 0) {
            providers.add(DEFAULT_PROVIDER3_1);
        }
        return providers;
    }

    /**
     *@return true if the given puDataObject is not null and its document is
     * parseable, false otherwise.
     */
    public static boolean isValid(PUDataObject puDataObject) {
        return null == puDataObject ? false : puDataObject.parseDocument();
    }

    /**
     * Gets the persistence units that are defined in the given <code>
     * puDataObject</code>.
     * 
     * @param puDataObject the PUDataObject whose persistence units are to be retrieved.
     * 
     * @return the persistence units specified in the given <code>puDataObject</code>
     * or an empty array if there were no persistence units defined in it.
     */
    public static PersistenceUnit[] getPersistenceUnits(PUDataObject puDataObject) {
        if (puDataObject.getPersistence() == null) {
            return new PersistenceUnit[0];
        }
        return puDataObject.getPersistence().getPersistenceUnit();
    }

    /**
     * Renames given managed class in given persistence unit.
     * @param persistenceUnit the unit that contains the class to be renamed.
     * @param newName the new name of the class.
     * @param oldName the name of the class to be renamed.
     * @param dataObject
     *
     */
    public static void renameManagedClass(PersistenceUnit persistenceUnit, String newName,
            String oldName, PUDataObject dataObject) {

        dataObject.removeClass(persistenceUnit, oldName, false);
        dataObject.addClass(persistenceUnit, newName, false);

    }

    /**
     * Removes given managed class from given persistence unit.
     * @param persistenceUnit the persistence unit from which the given class
     * is to be removed.
     * @param clazz fully qualified name of the class to be removed.
     * @param dataObject the data object representing persistence.xml.
     */
    public static void removeManagedClass(PersistenceUnit persistenceUnit, String clazz,
            PUDataObject dataObject) {

        dataObject.removeClass(persistenceUnit, clazz, false);
    }

    /**
     * Adds given managed class to given persistence unit.
     * @param persistenceUnit the persistence unit to which the given class
     * is to be added.
     * @param clazz fully qualified name of the class to be added.
     * @param dataObject the data object representing persistence.xml.
     */
    public static void addManagedClass(PersistenceUnit persistenceUnit, String clazz,
            PUDataObject dataObject) {

        dataObject.addClass(persistenceUnit, clazz, false);
    }

    /**
     * Adds the given <code>persistenceUnit</code> to the <code>PUDataObject</code>
     *  of the given <code>project</code> and saves it.
     * @param persistenceUnit the unit to be added
     * @param project the project to which the unit is to be added.
     * @throws InvalidPersistenceXmlException if the given project has an invalid persistence.xml file.
     *
     */
    public static void addPersistenceUnit(PersistenceUnit persistenceUnit, Project project) throws InvalidPersistenceXmlException {
        addPersistenceUnit(persistenceUnit, project, null);
    }

    /**
     * Adds the given <code>persistenceUnit</code> to the <code>PUDataObject</code>
     *  of the given <code>project</code>'s <code>root</code> and saves it.
     * @param persistenceUnit the unit to be added
     * @param project the project to which the unit is to be added.
     * @param root the root to which the unit is to be added
     * @throws InvalidPersistenceXmlException if the given project has an invalid persistence.xml file.
     * @since 1.55
     *
     */
    public static void addPersistenceUnit(PersistenceUnit persistenceUnit, Project project, FileObject root) throws InvalidPersistenceXmlException {
        String version = Persistence.VERSION_1_0;
        if(persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_2.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version =  Persistence.VERSION_3_2;
        } else if(persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version =  Persistence.VERSION_3_1;
        } else if(persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_0.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version =  Persistence.VERSION_3_0;
        } else if(persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_2.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version =  Persistence.VERSION_2_2;
        } else if(persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version =  Persistence.VERSION_2_1;
        } else if(persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version =  Persistence.VERSION_2_0;
        }
        PUDataObject pud = getPUDataObject(project, root, version);
        pud.addPersistenceUnit(persistenceUnit);
        pud.save();
    }

    /**
     *Gets the <code>PUDataObject</code> associated with the given <code>fo</code>.
     * 
     *@param fo the file object that has an associated <code>PUDataObject</code>. Must
     * not be null.
     * 
     *@return the <code>PUDataObject</code> associated with the given <code>fo</code>.
     * 
     *@throws IllegalArgumentException if the given <code>fo</code> is null.
     *@throws InvalidPersistenceXmlException if the given file object represents
     * an invalid persistence.xml file.
     */
    public static PUDataObject getPUDataObject(FileObject fo) throws InvalidPersistenceXmlException {
        Parameters.notNull("fo", fo); //NOI18N

        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (!(dataObject instanceof PUDataObject)) {
            throw new InvalidPersistenceXmlException(FileUtil.getFileDisplayName(fo));
        }
        return (PUDataObject) dataObject;
    }

    /**
     * Gets the PUDataObject associated with the given <code>project</code>. If there
     * was no PUDataObject (i.e. no persistence.xml) in the project, a new one
     * will be created. Use
     * {@link #getDDFile} for testing whether a project has a persistence.xml file.
     * 
     *@param project the project whose PUDataObject is to be get. Must not be null.
     *@param  version if version is specified corresponding persistence.xml will be created, otherwise version will be determined from project classpath
     * 
     *@return <code>PUDataObject</code> associated with the given project or null 
     * if there is no such <code>PUDataObject</code>.
     * 
     * @throws InvalidPersistenceXmlException if the given <code>project</code> had an existing
     * invalid persitence.xml file.
     */
    public static synchronized PUDataObject getPUDataObject(Project project, String version) throws InvalidPersistenceXmlException {
        return getPUDataObject(project, null, version);
    }

    /**
     * Gets the PUDataObject associated with the given <code>FileObject</code> within the given <code>project</code>.
     * If there was no PUDataObject (i.e. no persistence.xml) in the project associated with the given FileObject, a new one
     * will be created. Use
     * {@link #getDDFile(org.netbeans.api.project.Project, org.openide.filesystems.FileObject)} for testing whether a project has a persistence.xml file associated with the given FileObject.
     * 
     *@param project the project whose PUDataObject is to be get. Must not be null.
     *@param fo the FileObject whose PUDataObject is to be get.
     *@param version if version is specified corresponding persistence.xml will be created, otherwise version will be determined from project classpath
     * 
     *@return <code>PUDataObject</code> associated with the given FileObject or null 
     * if there is no such <code>PUDataObject</code>.
     * 
     * @throws InvalidPersistenceXmlException if the given <code>FileObject</code> had an existing
     * invalid persitence.xml file.
     * @since 1.55
     */
    public static synchronized PUDataObject getPUDataObject(Project project, FileObject fo, String version) throws InvalidPersistenceXmlException {
        Parameters.notNull("project", project); //NOI18N

        FileObject puFileObject = getDDFile(project, fo);
        if (puFileObject == null) {
            try {
                puFileObject = createPersistenceDDFile(project, fo, version);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        if (puFileObject == null) {
            return null;
        }
        return getPUDataObject(puFileObject);
    }

    /**
     * Gets the PUDataObject associated with the given <code>project</code>. If there
     * was no PUDataObject (i.e. no persistence.xml) in the project, a new one
     * will be created and version will be determined based on project classpath. Use
     * {@link #getDDFile} for testing whether a project has a persistence.xml file.
     * It's not recommended to call this method if there is no PUDataObject yet, it's better o get version first and call with version
     *
     *@param project the project whose PUDataObject is to be get. Must not be null.
     *
     *@return <code>PUDataObject</code> associated with the given project or null
     * if there is no such <code>PUDataObject</code>.
     *
     * @throws InvalidPersistenceXmlException if the given <code>project</code> had an existing
     * invalid persitence.xml file.
     */
    public static synchronized PUDataObject getPUDataObject(Project project) throws InvalidPersistenceXmlException {
        return getPUDataObject(project, null);
    }

    /**
     * Creates a new FileObject representing file that defines
     * persistence units (<tt>persistence.xml</tt>). <i>Todo: move somewhere else?</i>
     * @vers persistence version, if null will be determined from project classpath, if fails default will be 1.0
     * @return FileObject representing <tt>persistence.xml</tt>.
     */
    private static FileObject createPersistenceDDFile(Project project, FileObject fo, String vers) throws IOException {
        final FileObject persistenceLocation = PersistenceLocation.createLocation(project, fo);
        if (persistenceLocation == null) {
            return null;
        }
        final FileObject[] dd = new FileObject[1];
        //get max supported version
        String ret = vers == null ? PersistenceUtils.getJPAVersion(project) : vers;
        final String version = ret != null ? ret : Persistence.VERSION_3_1;
        // must create the file using AtomicAction, see #72058
        persistenceLocation.getFileSystem().runAtomicAction( () -> {
            dd[0] = FileUtil.copyFile(FileUtil.getConfigFile(
                    "org-netbeans-modules-j2ee-persistence/persistence-" + version + ".xml"), persistenceLocation, "persistence"); //NOI18N
        });
        PersistenceUtils.logUsage(ProviderUtil.class, "USG_PERSISTENCE_XML_CREATED", new String[]{version});
        return dd[0];
    }

    /**
     * Checks whether the given project has a persistence.xml that contains at least one
     * persistence unit.
     * 
     * @param project the project; must not be null.
     * 
     * @return true if the given project has a persistence.xml containing
     * at least one persitence unit, false otherwise.
     * 
     * @throws InvalidPersistenceXmlException if the given <code>project</code> has an
     *  invalid persistence.xml file.
     */
    public static boolean persistenceExists(Project project) throws InvalidPersistenceXmlException {
        return persistenceExists(project, null);
    }

    /**
     * Checks whether the given project has a persistence.xml associated with the given FileObject
     * that contains at least one persistence unit.
     * 
     * @param project the project; must not be null.
     * @param fo the FileObject
     * 
     * @return true if the given project has a persistence.xml associated with the given FileObject
     * containing at least one persitence unit, false otherwise.
     * 
     * @throws InvalidPersistenceXmlException if the given <code>project</code> has an
     *  invalid persistence.xml file.
     * @since 1.55
     */
    public static boolean persistenceExists(Project project, FileObject fo) throws InvalidPersistenceXmlException {
        Parameters.notNull("project", project); //NOI18N

        if (getDDFile(project, fo) == null) {
            return false;
        }
        PUDataObject pud = getPUDataObject(project, fo, null);
        try {
            return pud.getPersistence().getPersistenceUnit().length > 0;
        } catch (RuntimeException ex) {
            throw new InvalidPersistenceXmlException(ex.getMessage(), null);//persistence.xml may be corrupted and some parsing ways throw runtime
        }
    }

    /**
     * @return persistence.xml descriptor of first MetadataUnit found on project or null if none found
     */
    public static FileObject getDDFile(Project project) {
        return getDDFile(project, null);
    }

    /**
     * @return persistence.xml descriptor of first MetadataUnit associated with the FileObject or null if none found
     * @since 1.55
     */
    public static FileObject getDDFile(Project project, FileObject fo) {
        PersistenceScope[] persistenceScopes = PersistenceUtils.getPersistenceScopes(project, fo);
        for (int i = 0; i < persistenceScopes.length; i++) {
            return persistenceScopes[i].getPersistenceXml();
        }
        return null;
    }
    
    public static String getDatasourceName(PersistenceUnit pu) {
        String datasourceName = pu.getJtaDataSource();
        if (datasourceName == null) {
            datasourceName = pu.getNonJtaDataSource();
        }
        return datasourceName;
    }
    
    /**
     * @return array of providers known to the IDE.
     */
    public static Provider[] getAllProviders() {
        return new Provider[] {
                    DATANUCLEUS_PROVIDER3_2,
                    ECLIPSELINK_PROVIDER3_2,
                    HIBERNATE_PROVIDER3_2,
                    DATANUCLEUS_PROVIDER3_1,
                    ECLIPSELINK_PROVIDER3_1,
                    HIBERNATE_PROVIDER3_1,
                    DATANUCLEUS_PROVIDER3_0,
                    ECLIPSELINK_PROVIDER3_0,
                    HIBERNATE_PROVIDER3_0,
                    DATANUCLEUS_PROVIDER2_2, 
                    ECLIPSELINK_PROVIDER2_2, 
                    HIBERNATE_PROVIDER2_2, 
                    OPENJPA_PROVIDER2_2, 
                    DATANUCLEUS_PROVIDER2_1, 
                    ECLIPSELINK_PROVIDER2_1, 
                    HIBERNATE_PROVIDER2_1, 
                    OPENJPA_PROVIDER2_1, 
                    DATANUCLEUS_PROVIDER2_0, 
                    ECLIPSELINK_PROVIDER2_0, 
                    HIBERNATE_PROVIDER2_0, 
                    OPENJPA_PROVIDER2_0, 
                    DATANUCLEUS_PROVIDER1_0, 
                    ECLIPSELINK_PROVIDER1_0, 
                    HIBERNATE_PROVIDER1_0, 
                    KODO_PROVIDER, 
                    OPENJPA_PROVIDER1_0, 
                    TOPLINK_PROVIDER1_0, 
                    TOPLINK_PROVIDER_55_COMPATIBLE
        };
    }
    
    /**
     * Makes the given persistence unit portable if possible, i.e. removes the provider class from it.
     * A persistence unit may be made portable if it uses the default provider of the project's target
     * server, it doesn't specify any properties and it is not defined in Java SE environment.
     * 
     * @param project the project in which the given persistence unit is defined. Must not be null.
     * @param persistenceUnit the persistence unit to be made portable. Must not be null.
     * 
     * @return true if given persistence unit could be made portable, false otherwise.
     * 
     * @throws NullPointerException if either project or persistenceUnit was null.
     */
    public static boolean makePortableIfPossible(Project project, PersistenceUnit persistenceUnit) {
        return normalizeIfPossible(project, persistenceUnit, true);
    }

    /**
     * Makes the given persistence unit portable if possible, i.e. removes the provider class from it.
     * A persistence unit may be made portable if it uses the default provider of the project's target
     * server, it doesn't specify any properties and it is not defined in Java SE environment.
     * Restore provider class if necessary if there are properties and pu can't be fully portable
     *
     * @param project the project in which the given persistence unit is defined. Must not be null.
     * @param persistenceUnit the persistence unit to be made portable. Must not be null.
     *
     * @return true if given persistence unit could be made portable, false otherwise.
     *
     * @throws NullPointerException if either project or persistenceUnit was null.
     */
    public static boolean normalizeIfPossible(Project project, PersistenceUnit persistenceUnit) {
        return normalizeIfPossible(project, persistenceUnit, false);
    }

    private static boolean normalizeIfPossible(Project project, PersistenceUnit persistenceUnit, boolean donotrestore) {
        Parameters.notNull("project", project); //NOI18N
        Parameters.notNull("persistenceUnit", persistenceUnit); //NOI18N

        if (Util.isJavaSE(project)) {
            return false;
        }

        Provider defaultProvider = getContainerManagedProvider(project);

        if (defaultProvider == null) {
            return false;
        }

        boolean requiredTag = isProviderTagRequired(project);

        int notPortablePropSize = persistenceUnit.getProperties() == null ? 0 : persistenceUnit.getProperties().sizeProperty2();
        
        if(persistenceUnit.getProperties() != null) {
            for(Property prop:persistenceUnit.getProperties().getProperty2()) {
                if(prop.getName().startsWith("javax.persistence.") || prop.getName().startsWith("jakarta.persistence.")) {//not vendor specific
                    notPortablePropSize--;
                }
            }
        }
        
        if((notPortablePropSize == 0) && !requiredTag){
            if (defaultProvider.getProviderClass()!=null && defaultProvider.getProviderClass().equals(persistenceUnit.getProvider())) {

                persistenceUnit.setProvider(null);
                return true;
            }
        } else if (persistenceUnit.getProvider() == null && (notPortablePropSize > 0 || requiredTag) && !donotrestore){
            persistenceUnit.setProvider(defaultProvider.getProviderClass());
        }

        return false;
    }
    /**
     * Checks whether the given <code>project</code>'s target server is present.
     *
     * @param project the project whose target server's presence is checked; must not be null.
     * @return true if the given <code>project</code> has its target server present or
     *  if the project does not need a target server (i.e. it is not a J2EE project), false otherwise.
     * @throws NullPointerException if the given <code>project</code> was null.
     */
    public static boolean isValidServerInstanceOrNone(Project project) {
        Parameters.notNull("project", project);
        ServerStatusProvider serverStatusProvider = project.getLookup().lookup(ServerStatusProvider.class);
        if (serverStatusProvider == null) {
            // not a J2EE project
            return true;
        }
        return serverStatusProvider.validServerInstancePresent();
    }

    public static boolean canServerBeSelected(Project project) {
        Parameters.notNull("project", project);
        ServerStatusProvider2 serverStatusProvider = project.getLookup().lookup(ServerStatusProvider2.class);
        return serverStatusProvider != null;
    }

    /**
     * Help to migrate the Toplink properties to the corresponding Eclipselink ones and vice versa
     * 
     * @param prevProvider the provider class string 
     * @param curProvider the provider class string
     * @param persistenceUnit the persistence unit that is being modified on
     */
    public static void migrateProperties(String prevProvider, String curProvider, PersistenceUnit persistenceUnit) {
        if (prevProvider.equals("oracle.toplink.essentials.PersistenceProvider") && // NOI18N
                curProvider.equals("org.eclipse.persistence.jpa.PersistenceProvider")) { // NOI18N
            // Migrate TopLink properties to EclipseLink
            Property[] toplinkProps = persistenceUnit.getProperties().getProperty2();
            for (int i = 0; i < toplinkProps.length; i++) {
                if (toplinkProps[i].getName().contains("toplink")) { // NOI18N
                    String propName = toplinkProps[i].getName();
                    propName = propName.replace("toplink", "eclipselink"); // NOI18N

                    Property eclipselinkProp = persistenceUnit.getProperties().newProperty();
                    eclipselinkProp.setName(propName);
                    eclipselinkProp.setValue(toplinkProps[i].getValue());

                    persistenceUnit.getProperties().removeProperty2(toplinkProps[i]);
                    persistenceUnit.getProperties().addProperty2(eclipselinkProp);
                }
            }
        } else if (prevProvider.equals("org.eclipse.persistence.jpa.PersistenceProvider") && // NOI18N
                curProvider.equals("oracle.toplink.essentials.PersistenceProvider")) { // NOI18N
            // Change back to TopLink properties from EclipseLink
            Property[] eclipselinkProps = persistenceUnit.getProperties().getProperty2();
            for (int i = 0; i < eclipselinkProps.length; i++) {
                if (eclipselinkProps[i].getName().contains("eclipselink")) { // NOI18N
                    String propName = eclipselinkProps[i].getName();
                    propName = propName.replace("eclipselink", "toplink"); // NOI18N

                    Property toplinkProp = persistenceUnit.getProperties().newProperty();
                    toplinkProp.setName(propName);
                    toplinkProp.setValue(eclipselinkProps[i].getValue());

                    persistenceUnit.getProperties().removeProperty2(eclipselinkProps[i]);
                    persistenceUnit.getProperties().addProperty2(toplinkProp);
                }
            }
        }
    }

    /*
     * currently it's workaroud for spring support. spring support rrequires provider class in pu even for jee environment with default provider support
     * see issue #195973
     * TODO: consider if it should be implemented in some persistence provider (but it's likely will duplicate code for each project)
     * TODO: consider if it should be in J2eeProjectCapabilities
     */
    private static boolean isProviderTagRequired(Project project){
        // check if swdp is already part of classpath
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sgs.length < 1) {
            return false;
        }
        FileObject sourceRoot = sgs[0].getRootFolder();
        ClassPath classPath = ClassPath.getClassPath(sourceRoot, ClassPath.COMPILE);
        //this package name will change when open source, should just rely on subclass to use file names
        FileObject utxClass = classPath.findResource("org/springframework/transaction/annotation/Transactional.class"); // NOI18N
        return utxClass != null;
    }
}
