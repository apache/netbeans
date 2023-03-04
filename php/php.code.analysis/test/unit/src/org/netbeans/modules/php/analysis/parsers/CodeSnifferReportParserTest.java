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
package org.netbeans.modules.php.analysis.parsers;

import java.io.File;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.analysis.results.Result;

public class CodeSnifferReportParserTest extends NbTestCase {

    public CodeSnifferReportParserTest(String name) {
        super(name);
    }

    public void testParse() throws Exception {
        List<Result> results = CodeSnifferReportParser.parse(getLogFile("phpcs-log.xml"));
        assertNotNull(results);

        assertEquals(9, results.size());
        Result result = results.get(0);
        assertEquals("/home/gapon/NetBeansProjects/_important/TodoList/config/Config.php", result.getFilePath());
        assertEquals(48, result.getLine());
        assertEquals(7, result.getColumn());
        assertEquals("PSR1: Classes > ClassDeclaration > MissingNamespace", result.getCategory());
        assertEquals("Each class must be in a namespace of at least one level (a top-level vendor name)", result.getDescription());


        result = results.get(6);
        assertEquals("/home/gapon/NetBeansProjects/_important/TodoList/exception/MyPresenter.php", result.getFilePath());
        assertEquals(1, result.getLine());
        assertEquals(1, result.getColumn());
        assertEquals("PSR1: Files > SideEffects > FoundWithSymbols", result.getCategory());
        assertEquals("A file should declare new symbols (classes, functions, constants, etc.) and cause no other side effects, "
                + "or it should execute logic with side effects, but should not do both. The first symbol is defined on line 15 "
                + "and the first side effect is on line 19.", result.getDescription());
    }

    public void testParseLogWithSummary1() throws Exception {
        List<Result> results = CodeSnifferReportParser.parse(getLogFile("phpcs-log-with-summary-1.xml"));
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    public void testParseLogWithSummary2() throws Exception {
        List<Result> results = CodeSnifferReportParser.parse(getLogFile("phpcs-log-with-summary-2.xml"));
        assertNotNull(results);
        assertEquals(1, results.size());

        Result result = results.get(0);
        assertEquals("/home/gapon/Download/PHP_CodeSniffer-master/CodeSniffer/DocGenerators/Generator.php", result.getFilePath());
        assertEquals(31, result.getLine());
        assertEquals(56, result.getColumn());
        assertEquals("PEAR: Classes > ClassDeclaration > OpenBraceNewLine", result.getCategory());
        assertEquals("Opening brace of a class must be on the line after the definition", result.getDescription());
    }

    private File getLogFile(String name) throws Exception {
        assertNotNull(name);
        File xmlLog = new File(getDataDir(), name);
        assertTrue(xmlLog.isFile());
        return xmlLog;
    }

}
