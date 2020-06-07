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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Exports the given data to the target file in Excel Workbook format.
 *
 * @author Periklis Ntanasis <pntanasis@gmail.com>
 */
@NbBundle.Messages("XLSX_DESCRIPTION=.xlsx - Excel Workbook")
public enum XLSXDataExporter implements DataExporter {
    INSTANCE;
    
    private final String DATE_FORMAT = "yyyy-mm-dd";
    private final String TIME_FORMAT = "hh:mm:ss";
    private final String TIMESTAMP_FORMAT = "yyyy-mm-dd hh:mm:ss.000";
    
    private final String APP_VERSION = "Apache NetBeans IDE " + System.getProperty("netbeans.buildnumber");
    
    private final Set<String> SUFFIXES = new HashSet<>();
    private final String SUFFIX_DESCRIPTION = Bundle.XLSX_DESCRIPTION();
    private final FileFilter FILE_FILTER;
    
    private XLSXDataExporter() {
        SUFFIXES.add("xlsx");
        FILE_FILTER = new FileNameExtensionFilter(SUFFIX_DESCRIPTION, SUFFIXES.toArray(new String[SUFFIXES.size()]));
    }
    
    @Override
    public boolean handlesFileFormat(File file) {
        return SUFFIXES.contains(DataExportUtils.getExtension(file.getName()));
    }
    
    @Override
    public FileFilter getFileFilter() {
        return FILE_FILTER;
    }
    
    @Override
    public void exportData(String[] headers, Object[][] contents, File file) {
        int columns = headers.length;
        int rows = contents.length;
        try (OutputStream os = new FileOutputStream(file)) {
            int row = 0;
            Workbook wb = new Workbook(os, APP_VERSION, null);
            Worksheet ws = wb.newWorksheet("Sheet1");
            for (int j = 0; j < columns; j++) {
                ws.value(row, j, headers[j]);
            }
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    Object value = contents[i][j];
                    if (value instanceof Number) {
                        ws.value(i + 1, j, ((Number) value).doubleValue());
                    } else if (value instanceof Time) {
                        ws.value(i + 1, j, (Time) value);
                        ws.style(i + 1, j).format(TIME_FORMAT).set();
                    } else if (value instanceof Timestamp) {
                        ws.value(i + 1, j, (Timestamp) value);
                        ws.style(i + 1, j).format(TIMESTAMP_FORMAT).set();
                    } else if (value instanceof Date) {
                        ws.value(i + 1, j, (Date) value);
                        ws.style(i + 1, j).format(DATE_FORMAT).set();
                    } else if (value instanceof Boolean) {
                        ws.value(i + 1, j, (Boolean) value);
                    } else if (value != null) {
                        ws.value(i + 1, j, value.toString());
                    }
                }
            }
            wb.finish();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public String getDefaultFileExtension() {
        return SUFFIXES.iterator().next();
    }
    
}
