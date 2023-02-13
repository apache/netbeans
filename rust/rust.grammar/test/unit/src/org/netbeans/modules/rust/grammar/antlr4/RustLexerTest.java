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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.rust.grammar.RustTokenID;
import org.netbeans.modules.rust.grammar.RustTokenIDCategory;

/**
 *
 * @author antonio
 */
public class RustLexerTest extends NbTestCase {

    public RustLexerTest(String testName) {
        super(testName);
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
    public void testShouldParseSimpleFileSuccessfully() {
        System.out.println("testShouldParseSimpleFileSuccessfully");
        RustLexer lexer = new RustLexer(CharStreams.fromString("/* Comment */ fn main() { println!(\"Hello world!\"); }"));
        for (Token token = lexer.nextToken(); token != null && token.getType() != Token.EOF; token = lexer.nextToken()) {
            RustTokenID tokenID = RustTokenID.from(token);
            // Keep for debugging
            // System.out.format("Token: %s (%d) %s%n", token.getText(), token.getType(), tokenID == null ? "NULL": tokenID.name());
        }
    }

    /*
     * The original Rust lexer does not recognize block comments with a standalone '*' (such as this comment, for instance).
     */
    @Test
    public void testShouldParseBlockCommentWithSingleAsterisk() {
        System.out.println("testShouldParseBlockCommentWithSingleAsterisk");
        RustLexer lexer = new RustLexer(CharStreams.fromString("/*\n * Comment */ fn main() { println!(\"Hello world!\"); }"));
        boolean blockCommentSeen = false;
        for (Token token = lexer.nextToken(); token != null && token.getType() != Token.EOF; token = lexer.nextToken()) {
            RustTokenID tokenID = RustTokenID.from(token);
            if (tokenID == RustTokenID.BLOCK_COMMENT) {
                blockCommentSeen = true;
            }
            // Keep for debugging
            // System.out.format("Token: %s (%d) %s%n", token.getText(), token.getType(), tokenID == null ? "NULL": tokenID.name());
        }
        Assert.assertTrue("This test should have visited a BLOCK_COMMENT, but hasn't", blockCommentSeen);
    }

    /*
     * Test for documentation comments
     */
    @Test
    public void testShouldDetectInnerBlockDoc() {
        System.out.println("testShouldParseBlockCommentWithSingleAsterisk");
        RustLexer lexer = new RustLexer(CharStreams.fromString("/*!\n * Comment */ fn main() { println!(\"Hello world!\"); }"));
        boolean blockCommentSeen = false;
        for (Token token = lexer.nextToken(); token != null && token.getType() != Token.EOF; token = lexer.nextToken()) {
            RustTokenID tokenID = RustTokenID.from(token);
            if (tokenID == RustTokenID.INNER_BLOCK_DOC) {
                blockCommentSeen = true;
            }
            // Keep for debugging
            // System.out.format("Token: %s (%d) %s%n", token.getText(), token.getType(), tokenID == null ? "NULL": tokenID.name());
        }
        Assert.assertTrue("This test should have visited a INNER_BLOCK_DOC, but hasn't", blockCommentSeen);
    }

    @Test
    public void testShouldDetectNumbersInProgram() {
        System.out.println("testShouldDetectNumbersInProgram");
        RustLexer lexer = new RustLexer(CharStreams.fromString("/*\n * Comment */ fn main() { let a = 3; println!(\"Hello world!\"); }"));
        boolean numberSeen = false;
        for (Token token = lexer.nextToken(); token != null && token.getType() != Token.EOF; token = lexer.nextToken()) {
            RustTokenID tokenID = RustTokenID.from(token);
            if (tokenID.category == RustTokenIDCategory.NUMBER) {
                numberSeen = true;
            }
            // Keep for debugging
            // System.out.format("Token: %s (%d) %s%n", token.getText(), token.getType(), tokenID , tokenID.name());
        }
        Assert.assertTrue("This test should have visited a token in the RustTokenIDCategory.NUMBER, but hasn't", numberSeen);
    }

    @Test
    public void testShouldParseRawStringsCorrectly() {
        System.out.println("testShouldParseRawStringsCorrectly");
        RustLexer lexer = new RustLexer(CharStreams.fromString("noparse!(fail_class_no_begin, r\"[\\A]\");"));
        boolean rawSeen = true;
        for (Token token = lexer.nextToken(); token != null && token.getType() != Token.EOF; token = lexer.nextToken()) {
            RustTokenID tokenID = RustTokenID.from(token);
            if (tokenID == RustTokenID.RAW_STRING_LITERAL) {
                rawSeen = "r\"[\\A]\"".equals(token.getText());
            }
            // Keep for debugging
            // System.out.format("Token: %s (%d) %s%n", token.getText(), token.getType(), tokenID , tokenID.name());
        }
        Assert.assertTrue("This test should have visited a RAW_STRING_LITERAL, but hasn't", rawSeen);
    }

    @Test
    public void testShouldParseStringsWithEscapes() {
        System.out.println("testShouldParseStringsWithEscapes");
        // The following String failed with the original lexer
        // This is so because the string literal spawned different lines
        RustLexer lexer = new RustLexer(CharStreams.fromString("noparse!(fail_incomplete_escape, \"\\\\\");\n"
                + "noparse!(fail_class_incomplete, \"[A-\");"));
        boolean stringSeen = false;
        for (Token token = lexer.nextToken(); token != null && token.getType() != Token.EOF; token = lexer.nextToken()) {
            RustTokenID tokenID = RustTokenID.from(token);
            if (tokenID == RustTokenID.STRING_LITERAL) {
                if ("\"\\\\\"".equals(token.getText())) {
                    stringSeen = true;
                }
            }
            // Keep for debugging
            System.out.format("Token: %s (%d) %s%n", token.getText(), token.getType(), tokenID, tokenID.name());
        }
        Assert.assertTrue("This test should have visited a STRING_LITERAL, but hasn't", stringSeen);
    }

}
