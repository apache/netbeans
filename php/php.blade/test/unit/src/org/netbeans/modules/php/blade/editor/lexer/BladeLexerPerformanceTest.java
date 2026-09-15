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
package org.netbeans.modules.php.blade.editor.lexer;

import java.io.File;
import java.util.Date;
import org.netbeans.modules.php.blade.editor.BladeTestBase;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.php.blade.editor.BladeLanguage;
import org.netbeans.modules.php.blade.editor.BladeUtils;

/**
 *
 * @author bogdan
 */
public class BladeLexerPerformanceTest extends BladeTestBase {

    public BladeLexerPerformanceTest(String testName) {
        super(testName);
    }

    public void testFile_01() throws Exception {
        testFile("perf_test_01");
    }

    private void testFile(String filename) throws Exception {
        Date start = new Date();
        File testFile =  new File(getDataDir(), "testfiles/lexer/blade/performance/" + filename + ".blade.php");
        String content = BladeUtils.getFileContent(testFile);
        BladeLanguage lang = new BladeLanguage();
        Language<BladeTokenId> language = lang.getLexerLanguage();
        TokenHierarchy.create(content, language);
        Date end = new Date();
        long time = end.getTime() - start.getTime();
        long fileSize = testFile.getTotalSpace() / 1024;
        String output = String.format(
                "Lexer tokenization of file(%s: %sKB) takes: %sms",
                testFile.getName(),
                fileSize,
                time
        );
//
        System.out.println(output);
        assertTrue("Lexer tokenization time should be below 200", time < 200);
    }
}
