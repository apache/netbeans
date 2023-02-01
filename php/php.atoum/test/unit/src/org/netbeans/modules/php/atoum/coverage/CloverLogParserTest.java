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

package org.netbeans.modules.php.atoum.coverage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.spi.testing.coverage.Coverage;
import org.netbeans.modules.php.spi.testing.coverage.FileMetrics;


public class CloverLogParserTest extends NbTestCase {

    public CloverLogParserTest(String name) {
        super(name);
    }

    public void testParse() throws Exception {
        Reader reader = new BufferedReader(new FileReader(getCoverageLog("coverage.xml")));

        List<Coverage.File> files = CloverLogParser.parse(reader);

        assertNotNull(files);
        assertTrue(!files.isEmpty());
        assertEquals(2, files.size());

        Coverage.File file = files.get(0);
        assertEquals(new File(getDataDir(), "testdata/Calculator.php").getAbsolutePath(), file.getPath());
        FileMetrics metrics = file.getMetrics();
        assertNotNull(metrics);
        assertEquals(45, metrics.getLineCount());
        assertEquals(6, metrics.getStatements());
        assertEquals(6, metrics.getCoveredStatements());
        assertNotNull(file.getLines());
        assertEquals(11, file.getLines().size());
        Coverage.Line line = file.getLines().get(0);
        assertEquals(50, line.getNumber());
        assertEquals(1, line.getHitCount());
        line = file.getLines().get(1);
        assertEquals(51, line.getNumber());
        assertEquals(0, line.getHitCount());
        line = file.getLines().get(10);
        assertEquals(66, line.getNumber());
        assertEquals(0, line.getHitCount());

        file = files.get(1);
        assertEquals(new File(getDataDir(), "testdata/Calculator2.php").getAbsolutePath(), file.getPath());
        metrics = file.getMetrics();
        assertNotNull(metrics);
        assertEquals(42, metrics.getLineCount());
        assertEquals(6, metrics.getStatements());
        assertEquals(4, metrics.getCoveredStatements());
        assertNotNull(file.getLines());
        assertEquals(11, file.getLines().size());
        line = file.getLines().get(0);
        assertEquals(50, line.getNumber());
        assertEquals(0, line.getHitCount());
        line = file.getLines().get(2);
        assertEquals(54, line.getNumber());
        assertEquals(1, line.getHitCount());
        line = file.getLines().get(10);
        assertEquals(66, line.getNumber());
        assertEquals(0, line.getHitCount());
    }

    private File getCoverageLog(String filename) throws Exception {
        File coverageLog = new File(getDataDir(), filename);
        assertTrue(coverageLog.isFile());
        fixContent(coverageLog);
        return coverageLog;
    }

    private void fixContent(File file) throws Exception {
        Path path = file.toPath();
        Charset charset = StandardCharsets.UTF_8;
        String content = new String(Files.readAllBytes(path), charset);
        content = content.replace("%WORKDIR%", getDataDir().getAbsolutePath());
        content = content.replace("%SEP%", File.separator);
        Files.write(path, content.getBytes(charset));
    }

}
