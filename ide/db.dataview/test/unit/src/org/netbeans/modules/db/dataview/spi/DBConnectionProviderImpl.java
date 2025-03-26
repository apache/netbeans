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

package org.netbeans.modules.db.dataview.spi;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.dataview.util.DbUtil;
import org.netbeans.modules.db.dataview.util.TestCaseContext;
import org.openide.util.Exceptions;

/**
 *
 * @author jawed
 */
//@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.db.dataview.spi.DBConnectionProvider.class)
public class DBConnectionProviderImpl implements DBConnectionProvider{

    /** Creates a new instance of DBConnectionProviderImpl */
    public DBConnectionProviderImpl() {
    }
    
    public Connection getConnection(Properties connProps) throws Exception {
        try {
            String driver = connProps.getProperty("driver");
            String username = connProps.getProperty("user");
            String password = connProps.getProperty("password");
            String url = connProps.getProperty("url");
            return DbUtil.createConnection(driver, url, username, password);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void closeConnection(Connection con) {
        try {
            if(con != null) {
                con.close();
            }
        } catch (SQLException ex) {
            //ignore
        } finally {
            if(con != null) {
                try {
                    con.close();
                } catch(SQLException e) {
                    //ignore
                }
            }
        }
    }

    @Override
    public Connection getConnection(DatabaseConnection dbConn) {
        try {
            String driver = dbConn.getDriverClass();
            String username = dbConn.getUser();
            String password = dbConn.getPassword();
            String url = dbConn.getDatabaseURL();
            Properties prop = new Properties();
            prop.setProperty("user", username);
            prop.setProperty("password", password);

            TestCaseContext context = DbUtil.getContext();
            File[] jars = context.getJars();
            ArrayList<URL> list = new ArrayList<>();
            for (int i = 0; i < jars.length; i++) {
                list.add(jars[i].toURI().toURL());
            }
            URL[] driverURLs = list.toArray(new URL[0]);
            URLClassLoader l = new URLClassLoader(driverURLs);
            Class<?> c = Class.forName(driver, true, l);
            Driver drv = (Driver) c.getDeclaredConstructor().newInstance();
            Connection con = drv.connect(url, prop);
            return con;
        } catch (ReflectiveOperationException | SQLException | MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
