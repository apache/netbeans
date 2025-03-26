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
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author abadea
 */
public class Catalog {
    
    private final DBMetaDataProvider provider;
    private final String name;
    
    private Map<String, Schema> schemas;
    
    Catalog(DBMetaDataProvider provider, String name) {
        this.provider = provider;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public synchronized Schema[] getSchemas() throws SQLException {
        if (schemas == null) {
            schemas = new TreeMap();

            // (if name == null)
            //      assuming the current catalog when the catalog name is null
            // else
            //      DatabaseMetaData.getSchemas() can not be used to retrieved the
            //      list of schemas in a given catalog, since it (e.g. for the JTDS
            //      driver) only returns the schemas in the current catalog. The 
            //      workaround is to retrieve all tables from all schemas in the given
            //      catalog and obtain a schema list from that. This is not perfect, 
            //      since it will not return the schemas containig neither tables nor views.
            try (ResultSet rs = (name == null) 
                    ? provider.getMetaData().getSchemas() 
                    : provider.getMetaData().getTables(name, "%", "%", new String[] { "TABLE", "VIEW" })) {
                while (rs.next()) {
                    String schemaName = rs.getString("TABLE_SCHEM"); // NOI18N
                    if(schemaName == null) {
                        schemaName = "";//handle null as empty name
                    }
                    Schema schema = new Schema(provider, this, schemaName);
                    schemas.put(schemaName, schema);
                }
            }
        }
        
        return schemas.values().toArray(new Schema[schemas.size()]);
    }
    
    public synchronized Schema getSchema(String name) throws SQLException {
        if (schemas == null) {
            getSchemas();
        }
        
        return schemas.get(name);
    }
    
    @Override
    public String toString() {
        return "Catalog[name='" + name + "']"; // NOI18N
    }
}
