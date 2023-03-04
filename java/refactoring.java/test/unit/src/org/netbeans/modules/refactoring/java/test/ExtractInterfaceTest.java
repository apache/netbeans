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
package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.api.ExtractInterfaceRefactoring;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ralph Benjamin Ruijs
 */
public class ExtractInterfaceTest extends RefactoringTestBase {

    public ExtractInterfaceTest(String name) {
        super(name);
    }
    
    public void testExtractInterfaceException() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                        + "import java.io.BufferedReader;\n"
                        + "import java.io.FileNotFoundException;\n"
                        + "import java.io.FileReader;\n"
                        + "import java.io.IOException;\n"
                        + "public class ExtractBaseClass {\n"
                        + "    public void m3() throws FileNotFoundException, IOException {\n"
                        + "        BufferedReader br = new BufferedReader(new FileReader(\"C:\\\\test.txt\"));\n"
                        + "        String s;\n"
                        + "        while ((s = br.readLine()) != null) {\n"
                        + "            System.out.println(s);\n"
                        + "        }\n"
                        + "    }\n"
                        + "}"));
        performExtractInterface(src.getFileObject("extract/ExtractBaseClass.java"), 0, "ExtractInterface", Boolean.FALSE);
        verifyContent(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                        + "import java.io.BufferedReader;\n"
                        + "import java.io.FileNotFoundException;\n"
                        + "import java.io.FileReader;\n"
                        + "import java.io.IOException;\n"
                        + "public class ExtractBaseClass implements ExtractInterface {\n"
                        + "    @Override\n"
                        + "    public void m3() throws FileNotFoundException, IOException {\n"
                        + "        BufferedReader br = new BufferedReader(new FileReader(\"C:\\\\test.txt\"));\n"
                        + "        String s;\n"
                        + "        while ((s = br.readLine()) != null) {\n"
                        + "            System.out.println(s);\n"
                        + "        }\n"
                        + "    }\n"
                        + "}"),
                new File("extract/ExtractInterface.java", "/* * Refactoring License */ package extract;\n"
                        + "import java.io.FileNotFoundException;\n"
                        + "import java.io.IOException;\n"
                        + "/**\n"
                        + " *\n"
                        + " * @author junit\n"
                        + " */\n"
                        + "public interface ExtractInterface {\n"
                        + "    void m3() throws FileNotFoundException, IOException;\n"
                        + "}\n"));
    }
    
    public void test231147() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "public class ExtractBaseClass {\n"
                + "    // Method comments\n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    } // Trailing comments\n"
                + "}\n"
                + "interface ExtractInterface { }\n"));
        performExtractInterface(src.getFileObject("extract/ExtractBaseClass.java"), 0, "ExtractInterface", Boolean.FALSE, new Problem(true, "ERR_ClassClash"));
    }
    
    public void test228474() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "public class ExtractBaseClass {\n"
                + "    // Method comments\n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    } // Trailing comments\n"
                + "}"));
        performExtractInterface(src.getFileObject("extract/ExtractBaseClass.java"), 0, "ExtractBaseclass", Boolean.FALSE, new Problem(true, "ERR_ClassClash"));
    }

    public void testExtractInterface() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "public class ExtractBaseClass {\n"
                + "    // Method comments\n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    } // Trailing comments\n"
                + "}"));
        performExtractInterface(src.getFileObject("extract/ExtractBaseClass.java"), 0, "ExtractInterface", Boolean.FALSE);
        verifyContent(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "public class ExtractBaseClass implements ExtractInterface {\n"
                + "    // Method comments\n"
                + "    @Override"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    } // Trailing comments\n"
                + "}"),
                new File("extract/ExtractInterface.java", "/* * Refactoring License */ package extract;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author junit\n"
                + " */\n"
                + "public interface ExtractInterface {\n"
                + "    // Method comments\n"
                + "    void method();\n"
                + "}\n"));
    }

    private void performExtractInterface(FileObject source, final int position, final String newName, final Boolean makeAbstract, Problem... expectedProblems) throws IOException, IllegalArgumentException, InterruptedException {
        final ExtractInterfaceRefactoring[] r = new ExtractInterfaceRefactoring[1];
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = info.getCompilationUnit();
                
                final ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                final TreePath classPath = info.getTrees().getPath(cut, classTree);
                TypeElement classEl = (TypeElement) info.getTrees().getElement(classPath);
                
                TypeMirror superclass = classEl.getSuperclass();
                TypeElement superEl = (TypeElement) info.getTypes().asElement(superclass);
                
                List<ElementHandle<ExecutableElement>> members;
                if(position < 0) {
                    members = new ArrayList<>();
                    for (ExecutableElement executableElement : ElementFilter.methodsIn(classEl.getEnclosedElements())) {
                        members.add(ElementHandle.create(executableElement));
                    }
                } else {
                    members = new ArrayList<>(1);
                    ExecutableElement el = ElementFilter.methodsIn(classEl.getEnclosedElements()).get(position);
                    members.add(ElementHandle.create(el));
                }
                r[0] = new ExtractInterfaceRefactoring(TreePathHandle.create(classEl, info));
                r[0].setInterfaceName(newName);
                r[0].setMethods(members);
            }
        }, true);
        
        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<Problem>();

        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }

        assertProblems(Arrays.asList(expectedProblems), problems);
    }
}
