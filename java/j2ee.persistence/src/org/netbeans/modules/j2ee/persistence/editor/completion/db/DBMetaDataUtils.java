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

package org.netbeans.modules.j2ee.persistence.editor.completion.db;

import java.sql.SQLException;
import org.netbeans.modules.dbschema.TableElement;

/**
 *
 * @author abadea
 */
public class DBMetaDataUtils {
    
    private DBMetaDataUtils() {
        assert false;
    }
    
    public static Schema getSchema(DBMetaDataProvider provider, String catalogName, String schemaName) throws SQLException {
        Catalog catalog = provider.getCatalog(catalogName);
        if (catalog != null) {
            return catalog.getSchema(schemaName);
        } 
        return null;
        
    }
    
    public static TableElement getTable(DBMetaDataProvider provider, String catalogName, String schemaName, String tableName) throws SQLException {
        Schema schema = getSchema(provider, catalogName, schemaName);
        if (schema != null) {
            return schema.getTable(tableName);
        }
        return null;
    }
}
