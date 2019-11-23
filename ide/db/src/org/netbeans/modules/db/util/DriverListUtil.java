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

package org.netbeans.modules.db.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.openide.util.NbBundle;

public class DriverListUtil {

    private static final List<JdbcUrl> templateUrls = new ArrayList<>(61);

    private DriverListUtil() {
    }
    
    private static JdbcUrl add(String name, String type, String driverClassName, String urlTemplate) {
        JdbcUrl url = new JdbcUrl(name, name, driverClassName, type, urlTemplate);
        templateUrls.add(url);
        return url;
    }
    
    private static JdbcUrl add(String name, String type, String driverClassName, String urlTemplate, boolean parseUrl) {
        JdbcUrl url = new JdbcUrl(name, name, driverClassName, type, urlTemplate, parseUrl);
        templateUrls.add(url);
        return url;
    }
        
    private  static JdbcUrl add(String name, String driverClassName, String urlTemplate) {
        return add(name, null, driverClassName, urlTemplate);
    }
    
    /**
     * Do NOT use this version of add() (with parseUrl set to true) unless you have added a 
     * unit test to DriverListUtilTest for the driver, to make sure that it is parsed correctly.
     */
    private static JdbcUrl add(String name, String driverClassName, String urlTemplate, boolean parseUrl) {
        return add(name, null, driverClassName, urlTemplate, parseUrl);
    }

