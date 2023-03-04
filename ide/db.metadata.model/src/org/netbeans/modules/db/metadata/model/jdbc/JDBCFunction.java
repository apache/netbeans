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

import java.sql.DatabaseMetaData;
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
import org.netbeans.modules.db.metadata.model.api.Parameter;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Value;
import org.netbeans.modules.db.metadata.model.spi.FunctionImplementation;

/**
 *
 * @author David Van Couvering
 */
public class JDBCFunction extends FunctionImplementation {

    private static final Logger LOGGER = Logger.getLogger(JDBCFunction.class.getName());
    private final JDBCSchema jdbcSchema;
    private final String name;
    private Map<String, Column> columns;
    private Map<String, Parameter> parameters;
    private Value returnValue;

    public JDBCFunction(JDBCSchema jdbcSchema, String name) {
        this.jdbcSchema = jdbcSchema;
        this.name = name;
    }

    @Override
    public final Schema getParent() {
        return jdbcSchema.getSchema();
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final Collection<Column> getColumns() {
        return initColumns().values();
    }

    @Override
    public final Column getColumn(String name) {
        return MetadataUtilities.find(name, initColumns());
    }

    @Override
    public final void refresh() {
        columns = null;
        parameters = null;
    }

    @Override
    public Collection<Parameter> getParameters() {
        return initParameters().values();
    }

    @Override
    public Parameter getParameter(String name) {
        return initParameters().get(name);
    }

    @Override
    public Value getReturnValue() {
        return initReturnValue();
    }

    @Override
    public String toString() {
        return "JDBCFunction[name='" + name + "']"; // NOI18N
    }

    protected JDBCColumn createJDBCColumn(int position, ResultSet rs) throws SQLException {
        return new JDBCColumn(this.getFunction(), position, JDBCValue.createFunctionValue(rs, this.getFunction()));
    }

    protected JDBCParameter createJDBCParameter(int position, ResultSet rs) throws SQLException {
        Parameter.Direction direction = JDBCUtils.getFunctionDirection(rs.getShort("COLUMN_TYPE"));
        return new JDBCParameter(this.getFunction(), JDBCValue.createFunctionValue(rs, this.getFunction()), direction, position);
    }

    protected JDBCValue createJDBCValue(ResultSet rs) throws SQLException {
        return JDBCValue.createFunctionValue(rs, this.getFunction());
    }

    protected void createProcedureInfo() {
        LOGGER.log(Level.FINE, "Initializing procedure info in " + this);

        Map<String, Column> newColumns = new LinkedHashMap<String, Column>();
        Map<String, Parameter> newParams = new LinkedHashMap<String, Parameter>();
        int resultCount = 0;
        int paramCount = 0;

        try {
            ResultSet rs = jdbcSchema.getJDBCCatalog().getJDBCMetadata().getDmd().getFunctionColumns(jdbcSchema.getJDBCCatalog().getName(), jdbcSchema.getName(), name, "%"); // NOI18N
            try {
                while (rs.next()) {
                    short columnType = rs.getShort("COLUMN_TYPE");
                    switch (columnType) {
                        case DatabaseMetaData.functionColumnResult:
                            addColumn(++resultCount, rs, newColumns);
                            break;
                        case DatabaseMetaData.functionColumnIn:
                        case DatabaseMetaData.functionColumnInOut:
                        case DatabaseMetaData.functionColumnOut:
                        case DatabaseMetaData.functionColumnUnknown:
                            addParameter(++paramCount, rs, newParams);
                            break;
                        case DatabaseMetaData.functionReturn:
                            setReturnValue(rs);
                            break;
                        default:
                            LOGGER.log(Level.INFO, "Encountered unexpected column type " + columnType + " when retrieving metadadta for procedure " + name);
                    }
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        columns = Collections.unmodifiableMap(newColumns);
        parameters = Collections.unmodifiableMap(newParams);
    }

    private void addColumn(int position, ResultSet rs, Map<String, Column> newColumns) throws SQLException {
        Column column = createJDBCColumn(position, rs).getColumn();
        newColumns.put(column.getName(), column);
        LOGGER.log(Level.FINE, "Created column {0}", column);
    }

    private void addParameter(int position, ResultSet rs, Map<String, Parameter> newParams) throws SQLException {
        Parameter param = createJDBCParameter(position, rs).getParameter();
        newParams.put(param.getName(), param);
        LOGGER.log(Level.FINE, "Created parameter {0}", param);
    }

    private void setReturnValue(ResultSet rs) throws SQLException {
        returnValue = createJDBCValue(rs).getValue();
        LOGGER.log(Level.FINE, "Created return value {0}", returnValue);
    }

    private Map<String, Column> initColumns() {
        if (columns != null) {
            return columns;
        }
        createProcedureInfo();
        return columns;
    }

    private Map<String, Parameter> initParameters() {
        if (parameters != null) {
            return parameters;
        }
        createProcedureInfo();
        return parameters;
    }

    private Value initReturnValue() {
        if (returnValue != null) {
            return returnValue;
        }
        createProcedureInfo();
        return returnValue;
    }
}
