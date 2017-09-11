/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
