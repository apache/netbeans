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

package org.netbeans.modules.glassfish.javaee.db;

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;

/**
 *
 * @author Peter Williams
 */
public class DbUtil {

    private static final String __DatabaseVendor = "database-vendor";
    private static final String __DatabaseName = "databaseName";
    private static final String __Url = "URL";
    private static final String __User = "User";
    private static final String __Password = "Password";
    private static final String __NotApplicable = "NA";
    private static final String __IsXA = "isXA";  
    private static final String __IsCPExisting = "is-cp-existing";
    
    private static final String __DerbyDatabaseName = "DatabaseName";
    private static final String __DerbyPortNumber = "PortNumber";
    private static final String __ServerName = "serverName";
    private static final String __InformixHostName = "IfxIFXHOST";
    private static final String __InformixServer = "InformixServer";
    private static final String __DerbyConnAttr = "connectionAttributes";
    
    private static final String __PortNumber = "portNumber";
    private static final String __SID = "SID";
    private static final String __DriverClass = "driverClass";    
    
    
    static final String[] VendorsDBNameProp = {
        "sun_db2", "sun_oracle", "sun_msftsql", "db2", "microsoft_sql", 
        "post-gre-sql", "mysql", "datadirect_sql", "datadirect_db2",
        "datadirect_informix", "datadirect_sybase", "datadirect_oracle",
        "jtds_sql", "jtds_sybase", "informix"
    };
    
    static final String[] Reqd_DBName = {
        "sun_db2", "sun_msftsql", "datadirect_sql", "microsoft_sql", 
        "datadirect_db2", "datadirect_informix", "datadirect_sybase"
    };
    
    public static Map<String, String> normalizePoolMap(Map<String, String> poolValues) {
        String driverClassName = poolValues.get("dsClassName"); //NOI18N
        String resType = poolValues.get("resType"); //NOI18N
        String url = ""; //NOI18N
        String serverName = poolValues.get(__ServerName);
        String portNo = poolValues.get(__DerbyPortNumber);
        String dbName = poolValues.get(__DerbyDatabaseName);
        String dbVal = poolValues.get(__DatabaseName);
        String portVal = poolValues.get(__PortNumber);
        String sid = poolValues.get(__SID);
        String urlValue = poolValues.get(__Url);
        String driverClass = poolValues.get(__DriverClass);
        String derbyConnAttr = poolValues.get(__DerbyConnAttr);
        String password = poolValues.get(__Password);
        String user = poolValues.get(__User);
        
        if (driverClassName != null && driverClassName.indexOf("pointbase") != -1) {
            url = poolValues.get(__DatabaseName);
        }
        // Search for server name key should be case insensitive.
        if (serverName == null) {
            for (String key : poolValues.keySet()) {
                if (__ServerName.equalsIgnoreCase(key)) {
                    serverName = poolValues.get(key);
                    break;
                }
            }
            poolValues.put(__ServerName, serverName);
        }
        if (urlValue == null || urlValue.equals("")) { //NOI18N
            if (driverClassName.indexOf("derby") != -1) {
                if (serverName != null) {
                    url = "jdbc:derby://" + serverName;
                    if (portNo != null && portNo.length() > 0) {
                        url = url + ":" + portNo; //NOI18N
                    }   
                    url = url + "/" + dbName; //NOI18N
                    if(derbyConnAttr != null && (! derbyConnAttr.equals(""))) { //NOI18N
                        url = url + derbyConnAttr;
                    }
                }
            } else {
                if (url == null || url.equals("")) {  //NOI18N
                    String urlPrefix = DriverMaps.getUrlPrefix(driverClassName, resType);
                    // !PW FIXME no access to vendor name yet.
//                    String vName = ResourceConfigurator.getDatabaseVendorName(urlPrefix, null);
                    //
                    String vName = "Unknown";//NOI18N
                    Logger.getLogger("glassfish-javaee").log(Level.WARNING, 
                            "Unable to compute database vendor name for datasource url.");
                    if (serverName != null) {
                        if (vName.equals("sun_oracle")) {    //NOI18N
                            url = urlPrefix + serverName;
                        } else {
                            url = urlPrefix + "//" + serverName; //NOI18N
                        }
                        if (portVal != null && portVal.length() > 0) {
                            url = url + ":" + portVal; //NOI18N
                        }
                    }
                    if (vName.equals("sun_oracle") || vName.equals("datadirect_oracle")) {  //NOI18N
                        url = url + ";SID=" + sid; //NOI18N
                    } else if (Arrays.asList(Reqd_DBName).contains(vName)) {
                        url = url + ";databaseName=" + dbVal; //NOI18N
                    } else if (Arrays.asList(VendorsDBNameProp).contains(vName) || "Unknown".equals(vName)) {//NOI18N
                        url = url + "/" + dbVal; //NOI18N
                    }
                }
            }
        } else {
            url = urlValue;
        }
        
        if (url != null && (!url.equals(""))) { //NOI18N
            if (driverClass == null || driverClass.equals("")) { //NOI18N
                DatabaseConnection databaseConnection = getDatabaseConnection(url);
                if (databaseConnection != null) {
                    driverClass = databaseConnection.getDriverClass();
                } else {
                    //Fix Issue 78212 - NB required driver classname
                    String drivername = DriverMaps.getDriverName(url);
                    if (drivername != null) {
                        driverClass = drivername;
                    } else {
                        driverClass = driverClassName;
                    }
                }
            }
        }

        if (user == null) {
            for(String key : poolValues.keySet()){
                if(__User.equalsIgnoreCase(key)){
                    user = poolValues.get(key);
                    break;
                }
            }
            poolValues.put(__User, user);
        }

        if (password == null) {
            for(String key : poolValues.keySet()){
                if(__Password.equalsIgnoreCase(key)){
                    password = poolValues.get(key);
                    break;
                }
            }
            poolValues.put(__Password, password);
        }
        
        poolValues.put(__Url, url);
        poolValues.put(__DriverClass, driverClass);
        
        return poolValues;
    }
    
    private static DatabaseConnection getDatabaseConnection(String url) {
        DatabaseConnection [] dbConns = ConnectionManager.getDefault().getConnections();
        for(int i = 0; i < dbConns.length; i++) {
            String dbConnUrl = dbConns[i].getDatabaseURL();
            if(dbConnUrl.startsWith(url)) {
                return dbConns[i];
            }
        }
        return null;
    }
    
    public static final boolean notEmpty(String testedString) {
        return (testedString != null) && (testedString.length() > 0);
    }
    
    public static final boolean strEmpty(String testedString) {
        return testedString == null || testedString.length() == 0;
    }
    
    public static final boolean strEquals(String one, String two) {
        boolean result = false;
        
        if(one == null) {
            result = (two == null);
        } else {
            if(two == null) {
                result = false;
            } else {
                result = one.equals(two);
            }
        }
        return result;
    }

    public static final boolean strEquivalent(String one, String two) {
        boolean result = false;
        
        if(strEmpty(one) && strEmpty(two)) {
            result = true;
        } else if(one != null && two != null) {
            result = one.equals(two);
        }
        
        return result;
    }
    
    public static final int strCompareTo(String one, String two) {
        int result;
        
        if(one == null) {
            if(two == null) {
                result = 0;
            } else {
                result = -1;
            }
        } else {
            if(two == null) {
                result = 1;
            } else {
                result = one.compareTo(two);
            }
        }
        
        return result;
    }
    
}
