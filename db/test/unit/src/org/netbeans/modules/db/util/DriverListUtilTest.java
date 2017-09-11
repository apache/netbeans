/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.db.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static junit.framework.Assert.assertEquals;
import junit.framework.TestCase;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.openide.util.NbBundle;

/**
 *
 * @author David
 */
public class DriverListUtilTest extends TestCase {
    private static final String HOST = "myhost";
    private static final String PORT = "8888";
    private static final String DB = "mydb";
    private static final String SERVERNAME = "servername";
    private static final String ADDITIONAL = "foo;bar;baz";
    private static final String SERVICENAME = "servicename";
    private static final String INSTANCE = "instancename";
    private static final String SID = "mysid";
    private static final String DSN = "mydsn";
    private static final String TNSNAME = "mytns";
    
    private static final HashMap<String, String> ALLPROPS = new HashMap<>();
    
    private static final ArrayList<String> STD_SUPPORTED_PROPS = new ArrayList<>();
    
    static {
        ALLPROPS.put(JdbcUrl.TOKEN_HOST, HOST);
        ALLPROPS.put(JdbcUrl.TOKEN_DB, DB);
        ALLPROPS.put(JdbcUrl.TOKEN_PORT, PORT);
        ALLPROPS.put(JdbcUrl.TOKEN_SERVERNAME, SERVERNAME);
        ALLPROPS.put(JdbcUrl.TOKEN_ADDITIONAL, ADDITIONAL);
        ALLPROPS.put(JdbcUrl.TOKEN_DSN, DSN);
        ALLPROPS.put(JdbcUrl.TOKEN_SERVICENAME, SERVICENAME);
        ALLPROPS.put(JdbcUrl.TOKEN_SID, SID);
        ALLPROPS.put(JdbcUrl.TOKEN_TNSNAME, TNSNAME);
        ALLPROPS.put(JdbcUrl.TOKEN_INSTANCE, INSTANCE);
        
        STD_SUPPORTED_PROPS.add(JdbcUrl.TOKEN_HOST);
        STD_SUPPORTED_PROPS.add(JdbcUrl.TOKEN_PORT);
        STD_SUPPORTED_PROPS.add(JdbcUrl.TOKEN_DB);
        STD_SUPPORTED_PROPS.add(JdbcUrl.TOKEN_ADDITIONAL);
    }
    
    public DriverListUtilTest(String testName) {
        super(testName);
    }
    
    public void testNonParsedJdbcUrls() throws Exception {
        Collection<JdbcUrl> urls = DriverListUtil.getJdbcUrls();
        for ( JdbcUrl url : urls ) {
            if (! url.isParseUrl()) {
                testNonParsedUrl(url);
            }
        }
    }
    
    /**
     * Reproducer for bug #229250. - Single URL
     *
     * @throws MalformedURLException
     */
    public void testGetJdbcUrls() throws MalformedURLException {
        JDBCDriver driver = JDBCDriver.create("Mysql 1", "Mysql 1", "com.mysql.jdbc.Driver", new URL[] {new URL("file://demo1")});
        JDBCDriver driver2 = JDBCDriver.create("Mysql 2", "Mysql 2", "com.mysql.jdbc.Driver", new URL[] {new URL("file://demo1")});
        assertEquals(1, DriverListUtil.getJdbcUrls(driver).size());
        assertEquals(1, DriverListUtil.getJdbcUrls(driver2).size());
        assertEquals(1, DriverListUtil.getJdbcUrls(driver).size());
        assertEquals(1, DriverListUtil.getJdbcUrls(driver2).size());
    }

    /**
     * Reproducer for bug #229250. - Multiple URLs
     *
     * @throws MalformedURLException
     */
    public void testGetJdbcUrlsMultiple() throws MalformedURLException {
        JDBCDriver driver = JDBCDriver.create("PB 1", "PB 1", "com.pointbase.jdbc.jdbcUniversalDriver", new URL[] {new URL("file://demo1")});
        JDBCDriver driver2 = JDBCDriver.create("PB 2", "PB 2", "com.pointbase.jdbc.jdbcUniversalDriver", new URL[] {new URL("file://demo1")});
        assertEquals(3, DriverListUtil.getJdbcUrls(driver).size());
        assertEquals(3, DriverListUtil.getJdbcUrls(driver2).size());
        assertEquals(3, DriverListUtil.getJdbcUrls(driver).size());
        assertEquals(3, DriverListUtil.getJdbcUrls(driver2).size());
    }

