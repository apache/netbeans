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

package org.netbeans.modules.javascript.karma.coverage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.clientproject.api.jstesting.Coverage;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class CloverLogParserTest extends NbTestCase {

    private File sourceDir;


    public CloverLogParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        sourceDir = new File(getWorkDir(), "testdata");
        assertTrue(sourceDir.mkdirs());
    }

    @Override
    protected void tearDown() throws Exception {
        clearWorkDir();
    }

    public void testParseLog() throws Exception {
        ensureTestData();

        Reader reader = new BufferedReader(new FileReader(getCoverageLog("clover.xml")));

        List<Coverage.File> files = CloverLogParser.parse(reader, sourceDir);

        assertNotNull(files);
        assertTrue(!files.isEmpty());
        assertEquals(5, files.size());

        Coverage.File file = files.get(0);
        assertEquals(new File(sourceDir, "app/js/app.js").getAbsolutePath(), file.getPath());
        Coverage.FileMetrics metrics = file.getMetrics();
        assertNotNull(metrics);
        assertEquals(0, metrics.getLineCount());
        assertEquals(4, metrics.getStatements());
        assertEquals(1, metrics.getCoveredStatements());
        assertNotNull(file.getLines());
        assertEquals(4, file.getLines().size());
        Coverage.Line line = file.getLines().get(0);
        assertEquals(5, line.getNumber());
        assertEquals(1, line.getHitCount());
        line = file.getLines().get(1);
        assertEquals(13, line.getNumber());
        assertEquals(0, line.getHitCount());
        line = file.getLines().get(2);
        assertEquals(14, line.getNumber());
        assertEquals(0, line.getHitCount());
        line = file.getLines().get(3);
        assertEquals(15, line.getNumber());
        assertEquals(0, line.getHitCount());

        file = files.get(4);
        assertEquals(new File(sourceDir, "app/js/services.js").getAbsolutePath(), file.getPath());
        metrics = file.getMetrics();
        assertNotNull(metrics);
        assertEquals(0, metrics.getLineCount());
        assertEquals(1, metrics.getStatements());
        assertEquals(1, metrics.getCoveredStatements());
        assertNotNull(file.getLines());
        assertEquals(1, file.getLines().size());
        line = file.getLines().get(0);
        assertEquals(8, line.getNumber());
        assertEquals(1, line.getHitCount());
    }

    private void ensureTestData() throws IOException {
        FileObject fo = FileUtil.toFileObject(sourceDir);
        assertNotNull(FileUtil.createData(fo, "app/js/app.js"));
        assertNotNull(FileUtil.createData(fo, "app/js/controllers.js"));
        assertNotNull(FileUtil.createData(fo, "app/js/directives.js"));
        assertNotNull(FileUtil.createData(fo, "app/js/filters.js"));
        assertNotNull(FileUtil.createData(fo, "app/js/services.js"));
    }

    private File getCoverageLog(String filename) throws Exception {
        File coverageLog = new File(getDataDir(), filename);
        assertTrue(coverageLog.isFile());
        return coverageLog;
    }

}
