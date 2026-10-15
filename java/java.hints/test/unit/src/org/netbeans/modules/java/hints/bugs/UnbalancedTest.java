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
package org.netbeans.modules.java.hints.bugs;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class UnbalancedTest extends NbTestCase {

    public UnbalancedTest(String name) {
        super(name);
    }

    public void testArrayWriteOnly() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private byte[] arr;\n" +
                       "    private void t() { arr[0] = 0; }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertContainsWarnings("2:19-2:22:verifier:ERR_UnbalancedArrayWRITE arr");
    }

    public void testArrayReadOnly1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private byte[] arr;\n" +
                       "    private void t() { System.err.println(arr[0]); }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertContainsWarnings("2:19-2:22:verifier:ERR_UnbalancedArrayREAD arr");
    }

    public void testArrayReadOnly2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private byte[] arr = new byte[0];\n" +
                       "    private void t() { System.err.println(arr[0]); }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertContainsWarnings("2:19-2:22:verifier:ERR_UnbalancedArrayREAD arr");
    }
/* TODO: fails, see 4402
    public void testArrayReadOnly3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private final byte[] arr;\n" +
                       "    private Test() { arr = new byte[0]; }\n" +
                       "    private void t() { System.err.println(arr[0]); }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertContainsWarnings("2:25-2:28:verifier:ERR_UnbalancedArrayREAD arr");
    }
*/
    public void testArrayNeg1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private byte[] arr;\n" +
                       "    private void t() { arr[0] = 0; System.err.println(arr[0]); }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertWarnings();
    }

    public void testArrayNeg2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private byte[] arr;\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertWarnings();
    }

    public void testArrayNeg3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private byte[] arr;\n" +
                       "    private void t() { System.err.println(arr[0]); }\n" +
                       "    private Object g() { return arr; }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertWarnings();
    }

    public void testArrayNeg4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private byte[] arr = {1, 2, 3};\n" +
                       "    private void t() { System.err.println(arr[0]); }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertWarnings();
    }

    public void testArrayNeg206855() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "  private final int aa[][] = new int[3][3];\n" +
                       "  public Test() {\n" +
                       "    aa[0][0] = 1;\n" +
                       "  }\n" +
                       "  public int get() {\n" +
                       "    return aa[0][0];\n" +
                       "  }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertWarnings();
    }

    public void testNegForeach209850() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "  private final int aa[][] = new int[3][3];\n" +
                       "  public void get() {\n" +
                       "    for (int[] span : aa) {\n" +
                       "         System.err.println(span[0] + span[1]);\n" +
                       "    }\n" +
                       "  }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertWarnings();
    }
    
    public void testNeg211248a() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "  private final String aa[] = \"\".split(\";\");\n" +
                       "  public void get() {\n" +
                       "     System.err.println(aa[0]);\n" +
                       "  }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertWarnings();
    }
    
    public void testNeg211248b() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "  private final String aa[] = \"\".split(\";\");\n" +
                       "  public void get() {\n" +
                       "     String str;\n" +
                       "     str = aa[0];\n" +
                       "     System.err.println(str);\n" +
                       "  }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertWarnings();
    }
    
    public void testNeg211248c() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "  private final String aa[] = \"\".split(\";\");\n" +
                       "  public void get() {\n" +
                       "     String str = \"\";\n" +
                       "     str += aa[0];\n" +
                       "     System.err.println(str);\n" +
                       "  }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertWarnings();
    }
    
    public void testInit1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "  private final String aa[] = \"\".split(\";\");\n" +
                       "  public void get() {\n" +
                       "  }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertWarnings("2:23-2:25:verifier:ERR_UnbalancedArrayWRITE aa");
    }
    
    public void testInit2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "  private final String aa[] = new String[] {\";\"};\n" +
                       "  public void get() {\n" +
                       "  }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertWarnings("2:23-2:25:verifier:ERR_UnbalancedArrayWRITE aa");
    }

    public void testCollectionWriteOnly1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll;\n" +
                       "    private void t() { coll.add(\"a\"); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertContainsWarnings("2:35-2:39:verifier:ERR_UnbalancedCollectionWRITE coll");
    }

    public void testCollectionWriteOnly2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll;\n" +
                       "    private void t() { coll.add(\"a\"); coll.iterator(); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertContainsWarnings("2:35-2:39:verifier:ERR_UnbalancedCollectionWRITE coll");
    }

    public void testCollectionReadOnly1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll;\n" +
                       "    private void t() { String str = coll.get(0); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertContainsWarnings("2:35-2:39:verifier:ERR_UnbalancedCollectionREAD coll");
    }

    public void testCollectionReadOnly2() throws Exception {//XXX ?
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll;\n" +
                       "    private void t() { String str = coll.remove(0); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertContainsWarnings("2:35-2:39:verifier:ERR_UnbalancedCollectionREAD coll");
    }

    public void testCollectionReadOnly3() throws Exception {//XXX ?
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll = new java.util.ArrayList<String>(1);\n" +
                       "    private void t() { String str = coll.remove(0); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertContainsWarnings("2:35-2:39:verifier:ERR_UnbalancedCollectionREAD coll");
    }
