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

import org.netbeans.junit.NbTestSuite;
import org.netbeans.api.java.source.*;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol;

import javax.lang.model.type.TypeKind;
import javax.lang.model.element.ExecutableElement;

/**
 * @author Rastislav Komara (<a href="mailto:moonko@netbeans.orgm">RKo</a>)
 * @todo documentation
 */
public class TreeManipulationTest extends GeneratorTestBase  {
    
    public TreeManipulationTest(String aName) {
        super(aName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(TreeManipulationTest.class);
        return suite;
    }
    
    public void test121444() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        String testContent = "\n" +
                "public class NewArrayTest {\n" +
                "\n" +
                " public void foo(String... a){;}" +
                "}\n";
        TestUtilities.copyStringToFile(testFile, testContent);

        JavaSource src = getJavaSource(testFile);
/*
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                Tree node = extractOriginalNode(cut);
                TreeMaker make = workingCopy.getTreeMaker();
                List<ExpressionTree> init = new ArrayList<ExpressionTree>();
                init.add(make.Literal(5));
                ExpressionTree modified = make.NewArray(
                        make.PrimitiveType(TypeKind.INT),
                        init, new ArrayList<ExpressionTree>());
                System.out.println("original: " + node);
                System.out.println("modified: " + modified);
                workingCopy.rewrite(node, modified);
            }

        };
*/
        final boolean[] result = new boolean[]{false}; 
        src.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController cc) throws Exception {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cu = cc.getCompilationUnit();
                Tree node = extractOriginalNode(cu);
                TreePath path = TreePath.getPath(cu, node);
                ExecutableElement ee = (ExecutableElement) cc.getTrees().getElement(path);
                result[0] = ee.isVarArgs();                                                
            }
        }, true);
        assertEquals("Executable element accepts variable number of arguments", true, result[0]);
    }

    private TreePath extractInterestingPath(CompilationController cc) {
        CompilationUnitTree cu = cc.getCompilationUnit();
        return TreePath.getPath(cu, extractOriginalNode(cu));
    }

    private Tree extractOriginalNode(CompilationUnitTree cut) {
        List<? extends Tree> classes = cut.getTypeDecls();
        if (!classes.isEmpty()) {
            ClassTree clazz = (ClassTree) classes.get(0);
            List<? extends Tree> trees = clazz.getMembers();
//                    System.out.println("Trees:" + trees);
            if (trees.size() == 2) {

                return trees.get(1);
            }
        }

        throw new IllegalStateException("There is no array declaration in expected place.");
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
}
