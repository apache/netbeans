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
package org.netbeans.modules.languages.yaml;

import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 *
 * @author tor
 */
public class YamlLexerTest extends YamlTestBase {

    public YamlLexerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    public void testInput() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/input.yaml.txt",
                YamlTokenId.language());
    }

    public void testInput2() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/input2.yaml.txt",
                YamlTokenId.language());
    }

    public void testInput3() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/input3.yaml.txt",
                YamlTokenId.language());
    }
    
    public void testInput4() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/input4.yaml.txt",
                YamlTokenId.language());
    }

     public void testIssue246124() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/issue246124.yaml",
                YamlTokenId.language());
    }
}
