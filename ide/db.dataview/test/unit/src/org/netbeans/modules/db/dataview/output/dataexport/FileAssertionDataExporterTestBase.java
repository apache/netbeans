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
import static org.netbeans.junit.NbTestCase.assertFile;

/**
 * DataExporter test class. Extends DataExporterTest and adds default test that
 * compares the created file in test with a provided golden file. The test
 * passes only if the generated file exactly matches with the provided golden
 * file.
 *
 * @author Periklis Ntanasis <pntanasis@gmail.com>
 */
public class FileAssertionDataExporterTestBase extends AbstractDataExporterTestBase {

    public FileAssertionDataExporterTestBase(String name, DataExporter exporter,
            String defaultFileExtension, String... fileExtensions) {
        super(name, exporter, defaultFileExtension, fileExtensions);
    }

    public void testFileCreation() throws IOException {
        File file = new File(getWorkDir(), TEST_FILE);

        EXPORTER.exportData(headers, contents, file);

        assertFile(file, getGoldenFile());
    }

}
