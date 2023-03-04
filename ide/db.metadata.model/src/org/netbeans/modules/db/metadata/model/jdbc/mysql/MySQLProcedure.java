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

package org.netbeans.modules.db.metadata.model.jdbc.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.netbeans.modules.db.metadata.model.api.MetadataElement;
import org.netbeans.modules.db.metadata.model.api.Nullable;
import org.netbeans.modules.db.metadata.model.api.Parameter.Direction;
import org.netbeans.modules.db.metadata.model.api.SQLType;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCParameter;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCProcedure;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCSchema;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCUtils;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCValue;

/**
 *
 * @author David Van Couvering
 */
public class MySQLProcedure extends JDBCProcedure {
    public MySQLProcedure(JDBCSchema jdbcSchema, String name) {
        super(jdbcSchema, name);
    }

    @Override
    protected JDBCParameter createJDBCParameter(int position, ResultSet rs) throws SQLException {
        Direction direction = JDBCUtils.getProcedureDirection(rs.getShort("COLUMN_TYPE"));
        return new JDBCParameter(this.getProcedure(), createValue(rs, this.getProcedure()), direction, position);
    }

    @Override
    protected JDBCValue createJDBCValue(ResultSet rs) throws SQLException {
        return createValue(rs, this.getProcedure());
    }

    @Override
    public String toString() {
        return "MySQLProcedure[name=" + getName() + "]";
    }

    /**
     * A "special" version because MySQL returns character lengths in
     * the precision column instead of the length column - sheesh.
     *
     * Logged as a MySQL bug - http://bugs.mysql.com/bug.php?id=41269
     * When this is fixed this workaround will need to be backed out.
     */
    private static JDBCValue createValue(ResultSet rs, MetadataElement parent) throws SQLException {
        String name = rs.getString("COLUMN_NAME");

        int length = 0;
        int precision = 0;

        SQLType type = JDBCUtils.getSQLType(rs.getInt("DATA_TYPE"));
        String typeName = rs.getString("TYPE_NAME");
        if (JDBCUtils.isNumericType(type)) {
            precision = rs.getInt("PRECISION");
        } else {
            length = rs.getInt("PRECISION");
        }
        short scale = rs.getShort("SCALE");
        short radix = rs.getShort("RADIX");
        Nullable nullable = JDBCUtils.getProcedureNullable(rs.getShort("NULLABLE"));

        return new JDBCValue(parent, name, type, typeName, length, precision, radix, scale, nullable);
    }


}
