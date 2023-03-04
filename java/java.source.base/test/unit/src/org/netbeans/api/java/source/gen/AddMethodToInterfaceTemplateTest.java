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
import static com.sun.source.tree.Tree.*;
import com.sun.source.util.TreePath;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.lexer.JavaTokenId;

import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.JavaDataLoader;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.PositionBounds;

/**
 *
 * @author Jan Pokorsky
 * @author Pavel Flaska
 */
public class AddMethodToInterfaceTemplateTest extends GeneratorTestBase {

    public AddMethodToInterfaceTemplateTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(AddMethodToInterfaceTemplateTest.class);
//        suite.addTest(new AddMethodToInterfaceTemplateTest("test1"));
        return suite;
    }

    public void test1() throws Exception {
        MockServices.setServices(JavaDataLoader.class);
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test implements Runnable {\n" +
            "\n" +
            "   public void method(int a) {\n" +
            "   }\n" +
            "\n" +
            "   public static class CC{\n" +
            "   }\n" +
            "\n" +
            "   public void run() {\n" +
            "   }\n" +
            "\n" +
            "}\n"
        );
        FileObject testFO = FileUtil.toFileObject(testFile);
        DataObject testDO = DataObject.find(testFO);
        EditorCookie editor = testDO.getCookie(EditorCookie.class);
        StyledDocument doc = editor.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        JavaSource firstSrc = JavaSource.forFileObject(testFO);
        
        final ElementHandle[] methodHandle = new ElementHandle[1];
        final ElementHandle[] classHandle = new ElementHandle[1];
        final TreePathHandle[] methodTPHandle = new TreePathHandle[1];
        final TreePathHandle[] classTPHandle = new TreePathHandle[1];
        Task<CompilationController> userTask = new Task<CompilationController>() {
            // remember handles, no changes

            public void run(CompilationController javac) throws IOException {
                javac.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = javac.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                TreePath methodTreePath = javac.getTrees().getPath(cut, method);
                methodHandle[0] = ElementHandle.create(javac.getTrees().getElement(methodTreePath));
                methodTPHandle[0] = TreePathHandle.create(methodTreePath, javac);
                TreePath classTreePath = javac.getTrees().getPath(cut, clazz);
                classTPHandle[0] = TreePathHandle.create(classTreePath, javac);
                classHandle[0] = ElementHandle.create(javac.getTrees().getElement(classTreePath));
            }
        };
        
        firstSrc.runUserActionTask(userTask, true);
        assertNotNull(methodHandle[0]);
        assertNotNull(classHandle[0]);
        assertNotNull(methodTPHandle[0]);
        assertNotNull(classTPHandle[0]);
        
        Task firstTask = new Task<WorkingCopy>() {
            // add implements to class

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                Element classElm = classHandle[0].resolve(workingCopy);
                ClassTree clazz = (ClassTree) workingCopy.getTrees().getTree(classElm);
                TreeMaker make = workingCopy.getTreeMaker();
                workingCopy.rewrite(clazz, make.addClassImplementsClause(clazz, make.Identifier("Charles")));
            }

        };
        ModificationResult result = firstSrc.runModificationTask(firstTask);
        List<? extends Difference> diffs = result.getDifferences(testFO);
        List<Difference> difflist = new ArrayList<Difference>();
        for (Difference d : diffs) {
            System.err.println("Description: " + d.getDescription());
            difflist.add(d);
        }

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            // interface changes
            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree interfaceTree = (ClassTree) wc.getCompilationUnit().getTypeDecls().get(0);
                TreeMaker make = wc.getTreeMaker();
                List<TypeParameterTree> newTypeParams = new ArrayList<TypeParameterTree>();
                List<Tree> members = new ArrayList<Tree>();
                ExecutableElement element = (ExecutableElement) methodHandle[0].resolve(wc);
                assertNotNull(element);
                members.add(make.Method(element, null));
                ClassTree interfaceTreeCopy = make.Interface(
                    interfaceTree.getModifiers(),
                    interfaceTree.getSimpleName(),
                    newTypeParams,
                    Collections.<ExpressionTree>emptyList(),
                    members);

                wc.rewrite(interfaceTree, interfaceTreeCopy);
            }

        };
            
        FileObject folderFO = URLMapper.findFileObject(getWorkDir().toURI().toURL());
        assertTrue(folderFO != null);
        // create new file
        FileObject tempFO = FileUtil.getConfigFile("Templates/Classes/Interface.java"); // NOI18N
        DataFolder folder = (DataFolder) DataObject.find(folderFO);
        DataObject template = DataObject.find(tempFO);
        DataObject newIfcDO = template.createFromTemplate(folder, "Charles");
        // add type params
        JavaSource secondSrc = JavaSource.forFileObject(newIfcDO.getPrimaryFile());
        String res = TestUtilities.copyFileToString(FileUtil.toFile(newIfcDO.getPrimaryFile()));
        //System.err.println(res);
        secondSrc.runModificationTask(task).commit();
        res = TestUtilities.copyFileToString(FileUtil.toFile(newIfcDO.getPrimaryFile()));
        //System.err.println(res);
        result.commit();
        res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
}
