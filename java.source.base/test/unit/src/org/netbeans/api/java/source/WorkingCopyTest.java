/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.api.java.source;

import com.sun.source.tree.*;
import java.io.File;
import java.util.List;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class WorkingCopyTest extends NbTestCase {

    public WorkingCopyTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        this.clearWorkDir();
        File workDir = getWorkDir();
        File cacheFolder = new File (workDir, "cache"); //NOI18N
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
    }

    public void testToPhaseAfterRewrite() throws Exception {
        File f = new File(getWorkDir(), "TestClass.java");
        TestUtilities.copyStringToFile(f,
                "package foo;" +
                "public class TestClass{" +
                "   public void foo() {" +
                "   }" +
                "}");
        FileObject fo = FileUtil.toFileObject(f);
        JavaSource javaSource = JavaSource.forFileObject(fo);
        javaSource.runModificationTask(new Task<WorkingCopy>() {

            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(Phase.RESOLVED);

                TreeMaker maker = copy.getTreeMaker();
                ClassTree classTree = (ClassTree)copy.getCompilationUnit().getTypeDecls().get(0);
                TypeElement serializableElement = copy.getElements().getTypeElement("java.io.Serializable");
                ExpressionTree serializableTree = maker.QualIdent(serializableElement);
                ClassTree newClassTree = maker.addClassImplementsClause(classTree, serializableTree);

                copy.rewrite(classTree, newClassTree);
                // remove the following to make the test pass
                copy.toPhase(Phase.RESOLVED);
            }
        }).commit();

        javaSource.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController copy) throws Exception {
                copy.toPhase(Phase.RESOLVED);

                TypeElement testClassElement = copy.getElements().getTypeElement("foo.TestClass");
                TypeMirror serializableType = copy.getElements().getTypeElement("java.io.Serializable").asType();
                boolean serializableFound = false;
                for (TypeMirror type : testClassElement.getInterfaces()) {
                    if (copy.getTypes().isSameType(serializableType, type)) {
                        serializableFound = true;
                    }
                }
                assertTrue("TestClass should implement Serializable", serializableFound);
            }
        }, true);
    }

    public void testResolveRewriteTarget() throws Exception {
        File f = new File(getWorkDir(), "TestClass.java");
        String code = "package foo;" +
                      "public class TestClass{" +
                      "   public void foo() {" +
                      "   }" +
                      "}";

        TestUtilities.copyStringToFile(f, code);
        FileObject fo = FileUtil.toFileObject(f);
        JavaSource javaSource = JavaSource.forFileObject(fo);
        javaSource.runModificationTask(new Task<WorkingCopy>() {

            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(Phase.RESOLVED);

                TreeMaker maker = copy.getTreeMaker();
                ClassTree classTree = (ClassTree)copy.getCompilationUnit().getTypeDecls().get(0);
                TypeElement serializableElement = copy.getElements().getTypeElement("java.io.Serializable");
                ExpressionTree serializableTree = maker.QualIdent(serializableElement);
                ClassTree newClassTree = maker.addClassImplementsClause(classTree, serializableTree);

                copy.rewrite(classTree, newClassTree);

                assertSame(newClassTree, copy.resolveRewriteTarget(classTree));

                ClassTree finalClassTree = maker.removeClassImplementsClause(newClassTree, 0);

                copy.rewrite(newClassTree, finalClassTree);

                assertSame(finalClassTree, copy.resolveRewriteTarget(classTree));

                // remove the following to make the test pass
                copy.toPhase(Phase.RESOLVED);
            }
        }).commit();

        assertEquals(code, fo.asText());
    }
    
    public void testRewriteInComments185739() throws Exception {
        File f = new File(getWorkDir(), "TestClass.java");
        String code = "package foo;\n" +
                      "public class TestClass{\n" +
                      "   /**\n" +
                      "    * aaaa\n" +
                      "    */\n" +
                      "   public void foo() {\n" +
                      "   }\n" +
                      "}";
        TestUtilities.copyStringToFile(f, code);
        FileObject fo = FileUtil.toFileObject(f);
        JavaSource javaSource = JavaSource.forFileObject(fo);
        javaSource.runModificationTask(new Task<WorkingCopy>() {

            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(Phase.RESOLVED);

                int aaaa = copy.getText().indexOf("aaaa");

                assertTrue(aaaa >= 0);
                copy.rewriteInComment(aaaa, 2, "");
                copy.rewriteInComment(aaaa + 2, 2, "");
            }
        }).commit();

        assertEquals(code.replace("aaaa", ""), fo.asText("UTF-8"));
    }
    
    public void testSynthetic2NonSynthetic185739() throws Exception {
        File f = new File(getWorkDir(), "TestClass.java");
        String code = "package foo;\n" +
                      "public class TestClass {\n" +
                      "    public void foo() {\n" +
                      "    }\n" +
                      "}";
        TestUtilities.copyStringToFile(f, code);
        FileObject fo = FileUtil.toFileObject(f);
        JavaSource javaSource = JavaSource.forFileObject(fo);
        javaSource.runModificationTask(new Task<WorkingCopy>() {

            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(Phase.RESOLVED);

                CompilationUnitTree cut = copy.getCompilationUnit();
                TreeMaker make = copy.getTreeMaker();
                final ClassTree ct = (ClassTree) cut.getTypeDecls().get(0);
                final MethodTree mt = (MethodTree) ct.getMembers().get(0);
                ModifiersTree modT = mt.getModifiers();
                ModifiersTree newModT = make.removeModifiersModifier(modT, Modifier.PUBLIC);
                copy.rewrite(modT, newModT);
            }
        }).commit();

        String golden = "package foo;\n" +
                        "public class TestClass {\n\n" +
                        "    TestClass() {\n" +
                        "    }\n" +
                        "    public void foo() {\n" +
                        "    }\n" +
                        "}";
        assertEquals(golden, fo.asText("UTF-8"));
    }
    
    //will go away one TreeMaker.AnnotatedType is public:
    public static AnnotatedTypeTree MakeAnnotatedTypeTemp(TreeMaker make, Tree underlyingType, List<? extends AnnotationTree> annotations) {
        return make.AnnotatedType(underlyingType, annotations);
    }
}
