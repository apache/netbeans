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
import java.io.IOException;
import static junit.framework.TestCase.assertEquals;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Periklis Ntanasis <pntanasis@gmail.com>
 */
public class XLSXDataExporterTest extends AbstractDataExporterTestBase {

    public XLSXDataExporterTest(String name) {
        super(name, new XLSXDataExporter(), "xlsx");
    }

    /**
     * Compare generated file to golden file by content. It does not perform
     * exact match check because it will fail.
     *
     * @throws IOException
     * @throws InvalidFormatException
     */
    public void testFileCreation() throws IOException, InvalidFormatException {
        File file = new File(getWorkDir(), "test.xlsx");

        EXPORTER.exportData(headers, contents, file);

        try ( XSSFWorkbook wb1 = new XSSFWorkbook(file)) {
            try ( XSSFWorkbook wb2 = new XSSFWorkbook(getGoldenFile())) {
                String workbookA = new XSSFExcelExtractor(wb1).getText();
                String workbookB = new XSSFExcelExtractor(wb2).getText();
                assertEquals("XLSX Content Mismatch", workbookB, workbookA);
            }
        }
    }
}
