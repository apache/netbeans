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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Consumer;
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
        assertNotNull(crate.getImpl("Val"));
        assertNotNull(crate.getImpl("Val").getFunction("value"));

        RustASTNode main = crate.getFunction("main");
        assertNotNull(main);

        // main must have a fold
        OffsetRange mainFold = main.getFold();
        assertNotNull(mainFold);

        assertEquals(mainFold.toString(), 1149, mainFold.getStart());
        assertEquals(mainFold.toString(), 1360, mainFold.getEnd());

    }

    /**
     * Test of parse method, of class RustAST with nested modules.
     */
    @Test
    public void testShouldBuildAST_for_nested_modules_rs() throws IOException {
        System.out.println("testShouldBuildAST_for_nested_modules_rs");
        String fileText = RustTestUtils.getFileText(getDataDir(), "nested_modules.rs");
        RustAST ast = RustAST.parse(null, fileText);
        RustASTNode crate = ast.getCrate();

        assertNotNull(crate);
        assertEquals(1, crate.functions().size());
        assertNotNull("main", crate.getFunction("main"));

        assertEquals(1, crate.modules().size());
        RustASTNode A = crate.getModule("A");
        assertNotNull("A", A);

        assertEquals(2, A.functions().size());
        assertNotNull("A.a", A.getFunction("a"));
        assertNotNull("A.aa", A.getFunction("aa"));

        assertEquals(1, A.modules().size());
        RustASTNode B = A.getModule("B");
        assertNotNull("B", B);

        assertEquals(1, B.functions().size());
        assertNotNull("b", B.getFunction("b"));
    }

    private static String String_rep(int n) {
        byte[] spaces = new byte[n];
        Arrays.fill(spaces, (byte) ' ');
        return new String(spaces, StandardCharsets.US_ASCII);
    }

    @Test
    public void testShouldBuildAST_visit_nodes_of_nested_modules_rs() throws IOException {
        System.out.println("testShouldBuildAST_visit_nodes_of_nested_modules_rs");
        String fileText = RustTestUtils.getFileText(getDataDir(), "nested_modules.rs");
        RustAST ast = RustAST.parse(null, fileText);
        RustASTNode crate = ast.getCrate();

        StringBuilder sb = new StringBuilder();

        class PrintingVisitor implements Consumer<RustASTNode> {

            int level = 0;

            @Override
            public void accept(RustASTNode node) {
                sb.append(String_rep(level));
                sb.append(node);
                sb.append('\n');
                level += 2;
                node.visit(this);
                level -= 2;
            }

        };

        crate.visit(new PrintingVisitor());

        String result = sb.toString();
        // DEBUGGING:
        // System.out.format("RESULT: '%s'%n", result);
        assertTrue(result.contains("    [FUNCTION:b]"));

    }

}
