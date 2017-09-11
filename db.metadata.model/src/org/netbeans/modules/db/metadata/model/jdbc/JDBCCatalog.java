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
import org.netbeans.modules.db.metadata.model.api.MetadataException;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.spi.CatalogImplementation;

/**
 *
 * @author Andrei Badea
 */
public class JDBCCatalog extends CatalogImplementation {

    private static final Logger LOGGER = Logger.getLogger(JDBCCatalog.class.getName());

    protected final JDBCMetadata jdbcMetadata;
    protected final String name;
    protected final boolean _default;
    protected final String defaultSchemaName;

    protected Schema defaultSchema;
    protected Schema syntheticSchema;
    protected Map<String, Schema> schemas;

    public JDBCCatalog(JDBCMetadata jdbcMetadata, String name, boolean _default, String defaultSchemaName) {
        // defaultSchemaName only makes sense for the default catalog.
        assert defaultSchemaName == null || _default;
        this.jdbcMetadata = jdbcMetadata;
        this.name = name;
        this._default = _default;
        this.defaultSchemaName = defaultSchemaName;
        LOGGER.log(Level.FINE, "Create JDBCCatalog(jdbcMetadata={0}, name={1}, _default={2}, defaultSchemaName={3})",
                new Object[] {jdbcMetadata, name, _default, defaultSchemaName});
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
    public final Schema getSyntheticSchema() {
        initSchemas();
        return syntheticSchema;
    }

    @Override
    public final Collection<Schema> getSchemas() {
        return initSchemas().values();
    }

    @Override
    public final Schema getSchema(String name) {
        return MetadataUtilities.find(name, initSchemas());
    }

    @Override
    public final void refresh() {
        schemas = null;
    }

    @Override
    public String toString() {
        return "JDBCCatalog[name='" + name + "',default=" + _default + "]"; // NOI18N
    }

    public final Schema getDefaultSchema() {
        initSchemas();
        return defaultSchema;
    }

    protected JDBCSchema createJDBCSchema(String name, boolean _default, boolean synthetic) {
        return new JDBCSchema(this, name, _default, synthetic);
    }

    protected void createSchemas() {
        Map<String, Schema> newSchemas = new LinkedHashMap<String, Schema>();
        try {
            if (jdbcMetadata.getDmd().supportsSchemasInTableDefinitions()) {
                ResultSet rs = jdbcMetadata.getDmd().getSchemas();
                if (rs != null) {
                    int columnCount = rs.getMetaData().getColumnCount();
                    if (columnCount < 2) {
                        LOGGER.fine("DatabaseMetaData.getSchemas() not JDBC 3.0-compliant");
                    }
                    boolean supportsCatalog = jdbcMetadata.getDmd().supportsCatalogsInTableDefinitions();
                    try {
                        while (rs.next()) {
                            String schemaName = MetadataUtilities.trimmed(rs.getString("TABLE_SCHEM")); // NOI18N
                            // Workaround for pre-JDBC 3.0 drivers, where DatabaseMetaData.getSchemas()
                            // only returns a TABLE_SCHEM column.
                            String catalogName = columnCount > 1 && supportsCatalog ? MetadataUtilities.trimmed(rs.getString("TABLE_CATALOG")) : name; // NOI18N
                            LOGGER.log(Level.FINE, "Read schema ''{0}'' in catalog ''{1}''", new Object[] { schemaName, catalogName });
                            LOGGER.log(Level.FINEST, "MetadataUtilities.equals(catalogName=''{0}'', name=''{1}'') returns {2}",
                                    new Object[] {catalogName, name, MetadataUtilities.equals(catalogName, name)});
                            if (MetadataUtilities.equals(catalogName, name)) {
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
                        }
                    } finally {
                        if (rs != null) {
                            rs.close();
                        }
                    }
                } else {
                    LOGGER.info(this + " returns null from jdbcMetadata.getDmd().getSchemas().");
                }
            }
            if (newSchemas.isEmpty() && !jdbcMetadata.getDmd().supportsSchemasInTableDefinitions()) {
                syntheticSchema = createJDBCSchema(null, _default, true).getSchema();
                if (_default) {
                    defaultSchema = syntheticSchema;
                }
                LOGGER.log(Level.FINE, "Created synthetic schema {0}", syntheticSchema);
            }
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        schemas = Collections.unmodifiableMap(newSchemas);
    }

    private Map<String, Schema> initSchemas() {
        if (schemas != null) {
            return schemas;
        }
        LOGGER.log(Level.FINE, "Initializing schemas in {0}", this);
        createSchemas();
        return schemas;
    }

    public final JDBCMetadata getJDBCMetadata() {
        return jdbcMetadata;
    }
}
