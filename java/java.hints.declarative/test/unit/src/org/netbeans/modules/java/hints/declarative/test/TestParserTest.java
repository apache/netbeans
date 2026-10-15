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

package org.netbeans.modules.java.hints.declarative.test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.SourceVersion;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.java.hints.declarative.test.TestParser.TestCase;

/**
 *
 * @author lahvac
 */
public class TestParserTest {

    public TestParserTest() {}

    @Test
    public void testParse1() {
        String code = "%%TestCase name\ncode\n%%=>\nfixed1\n%%=>\nfixed2\n";

        code += code;
        
        List<String> golden = Arrays.asList("name:code\n:[fixed1\n, fixed2\n]:0:16:[26, 38]",
                                            "name:code\n:[fixed1\n, fixed2\n]:45:61:[71, 83]");
        List<String> testCases = new LinkedList<>();

        for (TestCase ts : TestParser.parse(code)) {
            testCases.add(ts.toString());
        }

        assertEquals(golden, testCases);
    }

    @Test
    public void testNoResults() {
        String code = "%%TestCase name\ncode\n";

        code += code;

        List<String> golden = Arrays.asList("name:code\n:[]:0:16:[]",
                                            "name:code\n:[]:21:37:[]");
        List<String> testCases = new LinkedList<>();

        for (TestCase ts : TestParser.parse(code)) {
            testCases.add(ts.toString());
        }

        assertEquals(golden, testCases);
    }

    @Test
    public void testSourceLevelOptionOld() {
        String code = "%%TestCase name source-level=1.4\ncode\n%%=>\nfixed1\n%%=>\nfixed2\n";
        TestCase[] tests = TestParser.parse(code);
        
        assertEquals(1, tests.length);

        assertEquals(SourceVersion.RELEASE_4, tests[0].getSourceLevel());
        assertEquals("name:code\n:[fixed1\n, fixed2\n]:0:33:[43, 55]", tests[0].toString());
    }

    @Test
    public void testSourceLevelOptionNew() {
        String code = "%%TestCase name source-level=21\ncode\n%%=>\nfixed1\n%%=>\nfixed2\n";
        TestCase[] tests = TestParser.parse(code);
        
        assertEquals(1, tests.length);

        assertEquals(SourceVersion.RELEASE_21, tests[0].getSourceLevel());
        assertEquals("name:code\n:[fixed1\n, fixed2\n]:0:32:[42, 54]", tests[0].toString());
    }
}