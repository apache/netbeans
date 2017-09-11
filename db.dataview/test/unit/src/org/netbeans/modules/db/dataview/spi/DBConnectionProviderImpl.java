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
            ArrayList<URL> list = new java.util.ArrayList<URL>();
            for (int i = 0; i < jars.length; i++) {
                list.add(jars[i].toURI().toURL());
            }
            URL[] driverURLs = list.toArray(new URL[0]);
            URLClassLoader l = new URLClassLoader(driverURLs);
            Class<?> c = Class.forName(driver, true, l);
            Driver drv = (Driver) c.newInstance();
            Connection con = drv.connect(url, prop);
            return con;
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
