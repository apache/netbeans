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
/*
 * DatabaseUtils.java
 *
 * Created on June 26, 2006, 1:55 PM
 *
 */

package org.netbeans.modules.payara.jakartaee.db;

import java.util.HashMap;
import java.util.Map;

/**
 * From DatabaseUtils in the V2 plugin
 * 
 * XXX This primarily code does linear searchs through map keys because of extra
 * logic on key comparisons.  Find a way to use the maps properly.
 *
 * @author Nitya Doraisamy
 * @author Peter Williams
 */
public class DriverMaps {
    
    private static final int INITIAL_MAP_SIZE = 107;
    
    private static Map<String, String> driverMap;
    private static Map<String, String> dsClassMap;
    private static Map<String, String> cpClassMap;

    private DriverMaps() {
    }

    /**
     * Returns the driver name for the given url
     * 
     * @param url url of this connection
     * @return driver name required
     */
    public static final String getDriverName(String url) {
        return getValueForUrl(driverMap, url);
    }

    /**
     * Returns driver class name for the given url
     * 
     * @param url url of this connection
     * @return driver class name required
     */
    public static final String getDSClassName(String url) {
        return getValueForUrl(dsClassMap, url);
    }
    
    private static final String getValueForUrl(Map<String, String> mapping, String url) {
        for(String urlPrefix: mapping.keySet()) {
            if(url.startsWith(urlPrefix)){
                return mapping.get(urlPrefix);
            }
        }
        return null;
    }
    
    /**
     * Gets the URL prefix fiven a Datasource Classname
     * @param inClass Datasource Classname
     * @return URL prefix for given Datasource Classname
     */
    public static final String getUrlPrefix(String inClass, String resType) {
        return "javax.sql.ConnectionPoolDataSource".equals(resType) ?           // NOI18N
            getPrefix(cpClassMap, inClass) : getPrefix(dsClassMap, inClass);
    }
    
    private static final String getPrefix(Map<String, String> classMap, String inClass) {
        for(String urlPrefix: classMap.keySet()) {
            String dsClass = classMap.get(urlPrefix);
            if(dsClass.equalsIgnoreCase(inClass)) {
                return urlPrefix;           
            }    
        }
        return null; 
    }
    
