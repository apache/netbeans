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
package org.netbeans.modules.j2ee.persistence.unit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import static org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit.JAKARTA_NAMESPACE;
import static org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit.JAVAX_NAMESPACE;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;

/**
 *
 * @author sp153251
 */
public class PersistenceCfgProperties {

    // String[] for selecting one of the values
    private static final String[] TRUE_FALSE = new String[]{"true", "false"}; // NOI18N
    private static final String[] RESOURCE_TYPE = new String[]{"RESOURCE_LOCAL", "JTA"}; // NOI18N
    private static final String[] SCHEMA_GEN_DB_OPTIONS = new String[]{"none", "create", "drop-and-create", "drop", "create-or-extend-tables"};
    private static final String[] SCHEMA_GEN_SCRIPTS_OPTIONS = new String[]{"none", "create", "drop-and-create", "drop"};
    private static final String[] SCHEMA_GEN_SOURCE_TYPES = new String[]{"metadata", "script", "metadata-then-script", "script-then-metadata"};
    private static final String[] SHARED_CACHE_MODE = new String[]{"ALL", "NONE", "ENABLE_SELECTIVE", "DISABLE_SELECTIVE", "UNSPECIFIED"};
    //eclipselink
    private static final String[] EL_CACHE_TYPES = new String[]{"DEFAULT", "Full", "Weak", "Soft", "SoftWeak", "HardWeak", "NONE"};//NOI18N
    private static final String[] EL_FLUSH_CLEAR_CACHE = new String[]{"DEFAULT", "Drop", "DropInvalidate", "Merge"};//NOI18N
    private static final String[] EL_WEAVING = new String[] {"true", "false", "static"};//NOI18N
    private static final String[] EL_PROFILER = new String[]{"NoProfiler", "PerformanceMonitor", "PerformanceProfiler", "QueryMonitor", "DMSProfiler"};//NOI18N
    private static final String[] EL_CONTEXT_REFMODE = new String[]{"HARD", "WEAK", "FORCE_WEAK"};//NOI18N
    private static final String[] EL_BATCHWRITER = new String[]{"JDBC", "Buffered", "Oracle-JDBC", "None"};//NOI18N
    private static final String[] EL_EXCLUSIVE_CON_MODE = new String[]{"Transactional", "Isolated", "Always"};//NOI18N
    private static final String[] EL_LOGGER = new String[]{"DEFAULT", "DefaultLogger", "JavaLogger", "ServerLogger"};//NOI18N
    private static final String[] EL_LOGGER_LEVEL = new String[]{"OFF", "SEVERE", "WARNING", "INFO", "CONFIG", "FINE", "FINER", "FINEST", "ALL"};//NOI18N
    private static final String[] EL_TARGET_DATABASE = new String[]{"Attunity", "Auto", "Cloudscape", "Database", "DB2", "DB2Mainframe", "DBase", "DEFAULT", "Derby", "HANA", "HSQL", "Informix", "Informix11", "JavaDB", "MaxDB", "MySQL", "MySQL4", "Oracle", "Oracle8", "Oracle9", "Oracle10", "Oracle11", "PointBase", "PostgreSQL", "SQLAnywhere", "SQLServer", "Sybase", "Symfoware", "TimesTen"};//NOI18N
    private static final String[] EL_TARGET_SERVER = new String[]{"DEFAULT", "Glassfish", "JBoss", "None", "OC4J", "SAPNetWeaver_7_1", "SunAS9", "WebLogic", "WebLogic_9", "WebLogic_10", "WebLogic_12", "WebSphere", "WebSphere_6_1", "WebSphere_7", "WebSphere_EJBEmbeddable", "WebSphere_Liberty"};//NOI18N
    private static final String[] EL_DDL_GEN_MODE = new String[]{"both", "database", "sql-script"};//NOI18N
    //hibernate 5.6 (JPA 2.2) dialects
    private static final String[] HIBERNATE_DIALECTS = new String[]{"org.hibernate.dialect.AbstractHANADialect", "org.hibernate.dialect.Cache71Dialect", "org.hibernate.dialect.CockroachDB192Dialect", "org.hibernate.dialect.CockroachDB201Dialect", "org.hibernate.dialect.CUBRIDDialect", "org.hibernate.dialect.DataDirectOracle9Dialect", "org.hibernate.dialect.DB2390Dialect", 
            "org.hibernate.dialect.DB2390V8Dialect", "org.hibernate.dialect.DB2400Dialect", "org.hibernate.dialect.DB2400V7R3Dialect", "org.hibernate.dialect.DB297Dialect", "org.hibernate.dialect.DB2Dialect", "org.hibernate.dialect.DerbyDialect", "org.hibernate.dialect.DerbyTenFiveDialect", "org.hibernate.dialect.DerbyTenSevenDialect", "org.hibernate.dialect.DerbyTenSixDialect", 
            "org.hibernate.dialect.FirebirdDialect", "org.hibernate.dialect.FrontBaseDialect", "org.hibernate.dialect.H2Dialect", "org.hibernate.dialect.HANACloudColumnStoreDialect", "org.hibernate.dialect.HANAColumnStoreDialect", "org.hibernate.dialect.HANARowStoreDialect", "org.hibernate.dialect.HSQLDialect", "org.hibernate.dialect.Informix10Dialect", "org.hibernate.dialect.InformixDialect", 
            "org.hibernate.dialect.Ingres10Dialect", "org.hibernate.dialect.Ingres9Dialect", "org.hibernate.dialect.IngresDialect", "org.hibernate.dialect.InnoDBStorageEngine", "org.hibernate.dialect.InterbaseDialect", "org.hibernate.dialect.JDataStoreDialect", "org.hibernate.dialect.MariaDB102Dialect", "org.hibernate.dialect.MariaDB103Dialect", "org.hibernate.dialect.MariaDB106Dialect", 
            "org.hibernate.dialect.MariaDB10Dialect", "org.hibernate.dialect.MariaDB53Dialect", "org.hibernate.dialect.MariaDBDialect", "org.hibernate.dialect.MckoiDialect", "org.hibernate.dialect.MimerSQLDialect", "org.hibernate.dialect.MyISAMStorageEngine", "org.hibernate.dialect.MySQL55Dialect", "org.hibernate.dialect.MySQL57Dialect", "org.hibernate.dialect.MySQL57InnoDBDialect", 
            "org.hibernate.dialect.MySQL5Dialect", "org.hibernate.dialect.MySQL5InnoDBDialect", "org.hibernate.dialect.MySQL8Dialect", "org.hibernate.dialect.MySQLDialect", "org.hibernate.dialect.MySQLInnoDBDialect", "org.hibernate.dialect.MySQLMyISAMDialect", "org.hibernate.dialect.Oracle10gDialect", "org.hibernate.dialect.Oracle12cDialect", "org.hibernate.dialect.Oracle8iDialect", 
            "org.hibernate.dialect.Oracle9Dialect", "org.hibernate.dialect.Oracle9iDialect", "org.hibernate.dialect.OracleDialect", "org.hibernate.dialect.PointbaseDialect", "org.hibernate.dialect.PostgresPlusDialect", "org.hibernate.dialect.PostgreSQL10Dialect", "org.hibernate.dialect.PostgreSQL81Dialect", "org.hibernate.dialect.PostgreSQL82Dialect", "org.hibernate.dialect.PostgreSQL91Dialect", 
            "org.hibernate.dialect.PostgreSQL92Dialect", "org.hibernate.dialect.PostgreSQL93Dialect", "org.hibernate.dialect.PostgreSQL94Dialect", "org.hibernate.dialect.PostgreSQL95Dialect", "org.hibernate.dialect.PostgreSQL9Dialect", "org.hibernate.dialect.ProgressDialect", "org.hibernate.dialect.RDMSOS2200Dialect", "org.hibernate.dialect.SAPDBDialect", 
            "org.hibernate.dialect.SQLServer2005Dialect", "org.hibernate.dialect.SQLServer2008Dialect", "org.hibernate.dialect.SQLServer2012Dialect", "org.hibernate.dialect.SQLServer2016Dialect", "org.hibernate.dialect.SQLServerDialect", "org.hibernate.dialect.Sybase11Dialect", "org.hibernate.dialect.SybaseAnywhereDialect", "org.hibernate.dialect.SybaseASE157Dialect", 
            "org.hibernate.dialect.SybaseASE15Dialect", "org.hibernate.dialect.SybaseDialect", "org.hibernate.dialect.Teradata14Dialect", "org.hibernate.dialect.TeradataDialect", "org.hibernate.dialect.TimesTenDialect"};//NOI18N
    //DataNucleus
    private static final String[] DN_CONN_POOLING = new String[]{"None", "DBCP2", "C3P0", "BoneCP", "HikariCP", "Tomcat"};//NOI18N
    private static final String[] DN_VALIDATION_MODE = new String[]{"auto", "callback", "none"};//NOI18N
    private static final String[] DN_TRX_ATTR = new String[]{"NEW", "EXISTING"};//NOI18N
    private static final String[] DN_TRX_ISO = new String[]{"read-uncommitted", "read-committed", "repeatable-read", "serializable"};//NOI18N
    
