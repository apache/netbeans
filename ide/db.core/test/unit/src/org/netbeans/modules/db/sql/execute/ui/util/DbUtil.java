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

package org.netbeans.modules.db.sql.execute.ui.util;



import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.*;
import org.netbeans.junit.Manager;

/**
 *
 * @author luke
 */


public class DbUtil {
    
    public static  String DRIVER_CLASS_NAME="driver_class_name";
    public static String URL="url";
    public static String USER="user";
    public static String PASSWORD="password";
    
    public static Connection createConnection(Properties p,File[] f) throws Exception{
        String driver_name=p.getProperty(DRIVER_CLASS_NAME);
        String url=p.getProperty(URL);
        String user=p.getProperty(USER);
        String passwd=p.getProperty(PASSWORD);
        ArrayList list=new java.util.ArrayList();
        for(int i=0;i<f.length;i++){
            list.add(f[i].toURI().toURL());
        }
        URL[] driverURLs=(URL[])list.toArray(new URL[0]);
        URLClassLoader l = new URLClassLoader(driverURLs);
        Class c = Class.forName(driver_name, true, l);
        Driver driver=(Driver)c.getDeclaredConstructor().newInstance();
        Connection con=driver.connect(url,p);
        return con;
    }
    
    
    
    
}

