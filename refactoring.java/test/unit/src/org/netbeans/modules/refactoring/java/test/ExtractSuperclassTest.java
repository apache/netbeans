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
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.api.ExtractSuperclassRefactoring;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ralph Benjamin Ruijs
 */
public class ExtractSuperclassTest extends RefactoringTestBase {

    public ExtractSuperclassTest(String name) {
        super(name);
    }

    public void test235246() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "public class ExtractBaseClass {\n"
                + "    public class MyClass {\n"
                + "        public void method() {\n"
                + "            System.out.println(\"123\");\n"
                + "        }\n"
                + "    }\n"
                + "}"));
        performExtractSuperclass(src.getFileObject("extract/ExtractBaseClass.java"), 1, 1, "ExtractSuperClass", Boolean.FALSE);
        verifyContent(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "public class ExtractBaseClass {\n"
                + "    public class MyClass extends ExtractSuperClass {\n"
                + "    }\n"
                + "}"),
                new File("extract/ExtractSuperClass.java", "/* * Refactoring License */ package extract;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author junit\n"
                + " */\n"
                + "public class ExtractSuperClass {\n"
                + "    public void method() {\n"
                + "        System.out.println(\"123\");\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void test231146() throws Exception {
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
                + "class ExtractSuperClass { }\n"));
        performExtractSuperclass(src.getFileObject("extract/ExtractBaseClass.java"), 1, -1, "ExtractSuperClass", Boolean.FALSE, new Problem(true, "ERR_ClassClash"));
    }
    
    public void test252621() throws Exception { //#252621 - [Extract Superclass] Import for annotation is not added
        writeFilesAndWaitForScan(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                        + "\n"
                        + "import java.beans.Transient;\n"
                        + "\n"
                        + "public class ExtractBaseClass {\n"
                        + "    @Transient\n"
                        + "    public void m() {\n"
                        + "    }\n"
                        + "}"));
        performExtractSuperclass(src.getFileObject("extract/ExtractBaseClass.java"), 1, -1, "ExtractSuperClass", Boolean.FALSE);
        verifyContent(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "import java.beans.Transient;\n"
                + "\n"
                + "public class ExtractBaseClass extends ExtractSuperClass {\n"

                + "}"),
                new File("extract/ExtractSuperClass.java", "/* * Refactoring License */ package extract;\n"
                + "\n"
                + "import java.beans.Transient;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author junit\n"
                + " */\n"
                + "public class ExtractSuperClass {\n"
                + "    @Transient\n"
                + "    public void m() {\n"
                + "    }\n"
                + "}\n"));
    }
    
    
    public void test231639() throws Exception { //#231639 - StackOverflowError at com.sun.tools.javac.code.Type$WildcardType.getExtendsBound 
        writeFilesAndWaitForScan(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "import java.io.IOException;\n"
                + "\n"
                + "public class ExtractBaseClass<D extends Comparable<? super D>> {\n"
                + "    public void method(D d) throws IOException {\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "}"));
        performExtractSuperclass(src.getFileObject("extract/ExtractBaseClass.java"), 1, -1, "ExtractSuperClass", Boolean.FALSE);
        verifyContent(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "import java.io.IOException;\n"
                + "\n"
                + "public class ExtractBaseClass<D extends Comparable<? super D>> extends ExtractSuperClass<D> {\n"
                + "    \n"
                + "}"),
                new File("extract/ExtractSuperClass.java", "/* * Refactoring License */ package extract;\n"
                + "\n"
                + "import java.io.IOException;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author junit\n"
                + " */\n"
                + "public class ExtractSuperClass<D extends Comparable<? super D>> {\n"
                + "    public void method(D d) throws IOException {\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void test226518a() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "import java.io.IOException;\n"
                + "\n"
                + "public class ExtractBaseClass {\n"
                + "    public void method() throws IOException {\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "}"));
        performExtractSuperclass(src.getFileObject("extract/ExtractBaseClass.java"), 1, -1, "ExtractSuperClass", Boolean.FALSE);
        verifyContent(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "import java.io.IOException;\n"
                + "\n"
                + "public class ExtractBaseClass extends ExtractSuperClass {\n"
                + "    \n"
                + "}"),
                new File("extract/ExtractSuperClass.java", "/* * Refactoring License */ package extract;\n"
                + "\n"
                + "import java.io.IOException;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author junit\n"
                + " */\n"
                + "public class ExtractSuperClass {\n"
                + "    public void method() throws IOException {\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void test226518b() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "import java.util.List;\n"
                + "\n"
                + "public class ExtractBaseClass {\n"
                + "    public List method() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}"));
        performExtractSuperclass(src.getFileObject("extract/ExtractBaseClass.java"), 1, -1, "ExtractSuperClass", Boolean.FALSE);
        verifyContent(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "import java.util.List;\n"
                + "\n"
                + "public class ExtractBaseClass extends ExtractSuperClass {\n"
                + "    \n"
                + "}"),
                new File("extract/ExtractSuperClass.java", "/* * Refactoring License */ package extract;\n"
                + "\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author junit\n"
                + " */\n"
                + "public class ExtractSuperClass {\n"
                + "    public List method() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void test226518c() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "import java.util.List;\n"
                + "import java.util.LinkedList;\n"
                + "\n"
                + "public class ExtractBaseClass {\n"
                + "    public List method() {\n"
                + "        return new LinkedList();\n"
                + "    }\n"
                + "}"));
        performExtractSuperclass(src.getFileObject("extract/ExtractBaseClass.java"), 1, -1, "ExtractSuperClass", Boolean.FALSE);
        verifyContent(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "import java.util.List;\n"
                + "import java.util.LinkedList;\n"
                + "\n"
                + "public class ExtractBaseClass extends ExtractSuperClass {\n"
                + "    \n"
                + "}"),
                new File("extract/ExtractSuperClass.java", "/* * Refactoring License */ package extract;\n"
                + "\n"
                + "import java.util.LinkedList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author junit\n"
                + " */\n"
                + "public class ExtractSuperClass {\n"
                + "    public List method() {\n"
                + "        return new LinkedList();\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void test212624a() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "public class ExtractBaseClass {\n"
                + "    /**\n"
                + "     * Method comments\n"
                + "     */\n"
                + "    private String value;\n"
                + "}"));
        performExtractSuperclass(src.getFileObject("extract/ExtractBaseClass.java"), 1, -1, "ExtractSuperClass", Boolean.FALSE);
        verifyContent(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "public class ExtractBaseClass extends ExtractSuperClass {\n"
                + "    \n"
                + "}"),
                new File("extract/ExtractSuperClass.java", "/* * Refactoring License */ package extract;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author junit\n"
                + " */\n"
                + "public class ExtractSuperClass {\n"
                + "    /**\n"
                + "     * Method comments\n"
                + "     */\n"
                + "    protected String value;\n"
                + "}\n"));
    }
     
    public void test212624b() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "public class ExtractBaseClass {\n"
                + "    // Method comments\n"
                + "    private String value;\n"
                + "}"));
        performExtractSuperclass(src.getFileObject("extract/ExtractBaseClass.java"), 1, -1, "ExtractSuperClass", Boolean.FALSE);
        verifyContent(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "public class ExtractBaseClass extends ExtractSuperClass {\n"
                + "    \n"
                + "}"),
                new File("extract/ExtractSuperClass.java", "/* * Refactoring License */ package extract;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author junit\n"
                + " */\n"
                + "public class ExtractSuperClass {\n"
                + "    // Method comments\n"
                + "    protected String value;\n"
                + "}\n"));
    }

    public void test211894a() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "public class ExtractBaseClass {\n"
                + "    /**\n"
                + "     * Method comments\n"
                + "     */\n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    } // Trailing comments\n"
                + "}"));
        performExtractSuperclass(src.getFileObject("extract/ExtractBaseClass.java"), 1, -1, "ExtractSuperClass", Boolean.FALSE);
        verifyContent(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "public class ExtractBaseClass extends ExtractSuperClass {\n"
                + "    \n"
                + "}"),
                new File("extract/ExtractSuperClass.java", "/* * Refactoring License */ package extract;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author junit\n"
                + " */\n"
                + "public class ExtractSuperClass {\n"
                + "    /**\n"
                + "     * Method comments\n"
                + "     */\n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    } // Trailing comments\n"
                + "}\n"));
    }
    
    public void test211894b() throws Exception {    
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
        performExtractSuperclass(src.getFileObject("extract/ExtractBaseClass.java"), 1, -1, "ExtractSuperClass", Boolean.FALSE);
        verifyContent(src,
                new File("extract/ExtractBaseClass.java", "package extract;\n"
                + "\n"
                + "public class ExtractBaseClass extends ExtractSuperClass {\n"
                + "    \n"
                + "}"),
                new File("extract/ExtractSuperClass.java", "/* * Refactoring License */ package extract;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author junit\n"
                + " */\n"
                + "public class ExtractSuperClass {\n"
                + "    // Method comments\n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    } // Trailing comments\n"
                + "}\n"));
    }

    private void performExtractSuperclass(FileObject source, final int position, final int innerpos, final String newName, final Boolean makeAbstract, Problem... expectedProblems) throws IOException, IllegalArgumentException, InterruptedException {
        final ExtractSuperclassRefactoring[] r = new ExtractSuperclassRefactoring[1];
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = info.getCompilationUnit();
                
                ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                if(innerpos >= 0) {
                    classTree = (ClassTree) classTree.getMembers().get(innerpos);
                }
                final TreePath classPath = info.getTrees().getPath(cut, classTree);
                TypeElement classEl = (TypeElement) info.getTrees().getElement(classPath);
                
                TypeMirror superclass = classEl.getSuperclass();
                TypeElement superEl = (TypeElement) info.getTypes().asElement(superclass);
                
                MemberInfo[] members;
                if(position < 0) {
                    List<? extends Tree> classMembers = classTree.getMembers();
                    List<MemberInfo> selectedMembers = new LinkedList<MemberInfo>();
                    for (int i = 0; i < classMembers.size(); i++) {
                        Tree tree = classMembers.get(i);
                        if(!info.getTreeUtilities().isSynthetic(new TreePath(classPath, tree)) ) {
                            Element el = info.getTrees().getElement(new TreePath(classPath, tree));
                            MemberInfo<ElementHandle<Element>> memberInfo = MemberInfo.create(el, info);
                            memberInfo.setMakeAbstract(makeAbstract);
                            selectedMembers.add(memberInfo);
                        }
                    }
                    members = selectedMembers.toArray(new MemberInfo[selectedMembers.size()]);
                } else {
                    members = new MemberInfo[1];
                    Tree member = classTree.getMembers().get(position);
                    Element el = info.getTrees().getElement(new TreePath(classPath, member));
                    members[0] = MemberInfo.create(el, info);
                    members[0].setMakeAbstract(makeAbstract);
                }
                r[0] = new ExtractSuperclassRefactoring(TreePathHandle.create(classEl, info));
                r[0].setSuperClassName(newName);
                r[0].setMembers(members);
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
