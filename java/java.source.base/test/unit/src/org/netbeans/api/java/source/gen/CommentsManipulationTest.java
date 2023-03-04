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

import com.sun.source.tree.*;
import org.junit.Test;
import org.netbeans.api.java.source.*;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.save.PositionEstimator;
import org.netbeans.modules.java.source.query.CommentHandler;
import org.netbeans.modules.java.source.builder.CommentHandlerService;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Rastislav Komara (<a href="mailto:moonko@netbeans.orgm">RKo</a>)
 * @todo documentation
 */
public class CommentsManipulationTest extends GeneratorTestBase {
//    private final String TEST_CONTENT

    public CommentsManipulationTest(String aName) {
        super(aName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(CommentsManipulationTest.class);
        return suite;
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }


    /**
     * Simulates encapsulate field refactoring with copy javadoc feature.
     *
     * @throws Exception if something goes wrong.
     */
    /*    @Test
public void testEncapsulateField() throws Exception {
File testFile = new File(getWorkDir(), "Test.java");
String origin =
"public class EncapsulateField {\n" +
    "\n" +
    "*//**
     * Level of encapsulation
     *//*\n" +
                        "public int encapsulate = 5;" +
                        "}\n";
        TestUtilities.copyStringToFile(testFile, origin);
        String golden = "\n" +
                "public class NewArrayTest {\n" +
                "\n" +
                "int[] test = new int[5];" +
                "}\n";


        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                TreeMaker make = workingCopy.getTreeMaker();
                Tree setter = createSetter(make, "Encapsulate");
                ClassTree toRewrite = make.insertClassMember(clazz, clazz.getMembers().size(), setter);

                VariableTree node = (VariableTree) extractOriginalNode(cut);
                VariableTree newVar = make.Variable(make.Modifiers(EnumSet.of(Modifier.PRIVATE)), node.getName(), node.getType(), node.getInitializer());

                workingCopy.rewrite(clazz, toRewrite);
                workingCopy.rewrite(node, newVar);

            }
        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
//        assertEquals(golden, res);
    }*/
    private Tree createSetter(TreeMaker make, String name) {
        VariableTree parameter = make.Variable(make.Modifiers(EnumSet.of(Modifier.FINAL)), "encapsulate", make.PrimitiveType(TypeKind.INT), null);
        return make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC)),
                "set" + name, make.PrimitiveType(TypeKind.INT),
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>singletonList(parameter),
                Collections.<ExpressionTree>emptyList(),
                "{;}",
                make.Literal(5));
    }

    private Tree extractOriginalNode(CompilationUnitTree cut) {
        List<? extends Tree> classes = cut.getTypeDecls();
        if (!classes.isEmpty()) {
            ClassTree clazz = (ClassTree) classes.get(0);
            List<? extends Tree> trees = clazz.getMembers();
            if (trees.size() == 2) {
                return trees.get(1);
            }
        }

        throw new IllegalStateException("There is no array declaration in expected place.");

    }
    
    public void testAddCommentOnClassTree() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        String origin =
                "public class EncapsulateField {\n" +
                        "\n" +
                        "/** Level of encapsulation */\n" +
                        "public int encapsulate = 5;" +
                        "}\n";
        TestUtilities.copyStringToFile(testFile, origin);

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
//
                TreeMaker treeMaker = workingCopy.getTreeMaker();
                CompilationUnitTree cu = workingCopy.getCompilationUnit();


                List<? extends Tree> list = cu.getTypeDecls();
                for (int i = 0; i < list.size(); i++) {
                    Tree tree = list.get(i);
                    if (tree instanceof ClassTree) {
                        javax.lang.model.element.Name simpleName = ((ClassTree) tree).getSimpleName();
                        Tree newClassTree = treeMaker.setLabel(tree, simpleName);
                        // setup new coment
                        setComment("What's up?\n", treeMaker, newClassTree, workingCopy.getTreeUtilities());
                        workingCopy.rewrite(tree, newClassTree);
                    }
                }

            }

            public void setComment(String commentText, TreeMaker treeMaker, Tree commentTree, TreeUtilities utils) {
                int found = -1;

                List<Comment> comments = utils.getComments(commentTree, true);
                for (int i = 0; i < comments.size() && found == -1; i++) {
                    Comment comment = comments.get(i);
                    if (comment.isDocComment()) {
                        found = i;
                    }
                }

                if (found != -1) {
                    treeMaker.removeComment(commentTree, found, true);
                }

                if (commentText != null) {
                    Comment comment = Comment.create(Comment.Style.JAVADOC, PositionEstimator.NOPOS, PositionEstimator.NOPOS, 1,
                            commentText);
                    treeMaker.insertComment(commentTree, comment, found, true);
                }
            }

        };
        src.runModificationTask(task).commit();
        System.out.println(TestUtilities.copyFileToString(testFile));
    }

}