    static {
        driverMap = new HashMap<String, String>(INITIAL_MAP_SIZE);
        
        //IBM DB2 
        driverMap.put("jdbc:db2:", "COM.ibm.db2.jdbc.net.DB2Driver");
        //JDBC-ODBC Bridge
        driverMap.put("jdbc:odbc:", "sun.jdbc.odbc.JdbcOdbcDriver");
        //Microsoft SQL Server (Weblogic driver)
        driverMap.put("jdbc:weblogic:mssqlserver4:", "weblogic.jdbc.mssqlserver4.Driver");
        
        //Oracle-thin
        driverMap.put("jdbc:oracle:thin:", "oracle.jdbc.driver.OracleDriver");
        //Oracle //OCI 8i
        driverMap.put("jdbc:oracle:oci8:", "oracle.jdbc.driver.OracleDriver");
        //Oracle //OCI 9i
        driverMap.put("jdbc:oracle:oci:", "oracle.jdbc.driver.OracleDriver");
        
        //PointBase
        driverMap.put("jdbc:pointbase:", "com.pointbase.jdbc.jdbcUniversalDriver");
        //Cloudscape
        driverMap.put("jdbc:cloudscape:", "COM.cloudscape.core.JDBCDriver");
        //Java DB (Net)
        driverMap.put("jdbc:derby:", "org.apache.derby.jdbc.ClientDriver");
        //Firebird (JCA/JDBC driver)
        driverMap.put("jdbc:firebirdsql:", "org.firebirdsql.jdbc.FBDriver");
        //FirstSQL/J //Enterprise Server Edition
        driverMap.put("jdbc:dbcp:", "COM.FirstSQL.Dbcp.DbcpDriver");
        //FirstSQL/J //Professional Edition
        driverMap.put("jdbc:dbcp:", "COM.FirstSQL.Dbcp.DbcpDriver");
        
        //IBM DB2 (DataDirect Connect for JDBC)
        driverMap.put("jdbc:datadirect:db2:", "com.ddtek.jdbc.db2.DB2Driver");
        //Informix Dynamic Server (DataDirect Connect for JDBC)
        driverMap.put("jdbc:datadirect:informix:", "com.ddtek.jdbc.informix.InformixDriver");
        //Oracle (DataDirect Connect for JDBC)
        driverMap.put("jdbc:datadirect:oracle:", "com.ddtek.jdbc.oracle.OracleDriver");
        //Microsoft SQL Server (DataDirect Connect for JDBC)
        driverMap.put("jdbc:datadirect:sqlserver:", "com.ddtek.jdbc.sqlserver.SQLServerDriver");
        //Sybase (DataDirect Connect for JDBC)
        driverMap.put("jdbc:datadirect:sybase:", "com.ddtek.jdbc.sybase.SybaseDriver");
        
        //IDS Server
        driverMap.put("jdbc:ids:", "ids.sql.IDSDriver");
        //Informix Dynamic Server
        driverMap.put("jdbc:informix-sqli:", "com.informix.jdbc.IfxDriver");
        //InstantDB (v3.13 and earlier)
        driverMap.put("jdbc:idb:", "jdbc.idbDriver");
        //InstantDB (v3.14 and later)
        driverMap.put("jdbc:idb:", "org.enhydra.instantdb.jdbc.idbDriver");
        //Interbase (InterClient driver)
        driverMap.put("jdbc:interbase:", "interbase.interclient.Driver");
        //HSQLDB //(server)
        driverMap.put("jdbc:hsqldb:hsql:", "org.hsqldb.jdbcDriver");
        //HSQLDB //(webserver)
        driverMap.put("jdbc:hsqldb:http:", "org.hsqldb.jdbcDriver");
        //Hypersonic SQL (v1.2 and earlier)
        driverMap.put("jdbc:HypersonicSQL:", "hSql.hDriver");
        //Hypersonic SQL (v1.3 and later)
        driverMap.put("jdbc:HypersonicSQL:", "org.hsql.jdbcDriver");
        //jTDS
        driverMap.put("jdbc:jtds:sqlserver:", "net.sourceforge.jtds.jdbc.Driver");
        //jTDS
        driverMap.put("jdbc:jtds:sybase:", "net.sourceforge.jtds.jdbc.Driver");
        //Mckoi SQL Database //(server)
        driverMap.put("jdbc:mckoi:", "com.mckoi.JDBCDriver");
        //Mckoi SQL Database //(standalone)
        driverMap.put("jdbc:mckoi:local:", "com.mckoi.JDBCDriver");
        //Microsoft SQL Server (JTurbo driver)
        driverMap.put("jdbc:JTurbo:", "com.ashna.jturbo.driver.Driver");
        //Microsoft SQL Server (JTurbo driver 3.0)
        //driverMap.put("jdbc:JTurbo:", "com.newatlanta.jturbo.driver.Driver");
        //Microsoft SQL Server (Sprinta driver)
        driverMap.put("jdbc:inetdae:", "com.inet.tds.TdsDriver");
        //Microsoft SQL Server 2005 (Microsoft driver)
        //driverMap.put("jdbc:microsoft:sqlserver:", "com.microsoft.jdbc.sqlserver.SQLServerDriver");
        driverMap.put("jdbc:sqlserver:", "com.microsoft.sqlserver.jdbc.SQLServerDriver"); //NOI18N
        //MySQL (Connector/J driver)
        driverMap.put("jdbc:mysql:", "com.mysql.jdbc.Driver");
        //MySQL (MM.MySQL driver)
        //driverMap.put("jdbc:mysql:", "org.gjt.mm.mysql.Driver");
        
        //PostgreSQL (v6.5 and earlier)
        //driverMap.put("jdbc:postgresql:", "postgresql.Driver");
        //PostgreSQL (v7.0 and later)
        driverMap.put("jdbc:postgresql:", "org.postgresql.Driver");
        //Quadcap Embeddable Database
        driverMap.put("jdbc:qed:", "com.quadcap.jdbc.JdbcDriver");
        //Sybase (jConnect 4.2 and earlier)
        //driverMap.put("jdbc:sybase:Tds:", "com.sybase.jdbc.SybDriver");
        //Sybase (jConnect 5.2)
        driverMap.put("jdbc:sybase:Tds:", "com.sybase.jdbc2.jdbc.SybDriver");
        
        // Following four entries for drivers to be included in Java Studio Enterprise 7 (Bow)
        //Microsoft SQL Server Driver
        driverMap.put("jdbc:sun:sqlserver:", "com.sun.sql.jdbc.sqlserver.SQLServerDriver");
        //DB2 Driver
        driverMap.put("jdbc:sun:db2:", "com.sun.sql.jdbc.db2.DB2Driver");
        //Oracle Driver
        driverMap.put("jdbc:sun:oracle:", "com.sun.sql.jdbc.oracle.OracleDriver");
        //Sybase Driver
        driverMap.put("jdbc:sun:sybase:", "com.sun.sql.jdbc.sybase.SybaseDriver");
        
        //AS400 Driver
        driverMap.put("jdbc:as400:", "com.ibm.as400.access.AS400JDBCDriver");
    }

