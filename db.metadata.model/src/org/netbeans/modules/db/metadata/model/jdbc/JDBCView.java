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
