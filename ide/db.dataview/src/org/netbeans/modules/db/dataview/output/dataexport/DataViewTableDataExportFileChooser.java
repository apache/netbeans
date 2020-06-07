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
import java.util.Arrays;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.progress.BaseProgressUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author Periklis Ntanasis <pntanasis@gmail.com>
 */
@NbBundle.Messages({
    "LBL_FILE_CHOOSER=Export Table Data",
    "LBL_OVEWRITE_DIALOG=Confirm Data Export",
    "MSG_OVEWRITE_DIALOG=File already exists.\nDo you want to overwrite it?",
    "MSG_EXPORT_DATA=Export Data..."
})
public class DataViewTableDataExportFileChooser {

    private static final List<DataExporter> EXPORTERS = Arrays.asList(
            CSVDataExporter.INSTANCE,
            TSVDataExporter.INSTANCE,
            XLSXDataExporter.INSTANCE
    );

    public static void extractAsFile(final JTable table) {
        final JFileChooser fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed(false);
        EXPORTERS.forEach(exporter -> fc.addChoosableFileFilter(exporter.getFileFilter()));
        int returnVal = fc.showDialog(null, Bundle.LBL_FILE_CHOOSER());
        switch (returnVal) {
            case JFileChooser.APPROVE_OPTION:
                FileFilter filter = fc.getFileFilter();
                DataExporter selectedExporter = EXPORTERS.stream()
                        .filter(exporter -> exporter.getFileFilter() == filter)
                        .findAny().orElseThrow(() -> new AssertionError("No matching file exporter filter found."));
                final File file = checkFileExtension(fc.getSelectedFile(), selectedExporter);
                if (checkFile(file)) {
                    final String[] columnNames = DataExportUtils.getColumnNames(table);
                    final Object[][] content = DataExportUtils.getTableContents(table);
                    BaseProgressUtils.showProgressDialogAndRun(
                            () -> selectedExporter.exportData(
                                    columnNames,
                                    content,
                                    file),
                            Bundle.MSG_EXPORT_DATA());
                }
                break;
        }
    }

    private static boolean checkFile(File file) {
        if (file.exists()) {
            int a = JOptionPane.showConfirmDialog(
                    null,
                    Bundle.LBL_OVEWRITE_DIALOG(),
                    Bundle.MSG_OVEWRITE_DIALOG(),
                    JOptionPane.YES_NO_OPTION);
            return a == JOptionPane.YES_OPTION;
        }
        return true;
    }

    private static File checkFileExtension(File file, DataExporter exporter) {
        if (!exporter.handlesFileFormat(file)) {
            return new File(file.getAbsolutePath() + "." + exporter.getDefaultFileExtension());
        }
        return file;
    }

}
