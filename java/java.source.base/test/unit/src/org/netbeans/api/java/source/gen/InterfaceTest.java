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
        //System.err.println(res);
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
        //System.err.println(res);
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
        //System.err.println(res);
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
        //System.err.println(res);
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
        //System.err.println(res);
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
        //System.err.println(res);
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
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

}
