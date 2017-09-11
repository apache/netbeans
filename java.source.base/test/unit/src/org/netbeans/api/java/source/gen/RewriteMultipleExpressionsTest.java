/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author Ralph Ruijs
 */
public class RewriteMultipleExpressionsTest extends GeneratorTestBase {
    
    
    public RewriteMultipleExpressionsTest(String aName) {
        super(aName);
    }
    
    @Test
    public void testRewriteMultipleExpressions() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "\n" +
                        "public class MultipleExpressionsTest {\n" +
                        "    public void testMethod() {\n" +
                        "        printGreeting();\n" +
                        "        printGreeting();\n" +
                        "        printGreeting();\n" +
                        "    }\n" +
                        "    public void printGreeting() {\n" +
                        "        System.out.println(\"Hello World!\");\n" +
                        "    }\n" +
                        "}\n");
        String golden = "\n" +
                "public class MultipleExpressionsTest {\n" +
                "    public void testMethod() {\n" +
                "        System.out.println(\"Hello World!\");\n" +
                "        System.out.println(\"Hello World!\");\n" +
                "        System.out.println(\"Hello World!\");\n" +
                "    }\n" +
                "    public void printGreeting() {\n" +
                "        System.out.println(\"Hello World!\");\n" +
                "    }\n" +
                "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                
                List<? extends Tree> classes = cut.getTypeDecls();
                ClassTree clazz = (ClassTree) classes.get(0);
                List<? extends Tree> trees = clazz.getMembers();
                
                MethodTree testMethod = (MethodTree) trees.get(1);
                BlockTree body = testMethod.getBody();
                
                MethodTree printMethod = (MethodTree) trees.get(2);
                BlockTree printBody = printMethod.getBody();
                
                List<StatementTree> statements = new LinkedList<StatementTree>();
                statements.add(printBody.getStatements().get(0));
                statements.add(printBody.getStatements().get(0));
                statements.add(printBody.getStatements().get(0));
                
                BlockTree modified = make.Block(statements, false);
                
                workingCopy.rewrite(body, modified);
            }

        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals(golden, res);
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
}
