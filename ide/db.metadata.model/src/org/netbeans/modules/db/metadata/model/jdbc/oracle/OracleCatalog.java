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

package org.netbeans.modules.db.metadata.model.jdbc.oracle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.metadata.model.MetadataUtilities;
import org.netbeans.modules.db.metadata.model.api.MetadataException;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCCatalog;

/**
 *
 * @author Andrei Badea
 */
public class OracleCatalog extends JDBCCatalog {

    private static final Logger LOGGER = Logger.getLogger(OracleCatalog.class.getName());

    public OracleCatalog(OracleMetadata metadata, String name, boolean _default, String defaultSchemaName) {
        super(metadata, name, _default, defaultSchemaName);
    }

    @Override
    public String toString() {
        return "OracleCatalog[name='" + getName() + "']"; // NOI18N
    }

    @Override
    protected OracleSchema createJDBCSchema(String name, boolean _default, boolean synthetic) {
        return new OracleSchema(this, name, _default, synthetic);
    }

    @Override
    protected void createSchemas() {
        Map<String, Schema> newSchemas = new LinkedHashMap<String, Schema>();
        try {
            ResultSet rs = getJDBCMetadata().getDmd().getSchemas();
            try {
                while (rs.next()) {
                    String schemaName = rs.getString("TABLE_SCHEM"); // NOI18N
                    // #140376: Oracle JDBC driver doesn't return a TABLE_CATALOG column
                    // in DatabaseMetaData.getSchemas().
                    LOGGER.log(Level.FINE, "Read schema ''{0}''", schemaName);
                    if (defaultSchemaName != null && MetadataUtilities.equals(schemaName, defaultSchemaName)) {
                        defaultSchema = createJDBCSchema(defaultSchemaName, true, false).getSchema();
                        newSchemas.put(defaultSchema.getName(), defaultSchema);
                        LOGGER.log(Level.FINE, "Created default schema {0}", defaultSchema);
                    } else {
                        Schema schema = createJDBCSchema(schemaName, false, false).getSchema();
                        newSchemas.put(schemaName, schema);
                        LOGGER.log(Level.FINE, "Created schema {0}", schema);
                    }
                }
            } finally {
                rs.close();
            }
            // Schemas always supported, so no need to try to create a synthetic schema.
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        schemas = Collections.unmodifiableMap(newSchemas);
    }
}
