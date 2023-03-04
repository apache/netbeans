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
        Map<String, Map> props = connector.getDatabaseSpecification().getProperties();
        Map cprops = connector.getDatabaseSpecification().getCommandProperties(
                Specification.CREATE_TABLE);
        Map<String, String> bindmap = (Map) cprops.get("Binding");                     // NOI18N
        String tname = bindmap.get(TableColumn.COLUMN);
        column.setFormat((String)props.get(tname).get( "Format"));                     //NOI18N
    }
}
