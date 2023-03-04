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

package org.netbeans.modules.java.editor.codegen;

import com.sun.source.util.TreePath;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;

/**
 *
 * @author Jan Lahoda
 */
public class GeneratorUtilsTest extends NbTestCase {
    
    public GeneratorUtilsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
    }

    static void writeIntoFile(FileObject file, String what) throws Exception {
        FileLock lock = file.lock();
        OutputStream out = file.getOutputStream(lock);
        
        try {
            out.write(what.getBytes());
        } finally {
            out.close();
            lock.releaseLock();
        }
    }
    
    static String readFromFile(FileObject fo) throws Exception {
        InputStream in = fo.getInputStream();
        
        int s = (int)fo.getSize();
        byte[] arr = new byte[s];
        assertEquals("All read", s, in.read(arr));
        return new String(arr);
    }
    
    public void testImplementAllAbstractMethods1() throws Exception {
        performTest("package test;\npublic class Test implements Runnable {\npublic Test(){\n}\n }\n", 54, new RunnableValidator());
    }
    
    public static interface Validator {
        
        public void validate(CompilationInfo info);
        
    }
    
    private final class RunnableValidator implements Validator {
        
        public void validate(CompilationInfo info) {
            TypeElement test = info.getElements().getTypeElement("test.Test");
            
            boolean foundRunMethod = false;
            
            for (ExecutableElement ee : ElementFilter.methodsIn(test.getEnclosedElements())) {
                if ("run".equals(ee.getSimpleName().toString())) {
                    if (ee.getParameters().isEmpty()) {
                        assertFalse(foundRunMethod);
                        foundRunMethod = true;
                    }
                }
            }
            
            assertTrue(foundRunMethod);
        }
        
    }
    
    private final class SimpleFutureValidator extends FutureValidator {
        
        private String returnTypeName;
        
        public SimpleFutureValidator(String returnTypeName) {
            this.returnTypeName = returnTypeName;
        }
        
        protected TypeMirror returnType(CompilationInfo info) {
            TypeElement returnTypeElement = info.getElements().getTypeElement(returnTypeName);
            
            return returnTypeElement.asType();
        }
    }
    
    private abstract class FutureValidator implements Validator {
        
        protected abstract TypeMirror returnType(CompilationInfo info);

        public void validate(CompilationInfo info) {
            TypeElement test = info.getElements().getTypeElement("test.Test");
            TypeMirror returnType = returnType(info);
            
            boolean hasShortGet = false;
            boolean hasLongGet = false;
            
            for (ExecutableElement ee : ElementFilter.methodsIn(test.getEnclosedElements())) {
                if ("get".equals(ee.getSimpleName().toString())) {
                    if (ee.getParameters().isEmpty()) {
                        assertFalse(hasShortGet);
                        assertTrue(info.getTypes().isSameType(returnType, ee.getReturnType()));
                        hasShortGet = true;
                    }
                    if (ee.getParameters().size() == 2) {
                        assertFalse(hasLongGet);
                        assertTrue(info.getTypes().isSameType(returnType, ee.getReturnType()));
                        hasLongGet = true;
                    }
                }
            }
            
            assertTrue(hasShortGet);
            assertTrue(hasLongGet);
        }
        
    }
    
    private void performTest(String sourceCode, final int offset, final Validator validator) throws Exception {
        FileObject root = makeScratchDir(this);
        
        FileObject sourceDir = root.createFolder("src");
        FileObject buildDir = root.createFolder("build");
        FileObject cacheDir = root.createFolder("cache");
        
        FileObject source = sourceDir.createFolder("test").createData("Test.java");
        
        writeIntoFile(source, sourceCode);
        
        SourceUtilsTestUtil.prepareTest(sourceDir, buildDir, cacheDir, new FileObject[0]);
        
        JavaSource js = JavaSource.forFileObject(source);
        
        ModificationResult result = js.runModificationTask(new Task<WorkingCopy>() {

            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(Phase.RESOLVED);
                TreePath tp = copy.getTreeUtilities().pathFor(offset);
                GeneratorUtils.generateAllAbstractMethodImplementations(copy, tp);
            }
        });
        
        result.commit();
        
        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                System.err.println("text:");
                System.err.println(controller.getText());
                controller.toPhase(Phase.RESOLVED);
                
                assertEquals(controller.getDiagnostics().toString(), 0, controller.getDiagnostics().size());
                
                validator.validate(controller);
            }
        }, true);
    }
    
    /**Copied from org.netbeans.api.project.
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() && root.list().length == 0;
        FileObject fo = FileUtil.toFileObject(root);
        if (fo != null) {
            // Presumably using masterfs.
            return fo;
        } else {
            // For the benefit of those not using masterfs.
            LocalFileSystem lfs = new LocalFileSystem();
            try {
                lfs.setRootDirectory(root);
            } catch (PropertyVetoException e) {
                assert false : e;
            }
            Repository.getDefault().addFileSystem(lfs);
            return lfs.getRoot();
        }
    }
    
}
