/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
import org.netbeans.modules.db.metadata.model.api.*;
import org.netbeans.modules.db.metadata.model.spi.SchemaImplementation;

/**
 *
 * @author Andrei Badea
 */
public class JDBCSchema extends SchemaImplementation {

    private static final Logger LOGGER = Logger.getLogger(JDBCSchema.class.getName());

    protected final JDBCCatalog jdbcCatalog;
    protected final String name;
    protected final boolean _default;
    protected final boolean synthetic;

    protected Map<String, Table> tables;
    protected Map<String, View> views;
    protected Map<String, Procedure> procedures;
    protected Map<String, Function> functions;

    public JDBCSchema(JDBCCatalog jdbcCatalog, String name, boolean _default, boolean synthetic) {
        this.jdbcCatalog = jdbcCatalog;
        this.name = name;
        this._default = _default;
        this.synthetic = synthetic;
    }

    @Override
    public final Catalog getParent() {
        return jdbcCatalog.getCatalog();
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final boolean isDefault() {
        return _default;
    }

    @Override
    public final boolean isSynthetic() {
        return synthetic;
    }

    @Override
    public final Collection<Table> getTables() {
        return initTables().values();
    }

    @Override
    public final Table getTable(String name) {
        return MetadataUtilities.find(name, initTables());
    }

    @Override
    public View getView(String name) {
        return MetadataUtilities.find(name, initViews());
    }

    @Override
    public Collection<View> getViews() {
        return initViews().values();
    }

    @Override
    public Procedure getProcedure(String name) {
        return initProcedures().get(name);
    }

    @Override
    public Collection<Procedure> getProcedures() {
        return initProcedures().values();
    }

    @Override
    public Function getFunction(String name) {
        return initFunctions().get(name);
    }

    @Override
    public Collection<Function> getFunctions() {
        return initFunctions().values();
    }

    @Override
    public void refresh() {
        tables = null;
        views = null;
        procedures = null;
    }

    @Override
    public String toString() {
        return "JDBCSchema[name='" + name + "',default=" + _default + ",synthetic=" + synthetic + "]"; // NOI18N
    }

    protected JDBCTable createJDBCTable(String name, boolean system) {
        return new JDBCTable(this, name, system);
    }

    protected JDBCProcedure createJDBCProcedure(String procedureName) {
        return new JDBCProcedure(this, procedureName);
    }

    protected JDBCFunction createJDBCFunction(String functionName) {
        return new JDBCFunction(this, functionName);
    }

    protected JDBCView createJDBCView(String viewName) {
        return new JDBCView(this, viewName);
    }

    protected void createTables() {
        LOGGER.log(Level.FINE, "Initializing tables in {0}", this);
        Map<String, Table> newTables = new LinkedHashMap<String, Table>();
        try {
            ResultSet rs = MetadataUtilities.getTables(jdbcCatalog.getJDBCMetadata().getDmd(),
                    jdbcCatalog.getName(), name, "%", new String[]{"TABLE", "SYSTEM TABLE"}); // NOI18N
            if (rs != null) {
                try {
                    while (rs.next()) {
                        String type = MetadataUtilities.trimmed(rs.getString("TABLE_TYPE")); //NOI18N
                        String tableName = MetadataUtilities.trimmed(rs.getString("TABLE_NAME")); // NOI18N
                        Table table = createJDBCTable(tableName, type.contains("SYSTEM")).getTable(); //NOI18N
                        newTables.put(tableName, table);
                        LOGGER.log(Level.FINE, "Created table {0}", table); //NOI18N
                    }
                } finally {
                    rs.close();
                }
            }
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        tables = Collections.unmodifiableMap(newTables);
    }

    protected void createViews() {
        LOGGER.log(Level.FINE, "Initializing views in {0}", this);
        Map<String, View> newViews = new LinkedHashMap<String, View>();
        try {
            ResultSet rs = MetadataUtilities.getTables(jdbcCatalog.getJDBCMetadata().getDmd(),
                    jdbcCatalog.getName(), name, "%", new String[]{"VIEW"}); // NOI18N
            if (rs != null) {
                try {
                    while (rs.next()) {
                        String viewName = MetadataUtilities.trimmed(rs.getString("TABLE_NAME")); // NOI18N
                        View view = createJDBCView(viewName).getView();
                        newViews.put(viewName, view);
                        LOGGER.log(Level.FINE, "Created view {0}", view); // NOI18N
                    }
                } finally {
                    rs.close();
                }
            }
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        views = Collections.unmodifiableMap(newViews);
    }

    protected void createProcedures() {
        LOGGER.log(Level.FINE, "Initializing procedures in {0}", this);
        Map<String, Procedure> newProcedures = new LinkedHashMap<String, Procedure>();
        try {
            ResultSet rs = MetadataUtilities.getProcedures(jdbcCatalog.getJDBCMetadata().getDmd(),
                    jdbcCatalog.getName(), name, "%"); // NOI18N
            if (rs != null) {
                try {
                    while (rs.next()) {
                        String procedureName = MetadataUtilities.trimmed(rs.getString("PROCEDURE_NAME")); // NOI18N
                        Procedure procedure = createJDBCProcedure(procedureName).getProcedure();
                        newProcedures.put(procedureName, procedure);
                        LOGGER.log(Level.FINE, "Created procedure {0}", procedure); //NOI18N
                    }
                } finally {
                    rs.close();
                }
            }
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        procedures = Collections.unmodifiableMap(newProcedures);
    }

    protected void createFunctions() {
        LOGGER.log(Level.FINE, "Initializing functions in {0}", this); //NOI18N
        Map<String, Function> newProcedures = new LinkedHashMap<String, Function>();
        try {
            ResultSet rs = MetadataUtilities.getFunctions(jdbcCatalog.getJDBCMetadata().getDmd(),
                    jdbcCatalog.getName(), name, "%"); // NOI18N
            if (rs != null) {
                try {
                    while (rs.next()) {
                        String functionName = MetadataUtilities.trimmed(rs.getString("FUNCTION_NAME")); // NOI18N
                        Function function = createJDBCFunction(functionName).getFunction();
                        newProcedures.put(functionName, function);
                        LOGGER.log(Level.FINE, "Created function {0}", function); //NOI18N
                    }
                } finally {
                    rs.close();
                }
            }
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        functions = Collections.unmodifiableMap(newProcedures);
    }

    private Map<String, Table> initTables() {
        if (tables != null) {
            return tables;
        }
        createTables();
        return tables;
    }

    public final JDBCCatalog getJDBCCatalog() {
        return jdbcCatalog;
    }

    private Map<String, View> initViews() {
        if (views != null) {
            return views;
        }
        createViews();
        return views;
    }

    private Map<String, Procedure> initProcedures() {
        if (procedures != null) {
            return procedures;
        }

        createProcedures();
        return procedures;
    }

    private Map<String, Function> initFunctions() {
        if (functions != null) {
            return functions;
        }

        createFunctions();
        return functions;
    }
}