    private JdbcUrl getJdbcUrl(String name, String type) throws Exception {
        Collection<JdbcUrl> urls = DriverListUtil.getJdbcUrls();
        for (JdbcUrl url : urls) {
            if (url.getName().equals(name) &&
                    isEqual(url.getType(), type)) {
                return url;
            }
        }
        
        throw new Exception("No JdbcUrl found for name " + name + " and type " + type);
    }
        
    private boolean isEqual(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        }
        
        if (o1 == null || o2 == null) {
            return false;
        }
        
        return o1.equals(o2);
    }
    
    public void testJavaDbEmbedded() throws Exception {
        ArrayList<String> requiredProps = new ArrayList<String>();
        requiredProps.add(JdbcUrl.TOKEN_DB);
        
        ArrayList<String> supportedProps = new ArrayList<String>();
        supportedProps.add(JdbcUrl.TOKEN_DB);
        supportedProps.add(JdbcUrl.TOKEN_ADDITIONAL);
        
        JdbcUrl url = checkUrl(getDriverName("DRIVERNAME_JavaDbEmbedded"), null, "org.apache.derby.jdbc.EmbeddedDriver", 
                "jdbc:derby:<DB>[;<ADDITIONAL>]", supportedProps, requiredProps);
        
        HashMap<String, String> propValues = buildPropValues(supportedProps);        
        testUrlString(url, propValues, "jdbc:derby:" + DB + ";" + ADDITIONAL);

        propValues.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, propValues, "jdbc:derby:" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_DB);
        testMissingParameter(url, propValues);
        
        testBadUrlString(url, "jdbc:derby:");
        testBadUrlString(url, "jdbc:daryb://db");
        testBadUrlString(url, "jdbc:derby/:db;create=true");
    }

    public void testJavaDbNetwork() throws Exception {
        ArrayList<String> requiredProps = new ArrayList<String>();
        requiredProps.add(JdbcUrl.TOKEN_HOST);
        requiredProps.add(JdbcUrl.TOKEN_DB);
        JdbcUrl url = checkUrl(getDriverName("DRIVERNAME_JavaDbNetwork"), null, "org.apache.derby.jdbc.ClientDriver", 
                "jdbc:derby://<HOST>[:<PORT>]/<DB>[;<ADDITIONAL>]", STD_SUPPORTED_PROPS, requiredProps);
        
        HashMap<String, String> propValues = buildPropValues(STD_SUPPORTED_PROPS);        
        testUrlString(url, propValues, "jdbc:derby://" + HOST + ":" + PORT + "/" + DB + ";" + ADDITIONAL);

        propValues.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, propValues, "jdbc:derby://" + HOST + ":" + PORT + "/" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_PORT);
        testUrlString(url, propValues, "jdbc:derby://" + HOST + "/" + DB);  
        
        propValues.remove(JdbcUrl.TOKEN_DB);
        testMissingParameter(url, propValues);
        
        propValues.remove(JdbcUrl.TOKEN_HOST);
        propValues.put(JdbcUrl.TOKEN_DB, DB);
        testMissingParameter(url, propValues);
        
        testBadUrlString(url, "jdbc:derby:///db");
        testBadUrlString(url, "jdbc:derby://localhost");
        testBadUrlString(url, "jdbc:derby://localhost/;create=true");
        testBadUrlString(url, "jdbc:derby:/localhost:8889/db;create=true");
    }

    public void testMySQL() throws Exception {
        ArrayList<String> requiredProps = new ArrayList<String>();
        JdbcUrl url = checkUrl(getDriverName("DRIVERNAME_MySQL"), null, "com.mysql.jdbc.Driver", 
                "jdbc:mysql://[<HOST>[:<PORT>]][/<DB>][?<ADDITIONAL>]",
                STD_SUPPORTED_PROPS, requiredProps);
        
        HashMap<String, String> propValues = buildPropValues(STD_SUPPORTED_PROPS);
        
        testUrlString(url, propValues, "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB + "?" + ADDITIONAL);

        propValues.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, propValues, "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_PORT);
        testUrlString(url, propValues, "jdbc:mysql://" + HOST + "/" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_HOST);
        testUrlString(url, propValues, "jdbc:mysql:///" + DB); 
    }
    
    enum DB2Types { DB2, IDS, CLOUDSCAPE };
    
    public void testDB2() throws Exception {
        testDB2(DB2Types.DB2);
    }
    
    public void testDB2IDS() throws Exception {
        testDB2(DB2Types.IDS);
    }
    
    public void testDB2Cloudscape() throws Exception {
        testDB2(DB2Types.CLOUDSCAPE);
    }
    private void testDB2(DB2Types type) throws Exception {
        ArrayList<String> requiredProps = new ArrayList<String>();
        requiredProps.add(JdbcUrl.TOKEN_HOST);
        requiredProps.add(JdbcUrl.TOKEN_PORT);
        requiredProps.add(JdbcUrl.TOKEN_DB);
        
        String typeString;
        String urlType;
        switch(type) {
            case DB2:
                typeString = null;
                urlType = "db2";
                break;
            case IDS:
                typeString = getType("TYPE_IDS");
                urlType = "ids";
                break;
            case CLOUDSCAPE:
                typeString = getType("TYPE_Cloudscape");
                urlType = "db2j:net";
                break;
            default:
                throw new Exception("Unrecognized type " + type);
        }
        
        String prefix = "jdbc:" + urlType + "://";
        
        JdbcUrl url = checkUrl(getDriverName("DRIVERNAME_DB2JCC"), typeString, "com.ibm.db2.jcc.DB2Driver",
                prefix + "<HOST>:<PORT>/<DB>[:<ADDITIONAL>]", 
                STD_SUPPORTED_PROPS, requiredProps);
        
        HashMap<String, String> propValues = buildPropValues(STD_SUPPORTED_PROPS);        
        testUrlString(url, propValues, prefix + HOST + ":" + PORT + "/" + DB + ":" + ADDITIONAL);

        propValues.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, propValues, prefix + HOST + ":" + PORT + "/" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_PORT);
        testMissingParameter(url, propValues);
        
        propValues.remove(JdbcUrl.TOKEN_DB);
        propValues.put(JdbcUrl.TOKEN_PORT, PORT);
        testMissingParameter(url, propValues);
        
        propValues.remove(JdbcUrl.TOKEN_HOST);
        propValues.put(JdbcUrl.TOKEN_DB, DB);
        testMissingParameter(url, propValues);
        
        testBadUrlString(url, "jdbc:db2:///db");
        testBadUrlString(url, "jdbc:db2://localhost");
    }
    
    enum JTDSTypes { SYBASE, SQLSERVER } ;
    
    public void testJTDSSybase() throws Exception {
        testJTDS(JTDSTypes.SYBASE);
    }
    
    public void testJTDSSQLServer() throws Exception {
        testJTDS(JTDSTypes.SQLSERVER);
    }
    private void testJTDS(JTDSTypes type) throws Exception {
        ArrayList<String> requiredProps = new ArrayList<String>();
        requiredProps.add(JdbcUrl.TOKEN_HOST);
        
        String typeString;
        String urlType;
        switch(type) {
            case SYBASE:
                typeString = getType("TYPE_ForSybase");
                urlType = "sybase";
                break;
            case SQLSERVER:
                typeString = getType("TYPE_ForSQLServer");
                urlType = "sqlserver";
                break;
            default:
                throw new Exception("Unrecognized type " + type);
        }
        
        String prefix = "jdbc:jtds:" + urlType + "://";
        
        JdbcUrl url = checkUrl(getDriverName("DRIVERNAME_JTDS"), typeString, "net.sourceforge.jtds.jdbc.Driver",
                prefix + "<HOST>[:<PORT>][/<DB>][;<ADDITIONAL>]", 
                STD_SUPPORTED_PROPS, requiredProps);
        
        HashMap<String, String> propValues = buildPropValues(STD_SUPPORTED_PROPS);        
        testUrlString(url, propValues, prefix + HOST + ":" + PORT + "/" + DB + ";" + ADDITIONAL);

        propValues.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, propValues, prefix + HOST + ":" + PORT + "/" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_PORT);
        testUrlString(url, propValues, prefix + HOST + "/" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_DB);
        propValues.put(JdbcUrl.TOKEN_PORT, PORT);
        testUrlString(url, propValues, prefix + HOST + ":" + PORT);
        
        propValues.remove(JdbcUrl.TOKEN_HOST);
        propValues.put(JdbcUrl.TOKEN_DB, DB);
        testMissingParameter(url, propValues);
        
        testBadUrlString(url, "jdbc:jtds:///db");
        testBadUrlString(url, "jdbc:jtds://localhost");
    }
    
    public void testMSSQL2005() throws Exception {
        /*
                add(getMessage("DRIVERNAME_MSSQL2005"),
        "com.microsoft.sqlserver.jdbc.SQLServerDriver",
        "jdbc:sqlserver://[<HOST>[\\<INSTANCE>][:<PORT>]][;databaseName=<DB>][;<ADDITIONAL>]", true);
        */
        ArrayList<String> supportedProps = new ArrayList<String>();
        supportedProps.addAll(STD_SUPPORTED_PROPS);
        supportedProps.add(JdbcUrl.TOKEN_INSTANCE);
        
        ArrayList<String> requiredProps = new ArrayList<String>();
        JdbcUrl url = checkUrl(getDriverName("DRIVERNAME_MSSQL2005"), null, 
                "com.microsoft.sqlserver.jdbc.SQLServerDriver", 
                "jdbc:sqlserver://[<HOST>[\\<INSTANCE>][:<PORT>]][;databaseName=<DB>][;<ADDITIONAL>]", 
                supportedProps, requiredProps);
        
        HashMap<String, String> propValues = buildPropValues(supportedProps);        
        testUrlString(url, propValues, "jdbc:sqlserver://" + HOST + "\\" + INSTANCE + ":" + PORT + ";databaseName=" + DB + ";" + ADDITIONAL);

        propValues.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, propValues, "jdbc:sqlserver://" + HOST + "\\" + INSTANCE + ":" + PORT + ";databaseName=" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_PORT);
        testUrlString(url, propValues, "jdbc:sqlserver://" + HOST + "\\" + INSTANCE + ";databaseName=" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_INSTANCE);
        testUrlString(url, propValues, "jdbc:sqlserver://" + HOST + ";databaseName=" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_DB);
        testUrlString(url, propValues, "jdbc:sqlserver://" + HOST);
        
        propValues.remove(JdbcUrl.TOKEN_HOST);
        propValues.put(JdbcUrl.TOKEN_DB, DB);
        testUrlString(url, propValues, "jdbc:sqlserver://;databaseName=" + DB);
        
        propValues.clear();
        testUrlString(url, propValues, "jdbc:sqlserver://");
        
    }

    enum OracleTypes { THIN, OCI, OCI8 };
    
    public void testOracleThinSID() throws Exception {
        testOracleSID(OracleTypes.THIN);
    }
    
    public void testOracleOciSID() throws Exception {
        testOracleSID(OracleTypes.OCI);
    }
    
    public void testOracleOci8SID() throws Exception {
        testOracleSID(OracleTypes.OCI8);
    }
    
    public void testOracleThinServiceName() throws Exception {
        testOracleServiceName(OracleTypes.THIN);
    }
    public void testOracleOciServiceName() throws Exception {
        testOracleServiceName(OracleTypes.OCI);
    }
    public void testOracleOci8ServiceName() throws Exception {
        testOracleServiceName(OracleTypes.OCI8);
    }
    
    public void testOracleThinTnsName() throws Exception {
        testOracleTnsName(OracleTypes.THIN);
    }
    public void testOracleOciTnsName() throws Exception {
        testOracleTnsName(OracleTypes.OCI);
    }

    private JdbcUrl checkOracleUrl(OracleTypes otype, String urlSuffix, String type,
            List<String> supportedProps, List<String> requiredProps) throws Exception {
        String driverClass;
        String driverName;
        
        switch (otype) {
            case THIN:
                driverClass = "oracle.jdbc.OracleDriver";
                driverName = getDriverName("DRIVERNAME_OracleThin");
                break;
            case OCI:
                driverClass = "oracle.jdbc.driver.OracleDriver";
                driverName = getDriverName("DRIVERNAME_OracleOCI");
                break;
            case OCI8:
                driverClass = "oracle.jdbc.driver.OracleDriver";
                driverName = getDriverName("DRIVERNAME_OracleOCI");
                type = "OCI8 " + type;
                break;
            default:
                throw new Exception("Unknown Oracle Type " + otype);                
        }
        
        String prefix = getOracleUrlPrefix(otype);        
        
        return checkUrl(driverName, type, driverClass, 
                prefix + urlSuffix, supportedProps, requiredProps);
     
    }
    
    private String getOracleUrlPrefix(OracleTypes otype) {
        String prefix = "jdbc:oracle:";
        switch (otype) {
            case THIN:
                prefix = prefix + "thin";
                break;
            case OCI:
                prefix = prefix  + "oci";
                break;
            case OCI8:
                prefix = prefix + "oci8";
                break;
        }
        
        prefix = prefix + ":@";
        return prefix;
    }
    
    private void testOracleSID(OracleTypes otype) throws Exception {
        ArrayList<String> requiredProps = new ArrayList<String>();
        requiredProps.add(JdbcUrl.TOKEN_HOST);
        requiredProps.add(JdbcUrl.TOKEN_SID);
        requiredProps.add(JdbcUrl.TOKEN_PORT);
        
        ArrayList<String> supportedProps = new ArrayList<String>();
        supportedProps.addAll(requiredProps);
        supportedProps.add(JdbcUrl.TOKEN_ADDITIONAL);
        
        JdbcUrl url = checkOracleUrl(otype, "<HOST>:<PORT>:<SID>[?<ADDITIONAL>]", getType("TYPE_SID"),
                supportedProps, requiredProps);

        String prefix = getOracleUrlPrefix(otype);
        
        HashMap<String, String> propValues = buildPropValues(supportedProps);
        
        testUrlString(url, propValues, prefix + HOST + ":" + PORT + ":" + SID + "?" + ADDITIONAL);

        propValues.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, propValues, prefix + HOST + ":" + PORT + ":" + SID);
                
        propValues.remove(JdbcUrl.TOKEN_SID);
        testMissingParameter(url, propValues);
        
        propValues.remove(JdbcUrl.TOKEN_HOST);
        propValues.put(JdbcUrl.TOKEN_SID, SID);
        testMissingParameter(url, propValues);
        
        testBadUrlString(url, prefix + ":db");
        testBadUrlString(url, prefix);
    }
    
    private void testOracleServiceName(OracleTypes otype) throws Exception {
        ArrayList<String> requiredProps = new ArrayList<String>();
        requiredProps.add(JdbcUrl.TOKEN_HOST);
        
        ArrayList<String> supportedProps = new ArrayList<String>();
        supportedProps.addAll(requiredProps);
        supportedProps.add(JdbcUrl.TOKEN_ADDITIONAL);
        supportedProps.add(JdbcUrl.TOKEN_PORT);
        supportedProps.add(JdbcUrl.TOKEN_SERVICENAME);
        
        JdbcUrl url = checkOracleUrl(otype, "//<HOST>[:<PORT>][/<SERVICE>][?<ADDITIONAL>]", getType("TYPE_Service"),
                supportedProps, requiredProps);

        String prefix = getOracleUrlPrefix(otype);
        
        HashMap<String, String> propValues = buildPropValues(supportedProps);
        
        testUrlString(url, propValues, prefix + "//" + HOST + ":" + PORT + "/" + SERVICENAME + "?" + ADDITIONAL);

        propValues.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, propValues, prefix + "//" + HOST + ":" + PORT + "/" + SERVICENAME);
                
        propValues.remove(JdbcUrl.TOKEN_HOST);
        propValues.put(JdbcUrl.TOKEN_SERVICENAME, SERVICENAME);
        testMissingParameter(url, propValues);
        
        testBadUrlString(url, prefix + ":db");
        testBadUrlString(url, prefix);
    }
    private void testOracleTnsName(OracleTypes otype) throws Exception {
        ArrayList<String> requiredProps = new ArrayList<String>();
        requiredProps.add(JdbcUrl.TOKEN_TNSNAME);
        
        ArrayList<String> supportedProps = new ArrayList<String>();
        supportedProps.addAll(requiredProps);
        supportedProps.add(JdbcUrl.TOKEN_ADDITIONAL);
        
        JdbcUrl url = checkOracleUrl(otype, "<TNSNAME>[?<ADDITIONAL>]", getType("TYPE_TNSName"),
                supportedProps, requiredProps);

        String prefix = getOracleUrlPrefix(otype);
        
        HashMap<String, String> propValues = buildPropValues(supportedProps);
        
        testUrlString(url, propValues, prefix + TNSNAME + "?" + ADDITIONAL);

        propValues.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, propValues, prefix + TNSNAME);
                
        propValues.remove(JdbcUrl.TOKEN_TNSNAME);
        testMissingParameter(url, propValues);

        testBadUrlString(url, prefix);
    }

    private HashMap<String,String> buildPropValues(List<String> supportedProps) {
        HashMap<String, String> propValues = new HashMap<String,String>();
        for (String prop : ALLPROPS.keySet()) {
            if (supportedProps.contains(prop)) {
                propValues.put(prop, ALLPROPS.get(prop));
            }
        }
        
        return propValues;        
    }
    private static String getDriverName(String key) {
        return NbBundle.getMessage(DriverListUtil.class, key);
    }

    private static String getType(String typeKey) {
        return NbBundle.getMessage(DriverListUtil.class, typeKey);
    }


    private void testNonParsedUrl(JdbcUrl url) throws Exception {
        String urlString = "foo:bar:my.url";
        url.setUrl(urlString);
        assertEquals(url.getUrl(), urlString);
    }
    
    private JdbcUrl checkUrl(String name, String type, String className,
            String template, List<String> supportedTokens, List<String> requiredTokens) throws Exception {
        JdbcUrl url = getJdbcUrl(name, type);
        assertEquals(name, url.getName());
        assertEquals(type, url.getType());
        
        if (type == null) {
            assertEquals(name, url.getDisplayName());
        } else {
            assertEquals(name + " (" + type + ")", url.getDisplayName());
        }
        
        assertEquals(className, url.getClassName());
        assertEquals(template, url.getUrlTemplate());
        
        JdbcUrl other = new JdbcUrl(url.getName(), url.getName(), url.getClassName(),
                url.getType(), url.getUrlTemplate(), url.isParseUrl());
        
        assertEquals(url, other);

        checkSupportedTokens(url, supportedTokens);
        checkRequiredTokens(url, requiredTokens);
        
        return url;
    }

    public void testPostgreSQL() throws Exception {
        ArrayList<String> requiredProps = new ArrayList<String>();
        requiredProps.add(JdbcUrl.TOKEN_DB);
        
        JdbcUrl url = checkUrl(getDriverName("DRIVERNAME_PostgreSQL"), null, "org.postgresql.Driver", 
                "jdbc:postgresql:[//<HOST>[:<PORT>]/]<DB>[?<ADDITIONAL>]",
                STD_SUPPORTED_PROPS, requiredProps);
        
        HashMap<String, String> propValues = buildPropValues(STD_SUPPORTED_PROPS);        
        testUrlString(url, propValues, "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB + "?" + ADDITIONAL);

        propValues.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, propValues, "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_PORT);
        testUrlString(url, propValues, "jdbc:postgresql://" + HOST + "/" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_HOST);
        testUrlString(url, propValues, "jdbc:postgresql:" + DB);
        
        propValues.remove(JdbcUrl.TOKEN_DB);
        testMissingParameter(url, propValues);
        
        testBadUrlString(url, "jdbc:postgresql:");
        testBadUrlString(url, "jdbc:postgresql:///" + DB);
    }
    
    private void testUrlString(JdbcUrl url, Map<String, String> props, String urlString) throws Exception {
        url.clear();
        url.putAll(props);
        assertEquals(urlString, url.getUrl());
        
        url.clear();
        
        url.setUrl(urlString);
        for (String prop : props.keySet()) {
            assertEquals(props.get(prop), url.get(prop));
        }
    }
    
    private void testMissingParameter(JdbcUrl url, HashMap<String, String> props) {
        url.clear();
        url.putAll(props);        
        
        assertEquals("", url.getUrl());
    }

    private void testBadUrlString(JdbcUrl url, String urlString) {
        boolean shouldHaveFailed = false;
        try {
          url.setUrl(urlString);
          shouldHaveFailed = true;
        } catch (Throwable t) {
            if (! (t instanceof MalformedURLException)) {
                fail("Should have thrown a MalformedURLException");
            }
        }
        
        if (shouldHaveFailed) {
            fail("Should have thrown an exception");
        }
    }


    private void checkSupportedTokens(JdbcUrl url, List<String> expected) {       
        for (String token : ALLPROPS.keySet()) {
            if (expected.contains(token)) {
                assertTrue(url.supportsToken(token));
            } else {
                assertFalse(url.supportsToken(token));
                assertFalse(url.requiresToken(token));
            }
        }
    }

    private void checkRequiredTokens(JdbcUrl url, List<String> expected) { 
        for (String token : ALLPROPS.keySet()) {
            if (expected.contains(token)) {
                assertTrue(url.requiresToken(token));
                assertTrue(url.supportsToken(token));
            } else {
                assertFalse(url.requiresToken(token));
            }
        }
    }
    
}
