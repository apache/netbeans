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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.openide.util.Exceptions;

/**
 *
 * Exports the given data to the target file in the provided CSV format.
 *
 * @author Periklis Ntanasis <pntanasis@gmail.com>
 */
abstract class CSVCommonsDataExporter extends DataExporter {

    public CSVCommonsDataExporter(String[] suffixes, String suffixDescription) {
        super(suffixes, suffixDescription);
    }

    protected void exportData(String[] headers, Object[][] contents, File file, CSVFormat format) {
        int rows = contents.length;

        try ( CSVPrinter printer = new CSVPrinter(
                new FileWriter(file), format.withHeader(headers))) {
            for (int i = 0; i < rows; i++) {
                printer.printRecord(contents[i]);
            }
            printer.flush();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
