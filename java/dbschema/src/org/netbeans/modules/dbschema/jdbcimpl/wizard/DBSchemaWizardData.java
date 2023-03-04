/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.dbschema.jdbcimpl.wizard;

import java.beans.*;
import java.util.LinkedList;
import java.util.Vector;
import org.netbeans.api.db.explorer.DatabaseConnection;

import org.openide.loaders.DataFolder;

import org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider;

public class DBSchemaWizardData {

    private DataFolder destinationPackage;
    private String name;
    private String driver;
    private DatabaseConnection dbconn;
    private boolean existingConn;
    private ConnectionProvider cp;
    private LinkedList tables;
    private LinkedList views;
    private boolean connected;
    private String schema;
    private Vector schemas;
    private boolean all;

    private PropertyChangeSupport propertySupport;

    /** Creates new DBSchemaWizardData */
    public DBSchemaWizardData() {
        schemas = new Vector();
        propertySupport = new PropertyChangeSupport(this);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDestinationPackage(DataFolder destinationPackage) {
        this.destinationPackage = destinationPackage;
    }

    public DataFolder getDestinationPackage() {
        return destinationPackage;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getDriver() {
        return driver;
    }
    
    public void setDatabaseConnection(DatabaseConnection dbconn) {
        this.dbconn = dbconn;
    }
    
    public DatabaseConnection getDatabaseConnection() {
        return dbconn;
    }

    public void setExistingConn(boolean existingConn) {
        this.existingConn = existingConn;
    }

    public boolean isExistingConn() {
        return existingConn;
    }

    public void setConnectionProvider(ConnectionProvider cp) {
        this.cp = cp;
    }

    public ConnectionProvider getConnectionProvider() {
        return cp;
    }

    public void setTables(LinkedList tables) {
        this.tables = tables;
    }

    public LinkedList getTables() {
        return tables;
    }

    public void setViews(LinkedList views) {
        this.views = views;
    }

    public LinkedList getViews() {
        return views;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setSchemas(Vector schemas) {
        this.schemas = schemas;
    }

    public Vector getSchemas() {
        return schemas;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getSchema() {
        return schema;
    }

    public void setAllTables(boolean all) {
        this.all = all;
    }

    public boolean isAllTables() {
        return all;
    }

    //==== property change support needed for schemas ====
    public PropertyChangeSupport getPropertySupport() {
        return propertySupport;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertySupport.addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertySupport.removePropertyChangeListener (l);
    }
}
