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

import org.junit.Assert;
import org.junit.Test;
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

    @Test
    public void testShouldLexSimpleFileSuccessfully() throws Exception {
        System.out.println("testShouldLexSimpleFileSuccessfully");
        RustTestUtils.lexString("/* Comment */ fn main() { println!(\"Hello world!\"); }", false, null);
    }

    /*
     * The original Rust lexer does not recognize block comments with a standalone '*' (such as this comment, for instance).
     */
    @Test
    public void testShouldLexBlockCommentWithSingleAsterisk() throws Exception {
        System.out.println("testShouldLexBlockCommentWithSingleAsterisk");
        final boolean[] blockCommentVisited = {false};
        RustTestUtils.lexString("/*\n * Comment */ fn main() { println!(\"Hello world!\"); }", false, (token) -> {
            if (RustTokenID.from(token) == RustTokenID.BLOCK_COMMENT) {
                blockCommentVisited[0] = true;
                return false;
            }
            return true;
        });
        Assert.assertTrue("This test should have visited a BLOCK_COMMENT, but hasn't", blockCommentVisited[0]);
    }

    /*
     * Test for documentation comments
     */
    @Test
    public void testShouldDetectInnerBlockDoc() throws Exception {
        System.out.println("testShouldDetectInnerBlockDoc");
        final boolean[] innerBlockDocVisited = {false};
        RustTestUtils.lexString("/*!\n * Comment */ fn main() { println!(\"Hello world!\"); }", false, (token) -> {
            if (RustTokenID.from(token) == RustTokenID.INNER_BLOCK_DOC) {
                innerBlockDocVisited[0] = true;
                return false;
            }
            return true;
        });
        Assert.assertTrue("This test should have visited a INNER_BLOCK_DOC, but hasn't", innerBlockDocVisited[0]);
    }

    @Test
    public void testShouldDetectNumbersInProgram() throws Exception {
        System.out.println("testShouldDetectNumbersInProgram");
        final boolean[] numberVisited = {false};
        RustTestUtils.lexString("/*\n * Comment */ fn main() { let a = 3; println!(\"Hello world!\"); }", false, (token) -> {
            if (RustTokenID.from(token).category == RustTokenIDCategory.NUMBER) {
                numberVisited[0] = true;
                return false;
            }
            return true;
        });
        Assert.assertTrue("This test should have visited a token in the RustTokenIDCategory.NUMBER, but hasn't", numberVisited[0]);
    }

    @Test
    public void testShouldLexRawStringsCorrectly() throws Exception {
        System.out.println("testShouldLexRawStringsCorrectly");
        final boolean[] rawStringVisited = {false};
        RustTestUtils.lexString("noparse!(fail_class_no_begin, r\"[\\A]\");", false, (token) -> {
            if (RustTokenID.from(token) == RustTokenID.RAW_STRING_LITERAL) {
                rawStringVisited[0] = true;
                return false;
            }
            return true;
        });
        Assert.assertTrue("This test should have visited a RAW_STRING_LITERAL, but hasn't", rawStringVisited[0]);
    }

    @Test
    public void testShouldLexStringsWithEscapes() throws Exception {
        System.out.println("testShouldLexStringsWithEscapes");
        // The following String failed with the original lexer
        // This is so because the string literal spawned different lines

        final boolean[] weirdStringVisited = {false};
        RustTestUtils.lexString("noparse!(fail_incomplete_escape, \"\\\\\");\n" + "noparse!(fail_class_incomplete, \"[A-\");", false, (token) -> {
            RustTokenID tokenID = RustTokenID.from(token);
            if (tokenID == RustTokenID.STRING_LITERAL) {
                if ("\"\\\\\"".equals(token.getText())) {
                    weirdStringVisited[0] = true;
                    return false;
                }
            }
            return true;
        });
        Assert.assertTrue("This test should have visited a STRING_LITERAL with a specific content, but hasn't", weirdStringVisited[0]);
    }

    @Test
    public void testShouldLexIncompleteLiterals() throws Exception {
        RustTestUtils.lexString("fn main() { println!(\"", false, null);
        RustTestUtils.lexString("fn main() { println!('", false, null);
        RustTestUtils.lexString("fn main() { println!('\\", false, null);
        RustTestUtils.lexString("fn main() { println!('\\u", false, null);
        RustTestUtils.lexString("fn main() { println!(b", false, null);
        RustTestUtils.lexString("fn main() { println!(b\"", false, null);
        RustTestUtils.lexString("fn main() { println!(r", false, null);
        RustTestUtils.lexString("fn main() { println!(r#", false, null);
        RustTestUtils.lexString("fn main() { println!(r##", false, null);
        RustTestUtils.lexString("fn main() { println!(r##\"", false, null);
    }
}
