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

package org.netbeans.modules.db.explorer.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.netbeans.lib.ddl.impl.AbstractTableColumn;
import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.impl.TableColumn;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseConnector;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.Table;

public class GrabTableHelper {

    public void execute(final DatabaseConnection databaseConnection,
            final Specification spec,
            final MetadataElementHandle<Table> tableHandle,
            final File file) throws Exception {

        final DatabaseConnector connector = databaseConnection.getConnector();
        final MetadataModel model = databaseConnection.getMetadataModel();

        final Exception[] array = new Exception[1];

        model.runReadAction(
            new Action<Metadata>() {
                public void run(Metadata metaData) {
                    Table table = tableHandle.resolve(metaData);

                    try {
                        CreateTable cmd = spec.createCommandCreateTable(table.getName());

                        Collection<Column> columns = table.getColumns();
                        List<TableColumn> pks = new LinkedList<TableColumn>();
                        for (Column column : columns) {
                            TableColumn col = connector.getColumnSpecification(
                                    table, column);
                            cmd.getColumns().add(col);
                            if (col.getObjectType().equals(
                                    TableColumn.PRIMARY_KEY)) {
                                pks.add(col);
                            }
                        }
                        if (pks.size() > 1) {
                            setPrimaryKeyColumns(pks, connector, cmd, table);
                        }

                        FileOutputStream fstream = new FileOutputStream(file);
                        ObjectOutputStream ostream = new ObjectOutputStream(fstream);
                        cmd.setSpecification(null);
                        ostream.writeObject(cmd);
                        ostream.flush();
                        ostream.close();
                    } catch (Exception e) {
                        array[0] = e;
                    }
                }
            }
        );

        if (array[0] != null) {
            throw array[0];
        }
    }

    /**
     * Set primary key columns. Must be called if the table has a compound
     * primary key only. It sets format of column definition to standard column
     * format (primary key modifiers are suppressed) and adds a primary key
     * constraint "column".
     */
    private static void setPrimaryKeyColumns(List<TableColumn> pks,
            DatabaseConnector connector, CreateTable cmd, Table table)
            throws ClassNotFoundException, IllegalAccessException,
            InstantiationException {

        assert pks.size() > 1;
        Vector<Hashtable<String, String>> colItems =
                new Vector<Hashtable<String, String>>();
        for (AbstractTableColumn pKey : pks) {
            setColumnFormatToStandard(connector, pKey);
            Hashtable<String, String> colItem =
                    new Hashtable<String, String>();
            colItem.put("name", pKey.getColumnName());                  //NOI18N
            colItems.add(colItem);
        }
        TableColumn cmdcol = cmd.createPrimaryKeyConstraint(
                table.getName());
        cmdcol.setTableConstraintColumns(colItems);
        cmdcol.setColumnType(0);
        cmdcol.setColumnSize(0);
        cmdcol.setDecimalSize(0);
        cmdcol.setNullAllowed(true);
    }

    /**
     * Set database column format to the default one. It can be usefull e.g. to
     * suppress primary key column modifier in case the table has a compound
     * primary key.
     */
    private static void setColumnFormatToStandard(DatabaseConnector connector,
            AbstractTableColumn column) {
        Map props = connector.getDatabaseSpecification().getProperties();
        Map cprops = connector.getDatabaseSpecification().getCommandProperties(
                Specification.CREATE_TABLE);
        Map bindmap = (Map) cprops.get("Binding");                     // NOI18N
        String tname = (String) bindmap.get(TableColumn.COLUMN);
        column.setFormat((String) ((Map) props.get(tname)).get(
                "Format"));                                             //NOI18N
    }
}