    static {
        JdbcUrl url;
        
        add("IBM DB2 (net)",
        "COM.ibm.db2.jdbc.net.DB2Driver",
        "jdbc:db2://<HOST>:<PORT>/<DB>");

        add("IBM DB2 (local)",
        "COM.ibm.db2.jdbc.app.DB2Driver",
        "jdbc:db2:<DB>");
        
        add("JDBC-ODBC Bridge",
        "sun.jdbc.odbc.JdbcOdbcDriver",
        "jdbc:odbc:<DB>");
        
        add("Microsoft SQL Server (Weblogic driver)",
        "weblogic.jdbc.mssqlserver4.Driver",
        "jdbc:weblogic:mssqlserver4:<DB>@<HOST>[:<PORT>]");
        
        add("PointBase", "Network Server",
        "com.pointbase.jdbc.jdbcUniversalDriver",
        "jdbc:pointbase://<HOST>[:<PORT>]/<DB>");

        add("PointBase", "Embedded", 
        "com.pointbase.jdbc.jdbcUniversalDriver",
        "jdbc:pointbase://embedded[:<PORT>]/<DB>");
        
        add("PointBase", "Mobile Edition",
        "com.pointbase.jdbc.jdbcUniversalDriver",
        "jdbc:pointbase:<DB>");
        
        add("Cloudscape",
        "COM.cloudscape.core.JDBCDriver",
        "jdbc:cloudscape:<DB>");
        
        add("Cloudscape RMI",
        "RmiJdbc.RJDriver",
        "jdbc:rmi://<HOST>[:<PORT>]/jdbc:cloudscape:<DB>");
        
        add(NbBundle.getMessage(DriverListUtil.class, "DRIVERNAME_JavaDbEmbedded"),
        "org.apache.derby.jdbc.EmbeddedDriver",
        "jdbc:derby:<DB>[;<ADDITIONAL>]", true);
        
        add(NbBundle.getMessage(DriverListUtil.class, "DRIVERNAME_JavaDbNetwork"),
        "org.apache.derby.jdbc.ClientDriver",
        "jdbc:derby://<HOST>[:<PORT>]/<DB>[;<ADDITIONAL>]", true);
        
        add(NbBundle.getMessage(DriverListUtil.class, "DRIVERNAME_DB2JCC"),
            "com.ibm.db2.jcc.DB2Driver",
            "jdbc:db2://<HOST>:<PORT>/<DB>[:<ADDITIONAL>]", true);
        
        add(NbBundle.getMessage(DriverListUtil.class, "DRIVERNAME_DB2JCC"), NbBundle.getMessage(DriverListUtil.class, "TYPE_IDS"),
                "com.ibm.db2.jcc.DB2Driver",
                "jdbc:ids://<HOST>:<PORT>/<DB>[:<ADDITIONAL>]", true);
        
        add(NbBundle.getMessage(DriverListUtil.class, "DRIVERNAME_DB2JCC"), NbBundle.getMessage(DriverListUtil.class, "TYPE_Cloudscape"),
                "com.ibm.db2.jcc.DB2Driver",
                "jdbc:db2j:net://<HOST>:<PORT>/<DB>[:<ADDITIONAL>]", true);

        add("Firebird (JCA/JDBC driver)",
        "org.firebirdsql.jdbc.FBDriver",
        "jdbc:firebirdsql:[//<HOST>[:<PORT>]/]<DB>");
        
        add("FirstSQL/J", "Enterprise Server Edition",
        "COM.FirstSQL.Dbcp.DbcpDriver",
        "jdbc:dbcp://<HOST>[:<PORT>]");
        
        add("FirstSQL/J" , "Professional Edition", 
        "COM.FirstSQL.Dbcp.DbcpDriver",
        "jdbc:dbcp://local");
        
        add("IBM DB2 (DataDirect Connect for JDBC)",
        "com.ddtek.jdbc.db2.DB2Driver",
        "jdbc:datadirect:db2://<HOST>[:<PORT>][;databaseName=<DB>]");

        add("IDS Server",
        "ids.sql.IDSDriver",
        "jdbc:ids://<HOST>[:<PORT>]/conn?dsn='<DSN>'");
        
        add("Informix Dynamic Server",
        "com.informix.jdbc.IfxDriver",
        "jdbc:informix-sqli://<HOST>[:<PORT>]/<DB>:INFORMIXSERVER=<SERVER_NAME>");

        add("Informix Dynamic Server (DataDirect Connect for JDBC)",
        "com.ddtek.jdbc.informix.InformixDriver",
        "jdbc:datadirect:informix://<HOST>[:<PORT>];informixServer=<SERVER_NAME>;databaseName=<DB>");
        
        add("InstantDB (v3.13 and earlier)",
        "jdbc.idbDriver",
        "jdbc:idb:<DB>");
        
        add("InstantDB (v3.14 and later)",
        "org.enhydra.instantdb.jdbc.idbDriver",
        "jdbc:idb:<DB>");
        
        add("Interbase (InterClient driver)",
        "interbase.interclient.Driver",
        "jdbc:interbase://<HOST>/<DB>");
        
        add("HSQLDB", "Server", 
        "org.hsqldb.jdbcDriver",
        "jdbc:hsqldb:hsql://<HOST>[:<PORT>]");
        
        add("HSQLDB", "Embedded",
        "org.hsqldb.jdbcDriver",
        "jdbc:hsqldb:<DB>");
        
        add("HSQLDB", "Web Server",
        "org.hsqldb.jdbcDriver",
        "jdbc:hsqldb:http://<HOST>[:<PORT>]");
        
        add("HSQLDB", "In-Memory",
        "org.hsqldb.jdbcDriver",
        "jdbc:hsqldb:.");
        
        add("Hypersonic SQL (v1.2 and earlier)",
        "hSql.hDriver",
        "jdbc:HypersonicSQL:<DB>");
        
        add("Hypersonic SQL (v1.3 and later)",
        "org.hsql.jdbcDriver",
        "jdbc:HypersonicSQL:<DB>");
        
        add(NbBundle.getMessage(DriverListUtil.class, "DRIVERNAME_JTDS"), NbBundle.getMessage(DriverListUtil.class, "TYPE_ForSQLServer"),
        "net.sourceforge.jtds.jdbc.Driver",
        "jdbc:jtds:sqlserver://<HOST>[:<PORT>][/<DB>][;<ADDITIONAL>]", true);
        
        add(NbBundle.getMessage(DriverListUtil.class, "DRIVERNAME_JTDS"), NbBundle.getMessage(DriverListUtil.class, "TYPE_ForSybase"),
        "net.sourceforge.jtds.jdbc.Driver",
        "jdbc:jtds:sybase://<HOST>[:<PORT>][/<DB>][;<ADDITIONAL>]", true);

        add("Mckoi SQL Database", "Server",
        "com.mckoi.JDBCDriver",
        "jdbc:mckoi://<HOST>[:<PORT>]");
        
        add("Mckoi SQL Database", "Embedded",
        "com.mckoi.JDBCDriver",
        "jdbc:mckoi:local://<DB>");
        
        add("Microsoft SQL Server (DataDirect Connect for JDBC)",
        "com.ddtek.jdbc.sqlserver.SQLServerDriver",
        "jdbc:datadirect:sqlserver://<HOST>[:<PORT>][;databaseName=<DB>]");
        
        add("Microsoft SQL Server (JTurbo driver)",
        "com.ashna.jturbo.driver.Driver",
        "jdbc:JTurbo://<HOST>:<PORT>/<DB>");
        
        add("Microsoft SQL Server (Sprinta driver)",
        "com.inet.tds.TdsDriver",
        "jdbc:inetdae:<HOST>[:<PORT>]?database=<DB>");
        
        add("Microsoft SQL Server 2000 (Microsoft driver)",
        "com.microsoft.jdbc.sqlserver.SQLServerDriver",
        "jdbc:microsoft:sqlserver://<HOST>[:<PORT>][;DatabaseName=<DB>]");

        add(NbBundle.getMessage(DriverListUtil.class, "DRIVERNAME_MSSQL2005"),
        "com.microsoft.sqlserver.jdbc.SQLServerDriver",
        "jdbc:sqlserver://[<HOST>[\\<INSTANCE>][:<PORT>]][;databaseName=<DB>][;<ADDITIONAL>]", true);
        
        url = add(NbBundle.getMessage(DriverListUtil.class, "DRIVERNAME_MySQL"),
                "com.mysql.cj.jdbc.Driver", 
                "jdbc:mysql://[<HOST>[:<PORT>]][/<DB>][?<ADDITIONAL>]", true); // NOI18N
        url.setSampleUrl("jdbc:mysql://localhost:3306/mysql?zeroDateTimeBehavior=CONVERT_TO_NULL");
        url.setSampleUser("root");
        url.setSamplePassword("");

        url = add(NbBundle.getMessage(DriverListUtil.class, "DRIVERNAME_MariaDB"),
                "org.mariadb.jdbc.Driver",
                "jdbc:mariadb://[<HOST>[:<PORT>]][/<DB>][?<ADDITIONAL>]", true); // NOI18N
        /* The zeroDateTimeBehavior property is not supported by the MariaDB driver, so drop it from
        the sample URL ( https://jira.mariadb.org/browse/CONJ-204 ). The system database seems to
        still be called "mysql" ( https://mariadb.com/kb/en/library/the-mysql-database-tables ). */
        url.setSampleUrl("jdbc:mariadb://localhost:3306/mysql");
        url.setSampleUser("root");
        url.setSamplePassword("");

        add("MySQL (MM.MySQL driver)",
        "org.gjt.mm.mysql.Driver",
        "jdbc:mysql://<HOST>[:<PORT>]/<DB>");
        
        url = add(NbBundle.getMessage(DriverListUtil.class, "DRIVERNAME_OracleThin"),
                NbBundle.getMessage(DriverListUtil.class, "TYPE_SID"), "oracle.jdbc.OracleDriver",
                "jdbc:oracle:thin:@<HOST>:<PORT>:<SID>[?<ADDITIONAL>]", true); // NOI18N
        url.setSampleUrl("jdbc:oracle:thin:@localhost:1521:XE");
        url.setSampleUser("hr");
        url.setSamplePassword("hr");
        
        url = add(NbBundle.getMessage(DriverListUtil.class, "DRIVERNAME_OracleThin"),
                NbBundle.getMessage(DriverListUtil.class, "TYPE_Service"), "oracle.jdbc.OracleDriver",
                "jdbc:oracle:thin:@//<HOST>[:<PORT>][/<SERVICE>][?<ADDITIONAL>]", true); // NOI18N
        
        url = add(NbBundle.getMessage(DriverListUtil.class, "DRIVERNAME_OracleThin"),
                NbBundle.getMessage(DriverListUtil.class, "TYPE_TNSName"), "oracle.jdbc.OracleDriver",
                "jdbc:oracle:thin:@<TNSNAME>[?<ADDITIONAL>]", true); // NOI18N
                
        url = add(NbBundle.getMessage(DriverListUtil.class, "DRIVERNAME_OracleOCI"),
                "OCI8 " + NbBundle.getMessage(DriverListUtil.class, "TYPE_SID"), "oracle.jdbc.driver.OracleDriver",
                "jdbc:oracle:oci8:@<HOST>:<PORT>:<SID>[?<ADDITIONAL>]", true); // NOI18N
        url.setSampleUrl("jdbc:oracle:oci8:@localhost:1521:XE");
        url.setSampleUser("hr");
        url.setSamplePassword("hr");
        
        url = add(NbBundle.getMessage(DriverListUtil.class, "DRIVERNAME_OracleOCI"),
                "OCI8 " + NbBundle.getMessage(DriverListUtil.class, "TYPE_Service"), "oracle.jdbc.driver.OracleDriver",
                "jdbc:oracle:oci8:@//<HOST>[:<PORT>][/<SERVICE>][?<ADDITIONAL>]", true); // NOI18N
        
        url = add(NbBundle.getMessage(DriverListUtil.class, "DRIVERNAME_OracleOCI"),
                NbBundle.getMessage(DriverListUtil.class, "TYPE_TNSName"), "oracle.jdbc.driver.OracleDriver",
                "jdbc:oracle:oci:@<TNSNAME>[?<ADDITIONAL>]", true); // NOI18N
        
        url = add(NbBundle.getMessage(DriverListUtil.class, "DRIVERNAME_OracleOCI"),
                NbBundle.getMessage(DriverListUtil.class, "TYPE_SID"), "oracle.jdbc.driver.OracleDriver",
                "jdbc:oracle:oci:@<HOST>:<PORT>:<SID>[?<ADDITIONAL>]", true); // NOI18N
        url.setSampleUrl("jdbc:oracle:oci:@localhost:1521:XE");
        url.setSampleUser("hr");
        url.setSamplePassword("hr");
        
        add(NbBundle.getMessage(DriverListUtil.class, "DRIVERNAME_OracleOCI"),
                NbBundle.getMessage(DriverListUtil.class, "TYPE_Service"), "oracle.jdbc.driver.OracleDriver",
                "jdbc:oracle:oci:@//<HOST>[:<PORT>][/<SERVICE>][?<ADDITIONAL>]", true); // NOI18N
        
        add("Oracle (DataDirect Connect for JDBC)",
        "com.ddtek.jdbc.oracle.OracleDriver",
        "jdbc:datadirect:oracle://<HOST>[:<PORT>];SID=<SID>");
        
        url = add("PostgreSQL (v6.5 and earlier)",
                "postgresql.Driver",
                "jdbc:postgresql:[//<HOST>[:<PORT>]/]<DB>[?<ADDITIONAL>]");
        
        url = add(NbBundle.getMessage(DriverListUtil.class, "DRIVERNAME_PostgreSQL"), // 7.0 and later
                "org.postgresql.Driver",
                "jdbc:postgresql:[//<HOST>[:<PORT>]/]<DB>[?<ADDITIONAL>]", true);
        url.setSampleUrl("jdbc:postgresql://localhost:5432/postgres");
        url.setSampleUser("postgres");
        url.setSamplePassword("");
        
        add("Quadcap Embeddable Database",
        "com.quadcap.jdbc.JdbcDriver",
        "jdbc:qed:<DB>");
        
        add("Sybase (jConnect 4.2 and earlier)",
        "com.sybase.jdbc.SybDriver",
        "jdbc:sybase:Tds:<HOST>[:<PORT>]");
        
        add("Sybase (jConnect 5.2)",
        "com.sybase.jdbc2.jdbc.SybDriver",
        "jdbc:sybase:Tds:<HOST>[:<PORT>]");
        
        add("Sybase (DataDirect Connect for JDBC)",
        "com.ddtek.jdbc.sybase.SybaseDriver",
        "jdbc:datadirect:sybase://<HOST>[:<PORT>][;databaseName=<DB>]");

        // Following four entries for drivers to be included in Java Studio Enterprise 7 (Bow)
        add("Microsoft SQL Server Driver",
        "com.sun.sql.jdbc.sqlserver.SQLServerDriver",
        "jdbc:sun:sqlserver://<HOST>[:<PORT>]");       
        
        add("DB2 Driver",
        "com.sun.sql.jdbc.db2.DB2Driver",
        "jdbc:sun:db2://<HOST>[:<PORT>];databaseName=<DB>");  
        
        add("Oracle Driver",
        "com.sun.sql.jdbc.oracle.OracleDriver",
        "jdbc:sun:oracle://<HOST>[:<PORT>][;SID=<SID>]");  
        
        add("Sybase Driver",
        "com.sun.sql.jdbc.sybase.SybaseDriver",
        "jdbc:sun:sybase://<HOST>[:<PORT]");          

        add("SQLite",
            "org.sqlite.JDBC",
            "jdbc:sqlite:<FILE>");
        
        add("H2 Database Engine",
            "org.h2.Driver",
            "jdbc:h2:<FILE>");
    }
    
