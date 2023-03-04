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
package org.netbeans.modules.php.codeception.coverage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;

public class CodeceptionCoverageLogParserTest extends NbTestCase {

    public CodeceptionCoverageLogParserTest(String name) {
        super(name);
    }

    /**
     * Test of parse method, of class CodeceptionCoverageLogParser.
     */
    @Test
    public void testParseLog() throws Exception {
        Reader reader = new BufferedReader(new FileReader(getCoverageLog("codeception-coverage.xml")));
        CoverageImpl coverage = new CoverageImpl();

        CodeceptionCoverageLogParser.parse(reader, coverage);
        assertEquals(1436766630, coverage.getGenerated());
        assertEquals(3, coverage.getFiles().size());

        // 1st file
        CoverageImpl.FileImpl file = (CoverageImpl.FileImpl) coverage.getFiles().get(0);
        assertEquals(FileUtil.normalizePath("/home/junichi11/NetBeansProjects/codeception/src/FizzBuzz.php"), file.getPath());
        assertEquals(1, file.getClasses().size());

        // class
        CoverageImpl.ClassImpl clazz = file.getClasses().get(0);
        assertEquals("FizzBuzz", clazz.getName());
        assertEquals("App", clazz.getNamespace());

        // metrics
        ClassMetricsImpl classMetrics = clazz.getMetrics();
        assertNotNull(classMetrics);
        assertEquals(3, classMetrics.getMethods());
        assertEquals(3, classMetrics.getCoveredMethods());
        assertEquals(0, classMetrics.getConditionals());
        assertEquals(0, classMetrics.getCoveredConditionals());
        assertEquals(16, classMetrics.getStatements());
        assertEquals(16, classMetrics.getCoveredStatements());
        assertEquals(19, classMetrics.getElements());
        assertEquals(19, classMetrics.getCoveredElements());

        // line
        assertEquals(19, file.getLines().size());
        CoverageImpl.LineImpl line = (CoverageImpl.LineImpl) file.getLines().get(0);
        assertEquals(12, line.getNumber());
        assertEquals("method", line.getType());
        assertEquals("__construct", line.getName());
        assertEquals(1, line.getCrap());
        assertEquals(3, line.getHitCount());

        line = (CoverageImpl.LineImpl) file.getLines().get(1);
        assertEquals(13, line.getNumber());
        assertEquals("stmt", line.getType());
        assertEquals(null, line.getName());
        assertEquals(-1, line.getCrap());
        assertEquals(3, line.getHitCount());

        // file metrics
        FileMetricsImpl fileMetrics = (FileMetricsImpl) file.getMetrics();
        assertNotNull(fileMetrics);
        assertEquals(42, fileMetrics.getLineCount());
        assertEquals(31, fileMetrics.getNcloc());
        assertEquals(1, fileMetrics.getClasses());
        assertEquals(3, fileMetrics.getMethods());
        assertEquals(3, fileMetrics.getCoveredMethods());
        assertEquals(0, fileMetrics.getConditionals());
        assertEquals(0, fileMetrics.getCoveredConditionals());
        assertEquals(16, fileMetrics.getStatements());
        assertEquals(16, fileMetrics.getCoveredStatements());
        assertEquals(19, fileMetrics.getElements());
        assertEquals(19, fileMetrics.getCoveredElements());

        // 2nd file
        file = (CoverageImpl.FileImpl) coverage.getFiles().get(1);
        assertEquals(FileUtil.normalizePath("/home/junichi11/NetBeansProjects/codeception/tests/_support/UnitTester.php"), file.getPath());
        assertEquals(1, file.getClasses().size());

        // class
        clazz = file.getClasses().get(0);
        assertEquals("UnitTester", clazz.getName());
        assertEquals("global", clazz.getNamespace());

        // metrics
        classMetrics = clazz.getMetrics();
        assertNotNull(classMetrics);
        assertEquals(0, classMetrics.getMethods());
        assertEquals(0, classMetrics.getCoveredMethods());
        assertEquals(0, classMetrics.getConditionals());
        assertEquals(0, classMetrics.getCoveredConditionals());
        assertEquals(0, classMetrics.getStatements());
        assertEquals(0, classMetrics.getCoveredStatements());
        assertEquals(0, classMetrics.getElements());
        assertEquals(0, classMetrics.getCoveredElements());

        // line
        assertEquals(0, file.getLines().size());

        // file metrics
        fileMetrics = (FileMetricsImpl) file.getMetrics();
        assertNotNull(fileMetrics);
        assertEquals(26, fileMetrics.getLineCount());
        assertEquals(8, fileMetrics.getNcloc());
        assertEquals(1, fileMetrics.getClasses());
        assertEquals(0, fileMetrics.getMethods());
        assertEquals(0, fileMetrics.getCoveredMethods());
        assertEquals(0, fileMetrics.getConditionals());
        assertEquals(0, fileMetrics.getCoveredConditionals());
        assertEquals(0, fileMetrics.getStatements());
        assertEquals(0, fileMetrics.getCoveredStatements());
        assertEquals(0, fileMetrics.getElements());
        assertEquals(0, fileMetrics.getCoveredElements());

        // 3rd file
        file = (CoverageImpl.FileImpl) coverage.getFiles().get(2);
        assertEquals(FileUtil.normalizePath("/home/junichi11/NetBeansProjects/codeception/tests/_support/_generated/UnitTesterActions.php"), file.getPath());
        assertEquals(1, file.getClasses().size());

        // class
        clazz = file.getClasses().get(0);
        assertEquals("UnitTesterActions", clazz.getName());
        assertEquals("_generated", clazz.getNamespace());

        // metrics
        classMetrics = clazz.getMetrics();
        assertNotNull(classMetrics);
        assertEquals(23, classMetrics.getMethods());
        assertEquals(0, classMetrics.getCoveredMethods());
        assertEquals(0, classMetrics.getConditionals());
        assertEquals(0, classMetrics.getCoveredConditionals());
        assertEquals(23, classMetrics.getStatements());
        assertEquals(0, classMetrics.getCoveredStatements());
        assertEquals(46, classMetrics.getElements());
        assertEquals(0, classMetrics.getCoveredElements());

        // line
        assertEquals(46, file.getLines().size());
        line = (CoverageImpl.LineImpl) file.getLines().get(0);
        assertEquals(31, line.getNumber());
        assertEquals("method", line.getType());
        assertEquals("assertEquals", line.getName());
        assertEquals(2, line.getCrap());
        assertEquals(0, line.getHitCount());

        // file metrics
        fileMetrics = (FileMetricsImpl) file.getMetrics();
        assertNotNull(fileMetrics);
        assertEquals(348, fileMetrics.getLineCount());
        assertEquals(121, fileMetrics.getNcloc());
        assertEquals(1, fileMetrics.getClasses());
        assertEquals(23, fileMetrics.getMethods());
        assertEquals(0, fileMetrics.getCoveredMethods());
        assertEquals(0, fileMetrics.getConditionals());
        assertEquals(0, fileMetrics.getCoveredConditionals());
        assertEquals(23, fileMetrics.getStatements());
        assertEquals(0, fileMetrics.getCoveredStatements());
        assertEquals(46, fileMetrics.getElements());
        assertEquals(0, fileMetrics.getCoveredElements());
    }

    private File getCoverageLog(String filename) throws Exception {
        File coverageLog = new File(getDataDir(), filename);
        assertTrue(coverageLog.isFile());
        return coverageLog;
    }

}
