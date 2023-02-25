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
package org.netbeans.modules.rust.grammar.ast;

import java.io.IOException;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.rust.grammar.antlr4.RustTestUtils;

/**
 * Tests for the RustAST
 */
public class RustASTTest extends NbTestCase {

    public RustASTTest(String name) {
        super(name);
    }

    /**
     * Test of parse method, of class RustAST.
     */
    @Test
    public void testShouldBuildASTFor_impl_example_rs() throws IOException {
        System.out.println("testShouldBuildASTFor_impl_example_rs");
        String fileText = RustTestUtils.getFileText(getDataDir(), "impl-example.rs");
        RustAST ast = RustAST.parse(null, fileText);
        RustASTNode crate = ast.getCrate();

        // Ensure we don't have any errors.
        String errorMessages = RustTestUtils.getErrorMessages(ast.getErrors());
        assertEquals(errorMessages, 0, ast.getErrors().size());

        // Find the 'impl Val' and find a 'value' function there
        System.out.format("Impls: '%s'%n", crate.implNames());
        assertNotNull(crate.getImpl("Val"));
        assertNotNull(crate.getImpl("Val").getFunction("value"));

        RustASTNode main = crate.getFunction("main");
        assertNotNull(main);

        // main must have a fold
        OffsetRange mainFold = main.getFold();
        assertNotNull(mainFold);

        assertEquals(mainFold.toString(), 1149, mainFold.getStart());
        assertEquals(mainFold.toString(), 1360, mainFold.getEnd());

        // main.codeblockFolds() should not be null, because we have an if statement...
        assertNotNull(main.codeblockFolds());
        assertFalse(main.codeblockFolds().isEmpty());
        assertEquals("if in main must have two code blocks", 2, main.codeblockFolds().size());
        OffsetRange ifThenFold = main.codeblockFolds().get(0);
        assertEquals(ifThenFold.toString(), 1262, ifThenFold.getStart());
        assertEquals(ifThenFold.toString(), 1284, ifThenFold.getEnd());
        OffsetRange ifElseFold = main.codeblockFolds().get(1);
        assertEquals(ifElseFold.toString(), 1290, ifElseFold.getStart());
        assertEquals(ifElseFold.toString(), 1310, ifElseFold.getEnd());

    }

}
