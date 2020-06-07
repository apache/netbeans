/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.db.dataview.output.dataexport;

import java.sql.SQLException;
import javax.swing.JTable;
import org.netbeans.modules.db.dataview.util.FileBackedClob;
import org.openide.util.Exceptions;

/**
 * Data export utility methods.
 *
 * @author Periklis Ntanasis <pntanasis@gmail.com>
 */
public class DataExportUtils {

    /**
     * Returns the filename file type extension in lower case. The extension is
     * the part after the last dot character (.). Example: for filename
     * "foo.java" it will return "java".
     *
     * @param filename
     * @return The filename extension (part after the last .) in lower case.
     */
    public static String getExtension(String filename) {
        String[] tokens = filename.split("\\.");
        return tokens[tokens.length - 1].toLowerCase();
    }

    /**
     * Returns the column names of a JTable as an array of strings.
     *
     * @param table A JTable.
     * @return String[] populated with the column names.
     */
    public static String[] getColumnNames(JTable table) {
        String[] header = new String[table.getColumnCount()];
        for (int i = 0; i < table.getColumnCount(); i++) {
            header[i] = table.getColumnName(i);
        }
        return header;
    }

    /**
     * Returns the contents of a JTable as a two dimensional Object array.
     *
     * @param table A JTable.
     * @return Object[][] populated with the table contents.
     */
    public static Object[][] getTableContents(JTable table) {
        Object[][] contents = new Object[table.getRowCount()][table.getColumnCount()];
        for (int i = 0; i < table.getRowCount(); i++) {
            for (int j = 0; j < table.getColumnCount(); j++) {
                if (table.getValueAt(i, j) instanceof FileBackedClob) {
                    FileBackedClob lob = (FileBackedClob) table.getValueAt(i, j);
                    try {
                        contents[i][j] = lob.getSubString(1, (int) lob.length());
                    } catch (SQLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    contents[i][j] = table.getValueAt(i, j);
                }
            }
        }
        return contents;
    }

}
