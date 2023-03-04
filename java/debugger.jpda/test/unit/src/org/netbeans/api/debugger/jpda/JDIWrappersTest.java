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

package org.netbeans.api.debugger.jpda;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import com.sun.source.util.Trees;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test that all JDI calls are wrapped.
 * 
 * @author Martin Entlicher
 */
public class JDIWrappersTest extends NbTestCase {

    public JDIWrappersTest (String s) {
        super (s);
    }

    public void testWrappers() throws Exception {
        java.io.File srcFile = new java.io.File("src");
        System.out.println("SRC = "+srcFile.getCanonicalPath());
        System.out.println("WorkDir = "+getWorkDirPath());
        //System.out.println("Java CP = "+System.getProperty("java.class.path"));
        //System.out.println("Java Library CP = "+System.getProperty("java.library.path"));
        FileObject src = FileUtil.toFileObject(getWorkDir());
        while (src != null && src.getFileObject("src") == null) {
            src = src.getParent();
        }
        assertNotNull(src);
        src = src.getFileObject("src");
        assertNotNull(src);
        if (!testWrappersRecursively(src, src)) {
            fail("Some JDI invocations were found.");
        }
    }

    private boolean isIgnored(FileObject root, FileObject c) {
        String relPath = FileUtil.getRelativePath(root, c);
        if (relPath.startsWith("org/netbeans/modules/debugger/jpda/jdi")) {
            return true;
        }
        if (relPath.equals("org/netbeans/modules/debugger/jpda/expr/EvaluatorVisitor.java")) {
            return true;
        }
        return false;
    }

    private boolean testWrappersRecursively(FileObject root, FileObject fo) throws Exception {
        boolean status = true;
        FileObject[] ch = fo.getChildren();
        for (FileObject c : ch) {
            if (c.isData() && !isIgnored(root, c)) {
                status = testWrappers(c) && status;
            } else {
                status = testWrappersRecursively(root, c) && status;
            }
        }
        return status;
    }

