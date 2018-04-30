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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.api.java.source.CompilationInfo;

/**
 *
 * @author arusinha
 */
public class ConvertInvalidVarToExplicitArrayTypeTest extends ErrorHintsTestBase {

    public ConvertInvalidVarToExplicitArrayTypeTest(String name) throws Exception {
        super(name, ConvertInvalidVarToExplicitArrayType.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sourceLevel = "1.10";
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }

    @Override
    protected void tearDown() throws Exception {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = false;
        super.tearDown();
    }

    public void testParameterizedElements() throws Exception {
        performAnalysisTest("test/Test.java",
                "package test; public class Test {{final var j = {new java.util.ArrayList<String>(),new java.util.ArrayList<String>()};}}",
                -1);
    }

    public void testArrayHetrogeneousElements() throws Exception {
        performAnalysisTest("test/Test.java",
                "package test; public class Test {{final/*comment1*/ var/**comment2**/ j/*comment3*/ = /*comment4*/{new java.util.ArrayList(),new java.util.HashMap()};}}",
                -1);
    }

    public void testArrayObjectElementsFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {{final/*comment1*/ var/**comment2**/ j/*comment3*/ = /*comment4*/{new java.util.ArrayList(),new java.util.ArrayList()};}}",
                -1, "Convert Var to Explicit Type",
                "package test; import java.util.ArrayList; public class Test {{final/*comment1*/ ArrayList[]/**comment2**/ j/*comment3*/ = /*comment4*/{new java.util.ArrayList(),new java.util.ArrayList()};}}");
    }

    public void testArrayPrimitiveNumericElementsFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {{final var j = {1,2.1,3f};}}",
                -1,
                "Convert Var to Explicit Type",
                "package test; public class Test {{final double[] j = {1,2.1,3f};}}");
    }

    public void testArrayPrimitiveNumeric2ElementsFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {{final var j = {(short)1,(byte)2};}}",
                -1,
                "Convert Var to Explicit Type",
                "package test; public class Test {{final short[] j = {(short)1,(byte)2};}}");
    }

    public void testArrayStringElementsFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {{/*comment1*/ /*comment2*/@NotNull final var j = {\"hello\",\"world\"};}}",
                -1,
                "Convert Var to Explicit Type",
                "package test; public class Test {{/*comment1*/ /*comment2*/@NotNull final String[] j = {\"hello\",\"world\"};}}");
    }

    public void testArrayObject1ElementsFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {{@NotNull final var j = {new Object(),new Object()};}}",
                -1,
                "Convert Var to Explicit Type",
                "package test; public class Test {{@NotNull final Object[] j = {new Object(),new Object()};}}");
    }

    public void testArrayObject2ElementsFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {{@NotNull var j = {new Object(),new Object()};}}",
                -1,
                "Convert Var to Explicit Type",
                "package test; public class Test {{@NotNull Object[] j = {new Object(),new Object()};}}");
    }

    public void testArrayObject3ElementsFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {{final @NotNull var j = {new Object(),new Object()};}}",
                -1,
                "Convert Var to Explicit Type",
                "package test; public class Test {{final @NotNull Object[] j = {new Object(),new Object()};}}");
    }

    public void testArrayObject4ElementsFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {{final/*comment1*/var a = {new Object(),new Object()};}}",
                -1,
                "Convert Var to Explicit Type",
                "package test; public class Test {{final/*comment1*/Object[] a = {new Object(),new Object()};}}");
    }

    public void testArrayObject5ElementsFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {{final/*comment1*/var /*comment2*/ a = {2,3.1f};}}",
                -1,
                "Convert Var to Explicit Type",
                "package test; public class Test {{final/*comment1*/float[] /*comment2*/ a = {2,3.1f};}}");
    }

    public void testArrayObject6ElementsFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {{/*comment1*/var/*comment2*/ a = {2,3.1f};}}",
                -1,
                "Convert Var to Explicit Type",
                "package test; public class Test {{/*comment1*/float[]/*comment2*/ a = {2,3.1f};}}");
    }

    public void testArrayObject7ElementsFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {{var/*comment1*/ a = {2,3.1f};}}",
                -1,
                "Convert Var to Explicit Type",
                "package test; public class Test {{float[]/*comment1*/ a = {2,3.1f};}}");
    }

    public void testArrayObject8ElementsFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {{@NotNull var j = {new Object(),new Object()};}}",
                -1,
                "Convert Var to Explicit Type",
                "package test; public class Test {{@NotNull Object[] j = {new Object(),new Object()};}}");
    }

    public void testArrayObject9ElementsFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {{var/*comment1*/ k = {1,'c'};}}",
                -1, "Convert Var to Explicit Type",
                "package test; public class Test {{int[]/*comment1*/ k = {1,'c'};}}");
    }

    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) {
        List<Fix> fixes = new ConvertInvalidVarToExplicitArrayType().run(info, null, pos, path, null);
        List<Fix> result = new LinkedList<Fix>();

        for (Fix f : fixes) {
            if (f instanceof Fix) {
                result.add(f);
            }
        }

        return result;
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return (f.getText());
    }

    static {
        NbBundle.setBranding("test");
    }

}