    static {
        dsClassMap = new HashMap<String, String>(INITIAL_MAP_SIZE);
        
        //IBM DB2 
        dsClassMap.put("jdbc:db2:", "com.ibm.db2.jcc.DB2DataSource");
        //JDBC-ODBC Bridge
        dsClassMap.put("jdbc:odbc:", "sun.jdbc.odbc.JdbcOdbcDriver");
        //Microsoft SQL Server (Weblogic driver)
        //dsClassMap.put("jdbc:weblogic:mssqlserver4:", "weblogic.jdbc.mssqlserver4.Driver");
        
        //Oracle-thin
        dsClassMap.put("jdbc:oracle:thin:", "oracle.jdbc.pool.OracleDataSource");
        //Oracle //OCI 8i
        dsClassMap.put("jdbc:oracle:oci8:", "oracle.jdbc.pool.OracleDataSource");
        //Oracle //OCI 9i
        dsClassMap.put("jdbc:oracle:oci:", "oracle.jdbc.pool.OracleDataSource");
        
        //PointBase
        dsClassMap.put("jdbc:pointbase:", "com.pointbase.jdbc.jdbcDataSource");
        //Cloudscape
        dsClassMap.put("jdbc:cloudscape:", "com.cloudscape.core.BasicDataSource");
        //Java DB (Net)
        dsClassMap.put("jdbc:derby:", "org.apache.derby.jdbc.ClientDataSource");
        //Firebird (JCA/JDBC driver)
        //dsClassMap.put("jdbc:firebirdsql:", "org.firebirdsql.jdbc.FBDriver");
        //FirstSQL/J //Enterprise Server Edition
        //dsClassMap.put("jdbc:dbcp:", "COM.FirstSQL.Dbcp.DbcpDriver");
        //FirstSQL/J //Professional Edition
        //dsClassMap.put("jdbc:dbcp:", "COM.FirstSQL.Dbcp.DbcpDriver");
        
        //IBM DB2 (DataDirect Connect for JDBC)
        dsClassMap.put("jdbc:datadirect:db2:", "com.ddtek.jdbcx.db2.DB2DataSource");
        //Informix Dynamic Server (DataDirect Connect for JDBC)
        dsClassMap.put("jdbc:datadirect:informix:", "com.ddtek.jdbcx.informix.InformixDataSource");
        //Oracle (DataDirect Connect for JDBC)
        dsClassMap.put("jdbc:datadirect:oracle:", "com.ddtek.jdbcx.oracle.OracleDataSource");
        //Microsoft SQL Server (DataDirect Connect for JDBC)
        dsClassMap.put("jdbc:datadirect:sqlserver:", "com.ddtek.jdbcx.sqlserver.SQLServerDataSource");
        //Sybase (DataDirect Connect for JDBC)
        dsClassMap.put("jdbc:datadirect:sybase:", "com.ddtek.jdbcx.sybase.SybaseDataSource");
        
        //IDS Server
        dsClassMap.put("jdbc:ids:", "ids.sql.IDSDriver");
        //Informix Dynamic Server
        dsClassMap.put("jdbc:informix-sqli:", "com.informix.jdbcx.IfxDataSource");
        //InstantDB (v3.13 and earlier)
        //dsClassMap.put("jdbc:idb:", "jdbc.idbDriver");
        //InstantDB (v3.14 and later)
        //dsClassMap.put("jdbc:idb:", "org.enhydra.instantdb.jdbc.idbDriver");
        //Interbase (InterClient driver)
        //dsClassMap.put("jdbc:interbase:", "interbase.interclient.Driver");
        //HSQLDB //(server)
        //dsClassMap.put("jdbc:hsqldb:hsql:", "org.hsqldb.jdbcDriver");
        //HSQLDB //(webserver)
        //dsClassMap.put("jdbc:hsqldb:http:", "org.hsqldb.jdbcDriver");
        //Hypersonic SQL (v1.2 and earlier)
        //dsClassMap.put("jdbc:HypersonicSQL:", "hSql.hDriver");
        //Hypersonic SQL (v1.3 and later)
        //dsClassMap.put("jdbc:HypersonicSQL:", "org.hsql.jdbcDriver");
        //jTDS
        dsClassMap.put("jdbc:jtds:sqlserver:", "net.sourceforge.jtds.jdbcx.JtdsDataSource");
        //jTDS
        dsClassMap.put("jdbc:jtds:sybase:", "net.sourceforge.jtds.jdbcx.JtdsDataSource");
        //Mckoi SQL Database //(server)
        //dsClassMap.put("jdbc:mckoi:", "com.mckoi.JDBCDriver");
        //Mckoi SQL Database //(standalone)
        //dsClassMap.put("jdbc:mckoi:local:", "com.mckoi.JDBCDriver");
        //Microsoft SQL Server (JTurbo driver old version)
        //dsClassMap.put("jdbc:JTurbo:", "com.ashna.jturbo.driver.DataSource");
        //Microsoft SQL Server (JTurbo driver 3.0)
        dsClassMap.put("jdbc:JTurbo:", "com.newatlanta.jturbo.driver.DataSource");
        //Microsoft SQL Server (Sprinta driver)
        dsClassMap.put("jdbc:inetdae:", "com.inet.tds.TdsDataSource");
        //Microsoft SQL Server 2005 (Microsoft driver)
        //dsClassMap.put("jdbc:microsoft:sqlserver:", "com.microsoft.jdbc.sqlserver.SQLServerDataSource");
        dsClassMap.put("jdbc:sqlserver:", "com.microsoft.sqlserver.jdbc.SQLServerDataSource"); //NOI18N
        //MySQL (Connector/J driver)
        //dsClassMap.put("jdbc:mysql:", "com.mysql.jdbc.Driver");
        //MySQL (MM.MySQL driver)
        dsClassMap.put("jdbc:mysql:", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        
        //PostgreSQL (v6.5 and earlier)
        //dsClassMap.put("jdbc:postgresql:", "postgresql.Driver");
        //PostgreSQL (v7.0 and later)
        dsClassMap.put("jdbc:postgresql:", "org.postgresql.ds.PGSimpleDataSource");//1st implementation without pooling
        dsClassMap.put("jdbc:postgresql:", "org.postgresql.ds.PGPoolingDataSource");//another one with pooling support
        //Quadcap Embeddable Database
        //dsClassMap.put("jdbc:qed:", "com.quadcap.jdbc.JdbcDriver");
        //Sybase (jConnect 4.2 and earlier)
        //dsClassMap.put("jdbc:sybase:Tds:", "com.sybase.jdbc.SybDriver");
        //Sybase (jConnect 5.2)
        dsClassMap.put("jdbc:sybase:Tds:", "com.sybase.jdbc2.jdbc.SybDataSource");
        
        // Following four entries for drivers to be included in Java Studio Enterprise 7 (Bow)
        //Microsoft SQL Server Driver
        dsClassMap.put("jdbc:sun:sqlserver:", "com.sun.sql.jdbcx.sqlserver.SQLServerDataSource");
        //DB2 Driver
        dsClassMap.put("jdbc:sun:db2:", "com.sun.sql.jdbcx.db2.DB2DataSource");
        //Oracle Driver
        dsClassMap.put("jdbc:sun:oracle:", "com.sun.sql.jdbcx.oracle.OracleDataSource");
        //Sybase 
        dsClassMap.put("jdbc:sun:sybase:", "com.sun.sql.jdbcx.sybase.SybaseDataSource");
        
        //AS400 
        dsClassMap.put("jdbc:as400:", "com.ibm.as400.access.AS400JDBCDataSource");
    }
    
    static {
        cpClassMap = new HashMap<String, String>(INITIAL_MAP_SIZE);
        //Java DB (Net)
        cpClassMap.put("jdbc:derby:", "org.apache.derby.jdbc.ClientConnectionPoolDataSource");
        //DB2 Driver
        cpClassMap.put("jdbc:sun:db2:", "com.sun.sql.jdbcx.db2.DB2DataSource");
        //Microsoft SQL Server Driver
        cpClassMap.put("jdbc:sun:sqlserver:", "com.sun.sql.jdbcx.sqlserver.SQLServerDataSource");
        //Oracle Driver
        cpClassMap.put("jdbc:sun:oracle:", "com.sun.sql.jdbcx.oracle.OracleDataSource");
        //Sybase Driver
        cpClassMap.put("jdbc:sun:sybase:", "com.sun.sql.jdbcx.sybase.SybaseDataSource");
        //PostgreSQL (v7.0 and later)
        cpClassMap.put("jdbc:postgresql:", "org.postgresql.ds.PGConnectionPoolDataSource");
        //Microsoft SQL Server 2000 (Microsoft driver)
        cpClassMap.put("jdbc:sqlserver:", "com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource"); 
        //jTDS
        cpClassMap.put("jdbc:jtds:sqlserver:", "net.sourceforge.jtds.jdbcx.JtdsDataSource");
        //jTDS
        cpClassMap.put("jdbc:jtds:sybase:", "net.sourceforge.jtds.jdbcx.JtdsDataSource");
        //Oracle //OCI 8i
        cpClassMap.put("jdbc:oracle:oci8:", "oracle.jdbc.pool.OracleConnectionPoolDataSource");
        //Oracle-thin
        cpClassMap.put("jdbc:oracle:thin:", "oracle.jdbc.pool.OracleConnectionPoolDataSource");
        //IBM DB2 
        cpClassMap.put("jdbc:db2:", "com.ibm.db2.jcc.DB2ConnectionPoolDataSource");
        //Microsoft SQL Server (DataDirect Connect for JDBC)
        cpClassMap.put("jdbc:datadirect:sqlserver:", "com.ddtek.jdbcx.sqlserver.SQLServerDataSource");
        //Oracle (DataDirect Connect for JDBC)
        cpClassMap.put("jdbc:datadirect:oracle:", "com.ddtek.jdbcx.oracle.OracleDataSource");
        //IBM DB2 (DataDirect Connect for JDBC)
        cpClassMap.put("jdbc:datadirect:db2:", "com.ddtek.jdbcx.db2.DB2DataSource");
        //Informix Dynamic Server (DataDirect Connect for JDBC)
        cpClassMap.put("jdbc:datadirect:informix:", "com.ddtek.jdbcx.informix.InformixDataSource");
        //Sybase (DataDirect Connect for JDBC)
        cpClassMap.put("jdbc:datadirect:sybase:", "com.ddtek.jdbcx.sybase.SybaseDataSource");
        //Sybase (jConnect 5.2)
        cpClassMap.put("jdbc:sybase:Tds:", "com.sybase.jdbc2.jdbc.SybConnectionPoolDataSource");
        //PointBase
        cpClassMap.put("jdbc:pointbase:", "com.pointbase.jdbc.jdbcDataSource");
        //Cloudscape
        cpClassMap.put("jdbc:cloudscape:", "COM.cloudscape.core.LocalConnectionPoolDataSource");
        //Informix Dynamic Server
        cpClassMap.put("jdbc:informix-sqli:", "com.informix.jdbcx.IfxConnectionPoolDataSource");
        //MySQL (MM.MySQL driver)
        cpClassMap.put("jdbc:mysql:", "com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource");
        //JDBC-ODBC Bridge
        cpClassMap.put("jdbc:odbc:", "sun.jdbc.odbc.JdbcOdbcDriver");
        
        //AS400 
        cpClassMap.put("jdbc:as400:", "com.ibm.as400.access.AS400JDBCConnectionPoolDataSource");
        
    }
}
