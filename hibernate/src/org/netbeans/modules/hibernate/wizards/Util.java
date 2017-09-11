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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.wizards;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.hibernate.cfg.HibernateCfgProperties;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;

/**
 * This class lists all the database dialects, drivers and URLs * 
 * 
 * @author gowri
 */
public class Util {

    private static String[] dialectCodes = new String[]{
        "DB2",
        "DB2/390",
        "DB2/400",
        "Derby",
        "Firebird",
        "FrontBase",
        "HSQL",
        "Informix",
        "Ingres",
        "Interbase",
        "Mckoi SQL",
        "MySQL(Connector/J driver)",
        "MySQL (InnoDB)",
        "MySQL (MyISAM)",
        "Oracle (Any version)",
        "Oracle 9i/10g",
        "Pointbase",
        "PostgreSQL",
        "Progress",
        "SAP DB",
        "SQL Server",
        "Sybase",
        "Sybase Anywhere"
    };
    private static String[] dialects = HibernateCfgProperties.dialects;
    private static String[] drivers = new String[]{
        "COM.ibm.db2.jdbc.app.DB2Driver",
        "org.apache.derby.jdbc.ClientDriver",
        "org.firebirdsql.jdbc.FBDriver",
        "org.hsqldb.jdbcDriver",
        "com.informix.jdbc.IfxDriver",
        "interbase.interclient.Driver",
        "com.mckoi.JDBCDriver",
        "com.mysql.jdbc.Driver",
        "oracle.jdbc.OracleDriver",
        "oracle.jdbc.driver.OracleDriver",
        "com.pointbase.jdbc.jdbcUniversalDriver",
        "org.postgresql.Driver",
        "com.sun.sql.jdbc.sqlserver.SQLServerDriver",
        "com.sun.sql.jdbc.sybase.SybaseDriver"
    };
    private static String[] urlConnections = new String[]{
        "jdbc:db2:<DB>",
        "jdbc:derby://localhost:1527/travel",
        "jdbc:firebirdsql:[//<HOST>[:<PORT>]/]<DB>",
        "jdbc:hsqldb:hsql://<HOST>[:<PORT>]",
        "jdbc:informix-sqli://<HOST>:<PORT>/<DB>:INFORMIXSERVER=<SERVER_NAME>",
        "jdbc:interbase://<HOST>/<DB>",
        "jdbc:mckoi://<HOST>[:<PORT>]",
        "jdbc:mysql:///test",
        "jdbc:oracle:thin:@<HOST>:<PORT>:<SID>",
        "jdbc:oracle:oci:@<SID>",
        "jdbc:pointbase://<HOST>[:<PORT>]/<DB>",
        "jdbc:postgresql://<HOST>:<PORT>/<DB>",
        "jdbc:sun:sqlserver://server_name[:portNumber]",
        "jdbc:sun:sybase://server_name[:portNumber]"
    };
    private static Map<String, String> dialectMap = new HashMap<String, String>();

    static {
        for (int i = 0; i < dialects.length; i++) {
            dialectMap.put(dialectCodes[i], dialects[i]);
        }
    }
    private static Map<String, String> driverMap = new HashMap<String, String>();

    static {
        driverMap.put(dialectCodes[0], drivers[0]);
        driverMap.put(dialectCodes[3], drivers[1]);
        driverMap.put(dialectCodes[4], drivers[2]);
        driverMap.put(dialectCodes[6], drivers[3]);
        driverMap.put(dialectCodes[7], drivers[4]);
        driverMap.put(dialectCodes[9], drivers[5]);
        driverMap.put(dialectCodes[10], drivers[6]);
        driverMap.put(dialectCodes[11], drivers[7]);
        driverMap.put(dialectCodes[14], drivers[8]);
        driverMap.put(dialectCodes[15], drivers[9]);
        driverMap.put(dialectCodes[16], drivers[10]);
        driverMap.put(dialectCodes[17], drivers[11]);
        driverMap.put(dialectCodes[20], drivers[12]);
        driverMap.put(dialectCodes[21], drivers[13]);

    }
    private static Map<String, String> driversMap = new HashMap<String, String>();

    static {
        driversMap.put(drivers[0], dialectCodes[0]);
        driversMap.put(drivers[1], dialectCodes[3]);
        driversMap.put(drivers[2], dialectCodes[4]);
        driversMap.put(drivers[3], dialectCodes[6]);
        driversMap.put(drivers[4], dialectCodes[7]);
        driversMap.put(drivers[5], dialectCodes[9]);
        driversMap.put(drivers[6], dialectCodes[10]);
        driversMap.put(drivers[7], dialectCodes[11]);
        driversMap.put(drivers[8], dialectCodes[14]);
        driversMap.put(drivers[9], dialectCodes[15]);
        driversMap.put(drivers[10], dialectCodes[16]);
        driversMap.put(drivers[11], dialectCodes[17]);
        driversMap.put(drivers[12], dialectCodes[20]);
        driversMap.put(drivers[13], dialectCodes[21]);

    }
    private static Map<String, String> urlConnectionMap = new HashMap<String, String>();

    static {
        urlConnectionMap.put(dialectCodes[0], urlConnections[0]);
        urlConnectionMap.put(dialectCodes[3], urlConnections[1]);
        urlConnectionMap.put(dialectCodes[4], urlConnections[2]);
        urlConnectionMap.put(dialectCodes[6], urlConnections[3]);
        urlConnectionMap.put(dialectCodes[7], urlConnections[4]);
        urlConnectionMap.put(dialectCodes[9], urlConnections[5]);
        urlConnectionMap.put(dialectCodes[10], urlConnections[6]);
        urlConnectionMap.put(dialectCodes[11], urlConnections[7]);
        urlConnectionMap.put(dialectCodes[14], urlConnections[8]);
        urlConnectionMap.put(dialectCodes[15], urlConnections[9]);
        urlConnectionMap.put(dialectCodes[16], urlConnections[10]);
        urlConnectionMap.put(dialectCodes[17], urlConnections[11]);
        urlConnectionMap.put(dialectCodes[20], urlConnections[12]);
        urlConnectionMap.put(dialectCodes[21], urlConnections[13]);

    }

    public static String[] getDialectCodes() {
        return dialectCodes;
    }

    public static String[] getDrivers() {
        return drivers;

    }

    public static String[] getURLConnections() {
        return urlConnections;
    }

    public static String getSelectedDialect(String code) {
        return (String) dialectMap.get(code);
    }

    public static String getSelectedDriver(String code) {
        return (String) driverMap.get(code);
    }

    public static String getSelectedURLConnection(String code) {
        return (String) urlConnectionMap.get(code);
    }

    public static String getDailectCode(String dialectName) {
        for (String key : dialectMap.keySet()) {
            if (dialectMap.get(key).equals(dialectName)) {
                return key;
            }
        }
        return ""; // A blank string is ok here.
    }

    public static String getDialectName(String driver) {
        if (driversMap.containsKey(driver)) {
            return dialectMap.get(driversMap.get(driver));
        }
        return "";
    }
}
