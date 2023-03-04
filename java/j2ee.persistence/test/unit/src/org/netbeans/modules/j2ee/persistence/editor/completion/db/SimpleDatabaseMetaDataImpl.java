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

import java.sql.ResultSet;
import org.netbeans.modules.db.test.jdbcstub.JDBCStubUtil;
import org.netbeans.test.stub.api.StubDelegate;

/**
 * A simple implementation of DatabaseMetaData which just return the properties
 * gived in the constructor or set using setters.
 *
 * @author Andrei Badea
 */
public class SimpleDatabaseMetaDataImpl extends StubDelegate {
    
    private String[] catalogNames;
    private String[] schemaNames;
    private String[][] tableNamesBySchema;

    public SimpleDatabaseMetaDataImpl(String[] catalogNames) {
        this.catalogNames = catalogNames;
    }
    
    public SimpleDatabaseMetaDataImpl(String[] catalogNames, String[] schemaNames, String[][] tableNamesBySchema) {
        this.catalogNames = catalogNames;
        this.schemaNames = schemaNames;
        this.tableNamesBySchema = tableNamesBySchema;
    }

    public ResultSet getCatalogs() {
        return JDBCStubUtil.catalogsResultSet(catalogNames);
    }
    
    public ResultSet getSchemas() {
        return JDBCStubUtil.schemasResultSet(schemaNames);
    }
    
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) {
        return JDBCStubUtil.tablesResultSet(tableNamesBySchema);
    }

    public void setCatalogs(String[] catalogNames) {
        this.catalogNames = catalogNames;
    }
    
    public void setTables(String[][] tableNamesBySchema) {
        this.tableNamesBySchema = tableNamesBySchema;
    }
}
