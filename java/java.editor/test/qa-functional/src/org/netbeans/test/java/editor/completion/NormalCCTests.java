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

package org.netbeans.test.java.editor.completion;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author jp159440
 */
public class NormalCCTests  extends CompletionTestPerformer{

    /** Creates a new instance of NormalCCTests */
    public NormalCCTests(String name) {
        super(name);
    }
    
    public void testarrayunsorted() throws Exception {        
        new CompletionTestCase(this).test(outputWriter, logWriter, "int[] a; a.", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/TestFile.java", 20);                
    }
    
    public void testcommonunsorted() throws Exception {        
        new CompletionTestCase(this).test(outputWriter, logWriter, " ", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/TestFile.java", 20);        
    }
    
    public void testtypecastunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "Object a = new Integer(1);((Integer) a.getClass()).", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/TestFile.java", 20);        
    }
    
    public void testarrayIIunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "String[] a = new String[10]; a[\"test\".length()].", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/TestFile.java", 20);        
    }
    
    public void testinsideunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "java.", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/TestFile.java", 20);        
    }
    
    public void testcomplexunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "Class.forName(\"\").getConstructor(new Class[] {}).", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/TestFile.java", 20);        
    }
    
    public void testoutterIunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "InnerOutter.this.", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/InnerOutter.java", 20);        
    }
    
    public void testoutterIIunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "Innerer.this.", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/InnerOutter.java", 20);        
    }
    
    public void testequalSignIunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "String x; x = ", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/TestFile.java", 20);        
    }
    
    public void testfirstArgumentunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "first.", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/ArgumentTest.java", 14);        
    }
    
    public void testsecondArgumentunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "second.", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/ArgumentTest.java", 14);        
    }
    
    public void testthirdArgumentunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "third.", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/ArgumentTest.java", 14);        
    }
    
    public void testfourthArgumentunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "fourth.", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/ArgumentTest.java", 14);        
    }
    
    public void testjdk15CCTest1iunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " private static List<java.lang.", false, getDataDir(), "CC15Tests", "test1/CCTest1i.java", 7);        
    }
    
    public void testjdk15CCTest1iiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l = new ArrayList<java.lang.", false, getDataDir(), "CC15Tests", "test1/CCTest1ii.java", 10);        
    }
    
    public void testjdk15CCTest1iiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l.add", false, getDataDir(), "CC15Tests", "test1/CCTest1iii.java", 12);        
    }
    
    public void testjdk15CCTest1ivunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l.get", false, getDataDir(), "CC15Tests", "test1/CCTest1iv.java", 14);        
    }
    
    public void testjdk15CCTest1vunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l.get(0).", false, getDataDir(), "CC15Tests", "test1/CCTest1v.java", 14);        
    }
    
    public void testjdk15CCTest2iunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " List<java.lang.", false, getDataDir(), "CC15Tests", "test2/CCTest2i.java", 9);        
    }
    
    public void testjdk15CCTest2iiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l = new ArrayList<java.lang.", false, getDataDir(), "CC15Tests", "test2/CCTest2ii.java", 11);        
    }
    
    public void testjdk15CCTest2iiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l.add", false, getDataDir(), "CC15Tests", "test2/CCTest2iii.java", 13);        
    }
    
    public void testjdk15CCTest2ivunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l.get", false, getDataDir(), "CC15Tests", "test2/CCTest2iv.java", 15);        
    }
    
    public void testjdk15CCTest2vunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l.get(0).", false, getDataDir(), "CC15Tests", "test2/CCTest2v.java", 15);        
    }
    
    public void testjdk15CCTest11iunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " private static List<java.lang.", false, getDataDir(), "CC15Tests", "test11/CCTest11i.java", 4);        
    }
    
    public void testjdk15CCTest11iiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l = new List<java.lang.", false, getDataDir(), "CC15Tests", "test11/CCTest11ii.java", 7);        
    }
    
    public void testjdk15CCTest11iiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l.add", false, getDataDir(), "CC15Tests", "test11/CCTest11iii.java", 9);        
    }
    
    public void testjdk15CCTest11ivunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l.get", false, getDataDir(), "CC15Tests", "test11/CCTest11iv.java", 11);        
    }
    
    public void testjdk15CCTest11vunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l.get(0).", false, getDataDir(), "CC15Tests", "test11/CCTest11v.java", 11);        
    }
    
    public void testjdk15CCTest12iunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " private static List<java.lang.", false, getDataDir(), "CC15Tests", "test12/CCTest12i.java", 4);        
    }
    
    public void testjdk15CCTest12iiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l = new List<java.lang.", false, getDataDir(), "CC15Tests", "test12/CCTest12ii.java", 7);        
    }
    
    public void testjdk15CCTest12iiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l.add", false, getDataDir(), "CC15Tests", "test12/CCTest12iii.java", 9);        
    }
    
    public void testjdk15CCTest12ivunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l.get", false, getDataDir(), "CC15Tests", "test12/CCTest12iv.java", 11);        
    }
    
    public void testjdk15CCTest12vunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l.get(0).", false, getDataDir(), "CC15Tests", "test12/CCTest12v.java", 11);        
    }
    
    public void testjdk15CCTest13iunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " private static List<java.lang.", false, getDataDir(), "CC15Tests", "test13/CCTest13i.java", 4);        
    }
    
    public void testjdk15CCTest13iiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l = new List<java.lang.", false, getDataDir(), "CC15Tests", "test13/CCTest13ii.java", 7);        
    }
    
    public void testjdk15CCTest13iiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l.add", false, getDataDir(), "CC15Tests", "test13/CCTest13iii.java", 9);        
    }
    
    public void testjdk15CCTest13ivunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l.get", false, getDataDir(), "CC15Tests", "test13/CCTest13iv.java", 11);        
    }
    
    public void testjdk15CCTest13vunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l.get(0).", false, getDataDir(), "CC15Tests", "test13/CCTest13v.java", 11);        
    }
    
    public void testjdk15CCTest14iunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " List<java.lang.", false, getDataDir(), "CC15Tests", "test14/CCTest14i.java", 6);        
    }
    
    public void testjdk15CCTest14iiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l = new List<java.lang.", false, getDataDir(), "CC15Tests", "test14/CCTest14ii.java", 8);        
    }
    
    public void testjdk15CCTest14iiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l.add", false, getDataDir(), "CC15Tests", "test14/CCTest14iii.java", 10);        
    }
    
    public void testjdk15CCTest14ivunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l.get", false, getDataDir(), "CC15Tests", "test14/CCTest14iv.java", 12);        
    }
    
    public void testjdk15CCTest14vunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " l.get(0).", false, getDataDir(), "CC15Tests", "test14/CCTest14v.java", 12);        
    }
    
    public void testjdk15CCTest3iunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "", false, getDataDir(), "CC15Tests", "test3/CCTest3i.java", 16);        
    }
    
    public void testjdk15CCTest3iiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " s.", false, getDataDir(), "CC15Tests", "test3/CCTest3i.java", 16);        
    }
    
    public void testjdk15CCTest4aiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "import ", false, getDataDir(), "CC15Tests", "test4/CCTest4ai.java", 4);        
    }
    
    public void testjdk15CCTest4aiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "import j", false, getDataDir(), "CC15Tests", "test4/CCTest4ai.java", 4);        
    }
    
    public void testjdk15CCTest4aiiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "import java.", false, getDataDir(), "CC15Tests", "test4/CCTest4ai.java", 4);        
    }
    
    public void testjdk15CCTest4aivunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "import java.util.Lis", false, getDataDir(), "CC15Tests", "test4/CCTest4ai.java", 4);        
    }
    
    public void testjdk15CCTest4avunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "import java.util.List.", false, getDataDir(), "CC15Tests", "test4/CCTest4ai.java", 4);        
    }
    
    public void testjdk15CCTest4biunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " int x = TEST_FIELD", false, getDataDir(), "CC15Tests", "test4/CCTest4bi.java", 9);        
    }
    
    public void testjdk15CCTest4biiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " testMethod", false, getDataDir(), "CC15Tests", "test4/CCTest4bii.java", 11);        
    }
    
    public void testjdk15CCTest4biiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " testMethod().get(0).", false, getDataDir(), "CC15Tests", "test4/CCTest4biii.java", 11);        
    }
    
    public void testjdk15CCTest4bivunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "import static ", false, getDataDir(), "CC15Tests", "test4/CCTest4biv.java", 4);        
    }
    
    public void testjdk15CCTest4bvunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "import static t", false, getDataDir(), "CC15Tests", "test4/CCTest4biv.java", 4);        
    }
    
    public void testjdk15CCTest4bviunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "import static test4.", false, getDataDir(), "CC15Tests", "test4/CCTest4biv.java", 4);        
    }
    
    public void testjdk15CCTest4bviiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "import static test4.CCTest", false, getDataDir(), "CC15Tests", "test4/CCTest4biv.java", 4);        
    }
    
    public void testjdk15CCTest4bviiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "import static test4.CCTest4a.", false, getDataDir(), "CC15Tests", "test4/CCTest4biv.java", 4);        
    }
    
    public void testjdk15CCTest4bixunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "import static test4.CCTest4a.T", false, getDataDir(), "CC15Tests", "test4/CCTest4biv.java", 4);        
    }
    
    public void testjdk15CCTest4bxunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "import static test4.CCTest4a.t", false, getDataDir(), "CC15Tests", "test4/CCTest4biv.java", 4);        
    }
    
    public void testjdk15CCTest4bxiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "import test4.CCTest4a.", false, getDataDir(), "CC15Tests", "test4/CCTest4biv.java", 4);        
    }
    
    public void testjdk15CCTest4bxiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "import test4.CCTest4a.I", false, getDataDir(), "CC15Tests", "test4/CCTest4biv.java", 4);        
    }
    
    public void testjdk15CCTest5biunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " int x = TEST_FIELD", false, getDataDir(), "CC15Tests", "test5/CCTest5bi.java", 8);        
    }
    
    public void testjdk15CCTest5biiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " testMethod", false, getDataDir(), "CC15Tests", "test5/CCTest5bii.java", 10);        
    }
    
    public void testjdk15CCTest5biiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " testMethod().get(0).", false, getDataDir(), "CC15Tests", "test5/CCTest5biii.java", 10);        
    }
    
    public void testjdk15CCTest5cunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " new Inner(", false, getDataDir(), "CC15Tests", "test5/CCTest5c.java", 8);        
    }
    
    public void testjdk15CCTest6iunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " t.test", false, getDataDir(), "CC15Tests", "test6/CCTest6i.java", 8);        
    }
    
    public void testjdk15CCTest6iiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " permanent.", false, getDataDir(), "CC15Tests", "test6/CCTest6ii.java", 12);        
    }
    
    public void testjdk15CCTest6iiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " int dummy = variable.", false, getDataDir(), "CC15Tests", "test6/CCTest6iii.java", 14);        
    }
    
    public void testjdk15CCTest6ivunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " variable[0].", false, getDataDir(), "CC15Tests", "test6/CCTest6iv.java", 16);        
    }
    
    public void testjdk15CCTest6vunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " t.test(", false, getDataDir(), "CC15Tests", "test6/CCTest6i.java", 8);        
    }
    
    public void testjdk15CCTest6viunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " t.test(\"Hello\",", false, getDataDir(), "CC15Tests", "test6/CCTest6i.java", 8);        
    }
    
    public void testjdk15CCTest6viiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " t.test(\"Hello\", \"Hello\",", false, getDataDir(), "CC15Tests", "test6/CCTest6i.java", 8);        
    }
    
    public void testjdk15CCTest6biunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " t.test", false, getDataDir(), "CC15Tests", "test6/CCTest6b.java", 7);        
    }
    
    public void testjdk15CCTest6biiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " t.test(1, ", false, getDataDir(), "CC15Tests", "test6/CCTest6b.java", 7);        
    }
    
    public void testjdk15CCTest6biiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " t.test(\"aaa\", ", false, getDataDir(), "CC15Tests", "test6/CCTest6b.java", 7);        
    }
    
    public void testjdk15CCTest6bivunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " t.test(\"aaa\", \"bbb\", ", false, getDataDir(), "CC15Tests", "test6/CCTest6b.java", 7);        
    }
    
    public void testjdk15CCTest6bvunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " t.test(\"aaa\", null, ", false, getDataDir(), "CC15Tests", "test6/CCTest6b.java", 7);        
    }
    
    public void testjdk15CCTest6bviunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " t.test(null, ", false, getDataDir(), "CC15Tests", "test6/CCTest6b.java", 7);        
    }
    
    public void testjdk15CCTest7aiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " permanent.", false, getDataDir(), "CC15Tests", "test7/CCTest7ai.java", 6);        
    }
    
    public void testjdk15CCTest7aiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " int dummy = variable.", false, getDataDir(), "CC15Tests", "test7/CCTest7aii.java", 8);        
    }
    
    public void testjdk15CCTest7aiiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " variable[0].", false, getDataDir(), "CC15Tests", "test7/CCTest7aiii.java", 10);        
    }
    
    public void testjdk15CCTest7aivunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " permanent.", false, getDataDir(), "CC15Tests", "test7/CCTest7aiv.java", 14);        
    }
    
    public void testjdk15CCTest7avunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " int dummy = variable.", false, getDataDir(), "CC15Tests", "test7/CCTest7av.java", 16);        
    }
    
    public void testjdk15CCTest7aviunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " variable[0].", false, getDataDir(), "CC15Tests", "test7/CCTest7avi.java", 18);        
    }
    
    public void testjdk15CCTest7biunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " t.test", false, getDataDir(), "CC15Tests", "test7/CCTest7bi.java", 10);        
    }
    
    public void testjdk15CCTest7biiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " testStatic", false, getDataDir(), "CC15Tests", "test7/CCTest7bii.java", 11);        
    }
    
    public void testjdk15CCTest8iunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " InnerEnum", false, getDataDir(), "CC15Tests", "test8/CCTest8i.java", 6);        
    }
    
    public void testjdk15CCTest8iiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " e = InnerEnum.", false, getDataDir(), "CC15Tests", "test8/CCTest8ii.java", 8);        
    }
    
    public void testjdk15CCTest8iiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " InnerEnum x = e.", false, getDataDir(), "CC15Tests", "test8/CCTest8iii.java", 10);        
    }
    
    public void testjdk15CCTest9biunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " CCTest9a", false, getDataDir(), "CC15Tests", "test9/CCTest9bi.java", 6);        
    }
    
    public void testjdk15CCTest9biiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " e = CCTest9a.", false, getDataDir(), "CC15Tests", "test9/CCTest9bii.java", 8);        
    }
    
    public void testjdk15CCTest9biiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " CCTest9a x = e.", false, getDataDir(), "CC15Tests", "test9/CCTest9biii.java", 10);        
    }
    
    public void testjdk15CCTest9ciunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " case ", false, getDataDir(), "CC15Tests", "test9/CCTest9c.java", 10);        
    }
    
    public void testjdk15CCTest9ciiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " case ", false, getDataDir(), "CC15Tests", "test9/CCTest9c.java", 13);        
    }
    
    public void testjdk15CCTest9ciiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " case A", false, getDataDir(), "CC15Tests", "test9/CCTest9c.java", 13);        
    }
    
    public void testjdk15CCTest9civunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " case ", false, getDataDir(), "CC15Tests", "test9/CCTest9c.java", 15);        
    }
    
    public void testjdk15CCTest10biunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " CCTest9a", false, getDataDir(), "CC15Tests", "test10/CCTest10bi.java", 8);        
    }
    
    public void testjdk15CCTest10biiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " e = CCTest9a.", false, getDataDir(), "CC15Tests", "test10/CCTest10bii.java", 10);        
    }
    
    public void testjdk15CCTest10biiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " CCTest9a x = e.", false, getDataDir(), "CC15Tests", "test10/CCTest10biii.java", 12);        
    }
    
    public void testjdk15GenericsTestiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTesti.java", 6);        
    }
    
    public void testjdk15GenericsTestiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " param.", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTesti.java", 6);        
    }
    
    public void testjdk15GenericsTestiiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " MyGenericsTest<", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 10);        
    }
    
    public void testjdk15GenericsTestivunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " MyGenericsTest<Int", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 10);        
    }
    
    public void testjdk15GenericsTestvunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " MyGenericsTest<Integer, ", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 10);        
    }
    
    public void testjdk15GenericsTestviunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " MyGenericsTest<Integer, ArithmeticException, ", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 10);        
    }
    
    public void testjdk15GenericsTestviiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " MyGenericsTest<?, ", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 13);        
    }
    
    public void testjdk15GenericsTestviiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " MyGenericsTest<? extends Number, ", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 13);        
    }
    
    public void testjdk15GenericsTestixunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " MyGenericsTest<? super Number, ", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 13);        
    }
    
    public void testjdk15GenericsTestxunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " genericstest.MyGenericClass<", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 13);        
    }
    
    public void testjdk15GenericsTestxiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " genericstest.MyGenericClass<java.lang.", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 13);        
    }
    
    public void testjdk15GenericsTestxiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " genericstest.MyGenericClass<java.lang.L", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 13);        
    }
    
    public void testjdk15GenericsTestxiiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " genericstest.MyGenericClass<java.lang.Long, ", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 13);        
    }
    
    public void testjdk15GenericsTestxivunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestiii.java", 17);        
    }
    
    public void testjdk15GenericsTestxvunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " mgt.", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestiii.java", 17);        
    }
    
    public void testjdk15GenericsTestxviunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " mc.", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestiii.java", 17);        
    }
    
    public void testjdk15GenericsTestxviiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " mcl.", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestiii.java", 17);        
    }
    
    public void testjdk15GenericsTestxviiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " mci.", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestiii.java", 17);        
    }
    
    public void testjdk15GenericsTestxixunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " mcgi.", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestiii.java", 17);        
    }
    
    public void testjdk15GenericsTestxxunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " mcsi.", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestiii.java", 17);        
    }
    
    public void testjdk15GenericsTestxxiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " mcdgi.", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestiii.java", 17);        
    }
    
    public void testjdk15AccessControlTestiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " accesscontroltest.points.", false, getDataDir(), "CC15Tests", "accesscontroltest/points/Test.java", 7);        
    }
    
    public void testjdk15AccessControlTestiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " accesscontroltest.points.", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/Test.java", 5);        
    }
    
    public void testjdk15AccessControlTestiiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/PlusPoint.java", 5);        
    }
    
    public void testjdk15AccessControlTestivunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " super.", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/PlusPoint.java", 5);        
    }
    
    public void testjdk15AccessControlTestvunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " a.", false, getDataDir(), "CC15Tests", "accesscontroltest/points/Point.java", 9);        
    }
    
    public void testjdk15AccessControlTestviunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " p.", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/Point3d.java", 8);        
    }
    
    public void testjdk15AccessControlTestviiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " this.", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/Point3d.java", 8);        
    }
    
    public void testjdk15AccessControlTestviiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " q.", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/Point3d.java", 11);        
    }
    
    public void testjdk15AccessControlTestixunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " this.", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/Point3d.java", 11);        
    }
    
    public void testjdk15AccessControlTestxunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " r.", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/Point3d.java", 15);        
    }
    
    public void testjdk15AccessControlTestxiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/Point3d.java", 15);        
    }
    
    public void testjdk15AccessControlTestxiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "accesscontroltest/points/Test.java", 7);        
    }
    
    public void testjdk15AccessControlTestxiiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " super(", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/Point3d.java", 20);        
    }
    
    public void testjdk15LocalVarsTestiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 6);        
    }
    
    public void testjdk15LocalVarsTestiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 8);        
    }
    
    public void testjdk15LocalVarsTestiiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 10);        
    }
    
    public void testjdk15LocalVarsTestivunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 13);        
    }
    
    public void testjdk15LocalVarsTestvunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 16);        
    }
    
    public void testjdk15LocalVarsTestviunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 18);        
    }
    
    public void testjdk15LocalVarsTestviiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 20);        
    }
    
    public void testjdk15LocalVarsTestviiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " if (", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 10);        
    }
    
    public void testjdk15LocalVarsTestixunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " if (i < ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 10);        
    }
    
    public void testjdk15LocalVarsTestxunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " args[i].", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 10);        
    }
    
    public void testjdk15LocalVarsTestxiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " for(int j = 0; ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 20);        
    }
    
    public void testjdk15LocalVarsTestxiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " for(int j = 0; j < 10; j++) ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 20);        
    }
    
    public void testjdk15LocalVarsTestxiiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " ((String[])objs)[0].", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 25);        
    }
    
    public void testjdk15ArraysTestiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " args.", false, getDataDir(), "CC15Tests", "arraystest/Test.java", 9);        
    }
    
    public void testjdk15ArraysTestiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " args[0].", false, getDataDir(), "CC15Tests", "arraystest/Test.java", 9);        
    }
    
    public void testjdk15ArraysTestiiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " new String[0].", false, getDataDir(), "CC15Tests", "arraystest/Test.java", 9);        
    }
    
    public void testjdk15ArraysTestivunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " new String[] {\"one\", \"two\"}.", false, getDataDir(), "CC15Tests", "arraystest/Test.java", 9);        
    }
    
    public void testjdk15ArraysTestvunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " new String[] {\"one\", \"two\"}[0].", false, getDataDir(), "CC15Tests", "arraystest/Test.java", 9);        
    }
    
    public void testjdk15ArraysTestviunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " Test.this.testArray[2].", false, getDataDir(), "CC15Tests", "arraystest/Test.java", 15);        
    }
    
    public void testjdk15ArraysTestviiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " testString.", false, getDataDir(), "CC15Tests", "arraystest/Test.java", 17);        
    }
    
    public void testjdk15ArraysTestviiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " Test.this.oneString.", false, getDataDir(), "CC15Tests", "arraystest/Test.java", 17);        
    }
    
    public void testjdk15ArraysTestixunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " ((String)objs[0]).", false, getDataDir(), "CC15Tests", "arraystest/Test.java", 24);        
    }
    
    public void testjdk15ConstructorsTestiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " new NoCtor(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 6);        
    }
    
    public void testjdk15ConstructorsTestiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " new DefaultCtor(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 6);        
    }
    
    public void testjdk15ConstructorsTestiiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " new CopyCtor(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 6);        
    }
    
    public void testjdk15ConstructorsTestivunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " new MoreCtors(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 6);        
    }
    
    public void testjdk15ConstructorsTestvunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " new GenericNoCtor<Long>(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 6);        
    }
    
    public void testjdk15ConstructorsTestviunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " new GenericDefaultCtor<Long>(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 6);        
    }
    
    public void testjdk15ConstructorsTestviiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " new GenericCopyCtor<Long>(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 6);        
    }
    
    public void testjdk15ConstructorsTestviiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " new GenericMoreCtors<Long>(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 6);        
    }
    
    public void testjdk15ConstructorsTestixunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " super(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 53);        
    }
    
    public void testjdk15ConstructorsTestxunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " super(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 59);        
    }
    
    public void testjdk15ConstructorsTestxiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " super(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 65);        
    }
    
    public void testjdk15ConstructorsTestxiiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " super(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 71);        
    }
    
    public void testjdk15ConstructorsTestxivunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " super(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 77);        
    }
    
    public void testjdk15ConstructorsTestxvunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " super(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 83);        
    }
    
    public void testjdk15ConstructorsTestxviunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " super(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 89);        
    }
    
    public void testjdk15ConstructorsTestxviiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " super(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 95);        
    }
    
    public void testjdk15ConstructorsTestxviiiunsorted() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, " new ArrayList<String[]>(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 6);        
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(NormalCCTests.class).enableModules(".*").clusters(".*"));
    }
    
}
