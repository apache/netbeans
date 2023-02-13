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
package org.netbeans.modules.rust.grammar.antlr4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.rust.grammar.RustTokenID;

/**
 *
 * @author antonio
 */
public class RustLexerWithFilesTest extends NbTestCase {

    public RustLexerWithFilesTest(String testName) {
        super(testName);
    }

    private File getTestFile(String name) throws FileNotFoundException {
        File file = new File(getDataDir(), name);
        if (!file.exists() || !file.canRead()) {
            throw new FileNotFoundException("Cannot find test file " + file.getAbsolutePath());
        }
        return file;
    }

    /**
     * Visits a test file using a visitor.
     *
     * @param testFileName The name of the test file under test/unit/data
     * @param visitor The visitor, that receives a Token and returns true to
     * continue iterating, false otherwise.
     * @param dumpTokens True to dump tokens to System.out for debugging.
     * @throws Exception On error
     */
    private void visitFileTokens(String testFileName, Function<Token, Boolean> visitor, boolean dumpTokens) throws Exception {
        File testFile = getTestFile(testFileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), StandardCharsets.UTF_8))) {
            RustLexer lexer = new RustLexer(CharStreams.fromReader(reader));
            lexer.addErrorListener(new RustANTLRTestErrorListener());
            for (Token token = lexer.nextToken(); token != null && token.getType() != Token.EOF; token = lexer.nextToken()) {
                if (dumpTokens) {
                    RustTokenID tokenID = RustTokenID.from(token);
                    System.out.format("Token: %3d:%-3d (%s) = '%s'%n",
                            token.getLine(),
                            token.getCharPositionInLine(),
                            tokenID.name(),
                            token.getText().replace("\n", "\\n"));
                }
                if (!visitor.apply(token)) {
                    break;
                }
            }
        }
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testShouldVisit_noparse_rs() throws Exception {
        visitFileTokens("noparse.rs", (token) -> true, true);
    }

}
