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
package org.netbeans.modules.php.blade.syntax.antlr4.v10;

import java.io.File;
import java.util.Date;
import static junit.framework.TestCase.assertTrue;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 *
 * @author bhaidu
 */
public class BladeAntrlParserTest extends BladeAntlrParserTestBase {

    public BladeAntrlParserTest(String testName) {
        super(testName);
    }

    public void testSmokeFile_01() throws Exception {
        testFile("smoke/test_01");
    }
    
    private void testFile(String filename) throws Exception {
        Date start = new Date();
        File testFile =  new File(getDataDir(), "testfiles/parser/" + filename + ".blade.php");
        String content = org.netbeans.modules.php.blade.editor.BladeUtils.getFileContent(testFile);
        CharStream stream = CharStreams.fromString(content);
        BladeAntlrLexer lexer = new BladeAntlrLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BladeAntlrParser parser = new BladeAntlrParser(tokens);
        parser.setBuildParseTree(false);
        parser.file();
        Date end = new Date();
        long time = end.getTime() - start.getTime();
        long fileSize = testFile.getTotalSpace() / 1024;
        String output = String.format(
                "Parsing of file(%s: %sKB) takes: %sms",
                testFile.getName(),
                fileSize,
                time
        );
//
        System.out.println(output);
        assertTrue(time < 200);
    }
}
