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
import org.netbeans.modules.db.metadata.model.api.Parameter.Direction;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Value;
import org.netbeans.modules.db.metadata.model.spi.ProcedureImplementation;

/**
 *
 * @author David Van Couvering
 */
public class JDBCProcedure extends ProcedureImplementation {

    private static final Logger LOGGER = Logger.getLogger(JDBCProcedure.class.getName());

    private final JDBCSchema jdbcSchema;
    private final String name;

    private Map<String, Column> columns;
    private Map<String, Parameter> parameters;
    private Value returnValue;

    public JDBCProcedure(JDBCSchema jdbcSchema, String name) {
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
        return "JDBCProcedure[name='" + name + "']"; // NOI18N
    }

    protected JDBCColumn createJDBCColumn(int position, ResultSet rs) throws SQLException {
        return new JDBCColumn(this.getProcedure(), position, JDBCValue.createProcedureValue(rs, this.getProcedure()));
    }

    protected JDBCParameter createJDBCParameter(int position, ResultSet rs) throws SQLException {
        Direction direction = JDBCUtils.getProcedureDirection(rs.getShort("COLUMN_TYPE")); //NOI18N
        return new JDBCParameter(this.getProcedure(), JDBCValue.createProcedureValue(rs, this.getProcedure()), direction, position);
    }

    protected JDBCValue createJDBCValue(ResultSet rs) throws SQLException {
        return JDBCValue.createProcedureValue(rs, this.getProcedure());
    }

    protected void createProcedureInfo() {
        LOGGER.log(Level.FINE, "Initializing procedure info in " + this);
        
        Map<String, Column> newColumns = new LinkedHashMap<String, Column>();
        Map<String, Parameter> newParams = new LinkedHashMap<String, Parameter>();
        int resultCount = 0;
        int paramCount = 0;

        try {
            ResultSet rs = jdbcSchema.getJDBCCatalog().getJDBCMetadata().getDmd().getProcedureColumns(jdbcSchema.getJDBCCatalog().getName(), jdbcSchema.getName(), name, "%"); // NOI18N
            try {
                while (rs.next()) {
                    short columnType = rs.getShort("COLUMN_TYPE");
                    switch (columnType) {
                        case DatabaseMetaData.procedureColumnResult:
                            addColumn(++resultCount, rs, newColumns);
                            break;
                        case DatabaseMetaData.procedureColumnIn:
                        case DatabaseMetaData.procedureColumnInOut:
                        case DatabaseMetaData.procedureColumnOut:
                        case DatabaseMetaData.procedureColumnUnknown:
                            addParameter(++paramCount, rs, newParams);
                            break;
                        case DatabaseMetaData.procedureColumnReturn:
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

    private void addColumn(int position, ResultSet rs, Map<String,Column> newColumns) throws SQLException {
        Column column = createJDBCColumn(position, rs).getColumn();
        newColumns.put(column.getName(), column);
        LOGGER.log(Level.FINE, "Created column {0}", column);
    }

    private void addParameter(int position, ResultSet rs, Map<String,Parameter> newParams) throws SQLException {
        Parameter  param = createJDBCParameter(position, rs).getParameter();
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
