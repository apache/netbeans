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
package org.netbeans.modules.hibernate.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.hibernate.service.spi.Stoppable;
//import org.hibernate.connection.ConnectionProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

/**
 * Provides custom JDBC Connection for Hibernate
 * 
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class CustomJDBCConnectionProvider implements ConnectionProvider, org.hibernate.service.spi.Configurable, Stoppable {

    private Connection connection = null;
    private Properties connectionProperties = null;
    
    private static final Logger logger = Logger.getLogger(CustomJDBCConnectionProvider.class.getName());
    
    public CustomJDBCConnectionProvider() {
        
    }
    

    @Override
    public void configure(Map map) {
        this.connectionProperties = new Properties();
        connectionProperties.putAll(map);
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection != null && (!connection.isClosed())) {
            logger.info("Connection already established.. returing");
            return connection;
        } else {
            try {
                String driverClassName = connectionProperties.getProperty("hibernate.connection.driver_class"); //NOI18N

                String driverURL = connectionProperties.getProperty("hibernate.connection.url"); //NOI18N

                String username = connectionProperties.getProperty("hibernate.connection.username"); //NOI18N

                String password = connectionProperties.getProperty("hibernate.connection.password"); //NOI18N

                //Hibernate allows abbrivated properties
                if (driverClassName == null) {
                    driverClassName = connectionProperties.getProperty("connection.driver_class"); //NOI18N

                }
                if (driverURL == null) {
                    driverURL = connectionProperties.getProperty("connection.url"); //NOI18N

                }
                if (username == null) {
                    username = connectionProperties.getProperty("connection.username"); //NOI18N

                }
                if (password == null) {
                    password = connectionProperties.getProperty("connection.password"); //NOI18N

                }

                // Sometimes the username can be empty
                // See Issue 159417 for details
                if (username == null) {
                    username = "";
                }

                // Some Database (such as HSQLDB) alow empty password.
                if(password == null) {
                    password = "";
                }

                // load the driver
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Class clazz = classLoader.loadClass(driverClassName);

                java.sql.Driver driver = (java.sql.Driver) clazz.newInstance();

                logger.info("Loaded JDBC driver ");
                // Establish the connection
                java.util.Properties info = new java.util.Properties();
                info.setProperty("user", username);
                info.setProperty("password", password);
                connection = driver.connect(driverURL, info);
                logger.info("Got connection.. returning");
                info = null;
            } catch (ClassNotFoundException e) {
                logger.log(Level.INFO, "DB Driver class not found during connection creation.", e);
                NotifyDescriptor.Exception ne = new NotifyDescriptor.Exception(e);
                DialogDisplayer.getDefault().notifyLater(ne);
            } catch (InstantiationException e) {
                logger.log(Level.INFO, "Cannot instantiate driver class.", e);
                NotifyDescriptor.Exception ne = new NotifyDescriptor.Exception(e);
                DialogDisplayer.getDefault().notifyLater(ne);
            } catch (IllegalAccessException e) {
                logger.log(Level.INFO, "Illegal access during connection creation.", e);
                NotifyDescriptor.Exception ne = new NotifyDescriptor.Exception(e);
                DialogDisplayer.getDefault().notifyLater(ne);
            } catch (SQLException e) {
                logger.log(Level.INFO, "DB connection error.", e);
                NotifyDescriptor.Exception ne = new NotifyDescriptor.Exception(e);
                DialogDisplayer.getDefault().notifyLater(ne);
            }
        }
        return connection;
    }

    @Override
    public void closeConnection(Connection arg0) throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    public void close() throws HibernateException {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return true;
    }

    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
        return ConnectionProvider.class.equals(unwrapType) ||
                CustomJDBCConnectionProvider.class.isAssignableFrom(unwrapType);
   }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        if (ConnectionProvider.class.equals(unwrapType) ||
                CustomJDBCConnectionProvider.class.isAssignableFrom(unwrapType)) {
            return (T) this;
        } else {
            throw new UnknownUnwrapTypeException( unwrapType );
        }
  }

    @Override
    public void stop() {
        if(connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                
            }
            connection = null;
        }
    }
}
