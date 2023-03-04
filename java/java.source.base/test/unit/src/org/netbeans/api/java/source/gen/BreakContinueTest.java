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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.BreakTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.File;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 * The following shell script was used to generate the code snippets
 * <code>cat test/unit/data/test/Test.java | tr '\n' ' ' | tr '\t' ' ' | sed -E 's| +| |g' | sed 's|"|\\"|g'</code>
 * @author Samuel Halliday
 */
public class BreakContinueTest extends GeneratorTestBase {

    public BreakContinueTest(String name) {
        super(name);
    }

    private interface Delegate {

        public void run(WorkingCopy copy, Tree tree);
    }

    private void testHelper(String test, String golden, final Kind kind, final Delegate delegate) throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        final int index = test.indexOf("|");
        assertTrue(index != -1);
        TestUtilities.copyStringToFile(testFile, test.replace("|", ""));
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy copy) throws Exception {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                TreePath node = copy.getTreeUtilities().pathFor(index);
                assertTrue(node.getLeaf().getKind() == kind);
                delegate.run(copy, node.getLeaf());
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }

    public void testBreak158130() throws Exception {
        String test = "public class Test { void m(int p) { loop: while (true) { if (p == 0) { b|reak loop; } } } }";
        String golden = "public class Test { void m(int p) { loop: while (true) { if (p == 0) { break; } } } }";
        testHelper(test, golden, Kind.BREAK, new Delegate() {

            public void run(WorkingCopy copy, Tree tree) {
                BreakTree original = (BreakTree) tree;
                TreeMaker make = copy.getTreeMaker();
                BreakTree modified = make.Break(null);
                copy.rewrite(original, modified);
            }
        });
    }

    public void testBreak158130b() throws Exception {
        String test = "public class Test { void m(int p) { loop: while (true) { i|f (p == 0) { break loop; } else { break ; } } } }";
        String golden = "public class Test { void m(int p) { loop: while (true) { if (p == 0) { break ; } } } }";
        testHelper(test, golden, Kind.IF, new Delegate() {

            public void run(WorkingCopy copy, Tree tree) {
                IfTree original = (IfTree) tree;
                IfTree modified = copy.getTreeMaker().If(original.getCondition(), original.getElseStatement(), null);
                copy.rewrite(original, modified);
            }
        });
    }

    public void testBreak158130c() throws Exception {
        String test = "public class Test { void m(int p) { loop: while (true) { i|f (p == 0) { break loop; } else { break; } } } }";
        String golden = "public class Test { void m(int p) { loop: while (true) { if (p == 0) { break; } } } }";
        testHelper(test, golden, Kind.IF, new Delegate() {

            public void run(WorkingCopy copy, Tree tree) {
                IfTree original = (IfTree) tree;
                IfTree modified = copy.getTreeMaker().If(original.getCondition(), original.getElseStatement(), null);
                copy.rewrite(original, modified);
            }
        });
    }

    public void testContinue158130() throws Exception {
        String test = "public class Test { void m(int p) { loop: while (true) { if (p == 0) { con|tinue loop; } } } }";
        String golden = "public class Test { void m(int p) { loop: while (true) { if (p == 0) { continue; } } } }";
        testHelper(test, golden, Kind.CONTINUE, new Delegate() {

            public void run(WorkingCopy copy, Tree tree) {
                ContinueTree original = (ContinueTree) tree;
                TreeMaker make = copy.getTreeMaker();
                ContinueTree modified = make.Continue(null);
                copy.rewrite(original, modified);
            }
        });
    }

    public void testBreak166327() throws Exception {
        String test =
                "public class Test {" +
                "    public static void test(int y) {" +
                "        for (int u = y;;) {" +
                "            if (y == 0)" +
                "                br|eak;" +
                "            else" +
                "                y++;" +
                "        }" +
                "    }" +
                "}";
        String golden = 
                "public class Test {" +
                "    public static void test(int y) {" +
                "        for (int u = y;;) {" +
                "            if (y == 0)" +
                "                break;" +
                "            else" +
                "                y++;" +
                "        }" +
                "    }" +
                "}";

        testHelper(test, golden, Kind.BREAK, new Delegate() {

            public void run(WorkingCopy copy, Tree tree) {
                BreakTree oldBt = (BreakTree) tree;
                BreakTree newBt = copy.getTreeMaker().Break(null);
                copy.rewrite(oldBt, newBt);
            }
            
        });
    }

    @Override
    String getSourcePckg() {
        return "";
    }

    @Override
    String getGoldenPckg() {
        return "";
    }
}