    private boolean testWrappers(FileObject fo) throws Exception {
        System.out.println("testWrappers("+fo+")");
        ClassPath bootPath = ClassPath.getClassPath(fo, ClassPath.BOOT);
        if (bootPath == null) {
            bootPath = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        }
        ClassPath srcPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        ClassPath compilePath = ClassPathSupport.createClassPath(System.getProperty("java.class.path"));
        ClasspathInfo cpi = ClasspathInfo.create(bootPath, compilePath, srcPath);
        JavaSource source = JavaSource.create(cpi, fo);//forFileObject(fo);
        if (source == null) {
            return true;
        }
        final boolean[] successPtr = new boolean[] { true };
        source.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController ci) throws Exception {
                if (ci.toPhase(Phase.UP_TO_DATE).compareTo(Phase.UP_TO_DATE) < 0) {
                    fail("Unable to resolve "+ci.getFileObject()+" to phase "+Phase.UP_TO_DATE+", current phase = "+ci.getPhase()+
                         "\nDiagnostics = "+ci.getDiagnostics()+
                         "\nFree memory = "+Runtime.getRuntime().freeMemory());
                    return;
                }
                List<? extends TypeElement> topElements = ci.getTopLevelElements();
                for (TypeElement el : topElements) {
                    ClassTree ct = ci.getTrees().getTree(el);
                    JDICallsScanner scanner =
                            new JDICallsScanner(ci.getTrees(), ci.getTypes(), ci.getElements(), ci.getCompilationUnit());
                    ct.accept(scanner, null);
                    successPtr[0] = scanner.isFailed() && successPtr[0];
                }
            }
        }, true);
        return successPtr[0];
    }

    private static class JDICallsScanner extends ErrorAwareTreeScanner<Void, Object> {

        private Trees trees;
        private Types types;
        private Elements elements;
        private CompilationUnitTree cut;
        private boolean failure = false;

        JDICallsScanner(Trees trees, Types types, Elements elements, CompilationUnitTree cut) {
            this.trees = trees;
            this.types = types;
            this.elements = elements;
            this.cut = cut;
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Object p) {
            ExpressionTree expr = node.getMethodSelect();
            if (expr.getKind() == Tree.Kind.MEMBER_SELECT) {
                MemberSelectTree mst = (MemberSelectTree) expr;
                //Object object = mst.getExpression().accept(this, p);
                Element expEl = getElement(mst.getExpression());
                String type = null;
                if (expEl == null) {
                    TreePath expPath = TreePath.getPath(cut, mst.getExpression());
                    TypeMirror tm = null;
                    try {
                        tm = trees.getTypeMirror(expPath);
                    } catch (IllegalArgumentException iaex) {}
                    if (tm != null) {
                        type = tm.toString();
                    }
                    /*
                    if (mst.getExpression().getKind() == Tree.Kind.STRING_LITERAL) {
                        // Skip Strings
                    } else {
                        System.err.println("Null element for '"+mst.getExpression()+"' in "+node);
                    }
                     */
                } else {
                    TypeMirror tm = expEl.asType();
                    tm = adjustTypeMirror(tm);
                    Element typeEl = types.asElement(tm);
                    if (typeEl instanceof TypeElement) {
                        type = ((TypeElement) typeEl).getQualifiedName().toString();
                    } else {
                        // Get it from the tree:
                        TreePath expPath = TreePath.getPath(cut, mst.getExpression());
                        tm = null;
                        try {
                            tm = trees.getTypeMirror(expPath);
                        } catch (IllegalArgumentException iaex) {}
                        if (tm != null) {
                            type = tm.toString();
                        }

                        /*
                        String typeStr = expEl.asType().toString();
                        TypeElement typeElm = elements.getTypeElement(typeStr);
                        if (typeElm == null) {
                            System.err.println("NULL type element for '"+typeStr+"'");
                        } else {
                            String binaryType = ElementUtilities.getBinaryName(typeElm);
                            System.err.println("\nBinary type = "+binaryType);
                        }

                        //ElementUtilities.getBinaryName(expEl);
                        System.err.println("  Type element "+typeEl+" for "+expEl+" of TypeMirror: "+expEl.asType()+"\n");
                        type = expEl.asType().toString();
                        TreePath expPath = TreePath.getPath(cut, mst.getExpression());
                        tm = null;
                        try {
                            tm = trees.getTypeMirror(expPath);
                        } catch (IllegalArgumentException iaex) {}
                        if (tm != null) {
                            System.err.println("  Tree Type Mirror = '"+tm.toString()+"'");
                        } else {
                            System.err.println("  Tree Type Mirror = null. :-(");
                        }
                         */
                    }
                }
                if (type == null) {
                    System.err.println("Unknown type for '"+mst.getExpression()+"' in "+node);
                } else if (isJDIType(type)) {
                    long offset = trees.getSourcePositions().getStartPosition(cut, node);
                    long line = cut.getLineMap().getLineNumber(offset);
                    //int line = NbDocument.findLineNumber(doc, offset);
                    System.err.println("Method "+mst.getIdentifier().toString()+" is invoked on "+type+" in MethodInvocationTree '"+node+"' in file "+cut.getSourceFile()+", line = "+line);
                    failure = true;
                }
            }
            return super.visitMethodInvocation(node, p);
        }

        private Element getElement(Tree tree) {
            TreePath expPath = TreePath.getPath(cut, tree);
            Element e = trees.getElement(expPath);
            if (e == null) {
                if (tree instanceof ParenthesizedTree) {
                    e = getElement(((ParenthesizedTree) tree).getExpression());
                    //if (e == null) {
                    //    System.err.println("Have null element for "+tree);
                    //}
                    //System.err.println("\nHAVE "+e.asType().toString()+" for ParenthesizedTree "+tree);
                }
                else if (tree instanceof TypeCastTree) {
                    e = getElement(((TypeCastTree) tree).getType());
                    //if (e == null) {
                    //    System.err.println("Have null element for "+tree);
                    //}
                    //System.err.println("\nHAVE "+e.asType().toString()+" for TypeCastTree "+tree);
                }
                else if (tree instanceof AssignmentTree) {
                    e = getElement(((AssignmentTree) tree).getVariable());
                }
                else if (tree instanceof ArrayAccessTree) {
                    e = getElement(((ArrayAccessTree) tree).getExpression());
                    if (e != null) {
                        TypeMirror tm = e.asType();
                        if (tm.getKind() == TypeKind.ARRAY) {
                            tm = ((ArrayType) tm).getComponentType();
                            e = types.asElement(tm);
                        }
                    }
                    //System.err.println("ArrayAccessTree = "+((ArrayAccessTree) tree).getExpression()+", element = "+getElement(((ArrayAccessTree) tree).getExpression())+", type = "+getElement(((ArrayAccessTree) tree).getExpression()).asType());
                }
            }
            return e;
        }

        private TypeMirror adjustTypeMirror(TypeMirror tm) {
            if (tm.getKind() == TypeKind.EXECUTABLE) {
                tm = ((ExecutableType) tm).getReturnType();
                tm = adjustTypeMirror(tm);
            } else if (tm.getKind() == TypeKind.ARRAY) {
                tm = ((ArrayType) tm).getComponentType();
                tm = adjustTypeMirror(tm);
            }
            return tm;
        }

        public boolean isFailed() {
            return failure;
        }

        private boolean isJDIType(String type) {
            boolean isJDI = type.startsWith("com.sun.jdi");
            if (isJDI) {
                if (type.endsWith("InternalException")) {
                    return false;
                }
            } else {
                //System.out.println("Not JDI type: '"+type+"'");
            }
            return isJDI;
        }

    }
}
