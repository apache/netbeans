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
package org.netbeans.modules.db.dataview.output.dataexport;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import org.netbeans.modules.db.dataview.output.DataViewTableUIModel;
import org.netbeans.modules.db.dataview.util.LobHelper;

/**
 * Data export utility methods.
 *
 * @author Periklis Ntanasis <pntanasis@gmail.com>
 */
public class DataExportUtils {

    /**
     * Returns the column names of a DataViewTableUIModel as an array of
     * strings.
     *
     * @param model DataViewTableUIModel model.
     * @return String[] populated with the column names.
     */
    public static String[] getColumnNames(final DataViewTableUIModel model) {
        String[] header = new String[model.getColumnCount()];
        for (int i = 0; i < model.getColumnCount(); i++) {
            header[i] = model.getColumnName(i);
        }
        return header;
    }

    /**
     * Returns the printable contents of a DataViewTableUIModel as a two
     * dimensional Object array.
     *
     * @param model DataViewTableUIModel model.
     * @return Object[][] populated with the table contents.
     */
    public static Object[][] getTableContents(final DataViewTableUIModel model) {
        Object[][] contents = new Object[model.getRowCount()][model.getColumnCount()];
        for (int i = 0; i < model.getRowCount(); i++) {
            for (int j = 0; j < model.getColumnCount(); j++) {
                Object value = model.getValueAt(i, j);
                Class c = model.getColumnClass(j);
                if (value != null && c == Blob.class) {
                    value = LobHelper.blobToString((Blob) value);
                } else if (value != null && c == Clob.class) {
                    Clob lob = (Clob) value;
                    try {
                        value = lob.getSubString(1, (int) lob.length());
                    } catch (SQLException ex) {
                        value = LobHelper.clobToString(lob);
                    }
                }
                contents[i][j] = value;
            }
        }
        return contents;
    }

}
