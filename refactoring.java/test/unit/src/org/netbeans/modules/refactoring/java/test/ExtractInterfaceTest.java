/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