    public static Set<String> getDrivers() {
        TreeSet<String> drivers = new TreeSet<>();
        for (JdbcUrl url : templateUrls) {
            // A set contains no duplicate elements, so if the same class name 
            // is found twice, that's OK, because it just replaces the entry
            // that was already there
            drivers.add(url.getClassName());
        }
        return drivers;
    }
    
    public static List<JdbcUrl> getJdbcUrls(JDBCDriver driver) {
        List<JdbcUrl> driverUrls = new ArrayList<>(3);
        
        for (JdbcUrl url : templateUrls) {
            if (url.getClassName().equals(driver.getClassName())) {
                JdbcUrl newurl = new JdbcUrl(url, driver);
                driverUrls.add(newurl);
            }
        }

        if (driverUrls.isEmpty()) {
            driverUrls.add(new JdbcUrl(driver));
        }

        return new ArrayList<>(driverUrls);
    }
    
    static List<JdbcUrl> getJdbcUrls() {
        // For unit testing
        return templateUrls;
    }
    
    public static String getName(String driverClass) {
        // Find the first match
        for ( JdbcUrl url : templateUrls) {
            if (url.getClassName().equals(driverClass)) {
                return url.getName();
            }
        }
        
        return "";
    }
    
    public static String findFreeName(String name) {
        String ret;
        List<String> names = new ArrayList<>();
        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers();
        for (int i = 0; i < drivers.length; i++) {
            names.add(drivers[i].getDisplayName());
        }
        if (names.contains(name)) {
            for (int i = 1;; i++) {
                ret = name + " (" + i + ")"; // NOI18N
                if (!names.contains(ret)) {
                    return ret;
                }
            }
        } else {
            return name;
        }
    }
}