/* TODO: fails, see 4402
    public void testCollectionReadOnly4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private final java.util.List<String> coll;\n" +
                       "    private Test () { coll = new java.util.ArrayList<String>(1); }\n" +
                       "    private java.util.stream.Stream t() { return coll.stream(); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertContainsWarnings("2:41-2:45:verifier:ERR_UnbalancedCollectionREAD coll");
    }
*/
    public void testMapReadOnly1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.Map<String, String> map;\n" +
                       "    private void t() { String str = map.get(\"a\"); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertContainsWarnings("2:42-2:45:verifier:ERR_UnbalancedCollectionREAD map");
    }

    public void testCollectionNeg1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll;\n" +
                       "    private void t() { coll.add(\"a\"); System.err.println(coll.get(0)); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertWarnings();
    }

    public void testCollectionNeg2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll;\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertWarnings();
    }

    public void testCollectionNeg3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll;\n" +
                       "    private void t() { System.err.println(coll.get(0)); }\n" +
                       "    private Object g() { return coll; }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertWarnings();
    }

    public void testCollectionNeg4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll = new java.util.ArrayList<String>(java.util.Arrays.asList(\"foo\"));\n" +
                       "    private void t() { System.err.println(coll.get(0)); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertWarnings();
    }

    public void testCollectionNegAddTested() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll = new java.util.ArrayList<String>();\n" +
                       "    public void t1(String str) { if (coll.add(str)) System.err.println(\"\"); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertWarnings();
    }

    public void testCollectionLocalVariable() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void t() { java.util.List<String> coll = new java.util.ArrayList<String>(); String str = coll.get(0); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertContainsWarnings("2:46-2:50:verifier:ERR_UnbalancedCollectionREAD coll");
    }

    public void testCollectionNegNonPrivate() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    java.util.List<String> coll = new java.util.ArrayList<String>();\n" +
                       "    public void t1(String str) { if (coll.add(str)) System.err.println(\"\"); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertWarnings();
    }

    public void testCollectionNegEnhForLoop() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.List;\n" +
                       "public class Test {\n" +
                       "    public int t1(List<List<String>> ll) { int total = 0; for (List<String> l : ll) total += l.size(); return total; }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertWarnings();
    }

    public void testListForEach() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void t() {\n" +
                       "        java.util.List<String> coll = new java.util.ArrayList<String>();\n" +
                       "        coll.add(\"\");\n" +
                       "        coll.forEach(e -> {});\n" +
                       "    }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertWarnings();
    }

    public void testSequencedCollection1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void t() {\n" +
                       "        java.util.List<String> coll = new java.util.ArrayList<String>();\n" +
                       "        coll.addFirst(\"\");\n" +
                       "        coll.addLast(\"\");\n" +
                       "    }\n" +
                       "}\n", false)
                .run(Unbalanced.Collection.class)
                .assertWarnings("3:31-3:35:verifier:ERR_UnbalancedCollectionWRITE coll");
    }

    public void testSequencedCollection2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void t() {\n" +
                       "        java.util.List<String> coll = new java.util.ArrayList<String>();\n" +
                       "        Object sink;\n" +
                       "        sink = coll.reversed();\n" +
                       "        sink = coll.getFirst();\n" +
                       "        sink = coll.getLast();\n" +
                       "        sink = coll.removeFirst();\n" +
                       "        sink = coll.removeLast();\n" +
                       "    }\n" +
                       "}\n", false)
                .run(Unbalanced.Collection.class)
                .assertWarnings("3:31-3:35:verifier:ERR_UnbalancedCollectionREAD coll");
    }
}
