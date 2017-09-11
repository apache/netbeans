/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.db.explorer;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.DatabaseProductNotFoundException;
import org.netbeans.lib.ddl.adaptors.DefaultAdaptor;
import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.impl.SpecificationFactory;
import org.netbeans.lib.ddl.impl.TableColumn;
import org.netbeans.modules.db.explorer.node.RootNode;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.Index;
import org.netbeans.modules.db.metadata.model.api.IndexColumn;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;

/**
 *
 * @author Rob Englander
 */
public class DatabaseConnector {
    private final DatabaseConnection databaseConnection;
    private Specification spec;

    // we maintain a lazy cache of driver specs mapped to the catalog name
    private ConcurrentHashMap<String, DriverSpecification> driverSpecCache 
            = new ConcurrentHashMap<>();

    public DatabaseConnector(DatabaseConnection conn) {
        databaseConnection = conn;
    }

    public Specification getDatabaseSpecification() {
        return spec;
    }

    public DriverSpecification getDriverSpecification(String catName) throws DatabaseException {
        DriverSpecification dspec = driverSpecCache.get(catName);
        if (dspec == null) {
            try {
                SpecificationFactory factory = RootNode.instance().getSpecificationFactory();
                dspec = factory.createDriverSpecification(spec.getMetaData().getDriverName().trim());
                if (spec.getMetaData().getDriverName().trim().equals("jConnect (TM) for JDBC (TM)")) //NOI18N
                    //hack for Sybase ASE - I don't guess why spec.getMetaData doesn't work
                    dspec.setMetaData(databaseConnection.getJDBCConnection().getMetaData());
                else
                    dspec.setMetaData(spec.getMetaData());

                dspec.setCatalog(catName);
                dspec.setSchema(databaseConnection.getSchema());
                driverSpecCache.put(catName, dspec);
            } catch (SQLException e) {
                throw new DatabaseException(e.getMessage(), e);
            }

        }

        return dspec;
    }
    
    void finishConnect(String dbsys) throws DatabaseException {
        try {
            SpecificationFactory factory = RootNode.instance().getSpecificationFactory();
            int readOnlyFlag = 0;
            if (dbsys != null) {
                spec = (Specification) factory.createSpecification(databaseConnection, dbsys, databaseConnection.getJDBCConnection());

                readOnlyFlag = 1;
            } else {
                spec = (Specification) factory.createSpecification(databaseConnection, databaseConnection.getJDBCConnection());
            }

            DatabaseMetaData md = spec.getMetaData();
            ((DefaultAdaptor)md).setreadOnly(readOnlyFlag);

            String adaname = "org.netbeans.lib.ddl.adaptors.DefaultAdaptor"; // NOI18N
            if (!spec.getMetaDataAdaptorClassName().equals(adaname)) {
                spec.setMetaDataAdaptorClassName(adaname);
            }

            driverSpecCache.clear();
        } catch (DatabaseProductNotFoundException e) {
            Logger.getLogger(DatabaseConnector.class.getName()).log(Level.FINE, e.getLocalizedMessage(), e);
            finishConnect("GenericDatabaseSystem"); // NOI18N
        } catch (DDLException | SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    /**
     * The method performs a disconnect on the associated DatabaseConnection.  This
     * method gets called by the DatabaseConnection itself, and should not be called
     * directly by any other object.
     *
     * @throws org.netbeans.api.db.explorer.DatabaseException
     */
    public void performDisconnect() throws DatabaseException {
                driverSpecCache.clear();
            }

    public static boolean containsColumn(Collection<Column> columnList, Column column) {
        boolean result = false;

        // this code is currently working around a metadata api bug.  the Column instances
        // should be the same, but they aren't always.  so for now we create handles
        // and determine equivalence from there
        MetadataElementHandle<Column> columnHandle = MetadataElementHandle.create(column);
        for (Column col : columnList) {
            MetadataElementHandle<Column> colHandle = MetadataElementHandle.create(col);
            if (columnHandle.equals(colHandle)) {
                result = true;
                break;
            }
        }

        return result;
    }

    public static boolean containsIndexColumn(Collection<Index> columnList, Column column) {
        boolean result = false;

        // this code is currently working around a metadata api bug.  the Column instances
        // should be the same, but they aren't always.  so for now we create handles
        // and determine equivalence from there
        MetadataElementHandle<Column> columnHandle = MetadataElementHandle.create(column);
        for (Index idx : columnList) {
            Collection<IndexColumn> cols = idx.getColumns();
            for (IndexColumn col : cols) {
                MetadataElementHandle<IndexColumn> colHandle = MetadataElementHandle.create(col);
                if (columnHandle.equals(colHandle)) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    public TableColumn getColumnSpecification(Table table, Column column) throws DatabaseException {
        TableColumn col = null;

        try {
            CreateTable cmd = spec.createCommandCreateTable ("DUMMY"); //NOI18N

            // When the metadata api bug fix is available, we can just ask if the
            // collections contain the column and then eliminate the special methods
            // we're using
            if (containsColumn(table.getPrimaryKey().getColumns(), column)) {
                col = cmd.createPrimaryKeyColumn(column.getName());
            } else if (containsIndexColumn(table.getIndexes(), column)) {
                col = cmd.createUniqueColumn(column.getName());
            } else {
                col = cmd.createColumn (column.getName ());
            }

            Schema schema = table.getParent();
            Catalog catalog = schema.getParent();
            String catName = catalog.getName();
            if (catName == null) {
                catName = schema.getName();
            }

            DriverSpecification drvSpec = this.getDriverSpecification(catName);
            if (!schema.isDefault() && schema.getName() != null
                    && schema.getName().length() > 0) {
                drvSpec.setSchema(schema.getName());
            }
            drvSpec.getColumns(table.getName(), column.getName());
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                boolean ok = rs.next();
                if (ok) {
                    @SuppressWarnings("unchecked")
                    Map<Integer, String> rset = drvSpec.getRow();

                    try {
                        //hack because of MSSQL ODBC problems - see DriverSpecification.getRow() for more info - shouln't be thrown
                        col.setColumnType(Integer.parseInt(rset.get(new Integer(5))));
                        col.setColumnSize(Integer.parseInt(rset.get(new Integer(7))));
                    } catch (NumberFormatException exc) {
                        col.setColumnType(0);
                        col.setColumnSize(0);
                    }

                    col.setNullAllowed((rset.get(new Integer(18))).toUpperCase().equals("YES")); //NOI18N
                    col.setDefaultValue(rset.get(new Integer(13)));
                    rset.clear();
                } else {
                    Logger.getLogger(DatabaseConnector.class.getName()).log(Level.INFO, "Empty ResultSet for {0}.{1} in catalog {2}",
                            new Object[]{table.getName(), column.getName(), catName});
                }

                rs.close();
            }
        } catch (Exception e) {
            throw new DatabaseException(e);
        }

        return col;
    }

    public boolean supportsCommand(String cmd) {
        boolean supported = spec.getCommandProperties(cmd) != null;

        if (supported && spec.getCommandProperties(cmd).containsKey("Supported")) {
            supported = spec.getCommandProperties(cmd).get("Supported").toString().equals("true");
        }

        return supported;
    }

    }
