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

package org.netbeans.api.java.source;

import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.VariableTree;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
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

    //disabled, because Types.isSameType cannot compare different instances of TypeVars anymore:
    public void DISABLEDtestTypeMirrorHandle196070() throws Exception {
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
                new ErrorAwareTreePathScanner<Void, Void>() {
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

    public void testTypeMirrorHandleErrorType() throws Exception {
        prepareTest();
        writeIntoFile(testSource, "package test; public class Test { void t() { new T(); } }");
        ClassPath empty = ClassPathSupport.createClassPath(new URL[0]);
        JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0])), empty, empty), testSource);

        js.runUserActionTask(new Task<CompilationController>() {

            public void run(final CompilationController info) throws Exception {
                info.toPhase(Phase.RESOLVED);
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitNewClass(NewClassTree node, Void p) {
                        TypeMirror tm = info.getTrees().getTypeMirror(getCurrentPath());
                        assertEquals(TypeKind.ERROR, tm.getKind());
                        assertTrue(info.getTypes().isSameType(tm, TypeMirrorHandle.create(tm).resolve(info)));
                        return super.visitNewClass(node, p);
                    }
                }.scan(info.getCompilationUnit(), null);
            }
        }, true);
    }
}