    private static final Map<Provider, Map<String, String[]>> possiblePropertyValues = new HashMap<>();
    
    static {
        //general 2.0
        possiblePropertyValues.put(null, new HashMap<String, String[]>());//it's for default
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.PESSIMISTIC_LOCK_TIMEOUT, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.QUERY_TIMEOUT, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.VALIDATION_GROUP_PRE_PERSIST, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.VALIDATION_GROUP_PRE_UPDATE, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.VALIDATION_GROUP_PRE_REMOVE, null);
//in current realization jdbc properties are derived from provider properties, commented
//        possiblePropertyValues.get(null).put(PersistenceUnitProperties.JDBC_DRIVER, null);
//        possiblePropertyValues.get(null).put(PersistenceUnitProperties.JDBC_URL, null);
//        possiblePropertyValues.get(null).put(PersistenceUnitProperties.JDBC_USER, null);
//        possiblePropertyValues.get(null).put(PersistenceUnitProperties.JDBC_PASSWORD, null);
        //2.1 but in the same area as 2.0 for now
//        possiblePropertyValues.get(null).put(PersistenceUnitProperties.SCHEMA_GENERATION_DATABASE_ACTION, SCHEMA_GEN_DB_OPTIONS);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_ACTION, SCHEMA_GEN_SCRIPTS_OPTIONS);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.SCHEMA_GENERATION_CREATE_SOURCE, SCHEMA_GEN_SOURCE_TYPES);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.SCHEMA_GENERATION_DROP_SOURCE, SCHEMA_GEN_SOURCE_TYPES);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.SCHEMA_GENERATION_CREATE_DATABASE_SCHEMAS, TRUE_FALSE);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_CREATE_TARGET, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_DROP_TARGET, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.SCHEMA_DATABASE_PRODUCT_NAME, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.SCHEMA_DATABASE_MAJOR_VERSION, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.SCHEMA_DATABASE_MINOR_VERSION, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.SCHEMA_GENERATION_CREATE_SCRIPT_SOURCE, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.SCHEMA_GENERATION_DROP_SCRIPT_SOURCE, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.SCHEMA_GENERATION_CONNECTION, null);
        possiblePropertyValues.get(null).put(PersistenceUnitProperties.SCHEMA_GENERATION_SQL_LOAD_SCRIPT_SOURCE, null);
        
