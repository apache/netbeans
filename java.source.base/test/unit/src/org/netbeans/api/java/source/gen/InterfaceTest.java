/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997, 2016 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import java.io.File;
import java.util.EnumSet;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Jan Pokorsky
 */
public class InterfaceTest extends GeneratorTestMDRCompat {

    public InterfaceTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(InterfaceTest.class);
//        suite.addTest(new InterfaceTest("testAddField"));
        return suite;
    }
    
    // issue #100796
    public void testAddField() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public interface Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public interface Test {\n" +
            "\n" +
            "    public static final int CONSTANT = 0;\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        src.runModificationTask(new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree ct = (ClassTree) wc.getCompilationUnit().getTypeDecls().get(0);
                TreeMaker make = wc.getTreeMaker();
                Tree vt = make.Variable(
                        make.Modifiers(EnumSet.<Modifier>of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)),
                        "CONSTANT",
                        make.PrimitiveType(TypeKind.INT),
                        make.Identifier("0")
                        );
                wc.rewrite(ct, make.addClassMember(ct, vt));
            }
        }).commit();
        
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testClassToInterfaceTest213002a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "@Deprecated\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "@Deprecated\n" +
            "public interface Test {\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        src.runModificationTask(new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree ct = (ClassTree) wc.getCompilationUnit().getTypeDecls().get(0);
                TreeMaker make = wc.getTreeMaker();
                ClassTree i = make.Interface(ct.getModifiers(), ct.getSimpleName(), ct.getTypeParameters(), ct.getImplementsClause(), ct.getMembers());
                wc.rewrite(ct, i);
            }
        }).commit();
        
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testClassToInterfaceTest213002b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public interface Test {\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        src.runModificationTask(new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree ct = (ClassTree) wc.getCompilationUnit().getTypeDecls().get(0);
                TreeMaker make = wc.getTreeMaker();
                ClassTree i = make.Interface(ct.getModifiers(), ct.getSimpleName(), ct.getTypeParameters(), ct.getImplementsClause(), ct.getMembers());
                wc.rewrite(ct, i);
            }
        }).commit();
        
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testClassToEnumTest213002c() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "@Deprecated\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "@Deprecated\n" +
            "public enum Test {\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        src.runModificationTask(new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree ct = (ClassTree) wc.getCompilationUnit().getTypeDecls().get(0);
                TreeMaker make = wc.getTreeMaker();
                ClassTree i = make.Enum(ct.getModifiers(), ct.getSimpleName(), ct.getImplementsClause(), ct.getMembers());
                wc.rewrite(ct, i);
            }
        }).commit();
        
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testClassToInterfaceTest213002d() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "@Deprecated\n" +
            "public interface Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "@Deprecated\n" +
            "public class Test {\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        src.runModificationTask(new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree ct = (ClassTree) wc.getCompilationUnit().getTypeDecls().get(0);
                TreeMaker make = wc.getTreeMaker();
                //XXX: TreeMaker.Class will keep whatever "kind" is in the modifiers, need to strip this extra information:
                ModifiersTree mt = make.Modifiers(ct.getModifiers().getFlags(), ct.getModifiers().getAnnotations());
                ClassTree i = make.Class(mt, ct.getSimpleName(), ct.getTypeParameters(), null, ct.getImplementsClause(), ct.getMembers());
                wc.rewrite(ct, i);
            }
        }).commit();
        
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testClassToInterfaceTest213002e() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "@Deprecated\n" +
            "public interface Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "@Deprecated\n" +
            "public @interface Test {\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        src.runModificationTask(new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree ct = (ClassTree) wc.getCompilationUnit().getTypeDecls().get(0);
                TreeMaker make = wc.getTreeMaker();
                ClassTree i = make.AnnotationType(ct.getModifiers(), ct.getSimpleName(), ct.getMembers());
                wc.rewrite(ct, i);
            }
        }).commit();
        
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testClassToInterfaceTest213002f() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "@Deprecated\n" +
            "public @interface Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "@Deprecated\n" +
            "public interface Test {\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        src.runModificationTask(new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree ct = (ClassTree) wc.getCompilationUnit().getTypeDecls().get(0);
                TreeMaker make = wc.getTreeMaker();
                ClassTree i = make.Interface(ct.getModifiers(), ct.getSimpleName(), ct.getTypeParameters(), ct.getImplementsClause(), ct.getMembers());
                wc.rewrite(ct, i);
            }
        }).commit();
        
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

}
