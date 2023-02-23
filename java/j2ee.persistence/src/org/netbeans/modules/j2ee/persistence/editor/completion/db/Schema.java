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

package org.netbeans.modules.j2ee.persistence.editor.completion.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.dbschema.DBException;
import org.netbeans.modules.dbschema.DBIdentifier;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.dbschema.TableElement;
import org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider;
import org.netbeans.modules.dbschema.jdbcimpl.SchemaElementImpl;
import org.openide.util.Exceptions;

/**
 * 
 * @author Andrei Badea
 */
public class Schema {
    
    private DBMetaDataProvider provider;
    private Catalog catalog;
    private String name;
    private Set<String> tableNames;
    
    private ConnectionProvider cp;
    private SchemaElementImpl schemaElementImpl;
    private SchemaElement schemaElement;
    
    // XXX views
    
    Schema(DBMetaDataProvider provider, Catalog catalog, String name) {
        this.provider = provider;
        this.catalog = catalog;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public synchronized String[] getTableNames() throws SQLException {
        if (tableNames == null) {
            tableNames = getTableNamesByType("TABLE"); // NOI18N
        }
        
        return (String[])tableNames.toArray(new String[0]);
    }
    
    public TableElement getTable(String tableName) throws SQLException {
        SchemaElement schemaElement;
        synchronized (this) {
            schemaElement = this.schemaElement;
        }
        if (schemaElement == null) {
            cp = new ConnectionProvider(provider.getConnection(), provider.getDriverClass());
            cp.setSchema(name);
            schemaElementImpl = new SchemaElementImpl(cp);
            try {
                schemaElementImpl.setName(DBIdentifier.create("foo")); // XXX set a proper name
            } catch (DBException e) {
                Exceptions.printStackTrace(e);
            }
            schemaElement = new SchemaElement(schemaElementImpl);
            synchronized (this) {
                this.schemaElement = schemaElement;
            }
        }
        
        DBIdentifier tableId = DBIdentifier.create(tableName);
        TableElement tableElement = schemaElement.getTable(tableId);
        if (tableElement == null) {
            LinkedList tableList = new LinkedList<>();
            tableList.add(tableName);
            LinkedList viewList = new LinkedList<>();
            schemaElementImpl.initTables(cp, tableList, viewList, false);
            
            tableElement = schemaElement.getTable(tableId);
        }
        
        return tableElement;
    }
    
    public synchronized void refresh() {
        schemaElement = null;
        tableNames = null;
    }
    
    private Set<String> getTableNamesByType(String type) throws SQLException {
        Set<String> result = new TreeSet<>();

        try (ResultSet rs = provider.getMetaData().getTables(catalog.getName(), name, "%", new String[] { type })) { // NOI18N
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME"); // NOI18N
                result.add(tableName);
            }
        }

        return result;
    }
    
    @Override
    public String toString() {
        return "Schema[catalog=" + catalog + ",name='" + name + "']"; // NOI18N
    }
}
