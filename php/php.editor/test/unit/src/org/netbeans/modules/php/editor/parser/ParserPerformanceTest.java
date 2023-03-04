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

package org.netbeans.modules.php.editor.parser;

import java.io.File;
import java.io.StringReader;
import java.util.Date;
import java_cup.runtime.Symbol;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.editor.csl.TestUtilities;

/**
 *
 * @author Petr Pisl
 */
public class ParserPerformanceTest extends NbTestCase {

    public ParserPerformanceTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBigFile_01() throws Exception {
        testBigFile("testfiles/parser/performance/Mpdf.php"); // 1.01MB
    }

    public void testBigFile_02() throws Exception {
        testBigFile("testfiles/actions/testImportData/libs/nette.min.php"); // 537KB
    }

    private void testBigFile(String filePath) throws Exception {
        File testFile = new File(getDataDir(), filePath);
        assertTrue(testFile.exists());
        String testSource = TestUtilities.copyFileToString(testFile);
        ASTPHP5Scanner scanner = new ASTPHP5Scanner(new StringReader(testSource));
        ASTPHP5Parser parser = new ASTPHP5Parser(scanner);
        Date start = new Date();
        Symbol root = parser.parse();
        Date end = new Date();
        long time = end.getTime() - start.getTime();
        long fileSize = testFile.length() / 1024;
        String output = String.format(
                "Parsing of big file(%s: %sKB) takes: %sms",
                testFile.getName(),
                fileSize,
                time
        );
        System.out.println(output);
        assertTrue(time < 2500);
    }
}
