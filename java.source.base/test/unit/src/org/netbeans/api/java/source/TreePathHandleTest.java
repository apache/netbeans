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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.File;
import java.io.OutputStream;
import java.security.Permission;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class TreePathHandleTest extends NbTestCase {
    
    public TreePathHandleTest(String testName) {
        super(testName);
    }
    
    private FileObject sourceRoot;
    
    protected void setUp() throws Exception {
        clearWorkDir();
        
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        
        File work = FileUtil.normalizeFile(getWorkDir());
        FileObject workFO = FileUtil.toFileObject(work);
        
        assertNotNull(workFO);
        
        sourceRoot = workFO.createFolder("src");
        
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
    }
    
    private void writeIntoFile(FileObject file, String what) throws Exception {
        FileLock lock = file.lock();
        OutputStream out = file.getOutputStream(lock);
        
        try {
            out.write(what.getBytes());
        } finally {
            out.close();
            lock.releaseLock();
        }
    }

    public void testHandleForMethodInvocation() throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, "test/test.java");
        
        writeIntoFile(file, "package test; public class test {public test() {aaa();} public void aaa() {}}");
        
        JavaSource js = JavaSource.forFileObject(file);
        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        assertTrue(info.getDiagnostics().toString(), info.getDiagnostics().isEmpty());
        
        TreePath       tp       = info.getTreeUtilities().pathFor(49);
        TreePathHandle handle   = TreePathHandle.create(tp, info);
        TreePath       resolved = handle.resolve(info);
        
        assertNotNull(resolved);
        
        assertTrue(tp.getLeaf() == resolved.getLeaf());
    }
        
    public void testHandleForNewClass() throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, "test/test.java");
        
        writeIntoFile(file, "package test; public class test {public test() {new Runnable() {public void run() {}};}}");
        
        JavaSource js = JavaSource.forFileObject(file);
        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        assertTrue(info.getDiagnostics().toString(), info.getDiagnostics().isEmpty());
        
        TreePath       tp       = info.getTreeUtilities().pathFor(55).getParentPath();
        TreePathHandle handle   = TreePathHandle.create(tp, info);
        TreePath       resolved = handle.resolve(info);
        
        assertNotNull(resolved);
        
        assertTrue(tp.getLeaf() == resolved.getLeaf());
    }

    public void test126732() throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, "test/test.java");
        String code = "package test;\n" +
                      "public class Test {\n" +
                      "    public static void test() {\n" +
                      "        return Runnable() {\n" +
                      "                new Runnable() {\n" +
                      "        };\n" +
                      "    }\n" +
                      "}";

        writeIntoFile(file,code);

        JavaSource js = JavaSource.forFileObject(file);
        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);

        TreePath       tp       = info.getTreeUtilities().pathFor(code.indexOf("new Runnable() {"));
        TreePathHandle handle   = TreePathHandle.create(tp, info);
        TreePath       resolved = handle.resolve(info);

        assertNotNull(resolved);

        assertTrue(tp.getLeaf() == resolved.getLeaf());
    }

    public void test134457() throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, "test/Test.java");
        String code = "package test;\n" +
                      "public class Test {\n" +
                      "    public static final String KONST = \"\";\n" +
                      "    public static void test() {\n" +
                      "        Test test = new Test();\n" +
                      "        test.KONST;\n" +
                      "    }\n" +
                      "}";
        
        writeIntoFile(file,code);
        
        JavaSource js = JavaSource.forFileObject(file);
        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        TreePath       tp       = info.getTreeUtilities().pathFor(code.indexOf("ONST;"));
        TreePathHandle handle   = TreePathHandle.create(tp, info);
        TreePath       resolved = handle.resolve(info);
        
        assertNotNull(resolved);
        
        assertTrue(tp.getLeaf() == resolved.getLeaf());
    }
    
    public void testTreePathIsNotParsing() throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, "test/test.java");
        
        writeIntoFile(file, "package test; public class test {}");
        writeIntoFile(FileUtil.createData(sourceRoot, "test/test2.java"), "package test; public class test2 {}");
        
        JavaSource js = JavaSource.forFileObject(file);
        
        SourceUtilsTestUtil.compileRecursively(sourceRoot);
        
        js.runUserActionTask(new  Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                
                TypeElement string = parameter.getElements().getTypeElement("test.test2");
                
                SecurityManager old = System.getSecurityManager();
                
                System.setSecurityManager(new SecMan());
                
                TreePathHandle.create(string, parameter);
                
                System.setSecurityManager(old);
            }
        }, true);
    }
    
    public void testEquals() throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, "test/test.java");
        final String code = "package test; public class test {}";

        writeIntoFile(file, code);

        JavaSource js = JavaSource.forFileObject(file);

        js.runUserActionTask(new  Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                
                TreePath tp = parameter.getTreeUtilities().pathFor(code.indexOf("{}") + 1);
                TreePathHandle handle = TreePathHandle.create(tp, parameter);

                assertFalse(handle.equals(null));
                assertFalse(handle.equals((Object) ""));
                assertTrue(handle.equals(handle));

            }
        }, true);
    }

    public void testEmptyMod() throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, "test/test.java");

        writeIntoFile(file, "package test; public class test { String a; }");

        JavaSource js = JavaSource.forFileObject(file);

        SourceUtilsTestUtil.compileRecursively(sourceRoot);

        js.runUserActionTask(new  Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);

                TypeElement test = parameter.getElements().getTypeElement("test.test");

                TreePath path  = parameter.getTrees().getPath(test);
                TreePath field = new TreePath(path, ((ClassTree) path.getLeaf()).getMembers().get(1));
                TreePath mods  = new TreePath(field, ((VariableTree) field.getLeaf()).getModifiers());

                assertSame(mods.getLeaf(), TreePathHandle.create(mods, parameter).resolve(parameter).getLeaf());
            }
        }, true);
    }

    public void testWithoutClassPath150650() throws Exception {
        FileObject file = FileUtil.createData(sourceRoot.getParent(), "aux/test/test.java");

        writeIntoFile(file, "package test; public class test { }");

        JavaSource js = JavaSource.forFileObject(file);

        js.runUserActionTask(new  Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);

                TypeElement test = parameter.getElements().getTypeElement("test.test");

                assertNotNull(test);
                
                TreePathHandle h = TreePathHandle.create(test, parameter);

                assertEquals(parameter.getFileObject(), h.getFileObject());
            }
        }, true);
    }

    public void testClassWithoutSource150650() throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, "test/test.java");

        writeIntoFile(file, "package test; public class test { }");

        JavaSource js = JavaSource.forFileObject(file);

        js.runUserActionTask(new  Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);

                TypeElement jlObject = parameter.getElements().getTypeElement("java.lang.Object");

                assertNotNull(jlObject);

                TreePathHandle h = TreePathHandle.create(jlObject, parameter);
                FileObject file = h.getFileObject();

                assertNotNull(file);
                assertEquals("Object.class", file.getNameExt());
            }
        }, true);
    }

    public void test190101() throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, "test/test.java");
        String code = "package test;\n" +
                      "//public class Test {\n" +
                      "    public static void test() {\n" +
                      "        return Runnable() {\n" +
                      "                new Runnable() {\n" +
                      "        };\n" +
                      "    }\n" +
                      "}";

        writeIntoFile(file,code);

        JavaSource js = JavaSource.forFileObject(file);
        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);

        TreePath       tp       = info.getTreeUtilities().pathFor(code.indexOf("public static") - 1);

        assertEquals(Kind.COMPILATION_UNIT, tp.getLeaf().getKind());

        tp = new TreePath(tp, info.getCompilationUnit().getTypeDecls().get(0));
        
        TreePathHandle handle   = TreePathHandle.create(tp, info);
        TreePath       resolved = handle.resolve(info);

        assertNotNull(resolved);

        assertTrue(tp.getLeaf() == resolved.getLeaf());
    }

    public void testFromElementHandle() throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, "test/test.java");
        String code = "package test;\n" +
                      "public class Test {\n" +
                      "    public static void test() {\n" +
                      "    }\n" +
                      "}";

        writeIntoFile(file,code);

        JavaSource js = JavaSource.forFileObject(file);
        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);

        ClassTree clazz = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        MethodTree method = (MethodTree) clazz.getMembers().get(1);
        TreePath tp = TreePath.getPath(info.getCompilationUnit(), method);
        Element el = info.getTrees().getElement(tp);
        ElementHandle<?> elHandle = ElementHandle.create(el);
        TreePathHandle handle   = TreePathHandle.from(elHandle, info.getClasspathInfo());
        TreePath       resolved = handle.resolve(info);

        assertNotNull(resolved);

        assertTrue(tp.getLeaf() == resolved.getLeaf());
        assertTrue(handle.getElementHandle().equals(elHandle));
    }
    
    public void testResolveToCorrectPath() throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, "test/test.java");
        String code = "package test;\n" +
                      "public class Test {\n" +
                      "    public static String test() {\n" +
                      "        return A.test();\n" +
                      "    }\n" +
                      "}";

        writeIntoFile(file,code);
        writeIntoFile(FileUtil.createData(sourceRoot, "test/A.java"),
                      "package test;\n" +
                      "public class A    {\n" +
                      "    public static String test() {\n" +
                      "        return A.toString();\n" +
                      "    }\n" +
                      "\n");

        SourceUtilsTestUtil.compileRecursively(sourceRoot);
        
        JavaSource js = JavaSource.forFileObject(file);
        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);

        TreePath tp = info.getTreeUtilities().pathFor(code.indexOf("A.test") + 3);
        TreePathHandle handle   = TreePathHandle.create(tp, info);
        TreePath       resolved = handle.resolve(info);

        assertNotNull(resolved);

        assertTrue(tp.getLeaf() == resolved.getLeaf());
    }
    
    public void testResolveElement() throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, "test/test.java");
        String code = "package test;\n" +
                      "import static java.lang.Math.min;\n" +
                      "public class Test {\n" +
                      "}";

        writeIntoFile(file,code);

        SourceUtilsTestUtil.compileRecursively(sourceRoot);
        
        JavaSource js = JavaSource.forFileObject(file);
        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);

        TreePath tp = info.getTreeUtilities().pathFor(code.indexOf("min") + 1);
        TreePathHandle handle   = TreePathHandle.create(tp, info);
        TreePath       resolved = handle.resolve(info);

        assertNotNull(resolved);

        assertTrue(tp.getLeaf() == resolved.getLeaf());
        assertNotNull(handle.resolveElement(info));
    }
    
    public void testLocVar() throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, "test/test.java");
        String code = "package test;\n" +
                      "public class Test {\n" +
                      "    private void test() {\n" +
                      "        int aa;\n" +
                      "    }\n" +
                      "}";

        writeIntoFile(file,code);

        SourceUtilsTestUtil.compileRecursively(sourceRoot);
        
        JavaSource js = JavaSource.forFileObject(file);
        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);

        TreePath tp = info.getTreeUtilities().pathFor(code.indexOf("aa") + 1);
        TreePathHandle handle   = TreePathHandle.create(tp, info);
        TreePath       resolved = handle.resolve(info);

        assertNotNull(resolved);

        assertTrue(tp.getLeaf() == resolved.getLeaf());
        assertNotNull(handle.resolveElement(info));
    }

    private static final class SecMan extends SecurityManager {

        @Override
        public void checkRead(String file) {
            assertFalse(file.endsWith("test2.java"));
        }

        @Override
        public void checkRead(String file, Object context) {
            assertFalse(file.endsWith("test2.java"));
        }
        
        @Override
        public void checkPermission(Permission perm) {
        }

        @Override
        public void checkPermission(Permission perm, Object context) {
        }

    }
}
