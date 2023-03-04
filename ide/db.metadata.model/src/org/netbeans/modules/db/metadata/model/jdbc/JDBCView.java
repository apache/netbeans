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

package org.netbeans.modules.db.metadata.model.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.metadata.model.MetadataUtilities;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.MetadataException;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.spi.ViewImplementation;

/**
 * This class delegates to an underlying table implementation, as basically
 * a view is a kind of table.  I didn't do this as inheritance because I
 * did not want to hard-code an inheritance relationship into the API.  Who
 * knows, for some implementations, a view is not a table.
 * 
 * @author David Van Couvering
 */
public class JDBCView extends ViewImplementation {
    private static final Logger LOGGER = Logger.getLogger(JDBCView.class.getName());

    private final JDBCSchema jdbcSchema;
    private final String name;
    private Map<String, Column> columns;

    public JDBCView(JDBCSchema jdbcSchema, String name) {
        this.jdbcSchema = jdbcSchema;
        this.name = name;
    }

    @Override
    public String toString() {
        return "JDBCView[name='" + getName() + "']"; // NOI18N
    }

    public final Schema getParent() {
        return jdbcSchema.getSchema();
    }

    public final String getName() {
        return name;
    }

    public final Collection<Column> getColumns() {
        return initColumns().values();
    }

    public final Column getColumn(String name) {
        return MetadataUtilities.find(name, initColumns());
    }

    protected JDBCColumn createJDBCColumn(ResultSet rs) throws SQLException {
        int ordinalPosition = rs.getInt("ORDINAL_POSITION");
        return new JDBCColumn(this.getView(), ordinalPosition, JDBCValue.createTableColumnValue(rs, this.getView()));
    }

    protected void createColumns() {
        Map<String, Column> newColumns = new LinkedHashMap<String, Column>();
        try {
            ResultSet rs = jdbcSchema.getJDBCCatalog().getJDBCMetadata().getDmd().getColumns(jdbcSchema.getJDBCCatalog().getName(), jdbcSchema.getName(), name, "%"); // NOI18N
            if (rs != null) {
                try {
                    while (rs.next()) {
                        Column column = createJDBCColumn(rs).getColumn();
                        newColumns.put(column.getName(), column);
                        LOGGER.log(Level.FINE, "Created column {0}", column);
                    }
                } finally {
                    rs.close();
                }
            }
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        columns = Collections.unmodifiableMap(newColumns);
    }

    private Map<String, Column> initColumns() {
        if (columns != null) {
            return columns;
        }
        LOGGER.log(Level.FINE, "Initializing columns in {0}", this);
        createColumns();
        return columns;
    }

    @Override
    public final void refresh() {
        columns = null;
    }

}
