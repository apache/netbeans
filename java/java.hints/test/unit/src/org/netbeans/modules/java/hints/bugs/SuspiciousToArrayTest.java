/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.hints.bugs;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author sdedic
 */
public class SuspiciousToArrayTest extends NbTestCase {

    public SuspiciousToArrayTest(String name) {
        super(name);
    }

    public void testParametrizedCollection() throws Exception {
        HintTest.create().
                input(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        Collection<String> col = new ArrayList<String>();  \n"
                + "        Object[] arr = col.toArray(/* comment */ new Integer[/* something */ 0x6f]);  \n"
                + "    }\n"
                + "}"
                )
                .run(SuspiciousToArray.class).
                assertWarnings("6:49-6:82:verifier:Suspicious Collection.toArray() call. Collection item type java.lang.String is not assignable to array component type java.lang.Integer").
                findWarning("6:49-6:82:verifier:Suspicious Collection.toArray() call. Collection item type java.lang.String is not assignable to array component type java.lang.Integer").
                applyFix().
                assertOutput(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        Collection<String> col = new ArrayList<String>();  \n"
                + "        Object[] arr = col.toArray(/* comment */ new String[/* something */ 0x6f]);  \n"
                + "    }\n"
                + "}");
    }

    public void testOuterTypeCast() throws Exception {
        HintTest.create().
                input(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        Collection col = new ArrayList();  \n"
                + "        Integer[] arr = (Integer[])col.toArray(/* comment */ new  String[/* something */ 0x6f]);\n"
                + "    }\n"
                + "}"
                )
                .run(SuspiciousToArray.class).
                assertWarnings("6:61-6:94:verifier:Suspicious Collection.toArray() call. The array type java.lang.String[] is not the same as casted-to type java.lang.Integer[]").
                findWarning("6:61-6:94:verifier:Suspicious Collection.toArray() call. The array type java.lang.String[] is not the same as casted-to type java.lang.Integer[]").
                applyFix().
                assertOutput(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        Collection col = new ArrayList();  \n"
                + "        Integer[] arr = (Integer[])col.toArray(/* comment */ new  Integer[/* something */ 0x6f]);\n"
                + "    }\n"
                + "}");
    }

    public void testChangeMultidimArrayTypeFix() throws Exception {
        HintTest.create().
                input(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        Collection col = new ArrayList();  \n"
                + "        Integer[][] arr = (Integer[][])col.toArray(/* comment */ new  String[/* something */ 0x6f][]);\n"
                + "    }\n"
                + "}"
                )
                .run(SuspiciousToArray.class).
                assertWarnings("6:65-6:100:verifier:Suspicious Collection.toArray() call. The array type java.lang.String[][] is not the same as casted-to type java.lang.Integer[][]").
                findWarning("6:65-6:100:verifier:Suspicious Collection.toArray() call. The array type java.lang.String[][] is not the same as casted-to type java.lang.Integer[][]").
                applyFix().
                assertOutput(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        Collection col = new ArrayList();  \n"
                + "        Integer[][] arr = (Integer[][])col.toArray(/* comment */ new  Integer[/* something */ 0x6f][]);\n"
                + "    }\n"
                + "}");
    }

    /**
     * requires fix for #236893 public void testChangeDifferentDimensionFix() throws Exception { }
     */
    public void testReplaceExpressionFix() throws Exception {
        HintTest.create().
                input(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        Collection col = new ArrayList();  \n"
                + "        String[][] x = new String[1][];\n"
                + "        Integer[][] arr = (Integer[][])col.toArray(/* comment */ x/* something */);\n"
                + "    }\n"
                + "}"
                )
                .run(SuspiciousToArray.class).
                assertWarnings("7:65-7:66:verifier:Suspicious Collection.toArray() call. The array type java.lang.String[][] is not the same as casted-to type java.lang.Integer[][]").
                findWarning("7:65-7:66:verifier:Suspicious Collection.toArray() call. The array type java.lang.String[][] is not the same as casted-to type java.lang.Integer[][]").
                applyFix().
                assertOutput(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        Collection col = new ArrayList();  \n"
                + "        String[][] x = new String[1][];\n"
                + "        Integer[][] arr = (Integer[][])col.toArray( /* comment */ new Integer[col.size()][] /* something */ );\n"
                + "    }\n"
                + "}");
    }

    /**
     * Checks that the fix that just replaces expression is not available.
     *
     * @throws Exception
     */
    public void testCollectionWithSideEffects() throws Exception {
        HintTest.create().
                input(
                "package test;\n"
                + "import java.util.Collection;\n"
                + "public final class Test {\n"
                + "    private static int i;\n"
                + "\n"
                + "    private static Collection c() { i++; return null; }\n"
                + "    public static void main(String[] args) {\n"
                + "        String[][] a = new String[7][];\n"
                + "        Integer[][] arr = (Integer[][])c().toArray( /* comment */ a /* something */ );\n"
                + "    }\n"
                + "}"
                )
                .run(SuspiciousToArray.class).
                assertWarnings("8:66-8:67:verifier:Suspicious Collection.toArray() call. The array type java.lang.String[][] is not the same as casted-to type java.lang.Integer[][]").
                findWarning("8:66-8:67:verifier:Suspicious Collection.toArray() call. The array type java.lang.String[][] is not the same as casted-to type java.lang.Integer[][]").
                assertFixes();
    }
    
    /**
     * Checks that the hint applies on the collection itself
     * @throws Exception 
     */
    public void testExtendsCollection() throws Exception {
        HintTest.create().
                input(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test extends ArrayList {\n"
                + "    public void main(String[] args) {\n"
                + "        Collection col = new ArrayList();  \n"
                + "        String[][] x = new String[1][];\n"
                + "        Integer[][] arr = (Integer[][])toArray(/* comment */ x/* something */);\n"
                + "    }\n"
                + "}"
                )
                .run(SuspiciousToArray.class)
                .findWarning("7:61-7:62:verifier:Suspicious Collection.toArray() call. The array type java.lang.String[][] is not the same as casted-to type java.lang.Integer[][]")
                .applyFix()
                .assertOutput(
                "package test;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.Collection;\n"
                + "public final class Test extends ArrayList {\n"
                + "    public void main(String[] args) {\n"
                + "        Collection col = new ArrayList();  \n"
                + "        String[][] x = new String[1][];\n"
                + "        Integer[][] arr = (Integer[][])toArray( /* comment */ new Integer[size()][] /* something */ );\n"
                + "    }\n"
                + "}");
        
    }
    
    public void testDoNotReportWildcards() throws Exception {
        HintTest.create().
                input(
                "package test;\n"
                + "import java.util.Collection;\n" +
                    "\n" +
                    "class Test {\n" +
                    "    class Item {\n" +
                    "    }\n" +
                    "    static Item[] gen(Collection<? extends Item> col) {\n" +
                    "        Item[] items = col.toArray(new Item[0]);  //<<<<<< hint\n" +
                    "        return items;\n" +
                    "    }\n" +
                    "}"
                )
                .run(SuspiciousToArray.class)
                .assertWarnings();
    }
}
