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
package org.netbeans.modules.php.blade.editor.parser;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.blade.editor.BladeTestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author bogdan
 */
public class ParserPerformanceTest extends BladeTestBase {

    public ParserPerformanceTest(String testName) {
        super(testName);
    }
    
    public void testFile_01() throws Exception {
        testFile("testfiles/parser/performance/perf_test_01.blade.php"); // 1.01MB
    }

    private void testFile(String filePath) throws Exception {
        FileObject fo = getTestFile(filePath);
        Source testSource = getTestSource(fo);

        Date start = new Date();
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override
            void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                assertNotNull(r);
                assertTrue(r instanceof ParserResult);

                ParserResult pr = (ParserResult) r;
                List<? extends org.netbeans.modules.csl.api.Error> diagnostics = pr.getDiagnostics();
                String annotatedSource = annotateErrors(diagnostics);
                assertDescriptionMatches(filePath, annotatedSource, false, ".errors");
            }
        });
        Date end = new Date();
        
        long time = end.getTime() - start.getTime();
        long fileSize = testSource.getFileObject().getSize() / 1024;
        String output = String.format(
                "Parsing of file(%s: %sKB) takes: %sms",
                testSource.getFileObject().getName(),
                fileSize,
                time
        );
//
        System.out.println(output);
        assertTrue("Parsing time should be below 650", time < 650);
    }
}
