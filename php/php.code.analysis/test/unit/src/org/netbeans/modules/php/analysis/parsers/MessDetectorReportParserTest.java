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

public class MessDetectorReportParserTest extends NbTestCase {

    public MessDetectorReportParserTest(String name) {
        super(name);
    }

    public void testParse() throws Exception {
        List<Result> results = MessDetectorReportParser.parse(getLogFile("phpmd-log.xml"));
        assertNotNull(results);

        assertEquals(8, results.size());
        Result result = results.get(0);
        assertEquals("/home/gapon/NetBeansProjects/TodoList/model/Todo.php", result.getFilePath());
        assertEquals(59, result.getLine());
        assertEquals("Naming Rules: ShortVariable", result.getCategory());
        assertEquals("Avoid variables with short names like $id. Configured minimum length is 3.", result.getDescription());


        result = results.get(7);
        assertEquals("/home/gapon/NetBeansProjects/TodoList/web/index.php", result.getFilePath());
        assertEquals(150, result.getLine());
        assertEquals("Design Rules: ExitExpression", result.getCategory());
        assertEquals("The method runPage() contains an exit expression.", result.getDescription());
    }

    private File getLogFile(String name) throws Exception {
        assertNotNull(name);
        File xmlLog = new File(getDataDir(), name);
        assertTrue(xmlLog.isFile());
        return xmlLog;
    }

}