        //EclipseLink JPA 1.0
        possiblePropertyValues.put(ProviderUtil.ECLIPSELINK_PROVIDER1_0, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER1_0).put(ProviderUtil.ECLIPSELINK_PROVIDER1_0.getJdbcUrl(), null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER1_0).put(ProviderUtil.ECLIPSELINK_PROVIDER1_0.getJdbcDriver(), null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER1_0).put(ProviderUtil.ECLIPSELINK_PROVIDER1_0.getJdbcPassword(), null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER1_0).put(ProviderUtil.ECLIPSELINK_PROVIDER1_0.getJdbcUsername(), null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER1_0).put(ProviderUtil.ECLIPSELINK_PROVIDER1_0.getTableGenerationPropertyName()
                ,new String[] {ProviderUtil.ECLIPSELINK_PROVIDER1_0.getTableGenerationCreateValue(),ProviderUtil.ECLIPSELINK_PROVIDER1_0.getTableGenerationDropCreateValue(), PersistenceUnitProperties.NONE });
        //EclipseLink JPA 2.0
        possiblePropertyValues.put(ProviderUtil.ECLIPSELINK_PROVIDER2_0, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.TEMPORAL_MUTABLE, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.CACHE_TYPE_DEFAULT, EL_CACHE_TYPES);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.CACHE_SIZE_DEFAULT, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.CACHE_SHARED_DEFAULT, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.FLUSH_CLEAR_CACHE, EL_FLUSH_CLEAR_CACHE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.THROW_EXCEPTIONS, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.EXCEPTION_HANDLER_CLASS, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.WEAVING, EL_WEAVING);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.WEAVING_LAZY, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.WEAVING_CHANGE_TRACKING, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.WEAVING_FETCHGROUPS, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.WEAVING_INTERNAL, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.WEAVING_EAGER, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.SESSION_CUSTOMIZER, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.VALIDATION_ONLY_PROPERTY, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.CLASSLOADER, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.PROFILER, EL_PROFILER);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.PERSISTENCE_CONTEXT_REFERENCE_MODE, EL_CONTEXT_REFMODE);//NOI18N
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.JDBC_BIND_PARAMETERS, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.NATIVE_SQL, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.BATCH_WRITING, EL_BATCHWRITER);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.BATCH_WRITING_SIZE, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.CACHE_STATEMENTS, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.CACHE_STATEMENTS_SIZE, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.EXCLUSIVE_CONNECTION_IS_LAZY, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.EXCLUSIVE_CONNECTION_MODE, EL_EXCLUSIVE_CON_MODE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.LOGGING_LOGGER, EL_LOGGER);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.LOGGING_LEVEL, EL_LOGGER_LEVEL);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.LOGGING_TIMESTAMP, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.LOGGING_THREAD, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.LOGGING_SESSION, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.LOGGING_EXCEPTIONS, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.LOGGING_FILE, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.PARTITIONING, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.PARTITIONING_CALLBACK, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.SESSION_NAME, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.SESSIONS_XML, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.SESSION_EVENT_LISTENER_CLASS, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.INCLUDE_DESCRIPTOR_QUERIES, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.TARGET_DATABASE, EL_TARGET_DATABASE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.TARGET_SERVER, EL_TARGET_SERVER);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.APP_LOCATION, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.CREATE_JDBC_DDL_FILE, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.DROP_JDBC_DDL_FILE, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.DDL_GENERATION_MODE, EL_DDL_GEN_MODE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.WEAVING_CHANGE_TRACKING, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.UPPERCASE_COLUMN_NAMES, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.CANONICAL_MODEL_PREFIX, null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.CANONICAL_MODEL_SUFFIX, null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(PersistenceUnitProperties.CANONICAL_MODEL_SUB_PACKAGE, null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(ProviderUtil.ECLIPSELINK_PROVIDER2_0.getJdbcUrl(), null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(ProviderUtil.ECLIPSELINK_PROVIDER2_0.getJdbcDriver(), null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(ProviderUtil.ECLIPSELINK_PROVIDER2_0.getJdbcPassword(), null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(ProviderUtil.ECLIPSELINK_PROVIDER2_0.getJdbcUsername(), null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).put(ProviderUtil.ECLIPSELINK_PROVIDER2_0.getTableGenerationPropertyName()
                ,new String[] {ProviderUtil.ECLIPSELINK_PROVIDER2_0.getTableGenerationCreateValue(),ProviderUtil.ECLIPSELINK_PROVIDER2_0.getTableGenerationDropCreateValue(), PersistenceUnitProperties.CREATE_OR_EXTEND, PersistenceUnitProperties.SCHEMA_GENERATION_DROP_ACTION, PersistenceUnitProperties.SCHEMA_GENERATION_NONE_ACTION });
        //EclipseLink JPA 2.1 (initially just copy of 2.0)
        possiblePropertyValues.put(ProviderUtil.ECLIPSELINK_PROVIDER2_1, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_1).putAll(possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0));
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_1).put(PersistenceUnitProperties.CONNECTION_POOL_SEQUENCE + PersistenceUnitProperties.CONNECTION_POOL_MAX, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_1).put(PersistenceUnitProperties.CONNECTION_POOL_SEQUENCE + PersistenceUnitProperties.CONNECTION_POOL_MIN, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_1).put(PersistenceUnitProperties.CONNECTION_POOL_SEQUENCE + PersistenceUnitProperties.CONNECTION_POOL_INITIAL, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_1).put(PersistenceUnitProperties.CONNECTION_POOL_SEQUENCE + PersistenceUnitProperties.CONNECTION_POOL_NON_JTA_DATA_SOURCE, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_1).put(PersistenceUnitProperties.CONNECTION_POOL_SEQUENCE + PersistenceUnitProperties.CONNECTION_POOL_JTA_DATA_SOURCE, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_1).put(PersistenceUnitProperties.CONNECTION_POOL_SEQUENCE + PersistenceUnitProperties.CONNECTION_POOL_WAIT, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_1).put(PersistenceUnitProperties.CONNECTION_POOL_SEQUENCE + PersistenceUnitProperties.CONNECTION_POOL_URL, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_1).put(PersistenceUnitProperties.CONNECTION_POOL_SEQUENCE + PersistenceUnitProperties.CONNECTION_POOL_USER, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_1).put(PersistenceUnitProperties.CONNECTION_POOL_SEQUENCE + PersistenceUnitProperties.CONNECTION_POOL_PASSWORD, null);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_1).put(PersistenceUnitProperties.CONNECTION_POOL_READ + PersistenceUnitProperties.CONNECTION_POOL_SHARED, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_1).put(ProviderUtil.ECLIPSELINK_PROVIDER2_1.getTableGenerationPropertyName(),possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).get(ProviderUtil.ECLIPSELINK_PROVIDER2_0.getTableGenerationPropertyName()));
        //EclipseLink JPA 2.2 (initially just copy of 2.0)
        possiblePropertyValues.put(ProviderUtil.ECLIPSELINK_PROVIDER2_2, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_2).putAll(possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0));
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_2).put(PersistenceUnitProperties.SHARED_CACHE_MODE, SHARED_CACHE_MODE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_2).put(PersistenceUnitProperties.WEAVING_MAPPEDSUPERCLASS, TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_2).put(ProviderUtil.ECLIPSELINK_PROVIDER2_2.getTableGenerationPropertyName(),possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_0).get(ProviderUtil.ECLIPSELINK_PROVIDER2_0.getTableGenerationPropertyName()));
        //EclipseLink JPA 3.0 (initially just copy of 2.2 and then replace javax to jakarta)
        possiblePropertyValues.put(ProviderUtil.ECLIPSELINK_PROVIDER3_0, new HashMap<>());
        for (Map.Entry<String, String[]> entry : possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER2_2).entrySet()) {
            possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER3_0).put(entry.getKey().replace(JAVAX_NAMESPACE, JAKARTA_NAMESPACE), entry.getValue());
        }
        //EclipseLink JPA 3.1 (initially just copy of 3.0)
        possiblePropertyValues.put(ProviderUtil.ECLIPSELINK_PROVIDER3_1, new HashMap<>());
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER3_1).putAll(possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER3_0));
        
        //EclipseLink JPA 3.2 (initially just copy of 3.0)
        possiblePropertyValues.put(ProviderUtil.ECLIPSELINK_PROVIDER3_2, new HashMap<>());
        possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER3_2).putAll(possiblePropertyValues.get(ProviderUtil.ECLIPSELINK_PROVIDER3_0));

        //Hibernate JPA 1.0
        possiblePropertyValues.put(ProviderUtil.HIBERNATE_PROVIDER1_0, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER1_0).put(ProviderUtil.HIBERNATE_PROVIDER1_0.getJdbcUrl(), null);
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER1_0).put(ProviderUtil.HIBERNATE_PROVIDER1_0.getJdbcDriver(), null);
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER1_0).put(ProviderUtil.HIBERNATE_PROVIDER1_0.getJdbcPassword(), null);
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER1_0).put(ProviderUtil.HIBERNATE_PROVIDER1_0.getJdbcUsername(), null);
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER1_0).put(ProviderUtil.HIBERNATE_PROVIDER1_0.getTableGenerationPropertyName()
                ,new String[] {ProviderUtil.HIBERNATE_PROVIDER1_0.getTableGenerationCreateValue(),ProviderUtil.HIBERNATE_PROVIDER1_0.getTableGenerationDropCreateValue(), "validate", "update" });
        //Hibernate JPA 2.0 //TODO? reuse hibernate module?
        possiblePropertyValues.put(ProviderUtil.HIBERNATE_PROVIDER2_0, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put(ProviderUtil.HIBERNATE_PROVIDER2_0.getTableGenerationPropertyName(), null);
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.dialect",  HIBERNATE_DIALECTS);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.show_sql",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.format_sql",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.transaction.manager_lookup_class",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.max_fetch_depth",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.ejb.cfgfile",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.archive.autodetection",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.ejb.interceptor",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.ejb.interceptor.session_scoped",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.ejb.naming_strategy",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.ejb.use_class_enhancer",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.ejb.discard_pc_on_close",  null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put("hibernate.ejb.resource_scanner",     null);//NOI18N
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put(ProviderUtil.HIBERNATE_PROVIDER2_0.getJdbcUrl(), null);
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put(ProviderUtil.HIBERNATE_PROVIDER2_0.getJdbcDriver(), null);
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put(ProviderUtil.HIBERNATE_PROVIDER2_0.getJdbcPassword(), null);
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put(ProviderUtil.HIBERNATE_PROVIDER2_0.getJdbcUsername(), null);
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0).put(ProviderUtil.HIBERNATE_PROVIDER2_0.getTableGenerationPropertyName()
                ,new String[] {ProviderUtil.HIBERNATE_PROVIDER2_0.getTableGenerationCreateValue(),ProviderUtil.HIBERNATE_PROVIDER2_0.getTableGenerationDropCreateValue(), "validate", "update" });//NOI18N
        //Hibernate JPA 2.1 (initially just copy of 2.0)
        possiblePropertyValues.put(ProviderUtil.HIBERNATE_PROVIDER2_1, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_1).putAll(possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0));
        //Hibernate JPA 2.2 (initially just copy of 2.0)
        possiblePropertyValues.put(ProviderUtil.HIBERNATE_PROVIDER2_2, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_2).putAll(possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_0));
        //Hibernate JPA 3.0 (initially just copy of 2.2 and then replace javax to jakarta)
        possiblePropertyValues.put(ProviderUtil.HIBERNATE_PROVIDER3_0, new HashMap<String, String[]>());
        for (Map.Entry<String, String[]> entry : possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER2_2).entrySet()) {
            possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER3_0).put(entry.getKey().replace(JAVAX_NAMESPACE, JAKARTA_NAMESPACE), entry.getValue());
        }
        //Hibernate JPA 3.1 (initially just copy of 3.0)
        possiblePropertyValues.put(ProviderUtil.HIBERNATE_PROVIDER3_1, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER3_1).putAll(possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER3_0));
        
        //Hibernate JPA 3.2 (initially just copy of 3.0)
        possiblePropertyValues.put(ProviderUtil.HIBERNATE_PROVIDER3_2, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER3_2).putAll(possiblePropertyValues.get(ProviderUtil.HIBERNATE_PROVIDER3_0));
        
        //OpenJPA JPA 1.0
        possiblePropertyValues.put(ProviderUtil.OPENJPA_PROVIDER1_0, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER1_0).put(ProviderUtil.OPENJPA_PROVIDER1_0.getJdbcUrl(), null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER1_0).put(ProviderUtil.OPENJPA_PROVIDER1_0.getJdbcDriver(), null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER1_0).put(ProviderUtil.OPENJPA_PROVIDER1_0.getJdbcPassword(), null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER1_0).put(ProviderUtil.OPENJPA_PROVIDER1_0.getJdbcUsername(), null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER1_0).put(ProviderUtil.OPENJPA_PROVIDER1_0.getTableGenerationPropertyName()
                ,new String[] {ProviderUtil.OPENJPA_PROVIDER1_0.getTableGenerationCreateValue(),ProviderUtil.OPENJPA_PROVIDER1_0.getTableGenerationDropCreateValue() });
        //OpenJPA JPA 2.0 (initially just copy of 1.0)
        possiblePropertyValues.put(ProviderUtil.OPENJPA_PROVIDER2_0, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_0).putAll(possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER1_0));
        //OpenJPA JPA 2.1
        possiblePropertyValues.put(ProviderUtil.OPENJPA_PROVIDER2_1, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put(ProviderUtil.OPENJPA_PROVIDER2_1.getJdbcUrl(), null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put(ProviderUtil.OPENJPA_PROVIDER2_1.getJdbcDriver(), null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put(ProviderUtil.OPENJPA_PROVIDER2_1.getJdbcPassword(), null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put(ProviderUtil.OPENJPA_PROVIDER2_1.getJdbcUsername(), null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.AutoClear", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.AutoDetach", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.BrokerFactory", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.BrokerImpl", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.Callbacks", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.ClassResolver", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.Compatibility", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.ConnectionDriverName", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.Connection2DriverName", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.ConnectionFactory", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.ConnectionFactory2", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.ConnectionFactoryName", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.ConnectionFactory2Name", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.ConnectionFactoryMode", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.ConnectionFactoryProperties", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.ConnectionFactory2Properties", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.ConnectionPassword", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.Connection2Password", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.ConnectionProperties", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.Connection2Properties", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.ConnectionURL", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.Connection2URL", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.ConnectionUserName", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.Connection2UserName", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.ConnectionRetainMode", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.DataCache", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.DataCacheManager", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.DataCacheMode", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.DataCacheTimeout", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.DetachState", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.DynamicDataStructs", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.DynamicEnhancementAgent", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.FetchBatchSize", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.EncryptionProvider", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.FetchGroups", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.FlushBeforeQueries", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.IgnoreChanges", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.Id", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.InitializeEagerly", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.Instrumentation", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.InverseManager", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.LockManager", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.LockTimeout", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.Log", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.ManagedRuntime", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.Mapping", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.MaxFetchDepth", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.MetaDataFactory", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.MetaDataRepository", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.Multithreaded", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.Optimistic", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.OptimizeIdCopy", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.OrphanedKeyAction", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.NontransactionalRead", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.NontransactionalWrite", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.ProxyManager", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.PostLoadOnMerge", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.QueryCache", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.QueryCompilationCache", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.ReadLockLevel", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.RemoteCommitProvider", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.RestoreState", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.RetainState", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.RetryClassRegistration", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.RuntimeUnenhancedClasses", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.SavepointManager", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.Sequence", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.Specification", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.TransactionMode", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.UseTCCLinSelectNew", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.WriteLockLevel", null);
        //JDBC Properties
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.jdbc.ConnectionDecorators", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.jdbc.DBDictionary", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.jdbc.DriverDataSource", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.jdbc.EagerFetchMode", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.jdbc.FetchDirection", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.jdbc.JDBCListeners", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.jdbc.LRSSize", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.jdbc.MappingDefaults", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.jdbc.MappingFactory", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.jdbc.QuerySQLCache", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.jdbc.ResultSetType", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.jdbc.Schema", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.jdbc.SchemaFactory", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.jdbc.Schemas", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.jdbc.SQLFactory", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.jdbc.SubclassFetchMode", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.jdbc.SynchronizeMappings", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.jdbc.TransactionIsolation", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.jdbc.UpdateManager", null);
        //Global Properties
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.slice.DistributionPolicy", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.slice.Lenient", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.slice.Master", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.slice.Names", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.slice.ThreadingPolicy", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put("openjpa.slice.TransactionPolicy", null);
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1).put(ProviderUtil.OPENJPA_PROVIDER2_1.getTableGenerationPropertyName()
                ,new String[] {ProviderUtil.OPENJPA_PROVIDER2_1.getTableGenerationCreateValue(),ProviderUtil.OPENJPA_PROVIDER2_1.getTableGenerationDropCreateValue() });
        //OpenJPA JPA 2.2 (initially just copy of 2.1)
        possiblePropertyValues.put(ProviderUtil.OPENJPA_PROVIDER2_2, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_2).putAll(possiblePropertyValues.get(ProviderUtil.OPENJPA_PROVIDER2_1));
        
        //DataNucleus JPA 1.0
        possiblePropertyValues.put(ProviderUtil.DATANUCLEUS_PROVIDER1_0, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER1_0).put(ProviderUtil.DATANUCLEUS_PROVIDER1_0.getJdbcUrl(), null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER1_0).put(ProviderUtil.DATANUCLEUS_PROVIDER1_0.getJdbcDriver(), null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER1_0).put(ProviderUtil.DATANUCLEUS_PROVIDER1_0.getJdbcPassword(), null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER1_0).put(ProviderUtil.DATANUCLEUS_PROVIDER1_0.getJdbcUsername(), null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER1_0).put(ProviderUtil.DATANUCLEUS_PROVIDER1_0.getTableGenerationPropertyName()
                ,new String[] {ProviderUtil.DATANUCLEUS_PROVIDER1_0.getTableGenerationCreateValue(),ProviderUtil.DATANUCLEUS_PROVIDER1_0.getTableGenerationDropCreateValue() });
        //DataNucleus JPA 2.0 (initially just copy of 1.0)
        possiblePropertyValues.put(ProviderUtil.DATANUCLEUS_PROVIDER2_0, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_0).putAll(possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER1_0));
        //DataNucleus Datastore Properties
        possiblePropertyValues.put(ProviderUtil.DATANUCLEUS_PROVIDER2_1, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put(ProviderUtil.DATANUCLEUS_PROVIDER2_1.getJdbcUrl(), null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put(ProviderUtil.DATANUCLEUS_PROVIDER2_1.getJdbcDriver(), null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put(ProviderUtil.DATANUCLEUS_PROVIDER2_1.getJdbcPassword(), null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put(ProviderUtil.DATANUCLEUS_PROVIDER2_1.getJdbcUsername(), null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.ConnectionURL", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.ConnectionUserName", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.ConnectionPassword", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.ConnectionDriverName", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.ConnectionFactory", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.ConnectionFactory2", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.ConnectionFactoryName", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.ConnectionFactory2Name", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.ConnectionPasswordDecrypter", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.connectionPoolingType", DN_CONN_POOLING);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.connectionPoolingType.nontx", DN_CONN_POOLING);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.connection.nontx.releaseAfterUse", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.connection.singleConnectionPerExecutionContext", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.connection.resourceType", RESOURCE_TYPE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.connection.resourceType2", RESOURCE_TYPE);
        //DataNucleus JPA 2.1 Persistence Properties
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.IgnoreCache", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.Multithreaded", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.Optimistic", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.RetainValues", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.RestoreValues", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.mapping.Catalog", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.mapping.Schema", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.tenantId", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.tenantProvider", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.CurrentUser", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.CurrentUserProvider", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.DetachAllOnCommit", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.detachAllOnRollback", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.CopyOnAttach", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.allowAttachOfTransient", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.attachSameDatastore", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.detachAsWrapped", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.DetachOnClose", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.detachmentFields", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.maxFetchDepth", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.detachedState", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.ServerTimeZoneID", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.PersistenceUnitLoadClasses", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.persistenceXmlFilename", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.datastoreReadTimeout", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.datastoreWriteTimeout", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.singletonEMFForName", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.jmxType", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.type.wrapper.basis", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.type.treatJavaUtilDateAsMutable", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.deletionPolicy", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.identityStringTranslatorType", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.identityKeyTranslatorType", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.datastoreIdentityType", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.executionContext.maxIdle", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.executionContext.reaperThread", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.executionContext.closeActiveTxAction", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.stateManager.className", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.manageRelationships", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.manageRelationshipsChecks", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.persistenceByReachabilityAtCommit", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.classLoaderResolverName", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.primaryClassLoader", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.plugin.pluginRegistryClassName", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.plugin.pluginRegistryBundleCheck", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.plugin.allowUserBundles", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.plugin.validatePlugins", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.findObject.validateWhenCached", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.findObject.typeConversion", TRUE_FALSE);
        //DataNucleus Schema Properties
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.schema.autoCreateAll", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.schema.autoCreateDatabase", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.schema.autoCreateTables", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.schema.autoCreateColumns", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.schema.autoCreateConstraints", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.schema.autoCreateWarnOnError", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.schema.validateAll", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.schema.validateTables", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.schema.validateColumns", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.schema.validateConstraints", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.readOnlyDatastore", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.readOnlyDatastoreAction", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.schema.generateDatabase.mode", SCHEMA_GEN_SCRIPTS_OPTIONS);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.schema.generateScripts.mode", SCHEMA_GEN_SCRIPTS_OPTIONS);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.schema.generateScripts.create", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.schema.generateScripts.drop", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.schema.generateDatabase.createScript", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.schema.generateDatabase.dropScript", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.schema.loadScript", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.identifierFactory", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.identifier.namingFactory", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.identifier.case", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.identifier.wordSeparator", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.identifier.tablePrefix", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.identifier.tableSuffix", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.store.allowReferencesWithNoImplementations", TRUE_FALSE);
        //DataNucleus Transaction Properties
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.transaction.type", RESOURCE_TYPE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.transaction.isolation", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.transaction.jta.transactionManagerLocator", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.transaction.jta.transactionManagerJNDI", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.transaction.nontx.read", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.transaction.nontx.write", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.transaction.nontx.atomic", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.SerializeRead", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.flush.auto.objectLimit", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.flush.mode", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.flush.optimised", TRUE_FALSE);
        // DataNucleus Cache Properties
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.collections", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.collections.lazy", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.level1.type", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.level2.type", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.level2.mode", SHARED_CACHE_MODE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.level2.storeMode", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.level2.retrieveMode", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.level2.updateMode", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.level2.cacheName", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.level2.maxSize", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.level2.clearAtClose", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.level2.batchSize", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.level2.expiryMillis", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.level2.readThrough", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.level2.writeThrough", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.level2.storeByValue", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.level2.statisticsEnabled", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.queryCompilation.type", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.queryCompilation.cacheName", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.queryCompilationDatastore.type", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.queryCompilationDatastore.cacheName", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.queryResults.type", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.queryResults.cacheName", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.queryResults.clearAtClose", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.queryResults.maxSize", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cache.queryResults.expiryMillis", null);
        //DataNucleus Bean Validation Properties
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.validation.mode", DN_VALIDATION_MODE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.validation.group.pre-persist", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.validation.group.pre-update", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.validation.group.pre-remove", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.validation.factory", null);
        //DataNucleus Value Generation Properties
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.valuegeneration.transactionAttribute", DN_TRX_ATTR);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.valuegeneration.transactionIsolation", DN_TRX_ISO);
        //DataNucleus Metadata Properties
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.metadata.alwaysDetachable", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.metadata.listener.object", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.metadata.ignoreMetaDataForMissingClasses", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.metadata.xml.validate", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.metadata.xml.namespaceAware", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.metadata.allowXML", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.metadata.allowAnnotations", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.metadata.allowLoadAtRuntime", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.metadata.defaultNullable", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.metadata.scanner", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.metadata.useDiscriminatorForSingleTable", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.metadata.javaxValidationShortcuts", TRUE_FALSE);
        //DataNucleus Query Properties
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.query.flushBeforeExecution", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.query.jpql.allowRange", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.query.checkUnusedParameters", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.query.sql.syntaxChecks", TRUE_FALSE);
        //DataNucleus Datastore-Specific Properties
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.datastoreAdapterClassName", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.useLegacyNativeValueStrategy", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.statementBatchLimit", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.checkExistTablesOrViews", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.useDefaultSqlType", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.initializeColumnInfo", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.classAdditionMaxRetries", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.constraintCreateMode", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.uniqueConstraints.mapInverse", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.discriminatorPerSubclassTable", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.stringDefaultLength", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.stringLengthExceededAction", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.useColumnDefaultWhenNull", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.persistEmptyStringAsNull", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.query.fetchDirection", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.query.resultSetType", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.query.resultSetConcurrency", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.query.multivaluedFetch", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.oracle.nlsSortOrder", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.mysql.engineType", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.mysql.collation", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.mysql.characterSet", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.informix.useSerialForIdentity", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.dynamicSchemaUpdates", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.omitDatabaseMetaDataGetColumns", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.sqlTableNamingStrategy", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.tableColumnOrder", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.allowColumnReuse", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.statementLogging", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.autoFetchUnloadedBasicFields", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.rdbms.autoFetchUnloadedFKs", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cloud.storage.bucket", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.hbase.relationUsesPersistableId", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.hbase.enforceUniquenessInApplication", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cassandra.enforceUniquenessInApplication", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cassandra.compression", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cassandra.metrics", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cassandra.ssl", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cassandra.socket.readTimeoutMillis", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cassandra.socket.connectTimeoutMillis", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cassandra.loadBalancingPolicy", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.cassandra.loadBalancingPolicy.tokenAwareLocalDC", null);
        //DataNucleus EMF Properties
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.jpa.addClassTransformer", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.jpa.persistenceContextType", null);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put("datanucleus.jpa.txnMarkForRollbackOnException", TRUE_FALSE);
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1).put(ProviderUtil.DATANUCLEUS_PROVIDER2_1.getTableGenerationPropertyName()
                ,new String[] {ProviderUtil.DATANUCLEUS_PROVIDER2_1.getTableGenerationCreateValue(),ProviderUtil.DATANUCLEUS_PROVIDER2_1.getTableGenerationDropCreateValue() });
        //DataNucleus JPA 2.2 (initially just copy of 2.1)
        possiblePropertyValues.put(ProviderUtil.DATANUCLEUS_PROVIDER2_2, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_2).putAll(possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_1));
        //DataNucleus JPA 3.0 (initially just copy of 2.2 and then replace javax to jakarta)
        possiblePropertyValues.put(ProviderUtil.DATANUCLEUS_PROVIDER3_0, new HashMap<String, String[]>());
        for (Map.Entry<String, String[]> entry : possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER2_2).entrySet()) {
            possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER3_0).put(entry.getKey().replace(JAVAX_NAMESPACE, JAKARTA_NAMESPACE), entry.getValue());
        }
        //DataNucleus JPA 3.1 (initially just copy of 3.0)
        possiblePropertyValues.put(ProviderUtil.DATANUCLEUS_PROVIDER3_1, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER3_1).putAll(possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER3_0));
        
        //DataNucleus JPA 3.2 (initially just copy of 3.0)
        possiblePropertyValues.put(ProviderUtil.DATANUCLEUS_PROVIDER3_2, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER3_2).putAll(possiblePropertyValues.get(ProviderUtil.DATANUCLEUS_PROVIDER3_0));
        
        //toplink 1.0
        possiblePropertyValues.put(ProviderUtil.TOPLINK_PROVIDER1_0, new HashMap<String, String[]>());
        possiblePropertyValues.get(ProviderUtil.TOPLINK_PROVIDER1_0).put(ProviderUtil.TOPLINK_PROVIDER1_0.getJdbcUrl(), null);
        possiblePropertyValues.get(ProviderUtil.TOPLINK_PROVIDER1_0).put(ProviderUtil.TOPLINK_PROVIDER1_0.getJdbcDriver(), null);
        possiblePropertyValues.get(ProviderUtil.TOPLINK_PROVIDER1_0).put(ProviderUtil.TOPLINK_PROVIDER1_0.getJdbcPassword(), null);
        possiblePropertyValues.get(ProviderUtil.TOPLINK_PROVIDER1_0).put(ProviderUtil.TOPLINK_PROVIDER1_0.getJdbcUsername(), null);
        possiblePropertyValues.get(ProviderUtil.TOPLINK_PROVIDER1_0).put(ProviderUtil.TOPLINK_PROVIDER1_0.getTableGenerationPropertyName()
                ,new String[] {ProviderUtil.TOPLINK_PROVIDER1_0.getTableGenerationCreateValue(),ProviderUtil.TOPLINK_PROVIDER1_0.getTableGenerationDropCreateValue() });
    }
    
    
    public static Object  getPossiblePropertyValue( Provider provider, String propName ) {
        if(provider == null) {
            provider = ProviderUtil.ECLIPSELINK_PROVIDER3_1;
        }//TODO, some logic to add, either search for all providers or some other
        Map<String, String[]> firstMap = possiblePropertyValues.get(provider);
        return firstMap != null ? firstMap.get(propName) : null;
    }
    
    /**
     * return list of pu properties for a provider including default properties
     * return default 2.0 if provider is null
     * @param provider
     * @return 
     */
    public static List<String> getKeys(Provider provider){
        //TODO: cache lists?
        ArrayList<String> ret = new ArrayList<>();
        String ver = provider == null ? null : ProviderUtil.getVersion(provider);
        if(provider == null || (ver!=null && !Persistence.VERSION_1_0.equals(ver))) {
            if (Float.parseFloat(ver) < Float.parseFloat(Persistence.VERSION_3_0)) {
                ret.addAll(possiblePropertyValues.get(null).keySet());
            } else {
                for (String key : possiblePropertyValues.get(null).keySet()) {
                    ret.add(key.replace(JAVAX_NAMESPACE, JAKARTA_NAMESPACE));
                }
            }
        }
        if(provider !=null ) {
            Map<String, String[]> props = possiblePropertyValues.get(provider);
            if(props!=null) {
                ret.addAll(props.keySet());
            }
        }
        ret.sort(new KeyOrder());
        return ret;
    }
    
     public static Map<Provider, Map<String, String[]>> getAllKeyAndValues(){
        return possiblePropertyValues;
    }   
    /**
     * return list of supported(by this class) providers with some known properties
     * @return 
     */
    public static List<Provider> getProviders(){
        ArrayList<Provider> ret = new ArrayList<>();
        ret.add(ProviderUtil.ECLIPSELINK_PROVIDER3_1);
        ret.add(ProviderUtil.HIBERNATE_PROVIDER2_2);
        ret.add(ProviderUtil.DATANUCLEUS_PROVIDER2_2);
        ret.add(ProviderUtil.OPENJPA_PROVIDER2_2);
        return ret;
    }
    
    private static final class KeyOrder implements Comparator<String>{

        @Override
        public int compare(String o1, String o2) {
            if(o1.startsWith("javax.persistence.") && !o2.startsWith("javax.persistence")) {//NOI18N
                return -11;
            } else if (!o1.startsWith("javax.persistence.") && o2.startsWith("javax.persistence")){//NOI18N
                return 1;
            } else if(o1.startsWith("jakarta.persistence.") && !o2.startsWith("jakarta.persistence")) {//NOI18N
                return -11;
            } else if (!o1.startsWith("jakarta.persistence.") && o2.startsWith("jakarta.persistence")){//NOI18N
                return 1;
            }
            return o1.compareTo(o2);
        }
        
    }
}
