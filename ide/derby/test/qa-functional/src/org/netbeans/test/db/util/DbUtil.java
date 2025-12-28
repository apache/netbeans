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

package org.netbeans.test.db.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import org.netbeans.modules.derby.DbURLClassLoader;
import org.netbeans.modules.derby.DerbyOptions;
import org.openide.util.Exceptions;

/**
 *
 * @author luke
 */
public class DbUtil {
    public static final String DRIVER_CLASS_NAME = "org.apache.derby.jdbc.ClientDriver";
    
    public static Connection createDerbyConnection(String dbURL) {
        // Derby Installation folder
        String location = DerbyOptions.getDefault().getLocation();
        File clientJar = new File(location, "lib/derbyclient.jar");
        Connection con = null;
        try {
            System.out.println("> Creating Derby connection using: "+clientJar.toURI().toURL());
            URL[] driverURLs = new URL[]{clientJar.toURI().toURL()};
            DbURLClassLoader loader = new DbURLClassLoader(driverURLs);
            Driver driver = (Driver) Class.forName(DRIVER_CLASS_NAME, true, loader).getDeclaredConstructor().newInstance();
            con = driver.connect(dbURL, null);
        } catch (MalformedURLException ex) {
            Exceptions.attachMessage(ex, "Cannot convert to URL: "+clientJar);
            Exceptions.printStackTrace(ex);
        } catch (SQLException ex) {
            Exceptions.attachMessage(ex, "Cannot conect to: "+dbURL);
            Exceptions.printStackTrace(ex);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
            Exceptions.attachMessage(ex, "Cannot instantiate: "+DRIVER_CLASS_NAME+" from: "+clientJar);
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.attachMessage(ex, "Cannot instantiate: "+DRIVER_CLASS_NAME+" from: "+clientJar);
            Exceptions.printStackTrace(ex);
        } catch (ClassNotFoundException ex) {
            Exceptions.attachMessage(ex, "Cannot obtain: "+DRIVER_CLASS_NAME+" from: "+clientJar);
            Exceptions.printStackTrace(ex);
        }
        return con;
    }
    
}
