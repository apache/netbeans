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
import java.util.HashSet;
import java.util.Set;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.csv.CSVFormat;
import org.openide.util.NbBundle;

/**
 *
 * Exports the given data to the target file in Standard Comma Separated Value
 * format, as for RFC4180 but allowing empty lines.
 *
 * @author Periklis Ntanasis <pntanasis@gmail.com>
 */
@NbBundle.Messages("CSV_DESCRIPTION=.csv - Comma Separated Values")
public enum CSVDataExporter implements DataExporter, CSVCommonsDataExporter {
    INSTANCE;

    private final Set<String> SUFFIXES = new HashSet<>();
    private final String SUFFIX_DESCRIPTION = Bundle.CSV_DESCRIPTION();
    private final FileFilter FILE_FILTER;

    private CSVDataExporter() {
        SUFFIXES.add("csv");
        FILE_FILTER = new FileNameExtensionFilter(SUFFIX_DESCRIPTION, SUFFIXES.toArray(new String[SUFFIXES.size()]));
    }

    @Override
    public FileFilter getFileFilter() {
        return FILE_FILTER;
    }

    @Override
    public void exportData(String[] headers, Object[][] contents, File file) {
        exportData(headers, contents, file, CSVFormat.DEFAULT);
    }

    @Override
    public boolean handlesFileFormat(File file) {
        return SUFFIXES.contains(DataExportUtils.getExtension(file.getName()));
    }

    @Override
    public String getDefaultFileExtension() {
        return SUFFIXES.iterator().next();
    }

}
