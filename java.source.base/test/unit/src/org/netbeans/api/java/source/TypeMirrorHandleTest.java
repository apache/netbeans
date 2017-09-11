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

import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePathScanner;
import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class TypeMirrorHandleTest extends NbTestCase {
    
    private FileObject testSource;
    
    public TypeMirrorHandleTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        this.clearWorkDir();
        File workDir = getWorkDir();
        File cacheFolder = new File (workDir, "cache"); //NOI18N
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
    }

    private TypeMirror parse(CompilationInfo info, String name) {
        TypeElement string = info.getElements().getTypeElement("test.Test");
        
        assertNotNull(string);
        
        return info.getTreeUtilities().parseType(name, string);
    }
    
    //TODO: cannot handle wildcards, as Types.isSameType returns false for wildcards:
    private void testCase(CompilationInfo info, String name) {
        TypeMirror tm = parse(info, name);
        TypeMirrorHandle th = TypeMirrorHandle.create(tm);
        
        assertTrue(info.getTypes().isSameType(th.resolve(info), tm));
        assertTrue(info.getTypes().isSameType(tm, th.resolve(info)));
    }
    
    private void testCaseEnum(CompilationInfo info) {
        TypeElement te = info.getElements().getTypeElement("java.util.EnumSet");
        
        assertNotNull(te);
        
        TypeMirror tm = te.getTypeParameters().get(0).getBounds().get(0);
        TypeMirrorHandle th = TypeMirrorHandle.create(tm);
        
        assertTrue(info.getTypes().isSameType(th.resolve(info), tm));
        assertTrue(info.getTypes().isSameType(tm, th.resolve(info)));
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
    
    private void prepareTest() throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        
        testSource = fs.getRoot().createData("Test.java");
        assertNotNull(testSource);
    }
    
    public void testTypeMirrorHandle() throws Exception {
        prepareTest();
        writeIntoFile(testSource, "package test; public class Test<T> {}");
        ClassPath empty = ClassPathSupport.createClassPath(new URL[0]);
        JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0])), empty, empty), testSource);
        
        js.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController info) throws Exception {
                info.toPhase(Phase.RESOLVED);
                testCase(info, "java.util.Map");
                testCase(info, "java.util.Map<java.lang.Object, java.util.List>");
                testCase(info, "java.util.Map<java.lang.Object, java.util.List<java.lang.String>>");
                testCase(info, "int[]");
//                testCaseEnum(info); IZ #111876.
            }
        }, true);
    }

    public void testTypeMirrorHandleCannotResolve() throws Exception {
        prepareTest();
        writeIntoFile(testSource, "package test; public class Test {} class Test1{}");
        ClassPath empty = ClassPathSupport.createClassPath(new URL[0]);
        JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0])), empty, empty), testSource);
        final List<TypeMirrorHandle> handles = new ArrayList<TypeMirrorHandle>();
        
        js.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController info) throws Exception {
                info.toPhase(Phase.RESOLVED);
                handles.add(TypeMirrorHandle.create(parse(info, "test.Test1")));
                handles.add(TypeMirrorHandle.create(parse(info, "java.util.List<test.Test1>")));
                handles.add(TypeMirrorHandle.create(parse(info, "test.Test1[]")));
            }
        }, true);
        writeIntoFile(testSource, "package test; public class Test {}");
        js.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController info) throws Exception {
                info.toPhase(Phase.RESOLVED);
                
                int count = 0;
                
                for (TypeMirrorHandle h : handles) {
                    assertNull(String.valueOf(count++), h.resolve(info));
                }
            }
        }, true);
    }

    public void testTypeMirrorHandle196070() throws Exception {
        prepareTest();
        writeIntoFile(testSource, "package test; public class Test<T extends IA & IB> {} interface IA {} interface IB {}");
        ClassPath empty = ClassPathSupport.createClassPath(new URL[0]);
        JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0])), empty, empty), testSource);
        final boolean[] finished = new boolean[1];

        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController info) throws Exception {
                info.toPhase(Phase.RESOLVED);
                TypeMirror tm = info.getTreeUtilities().parseType("T", info.getTopLevelElements().get(0));
                assertTrue(info.getTypes().isSameType(tm, TypeMirrorHandle.create(tm).resolve(info)));
                finished[0] = true;
            }
        }, true);

        assertTrue(finished[0]);
    }
    
    public void testTypeMirrorHandleUnion() throws Exception {
        prepareTest();
        writeIntoFile(testSource, "package test; public class Test { void t() { try { throw new Exception(); } catch (java.io.IOException | javax.swing.text.BadLocationException e) { } } }");
        ClassPath empty = ClassPathSupport.createClassPath(new URL[0]);
        JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0])), empty, empty), testSource);

        js.runUserActionTask(new Task<CompilationController>() {

            public void run(final CompilationController info) throws Exception {
                info.toPhase(Phase.RESOLVED);
                new TreePathScanner<Void, Void>() {
                    @Override public Void visitVariable(VariableTree node, Void p) {
                        if (node.getName().contentEquals("e")) {
                            TypeMirror tm = info.getTrees().getTypeMirror(getCurrentPath());

                            assertEquals(TypeKind.UNION, tm.getKind());

                            assertTrue(info.getTypes().isSameType(tm, TypeMirrorHandle.create(tm).resolve(info)));
                        }
                        return super.visitVariable(node, p);
                    }
                }.scan(info.getCompilationUnit(), null);
            }
        }, true);
    }
}
